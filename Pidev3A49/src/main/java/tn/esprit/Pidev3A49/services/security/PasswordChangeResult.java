package tn.esprit.Pidev3A49.services.security;

public record PasswordChangeResult(
        PasswordPolicyReport report,
        UserSecuritySnapshot snapshot,
        String message
) {
}
