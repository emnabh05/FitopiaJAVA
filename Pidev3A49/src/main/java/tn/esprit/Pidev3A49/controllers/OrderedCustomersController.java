package tn.esprit.Pidev3A49.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import tn.esprit.Pidev3A49.Models.OrderedCustomer;
import tn.esprit.Pidev3A49.services.ServiceSupplementOrder;
import tn.esprit.Pidev3A49.utils.SceneNavigator;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderedCustomersController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private TableView<OrderedCustomer> orderedCustomersTable;

    @FXML
    private TableColumn<OrderedCustomer, String> fullNameColumn;

    @FXML
    private TableColumn<OrderedCustomer, String> emailColumn;

    @FXML
    private TableColumn<OrderedCustomer, String> phoneColumn;

    @FXML
    private TableColumn<OrderedCustomer, String> cityColumn;

    @FXML
    private TableColumn<OrderedCustomer, Number> orderCountColumn;

    @FXML
    private TableColumn<OrderedCustomer, String> totalSpentColumn;

    @FXML
    private TableColumn<OrderedCustomer, String> lastOrderColumn;

    @FXML
    private Label statusLabel;

    private ServiceSupplementOrder serviceSupplementOrder;

    @FXML
    private void initialize() {
        orderedCustomersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        configureColumns();

        try {
            serviceSupplementOrder = new ServiceSupplementOrder();
            loadOrderedCustomers();
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
        orderCountColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getOrderCount()));
        totalSpentColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatMoney(cell.getValue().getTotalSpent())));
        lastOrderColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getLastOrderAt() == null ? "-" : cell.getValue().getLastOrderAt().format(DATE_FORMATTER)
        ));
    }

    private void loadOrderedCustomers() {
        List<OrderedCustomer> customers = serviceSupplementOrder.getOrderedCustomers();
        orderedCustomersTable.setItems(FXCollections.observableArrayList(customers));

        if (customers.isEmpty()) {
            setInfoMessage("Aucun client n'a passe de commande pour le moment.");
        } else {
            setInfoMessage("Clients ayant commande: " + customers.size());
        }
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

    private void setInfoMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #255D71; -fx-font-size: 14px; -fx-font-weight: 700;");
    }

    private void setErrorMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #D92D20; -fx-font-size: 14px; -fx-font-weight: 800;");
    }
}
