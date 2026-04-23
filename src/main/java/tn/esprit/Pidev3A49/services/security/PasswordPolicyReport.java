package tn.esprit.Pidev3A49.services.security;

import java.util.List;

public record PasswordPolicyReport(
        int score,
        String strengthLabel,
        boolean accepted,
        boolean compromised,
        int compromisedOccurrences,
        boolean breachCheckAvailable,
        List<String> feedback
) {
}
