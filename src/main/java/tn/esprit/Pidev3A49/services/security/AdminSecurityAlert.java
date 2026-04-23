package tn.esprit.Pidev3A49.services.security;

public record AdminSecurityAlert(
        int userId,
        String username,
        String email,
        int riskScore,
        String accountStatus,
        String securityAlertSummary
) {
}
