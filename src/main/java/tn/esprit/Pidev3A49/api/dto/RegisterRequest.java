package tn.esprit.Pidev3A49.api.dto;

public record RegisterRequest(
        String firstName,
        String lastName,
        String username,
        String email,
        String password,
        String birthDate,
        String role,
        String phone,
        String gender
) {
}
