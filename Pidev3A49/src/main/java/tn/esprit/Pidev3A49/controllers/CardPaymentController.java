package tn.esprit.Pidev3A49.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import tn.esprit.Pidev3A49.Models.SupplementOrder;
import tn.esprit.Pidev3A49.services.ServiceSupplementOrder;
import tn.esprit.Pidev3A49.utils.CartStore;
import tn.esprit.Pidev3A49.utils.ConfirmedOrderStore;
import tn.esprit.Pidev3A49.utils.PendingOrderStore;
import tn.esprit.Pidev3A49.utils.SceneNavigator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class CardPaymentController {

    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^\\d{16}$");
    private static final Pattern EXPIRY_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/\\d{2}$");
    private static final Pattern CVV_PATTERN = Pattern.compile("^\\d{3}$");

    @FXML
    private Label paymentMethodLabel;

    @FXML
    private TextField cardholderNameField;

    @FXML
    private TextField cardNumberField;

    @FXML
    private TextField expiryField;

    @FXML
    private TextField cvvField;

    @FXML
    private Label paymentStatusLabel;

    private final PendingOrderStore pendingOrderStore = PendingOrderStore.getInstance();
    private final CartStore cartStore = CartStore.getInstance();
    private final ConfirmedOrderStore confirmedOrderStore = ConfirmedOrderStore.getInstance();
    private ServiceSupplementOrder serviceSupplementOrder;

    @FXML
    private void initialize() {
        SupplementOrder pendingOrder = pendingOrderStore.getPendingOrder();
        if (pendingOrder == null) {
            paymentMethodLabel.setText("No pending card payment");
            setErrorMessage("No pending order found. Return to checkout and place order again.");
            return;
        }

        paymentMethodLabel.setText("Payment method: " + pendingOrder.getPaymentMethod());
        try {
            serviceSupplementOrder = new ServiceSupplementOrder();
        } catch (RuntimeException exception) {
            setErrorMessage("MySQL unavailable: " + exception.getMessage());
        }
    }

    @FXML
    public void confirmCardPayment(ActionEvent event) {
        SupplementOrder pendingOrder = pendingOrderStore.getPendingOrder();
        if (pendingOrder == null) {
            setErrorMessage("Pending order not found.");
            return;
        }
        if (serviceSupplementOrder == null) {
            setErrorMessage("Payment service unavailable.");
            return;
        }

        try {
            validateCardFields();
            int orderId = serviceSupplementOrder.placeOrder(pendingOrder);
            pendingOrder.setId(orderId);
            pendingOrder.setCreatedAt(LocalDateTime.now());
            cartStore.setLastCheckoutEmail(pendingOrder.getEmail());
            confirmedOrderStore.setConfirmedOrder(pendingOrder);
            cartStore.clear();
            pendingOrderStore.clear();
            setSuccessMessage("Payment accepted. Order #" + orderId + " confirmed.");
            SceneNavigator.navigate(event, SceneNavigator.CARD_PAYMENT_VIEW, SceneNavigator.ORDER_CONFIRMATION_VIEW);
        } catch (RuntimeException | IOException exception) {
            setErrorMessage(exception.getMessage());
        }
    }

    @FXML
    public void openCheckout(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.CARD_PAYMENT_VIEW, SceneNavigator.CHECKOUT_VIEW);
    }

    @FXML
    public void goBackOrExit(ActionEvent event) throws IOException {
        SceneNavigator.goBackOrClose(event);
    }

    private void validateCardFields() {
        String cardholder = read(cardholderNameField);
        String cardNumber = read(cardNumberField).replace(" ", "");
        String expiry = read(expiryField);
        String cvv = read(cvvField);

        if (cardholder.isEmpty()) {
            throw new IllegalArgumentException("Cardholder name is required.");
        }
        if (!CARD_NUMBER_PATTERN.matcher(cardNumber).matches()) {
            throw new IllegalArgumentException("Card number must be 16 digits.");
        }
        if (!EXPIRY_PATTERN.matcher(expiry).matches()) {
            throw new IllegalArgumentException("Expiry must be in MM/YY format.");
        }
        if (!CVV_PATTERN.matcher(cvv).matches()) {
            throw new IllegalArgumentException("CVV must be 3 digits.");
        }
    }

    private String read(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private void setSuccessMessage(String message) {
        paymentStatusLabel.setText(message);
        paymentStatusLabel.setStyle("-fx-text-fill: #13866A; -fx-font-size: 14px; -fx-font-weight: 800;");
    }

    private void setErrorMessage(String message) {
        paymentStatusLabel.setText(message);
        paymentStatusLabel.setStyle("-fx-text-fill: #D92D20; -fx-font-size: 14px; -fx-font-weight: 800;");
    }
}
