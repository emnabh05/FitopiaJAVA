package tn.esprit.Pidev3A49.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.Pidev3A49.models.Event;
import tn.esprit.Pidev3A49.models.Participation;
import tn.esprit.Pidev3A49.models.PaymentResult;
import tn.esprit.Pidev3A49.models.Reservation;
import tn.esprit.Pidev3A49.service.ParticipationService;
import tn.esprit.Pidev3A49.service.PaymentService;
import tn.esprit.Pidev3A49.service.ReservationService;
import tn.esprit.Pidev3A49.service.TwilioSmsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

public class PaymentController {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
    private static final String[] PROCESSING_STEPS = {
            "Verification des informations...",
            "Connexion au service de paiement...",
            "Validation de la transaction...",
            "Finalisation du paiement..."
    };

    @FXML private Label eventTitleLabel;
    @FXML private Label eventDateLabel;
    @FXML private Label eventLocationLabel;
    @FXML private Label amountLabel;
    @FXML private Label participantLabel;
    @FXML private Label emailLabel;
    @FXML private Label statusLabel;
    @FXML private Label transactionLabel;
    @FXML private Label feedbackLabel;
    @FXML private Label processingStepLabel;
    @FXML private Label successTransactionLabel;
    @FXML private Label successAmountLabel;
    @FXML private Label successStatusLabel;

    @FXML private TextField cardHolderField;
    @FXML private TextField cardNumberField;
    @FXML private TextField expirationField;
    @FXML private PasswordField cvvField;
    @FXML private RadioButton cardRadio;
    @FXML private RadioButton paypalRadio;
    @FXML private RadioButton walletRadio;
    @FXML private ToggleGroup paymentMethodGroup;

    @FXML private VBox paymentFormBox;
    @FXML private VBox processingBox;
    @FXML private VBox successBox;

    @FXML private Button payButton;
    @FXML private Button forceSuccessButton;
    @FXML private Button forceFailureButton;
    @FXML private Button retryButton;
    @FXML private Button cancelButton;
    @FXML private Button viewReservationsButton;

    private final PaymentService paymentService = new PaymentService();
    private final ReservationService reservationService = new ReservationService();
    private final ParticipationService participationService = new ParticipationService();
    private final TwilioSmsService smsService = new TwilioSmsService();

    private Event event;
    private Reservation reservation;
    private Consumer<Reservation> onPaymentValidated;

    @FXML
    private void initialize() {
        cardRadio.setSelected(true);
        processingBox.setVisible(false);
        processingBox.setManaged(false);
        successBox.setVisible(false);
        successBox.setManaged(false);
        retryButton.setVisible(false);
        retryButton.setManaged(false);
        limitNumericFields();
    }

    public void setContext(Event event, Reservation reservation, Consumer<Reservation> onPaymentValidated) {
        this.event = event;
        this.reservation = reservation;
        this.onPaymentValidated = onPaymentValidated;
        render();
    }

    @FXML
    private void payNow() {
        startTransaction(null);
    }

    @FXML
    private void forceSuccessfulPayment() {
        startTransaction(Boolean.TRUE);
    }

    @FXML
    private void forceFailedPayment() {
        startTransaction(Boolean.FALSE);
    }

    @FXML
    private void retryPayment() {
        reservation.setStatut("EN_ATTENTE_PAIEMENT");
        statusLabel.setText(reservation.getStatut());
        transactionLabel.setText("-");
        feedbackLabel.setText("Corrigez les informations puis relancez le paiement.");
        retryButton.setVisible(false);
        retryButton.setManaged(false);
        setProcessing(false);
    }

    @FXML
    private void viewReservations() {
        if (onPaymentValidated != null) {
            onPaymentValidated.accept(reservation);
        }
        closeWindow();
    }

    @FXML
    private void cancelPayment() {
        closeWindow();
    }

    private void startTransaction(Boolean forcedSuccess) {
        if (reservation == null) {
            showError("Paiement impossible", "Aucune reservation n'est liee a ce paiement.");
            return;
        }

        try {
            paymentService.validatePaymentForm(
                    cardHolderField.getText(),
                    cardNumberField.getText(),
                    expirationField.getText(),
                    cvvField.getText(),
                    selectedPaymentMethod()
            );
        } catch (IllegalArgumentException e) {
            feedbackLabel.setText(e.getMessage());
            return;
        }

        setProcessing(true);
        playProcessingSteps(forcedSuccess);
    }

