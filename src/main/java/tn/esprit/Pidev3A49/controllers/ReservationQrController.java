package tn.esprit.Pidev3A49.controllers;

import javafx.fxml.FXML;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.Pidev3A49.models.Event;
import tn.esprit.Pidev3A49.models.Reservation;
import tn.esprit.Pidev3A49.models.ReservationHistoryItem;
import tn.esprit.Pidev3A49.service.ReservationQrService;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ReservationQrController {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ENGLISH);

    @FXML private Label eventTitleLabel;
    @FXML private Label eventDateLabel;
    @FXML private Label eventLocationLabel;
    @FXML private Label reservationStatusLabel;
    @FXML private Label participantLabel;
    @FXML private Label emailLabel;
    @FXML private Label generatedAtLabel;
    @FXML private TextField tokenField;
    @FXML private Label copyFeedbackLabel;
    @FXML private ImageView qrImageView;

    private final ReservationQrService reservationQrService = new ReservationQrService();

    public void setReservationItem(ReservationHistoryItem item) {
        if (item == null || item.getReservation() == null) {
            return;
        }

        Reservation reservation = item.getReservation();
        Event event = item.getEvent();

        eventTitleLabel.setText(event == null ? "Evenement introuvable" : nullSafe(event.getTitre()));
        eventDateLabel.setText(event == null || event.getDateEvent() == null ? "-" : DATE_FORMATTER.format(event.getDateEvent()));
        eventLocationLabel.setText(event == null ? "-" : nullSafe(event.getLieu()));
        reservationStatusLabel.setText(normalizeStatus(reservation));
        participantLabel.setText(nullSafe(reservation.getNomParticipant()));
        emailLabel.setText(nullSafe(reservation.getEmailParticipant()));
        generatedAtLabel.setText(reservation.getQrGeneratedAt() == null ? "-" : DATE_TIME_FORMATTER.format(reservation.getQrGeneratedAt()));
        tokenField.setText(nullSafe(reservation.getQrToken()));
        copyFeedbackLabel.setText("");
        qrImageView.setImage(reservationQrService.generateQrImage(reservation, 360));
    }

    @FXML
    private void copyToken() {
        String qrToken = tokenField.getText();
        if (qrToken == null || qrToken.isBlank() || "-".equals(qrToken.trim())) {
            copyFeedbackLabel.setText("Aucun token a copier.");
            return;
        }

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(qrToken.trim());
        clipboard.setContent(content);
        copyFeedbackLabel.setText("Token copie avec succes.");
    }

    @FXML
    private void closeView() {
        Stage stage = (Stage) qrImageView.getScene().getWindow();
        stage.close();
    }

    private String normalizeStatus(Reservation reservation) {
        String status = reservation.getStatut();
        if (status == null || status.isBlank()) {
            return "INCONNU";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }
}
