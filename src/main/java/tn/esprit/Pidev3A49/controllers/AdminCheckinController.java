package tn.esprit.Pidev3A49.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import tn.esprit.Pidev3A49.models.Event;
import tn.esprit.Pidev3A49.models.Reservation;
import tn.esprit.Pidev3A49.models.ReservationHistoryItem;
import tn.esprit.Pidev3A49.service.ReservationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AdminCheckinController {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ENGLISH);

    @FXML private TextField qrTokenField;
    @FXML private Label messageLabel;
    @FXML private VBox reservationCard;
    @FXML private Label participantLabel;
    @FXML private Label emailLabel;
    @FXML private Label eventLabel;
    @FXML private Label eventDateLabel;
    @FXML private Label locationLabel;
    @FXML private Label statusLabel;
    @FXML private Label checkedInAtLabel;
    @FXML private Label usedAtLabel;
    @FXML private Button confirmButton;

    private final ReservationService reservationService = new ReservationService();
    private Reservation currentReservation;

    @FXML
    private void initialize() {
        clearResult();
        qrTokenField.setOnAction(event -> validateQrToken());
    }

    @FXML
    private void validateQrToken() {
        try {
            ReservationHistoryItem item = reservationService.validateQrToken(qrTokenField.getText());
            currentReservation = item.getReservation();
            renderReservation(item);
            setMessage("QR valide. Vous pouvez confirmer l'entree.", false);
            confirmButton.setDisable(false);
        } catch (IllegalArgumentException e) {
            currentReservation = null;
            clearResult();
            setMessage(e.getMessage(), true);
        } catch (Exception e) {
            currentReservation = null;
            clearResult();
            setMessage("Impossible de verifier ce QR pour le moment.", true);
            e.printStackTrace();
        }
    }

    @FXML
    private void confirmEntry() {
        if (currentReservation == null) {
            setMessage("Scannez un QR valide avant de confirmer l'entree.", true);
            return;
        }

        try {
            reservationService.confirmCheckin(currentReservation);
            statusLabel.setText(currentReservation.getStatut());
            checkedInAtLabel.setText(formatDateTime(currentReservation.getCheckedInAt()));
            usedAtLabel.setText(formatDateTime(currentReservation.getUsedAt()));
            confirmButton.setDisable(true);
            setMessage("Check-in valide avec succes.", false);
        } catch (IllegalArgumentException e) {
            confirmButton.setDisable(true);
            setMessage(e.getMessage(), true);
        } catch (Exception e) {
            setMessage("Impossible de valider le check-in.", true);
            e.printStackTrace();
        }
    }

    @FXML
    private void clearScan() {
        qrTokenField.clear();
        currentReservation = null;
        clearResult();
        setMessage("Pret a scanner un QR.", false);
        qrTokenField.requestFocus();
    }

    private void renderReservation(ReservationHistoryItem item) {
        Reservation reservation = item.getReservation();
        Event event = item.getEvent();

        reservationCard.setVisible(true);
        reservationCard.setManaged(true);
        participantLabel.setText(nullSafe(reservation.getNomParticipant()));
        emailLabel.setText(nullSafe(reservation.getEmailParticipant()));
        eventLabel.setText(event == null ? "-" : nullSafe(event.getTitre()));
        eventDateLabel.setText(event == null ? "-" : formatDate(event.getDateEvent()));
        locationLabel.setText(event == null ? "-" : nullSafe(event.getLieu()));
        statusLabel.setText(nullSafe(reservation.getStatut()));
        checkedInAtLabel.setText(formatDateTime(reservation.getCheckedInAt()));
        usedAtLabel.setText(formatDateTime(reservation.getUsedAt()));
    }

    private void clearResult() {
        reservationCard.setVisible(false);
        reservationCard.setManaged(false);
        confirmButton.setDisable(true);
        participantLabel.setText("-");
        emailLabel.setText("-");
        eventLabel.setText("-");
        eventDateLabel.setText("-");
        locationLabel.setText("-");
        statusLabel.setText("-");
        checkedInAtLabel.setText("-");
        usedAtLabel.setText("-");
    }

    private void setMessage(String message, boolean error) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("checkin-message-success", "checkin-message-error");
        messageLabel.getStyleClass().add(error ? "checkin-message-error" : "checkin-message-success");
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : DATE_FORMATTER.format(date);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "-" : DATE_TIME_FORMATTER.format(dateTime);
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }
}
