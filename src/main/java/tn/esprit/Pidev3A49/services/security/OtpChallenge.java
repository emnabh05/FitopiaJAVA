package tn.esprit.Pidev3A49.services.security;

import java.time.LocalDateTime;

public record OtpChallenge(String recipient, String code, LocalDateTime expiresAt) {
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }
}
