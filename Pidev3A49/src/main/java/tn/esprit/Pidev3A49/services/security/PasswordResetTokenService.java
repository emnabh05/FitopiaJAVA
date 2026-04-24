package tn.esprit.Pidev3A49.services.security;

import tn.esprit.Pidev3A49.Models.FitopiaUser;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordResetTokenService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Pattern STRING_CLAIM_PATTERN_TEMPLATE = Pattern.compile("\"%s\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern NUMBER_CLAIM_PATTERN_TEMPLATE = Pattern.compile("\"%s\"\\s*:\\s*([0-9]+)");

    private final byte[] secret;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetTokenService() {
        this(resolveSecret());
    }

    public PasswordResetTokenService(String secret) {
        String normalizedSecret = secret == null ? "" : secret.trim();
        if (normalizedSecret.isBlank()) {
            throw new IllegalArgumentException("Reset token secret is required.");
        }
        this.secret = normalizedSecret.getBytes(StandardCharsets.UTF_8);
    }

    public IssuedResetToken issue(FitopiaUser user, Duration validity) {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }
        Duration normalizedValidity = validity == null ? Duration.ofMinutes(10) : validity;
        long now = Instant.now().getEpochSecond();
        long exp = now + Math.max(60L, normalizedValidity.getSeconds());
        String nonce = generateNonce();
        String subject = safe(user.getEmail()).isBlank() ? safe(user.getUsername()) : safe(user.getEmail());

        String payload = "{"
                + "\"sub\":\"" + escapeJson(subject) + "\","
                + "\"uid\":" + user.getId() + ","
                + "\"nonce\":\"" + nonce + "\","
                + "\"purpose\":\"password_reset\","
                + "\"iat\":" + now + ","
                + "\"exp\":" + exp
                + "}";

        String encodedPayload = base64UrlEncode(payload);
        String signature = sign(encodedPayload);
        String token = encodedPayload + "." + signature;
        LocalDateTime expiresAt = LocalDateTime.ofInstant(Instant.ofEpochSecond(exp), ZoneId.systemDefault());
        return new IssuedResetToken(token, nonce, expiresAt);
    }

    public ResetTokenClaims verify(String token) {
        String normalized = safe(token).trim();
        if (normalized.isBlank()) {
            throw new RuntimeException("Token de reinitialisation manquant.");
        }

        String[] parts = normalized.split("\\.");
        if (parts.length != 2) {
            throw new RuntimeException("Format token de reinitialisation invalide.");
        }

        String expectedSignature = sign(parts[0]);
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[1].getBytes(StandardCharsets.UTF_8))) {
            throw new RuntimeException("Signature du token de reinitialisation invalide.");
        }

        String payloadJson = base64UrlDecode(parts[0]);
        String subject = extractStringClaim(payloadJson, "sub");
        int userId = Integer.parseInt(extractNumberClaim(payloadJson, "uid"));
        String nonce = extractStringClaim(payloadJson, "nonce");
        String purpose = extractStringClaim(payloadJson, "purpose");
        long iat = Long.parseLong(extractNumberClaim(payloadJson, "iat"));
        long exp = Long.parseLong(extractNumberClaim(payloadJson, "exp"));

        if (!"password_reset".equals(purpose)) {
            throw new RuntimeException("Token de reinitialisation invalide.");
        }
        long now = Instant.now().getEpochSecond();
        if (exp <= now) {
            throw new RuntimeException("Le token de reinitialisation a expire.");
        }
        if (iat > now + 30) {
            throw new RuntimeException("Token de reinitialisation invalide.");
        }

        return new ResetTokenClaims(userId, subject, nonce, iat, exp);
    }

    private String generateNonce() {
        byte[] bytes = new byte[12];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            byte[] signature = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception e) {
            throw new RuntimeException("Erreur de signature token: " + e.getMessage(), e);
        }
    }

    private String extractStringClaim(String json, String claim) {
        Pattern pattern = Pattern.compile(String.format(STRING_CLAIM_PATTERN_TEMPLATE.pattern(), claim));
        Matcher matcher = pattern.matcher(safe(json));
        if (!matcher.find()) {
            throw new RuntimeException("Claim manquant: " + claim);
        }
        return matcher.group(1);
    }

    private String extractNumberClaim(String json, String claim) {
        Pattern pattern = Pattern.compile(String.format(NUMBER_CLAIM_PATTERN_TEMPLATE.pattern(), claim));
        Matcher matcher = pattern.matcher(safe(json));
        if (!matcher.find()) {
            throw new RuntimeException("Claim manquant: " + claim);
        }
        return matcher.group(1);
    }

    private String base64UrlEncode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(safe(value).getBytes(StandardCharsets.UTF_8));
    }

    private String base64UrlDecode(String value) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(safe(value));
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Token non decodable.");
        }
    }

    private String escapeJson(String value) {
        return safe(value).replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private static String resolveSecret() {
        String fromEnv = System.getenv("RESET_TOKEN_SECRET");
        if (fromEnv != null && !fromEnv.trim().isBlank()) {
            return fromEnv.trim();
        }
        return "fitopia-reset-token-secret-change-in-production-2026";
    }

    public record IssuedResetToken(String token, String nonce, LocalDateTime expiresAt) {
    }

    public record ResetTokenClaims(int userId, String subject, String nonce, long issuedAtEpochSeconds, long expiresAtEpochSeconds) {
    }
}
