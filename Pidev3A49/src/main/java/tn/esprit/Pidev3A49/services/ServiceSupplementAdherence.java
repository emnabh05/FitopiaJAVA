package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.utils.MyDataBase;
import tn.esprit.Pidev3A49.utils.SchemaInitializer;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ServiceSupplementAdherence {

    private static final double AT_RISK_THRESHOLD = 70.0;

    private final Connection cnx;

    public ServiceSupplementAdherence() {
        cnx = MyDataBase.getInstance().getCnx();
        if (cnx == null) {
            throw new IllegalStateException("Impossible de se connecter a MySQL.");
        }
    }

    public ProgressDashboard getProgressDashboard(String email, LocalDate today) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return ProgressDashboard.empty();
        }

        LocalDate safeToday = today == null ? LocalDate.now() : today;
        synchronizePlansFromOrders();
        return buildProgressDashboard(normalizedEmail, safeToday);
    }

    public LogResult markTakenToday(String email, int planId, LocalDate today) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return new LogResult(false, "Connectez-vous pour enregistrer vos prises.", true, 0);
        }
        if (planId <= 0) {
            return new LogResult(false, "Supplement de suivi introuvable.", true, 0);
        }

        LocalDate safeToday = today == null ? LocalDate.now() : today;
        synchronizePlansFromOrders();
        PlanRecord plan = fetchPlanByIdForEmail(planId, normalizedEmail);
        if (plan == null) {
            return new LogResult(false, "Plan introuvable pour cet utilisateur.", true, 0);
        }

        LocalDate expectedEndDate = plan.expectedEndDate();
        if (safeToday.isBefore(plan.startDate())) {
            return new LogResult(false, "Votre plan commence le " + plan.startDate() + ".", true, 0);
        }
        if (safeToday.isAfter(expectedEndDate)) {
            return new LogResult(false, "Ce plan est termine depuis le " + expectedEndDate + ".", true, 0);
        }

        int unitsToday = countLoggedUnitsForPlanDate(plan.id(), safeToday);
        if (unitsToday >= plan.dailyTargetUnits()) {
            String safetyMessage = "Daily plan limit reached. More is not always better.";
            saveNotification(normalizedEmail, "SAFETY", safetyMessage);
            return new LogResult(false, safetyMessage, true, 0);
        }

        String insertQuery = """
                INSERT INTO %s (plan_id, user_email, supplement_id, log_date, taken_units)
                VALUES (?, ?, ?, ?, 1)
                """.formatted(SchemaInitializer.SUPPLEMENT_ADHERENCE_LOG_TABLE);

        try (PreparedStatement statement = cnx.prepareStatement(insertQuery)) {
            statement.setInt(1, plan.id());
            statement.setString(2, normalizedEmail);
            statement.setInt(3, plan.supplementId());
            statement.setDate(4, Date.valueOf(safeToday));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'enregistrer la prise du supplement.", exception);
        }

        ProgressDashboard dashboard = buildProgressDashboard(normalizedEmail, safeToday);
        SupplementProgressItem updatedItem = dashboard.supplementItems().stream()
                .filter(item -> item.planId() == plan.id())
                .findFirst()
                .orElse(null);

        int streak = updatedItem == null ? 0 : updatedItem.currentStreakDays();
        String message = streak > 0
                ? "You're on a " + streak + "-day streak " + "\uD83D\uDD25"
                : "Supplement logged for today.";
        saveNotification(normalizedEmail, "STREAK", message);
        return new LogResult(true, message, false, streak);
    }

    public LeaderboardResult getLeaderboard(YearMonth month, String currentUserEmail) {
        YearMonth safeMonth = month == null ? YearMonth.now() : month;
        LocalDate monthStart = safeMonth.atDay(1);
        LocalDate monthEnd = safeMonth.atEndOfMonth();
        LocalDate evaluationDate = monthEnd.isAfter(LocalDate.now()) ? LocalDate.now() : monthEnd;

        synchronizePlansFromOrders();
        List<PlanRecord> plans = fetchAllActivePlans();
        if (plans.isEmpty()) {
            return new LeaderboardResult(safeMonth, List.of(), List.of());
        }

        LocalDate globalStart = plans.stream()
                .map(PlanRecord::startDate)
                .min(LocalDate::compareTo)
                .orElse(monthStart);
        Map<Integer, Map<LocalDate, Integer>> unitsByPlan =
                loadLoggedUnitsByPlanIds(plans.stream().map(PlanRecord::id).collect(Collectors.toSet()), globalStart, evaluationDate);

        Map<String, String> userNames = resolveUserDisplayNames();
        Map<String, List<PlanRecord>> plansByUser = plans.stream()
                .collect(Collectors.groupingBy(plan -> plan.userEmail().toLowerCase(Locale.ROOT)));

        List<LeaderboardEntry> entries = new ArrayList<>();
        for (Map.Entry<String, List<PlanRecord>> groupedEntry : plansByUser.entrySet()) {
            String userEmail = groupedEntry.getKey();
            List<PlanRecord> userPlans = groupedEntry.getValue();

            int longestActiveStreak = 0;
            int totalMonthExpectedDays = 0;
            int totalMonthAdherentDays = 0;
            double goalCompletionAccumulator = 0.0;
            int goalCompletionCount = 0;

            for (PlanRecord plan : userPlans) {
                Map<LocalDate, Integer> dayUnits = unitsByPlan.getOrDefault(plan.id(), Map.of());
                int currentStreak = computeCurrentStreak(plan, dayUnits, evaluationDate);
                longestActiveStreak = Math.max(longestActiveStreak, currentStreak);

                int monthExpected = countExpectedDays(plan, monthStart, evaluationDate);
                int monthAdherent = countAdherentDays(plan, dayUnits, monthStart, evaluationDate);
                totalMonthExpectedDays += monthExpected;
                totalMonthAdherentDays += monthAdherent;

                double goalCompletion = computeGoalCompletionPercent(plan, dayUnits, evaluationDate);
                goalCompletionAccumulator += goalCompletion;
                goalCompletionCount++;
            }

            double consistency = percentage(totalMonthAdherentDays, totalMonthExpectedDays);
            double goalCompletion = goalCompletionCount == 0 ? 0.0 : goalCompletionAccumulator / goalCompletionCount;
            String displayName = resolveDisplayName(userNames, userEmail);

            entries.add(new LeaderboardEntry(
                    0,
                    displayName,
                    userEmail,
                    longestActiveStreak,
                    round(consistency),
                    round(goalCompletion)
            ));
        }

        entries.sort(Comparator
                .comparingInt(LeaderboardEntry::longestActiveStreak).reversed()
                .thenComparing(LeaderboardEntry::consistencyPercent, Comparator.reverseOrder())
                .thenComparing(LeaderboardEntry::goalCompletionPercent, Comparator.reverseOrder())
                .thenComparing(LeaderboardEntry::displayName, String.CASE_INSENSITIVE_ORDER));

        List<LeaderboardEntry> rankedEntries = new ArrayList<>();
        for (int index = 0; index < entries.size(); index++) {
            LeaderboardEntry entry = entries.get(index);
            rankedEntries.add(new LeaderboardEntry(
                    index + 1,
                    entry.displayName(),
                    entry.email(),
                    entry.longestActiveStreak(),
                    entry.consistencyPercent(),
                    entry.goalCompletionPercent()
            ));
        }

        List<String> notifications = buildLeaderboardNotifications(rankedEntries, currentUserEmail);
        return new LeaderboardResult(safeMonth, rankedEntries, notifications);
    }

    private ProgressDashboard buildProgressDashboard(String email, LocalDate today) {
        List<PlanRecord> plans = fetchActivePlansForEmail(email);
        if (plans.isEmpty()) {
            return ProgressDashboard.empty();
        }

        LocalDate globalStart = plans.stream()
                .map(PlanRecord::startDate)
                .min(LocalDate::compareTo)
                .orElse(today);
        Map<Integer, Map<LocalDate, Integer>> unitsByPlan =
                loadLoggedUnitsByPlanIds(plans.stream().map(PlanRecord::id).collect(Collectors.toSet()), globalStart, today);

        List<SupplementProgressItem> items = new ArrayList<>();
        for (PlanRecord plan : plans) {
            Map<LocalDate, Integer> dayUnits = unitsByPlan.getOrDefault(plan.id(), Map.of());
            items.add(buildProgressItem(plan, dayUnits, today));
        }

        items.sort(Comparator
                .comparing(SupplementProgressItem::atRisk).reversed()
                .thenComparing(SupplementProgressItem::loggedToday)
                .thenComparing(SupplementProgressItem::monthlyConsistencyPercent, Comparator.reverseOrder()));

        int trackedSupplements = items.size();
        int atRiskSupplements = (int) items.stream().filter(SupplementProgressItem::atRisk).count();
        int currentMaxStreak = items.stream().mapToInt(SupplementProgressItem::currentStreakDays).max().orElse(0);
        double averageCompliance = items.stream()
                .mapToDouble(SupplementProgressItem::monthlyConsistencyPercent)
                .average()
                .orElse(0.0);
        double averageGoalCompletion = items.stream()
                .mapToDouble(SupplementProgressItem::goalCompletionPercent)
                .average()
                .orElse(0.0);

        List<String> notifications = new ArrayList<>(buildProgressNotifications(items, currentMaxStreak, atRiskSupplements));
        notifications.addAll(fetchRecentNotifications(email, 3));

        return new ProgressDashboard(
                trackedSupplements,
                round(averageCompliance),
                currentMaxStreak,
                atRiskSupplements,
                round(averageGoalCompletion),
                items,
                deduplicate(notifications)
        );
    }

    private SupplementProgressItem buildProgressItem(PlanRecord plan, Map<LocalDate, Integer> dayUnits, LocalDate today) {
        int currentStreak = computeCurrentStreak(plan, dayUnits, today);
        int bestStreak = computeBestStreak(plan, dayUnits, today);
        double weeklyConsistency = computeConsistencyPercent(plan, dayUnits, today.minusDays(6), today);
        double monthlyConsistency = computeConsistencyPercent(plan, dayUnits, today.withDayOfMonth(1), today);
        double goalCompletion = computeGoalCompletionPercent(plan, dayUnits, today);

        LocalDate expectedEndDate = plan.expectedEndDate();
        LocalDate estimatedResultDate = estimateResultDate(plan, dayUnits, today);
        long lastGap = computeLastIntakeGap(plan, dayUnits, today);

        boolean loggedToday = isAdherent(plan, dayUnits, today);
        boolean currentlyActive = !today.isBefore(plan.startDate()) && !today.isAfter(expectedEndDate);
        boolean atRisk = currentlyActive
                ? monthlyConsistency < AT_RISK_THRESHOLD || currentStreak == 0 || lastGap >= 2
                : goalCompletion < 80.0;
        String riskLevel = atRisk ? "AT RISK" : "ON TRACK";
        String safetyReminder = loggedToday
                ? "Daily target reached safely."
                : "More is not always better. Stick to " + plan.dailyTargetUnits() + " log(s)/day.";

        return new SupplementProgressItem(
                plan.id(),
                plan.supplementId(),
                plan.supplementName(),
                plan.supplementBrand(),
                plan.startDate(),
                plan.plannedDays(),
                plan.dailyTargetUnits(),
                round(weeklyConsistency),
                round(monthlyConsistency),
                currentStreak,
                bestStreak,
                round(goalCompletion),
                estimatedResultDate,
                expectedEndDate,
                Math.max(0, lastGap),
                loggedToday,
                atRisk,
                riskLevel,
                safetyReminder
        );
    }

    private List<String> buildProgressNotifications(List<SupplementProgressItem> items, int currentMaxStreak, int atRiskSupplements) {
        if (items.isEmpty()) {
            return List.of("Start by logging your first supplement day to unlock streak tracking.");
        }

        List<String> notifications = new ArrayList<>();
        if (currentMaxStreak > 0) {
            notifications.add("You're on a " + currentMaxStreak + "-day streak " + "\uD83D\uDD25");
        }
        boolean missedToday = items.stream().anyMatch(item -> !item.loggedToday());
        if (missedToday) {
            notifications.add("You missed your supplement today.");
        }
        if (atRiskSupplements > 0) {
            notifications.add("More is not always better. Follow your plan limits.");
        }
        return notifications;
    }

    private List<String> buildLeaderboardNotifications(List<LeaderboardEntry> rankedEntries, String currentUserEmail) {
        String normalizedEmail = normalizeEmail(currentUserEmail);
        if (normalizedEmail == null) {
            return List.of("Ranking is based on active streak, consistency %, then goal completion.");
        }

        for (LeaderboardEntry entry : rankedEntries) {
            if (entry.email().equalsIgnoreCase(normalizedEmail)) {
                if (entry.rank() <= 5) {
                    return List.of(
                            "You're top " + entry.rank() + " in consistency this month.",
                            "Ranking is based on active streak, consistency %, then goal completion."
                    );
                }
                return List.of(
                        "You are currently ranked #" + entry.rank() + ".",
                        "Ranking is based on active streak, consistency %, then goal completion."
                );
            }
        }

        return List.of("Ranking is based on active streak, consistency %, then goal completion.");
    }

    private void synchronizePlansFromOrders() {
        String sourceQuery = """
                SELECT
                    LOWER(TRIM(o.email)) AS user_email,
                    oi.supplement_id,
                    oi.supplement_name,
                    COALESCE(MAX(s.brand), '-') AS supplement_brand,
                    COALESCE(MAX(s.recommended_duration_days), 30) AS recommended_days,
                    COALESCE(MAX(DATE(o.created_at)), CURRENT_DATE()) AS latest_order_date
                FROM %s o
                INNER JOIN %s oi ON oi.order_id = o.id
                LEFT JOIN %s s ON s.id = oi.supplement_id
                WHERE o.email IS NOT NULL
                  AND TRIM(o.email) <> ''
                  AND UPPER(COALESCE(o.status, 'ON_PROGRESS')) <> 'CANCELED'
                GROUP BY LOWER(TRIM(o.email)), oi.supplement_id, oi.supplement_name
                """.formatted(
                SchemaInitializer.SUPPLEMENT_ORDER_TABLE,
                SchemaInitializer.SUPPLEMENT_ORDER_ITEM_TABLE,
                SchemaInitializer.SUPPLEMENT_TABLE
        );

        String upsertQuery = """
                INSERT INTO %s (
                    user_email, supplement_id, supplement_name, daily_target_units, planned_days, start_date, is_active
                ) VALUES (?, ?, ?, 1, ?, ?, TRUE)
                ON DUPLICATE KEY UPDATE
                    supplement_name = VALUES(supplement_name),
                    planned_days = VALUES(planned_days),
                    start_date = GREATEST(start_date, VALUES(start_date)),
                    is_active = TRUE
                """.formatted(SchemaInitializer.SUPPLEMENT_ADHERENCE_PLAN_TABLE);

        try (PreparedStatement sourceStatement = cnx.prepareStatement(sourceQuery);
             ResultSet resultSet = sourceStatement.executeQuery();
             PreparedStatement upsertStatement = cnx.prepareStatement(upsertQuery)) {

            while (resultSet.next()) {
                String email = resultSet.getString("user_email");
                if (email == null || email.isBlank()) {
                    continue;
                }

                int recommendedDays = clamp(resultSet.getInt("recommended_days"), 7, 365);
                Date latestOrderDate = resultSet.getDate("latest_order_date");
                LocalDate startDate = latestOrderDate == null ? LocalDate.now() : latestOrderDate.toLocalDate();

                upsertStatement.setString(1, email);
                upsertStatement.setInt(2, resultSet.getInt("supplement_id"));
                upsertStatement.setString(3, resultSet.getString("supplement_name"));
                upsertStatement.setInt(4, recommendedDays);
                upsertStatement.setDate(5, Date.valueOf(startDate));
                upsertStatement.addBatch();
            }
            upsertStatement.executeBatch();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de synchroniser les plans d'adherence.", exception);
        }
    }

    private List<PlanRecord> fetchActivePlansForEmail(String email) {
        String query = """
                SELECT
                    p.id,
                    p.user_email,
                    p.supplement_id,
                    p.supplement_name,
                    COALESCE(s.brand, '-') AS supplement_brand,
                    p.daily_target_units,
                    p.planned_days,
                    p.start_date
                FROM %s p
                LEFT JOIN %s s ON s.id = p.supplement_id
                WHERE LOWER(p.user_email) = LOWER(?)
                  AND p.is_active = TRUE
                ORDER BY p.start_date DESC, p.id DESC
                """.formatted(
                SchemaInitializer.SUPPLEMENT_ADHERENCE_PLAN_TABLE,
                SchemaInitializer.SUPPLEMENT_TABLE
        );

        List<PlanRecord> plans = new ArrayList<>();
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    plans.add(mapPlan(resultSet));
                }
            }
            return plans;
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les plans d'adherence.", exception);
        }
    }

    private List<PlanRecord> fetchAllActivePlans() {
        String query = """
                SELECT
                    p.id,
                    p.user_email,
                    p.supplement_id,
                    p.supplement_name,
                    COALESCE(s.brand, '-') AS supplement_brand,
                    p.daily_target_units,
                    p.planned_days,
                    p.start_date
                FROM %s p
                LEFT JOIN %s s ON s.id = p.supplement_id
                WHERE p.is_active = TRUE
                """.formatted(
                SchemaInitializer.SUPPLEMENT_ADHERENCE_PLAN_TABLE,
                SchemaInitializer.SUPPLEMENT_TABLE
        );

        List<PlanRecord> plans = new ArrayList<>();
        try (PreparedStatement statement = cnx.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                plans.add(mapPlan(resultSet));
            }
            return plans;
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les plans actifs d'adherence.", exception);
        }
    }

    private PlanRecord fetchPlanByIdForEmail(int planId, String email) {
        String query = """
                SELECT
                    p.id,
                    p.user_email,
                    p.supplement_id,
                    p.supplement_name,
                    COALESCE(s.brand, '-') AS supplement_brand,
                    p.daily_target_units,
                    p.planned_days,
                    p.start_date
                FROM %s p
                LEFT JOIN %s s ON s.id = p.supplement_id
                WHERE p.id = ?
                  AND LOWER(p.user_email) = LOWER(?)
                  AND p.is_active = TRUE
                """.formatted(
                SchemaInitializer.SUPPLEMENT_ADHERENCE_PLAN_TABLE,
                SchemaInitializer.SUPPLEMENT_TABLE
        );

        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, planId);
            statement.setString(2, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapPlan(resultSet);
                }
                return null;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer le plan d'adherence.", exception);
        }
    }

    private PlanRecord mapPlan(ResultSet resultSet) throws SQLException {
        Date startDate = resultSet.getDate("start_date");
        return new PlanRecord(
                resultSet.getInt("id"),
                resultSet.getString("user_email"),
                resultSet.getInt("supplement_id"),
                resultSet.getString("supplement_name"),
                resultSet.getString("supplement_brand"),
                Math.max(1, resultSet.getInt("daily_target_units")),
                Math.max(1, resultSet.getInt("planned_days")),
                startDate == null ? LocalDate.now() : startDate.toLocalDate()
        );
    }

    private Map<Integer, Map<LocalDate, Integer>> loadLoggedUnitsByPlanIds(Set<Integer> planIds, LocalDate fromDate, LocalDate toDate) {
        Map<Integer, Map<LocalDate, Integer>> result = new HashMap<>();
        if (planIds == null || planIds.isEmpty() || fromDate == null || toDate == null || fromDate.isAfter(toDate)) {
            return result;
        }

        String placeholders = planIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String query = """
                SELECT plan_id, log_date, SUM(taken_units) AS units
                FROM %s
                WHERE plan_id IN (%s)
                  AND log_date BETWEEN ? AND ?
                GROUP BY plan_id, log_date
                """.formatted(SchemaInitializer.SUPPLEMENT_ADHERENCE_LOG_TABLE, placeholders);

        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            int index = 1;
            for (Integer planId : planIds) {
                statement.setInt(index++, planId);
            }
            statement.setDate(index++, Date.valueOf(fromDate));
            statement.setDate(index, Date.valueOf(toDate));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int planId = resultSet.getInt("plan_id");
                    Date logDate = resultSet.getDate("log_date");
                    if (logDate == null) {
                        continue;
                    }
                    LocalDate date = logDate.toLocalDate();
                    int units = resultSet.getInt("units");
                    result.computeIfAbsent(planId, ignored -> new HashMap<>()).put(date, units);
                }
            }
            return result;
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de lire les logs d'adherence.", exception);
        }
    }

    private int countLoggedUnitsForPlanDate(int planId, LocalDate date) {
        String query = """
                SELECT COALESCE(SUM(taken_units), 0) AS units
                FROM %s
                WHERE plan_id = ?
                  AND log_date = ?
                """.formatted(SchemaInitializer.SUPPLEMENT_ADHERENCE_LOG_TABLE);

        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, planId);
            statement.setDate(2, Date.valueOf(date));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Math.max(0, resultSet.getInt("units"));
                }
                return 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de verifier les logs du jour.", exception);
        }
    }

    private int computeCurrentStreak(PlanRecord plan, Map<LocalDate, Integer> dayUnits, LocalDate evaluationDate) {
        LocalDate endDate = minDate(evaluationDate, plan.expectedEndDate());
        if (endDate.isBefore(plan.startDate())) {
            return 0;
        }

        int streak = 0;
        for (LocalDate cursor = endDate; !cursor.isBefore(plan.startDate()); cursor = cursor.minusDays(1)) {
            if (!isAdherent(plan, dayUnits, cursor)) {
                break;
            }
            streak++;
        }
        return streak;
    }

    private int computeBestStreak(PlanRecord plan, Map<LocalDate, Integer> dayUnits, LocalDate evaluationDate) {
        LocalDate endDate = minDate(evaluationDate, plan.expectedEndDate());
        if (endDate.isBefore(plan.startDate())) {
            return 0;
        }

        int best = 0;
        int current = 0;
        for (LocalDate cursor = plan.startDate(); !cursor.isAfter(endDate); cursor = cursor.plusDays(1)) {
            if (isAdherent(plan, dayUnits, cursor)) {
                current++;
                best = Math.max(best, current);
            } else {
                current = 0;
            }
        }
        return best;
    }

    private double computeConsistencyPercent(PlanRecord plan, Map<LocalDate, Integer> dayUnits, LocalDate fromDate, LocalDate toDate) {
        int expectedDays = countExpectedDays(plan, fromDate, toDate);
        int adherentDays = countAdherentDays(plan, dayUnits, fromDate, toDate);
        return percentage(adherentDays, expectedDays);
    }

    private int countExpectedDays(PlanRecord plan, LocalDate fromDate, LocalDate toDate) {
        LocalDate start = maxDate(fromDate, plan.startDate());
        LocalDate end = minDate(toDate, plan.expectedEndDate());
        if (start.isAfter(end)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(start, end) + 1;
    }

    private int countAdherentDays(PlanRecord plan, Map<LocalDate, Integer> dayUnits, LocalDate fromDate, LocalDate toDate) {
        LocalDate start = maxDate(fromDate, plan.startDate());
        LocalDate end = minDate(toDate, plan.expectedEndDate());
        if (start.isAfter(end)) {
            return 0;
        }

        int adherentDays = 0;
        for (LocalDate cursor = start; !cursor.isAfter(end); cursor = cursor.plusDays(1)) {
            if (isAdherent(plan, dayUnits, cursor)) {
                adherentDays++;
            }
        }
        return adherentDays;
    }

    private boolean isAdherent(PlanRecord plan, Map<LocalDate, Integer> dayUnits, LocalDate date) {
        if (dayUnits == null || date == null) {
            return false;
        }
        int units = dayUnits.getOrDefault(date, 0);
        return units >= plan.dailyTargetUnits();
    }

    private double computeGoalCompletionPercent(PlanRecord plan, Map<LocalDate, Integer> dayUnits, LocalDate evaluationDate) {
        LocalDate end = minDate(evaluationDate, plan.expectedEndDate());
        if (end.isBefore(plan.startDate())) {
            return 0.0;
        }

        int elapsedDays = (int) ChronoUnit.DAYS.between(plan.startDate(), end) + 1;
        int expected = Math.min(plan.plannedDays(), Math.max(elapsedDays, 0));
        int completed = countAdherentDays(plan, dayUnits, plan.startDate(), end);
        return percentage(completed, expected);
    }

    private LocalDate estimateResultDate(PlanRecord plan, Map<LocalDate, Integer> dayUnits, LocalDate today) {
        LocalDate evaluationEnd = minDate(today, plan.expectedEndDate());
        if (evaluationEnd.isBefore(plan.startDate())) {
            return plan.expectedEndDate();
        }

        int completedDays = countAdherentDays(plan, dayUnits, plan.startDate(), evaluationEnd);
        int elapsedDays = (int) ChronoUnit.DAYS.between(plan.startDate(), evaluationEnd) + 1;
        int remainingCompletions = Math.max(0, plan.plannedDays() - completedDays);
        if (remainingCompletions == 0) {
            return evaluationEnd;
        }
        if (elapsedDays <= 0 || completedDays == 0) {
            return plan.expectedEndDate();
        }

        double completionPerDay = (double) completedDays / (double) elapsedDays;
        if (completionPerDay <= 0.0) {
            return plan.expectedEndDate();
        }

        int estimatedRemainingDays = (int) Math.ceil(remainingCompletions / completionPerDay);
        return evaluationEnd.plusDays(Math.max(0, estimatedRemainingDays));
    }

    private long computeLastIntakeGap(PlanRecord plan, Map<LocalDate, Integer> dayUnits, LocalDate today) {
        if (dayUnits == null || dayUnits.isEmpty()) {
            return ChronoUnit.DAYS.between(plan.startDate(), today);
        }

        LocalDate lastIntakeDate = null;
        for (Map.Entry<LocalDate, Integer> entry : dayUnits.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            LocalDate date = entry.getKey();
            if (date == null || date.isAfter(today)) {
                continue;
            }
            if (lastIntakeDate == null || date.isAfter(lastIntakeDate)) {
                lastIntakeDate = date;
            }
        }

        if (lastIntakeDate == null) {
            return ChronoUnit.DAYS.between(plan.startDate(), today);
        }
        return ChronoUnit.DAYS.between(lastIntakeDate, today);
    }

    private void saveNotification(String email, String type, String message) {
        if (email == null || email.isBlank() || message == null || message.isBlank()) {
            return;
        }

        String query = """
                INSERT INTO %s (user_email, notification_type, message)
                VALUES (?, ?, ?)
                """.formatted(SchemaInitializer.SUPPLEMENT_ADHERENCE_NOTIFICATION_TABLE);

        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setString(1, email);
            statement.setString(2, type == null ? "INFO" : type);
            statement.setString(3, message);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'enregistrer la notification d'adherence.", exception);
        }
    }

    private List<String> fetchRecentNotifications(String email, int limit) {
        if (email == null || email.isBlank() || limit <= 0) {
            return List.of();
        }

        String query = """
                SELECT message
                FROM %s
                WHERE LOWER(user_email) = LOWER(?)
                ORDER BY id DESC
                LIMIT ?
                """.formatted(SchemaInitializer.SUPPLEMENT_ADHERENCE_NOTIFICATION_TABLE);

        List<String> messages = new ArrayList<>();
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setString(1, email);
            statement.setInt(2, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String message = resultSet.getString("message");
                    if (message != null && !message.isBlank()) {
                        messages.add(message.trim());
                    }
                }
            }
            return messages;
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de lire les notifications d'adherence.", exception);
        }
    }

    private Map<String, String> resolveUserDisplayNames() {
        String query = """
                SELECT LOWER(TRIM(email)) AS user_email, MAX(TRIM(CONCAT(first_name, ' ', last_name))) AS display_name
                FROM %s
                WHERE email IS NOT NULL
                  AND TRIM(email) <> ''
                GROUP BY LOWER(TRIM(email))
                """.formatted(SchemaInitializer.SUPPLEMENT_ORDER_TABLE);

        Map<String, String> names = new HashMap<>();
        try (PreparedStatement statement = cnx.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String email = resultSet.getString("user_email");
                String displayName = resultSet.getString("display_name");
                if (email == null || email.isBlank()) {
                    continue;
                }
                names.put(
                        email.trim().toLowerCase(Locale.ROOT),
                        displayName == null || displayName.isBlank() ? email : displayName.trim()
                );
            }
            return names;
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de charger les noms des utilisateurs.", exception);
        }
    }

    private String resolveDisplayName(Map<String, String> namesByEmail, String email) {
        if (email == null || email.isBlank()) {
            return "Unknown user";
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        String displayName = namesByEmail.get(normalized);
        if (displayName != null && !displayName.isBlank()) {
            return displayName;
        }
        int atIndex = normalized.indexOf('@');
        if (atIndex > 0) {
            return normalized.substring(0, atIndex);
        }
        return normalized;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private List<String> deduplicate(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        List<String> deduplicated = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String message : messages) {
            if (message == null || message.isBlank()) {
                continue;
            }
            String normalized = message.trim();
            if (seen.add(normalized)) {
                deduplicated.add(normalized);
            }
            if (deduplicated.size() >= 5) {
                break;
            }
        }
        return deduplicated;
    }

    private double percentage(int value, int total) {
        if (total <= 0) {
            return 0.0;
        }
        return ((double) value * 100.0) / (double) total;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private LocalDate minDate(LocalDate left, LocalDate right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isBefore(right) ? left : right;
    }

    private LocalDate maxDate(LocalDate left, LocalDate right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isAfter(right) ? left : right;
    }

    private record PlanRecord(
            int id,
            String userEmail,
            int supplementId,
            String supplementName,
            String supplementBrand,
            int dailyTargetUnits,
            int plannedDays,
            LocalDate startDate
    ) {
        LocalDate expectedEndDate() {
            return startDate.plusDays(Math.max(1, plannedDays) - 1L);
        }
    }

    public record ProgressDashboard(
            int trackedSupplements,
            double averageCompliancePercent,
            int currentMaxStreakDays,
            int atRiskSupplements,
            double goalCompletionPercent,
            List<SupplementProgressItem> supplementItems,
            List<String> notifications
    ) {
        public static ProgressDashboard empty() {
            return new ProgressDashboard(0, 0.0, 0, 0, 0.0, List.of(), List.of());
        }
    }

    public record SupplementProgressItem(
            int planId,
            int supplementId,
            String supplementName,
            String supplementBrand,
            LocalDate startDate,
            int plannedDays,
            int dailyTargetUnits,
            double weeklyConsistencyPercent,
            double monthlyConsistencyPercent,
            int currentStreakDays,
            int bestStreakDays,
            double goalCompletionPercent,
            LocalDate estimatedResultDate,
            LocalDate expectedEndDate,
            long lastIntakeGapDays,
            boolean loggedToday,
            boolean atRisk,
            String riskLevel,
            String safetyReminder
    ) {
    }

    public record LogResult(
            boolean success,
            String message,
            boolean warning,
            int currentStreakDays
    ) {
    }

    public record LeaderboardResult(
            YearMonth month,
            List<LeaderboardEntry> entries,
            List<String> notifications
    ) {
    }

    public record LeaderboardEntry(
            int rank,
            String displayName,
            String email,
            int longestActiveStreak,
            double consistencyPercent,
            double goalCompletionPercent
    ) {
    }
}
