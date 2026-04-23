package tn.esprit.Pidev3A49.services.security;

public record JwtClaims(
        int userId,
        String role,
        String subject,
        long issuedAtEpochSeconds,
        long expiresAtEpochSeconds,
        String jwtId
) {
}
