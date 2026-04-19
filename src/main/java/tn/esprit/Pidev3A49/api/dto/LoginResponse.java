package tn.esprit.Pidev3A49.api.dto;

import tn.esprit.Pidev3A49.services.security.AuthenticationResult;

public record LoginResponse(
        AuthenticationResult.Status status,
        String message,
        Integer userId,
        String username,
        String email,
        Integer riskScore,
        String accountStatus
) {
}
