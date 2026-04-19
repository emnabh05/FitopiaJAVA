package tn.esprit.Pidev3A49.api.dto;

public record ChangePasswordRequest(
        String newPassword,
        String firstName,
        String lastName,
        String username,
        String email,
        String birthDate
) {
}
