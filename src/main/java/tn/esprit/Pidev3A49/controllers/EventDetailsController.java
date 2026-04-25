package tn.esprit.Pidev3A49.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import tn.esprit.Pidev3A49.models.Event;
import tn.esprit.Pidev3A49.models.Review;
import tn.esprit.Pidev3A49.service.ReviewService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class EventDetailsController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ENGLISH);

    @FXML private Label titleLabel;
    @FXML private Label typeLabel;
    @FXML private Label dateLabel;
    @FXML private Label locationLabel;
    @FXML private Label priceLabel;
    @FXML private Label premiumLabel;
    @FXML private Label averageLabel;
    @FXML private Label reviewCountLabel;
    @FXML private Label userEmailLabel;
    @FXML private ComboBox<Integer> noteComboBox;
    @FXML private TextArea commentaireArea;
    @FXML private Label formStatusLabel;
    @FXML private VBox reviewsContainer;

    private final ReviewService reviewService = new ReviewService();

    private Event event;
    private String currentUserEmail;
    private Runnable onReviewSaved;

    @FXML
    private void initialize() {
        noteComboBox.getItems().setAll(1, 2, 3, 4, 5);
        noteComboBox.setValue(5);
    }

    public void setContext(Event event, String currentUserEmail, Runnable onReviewSaved) {
        this.event = event;
        this.currentUserEmail = currentUserEmail;
        this.onReviewSaved = onReviewSaved;
        renderEventDetails();
        loadReviews();
    }

    @FXML
    private void submitReview() {
        if (event == null) {
            showError("Avis", "Aucun evenement n'est charge.");
            return;
        }

        try {
            Review review = new Review(
                    event.getIdEvent(),
                    requireCurrentUserEmail(),
                    requireNote(),
                    requireCommentaire(),
                    null,
                    null
            );

            reviewService.addReview(review);
            commentaireArea.clear();
            noteComboBox.setValue(5);
            formStatusLabel.setText("Avis ajoute avec succes.");
            loadReviews();

            if (onReviewSaved != null) {
                onReviewSaved.run();
            }
        } catch (IllegalArgumentException e) {
            formStatusLabel.setText(e.getMessage());
        } catch (Exception e) {
            showError("Avis", "Impossible d'ajouter l'avis.");
            e.printStackTrace();
        }
    }

    private void renderEventDetails() {
        if (event == null) {
            return;
        }

        titleLabel.setText(nullSafe(event.getTitre()));
        typeLabel.setText(nullSafe(event.getTypeEvent()));
        dateLabel.setText(event.getDateEvent() == null ? "-" : event.getDateEvent().toString());
        locationLabel.setText(nullSafe(event.getLieu()));
        priceLabel.setText(event.getPrixEvent() <= 0 ? "Free" : String.format(Locale.US, "%.2f DT", event.getPrixEvent()));
        premiumLabel.setText(event.isPremium() ? "Premium" : "Standard");
        premiumLabel.getStyleClass().removeAll("details-premium-chip", "details-chip");
        premiumLabel.getStyleClass().add(event.isPremium() ? "details-premium-chip" : "details-chip");
        userEmailLabel.setText(currentUserEmail == null ? "Utilisateur: -" : "Utilisateur: " + currentUserEmail);
    }

    private void loadReviews() {
        if (event == null) {
            return;
        }

        List<Review> reviews = reviewService.getReviewsByEvent(event.getIdEvent());
        double average = reviewService.getAverageNoteByEvent(event.getIdEvent());

        averageLabel.setText(String.format(Locale.US, "%.1f / 5", average));
        reviewCountLabel.setText(reviews.size() + " avis");

        reviewsContainer.getChildren().clear();
        if (reviews.isEmpty()) {
            Label empty = new Label("Aucun avis pour le moment. Soyez le premier a noter cet evenement.");
            empty.getStyleClass().add("details-empty-text");
            reviewsContainer.getChildren().add(empty);
            return;
        }

        for (Review review : reviews) {
            reviewsContainer.getChildren().add(buildReviewCard(review));
        }
    }

    private VBox buildReviewCard(Review review) {
        VBox card = new VBox(8);
        card.getStyleClass().add("review-card");

        Label header = new Label(review.getEmailParticipant() + " | " + buildStars(review.getNote()));
        header.getStyleClass().add("review-header");

        Label date = new Label(review.getCreatedAt() == null ? "-" : DATE_TIME_FORMATTER.format(review.getCreatedAt().toLocalDateTime()));
        date.getStyleClass().add("review-date");

        Label commentaire = new Label(nullSafe(review.getCommentaire()));
        commentaire.setWrapText(true);
        commentaire.getStyleClass().add("review-comment");

        card.getChildren().addAll(header, date, commentaire);
        return card;
    }

    private String buildStars(int note) {
        return "*".repeat(Math.max(0, note)) + "-".repeat(Math.max(0, 5 - note));
    }

    private String requireCurrentUserEmail() {
        if (currentUserEmail == null || currentUserEmail.isBlank() || !currentUserEmail.contains("@")) {
            throw new IllegalArgumentException("Saisis un email utilisateur valide pour laisser un avis.");
        }
        return currentUserEmail.trim();
    }

    private int requireNote() {
        Integer note = noteComboBox.getValue();
        if (note == null) {
            throw new IllegalArgumentException("La note est obligatoire.");
        }
        if (note < 1 || note > 5) {
            throw new IllegalArgumentException("La note doit etre comprise entre 1 et 5.");
        }
        return note;
    }

    private String requireCommentaire() {
        String commentaire = commentaireArea.getText();
        if (commentaire == null || commentaire.trim().isEmpty()) {
            throw new IllegalArgumentException("Le commentaire est obligatoire.");
        }
        return commentaire.trim();
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
