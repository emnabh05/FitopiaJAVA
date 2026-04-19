package tn.esprit.Pidev3A49.api.dto;

public record RegisterResponse(
        String message,
        int userId,
        int passwordScore,
        String passwordStrength,
        boolean compromisedPassword,
        int riskScore,
        String accountStatus
) {
}
