package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.api.dto.UserDigitalTwinReport;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class UserDigitalTwinService {
    private static final DateTimeFormatter[] DATE_FORMATS = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    private final Connection cnx;

    public UserDigitalTwinService() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public List<UserDigitalTwinReport> generateDigitalTwinAnalysis() {
        ensureConnection();
        SchemaSnapshot schema = inspectSchema();
        String sql = buildAnalysisQuery(schema);
        List<AnalysisContext> analysis = new ArrayList<>();

        try (PreparedStatement statement = cnx.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                analysis.add(mapAnalysisContext(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'analyse du digital twin utilisateur : " + e.getMessage(), e);
        }

        for (AnalysisContext context : analysis) {
            finalizeBusinessAnalysis(context);
        }

        analysis.sort(Comparator
                .comparingInt((AnalysisContext context) -> alertSeverity(context.report().getAlertLevel())).reversed()
                .thenComparingInt(context -> context.report().getTwinScore())
                .thenComparing(AnalysisContext::calorieGapForSort, Comparator.reverseOrder())
                .thenComparing(context -> safe(context.report().getFullName())));

        return analysis.stream()
                .map(AnalysisContext::report)
                .toList();
    }

    private AnalysisContext mapAnalysisContext(ResultSet rs) throws SQLException {
        UserDigitalTwinReport report = new UserDigitalTwinReport();
        report.setUserId(rs.getInt("user_id"));
        report.setFullName(safe(rs.getString("full_name")));
        report.setEmail(safe(rs.getString("email")));
        report.setGoal(safe(rs.getString("goal")));
        report.setRegimeName(safe(rs.getString("regime_name")));
        report.setTargetCalories(getNullableDouble(rs, "target_calories"));
        report.setMealsCount(rs.getInt("meals_count"));
        report.setAvgRealCalories(getNullableDouble(rs, "avg_real_calories"));
        report.setTotalRealCalories(getNullableDouble(rs, "total_real_calories"));
        report.setLastMealDate(safe(rs.getString("last_meal_date")));
        report.setCalorieGap(getNullableDouble(rs, "calorie_gap"));
        report.setAlignmentStatus(safe(rs.getString("preliminary_alignment_status")));

        return new AnalysisContext(
                report,
                safe(rs.getString("last_activity_date")),
                rs.getInt("inactivity_days")
        );
    }

    private void finalizeBusinessAnalysis(AnalysisContext context) {
        UserDigitalTwinReport report = context.report();
        double targetCalories = report.getTargetCalories() == null || report.getTargetCalories() <= 0
                ? defaultTargetCalories(report.getGoal())
                : report.getTargetCalories();
        report.setTargetCalories(targetCalories);

        Double avgRealCalories = report.getAvgRealCalories();
        if (avgRealCalories != null) {
            report.setCalorieGap(round(Math.abs(avgRealCalories - targetCalories)));
        }

        if (report.getMealsCount() <= 0 || avgRealCalories == null) {
            report.setAlignmentStatus("AUCUNE_DONNEE");
            report.setTwinScore(0);
            report.setAlertLevel("CRITIQUE");
            return;
        }

        double calorieGap = report.getCalorieGap() == null ? Math.abs(avgRealCalories - targetCalories) : report.getCalorieGap();
        double gapRatio = targetCalories <= 0 ? 1 : calorieGap / targetCalories;
        int score = 100;

        score -= (int) Math.min(55, Math.round(gapRatio * 100));

        if (report.getMealsCount() >= 21) {
            score += 5;
        } else if (report.getMealsCount() >= 10) {
            score += 2;
        } else if (report.getMealsCount() <= 3) {
            score -= 15;
        } else if (report.getMealsCount() <= 6) {
            score -= 8;
        }

        int inactivityDays = context.inactivityDays() < 0 ? 9999 : context.inactivityDays();
        if (inactivityDays == 9999) {
            LocalDateTime lastActivity = parseDateTime(context.lastActivityDate());
            if (lastActivity != null) {
                inactivityDays = (int) Math.max(0, ChronoUnit.DAYS.between(lastActivity.toLocalDate(), LocalDateTime.now().toLocalDate()));
            }
        }
        if (inactivityDays >= 21) {
            score -= 35;
        } else if (inactivityDays >= 14) {
            score -= 25;
        } else if (inactivityDays >= 7) {
            score -= 15;
        } else if (inactivityDays >= 3) {
            score -= 7;
        }

        if (safe(report.getGoal()).equalsIgnoreCase("OBJECTIF_NON_DEFINI")) {
            score -= 5;
        }
        if (safe(report.getRegimeName()).equalsIgnoreCase("REGIME_NON_DEFINI")) {
            score -= 5;
        }

        score = clamp(score, 0, 100);
        report.setTwinScore(score);

        if (calorieGap <= 120 && inactivityDays <= 3 && score >= 80) {
            report.setAlignmentStatus("ALIGNE");
        } else if (calorieGap <= 350 && inactivityDays <= 10 && score >= 50) {
            report.setAlignmentStatus("PARTIELLEMENT_ALIGNE");
        } else {
            report.setAlignmentStatus("DESALIGNE");
        }

        if (inactivityDays >= 14 || score < 40 || calorieGap > 500) {
            report.setAlertLevel("CRITIQUE");
        } else if (inactivityDays >= 7 || score < 70 || calorieGap > 250) {
            report.setAlertLevel("A_SURVEILLER");
        } else {
            report.setAlertLevel("NORMAL");
        }
    }

    private String buildAnalysisQuery(SchemaSnapshot schema) {
        String usersTable = schema.resolveTable("fitopia_users", "users");
        if (usersTable == null) {
            throw new IllegalStateException("Aucune table utilisateur exploitable n'a ete detectee.");
        }

        String userIdColumn = schema.resolveColumn(usersTable, "id");
        String firstNameColumn = schema.resolveColumn(usersTable, "first_name", "firstname", "nom");
        String lastNameColumn = schema.resolveColumn(usersTable, "last_name", "lastname", "prenom");
        String emailColumn = schema.resolveColumn(usersTable, "email", "mail");
        String archivedColumn = schema.resolveColumn(usersTable, "is_archived", "archived");
        String fallbackGoalColumn = schema.resolveColumn(usersTable, "fitness_goals", "goal", "objectif");
        String fallbackRegimeColumn = schema.resolveColumn(usersTable, "dietary_preferences", "regime_name", "diet_name");
        String lastLoginColumn = schema.resolveColumn(usersTable, "last_login_at", "last_activity_at", "updated_at");

        if (userIdColumn == null || emailColumn == null) {
            throw new IllegalStateException("Le schema utilisateur ne contient pas les colonnes minimales pour l'analyse.");
        }

        String profileJoin = buildProfileJoin(schema);
        String regimeJoin = buildRegimeJoin(schema);
        String weeklyPlanJoin = buildWeeklyPlanJoin(schema);
        String mealJoin = buildMealJoin(schema);

        String fullNameExpression = "TRIM(CONCAT(COALESCE(u." + q(firstNameColumn) + ", ''), ' ', COALESCE(u." + q(lastNameColumn) + ", '')))";
        String fallbackGoalExpression = fallbackGoalColumn == null ? "NULL" : "NULLIF(TRIM(u." + q(fallbackGoalColumn) + "), '')";
        String fallbackRegimeExpression = fallbackRegimeColumn == null ? "NULL" : "NULLIF(TRIM(u." + q(fallbackRegimeColumn) + "), '')";
        String lastLoginExpression = lastLoginColumn == null ? "NULL" : "NULLIF(u." + q(lastLoginColumn) + ", '')";
        String targetCaloriesExpression = "COALESCE(plan_info.target_calories, " + defaultTargetSql("COALESCE(profile_info.goal, " + fallbackGoalExpression + ", '')") + ")";
        String calorieGapExpression = "CASE WHEN meal_info.avg_real_calories IS NULL THEN NULL ELSE ABS(meal_info.avg_real_calories - "
                + targetCaloriesExpression + ") END";
        String lastActivityExpression = "CASE "
                + "WHEN meal_info.last_meal_date IS NULL THEN " + lastLoginExpression + " "
                + "WHEN " + lastLoginExpression + " IS NULL THEN meal_info.last_meal_date "
                + "WHEN meal_info.last_meal_date >= " + lastLoginExpression + " THEN meal_info.last_meal_date "
                + "ELSE " + lastLoginExpression + " END";
        String inactivityDaysExpression = "CASE WHEN " + lastActivityExpression + " IS NULL THEN 9999 "
                + "ELSE DATEDIFF(CURRENT_DATE, DATE(" + lastActivityExpression + ")) END";
        String preliminaryAlignmentExpression = "CASE "
                + "WHEN COALESCE(meal_info.meals_count, 0) = 0 THEN 'AUCUNE_DONNEE' "
                + "WHEN ABS(meal_info.avg_real_calories - " + targetCaloriesExpression + ") <= 120 THEN 'ALIGNE' "
                + "WHEN ABS(meal_info.avg_real_calories - " + targetCaloriesExpression + ") <= 350 THEN 'PARTIELLEMENT_ALIGNE' "
                + "ELSE 'DESALIGNE' END";
        String criticalityRankExpression = "CASE "
                + "WHEN COALESCE(meal_info.meals_count, 0) = 0 THEN 3 "
                + "WHEN " + inactivityDaysExpression + " >= 14 THEN 3 "
                + "WHEN ABS(COALESCE(meal_info.avg_real_calories, 0) - " + targetCaloriesExpression + ") > 350 THEN 2 "
                + "ELSE 1 END";

        return "SELECT "
                + "u." + q(userIdColumn) + " AS user_id, "
                + fullNameExpression + " AS full_name, "
                + "u." + q(emailColumn) + " AS email, "
                + "COALESCE(profile_info.goal, " + fallbackGoalExpression + ", 'OBJECTIF_NON_DEFINI') AS goal, "
                + "COALESCE(regime_info.regime_name, " + fallbackRegimeExpression + ", 'REGIME_NON_DEFINI') AS regime_name, "
                + targetCaloriesExpression + " AS target_calories, "
                + "COALESCE(meal_info.meals_count, 0) AS meals_count, "
                + "meal_info.avg_real_calories AS avg_real_calories, "
                + "COALESCE(meal_info.total_real_calories, 0) AS total_real_calories, "
                + "meal_info.last_meal_date AS last_meal_date, "
                + calorieGapExpression + " AS calorie_gap, "
                + lastActivityExpression + " AS last_activity_date, "
                + inactivityDaysExpression + " AS inactivity_days, "
                + preliminaryAlignmentExpression + " AS preliminary_alignment_status "
                + "FROM " + q(usersTable) + " u "
                + profileJoin + " "
                + regimeJoin + " "
                + weeklyPlanJoin + " "
                + mealJoin + " "
                + (archivedColumn == null ? "" : "WHERE COALESCE(u." + q(archivedColumn) + ", 0) = 0 ")
                + "ORDER BY " + criticalityRankExpression + " DESC, "
                + "COALESCE(" + calorieGapExpression + ", 99999) DESC, "
                + fullNameExpression + " ASC";
    }

    private String buildProfileJoin(SchemaSnapshot schema) {
        String profileTable = schema.resolveTable("user_profile");
        if (profileTable == null) {
            return "LEFT JOIN (SELECT NULL AS user_id, NULL AS goal FROM DUAL WHERE 1 = 0) profile_info ON profile_info.user_id = u.id";
        }

        String userIdColumn = schema.resolveColumn(profileTable, "user_id");
        String goalColumn = schema.resolveColumn(profileTable, "goal", "fitness_goal", "objective");
        if (userIdColumn == null || goalColumn == null) {
            return "LEFT JOIN (SELECT NULL AS user_id, NULL AS goal FROM DUAL WHERE 1 = 0) profile_info ON profile_info.user_id = u.id";
        }

        return "LEFT JOIN (SELECT up." + q(userIdColumn) + " AS user_id, "
                + "MAX(NULLIF(TRIM(up." + q(goalColumn) + "), '')) AS goal "
                + "FROM " + q(profileTable) + " up "
                + "GROUP BY up." + q(userIdColumn) + ") profile_info ON profile_info.user_id = u.id";
    }

    private String buildRegimeJoin(SchemaSnapshot schema) {
        String userRegimeTable = schema.resolveTable("user_regime");
        String regimeTable = schema.resolveTable("regime", "regimes");
        if (userRegimeTable == null || regimeTable == null) {
            return "LEFT JOIN (SELECT NULL AS user_id, NULL AS regime_name FROM DUAL WHERE 1 = 0) regime_info ON regime_info.user_id = u.id";
        }

        String userIdColumn = schema.resolveColumn(userRegimeTable, "user_id");
        String regimeIdColumn = schema.resolveColumn(userRegimeTable, "regime_id");
        String regimeTableIdColumn = schema.resolveColumn(regimeTable, "id", "regime_id");
        String regimeNameColumn = schema.resolveColumn(regimeTable, "name", "regime_name", "title");
        if (userIdColumn == null || regimeIdColumn == null || regimeTableIdColumn == null || regimeNameColumn == null) {
            return "LEFT JOIN (SELECT NULL AS user_id, NULL AS regime_name FROM DUAL WHERE 1 = 0) regime_info ON regime_info.user_id = u.id";
        }

        return "LEFT JOIN (SELECT ur." + q(userIdColumn) + " AS user_id, "
                + "MAX(NULLIF(TRIM(r." + q(regimeNameColumn) + "), '')) AS regime_name "
                + "FROM " + q(userRegimeTable) + " ur "
                + "JOIN " + q(regimeTable) + " r ON ur." + q(regimeIdColumn) + " = r." + q(regimeTableIdColumn) + " "
                + "GROUP BY ur." + q(userIdColumn) + ") regime_info ON regime_info.user_id = u.id";
    }

    private String buildWeeklyPlanJoin(SchemaSnapshot schema) {
        String weeklyPlansTable = schema.resolveTable("weekly_plans");
        if (weeklyPlansTable == null) {
            return "LEFT JOIN (SELECT NULL AS user_id, NULL AS target_calories FROM DUAL WHERE 1 = 0) plan_info ON plan_info.user_id = u.id";
        }

        String userIdColumn = schema.resolveColumn(weeklyPlansTable, "user_id");
        String targetCaloriesColumn = schema.resolveColumn(weeklyPlansTable, "target_calories", "daily_target_calories", "calories_target");
        if (userIdColumn == null || targetCaloriesColumn == null) {
            return "LEFT JOIN (SELECT NULL AS user_id, NULL AS target_calories FROM DUAL WHERE 1 = 0) plan_info ON plan_info.user_id = u.id";
        }

        return "LEFT JOIN (SELECT wp." + q(userIdColumn) + " AS user_id, "
                + "MAX(CASE WHEN wp." + q(targetCaloriesColumn) + " IS NULL OR wp." + q(targetCaloriesColumn) + " <= 0 "
                + "THEN NULL ELSE wp." + q(targetCaloriesColumn) + " END) AS target_calories "
                + "FROM " + q(weeklyPlansTable) + " wp "
                + "GROUP BY wp." + q(userIdColumn) + ") plan_info ON plan_info.user_id = u.id";
    }

    private String buildMealJoin(SchemaSnapshot schema) {
        String mealLogsTable = schema.resolveTable("meal_logs");
        if (mealLogsTable == null) {
            return "LEFT JOIN (SELECT NULL AS user_id, 0 AS meals_count, NULL AS avg_real_calories, NULL AS total_real_calories, NULL AS last_meal_date "
                    + "FROM DUAL WHERE 1 = 0) meal_info ON meal_info.user_id = u.id";
        }

        String userIdColumn = schema.resolveColumn(mealLogsTable, "user_id");
        String caloriesColumn = schema.resolveColumn(mealLogsTable, "calories", "consumed_calories", "meal_calories");
        String mealDateColumn = schema.resolveColumn(mealLogsTable, "logged_at", "meal_date", "consumed_at", "created_at");
        if (userIdColumn == null || caloriesColumn == null || mealDateColumn == null) {
            return "LEFT JOIN (SELECT NULL AS user_id, 0 AS meals_count, NULL AS avg_real_calories, NULL AS total_real_calories, NULL AS last_meal_date "
                    + "FROM DUAL WHERE 1 = 0) meal_info ON meal_info.user_id = u.id";
        }

        return "LEFT JOIN (SELECT ml." + q(userIdColumn) + " AS user_id, "
                + "COUNT(*) AS meals_count, "
                + "AVG(CASE WHEN ml." + q(caloriesColumn) + " IS NULL THEN 0 ELSE ml." + q(caloriesColumn) + " END) AS avg_real_calories, "
                + "SUM(CASE WHEN ml." + q(caloriesColumn) + " IS NULL THEN 0 ELSE ml." + q(caloriesColumn) + " END) AS total_real_calories, "
                + "MAX(ml." + q(mealDateColumn) + ") AS last_meal_date "
                + "FROM " + q(mealLogsTable) + " ml "
                + "GROUP BY ml." + q(userIdColumn) + " "
                + "HAVING COUNT(*) >= 0) meal_info ON meal_info.user_id = u.id";
    }

    private SchemaSnapshot inspectSchema() {
        try {
            DatabaseMetaData metaData = cnx.getMetaData();
            Map<String, Set<String>> columnsByTable = new HashMap<>();
            try (ResultSet tables = metaData.getTables(cnx.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    columnsByTable.put(tableName.toLowerCase(Locale.ROOT), loadColumns(metaData, tableName));
                }
            }
            return new SchemaSnapshot(columnsByTable);
        } catch (SQLException e) {
            throw new RuntimeException("Impossible d'inspecter le schema MySQL pour le digital twin : " + e.getMessage(), e);
        }
    }

    private Set<String> loadColumns(DatabaseMetaData metaData, String tableName) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (ResultSet rs = metaData.getColumns(cnx.getCatalog(), null, tableName, "%")) {
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME").toLowerCase(Locale.ROOT));
            }
        }
        return columns;
    }

    private Double getNullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : round(value);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private int alertSeverity(String alertLevel) {
        return switch (safe(alertLevel).toUpperCase(Locale.ROOT)) {
            case "CRITIQUE" -> 3;
            case "A_SURVEILLER" -> 2;
            default -> 1;
        };
    }

    private double defaultTargetCalories(String goal) {
        String normalizedGoal = safe(goal).toLowerCase(Locale.ROOT);
        if (normalizedGoal.contains("perte") || normalizedGoal.contains("loss") || normalizedGoal.contains("cut")) {
            return 1800;
        }
        if (normalizedGoal.contains("prise") || normalizedGoal.contains("gain") || normalizedGoal.contains("masse")) {
            return 2600;
        }
        if (normalizedGoal.contains("maint")) {
            return 2200;
        }
        return 2000;
    }

    private String defaultTargetSql(String goalExpression) {
        String normalizedGoal = "LOWER(" + goalExpression + ")";
        return "CASE "
                + "WHEN " + normalizedGoal + " LIKE '%perte%' OR " + normalizedGoal + " LIKE '%loss%' OR " + normalizedGoal + " LIKE '%cut%' THEN 1800 "
                + "WHEN " + normalizedGoal + " LIKE '%prise%' OR " + normalizedGoal + " LIKE '%gain%' OR " + normalizedGoal + " LIKE '%masse%' THEN 2600 "
                + "WHEN " + normalizedGoal + " LIKE '%maint%' THEN 2200 "
                + "ELSE 2000 END";
    }

    private void ensureConnection() {
        if (cnx == null) {
            throw new IllegalStateException("Connexion MySQL indisponible. Verifiez la base fitopiabd.");
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String q(String identifier) {
        return "`" + identifier + "`";
    }

    private LocalDateTime parseDateTime(String raw) {
        String value = safe(raw);
        if (value.isBlank()) {
            return null;
        }
        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                if (formatter == DateTimeFormatter.ISO_OFFSET_DATE_TIME) {
                    return OffsetDateTime.parse(value, formatter).toLocalDateTime();
                }
                return LocalDateTime.parse(value, formatter);
            } catch (Exception ignored) {
            }
        }
        try {
            return LocalDateTime.parse(value.replace(' ', 'T'));
        } catch (Exception ignored) {
            return null;
        }
    }

    private record AnalysisContext(UserDigitalTwinReport report, String lastActivityDate, int inactivityDays) {
        private Double calorieGapForSort() {
            return report.getCalorieGap() == null ? 0.0 : report.getCalorieGap();
        }
    }

    private static final class SchemaSnapshot {
        private final Map<String, Set<String>> columnsByTable;

        private SchemaSnapshot(Map<String, Set<String>> columnsByTable) {
            this.columnsByTable = columnsByTable;
        }

        private String resolveTable(String... candidates) {
            for (String candidate : candidates) {
                if (candidate != null && columnsByTable.containsKey(candidate.toLowerCase(Locale.ROOT))) {
                    return candidate;
                }
            }
            return null;
        }

        private String resolveColumn(String table, String... candidates) {
            if (table == null) {
                return null;
            }
            Set<String> columns = columnsByTable.get(table.toLowerCase(Locale.ROOT));
            if (columns == null) {
                return null;
            }
            for (String candidate : candidates) {
                if (candidate != null && columns.contains(candidate.toLowerCase(Locale.ROOT))) {
                    return candidate;
                }
            }
            return null;
        }
    }
}
