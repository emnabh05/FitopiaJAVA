package tn.esprit.Pidev3A49.services.security;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

public class PwnedPasswordClient {
    private static final String API_URL = "https://api.pwnedpasswords.com/range/";
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public BreachCheckResult checkPassword(String rawPassword) {
        try {
            String sha1 = sha1Hex(rawPassword);
            String prefix = sha1.substring(0, 5);
            String suffix = sha1.substring(5);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + prefix))
                    .timeout(Duration.ofSeconds(8))
                    .header("user-agent", "FitopiaJavaFX-Security")
                    .header("Add-Padding", "true")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                return new BreachCheckResult(false, 0, false, "Service HIBP indisponible (" + response.statusCode() + ").");
            }

            String[] lines = response.body().split("\\R");
            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length != 2) {
                    continue;
                }
                if (suffix.equalsIgnoreCase(parts[0].trim())) {
                    int count = parseCount(parts[1].trim());
                    return new BreachCheckResult(true, count, true, "Mot de passe retrouve dans une fuite connue.");
                }
            }
            return new BreachCheckResult(false, 0, true, "Mot de passe absent des fuites connues.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new BreachCheckResult(false, 0, false, "Verification HIBP indisponible: " + e.getMessage());
        } catch (IOException e) {
            return new BreachCheckResult(false, 0, false, "Verification HIBP indisponible: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            return new BreachCheckResult(false, 0, false, "SHA-1 indisponible: " + e.getMessage());
        }
    }

    private String sha1Hex(String rawPassword) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash).toUpperCase();
    }

    private int parseCount(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public record BreachCheckResult(boolean compromised, int occurrences, boolean checkAvailable, String message) {
    }
}
