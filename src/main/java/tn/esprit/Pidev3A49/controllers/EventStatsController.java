package tn.esprit.Pidev3A49.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import tn.esprit.Pidev3A49.models.EventStats;
import tn.esprit.Pidev3A49.service.EventStatsService;

import java.util.List;
import java.util.Locale;

public class EventStatsController {

    @FXML private Label statusLabel;
    @FXML private TableView<EventStats> statsTable;
    @FXML private TableColumn<EventStats, String> titreColumn;
    @FXML private TableColumn<EventStats, Integer> nbReservationsColumn;
    @FXML private TableColumn<EventStats, Double> revenuTotalColumn;
    @FXML private TableColumn<EventStats, Double> tauxRemplissageColumn;

    private final EventStatsService eventStatsService = new EventStatsService();

    @FXML
    private void initialize() {
        configureTable();
        refreshStats();
    }

    @FXML
    private void refreshStats() {
        try {
            List<EventStats> stats = eventStatsService.getTopProfitableEvents();
            statsTable.setItems(FXCollections.observableArrayList(stats));

            revenuTotalColumn.setSortType(TableColumn.SortType.DESCENDING);
            statsTable.getSortOrder().setAll(revenuTotalColumn);
            statsTable.sort();

            statusLabel.setText(stats.size() + (stats.size() > 1 ? " evenements analyses." : " evenement analyse."));
        } catch (Exception e) {
            showError("Chargement impossible", "Erreur lors du chargement de l'analyse des evenements.", e);
        }
    }

    private void configureTable() {
        titreColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getTitre()));
        nbReservationsColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getNbReservations()));
        revenuTotalColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getRevenuTotal()));
        tauxRemplissageColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getTauxRemplissage()));

        revenuTotalColumn.setCellFactory(column -> new NumberTableCell("DT %.2f"));
        tauxRemplissageColumn.setCellFactory(column -> new NumberTableCell("%.2f %%"));
    }

    private void showError(String title, String message, Exception exception) {
        System.err.println(title + " : " + message);
        if (exception != null) {
            exception.printStackTrace();
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static final class NumberTableCell extends TableCell<EventStats, Double> {
        private final String formatPattern;

        private NumberTableCell(String formatPattern) {
            this.formatPattern = formatPattern;
        }

        @Override
        protected void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                return;
            }
            setText(String.format(Locale.US, formatPattern, item));
        }
    }
}
