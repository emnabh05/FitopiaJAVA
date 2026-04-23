package tn.esprit.Pidev3A49.test;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import tn.esprit.Pidev3A49.services.ServiceUser;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public final class PasswordResetWebServer {
    private static final PasswordResetWebServer INSTANCE = new PasswordResetWebServer();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int DEFAULT_PORT = 8787;
    private static final String RESET_PATH = "/reset-password";

    private final ServiceUser serviceUser = new ServiceUser();
    private final SecureRandom random = new SecureRandom();
    private final byte[] challengeSecret = resolveChallengeSecret().getBytes(StandardCharsets.UTF_8);
    private final Map<String, RecoveryCompletionStatus> recoveryStatuses = new ConcurrentHashMap<>();

    private HttpServer server;
    private int port = DEFAULT_PORT;

    private PasswordResetWebServer() {
    }

    public static PasswordResetWebServer getInstance() {
        return INSTANCE;
    }

    public synchronized void ensureStarted() {
        if (server != null) {
            return;
        }
        try {
            server = createServerOnPort(DEFAULT_PORT);
        } catch (IOException bindError) {
            try {
                // Fallback to an ephemeral free port when the default one is already in use.
                server = createServerOnPort(0);
            } catch (IOException fallbackError) {
                throw new RuntimeException("Impossible de demarrer le serveur web de reinitialisation: " + fallbackError.getMessage(), fallbackError);
            }
        }
        try {
            server.createContext(RESET_PATH, new ResetPasswordHandler());
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            port = server.getAddress().getPort();
        } catch (RuntimeException e) {
            throw e;
        }
    }

    private HttpServer createServerOnPort(int requestedPort) throws IOException {
        return HttpServer.create(new InetSocketAddress("127.0.0.1", requestedPort), 0);
    }

    public String buildResetUrl(String email, String token) {
        ensureStarted();
        return "http://127.0.0.1:" + port + RESET_PATH
                + "?email=" + urlEncode(email)
                + "&token=" + urlEncode(token);
    }

    public RecoveryCompletionStatus consumeRecoveryStatus(String email) {
        String normalizedEmail = safe(email).trim().toLowerCase();
        if (normalizedEmail.isBlank()) {
            return null;
        }
        return recoveryStatuses.remove(normalizedEmail);
    }

    private final class ResetPasswordHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleGet(exchange);
                return;
            }
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                handlePost(exchange);
                return;
            }
            sendHtml(exchange, 405, htmlLayout("Methode non autorisee", "<p>Utilisez GET ou POST.</p>"));
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());
            String email = safe(params.get("email")).trim();
            String token = safe(params.get("token")).trim();
            if (email.isBlank() || token.isBlank()) {
                sendHtml(exchange, 400, htmlLayout("Lien invalide", "<p>Le lien de reinitialisation est incomplet.</p>"));
                return;
            }

            ServiceUser.PasswordRecoveryPreview preview;
            try {
                preview = serviceUser.previewPasswordRecovery(email, token, clientIp(exchange), clientUserAgent(exchange));
            } catch (RuntimeException e) {
                sendHtml(exchange, 400, htmlLayout("Lien invalide", "<p>" + escapeHtml(e.getMessage()) + "</p>"));
                return;
            }

            HumanChallenge challenge = issueChallenge(token, email);
            String body = "<h2>Smart Password Recovery with Risk Verification</h2>"
                    + "<p>Compte: <b>" + escapeHtml(email) + "</b></p>"
                    + "<p>Confirmez que vous etes legitime puis definissez un nouveau mot de passe.</p>"
                    + "<div class=\"risk-box\">"
                    + "<p><b>Expiration du token:</b> " + escapeHtml(preview.expiresAt()) + "</p>"
                    + "<p><b>Tentatives recentes:</b> " + preview.recentAttempts() + " | <b>Echecs:</b> " + preview.recentFailures() + "</p>"
                    + "<p><b>Niveau de risque:</b> " + escapeHtml(preview.riskLevel()) + "</p>"
                    + "<p>" + escapeHtml(preview.message()) + "</p>"
                    + "</div>"
                    + "<form method=\"post\" action=\"" + RESET_PATH + "\">"
                    + "<input type=\"hidden\" name=\"email\" value=\"" + escapeHtml(email) + "\"/>"
                    + "<input type=\"hidden\" name=\"token\" value=\"" + escapeHtml(token) + "\"/>"
                    + "<input type=\"hidden\" name=\"a\" value=\"" + challenge.a + "\"/>"
                    + "<input type=\"hidden\" name=\"b\" value=\"" + challenge.b + "\"/>"
                    + "<input type=\"hidden\" name=\"proof\" value=\"" + challenge.proof + "\"/>"
                    + "<label>Nouveau mot de passe</label><input type=\"password\" name=\"newPassword\" required/>"
                    + "<label>Confirmer le mot de passe</label><input type=\"password\" name=\"confirmPassword\" required/>"
                    + "<label><input type=\"checkbox\" name=\"humanCheck\"/> Je confirme etre le proprietaire du compte.</label>"
                    + "<label>Verification anti-bot: " + challenge.a + " + " + challenge.b + " = ?</label>"
                    + "<input type=\"text\" name=\"humanAnswer\" required/>"
                    + "<button type=\"submit\">Valider la reinitialisation</button>"
                    + "</form>";
            sendHtml(exchange, 200, htmlLayout("Reset Password", body));
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange.getRequestBody());
            Map<String, String> form = parseQuery(body);
            String email = safe(form.get("email")).trim();
            String token = safe(form.get("token")).trim();
            String newPassword = safe(form.get("newPassword")).trim();
            String confirmPassword = safe(form.get("confirmPassword")).trim();
            String humanCheck = safe(form.get("humanCheck")).trim();
            String humanAnswer = safe(form.get("humanAnswer")).trim();
            String a = safe(form.get("a")).trim();
            String b = safe(form.get("b")).trim();
            String proof = safe(form.get("proof")).trim();

            if (email.isBlank() || token.isBlank()) {
                sendHtml(exchange, 400, htmlLayout("Erreur", "<p>Lien de reinitialisation invalide.</p>"));
                return;
            }
            if (newPassword.isBlank() || confirmPassword.isBlank()) {
                sendHtml(exchange, 400, htmlLayout("Erreur", "<p>Le mot de passe et la confirmation sont obligatoires.</p>"));
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                sendHtml(exchange, 400, htmlLayout("Erreur", "<p>La confirmation du mot de passe ne correspond pas.</p>"));
                return;
            }
            if (!"on".equalsIgnoreCase(humanCheck)) {
                sendHtml(exchange, 400, htmlLayout("Erreur", "<p>Veuillez confirmer que vous etes le proprietaire du compte.</p>"));
                return;
            }
            if (!verifyChallenge(token, email, a, b, humanAnswer, proof)) {
                sendHtml(exchange, 400, htmlLayout("Erreur", "<p>Verification anti-bot invalide.</p>"));
                return;
            }

            try {
                ServiceUser.SmartPasswordRecoveryResult result = serviceUser.resetPasswordWithRiskVerification(
                        email,
                        token,
                        newPassword,
                        clientIp(exchange),
                        clientUserAgent(exchange)
                );
                recoveryStatuses.put(email.toLowerCase(), new RecoveryCompletionStatus(
                        result.email(),
                        result.passwordUpdated(),
                        result.confirmationMessage(),
                        result.riskLevel(),
                        result.changedAt()
                ));
                String success = "<h2>Mot de passe mis a jour</h2>"
                        + "<p>La reinitialisation intelligente est terminee. Vous pouvez revenir a l'application et vous connecter.</p>"
                        + "<p><b>Score du mot de passe:</b> " + result.report().score() + "/100"
                        + " | <b>Force:</b> " + escapeHtml(result.report().strengthLabel()) + "</p>"
                        + "<p><b>Niveau de risque:</b> " + escapeHtml(result.riskLevel()) + "</p>"
                        + "<p><b>Historisation securite:</b> evenement enregistre avec succes.</p>"
                        + "<p><small>Heure: " + LocalDateTime.now().format(DATE_FORMATTER) + "</small></p>";
                sendHtml(exchange, 200, htmlLayout("Succes", success));
            } catch (RuntimeException e) {
                recoveryStatuses.put(email.toLowerCase(), new RecoveryCompletionStatus(
                        email,
                        false,
                        e.getMessage(),
                        "FAILED",
                        LocalDateTime.now().format(DATE_FORMATTER)
                ));
                sendHtml(exchange, 400, htmlLayout("Erreur", "<p>" + escapeHtml(e.getMessage()) + "</p>"));
            }
        }
    }

    private HumanChallenge issueChallenge(String token, String email) {
        int a = random.nextInt(8) + 2;
        int b = random.nextInt(8) + 2;
        int answer = a + b;
        String payload = token + "|" + email + "|" + a + "|" + b + "|" + answer;
        String proof = sign(payload);
        return new HumanChallenge(a, b, proof);
    }

    private boolean verifyChallenge(String token, String email, String a, String b, String providedAnswer, String proof) {
        try {
            int first = Integer.parseInt(a);
            int second = Integer.parseInt(b);
            int answer = Integer.parseInt(providedAnswer);
            String payload = token + "|" + email + "|" + first + "|" + second + "|" + answer;
            String expected = sign(payload);
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), safe(proof).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(challengeSecret, "HmacSHA256"));
            byte[] signature = mac.doFinal(safe(value).getBytes(StandardCharsets.UTF_8));
            return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception e) {
            throw new RuntimeException("Erreur de signature anti-bot: " + e.getMessage(), e);
        }
    }

    private String htmlLayout(String title, String body) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>" + escapeHtml(title) + "</title>"
                + "<style>"
                + "body{font-family:Segoe UI,Arial,sans-serif;background:#f4f7f8;margin:0;padding:32px;color:#1f2e33;}"
                + ".card{max-width:560px;margin:0 auto;background:white;border:1px solid #d9e4e7;border-radius:12px;padding:24px;}"
                + ".risk-box{background:#f2f7f8;border:1px solid #d9e4e7;border-radius:10px;padding:14px;margin:16px 0;}"
                + "h2{margin-top:0;color:#12343b;}label{display:block;margin:12px 0 6px;font-weight:600;}"
                + "input[type=password],input[type=text]{width:100%;padding:10px;border:1px solid #c8d6db;border-radius:8px;}"
                + "button{margin-top:16px;background:#1f6a5f;color:white;border:none;padding:10px 16px;border-radius:8px;font-weight:700;cursor:pointer;}"
                + "p{line-height:1.5;}"
                + "</style></head><body><div class=\"card\">" + body + "</div></body></html>";
    }

    private void sendHtml(HttpExchange exchange, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Map<String, String> parseQuery(String raw) {
        Map<String, String> result = new HashMap<>();
        if (raw == null || raw.isBlank()) {
            return result;
        }
        String[] pairs = raw.split("&");
        for (String pair : pairs) {
            if (pair.isBlank()) {
                continue;
            }
            String[] kv = pair.split("=", 2);
            String key = urlDecode(kv[0]);
            String value = kv.length > 1 ? urlDecode(kv[1]) : "";
            result.put(key, value);
        }
        return result;
    }

    private String readRequestBody(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(safe(value), StandardCharsets.UTF_8);
    }

    private String urlDecode(String value) {
        return URLDecoder.decode(safe(value), StandardCharsets.UTF_8);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String escapeHtml(String value) {
        return safe(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String clientIp(HttpExchange exchange) {
        return exchange == null || exchange.getRemoteAddress() == null
                ? ""
                : safe(exchange.getRemoteAddress().getAddress() == null
                ? exchange.getRemoteAddress().toString()
                : exchange.getRemoteAddress().getAddress().getHostAddress());
    }

    private String clientUserAgent(HttpExchange exchange) {
        return exchange == null ? "" : safe(exchange.getRequestHeaders().getFirst("User-Agent"));
    }

    private static String resolveChallengeSecret() {
        String fromEnv = System.getenv("RESET_HUMAN_SECRET");
        if (fromEnv != null && !fromEnv.trim().isBlank()) {
            return fromEnv.trim();
        }
        return "fitopia-reset-human-secret-change-in-production-2026";
    }

    private record HumanChallenge(int a, int b, String proof) {
    }

    public record RecoveryCompletionStatus(
            String email,
            boolean passwordUpdated,
            String message,
            String riskLevel,
            String changedAt
    ) {
    }
}
