package tn.esprit.Pidev3A49.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.Pidev3A49.models.Event;
import tn.esprit.Pidev3A49.models.WaitlistEntry;
import tn.esprit.Pidev3A49.service.WaitlistService;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class WaitlistSignupController {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);

    @FXML private Label eventTitleLabel;
    @FXML private Label eventMetaLabel;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private Label statusLabel;
    @FXML private Button confirmButton;

    private final WaitlistService waitlistService = new WaitlistService();

    private Event event;
    private Runnable onSuccess;

    public void setContext(Event event, String initialEmail, Runnable onSuccess) {
        this.event = event;
        this.onSuccess = onSuccess;
        eventTitleLabel.setText(event == null ? "Evenement" : nullSafe(event.getTitre()));
        eventMetaLabel.setText(buildMeta(event));
        if (initialEmail != null && !initialEmail.isBlank()) {
            emailField.setText(initialEmail.trim());
            nameField.setText(buildNameFromEmail(initialEmail));
        }
    }

    @FXML
    private void confirmWaitlist() {
        try {
            confirmButton.setDisable(true);
            WaitlistEntry entry = waitlistService.addToWaitlist(
                    event,
                    requireText(nameField.getText(), "Le nom est obligatoire."),
                    requireEmail(emailField.getText())
            );
            statusLabel.setText("Inscription confirmee. Position: " + entry.getPosition() + ".");
            if (onSuccess != null) {
                onSuccess.run();
            }
            closeLater();
        } catch (IllegalArgumentException e) {
            confirmButton.setDisable(false);
            statusLabel.setText(e.getMessage());
        } catch (Exception e) {
            confirmButton.setDisable(false);
            statusLabel.setText("Impossible de rejoindre la liste d'attente: " + (e.getMessage() == null ? "erreur inconnue" : e.getMessage()));
            e.printStackTrace();
        }
    }

    @FXML
    private void closeView() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }

    private void closeLater() {
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(650));
        delay.setOnFinished(event -> closeView());
        delay.play();
    }

    private String buildMeta(Event event) {
        if (event == null) {
            return "-";
        }
        String date = event.getDateEvent() == null ? "-" : DATE_FORMATTER.format(event.getDateEvent());
        return date + " | " + nullSafe(event.getLieu()) + " | " + nullSafe(event.getTypeEvent());
    }

    private String buildNameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "";
        }
        return email.substring(0, email.indexOf('@'));
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String requireEmail(String value) {
        String email = requireText(value, "L'email est obligatoire.");
        if (!email.contains("@")) {
            throw new IllegalArgumentException("L'email saisi est invalide.");
        }
        return email;
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }
}
