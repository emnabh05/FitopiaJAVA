package tn.esprit.Pidev3A49.service;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import tn.esprit.Pidev3A49.dao.EventDAO;
import tn.esprit.Pidev3A49.models.Event;
import tn.esprit.Pidev3A49.models.Reservation;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

public class TwilioSmsService {

    private static final String ACCOUNT_SID_KEY = "TWILIO_ACCOUNT_SID";
    private static final String AUTH_TOKEN_KEY = "TWILIO_AUTH_TOKEN";
    private static final String PHONE_NUMBER_KEY = "TWILIO_PHONE_NUMBER";
    private static final Path LOCAL_CONFIG_PATH = Path.of("twilio.local.properties");

    private final EventDAO eventDAO;
    private final Properties localConfig;

    public TwilioSmsService() {
        this.eventDAO = new EventDAO();
        this.localConfig = loadLocalConfig();
    }

    public boolean sendPaymentSuccessSms(String phone, Reservation reservation) {
        String normalizedRecipient = normalizePhone(phone);
        validatePhone(normalizedRecipient);
        if (reservation == null || reservation.getIdEvent() <= 0) {
            throw new IllegalArgumentException("La reservation est invalide pour l'envoi SMS.");
        }

        String accountSid = readConfig(ACCOUNT_SID_KEY);
        String authToken = readConfig(AUTH_TOKEN_KEY);
        String twilioPhoneNumber = normalizePhone(readConfig(PHONE_NUMBER_KEY));
        if (isBlank(accountSid) || isBlank(authToken) || isBlank(twilioPhoneNumber)) {
            throw new IllegalStateException("Configuration Twilio manquante. Verifiez TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN et TWILIO_PHONE_NUMBER.");
        }

        String messageBody = buildPaymentSuccessMessage(reservation);
        logTwilioRequest(normalizedRecipient, twilioPhoneNumber, messageBody, accountSid);
        try {
            Twilio.init(accountSid, authToken);
            Message message = Message.creator(
                    new PhoneNumber(normalizedRecipient),
                    new PhoneNumber(twilioPhoneNumber),
                    messageBody
            ).create();
            System.out.println("[TwilioSmsService] SMS Twilio envoye a " + normalizedRecipient
                    + " SID=" + message.getSid()
                    + " Status=" + message.getStatus());
            return true;
        } catch (ApiException e) {
            logTwilioApiError(e);
            return false;
        } catch (Exception e) {
            System.err.println("[TwilioSmsService] Paiement valide mais SMS non envoye.");
            System.err.println("[TwilioSmsService] Erreur exacte : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String buildPaymentSuccessMessage(Reservation reservation) {
        Event event = eventDAO.findById(reservation.getIdEvent());
        String eventTitle = event == null || event.getTitre() == null || event.getTitre().isBlank()
                ? "votre evenement"
                : event.getTitre().trim();

        return "Felicitations ! Votre paiement Fitopia a ete valide. "
                + "Votre reservation pour l'evenement " + eventTitle + " est confirmee. "
                + "Montant paye : " + String.format(Locale.US, "%.2f", reservation.getMontant()) + " DT. "
                + "Statut : " + nullSafe(reservation.getStatut()) + ". "
                + "Transaction : " + nullSafe(reservation.getTransactionId()) + ". "
                + "Merci pour votre confiance.";
    }

    private Properties loadLocalConfig() {
        Properties properties = new Properties();
        if (!Files.exists(LOCAL_CONFIG_PATH)) {
            return properties;
        }

        try (FileInputStream inputStream = new FileInputStream(LOCAL_CONFIG_PATH.toFile())) {
            properties.load(inputStream);
        } catch (IOException e) {
            System.err.println("Impossible de lire twilio.local.properties.");
            e.printStackTrace();
        }
        return properties;
    }

    private String readConfig(String key) {
        String value = System.getenv(key);
        if (!isBlank(value)) {
            return value;
        }
        return localConfig.getProperty(key);
    }

    private void logTwilioRequest(String recipient, String sender, String messageBody, String accountSid) {
        System.out.println("[TwilioSmsService] Envoi SMS Twilio");
        System.out.println("[TwilioSmsService] Destinataire = " + recipient);
        System.out.println("[TwilioSmsService] Expediteur Twilio = " + sender);
        System.out.println("[TwilioSmsService] TWILIO_ACCOUNT_SID present = " + !isBlank(accountSid)
                + " (" + maskSid(accountSid) + ")");
        System.out.println("[TwilioSmsService] TWILIO_AUTH_TOKEN present = " + !isBlank(readConfig(AUTH_TOKEN_KEY)));
        System.out.println("[TwilioSmsService] TWILIO_PHONE_NUMBER present = " + !isBlank(sender));
        System.out.println("[TwilioSmsService] Message = " + messageBody);
    }

    private void logTwilioApiError(ApiException e) {
        System.err.println("[TwilioSmsService] Paiement valide mais SMS non envoye.");
        System.err.println("[TwilioSmsService] Erreur Twilio exacte = " + e.getMessage());
        System.err.println("[TwilioSmsService] Code Twilio = " + e.getCode());
        System.err.println("[TwilioSmsService] Status HTTP = " + e.getStatusCode());
        System.err.println("[TwilioSmsService] More info = " + e.getMoreInfo());
        e.printStackTrace();
    }

    private void validatePhone(String normalizedPhone) {
        if (!normalizedPhone.matches("^\\+216[0-9]{8}$")) {
            throw new IllegalArgumentException("Le telephone Twilio doit etre au format +216XXXXXXXX, 216XXXXXXXX ou XXXXXXXX.");
        }
    }

    private String normalizePhone(String phone) {
        String normalized = phone == null ? "" : phone.trim().replaceAll("[\\s.-]+", "");
        if (normalized.matches("^[0-9]{8}$")) {
            return "+216" + normalized;
        }
        if (normalized.matches("^216[0-9]{8}$")) {
            return "+" + normalized;
        }
        return normalized;
    }

    private String maskSid(String accountSid) {
        if (isBlank(accountSid) || accountSid.length() <= 8) {
            return "****";
        }
        return accountSid.substring(0, 4) + "..." + accountSid.substring(accountSid.length() - 4);
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
