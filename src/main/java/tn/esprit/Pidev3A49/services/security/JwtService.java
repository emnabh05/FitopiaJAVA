package tn.esprit.Pidev3A49.services.security;

import tn.esprit.Pidev3A49.Models.FitopiaUser;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JwtService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String JWT_TYPE = "JWT";
    private static final String JWT_ALGORITHM = "HS256";
    private static final Pattern STRING_CLAIM_PATTERN_TEMPLATE = Pattern.compile("\"%s\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern NUMBER_CLAIM_PATTERN_TEMPLATE = Pattern.compile("\"%s\"\\s*:\\s*([0-9]+)");

    private final byte[] secret;
    private final long expirySeconds;

    public JwtService() {
        this(resolveSecret(), resolveExpirySeconds());
    }

    public JwtService(String secret, long expirySeconds) {
        String normalizedSecret = secret == null ? "" : secret.trim();
        if (normalizedSecret.isBlank()) {
            throw new IllegalArgumentException("JWT secret is required.");
        }
        if (expirySeconds <= 0) {
            throw new IllegalArgumentException("JWT expiry must be positive.");
        }
        this.secret = normalizedSecret.getBytes(StandardCharsets.UTF_8);
        this.expirySeconds = expirySeconds;
    }

    public String generateAccessToken(FitopiaUser user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required to generate JWT.");
        }
        long now = Instant.now().getEpochSecond();
        long exp = now + expirySeconds;
        String jti = buildTokenId();
        String role = safe(user.getRole()).isBlank() ? "Patient" : safe(user.getRole());
        String subject = safe(user.getEmail()).isBlank() ? safe(user.getUsername()) : safe(user.getEmail());

        String headerJson = "{\"alg\":\"" + JWT_ALGORITHM + "\",\"typ\":\"" + JWT_TYPE + "\"}";
        String payloadJson = "{"
                + "\"sub\":\"" + escapeJson(subject) + "\","
                + "\"uid\":" + user.getId() + ","
                + "\"role\":\"" + escapeJson(role) + "\","
                + "\"iat\":" + now + ","
                + "\"exp\":" + exp + ","
                + "\"jti\":\"" + jti + "\""
                + "}";

        String encodedHeader = base64UrlEncode(headerJson);
        String encodedPayload = base64UrlEncode(payloadJson);
        String signingInput = encodedHeader + "." + encodedPayload;
        String signature = sign(signingInput);
        return signingInput + "." + signature;
    }

    public JwtClaims validate(String token) {
        String normalized = safe(token).trim();
        if (normalized.isBlank()) {
            throw new RuntimeException("Token JWT manquant.");
        }

        String[] parts = normalized.split("\\.");
        if (parts.length != 3) {
            throw new RuntimeException("Format JWT invalide.");
        }

        String expectedSignature = sign(parts[0] + "." + parts[1]);
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw new RuntimeException("Signature JWT invalide.");
        }

        String headerJson = base64UrlDecode(parts[0]);
        String payloadJson = base64UrlDecode(parts[1]);
        String alg = extractStringClaim(headerJson, "alg");
        String typ = extractStringClaim(headerJson, "typ");
        if (!JWT_ALGORITHM.equalsIgnoreCase(alg) || !JWT_TYPE.equalsIgnoreCase(typ)) {
            throw new RuntimeException("Header JWT invalide.");
        }

        int uid = Integer.parseInt(extractNumberClaim(payloadJson, "uid"));
        String role = extractStringClaim(payloadJson, "role");
        String subject = extractStringClaim(payloadJson, "sub");
        long iat = Long.parseLong(extractNumberClaim(payloadJson, "iat"));
        long exp = Long.parseLong(extractNumberClaim(payloadJson, "exp"));
        String jti = extractStringClaim(payloadJson, "jti");

        long now = Instant.now().getEpochSecond();
        if (exp <= now) {
            throw new RuntimeException("Token JWT expire.");
        }
        if (iat > now + 30) {
            throw new RuntimeException("Token JWT invalide (iat).");
        }

        return new JwtClaims(uid, role, subject, iat, exp, jti);
    }

    public long getExpirySeconds() {
        return expirySeconds;
    }

    private String extractStringClaim(String json, String claim) {
        Pattern pattern = Pattern.compile(String.format(STRING_CLAIM_PATTERN_TEMPLATE.pattern(), claim));
        Matcher matcher = pattern.matcher(safe(json));
        if (!matcher.find()) {
            throw new RuntimeException("Claim JWT manquant: " + claim);
        }
        return matcher.group(1);
    }

    private String extractNumberClaim(String json, String claim) {
        Pattern pattern = Pattern.compile(String.format(NUMBER_CLAIM_PATTERN_TEMPLATE.pattern(), claim));
        Matcher matcher = pattern.matcher(safe(json));
        if (!matcher.find()) {
            throw new RuntimeException("Claim JWT manquant: " + claim);
        }
        return matcher.group(1);
    }

    private String sign(String signingInput) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            byte[] signature = mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la signature JWT: " + e.getMessage(), e);
        }
    }

    private String base64UrlEncode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(safe(value).getBytes(StandardCharsets.UTF_8));
    }

    private String base64UrlDecode(String value) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(safe(value));
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Token JWT non decodable.");
        }
    }

    private String escapeJson(String value) {
        return safe(value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String buildTokenId() {
        byte[] randomBytes = new byte[16];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private static String resolveSecret() {
        String fromEnv = System.getenv("JWT_SECRET");
        if (fromEnv != null && !fromEnv.trim().isBlank()) {
            return fromEnv.trim();
        }
        return "fitopia-dev-jwt-secret-change-in-production-2026";
    }

    private static long resolveExpirySeconds() {
        String fromEnv = System.getenv("JWT_EXP_MINUTES");
        if (fromEnv != null && !fromEnv.trim().isBlank()) {
            try {
                long minutes = Long.parseLong(fromEnv.trim());
                return Math.max(1, minutes) * 60;
            } catch (NumberFormatException ignored) {
                return 60 * 60;
            }
        }
        return 60 * 60;
    }
}
