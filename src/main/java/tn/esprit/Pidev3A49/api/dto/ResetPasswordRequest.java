package tn.esprit.Pidev3A49.api.dto;

public record ResetPasswordRequest(String email, String otpCode, String newPassword) {
}
