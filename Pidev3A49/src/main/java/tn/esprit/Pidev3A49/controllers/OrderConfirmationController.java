package tn.esprit.Pidev3A49.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tn.esprit.Pidev3A49.Models.SupplementOrder;
import tn.esprit.Pidev3A49.Models.SupplementOrderItem;
import tn.esprit.Pidev3A49.utils.ConfirmedOrderStore;
import tn.esprit.Pidev3A49.utils.InvoiceExporter;
import tn.esprit.Pidev3A49.utils.SceneNavigator;

import java.awt.Desktop;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class OrderConfirmationController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private Label orderReferenceLabel;

    @FXML
    private Label orderDateLabel;

    @FXML
    private Label paymentMethodLabel;

    @FXML
    private Label orderStatusLabel;

    @FXML
    private Label totalAmountLabel;

    @FXML
    private Label estimatedDeliveryLabel;

    @FXML
    private Label customerNameLabel;

    @FXML
    private Label customerEmailLabel;

    @FXML
    private Label customerPhoneLabel;

    @FXML
    private Label customerAddressLabel;

    @FXML
    private VBox orderedItemsContainer;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label shippingLabel;

    @FXML
    private Label discountLabel;

    @FXML
    private Label finalTotalLabel;

    @FXML
    private Label confirmationStatusLabel;

    @FXML
    private Button requestInvoiceButton;

    private final ConfirmedOrderStore confirmedOrderStore = ConfirmedOrderStore.getInstance();
    private SupplementOrder confirmedOrder;

    @FXML
    private void initialize() {
        confirmedOrder = confirmedOrderStore.getConfirmedOrder();
        if (confirmedOrder == null) {
            loadEmptyState();
            return;
        }
        fillOrderDetails();
        fillCustomerDetails();
        fillItems();
        fillTotals();
        setInfoMessage("Cliquez sur \"Demande facture\" pour afficher la facture complete avec cachet et signature.");
    }

    @FXML
    private void requestInvoice() {
        if (confirmedOrder == null) {
            setErrorMessage("Aucune commande recente a facturer.");
            return;
        }

        try {
            Path invoicePath = InvoiceExporter.exportInvoiceHtml(confirmedOrder);
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(invoicePath.toUri());
                setSuccessMessage("Facture generee et ouverte: " + invoicePath.getFileName());
            } else {
                setInfoMessage("Facture generee: " + invoicePath);
            }
        } catch (IOException exception) {
            setErrorMessage("Impossible de generer la facture: " + exception.getMessage());
        }
    }

    @FXML
    private void continueShopping(ActionEvent event) throws IOException {
        confirmedOrderStore.clear();
        SceneNavigator.navigate(event, SceneNavigator.ORDER_CONFIRMATION_VIEW, SceneNavigator.FRONT_END_VIEW);
    }

    @FXML
    private void openMyOrders(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.ORDER_CONFIRMATION_VIEW, SceneNavigator.FRONT_ORDERS_VIEW);
    }

    @FXML
    private void goBackOrExit(ActionEvent event) throws IOException {
        SceneNavigator.goBackOrClose(event);
    }

    private void loadEmptyState() {
        orderReferenceLabel.setText("-");
        orderDateLabel.setText("-");
        paymentMethodLabel.setText("-");
        orderStatusLabel.setText("-");
        totalAmountLabel.setText("0.00 DT");
        estimatedDeliveryLabel.setText("-");
        customerNameLabel.setText("-");
        customerEmailLabel.setText("-");
        customerPhoneLabel.setText("-");
        customerAddressLabel.setText("-");
        orderedItemsContainer.getChildren().clear();
        orderedItemsContainer.getChildren().add(buildEmptyRow());
        subtotalLabel.setText("0.00 DT");
        shippingLabel.setText("0.00 DT");
        discountLabel.setText("0.00 DT");
        finalTotalLabel.setText("0.00 DT");
        requestInvoiceButton.setDisable(true);
        setErrorMessage("Aucune commande confirmee a afficher.");
    }

    private void fillOrderDetails() {
        int orderId = Math.max(confirmedOrder.getId(), 0);
        LocalDateTime createdAt = confirmedOrder.getCreatedAt() == null ? LocalDateTime.now() : confirmedOrder.getCreatedAt();
        LocalDateTime estimatedDelivery = createdAt.plusDays(3);

        orderReferenceLabel.setText("ORD-" + String.format(Locale.ROOT, "%08d", orderId));
        orderDateLabel.setText(DATE_TIME_FORMATTER.format(createdAt));
        paymentMethodLabel.setText(valueOrDash(confirmedOrder.getPaymentMethod()));
        orderStatusLabel.setText(normalizeStatus(confirmedOrder.getStatus()));
        totalAmountLabel.setText(formatPrice(confirmedOrder.getTotalAmount()));
        estimatedDeliveryLabel.setText(DATE_FORMATTER.format(estimatedDelivery));
    }

    private void fillCustomerDetails() {
        String fullName = (valueOrDash(confirmedOrder.getFirstName()) + " " + valueOrDash(confirmedOrder.getLastName())).trim();
        customerNameLabel.setText(fullName.replace(" -", "").replace("- ", ""));
        customerEmailLabel.setText(valueOrDash(confirmedOrder.getEmail()));
        customerPhoneLabel.setText(valueOrDash(confirmedOrder.getPhone()));
        customerAddressLabel.setText(
                valueOrDash(confirmedOrder.getAddress()) + ", "
                        + valueOrDash(confirmedOrder.getCity()) + " "
                        + valueOrDash(confirmedOrder.getPostalCode())
        );
    }

    private void fillItems() {
        orderedItemsContainer.getChildren().clear();
        if (confirmedOrder.getItems() == null || confirmedOrder.getItems().isEmpty()) {
            orderedItemsContainer.getChildren().add(buildEmptyRow());
            return;
        }

        for (SupplementOrderItem item : confirmedOrder.getItems()) {
            orderedItemsContainer.getChildren().add(buildOrderItemRow(item));
        }
    }

    private HBox buildOrderItemRow(SupplementOrderItem item) {
        HBox row = new HBox(12.0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #F6FAFC; -fx-background-radius: 14; -fx-border-color: #D9E6ED; "
                + "-fx-border-radius: 14; -fx-padding: 12 14 12 14;");

        Label nameLabel = new Label(valueOrDash(item.getSupplementName()));
        nameLabel.setStyle("-fx-text-fill: #123748; -fx-font-size: 16px; -fx-font-weight: 800;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label qtyLabel = new Label("x" + Math.max(item.getQuantity(), 0));
        qtyLabel.setStyle("-fx-text-fill: #4D6877; -fx-font-size: 13px; -fx-font-weight: 700;");

        Label totalLabel = new Label(formatPrice(item.getLineTotal()));
        totalLabel.setStyle("-fx-text-fill: #0D4453; -fx-font-size: 15px; -fx-font-weight: 900;");

        row.getChildren().addAll(nameLabel, spacer, qtyLabel, totalLabel);
        return row;
    }

    private HBox buildEmptyRow() {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER);
        row.setStyle("-fx-background-color: #F6FAFC; -fx-background-radius: 14; -fx-border-color: #D9E6ED; "
                + "-fx-border-radius: 14; -fx-padding: 18 14 18 14;");

        Label emptyLabel = new Label("Aucun article disponible.");
        emptyLabel.setStyle("-fx-text-fill: #67808E; -fx-font-size: 13px; -fx-font-weight: 700;");
        row.getChildren().add(emptyLabel);
        return row;
    }

    private void fillTotals() {
        subtotalLabel.setText(formatPrice(confirmedOrder.getSubtotal()));
        BigDecimal shippingCost = confirmedOrder.getShippingCost();
        shippingLabel.setText(shippingCost == null || shippingCost.compareTo(BigDecimal.ZERO) == 0
                ? "GRATUIT"
                : formatPrice(shippingCost));
        discountLabel.setText(formatPrice(confirmedOrder.getDiscountAmount()));
        finalTotalLabel.setText(formatPrice(confirmedOrder.getTotalAmount()));
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "PENDING";
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "PLACED", "ON_PROGRESS" -> "PENDING";
            case "ON_THE_WAY" -> "ON THE WAY";
            default -> normalized;
        };
    }

    private String formatPrice(BigDecimal amount) {
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount;
        return safeAmount.setScale(2, RoundingMode.HALF_UP).toPlainString() + " DT";
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }

    private void setInfoMessage(String message) {
        confirmationStatusLabel.setText(message);
        confirmationStatusLabel.setStyle("-fx-text-fill: #255D71; -fx-font-size: 14px; -fx-font-weight: 700;");
    }

    private void setSuccessMessage(String message) {
        confirmationStatusLabel.setText(message);
        confirmationStatusLabel.setStyle("-fx-text-fill: #13866A; -fx-font-size: 14px; -fx-font-weight: 800;");
    }

    private void setErrorMessage(String message) {
        confirmationStatusLabel.setText(message);
        confirmationStatusLabel.setStyle("-fx-text-fill: #D92D20; -fx-font-size: 14px; -fx-font-weight: 800;");
    }
}
