package tn.esprit.Pidev3A49.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import tn.esprit.Pidev3A49.Models.SupplementOrder;
import tn.esprit.Pidev3A49.services.ServiceSupplementOrder;
import tn.esprit.Pidev3A49.utils.CartStore;
import tn.esprit.Pidev3A49.utils.SceneNavigator;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FrontOrdersController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private TextField emailSearchField;

    @FXML
    private TableView<SupplementOrder> ordersTable;

    @FXML
    private TableColumn<SupplementOrder, Number> orderIdColumn;

    @FXML
    private TableColumn<SupplementOrder, String> createdAtColumn;

    @FXML
    private TableColumn<SupplementOrder, String> totalColumn;

    @FXML
    private TableColumn<SupplementOrder, String> statusColumn;

    @FXML
    private Label statusLabel;

    private ServiceSupplementOrder serviceSupplementOrder;

    @FXML
    private void initialize() {
        ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        configureColumns();

        try {
            serviceSupplementOrder = new ServiceSupplementOrder();
            String lastEmail = CartStore.getInstance().getLastCheckoutEmail();
            if (lastEmail != null && !lastEmail.isBlank()) {
                emailSearchField.setText(lastEmail);
                searchOrders();
            } else {
                setInfoMessage("Enter your email then click Check Orders.");
            }
        } catch (RuntimeException exception) {
            setErrorMessage("MySQL unavailable: " + exception.getMessage());
        }
    }

    @FXML
    public void searchOrders() {
        if (serviceSupplementOrder == null) {
            setErrorMessage("Service unavailable.");
            return;
        }

        String email = emailSearchField.getText() == null ? "" : emailSearchField.getText().trim();
        if (email.isEmpty()) {
            ordersTable.setItems(FXCollections.observableArrayList());
            setInfoMessage("Please enter your email.");
            return;
        }

        List<SupplementOrder> orders = serviceSupplementOrder.getOrdersByEmail(email);
        ordersTable.setItems(FXCollections.observableArrayList(orders));
        if (orders.isEmpty()) {
            setInfoMessage("No orders found for " + email + ".");
        } else {
            setSuccessMessage("Found " + orders.size() + " order(s) for " + email + ".");
        }
    }

    @FXML
    public void openStore(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.FRONT_ORDERS_VIEW, SceneNavigator.FRONT_END_VIEW);
    }

    @FXML
    public void openCheckout(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.FRONT_ORDERS_VIEW, SceneNavigator.CHECKOUT_VIEW);
    }

    @FXML
    public void goBackOrExit(ActionEvent event) throws IOException {
        SceneNavigator.goBackOrClose(event);
    }

    private void configureColumns() {
        orderIdColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()));
        createdAtColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getCreatedAt() == null ? "-" : cell.getValue().getCreatedAt().format(DATE_FORMATTER)
        ));
        totalColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatPrice(cell.getValue().getTotalAmount())));
        statusColumn.setCellValueFactory(cell -> new SimpleStringProperty(normalizeStatus(cell.getValue().getStatus())));
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ON_PROGRESS";
        }
        String normalized = status.trim().toUpperCase();
        return "PLACED".equals(normalized) ? "ON_PROGRESS" : normalized;
    }

    private String formatPrice(BigDecimal amount) {
        if (amount == null) {
            return "0.00 DT";
        }
        return amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() + " DT";
    }

    private void setInfoMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #255D71; -fx-font-size: 14px; -fx-font-weight: 700;");
    }

    private void setSuccessMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #13866A; -fx-font-size: 14px; -fx-font-weight: 800;");
    }

    private void setErrorMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #D92D20; -fx-font-size: 14px; -fx-font-weight: 800;");
    }
}
