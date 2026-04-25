package tn.esprit.Pidev3A49.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;
import tn.esprit.Pidev3A49.models.Event;
import tn.esprit.Pidev3A49.models.Reservation;
import tn.esprit.Pidev3A49.models.WaitlistEntry;
import tn.esprit.Pidev3A49.service.EventService;
import tn.esprit.Pidev3A49.service.WaitlistService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class WaitlistAdminController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ENGLISH);

    @FXML private ComboBox<Event> eventComboBox;
    @FXML private Label statusLabel;
    @FXML private TableView<WaitlistEntry> waitlistTable;
    @FXML private TableColumn<WaitlistEntry, Integer> positionColumn;
    @FXML private TableColumn<WaitlistEntry, String> nameColumn;
    @FXML private TableColumn<WaitlistEntry, String> emailColumn;
    @FXML private TableColumn<WaitlistEntry, String> statusColumn;
    @FXML private TableColumn<WaitlistEntry, String> createdAtColumn;

    private final EventService eventService = new EventService();
    private final WaitlistService waitlistService = new WaitlistService();

    @FXML
    private void initialize() {
        configureTable();
        configureEvents();
        loadEvents();
    }

    @FXML
    private void refreshWaitlist() {
        Event event = eventComboBox.getValue();
        if (event == null) {
            waitlistTable.getItems().clear();
            statusLabel.setText("Selectionne un evenement pour consulter sa liste d'attente.");
            return;
        }

        List<WaitlistEntry> entries = waitlistService.getWaitlistForEvent(event.getIdEvent());
        waitlistTable.setItems(FXCollections.observableArrayList(entries));
        statusLabel.setText(entries.size() + (entries.size() == 1 ? " inscription" : " inscriptions") + " pour " + event.getTitre());
    }

    @FXML
    private void promoteFirst() {
        Event event = eventComboBox.getValue();
        if (event == null) {
            showInfo("Waitlist", "Selectionne un evenement.");
            return;
        }

        try {
            Reservation reservation = waitlistService.promoteNextFromWaitlist(event);
            showInfo("Promotion reussie", "Reservation creee pour " + reservation.getNomParticipant() + ".");
            refreshWaitlist();
        } catch (IllegalArgumentException e) {
            showInfo("Promotion impossible", e.getMessage());
            refreshWaitlist();
        } catch (Exception e) {
            showInfo("Promotion impossible", "Erreur technique: " + (e.getMessage() == null ? "inconnue" : e.getMessage()));
            e.printStackTrace();
        }
    }

    @FXML
    private void removeSelected() {
        WaitlistEntry selected = waitlistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Waitlist", "Selectionne une entree a retirer.");
            return;
        }

        try {
            waitlistService.removeFromWaitlist(selected);
            refreshWaitlist();
        } catch (Exception e) {
            showInfo("Waitlist", "Suppression impossible: " + (e.getMessage() == null ? "erreur inconnue" : e.getMessage()));
            e.printStackTrace();
        }
    }

    private void configureTable() {
        positionColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getPosition()));
        nameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getNomParticipant()));
        emailColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getEmailParticipant()));
        statusColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatus()));
        createdAtColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(formatDateTime(cellData.getValue().getCreatedAt())));
    }

    private void configureEvents() {
        eventComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Event event) {
                return event == null ? "" : event.getTitre();
            }

            @Override
            public Event fromString(String value) {
                return null;
            }
        });
        eventComboBox.valueProperty().addListener((observable, oldValue, newValue) -> refreshWaitlist());
    }

    private void loadEvents() {
        List<Event> events = eventService.getAll();
        eventComboBox.setItems(FXCollections.observableArrayList(events));
        if (!events.isEmpty()) {
            eventComboBox.getSelectionModel().selectFirst();
        }
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : DATE_TIME_FORMATTER.format(value);
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fitopia");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
