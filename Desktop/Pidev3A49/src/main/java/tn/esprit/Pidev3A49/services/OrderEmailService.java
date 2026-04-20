package tn.esprit.Pidev3A49.services;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.Authenticator;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import tn.esprit.Pidev3A49.Models.SupplementOrder;
import tn.esprit.Pidev3A49.Models.SupplementOrderItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class OrderEmailService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String DEFAULT_SMTP_HOST = "smtp.gmail.com";
    private static final String DEFAULT_SMTP_PORT = "587";

    public CompletableFuture<Boolean> sendOrderPlacedEmailAsync(SupplementOrder order) {
        return CompletableFuture.supplyAsync(() -> sendOrderPlacedEmail(order));
    }

    public boolean sendOrderPlacedEmail(SupplementOrder order) {
        if (order == null) {
            return false;
        }

        MailSettings settings = MailSettings.fromRuntimeConfig();
        if (!settings.isConfigured()) {
            System.err.println("Order email skipped: SMTP credentials are not configured.");
            return false;
        }

        String recipient = safeTrim(order.getEmail());
        if (recipient.isBlank()) {
            System.err.println("Order email skipped: recipient email is empty.");
            return false;
        }

        String subject = buildSubject();
        String body = buildBody(order);

        try {
            sendEmail(settings, recipient, subject, body);
            System.out.println("Order email sent to " + recipient + ".");
            return true;
        } catch (MessagingException exception) {
            System.err.println("Order email failed for " + recipient + ": " + exception.getMessage());
            return false;
        }
    }

    public boolean sendInvoiceEmail(SupplementOrder order, Path invoiceFile) {
        if (order == null || invoiceFile == null) {
            return false;
        }

        MailSettings settings = MailSettings.fromRuntimeConfig();
        if (!settings.isConfigured()) {
            System.err.println("Invoice email skipped: SMTP credentials are not configured.");
            return false;
        }

        String recipient = safeTrim(order.getEmail());
        if (recipient.isBlank()) {
            System.err.println("Invoice email skipped: recipient email is empty.");
            return false;
        }

        try {
            sendEmailWithAttachment(
                    settings,
                    recipient,
                    buildInvoiceSubject(),
                    buildInvoiceBody(order),
                    invoiceFile
            );
            System.out.println("Invoice email sent to " + recipient + ".");
            return true;
        } catch (MessagingException exception) {
            System.err.println("Invoice email failed for " + recipient + ": " + exception.getMessage());
            return false;
        }
    }

    private void sendEmail(MailSettings settings, String recipient, String subject, String body) throws MessagingException {
        Session session = createSession(settings);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(settings.fromAddress()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);
        message.setText(body);
        Transport.send(message);
    }

    private void sendEmailWithAttachment(
            MailSettings settings,
            String recipient,
            String subject,
            String body,
            Path attachmentPath
    ) throws MessagingException {
        Session session = createSession(settings);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(settings.fromAddress()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);

        Multipart multipart = new MimeMultipart();

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body);
        multipart.addBodyPart(messageBodyPart);

        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        FileDataSource source = new FileDataSource(attachmentPath.toFile());
        attachmentBodyPart.setDataHandler(new DataHandler(source));
        attachmentBodyPart.setFileName("fitopia-invoice.html");
        multipart.addBodyPart(attachmentBodyPart);

        message.setContent(multipart);
        Transport.send(message);
    }

    private Session createSession(MailSettings settings) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.ssl.trust", settings.smtpHost());
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");
        properties.put("mail.smtp.host", settings.smtpHost());
        properties.put("mail.smtp.port", settings.smtpPort());

        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(settings.username(), settings.password());
            }
        });
    }

    private String buildSubject() {
        return "Fitopia - Order Confirmation";
    }

    private String buildBody(SupplementOrder order) {
        StringBuilder builder = new StringBuilder();
        String fullName = (valueOrDash(order.getFirstName()) + " " + valueOrDash(order.getLastName())).trim();
        LocalDateTime createdAt = order.getCreatedAt() == null ? LocalDateTime.now() : order.getCreatedAt();

        builder.append("Hello ").append(fullName).append(",\n\n");
        builder.append("Your order was successfully placed on Fitopia.\n\n");
        builder.append("Order details:\n");
        builder.append("- Date: ").append(createdAt.format(DATE_FORMATTER)).append("\n");
        builder.append("- Email: ").append(valueOrDash(order.getEmail())).append("\n");
        builder.append("- Phone: ").append(valueOrDash(order.getPhone())).append("\n");
        builder.append("- Payment: ").append(valueOrDash(order.getPaymentMethod())).append("\n");
        builder.append("- Status: ").append(humanizeStatus(order.getStatus())).append("\n");
        builder.append("- Address: ")
                .append(valueOrDash(order.getAddress())).append(", ")
                .append(valueOrDash(order.getCity())).append(" ")
                .append(valueOrDash(order.getPostalCode())).append("\n");

        builder.append("\nItems:\n");
        List<SupplementOrderItem> items = order.getItems();
        if (items == null || items.isEmpty()) {
            builder.append("- No items found.\n");
        } else {
            for (SupplementOrderItem item : items) {
                builder.append("- ")
                        .append(valueOrDash(item.getSupplementName()))
                        .append(" | Qty ").append(item.getQuantity())
                        .append(" | Unit ").append(formatPrice(item.getUnitPrice()))
                        .append(" | Line ").append(formatPrice(item.getLineTotal()))
                        .append("\n");
            }
        }

        builder.append("\nTotals:\n");
        builder.append("- Subtotal: ").append(formatPrice(order.getSubtotal())).append("\n");
        builder.append("- Shipping: ").append(formatPrice(order.getShippingCost())).append("\n");
        builder.append("- Discount: ").append(formatPrice(order.getDiscountAmount())).append("\n");
        builder.append("- Total: ").append(formatPrice(order.getTotalAmount())).append("\n");

        String notes = safeTrim(order.getNotes());
        if (!notes.isBlank()) {
            builder.append("\nNotes:\n").append(notes).append("\n");
        }

        builder.append("\nThank you for choosing Fitopia.");
        return builder.toString();
    }

    private String buildInvoiceSubject() {
        return "Fitopia - Invoice";
    }

    private String buildInvoiceBody(SupplementOrder order) {
        String fullName = (valueOrDash(order.getFirstName()) + " " + valueOrDash(order.getLastName())).trim();
        return "Hello " + fullName + ",\n\n"
                + "Please find your invoice attached to this email.\n"
                + "You can print it directly from the attached file.\n\n"
                + "Thank you for choosing Fitopia.";
    }

    private String formatPrice(BigDecimal value) {
        if (value == null) {
            return "0.00 DT";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString() + " DT";
    }

    private String humanizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ON PROGRESS";
        }
        String normalized = status.trim().toUpperCase();
        if ("PLACED".equals(normalized)) {
            return "ON PROGRESS";
        }
        return normalized.replace('_', ' ');
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private record MailSettings(String username, String password, String fromAddress, String smtpHost, String smtpPort) {

        private static final String FALLBACK_USERNAME = "alitouaiti45@gmail.com";
        private static final String FALLBACK_PASSWORD = "cmkqzuicrwsttvea";

        private static final String USERNAME_PROPERTY = "fitopia.mail.username";
        private static final String PASSWORD_PROPERTY = "fitopia.mail.password";
        private static final String FROM_PROPERTY = "fitopia.mail.from";
        private static final String HOST_PROPERTY = "fitopia.mail.host";
        private static final String PORT_PROPERTY = "fitopia.mail.port";

        private static final String USERNAME_ENV = "FITOPIA_MAIL_USERNAME";
        private static final String PASSWORD_ENV = "FITOPIA_MAIL_PASSWORD";
        private static final String FROM_ENV = "FITOPIA_MAIL_FROM";
        private static final String HOST_ENV = "FITOPIA_MAIL_HOST";
        private static final String PORT_ENV = "FITOPIA_MAIL_PORT";

        private static MailSettings fromRuntimeConfig() {
            String username = readConfig(USERNAME_PROPERTY, USERNAME_ENV);
            String rawPassword = readConfig(PASSWORD_PROPERTY, PASSWORD_ENV);
            String password = rawPassword == null ? "" : rawPassword.replace(" ", "").trim();
            String from = readConfig(FROM_PROPERTY, FROM_ENV);
            String host = readConfig(HOST_PROPERTY, HOST_ENV);
            String port = readConfig(PORT_PROPERTY, PORT_ENV);

            String safeHost = host == null || host.isBlank() ? DEFAULT_SMTP_HOST : host.trim();
            String safePort = port == null || port.isBlank() ? DEFAULT_SMTP_PORT : port.trim();
            String safeUsername = username == null || username.isBlank() ? FALLBACK_USERNAME : username.trim();
            String safePassword = password == null || password.isBlank() ? FALLBACK_PASSWORD : password;
            String safeFrom = from == null || from.isBlank() ? safeUsername : from.trim();

            return new MailSettings(safeUsername, safePassword, safeFrom, safeHost, safePort);
        }

        private static String readConfig(String propertyName, String envName) {
            String propertyValue = System.getProperty(propertyName);
            if (propertyValue != null && !propertyValue.isBlank()) {
                return propertyValue;
            }
            return System.getenv(envName);
        }

        private boolean isConfigured() {
            return username != null
                    && !username.isBlank()
                    && password != null
                    && !password.isBlank()
                    && fromAddress != null
                    && !fromAddress.isBlank();
        }
    }
}
