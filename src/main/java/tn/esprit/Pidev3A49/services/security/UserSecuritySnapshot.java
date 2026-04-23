package tn.esprit.Pidev3A49.services.security;

import java.util.List;

public record UserSecuritySnapshot(
        int userId,
        int passwordScore,
        String passwordStrength,
        boolean compromisedPassword,
        int compromisedOccurrences,
        int failedLoginAttempts,
        int riskScore,
        String accountStatus,
        String passwordLastChangedAt,
        String lockedUntil,
        String lastLoginAt,
        String lastFailedLoginAt,
        List<String> alerts
) {
}