    private void playProcessingSteps(Boolean forcedSuccess) {
        Timeline timeline = new Timeline();
        for (int i = 0; i < PROCESSING_STEPS.length; i++) {
            final int stepIndex = i;
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(650.0 * i), eventAction ->
                    processingStepLabel.setText(PROCESSING_STEPS[stepIndex])));
        }
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(650.0 * PROCESSING_STEPS.length), eventAction ->
                completePayment(forcedSuccess)));
        timeline.play();
    }

    private void completePayment(Boolean forcedSuccess) {
        try {
            PaymentResult result;
            if (Boolean.TRUE.equals(forcedSuccess)) {
                result = paymentService.processSuccessfulTestPayment(reservation);
            } else if (Boolean.FALSE.equals(forcedSuccess)) {
                result = paymentService.processFailedTestPayment(reservation);
            } else {
                result = paymentService.processPayment(reservation, selectedPaymentMethod());
            }

            if (result.isSuccessful()) {
                handlePaymentSuccess(result);
            } else {
                handlePaymentFailure(result);
            }
        } catch (Exception e) {
            feedbackLabel.setText("Erreur pendant le paiement.");
            showError("Paiement impossible", "Erreur lors du traitement du paiement.");
            e.printStackTrace();
            setProcessing(false);
        }
    }

    private void handlePaymentSuccess(PaymentResult result) {
        reservationService.markPaymentSucceeded(reservation, result.getTransactionId());
        addParticipationIfMissing();
        boolean smsSent = sendWelcomeSms();

        statusLabel.setText(reservation.getStatut());
        transactionLabel.setText(result.getTransactionId());
        successTransactionLabel.setText(result.getTransactionId());
        successAmountLabel.setText(formatPrice(reservation.getMontant()));
        successStatusLabel.setText(reservation.getStatut());
        feedbackLabel.setText(smsSent
                ? "Paiement valide et SMS envoye. Le QR est disponible dans l'historique."
                : "Paiement valide, mais le SMS de bienvenue n'a pas pu etre envoye.");

        paymentFormBox.setVisible(false);
        paymentFormBox.setManaged(false);
        processingBox.setVisible(false);
        processingBox.setManaged(false);
        successBox.setVisible(true);
        successBox.setManaged(true);
    }

    private void handlePaymentFailure(PaymentResult result) {
        reservationService.markPaymentFailed(reservation);
        statusLabel.setText(reservation.getStatut());
        transactionLabel.setText("-");
        processingBox.setVisible(false);
        processingBox.setManaged(false);
        feedbackLabel.setText(result.getMessage());
        retryButton.setVisible(true);
        retryButton.setManaged(true);
        setProcessing(false);
        showError("Paiement refuse", "Paiement refuse. Veuillez reessayer.");
    }

    private boolean sendWelcomeSms() {
        try {
            return smsService.sendPaymentSuccessSms(reservation.getTelephoneParticipant(), reservation);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void addParticipationIfMissing() {
        if (event == null || reservation == null) {
            return;
        }
        if (participationService.existsForEventAndEmail(event.getIdEvent(), reservation.getEmailParticipant())) {
            return;
        }
        participationService.add(new Participation(
                event.getIdEvent(),
                reservation.getNomParticipant(),
                reservation.getEmailParticipant(),
                LocalDateTime.now()
        ));
    }

    private void render() {
        if (event == null || reservation == null) {
            return;
        }

        eventTitleLabel.setText(nullSafe(event.getTitre()));
        eventDateLabel.setText(formatDate(event.getDateEvent()));
        eventLocationLabel.setText(nullSafe(event.getLieu()));
        amountLabel.setText(formatPrice(reservation.getMontant()));
        participantLabel.setText(nullSafe(reservation.getNomParticipant()));
        emailLabel.setText(nullSafe(reservation.getEmailParticipant()));
        statusLabel.setText(nullSafe(reservation.getStatut()));
        transactionLabel.setText(nullSafe(reservation.getTransactionId()));
        feedbackLabel.setText("Saisissez des donnees de test. Aucune information bancaire n'est stockee.");
    }

    private void setProcessing(boolean processing) {
        paymentFormBox.setDisable(processing);
        payButton.setDisable(processing);
        forceSuccessButton.setDisable(processing);
        forceFailureButton.setDisable(processing);
        cancelButton.setDisable(processing);
        retryButton.setDisable(processing);
        processingBox.setVisible(processing);
        processingBox.setManaged(processing);
        if (processing) {
            feedbackLabel.setText("Transaction sandbox en cours...");
        }
    }

    private String selectedPaymentMethod() {
        if (paymentMethodGroup.getSelectedToggle() == paypalRadio) {
            return "PayPal simule";
        }
        if (paymentMethodGroup.getSelectedToggle() == walletRadio) {
            return "Wallet simule";
        }
        return "Carte bancaire";
    }

    private void limitNumericFields() {
        cardNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            String digits = newValue == null ? "" : newValue.replaceAll("[^0-9]", "");
            if (digits.length() > 16) {
                digits = digits.substring(0, 16);
            }
            if (!digits.equals(newValue)) {
                cardNumberField.setText(digits);
            }
        });

        cvvField.textProperty().addListener((observable, oldValue, newValue) -> {
            String digits = newValue == null ? "" : newValue.replaceAll("[^0-9]", "");
            if (digits.length() > 3) {
                digits = digits.substring(0, 3);
            }
            if (!digits.equals(newValue)) {
                cvvField.setText(digits);
            }
        });
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : DATE_FORMATTER.format(date);
    }

    private String formatPrice(double price) {
        return String.format(Locale.US, "%.2f DT", price);
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
