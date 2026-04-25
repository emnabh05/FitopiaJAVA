package tn.esprit.Pidev3A49.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.esprit.Pidev3A49.models.Event;
import tn.esprit.Pidev3A49.models.Participation;
import tn.esprit.Pidev3A49.models.Reservation;
import tn.esprit.Pidev3A49.service.EventService;
import tn.esprit.Pidev3A49.service.ParticipationService;
import tn.esprit.Pidev3A49.service.ReservationService;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

public class ReservationController {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);

    @FXML private StackPane eventVisualPane;
    @FXML private Label eventTypeLabel;
    @FXML private Label eventTitleLabel;
    @FXML private Label eventDisplayTitleLabel;
    @FXML private Label eventDateLabel;
    @FXML private Label eventLocationLabel;
    @FXML private Label eventPriceLabel;
    @FXML private Label eventSeatsLabel;
    @FXML private Label eventPremiumLabel;
    @FXML private TextArea eventDescriptionArea;

    @FXML private Label formSubtitleLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField firstNameField;
    @FXML private TextField emailField;

    @FXML private Label bookingAmountLabel;
    @FXML private Label bookingStatusLabel;
    @FXML private Button confirmButton;
    @FXML private Label feedbackLabel;

    private final ReservationService reservationService = new ReservationService();
    private final ParticipationService participationService = new ParticipationService();
    private final EventService eventService = new EventService();

    private Event event;
    private int remainingPlaces;
    private Consumer<String> onReservationSaved;

    public void setEvent(Event event, int remainingPlaces) {
        this.event = event;
        this.remainingPlaces = remainingPlaces;
        renderEvent();
    }

    public void setContext(Event event, int remainingPlaces, String initialEmail, Consumer<String> onReservationSaved) {
        this.event = event;
        this.remainingPlaces = remainingPlaces;
        this.onReservationSaved = onReservationSaved;
        if (emailField != null && initialEmail != null && !initialEmail.isBlank()) {
            emailField.setText(initialEmail);
        }
        renderEvent();
    }

    @FXML
    private void confirmReservation() {
        if (event == null) {
            showError("Reservation impossible", "Aucun evenement n'est lie a cette reservation.");
            return;
        }

        Connection connection = null;
        boolean previousAutoCommit = true;
        try {
            String lastName = requireText(fullNameField.getText(), "Le nom est obligatoire.");
            String firstName = requireText(firstNameField.getText(), "Le prenom est obligatoire.");
            String fullParticipantName = (lastName + " " + firstName).trim();
            String email = requireEmail(emailField.getText());
            eventService.ensureCanReserve(event, email);
            if (remainingPlaces <= 0) {
                throw new IllegalArgumentException("Il n'y a plus de places disponibles pour cet evenement.");
            }
            String initialStatus = "confirmee";

            connection = MyDataBase.getInstance().getConnection();
            previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            Reservation reservation = new Reservation(
                    event.getIdEvent(),
                    fullParticipantName,
                    email,
                    LocalDateTime.now(),
                    event.getPrixEvent(),
                    initialStatus
            );

            reservationService.add(reservation);
            boolean confirmedReservation = "confirmee".equalsIgnoreCase(reservation.getStatut());
            addParticipationIfConfirmed(confirmedReservation, fullParticipantName, email);

            connection.commit();

            feedbackLabel.setText(
                    (confirmedReservation ? "Reservation confirmee" : "Demande ajoutee en attente")
                            + " pour " + reservation.getNomParticipant() + "."
            );

            showConfirmation(reservation.getStatut());
            if (onReservationSaved != null) {
                onReservationSaved.accept(email);
            }
            closeWindow();

        } catch (IllegalArgumentException e) {
            feedbackLabel.setText(e.getMessage());
        } catch (SQLException e) {
            rollbackReservationTransaction(connection);
            showError("Reservation impossible", "Erreur lors de l'enregistrement dans la base fitopiabd.");
            e.printStackTrace();
        } catch (Exception e) {
            rollbackReservationTransaction(connection);
            showError("Reservation impossible", "Erreur lors de l'enregistrement de la reservation.");
            e.printStackTrace();
        } finally {
            restoreAutoCommit(connection, previousAutoCommit);
        }
    }

    private void addParticipationIfConfirmed(boolean confirmedReservation, String fullParticipantName, String email) {
        if (!confirmedReservation) {
            return;
        }

        participationService.add(new Participation(
                event.getIdEvent(),
                fullParticipantName,
                email,
                LocalDateTime.now()
        ));
    }

    private void rollbackReservationTransaction(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            if (!connection.getAutoCommit()) {
                connection.rollback();
            }
        } catch (SQLException rollbackException) {
            rollbackException.printStackTrace();
        }
    }

    private void restoreAutoCommit(Connection connection, boolean previousAutoCommit) {
        if (connection == null) {
            return;
        }

        try {
            connection.setAutoCommit(previousAutoCommit);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void renderEvent() {
        if (event == null) {
            return;
        }

        eventTypeLabel.setText(nullSafe(event.getTypeEvent()));
        eventTitleLabel.setText(nullSafe(event.getTitre()));
        eventDisplayTitleLabel.setText(nullSafe(event.getTitre()));

        eventDateLabel.setText(formatDate(event.getDateEvent()));
        eventLocationLabel.setText(nullSafe(event.getLieu()));
        eventPriceLabel.setText(formatPrice(event.getPrixEvent()));
        eventSeatsLabel.setText(String.valueOf(Math.max(0, remainingPlaces)));
        if (eventPremiumLabel != null) {
            eventPremiumLabel.setText(event.isPremium() ? "Premium - reserve VIP" : "Standard");
            eventPremiumLabel.getStyleClass().removeAll("reservation-premium-chip", "reservation-status-chip");
            eventPremiumLabel.getStyleClass().add(event.isPremium() ? "reservation-premium-chip" : "reservation-status-chip");
        }

        eventDescriptionArea.setText(buildDescription());

        bookingAmountLabel.setText(formatPrice(event.getPrixEvent()));
        bookingStatusLabel.setText("confirmee");
        formSubtitleLabel.setText(event.isPremium()
                ? "Evenement Premium: seuls les clients VIP peuvent confirmer cette reservation."
                : "Complete your details to confirm this booking.");
        confirmButton.setText(event.isPremium() ? "CONFIRMER EN TANT QUE VIP" : "CONFIRMER MA RESERVATION");
        confirmButton.setDisable(remainingPlaces <= 0);

        addEventImageIfPossible();
    }

    private void addEventImageIfPossible() {
        if (event == null || event.getImageEvent() == null || event.getImageEvent().isBlank()) {
            return;
        }

        try {
            String source = event.getImageEvent().trim();
            String url = source.startsWith("http://")
                    || source.startsWith("https://")
                    || source.startsWith("file:/")
                    ? source
                    : new File(source).toURI().toString();

            Image image = new Image(url, true);
            if (image.isError()) {
                return;
            }

            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(700);
            imageView.setFitHeight(430);
            imageView.setPreserveRatio(false);

            if (!eventVisualPane.getChildren().isEmpty() && eventVisualPane.getChildren().get(0) instanceof ImageView) {
                eventVisualPane.getChildren().remove(0);
            }

            eventVisualPane.getChildren().add(0, imageView);
        } catch (Exception ignored) {
        }
    }

    private String buildDescription() {
        String description = event.getDescription();
        if (description == null || description.isBlank()) {
            return "This wellness experience is ready for booking. You can confirm your reservation now.";
        }
        return description.trim();
    }

    private void showConfirmation(String status) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reservation enregistree");
        alert.setHeaderText("Fitopia Booking");
        alert.setContentText("Votre reservation a ete enregistree avec le statut : " + status + ".");
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
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

    private String formatDate(LocalDate date) {
        return date == null ? "Date TBD" : DATE_FORMATTER.format(date);
    }

    private String formatPrice(double price) {
        return price <= 0 ? "Free" : String.format(Locale.US, "%.2f DT", price);
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }
}
