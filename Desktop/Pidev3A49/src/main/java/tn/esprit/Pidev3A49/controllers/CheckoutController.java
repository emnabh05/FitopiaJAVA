package tn.esprit.Pidev3A49.controllers;

import java.awt.Desktop;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.Pidev3A49.Models.CartItem;
import tn.esprit.Pidev3A49.Models.Supplement;
import tn.esprit.Pidev3A49.Models.SupplementOrder;
import tn.esprit.Pidev3A49.Models.SupplementOrderItem;
import tn.esprit.Pidev3A49.services.ServiceSupplementOrder;
import tn.esprit.Pidev3A49.utils.AppSession;
import tn.esprit.Pidev3A49.utils.CartStore;
import tn.esprit.Pidev3A49.utils.ConfirmedOrderStore;
import tn.esprit.Pidev3A49.utils.PendingOrderStore;
import tn.esprit.Pidev3A49.utils.SceneNavigator;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public class CheckoutController {

    private static final String PAYMENT_OPTION_BASE_STYLE =
            "-fx-alignment: CENTER_LEFT; "
                    + "-fx-spacing: 16; "
                    + "-fx-background-radius: 18; "
                    + "-fx-border-radius: 18; "
                    + "-fx-padding: 18 20 18 20;";

    private static final String PAYMENT_OPTION_DEFAULT_STYLE =
            PAYMENT_OPTION_BASE_STYLE
                    + "-fx-background-color: white; "
                    + "-fx-border-color: #D5E2EA; "
                    + "-fx-border-width: 1;";

    private static final String PAYMENT_OPTION_SELECTED_STYLE =
            PAYMENT_OPTION_BASE_STYLE
                    + "-fx-background-color: #E8F7F1; "
                    + "-fx-border-color: #118465; "
                    + "-fx-border-width: 1.4;";

    private static final BigDecimal FIT10_DISCOUNT = new BigDecimal("10.00");
    private static final BigDecimal WELCOME5_DISCOUNT = new BigDecimal("5.00");
    private static final String PAYPAL_LOGIN_URL = "https://www.paypal.com/signin";

    @FXML
    private HBox visaOption;

    @FXML
    private HBox mastercardOption;

    @FXML
    private HBox paypalOption;

    @FXML
    private HBox cashOnDeliveryOption;

    @FXML
    private RadioButton visaRadio;

    @FXML
    private RadioButton mastercardRadio;

    @FXML
    private RadioButton paypalRadio;

    @FXML
    private RadioButton cashOnDeliveryRadio;

    @FXML
    private ToggleGroup paymentMethodGroup;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField cityField;

    @FXML
    private TextField postalCodeField;

    @FXML
    private TextArea notesArea;

    @FXML
    private TextField discountCodeField;

    @FXML
    private VBox orderItemsContainer;

    @FXML
    private Label orderSummaryHintLabel;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label shippingLabel;

    @FXML
    private Label discountLabel;

    @FXML
    private Label totalLabel;

    @FXML
    private Label checkoutStatusLabel;

    @FXML
    private Button placeOrderButton;

    private final CartStore cartStore = CartStore.getInstance();
    private final PendingOrderStore pendingOrderStore = PendingOrderStore.getInstance();
    private final ConfirmedOrderStore confirmedOrderStore = ConfirmedOrderStore.getInstance();
    private ServiceSupplementOrder serviceSupplementOrder;
    private BigDecimal appliedDiscountAmount = BigDecimal.ZERO;

    @FXML
    private void initialize() {
        updatePaymentStyles();
        initializeService();
        preloadCustomerEmail();
        refreshSummary();
    }

    public void openSupplementShowcase(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.CHECKOUT_VIEW, SceneNavigator.FRONT_END_VIEW);
    }

    public void openBackEnd(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.CHECKOUT_VIEW, SceneNavigator.BACK_END_VIEW);
    }

    public void openMyOrders(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.CHECKOUT_VIEW, SceneNavigator.FRONT_ORDERS_VIEW);
    }

    public void goBackOrExit(ActionEvent event) throws IOException {
        SceneNavigator.goBackOrClose(event);
    }

    @FXML
    private void updatePaymentStyles() {
        applyPaymentStyle(visaOption, visaRadio);
        applyPaymentStyle(mastercardOption, mastercardRadio);
        applyPaymentStyle(paypalOption, paypalRadio);
        applyPaymentStyle(cashOnDeliveryOption, cashOnDeliveryRadio);
    }

    @FXML
    private void handlePaypalSelection() {
        updatePaymentStyles();
        openPaypalLoginPage();
    }

    @FXML
    private void applyDiscountCode() {
        if (cartStore.isEmpty()) {
            setErrorMessage("Ajoute d'abord un produit au panier avant d'appliquer un code.");
            return;
        }

        String code = discountCodeField.getText() == null ? "" : discountCodeField.getText().trim().toUpperCase(Locale.ROOT);
        if (code.isEmpty()) {
            appliedDiscountAmount = BigDecimal.ZERO;
            refreshSummary();
            setInfoMessage("Aucun code promo applique.");
            return;
        }

        switch (code) {
            case "FIT10" -> appliedDiscountAmount = FIT10_DISCOUNT;
            case "WELCOME5" -> appliedDiscountAmount = WELCOME5_DISCOUNT;
            default -> {
                appliedDiscountAmount = BigDecimal.ZERO;
                refreshSummary();
                setErrorMessage("Code promo invalide. Codes disponibles: FIT10 ou WELCOME5.");
                return;
            }
        }

        refreshSummary();
        setSuccessMessage("Code promo applique.");
    }

    @FXML
    private void placeOrder(ActionEvent event) {
        ensureServiceAvailable();

        List<CartItem> cartItems = cartStore.getItems();
        if (cartItems.isEmpty()) {
            setErrorMessage("Le panier est vide.");
            refreshSummary();
            return;
        }

        try {
            SupplementOrder order = buildOrder(cartItems);

            if ("Visa".equalsIgnoreCase(order.getPaymentMethod()) || "Mastercard".equalsIgnoreCase(order.getPaymentMethod())) {
                pendingOrderStore.setPendingOrder(order);
                try {
                    SceneNavigator.navigate(event, SceneNavigator.CHECKOUT_VIEW, SceneNavigator.CARD_PAYMENT_VIEW);
                } catch (IOException exception) {
                    throw new IllegalStateException("Impossible d'ouvrir la page de paiement carte.");
                }
                return;
            }

            int orderId = serviceSupplementOrder.placeOrder(order);
            order.setId(orderId);
            order.setCreatedAt(LocalDateTime.now());
            cartStore.setLastCheckoutEmail(order.getEmail());
            confirmedOrderStore.setConfirmedOrder(order);

            cartStore.clear();
            appliedDiscountAmount = BigDecimal.ZERO;
            clearForm();
            refreshSummary();
            pendingOrderStore.clear();

            try {
                SceneNavigator.navigate(event, SceneNavigator.CHECKOUT_VIEW, SceneNavigator.ORDER_CONFIRMATION_VIEW);
            } catch (IOException exception) {
                throw new IllegalStateException("Commande enregistree mais impossible d'ouvrir la page de confirmation.");
            }
        } catch (RuntimeException exception) {
            setErrorMessage(exception.getMessage());
        }
    }

    private void initializeService() {
        try {
            serviceSupplementOrder = new ServiceSupplementOrder();
        } catch (RuntimeException exception) {
            serviceSupplementOrder = null;
            setErrorMessage(exception.getMessage());
        }
    }

    private void preloadCustomerEmail() {
        String lastCheckoutEmail = cartStore.getLastCheckoutEmail();
        if (lastCheckoutEmail != null && !lastCheckoutEmail.isBlank()) {
            emailField.setText(lastCheckoutEmail.trim());
            return;
        }

        String sessionEmail = AppSession.getInstance().getEmail();
        if (sessionEmail != null && !sessionEmail.isBlank()) {
            emailField.setText(sessionEmail.trim());
        }
    }

    private void refreshSummary() {
        orderItemsContainer.getChildren().clear();

        List<CartItem> cartItems = cartStore.getItems();
        if (cartItems.isEmpty()) {
            orderSummaryHintLabel.setText("Your cart is empty. Add products from the store to place an order.");
            orderItemsContainer.getChildren().add(buildEmptyState());
            subtotalLabel.setText("0.00 DT");
            shippingLabel.setText("0.00 DT");
            discountLabel.setText("0.00 DT");
            totalLabel.setText("0.00 DT");
            placeOrderButton.setDisable(true);
            return;
        }

        for (CartItem item : cartItems) {
            orderItemsContainer.getChildren().add(buildOrderItemRow(item));
        }

        BigDecimal subtotal = cartStore.getSubtotal();
        BigDecimal shippingCost = cartStore.getShippingCost();
        BigDecimal discountAmount = normalizeDiscount(subtotal);
        BigDecimal totalAmount = subtotal.add(shippingCost).subtract(discountAmount);

        orderSummaryHintLabel.setText(cartItems.size() + " line(s) ready for confirmation.");
        subtotalLabel.setText(formatPrice(subtotal));
        shippingLabel.setText(formatPrice(shippingCost));
        discountLabel.setText(formatPrice(discountAmount));
        totalLabel.setText(formatPrice(totalAmount));
        placeOrderButton.setDisable(serviceSupplementOrder == null);
    }

    private VBox buildEmptyState() {
        VBox emptyState = new VBox(8.0);
        emptyState.setStyle("-fx-background-color: #F6FAFC; -fx-background-radius: 18; -fx-border-color: #D9E6ED; "
                + "-fx-border-radius: 18; -fx-padding: 16 16 16 16;");

        Label titleLabel = new Label("No items in this order");
        titleLabel.setStyle("-fx-text-fill: #113748; -fx-font-size: 18px; -fx-font-weight: 900;");

        Label hintLabel = new Label("Go back to the store, add supplements to the cart, then return to checkout.");
        hintLabel.setWrapText(true);
        hintLabel.setStyle("-fx-text-fill: #768995; -fx-font-size: 12px; -fx-font-weight: 600;");

        emptyState.getChildren().addAll(titleLabel, hintLabel);
        return emptyState;
    }

    private HBox buildOrderItemRow(CartItem item) {
        Supplement supplement = item.getSupplement();

        HBox row = new HBox(12.0);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #F6FAFC; -fx-background-radius: 18; -fx-border-color: #D9E6ED; "
                + "-fx-border-radius: 18; -fx-padding: 16 16 16 16;");

        StackPane visualPane = new StackPane();
        visualPane.setPrefSize(62.0, 62.0);
        visualPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #D9F5EA, #F2FCF7); -fx-background-radius: 18;");
        Label visualLabel = new Label(valueOrDefault(supplement.getBrand()).substring(0, Math.min(3, valueOrDefault(supplement.getBrand()).length())).toUpperCase(Locale.ROOT));
        visualLabel.setStyle("-fx-text-fill: #0F6A58; -fx-font-size: 16px; -fx-font-weight: 900;");
        visualPane.getChildren().add(visualLabel);

        VBox detailsBox = new VBox(4.0);
        Label nameLabel = new Label(valueOrDefault(supplement.getName()));
        nameLabel.setStyle("-fx-text-fill: #113748; -fx-font-size: 17px; -fx-font-weight: 900;");
        Label metaLabel = new Label(valueOrDefault(supplement.getBrand()) + " | Qty " + item.getQuantity());
        metaLabel.setStyle("-fx-text-fill: #768995; -fx-font-size: 12px; -fx-font-weight: 600;");
        detailsBox.getChildren().addAll(nameLabel, metaLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label priceLabel = new Label(formatPrice(item.getLineTotal()));
        priceLabel.setStyle("-fx-text-fill: #123748; -fx-font-size: 15px; -fx-font-weight: 800;");

        row.getChildren().addAll(visualPane, detailsBox, spacer, priceLabel);
        return row;
    }

    private SupplementOrder buildOrder(List<CartItem> cartItems) {
        BigDecimal subtotal = cartStore.getSubtotal();
        BigDecimal shippingCost = cartStore.getShippingCost();
        BigDecimal discountAmount = normalizeDiscount(subtotal);
        BigDecimal totalAmount = subtotal.add(shippingCost).subtract(discountAmount);

        SupplementOrder order = new SupplementOrder();
        order.setFirstName(requireText(firstNameField, "Le prenom est obligatoire."));
        order.setLastName(requireText(lastNameField, "Le nom est obligatoire."));
        order.setEmail(requireEmail(emailField));
        order.setPhone(requireText(phoneField, "Le telephone est obligatoire."));
        order.setAddress(requireText(addressField, "L'adresse est obligatoire."));
        order.setCity(requireText(cityField, "La ville est obligatoire."));
        order.setPostalCode(requireText(postalCodeField, "Le code postal est obligatoire."));
        order.setNotes(optionalText(notesArea));
        order.setPaymentMethod(getSelectedPaymentMethod());
        order.setDiscountCode(optionalDiscountCode());
        order.setSubtotal(subtotal);
        order.setShippingCost(shippingCost);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);
        order.setStatus("ON_PROGRESS");
        order.setItems(cartItems.stream().map(this::toOrderItem).toList());
        return order;
    }

    private SupplementOrderItem toOrderItem(CartItem cartItem) {
        Supplement supplement = cartItem.getSupplement();
        return new SupplementOrderItem(
                supplement.getId(),
                valueOrDefault(supplement.getName()),
                supplement.getPrice(),
                cartItem.getQuantity(),
                cartItem.getLineTotal()
        );
    }

    private String getSelectedPaymentMethod() {
        Toggle selectedToggle = paymentMethodGroup.getSelectedToggle();
        if (selectedToggle == null) {
            throw new IllegalArgumentException("Choisis un mode de paiement.");
        }
        if (selectedToggle == visaRadio) {
            return "Visa";
        }
        if (selectedToggle == mastercardRadio) {
            return "Mastercard";
        }
        if (selectedToggle == paypalRadio) {
            return "PayPal";
        }
        return "Cash on Delivery";
    }

    private BigDecimal normalizeDiscount(BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (appliedDiscountAmount == null || appliedDiscountAmount.compareTo(BigDecimal.ZERO) < 0) {
            appliedDiscountAmount = BigDecimal.ZERO;
        }
        if (appliedDiscountAmount.compareTo(subtotal) > 0) {
            appliedDiscountAmount = subtotal;
        }
        return appliedDiscountAmount;
    }

    private String requireText(TextField field, String message) {
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private String requireEmail(TextField field) {
        String value = requireText(field, "L'email est obligatoire.");
        if (!value.contains("@") || !value.contains(".")) {
            throw new IllegalArgumentException("Entre une adresse email valide.");
        }
        return value;
    }

    private String optionalText(TextArea area) {
        String value = area.getText() == null ? "" : area.getText().trim();
        return value.isEmpty() ? null : value;
    }

    private String optionalDiscountCode() {
        String value = discountCodeField.getText() == null ? "" : discountCodeField.getText().trim().toUpperCase(Locale.ROOT);
        return value.isEmpty() ? null : value;
    }

    private void applyPaymentStyle(HBox option, RadioButton radioButton) {
        option.setStyle(radioButton.isSelected() ? PAYMENT_OPTION_SELECTED_STYLE : PAYMENT_OPTION_DEFAULT_STYLE);
    }

    private void openPaypalLoginPage() {
        if (!paypalRadio.isSelected()) {
            return;
        }

        try {
            URI paypalUri = URI.create(PAYPAL_LOGIN_URL);
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(paypalUri);
                setInfoMessage("PayPal login opened in your browser.");
            } else {
                setInfoMessage("Open this link to login to PayPal: " + PAYPAL_LOGIN_URL);
            }
        } catch (Exception exception) {
            setErrorMessage("Impossible d'ouvrir PayPal automatiquement. Utilise ce lien: " + PAYPAL_LOGIN_URL);
        }
    }

    private void ensureServiceAvailable() {
        if (serviceSupplementOrder == null) {
            throw new IllegalStateException("MySQL est indisponible. Impossible d'enregistrer la commande.");
        }
    }

    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        cityField.clear();
        postalCodeField.clear();
        notesArea.clear();
        discountCodeField.clear();
        paymentMethodGroup.selectToggle(visaRadio);
        updatePaymentStyles();
    }

    private String valueOrDefault(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String formatPrice(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString() + " DT";
    }

    private void setSuccessMessage(String message) {
        checkoutStatusLabel.setText(message);
        checkoutStatusLabel.setStyle("-fx-text-fill: #13866A; -fx-font-size: 14px; -fx-font-weight: 800;");
    }

    private void setInfoMessage(String message) {
        checkoutStatusLabel.setText(message);
        checkoutStatusLabel.setStyle("-fx-text-fill: #255D71; -fx-font-size: 14px; -fx-font-weight: 700;");
    }

    private void setErrorMessage(String message) {
        checkoutStatusLabel.setText(message);
        checkoutStatusLabel.setStyle("-fx-text-fill: #D92D20; -fx-font-size: 14px; -fx-font-weight: 800;");
    }
}
