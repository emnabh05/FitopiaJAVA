package tn.esprit.Pidev3A49.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import tn.esprit.Pidev3A49.Models.SupplementOrder;
import tn.esprit.Pidev3A49.services.ServiceSupplementOrder;
import tn.esprit.Pidev3A49.utils.SceneNavigator;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderedCustomersController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final List<String> ORDER_STATUS_OPTIONS = List.of(
            "DELIVERED",
            "ON_THE_WAY",
            "CANCELED",
            "ON_PROGRESS"
    );

    @FXML
    private TableView<SupplementOrder> orderedCustomersTable;

    @FXML
    private TableColumn<SupplementOrder, String> fullNameColumn;

    @FXML
    private TableColumn<SupplementOrder, String> emailColumn;

    @FXML
    private TableColumn<SupplementOrder, String> phoneColumn;

    @FXML
    private TableColumn<SupplementOrder, String> cityColumn;

    @FXML
    private TableColumn<SupplementOrder, String> totalAmountColumn;

    @FXML
    private TableColumn<SupplementOrder, String> statusColumn;

    @FXML
    private TableColumn<SupplementOrder, String> createdAtColumn;

    @FXML
    private Label statusLabel;

    private ServiceSupplementOrder serviceSupplementOrder;

    @FXML
    private void initialize() {
        orderedCustomersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        orderedCustomersTable.setEditable(true);
        configureColumns();

        try {
            serviceSupplementOrder = new ServiceSupplementOrder();
            loadOrders();
        } catch (RuntimeException exception) {
            setErrorMessage("MySQL indisponible: " + exception.getMessage());
            orderedCustomersTable.setItems(FXCollections.observableArrayList());
        }
    }

    @FXML
    public void openBackEnd(ActionEvent event) {
        try {
            SceneNavigator.navigate(event, SceneNavigator.ORDERD_VIEW, SceneNavigator.BACK_END_VIEW);
        } catch (IOException exception) {
            setErrorMessage("Impossible d'ouvrir le back end: " + exception.getMessage());
        }
    }

    @FXML
    public void openFrontEnd(ActionEvent event) {
        try {
            SceneNavigator.navigate(event, SceneNavigator.ORDERD_VIEW, SceneNavigator.FRONT_END_VIEW);
        } catch (IOException exception) {
            setErrorMessage("Impossible d'ouvrir le front end: " + exception.getMessage());
        }
    }

    @FXML
    public void goBackOrExit(ActionEvent event) {
        try {
            SceneNavigator.goBackOrClose(event);
        } catch (IOException exception) {
            setErrorMessage("Impossible de revenir a la page precedente.");
        }
    }

    private void configureColumns() {
        fullNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                (safe(cell.getValue().getFirstName()) + " " + safe(cell.getValue().getLastName())).trim()
        ));
        emailColumn.setCellValueFactory(cell -> new SimpleStringProperty(safe(cell.getValue().getEmail())));
        phoneColumn.setCellValueFactory(cell -> new SimpleStringProperty(safe(cell.getValue().getPhone())));
        cityColumn.setCellValueFactory(cell -> new SimpleStringProperty(safe(cell.getValue().getCity())));
        totalAmountColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatMoney(cell.getValue().getTotalAmount())));
        createdAtColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getCreatedAt() == null ? "-" : cell.getValue().getCreatedAt().format(DATE_FORMATTER)
        ));

        statusColumn.setCellValueFactory(cell -> new SimpleStringProperty(normalizeStatusForUi(cell.getValue().getStatus())));
        statusColumn.setCellFactory(ComboBoxTableCell.forTableColumn(
                FXCollections.observableArrayList(ORDER_STATUS_OPTIONS)
        ));
        statusColumn.setOnEditCommit(event -> {
            SupplementOrder order = event.getRowValue();
            String previousStatus = normalizeStatusForUi(order.getStatus());
            String newStatus = normalizeStatusForUi(event.getNewValue());

            if (newStatus.equals(previousStatus)) {
                return;
            }

            try {
                serviceSupplementOrder.updateOrderStatus(order.getId(), newStatus);
                order.setStatus(newStatus);
                orderedCustomersTable.refresh();
                setSuccessMessage("Statut de la commande #" + order.getId() + " mis a jour vers " + newStatus + ".");
            } catch (RuntimeException exception) {
                order.setStatus(previousStatus);
                orderedCustomersTable.refresh();
                setErrorMessage("Impossible de mettre a jour la commande #" + order.getId() + ": " + exception.getMessage());
            }
        });
    }

    private void loadOrders() {
        List<SupplementOrder> orders = serviceSupplementOrder.getAllOrdersForAdmin();
        orderedCustomersTable.setItems(FXCollections.observableArrayList(orders));

        if (orders.isEmpty()) {
            setInfoMessage("Aucune commande pour le moment.");
        } else {
            setInfoMessage("Commandes trouvees: " + orders.size() + ". Tu peux changer le statut dans la colonne STATUS.");
        }
    }

    private String normalizeStatusForUi(String status) {
        if (status == null || status.isBlank()) {
            return "ON_PROGRESS";
        }
        String normalized = status.trim().toUpperCase();
        if ("PLACED".equals(normalized)) {
            return "ON_PROGRESS";
        }
        return normalized;
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "0.00 DT";
        }
        return amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() + " DT";
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void setSuccessMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #13866A; -fx-font-size: 14px; -fx-font-weight: 800;");
    }

    private void setInfoMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #255D71; -fx-font-size: 14px; -fx-font-weight: 700;");
    }

    private void setErrorMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #D92D20; -fx-font-size: 14px; -fx-font-weight: 800;");
    }
}
