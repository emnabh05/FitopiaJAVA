package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.api.dto.AdaptiveAccountTrustReport;
import tn.esprit.Pidev3A49.services.security.JwtService;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AdaptiveAccountTrustEngineService {
    private static final String USERS_TABLE = "fitopia_users";
    private static final String PASSWORD_HISTORY_TABLE = "fitopia_user_password_history";
    private static final String PASSWORD_RESET_AUDIT_TABLE = "fitopia_user_password_reset_audit";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Connection cnx;
    private final JwtService jwtService = new JwtService();
    private final ServiceUser serviceUser;

    public AdaptiveAccountTrustEngineService() {
        this.serviceUser = new ServiceUser();
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public AdaptiveAccountTrustReport evaluateTrustForAction(int userId, String actionContext, String authorizationHeader) {
        List<AdaptiveAccountTrustReport> reports = generateTrustAnalysis(actionContext, authorizationHeader);
        return reports.stream()
                .filter(report -> report.getUserId() == userId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun rapport de confiance disponible pour l'utilisateur #" + userId + "."));
    }

    public List<AdaptiveAccountTrustReport> generateTrustAnalysis(String actionContext, String authorizationHeader) {
        ensureConnection();
        String normalizedAction = normalizeAction(actionContext);
        String jwtStatus = resolveJwtStatus(authorizationHeader);
        List<AdaptiveAccountTrustReport> reports = new ArrayList<>();

        try (PreparedStatement pstm = cnx.prepareStatement(buildTrustQuery());
             ResultSet rs = pstm.executeQuery()) {
            while (rs.next()) {
                AdaptiveAccountTrustReport report = mapReport(rs, normalizedAction, jwtStatus);
                finalizeTrustDecision(report, normalizedAction, jwtStatus);
                reports.add(report);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du calcul du trust engine : " + e.getMessage(), e);
        }

        reports.sort(Comparator
                .comparingInt((AdaptiveAccountTrustReport report) -> decisionRank(report.getDecision())).reversed()
                .thenComparingInt(AdaptiveAccountTrustReport::getTrustScore)
                .thenComparing(AdaptiveAccountTrustReport::getFullName));
        return reports;
    }

    private String buildTrustQuery() {
        return "SELECT "
                + "u.id AS user_id, "
                + "TRIM(CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, ''))) AS full_name, "
                + "u.email, u.role, u.account_status, u.face_id_enabled, u.failed_login_attempts, u.risk_score, "
                + "u.compromised_password, u.password_score, u.last_login_at, u.last_failed_login_at, u.password_last_changed_at, "
                + "COUNT(DISTINCT ph.id) AS password_history_entries, "
                + "MIN(ph.created_at) AS first_password_record_at, "
                + "MAX(ph.created_at) AS last_password_record_at, "
                + "COUNT(DISTINCT CASE WHEN pra.event_type='REQUEST_ISSUED' "
                + "AND pra.created_at >= DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 DAY), '%Y-%m-%dT%H:%i:%s') THEN pra.id END) AS reset_requests_24h, "
                + "COUNT(DISTINCT CASE WHEN pra.event_type='PASSWORD_RESET' "
                + "AND pra.created_at >= DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 DAY), '%Y-%m-%dT%H:%i:%s') THEN pra.id END) AS reset_attempts_24h, "
                + "COUNT(DISTINCT CASE WHEN pra.event_status IN ('FAILED','BLOCKED') "
                + "AND pra.created_at >= DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 DAY), '%Y-%m-%dT%H:%i:%s') THEN pra.id END) AS reset_failures_24h, "
                + "MAX(CASE WHEN pra.event_type IN ('REQUEST_ISSUED','PASSWORD_RESET','LINK_VALIDATION') THEN pra.created_at END) AS last_reset_event_at, "
                + "MAX(CASE "
                + "WHEN NULLIF(TRIM(u.first_name), '') IS NOT NULL THEN 1 ELSE 0 END "
                + "+ CASE WHEN NULLIF(TRIM(u.last_name), '') IS NOT NULL THEN 1 ELSE 0 END "
                + "+ CASE WHEN NULLIF(TRIM(u.email), '') IS NOT NULL THEN 1 ELSE 0 END "
                + "+ CASE WHEN NULLIF(TRIM(u.phone), '') IS NOT NULL THEN 1 ELSE 0 END "
                + "+ CASE WHEN NULLIF(TRIM(u.birth_date), '') IS NOT NULL THEN 1 ELSE 0 END "
                + "+ CASE WHEN NULLIF(TRIM(u.avatar_path), '') IS NOT NULL THEN 1 ELSE 0 END "
                + "+ CASE WHEN NULLIF(TRIM(u.bio), '') IS NOT NULL THEN 1 ELSE 0 END "
                + "+ CASE WHEN u.face_id_enabled = 1 THEN 1 ELSE 0 END) * 100 / 8 AS profile_completion_percent, "
                + "CASE "
                + "WHEN u.account_status='TEMP_LOCKED' THEN 'BLOCK_SIGNAL' "
                + "WHEN u.failed_login_attempts >= 3 THEN 'FAILURE_SIGNAL' "
                + "WHEN COUNT(DISTINCT CASE WHEN pra.event_status IN ('FAILED','BLOCKED') "
                + "AND pra.created_at >= DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 DAY), '%Y-%m-%dT%H:%i:%s') THEN pra.id END) >= 3 THEN 'RESET_SIGNAL' "
                + "ELSE 'NORMAL_SIGNAL' END AS sql_signal "
                + "FROM `" + USERS_TABLE + "` u "
                + "LEFT JOIN `" + PASSWORD_HISTORY_TABLE + "` ph ON ph.user_id = u.id "
                + "LEFT JOIN `" + PASSWORD_RESET_AUDIT_TABLE + "` pra ON pra.user_id = u.id "
                + "WHERE COALESCE(u.is_archived, 0) = 0 "
                + "GROUP BY u.id, u.first_name, u.last_name, u.email, u.role, u.account_status, u.face_id_enabled, "
                + "u.failed_login_attempts, u.risk_score, u.compromised_password, u.password_score, "
                + "u.last_login_at, u.last_failed_login_at, u.password_last_changed_at "
                + "HAVING COUNT(DISTINCT ph.id) >= 0 "
                + "ORDER BY "
                + "CASE "
                + "WHEN u.account_status='TEMP_LOCKED' THEN 4 "
                + "WHEN u.failed_login_attempts >= 3 THEN 3 "
                + "WHEN COUNT(DISTINCT CASE WHEN pra.event_status IN ('FAILED','BLOCKED') "
                + "AND pra.created_at >= DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 DAY), '%Y-%m-%dT%H:%i:%s') THEN pra.id END) >= 3 THEN 2 "
                + "ELSE 1 END DESC, "
                + "u.risk_score DESC, u.id DESC";
    }

    private AdaptiveAccountTrustReport mapReport(ResultSet rs, String actionContext, String jwtStatus) throws SQLException {
        AdaptiveAccountTrustReport report = new AdaptiveAccountTrustReport();
        report.setUserId(rs.getInt("user_id"));
        report.setFullName(safe(rs.getString("full_name")));
        report.setEmail(safe(rs.getString("email")));
        report.setRole(safe(rs.getString("role")));
        report.setActionContext(actionContext);
        report.setJwtStatus(jwtStatus);
        report.setProfileCompletionPercent(rs.getInt("profile_completion_percent"));
        report.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
        report.setResetRequests24h(rs.getInt("reset_requests_24h"));
        report.setResetFailures24h(rs.getInt("reset_failures_24h"));
        report.setResetAttempts24h(rs.getInt("reset_attempts_24h"));

        LocalDateTime firstPasswordRecord = parseDate(safe(rs.getString("first_password_record_at")));
        LocalDateTime lastPasswordChange = parseDate(safe(rs.getString("password_last_changed_at")));
        LocalDateTime lastLogin = parseDate(safe(rs.getString("last_login_at")));
        LocalDateTime lastFailedLogin = parseDate(safe(rs.getString("last_failed_login_at")));
        LocalDateTime lastResetEvent = parseDate(safe(rs.getString("last_reset_event_at")));

        LocalDateTime accountReference = firstNonNull(firstPasswordRecord, lastPasswordChange, lastLogin, lastResetEvent);
        report.setAccountSeniorityDays(accountReference == null
                ? 0
                : (int) Math.max(0, ChronoUnit.DAYS.between(accountReference.toLocalDate(), LocalDate.now())));

        LocalDateTime lastActivity = mostRecent(lastLogin, lastFailedLogin, lastPasswordChange, lastResetEvent);
        report.setLastActivityAt(lastActivity == null ? "" : lastActivity.format(DATE_FORMATTER));
        report.setInactivityDays(lastActivity == null
                ? 999
                : (int) Math.max(0, ChronoUnit.DAYS.between(lastActivity.toLocalDate(), LocalDate.now())));

        report.setRiskLevel("LOW");
        report.setDecision("LOW_TRUST");
        report.setTrustScore(0);
        return report;
    }

    private void finalizeTrustDecision(AdaptiveAccountTrustReport report, String actionContext, String jwtStatus) {
        int score = 100;

        if ("VALID".equals(jwtStatus)) {
            score += 8;
        } else if (requiresJwt(actionContext) && "INVALID".equals(jwtStatus)) {
            score -= 30;
        } else if (requiresJwt(actionContext) && "MISSING".equals(jwtStatus)) {
            score -= 20;
        }

        score += profileCompletionBonus(report.getProfileCompletionPercent());
        score += accountSeniorityBonus(report.getAccountSeniorityDays());
        score -= inactivityPenalty(report.getInactivityDays());
        score -= Math.min(24, report.getFailedLoginAttempts() * 8);
        score -= Math.min(18, report.getResetRequests24h() * 4);
        score -= Math.min(28, report.getResetFailures24h() * 9);
        score -= Math.min(18, report.getResetAttempts24h() * 5);

        try {
            var securitySnapshot = serviceUser.getSecuritySnapshot(report.getUserId());
            score -= Math.min(25, securitySnapshot.riskScore() / 4);
            if (securitySnapshot.compromisedPassword()) {
                score -= 18;
            }
            if (securitySnapshot.passwordScore() >= 85) {
                score += 6;
            } else if (securitySnapshot.passwordScore() < 60) {
                score -= 8;
            }
        } catch (RuntimeException ignored) {
            score -= 5;
        }

        if (requiresFaceIdPreference(actionContext)) {
            try {
                var user = serviceUser.getUserById(report.getUserId()).orElse(null);
                if (user != null && user.isFaceIdEnabled()) {
                    score += 10;
                } else if ("FACE_ID_ENABLE".equals(actionContext)) {
                    score += 0;
                } else {
                    score -= 8;
                }
            } catch (RuntimeException ignored) {
                score -= 4;
            }
        }

        score = Math.max(0, Math.min(100, score));
        report.setTrustScore(score);

        String decision;
        String riskLevel;
        if (score < 25 || report.getFailedLoginAttempts() >= 4 || report.getResetFailures24h() >= 4) {
            decision = "BLOCKED";
            riskLevel = "CRITICAL";
        } else if (score < 45 || report.getResetFailures24h() >= 2 || report.getInactivityDays() >= 45) {
            decision = "SUSPICIOUS";
            riskLevel = "HIGH";
        } else if (score < 70) {
            decision = "LOW_TRUST";
            riskLevel = "MEDIUM";
        } else {
            decision = "TRUSTED";
            riskLevel = "LOW";
        }

        if (requiresJwt(actionContext) && "INVALID".equals(jwtStatus)) {
            decision = "BLOCKED";
            riskLevel = "CRITICAL";
        }

        report.setDecision(decision);
        report.setRiskLevel(riskLevel);
        report.setTrustSummary(buildSummary(report));
    }

    private int profileCompletionBonus(int completionPercent) {
        if (completionPercent >= 90) {
            return 10;
        }
        if (completionPercent >= 70) {
            return 5;
        }
        if (completionPercent >= 50) {
            return 0;
        }
        return -8;
    }

    private int accountSeniorityBonus(int accountSeniorityDays) {
        if (accountSeniorityDays >= 180) {
            return 10;
        }
        if (accountSeniorityDays >= 30) {
            return 5;
        }
        if (accountSeniorityDays >= 7) {
            return 0;
        }
        return -8;
    }

    private int inactivityPenalty(int inactivityDays) {
        if (inactivityDays >= 120) {
            return 14;
        }
        if (inactivityDays >= 60) {
            return 10;
        }
        if (inactivityDays >= 30) {
            return 6;
        }
        if (inactivityDays >= 14) {
            return 3;
        }
        return 0;
    }

    private boolean requiresJwt(String actionContext) {
        return "PASSWORD_CHANGE".equals(actionContext)
                || "FACE_ID_ENABLE".equals(actionContext)
                || "EMAIL_UPDATE".equals(actionContext);
    }

    private boolean requiresFaceIdPreference(String actionContext) {
        return "PASSWORD_CHANGE".equals(actionContext)
                || "EMAIL_UPDATE".equals(actionContext)
                || "ADMIN_REVIEW".equals(actionContext);
    }

    private String buildSummary(AdaptiveAccountTrustReport report) {
        return "JWT=" + report.getJwtStatus()
                + " | Profil=" + report.getProfileCompletionPercent() + "%"
                + " | Echecs login=" + report.getFailedLoginAttempts()
                + " | Reset 24h=" + report.getResetRequests24h()
                + " | Fail reset 24h=" + report.getResetFailures24h()
                + " | Inactivite=" + report.getInactivityDays() + "j";
    }

    private int decisionRank(String decision) {
        return switch (safe(decision)) {
            case "BLOCKED" -> 4;
            case "SUSPICIOUS" -> 3;
            case "LOW_TRUST" -> 2;
            default -> 1;
        };
    }

    private String normalizeAction(String actionContext) {
        String action = safe(actionContext).trim().toUpperCase(Locale.ROOT);
        if (action.isBlank()) {
            return "ADMIN_REVIEW";
        }
        return action;
    }

    private String resolveJwtStatus(String authorizationHeader) {
        String token = safe(authorizationHeader).trim();
        if (token.isBlank()) {
            return "MISSING";
        }
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        }
        try {
            jwtService.validate(token);
            return "VALID";
        } catch (RuntimeException e) {
            return "INVALID";
        }
    }

    private LocalDateTime parseDate(String value) {
        if (safe(value).isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, DATE_FORMATTER);
        } catch (Exception firstError) {
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception secondError) {
                return null;
            }
        }
    }

    private LocalDateTime firstNonNull(LocalDateTime... values) {
        for (LocalDateTime value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private LocalDateTime mostRecent(LocalDateTime... values) {
        LocalDateTime latest = null;
        for (LocalDateTime value : values) {
            if (value != null && (latest == null || value.isAfter(latest))) {
                latest = value;
            }
        }
        return latest;
    }

    private void ensureConnection() {
        if (cnx == null) {
            throw new IllegalStateException("Connexion MySQL indisponible. Verifiez la base fitopiabd.");
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
