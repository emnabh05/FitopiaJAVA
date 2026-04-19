package tn.esprit.Pidev3A49.api.dto;

public record ChangePasswordResponse(
        String message,
        int passwordScore,
        String passwordStrength,
        boolean compromisedPassword,
        String passwordLastChangedAt,
        int riskScore,
        String accountStatus
) {
}
