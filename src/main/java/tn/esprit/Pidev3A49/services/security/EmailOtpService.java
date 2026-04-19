package tn.esprit.Pidev3A49.services.security;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class EmailOtpService {
    private static final int OTP_LENGTH = 6;
    private static final int OTP_VALIDITY_MINUTES = 5;
    private static final String DEFAULT_RECIPIENT = "landolssirannya@gmail.com";
    private static final Path MAIL_CONFIG_PATH = Path.of("mail.properties");

    private final SecureRandom random = new SecureRandom();
    private OtpChallenge pendingChallenge;

    public OtpChallenge sendLoginOtp(String loginIdentifier) {
        Properties config = loadConfig();
        String smtpEmail = read(config, "FITOPIA_SMTP_EMAIL", "smtp.email");
        String smtpPassword = read(config, "FITOPIA_SMTP_APP_PASSWORD", "smtp.appPassword");
        String recipient = read(config, "FITOPIA_SECURITY_OTP_RECIPIENT", "otp.recipient");
        if (recipient.isBlank()) {
            recipient = DEFAULT_RECIPIENT;
        }

        if (smtpEmail.isBlank() || smtpPassword.isBlank()) {
            throw new RuntimeException("Email OTP non configure. Remplissez mail.properties avec smtp.email et smtp.appPassword.");
        }

        String code = generateCode();
        OtpChallenge challenge = new OtpChallenge(recipient, code, LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));
        sendEmail(smtpEmail, smtpPassword, challenge, loginIdentifier);
        pendingChallenge = challenge;
        return challenge;
    }

    public boolean verifyCode(String code) {
        if (pendingChallenge == null || pendingChallenge.isExpired()) {
            pendingChallenge = null;
            return false;
        }
        boolean matches = pendingChallenge.code().equals(code == null ? "" : code.trim());
        if (matches) {
            pendingChallenge = null;
        }
        return matches;
    }

    public Optional<OtpChallenge> getPendingChallenge() {
        if (pendingChallenge != null && pendingChallenge.isExpired()) {
            pendingChallenge = null;
        }
        return Optional.ofNullable(pendingChallenge);
    }

    private void sendEmail(String smtpEmail, String smtpPassword, OtpChallenge challenge, String loginIdentifier) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpEmail, smtpPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtpEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(challenge.recipient()));
            message.setSubject("Fitopia - Verification anti-robot");
            message.setText(
                    "Bonjour,\n\n"
                            + "Une verification anti-robot a ete demandee pour l'identifiant: " + loginIdentifier + ".\n\n"
                            + "Code de verification Fitopia: " + challenge.code() + "\n"
                            + "Expiration: " + challenge.expiresAt() + "\n\n"
                            + "Entrez ce code dans l'ecran de connexion Fitopia pour confirmer que vous n'etes pas un robot.\n\n"
                            + "Si ce n'est pas vous, ignorez cet email."
            );
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Envoi de l'OTP impossible: " + e.getMessage(), e);
        }
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int value = random.nextInt(bound);
        return String.format("%0" + OTP_LENGTH + "d", value);
    }

    private String env(String key) {
        String value = System.getenv(key);
        return value == null ? "" : value.trim();
    }

    private Properties loadConfig() {
        Properties properties = new Properties();
        if (!Files.exists(MAIL_CONFIG_PATH)) {
            return properties;
        }
        try (InputStream inputStream = Files.newInputStream(MAIL_CONFIG_PATH)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de lire mail.properties: " + e.getMessage(), e);
        }
        return properties;
    }

    private String read(Properties properties, String envKey, String propertyKey) {
        String envValue = env(envKey);
        if (!envValue.isBlank()) {
            return envValue;
        }
        String propertyValue = properties.getProperty(propertyKey);
        return propertyValue == null ? "" : propertyValue.trim();
    }
}
