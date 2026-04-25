package tn.esprit.Pidev3A49.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.Pidev3A49.models.Event;
import tn.esprit.Pidev3A49.models.Reservation;
import tn.esprit.Pidev3A49.models.ReservationHistoryItem;
import tn.esprit.Pidev3A49.service.ReservationService;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReservationHistoryController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ENGLISH);

    @FXML private Label emailLabel;
    @FXML private Label reservationCountLabel;
    @FXML private FlowPane reservationCardsContainer;
    @FXML private StackPane emptyStateBox;
    @FXML private VBox contentRoot;

    private final ReservationService reservationService = new ReservationService();

    private String currentEmail;
    private final List<ReservationHistoryItem> items = new ArrayList<>();

    public void loadHistory(String email) {
        this.currentEmail = email;
        System.out.println("[ReservationHistoryController] loadHistory email = " + email);
        emailLabel.setText("Reservations de : " + email);
        refresh();
    }

    private void refresh() {
        try {
            items.clear();
            items.addAll(reservationService.getHistoryByEmail(currentEmail));
            System.out.println("[ReservationHistoryController] items charges = " + items.size());
            renderItems();
        } catch (Exception e) {
            showError("Historique", "Impossible de charger les reservations.");
            e.printStackTrace();
        }
    }

    private void renderItems() {
        reservationCardsContainer.getChildren().clear();

        for (ReservationHistoryItem item : items) {
            reservationCardsContainer.getChildren().add(buildReservationCard(item));
        }
        System.out.println("[ReservationHistoryController] cartes rendues = " + reservationCardsContainer.getChildren().size());

        reservationCountLabel.setText(items.size() + (items.size() == 1 ? " reservation" : " reservations"));
        boolean empty = items.isEmpty();
        emptyStateBox.setVisible(empty);
        emptyStateBox.setManaged(empty);
        if (contentRoot != null) {
            contentRoot.requestLayout();
        }
    }

    private VBox buildReservationCard(ReservationHistoryItem item) {
        Reservation reservation = item.getReservation();
        Event event = item.getEvent();

        VBox card = new VBox(14);
        card.getStyleClass().add("history-card");
        card.setPrefWidth(680);
        card.setMinWidth(680);

        StackPane mediaPane = new StackPane();
        mediaPane.getStyleClass().add("history-card-media");
        mediaPane.getChildren().add(createMedia(event));

        VBox content = new VBox(12);
        content.setPadding(new Insets(0, 18, 18, 18));

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(event == null ? "Evenement introuvable" : nullSafe(event.getTitre()));
        titleLabel.getStyleClass().add("history-card-title");
        titleLabel.setWrapText(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusChip = new Label(normalizeStatus(reservation));
        statusChip.getStyleClass().add(resolveStatusStyle(reservation));

        titleRow.getChildren().addAll(titleLabel, spacer, statusChip);

        HBox infoRow = new HBox(10);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        infoRow.getChildren().addAll(
                buildMetaChip(event == null || event.getDateEvent() == null ? "-" : event.getDateEvent().toString()),
                buildMetaChip(event == null ? "-" : nullSafe(event.getLieu())),
                buildMetaChip(String.format(Locale.US, "%.2f DT", reservation.getMontant())),
                buildMetaChip(reservation.getDateReservation() == null ? "-" : DATE_TIME_FORMATTER.format(reservation.getDateReservation()))
        );

        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        Button qrButton = new Button("VOIR QR");
        qrButton.getStyleClass().add("history-secondary-button");
        qrButton.setDisable(reservation.getQrToken() == null || reservation.getQrToken().isBlank());
        qrButton.setOnAction(eventAction -> openQrView(item));

        Button cancelButton = new Button("ANNULER");
        cancelButton.getStyleClass().add("history-danger-button");
        cancelButton.setDisable(!reservationService.isCancelable(reservation));
        cancelButton.setOnAction(eventAction -> cancelReservation(item));

        actionRow.getChildren().addAll(qrButton, cancelButton);

        content.getChildren().addAll(titleRow, infoRow, actionRow);
        card.getChildren().addAll(mediaPane, content);
        return card;
    }

    private Node createMedia(Event event) {
        if (event == null || event.getImageEvent() == null || event.getImageEvent().isBlank()) {
            return createPlaceholder(event);
        }

        try {
            String source = event.getImageEvent().trim();
            String url = source.startsWith("http://") || source.startsWith("https://") || source.startsWith("file:/")
                    ? source
                    : new File(source).toURI().toString();

            Image image = new Image(url, true);
            if (image.isError()) {
                return createPlaceholder(event);
            }

            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(680);
            imageView.setFitHeight(260);
            imageView.setPreserveRatio(false);
            return imageView;
        } catch (Exception e) {
            return createPlaceholder(event);
        }
    }

    private Node createPlaceholder(Event event) {
        StackPane placeholder = new StackPane();
        placeholder.getStyleClass().add("history-card-placeholder");
        placeholder.setPrefSize(680, 260);

        Label label = new Label(event == null ? "Reservation" : nullSafe(event.getTitre()));
        label.getStyleClass().add("history-placeholder-title");
        placeholder.getChildren().add(label);
        return placeholder;
    }

    private Label buildMetaChip(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("history-chip");
        return label;
    }

    private void openQrView(ReservationHistoryItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReservationQrView.fxml"));
            Parent root = loader.load();

            ReservationQrController controller = loader.getController();
            controller.setReservationItem(item);

            Scene scene = new Scene(root, 980, 640);
            scene.getStylesheets().add(getClass().getResource("/styles/reservation-qr.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Fitopia - QR reservation");
            stage.setScene(scene);
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.show();
        } catch (Exception e) {
            showError("QR Reservation", "Impossible d'ouvrir la vue QR.");
            e.printStackTrace();
        }
    }

    private void cancelReservation(ReservationHistoryItem item) {
        try {
            ReservationService.CancelReservationResult result =
                    reservationService.cancelReservation(item.getReservation().getId());

            item.getReservation().setStatut("Annulee");
            String message = result.hasPromotion()
                    ? "Reservation annulee avec succes. Une personne de la liste d'attente a ete ajoutee."
                    : "Reservation annulee avec succes.";
            showInfo("Annulation", message);
            refresh();
        } catch (IllegalArgumentException e) {
            showInfo("Annulation", e.getMessage());
        } catch (Exception e) {
            showError("Annulation", "Impossible d'annuler cette reservation.");
            e.printStackTrace();
        }
    }

    private String normalizeStatus(Reservation reservation) {
        String status = reservation.getStatut();
        if (status == null || status.isBlank()) {
            return "INCONNU";
        }
        return status.toUpperCase(Locale.ROOT);
    }

    private String resolveStatusStyle(Reservation reservation) {
        String status = reservation.getStatut() == null ? "" : reservation.getStatut().trim().toLowerCase(Locale.ROOT);
        return switch (status) {
            case "confirmee" -> "history-status-confirmed";
            case "annulee" -> "history-status-cancelled";
            case "utilisee" -> "history-status-used";
            default -> "history-status-pending";
        };
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fitopia");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
