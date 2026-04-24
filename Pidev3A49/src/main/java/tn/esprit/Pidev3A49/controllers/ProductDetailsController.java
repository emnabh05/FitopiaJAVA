package tn.esprit.Pidev3A49.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tn.esprit.Pidev3A49.Models.Supplement;
import tn.esprit.Pidev3A49.Models.SupplementReview;
import tn.esprit.Pidev3A49.services.ServiceSupplementReview;
import tn.esprit.Pidev3A49.utils.CartStore;
import tn.esprit.Pidev3A49.utils.SceneNavigator;
import tn.esprit.Pidev3A49.utils.SelectedSupplementStore;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ProductDetailsController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.ENGLISH);
    private static final String STATUS_OK_STYLE = "-fx-text-fill: #0F6A58; -fx-font-size: 13px; -fx-font-weight: 700;";
    private static final String STATUS_ERROR_STYLE = "-fx-text-fill: #D92D20; -fx-font-size: 13px; -fx-font-weight: 700;";
    private static final String REVIEW_OK_STYLE = "-fx-text-fill: #0F6A58; -fx-font-size: 13px; -fx-font-weight: 700;";
    private static final String REVIEW_ERROR_STYLE = "-fx-text-fill: #D92D20; -fx-font-size: 13px; -fx-font-weight: 700;";
    private static final String STAR_ACTIVE_STYLE = "-fx-background-color: #0F6A58; -fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 900; -fx-pref-width: 42; -fx-pref-height: 38;";
    private static final String STAR_INACTIVE_STYLE = "-fx-background-color: #F4F8F6; -fx-background-radius: 10; -fx-border-color: #D8E5E0; -fx-border-radius: 10; -fx-text-fill: #6C8290; -fx-font-size: 17px; -fx-font-weight: 900; -fx-pref-width: 42; -fx-pref-height: 38;";
    private static final String REVIEW_CARD_STYLE = "-fx-background-color: #F8FCFA; -fx-background-radius: 14; -fx-border-color: #DFECE6; -fx-border-radius: 14; -fx-padding: 12 12 12 12;";
    private static final String FILLED_STAR = "\u2605";
    private static final String EMPTY_STAR = "\u2606";

    @FXML
    private Label productNameLabel;

    @FXML
    private Label brandValueLabel;

    @FXML
    private Label categoryValueLabel;

    @FXML
    private Label priceValueLabel;

    @FXML
    private Label caloriesValueLabel;

    @FXML
    private Label stockValueLabel;

    @FXML
    private Label postedAtValueLabel;

    @FXML
    private Label updatedAtValueLabel;

    @FXML
    private Label descriptionValueLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button addToCartButton;

    @FXML
    private Label averageRatingValueLabel;

    @FXML
    private Label totalReviewsValueLabel;

    @FXML
    private Button star1Button;

    @FXML
    private Button star2Button;

    @FXML
    private Button star3Button;

    @FXML
    private Button star4Button;

    @FXML
    private Button star5Button;

    @FXML
    private Label selectedRatingLabel;

    @FXML
    private TextArea reviewCommentTextArea;

    @FXML
    private Button submitReviewButton;

    @FXML
    private Label reviewStatusLabel;

    @FXML
    private VBox reviewsContainer;

    private final CartStore cartStore = CartStore.getInstance();
    private final SelectedSupplementStore selectedSupplementStore = SelectedSupplementStore.getInstance();
    private ServiceSupplementReview reviewService;

    private Supplement selectedSupplement;
    private List<Button> starButtons = List.of();
    private int selectedRating;

    @FXML
    private void initialize() {
        selectedSupplement = selectedSupplementStore.getSelectedSupplement();
        starButtons = List.of(star1Button, star2Button, star3Button, star4Button, star5Button);
        updateRatingSelection(0);
        renderProduct();
        loadReviews();
    }

    @FXML
    private void goBack(ActionEvent event) throws IOException {
        SceneNavigator.goBackOrClose(event);
    }

    @FXML
    private void openCheckout(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.PRODUCT_DETAILS_VIEW, SceneNavigator.CHECKOUT_VIEW);
    }

    @FXML
    private void openMyOrders(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.PRODUCT_DETAILS_VIEW, SceneNavigator.FRONT_ORDERS_VIEW);
    }

    @FXML
    private void addSelectedToCart() {
        if (selectedSupplement == null) {
            setStatus("No product is selected.", true);
            return;
        }

        try {
            cartStore.addSupplement(selectedSupplement);
            setStatus(valueOrDash(selectedSupplement.getName()) + " added to cart.", false);
        } catch (RuntimeException exception) {
            setStatus(exception.getMessage(), true);
        }
    }

    @FXML
    private void selectRating(ActionEvent event) {
        if (!(event.getSource() instanceof Button starButton)) {
            return;
        }

        int rating;
        try {
            rating = Integer.parseInt(String.valueOf(starButton.getUserData()));
        } catch (NumberFormatException exception) {
            return;
        }
        updateRatingSelection(rating);
        setReviewStatus("", false);
    }

    @FXML
    private void submitReview() {
        if (selectedSupplement == null) {
            setReviewStatus("No product is selected.", true);
            return;
        }
        if (selectedRating < 1 || selectedRating > 5) {
            setReviewStatus("Please choose a rating from 1 to 5 stars.", true);
            return;
        }

        String comment = reviewCommentTextArea.getText() == null ? "" : reviewCommentTextArea.getText().trim();
        if (comment.isBlank()) {
            setReviewStatus("Please add a comment before submitting.", true);
            return;
        }

        try {
            getReviewService().addReview(selectedSupplement.getId(), selectedRating, comment);
            reviewCommentTextArea.clear();
            updateRatingSelection(0);
            setReviewStatus("Thanks! Your rating and comment were saved.", false);
            loadReviews();
        } catch (RuntimeException exception) {
            setReviewStatus(exception.getMessage(), true);
        }
    }

    private void renderProduct() {
        if (selectedSupplement == null) {
            productNameLabel.setText("Product details unavailable");
            brandValueLabel.setText("-");
            categoryValueLabel.setText("-");
            priceValueLabel.setText("-");
            caloriesValueLabel.setText("-");
            stockValueLabel.setText("-");
            postedAtValueLabel.setText("-");
            updatedAtValueLabel.setText("-");
            descriptionValueLabel.setText("Open a product from the catalog to see details here.");
            addToCartButton.setDisable(true);
            disableReviewForm(true);
            setStatus("No product loaded from catalog.", true);
            averageRatingValueLabel.setText("-");
            totalReviewsValueLabel.setText("-");
            renderReviewEmptyState("Open a product from the catalog to read and add comments.");
            return;
        }

        productNameLabel.setText(valueOrDash(selectedSupplement.getName()));
        brandValueLabel.setText(valueOrDash(selectedSupplement.getBrand()));
        categoryValueLabel.setText(valueOrDash(selectedSupplement.getCategory()));
        priceValueLabel.setText(formatPrice(selectedSupplement.getPrice()));
        caloriesValueLabel.setText(selectedSupplement.getCalories() == null ? "-" : selectedSupplement.getCalories() + " kcal");
        stockValueLabel.setText(Integer.toString(selectedSupplement.getStock()));
        postedAtValueLabel.setText(formatDate(selectedSupplement.getCreatedAt()));
        updatedAtValueLabel.setText(formatDate(selectedSupplement.getUpdatedAt()));
        descriptionValueLabel.setText(valueOrDash(selectedSupplement.getDescription()));
        addToCartButton.setDisable(selectedSupplement.getStock() <= 0);
        disableReviewForm(false);

        if (selectedSupplement.getStock() <= 0) {
            setStatus("This product is currently out of stock.", true);
        } else {
            setStatus("Product loaded. You can add it to cart.", false);
        }
    }

    private void loadReviews() {
        if (selectedSupplement == null) {
            averageRatingValueLabel.setText("-");
            totalReviewsValueLabel.setText("-");
            renderReviewEmptyState("Open a product from the catalog to read and add comments.");
            return;
        }

        try {
            List<SupplementReview> reviews = getReviewService().getBySupplementId(selectedSupplement.getId());
            renderReviewSummary(reviews);
            renderReviews(reviews);
        } catch (RuntimeException exception) {
            averageRatingValueLabel.setText("Unavailable");
            totalReviewsValueLabel.setText("-");
            renderReviewEmptyState("Could not load comments right now.");
        }
    }

    private void renderReviewSummary(List<SupplementReview> reviews) {
        if (reviews.isEmpty()) {
            averageRatingValueLabel.setText("No rating yet");
            totalReviewsValueLabel.setText("0 reviews");
            return;
        }

        double average = reviews.stream()
                .mapToInt(SupplementReview::getRating)
                .average()
                .orElse(0.0);

        averageRatingValueLabel.setText(String.format(Locale.ENGLISH, "%.1f / 5", average));
        totalReviewsValueLabel.setText(reviews.size() + " review(s)");
    }

    private void renderReviews(List<SupplementReview> reviews) {
        reviewsContainer.getChildren().clear();
        if (reviews.isEmpty()) {
            renderReviewEmptyState("No comments yet. Be the first to review this product.");
            return;
        }

        for (SupplementReview review : reviews) {
            reviewsContainer.getChildren().add(buildReviewCard(review));
        }
    }

    private VBox buildReviewCard(SupplementReview review) {
        VBox reviewCard = new VBox(8.0);
        reviewCard.setStyle(REVIEW_CARD_STYLE);

        HBox header = new HBox(10.0);

        Label reviewerLabel = new Label(valueOrDash(review.getReviewerName()));
        reviewerLabel.setStyle("-fx-text-fill: #153D4E; -fx-font-size: 13px; -fx-font-weight: 900;");

        Label ratingLabel = new Label(renderStars(review.getRating()) + "  " + review.getRating() + "/5");
        ratingLabel.setStyle("-fx-text-fill: #0E3F4F; -fx-font-size: 13px; -fx-font-weight: 900;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateLabel = new Label(formatDate(review.getCreatedAt()));
        dateLabel.setStyle("-fx-text-fill: #7A909B; -fx-font-size: 12px; -fx-font-weight: 700;");

        header.getChildren().addAll(reviewerLabel, ratingLabel, spacer, dateLabel);

        Label commentLabel = new Label(valueOrDash(review.getComment()));
        commentLabel.setWrapText(true);
        commentLabel.setStyle("-fx-text-fill: #5F7885; -fx-font-size: 14px; -fx-font-weight: 600; -fx-line-spacing: 2px;");

        reviewCard.getChildren().addAll(header, commentLabel);
        return reviewCard;
    }

    private void renderReviewEmptyState(String message) {
        Label emptyLabel = new Label(message);
        emptyLabel.setWrapText(true);
        emptyLabel.setStyle("-fx-text-fill: #7A909B; -fx-font-size: 13px; -fx-font-weight: 700;");
        reviewsContainer.getChildren().setAll(emptyLabel);
    }

    private void updateRatingSelection(int rating) {
        selectedRating = Math.max(0, Math.min(5, rating));

        for (int index = 0; index < starButtons.size(); index++) {
            Button starButton = starButtons.get(index);
            boolean active = index < selectedRating;
            starButton.setText(active ? FILLED_STAR : EMPTY_STAR);
            starButton.setStyle(active ? STAR_ACTIVE_STYLE : STAR_INACTIVE_STYLE);
        }

        selectedRatingLabel.setText(
                selectedRating == 0
                        ? "Choose a rating (1-5 stars)."
                        : selectedRating + " / 5 selected"
        );
    }

    private void disableReviewForm(boolean disabled) {
        for (Button starButton : starButtons) {
            starButton.setDisable(disabled);
        }
        reviewCommentTextArea.setDisable(disabled);
        submitReviewButton.setDisable(disabled);
    }

    private String renderStars(int rating) {
        int safeRating = Math.max(0, Math.min(5, rating));
        return FILLED_STAR.repeat(safeRating) + EMPTY_STAR.repeat(5 - safeRating);
    }

    private ServiceSupplementReview getReviewService() {
        if (reviewService == null) {
            reviewService = new ServiceSupplementReview();
        }
        return reviewService;
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "-";
        }
        return price.setScale(2, RoundingMode.HALF_UP).toPlainString() + " DT";
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return DATE_TIME_FORMATTER.format(dateTime);
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setStyle(error ? STATUS_ERROR_STYLE : STATUS_OK_STYLE);
    }

    private void setReviewStatus(String message, boolean error) {
        reviewStatusLabel.setText(message == null ? "" : message);
        reviewStatusLabel.setStyle(error ? REVIEW_ERROR_STYLE : REVIEW_OK_STYLE);
    }
}
