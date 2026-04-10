package tn.esprit.Pidev3A49.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.Pidev3A49.Models.Supplement;
import tn.esprit.Pidev3A49.services.ServiceSupplement;
import tn.esprit.Pidev3A49.utils.SceneNavigator;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public class SupplementShowcaseController {

    @FXML
    private ScrollPane rootScrollPane;

    @FXML
    private VBox contentRoot;

    @FXML
    private VBox cartPanel;

    @FXML
    private FlowPane productGrid;

    @FXML
    private Label productSummaryLabel;

    @FXML
    private Label productHintLabel;

    @FXML
    private Label heroCountLabel;

    @FXML
    private Label availableProductsCountLabel;

    private ServiceSupplement serviceSupplement;

    @FXML
    private void initialize() {
        loadProducts();
    }

    public void openProgressTracker(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.FRONT_END_VIEW, SceneNavigator.PROGRESS_VIEW);
    }

    public void openMonthlyRanking(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.FRONT_END_VIEW, SceneNavigator.RANKING_VIEW);
    }

    public void openBackEnd(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.FRONT_END_VIEW, SceneNavigator.BACK_END_VIEW);
    }

    public void openCheckout(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.FRONT_END_VIEW, SceneNavigator.CHECKOUT_VIEW);
    }

    public void goBackOrExit(ActionEvent event) throws IOException {
        SceneNavigator.goBackOrClose(event);
    }

    @FXML
    private void showCartPanel() {
        cartPanel.setManaged(true);
        cartPanel.setVisible(true);

        Platform.runLater(() -> {
            double scrollableHeight = contentRoot.getBoundsInLocal().getHeight() - rootScrollPane.getViewportBounds().getHeight();
            if (scrollableHeight <= 0) {
                rootScrollPane.setVvalue(0.0);
                return;
            }

            double targetY = cartPanel.getBoundsInParent().getMinY();
            double targetValue = Math.max(0.0, Math.min(1.0, targetY / scrollableHeight));
            rootScrollPane.setVvalue(targetValue);
        });
    }

    @FXML
    private void hideCartPanel() {
        cartPanel.setVisible(false);
        cartPanel.setManaged(false);
    }

    private void loadProducts() {
        try {
            serviceSupplement = new ServiceSupplement();
            List<Supplement> supplements = serviceSupplement.getAll();
            updateHeader(supplements.size());
            renderProducts(supplements);
        } catch (RuntimeException exception) {
            updateHeader(0);
            productSummaryLabel.setText("Showing 0 products");
            productHintLabel.setText("MySQL is unavailable. Start the database to load products added from the back end.");
            productGrid.getChildren().setAll(buildEmptyCard(
                    "Database unavailable",
                    "The shop could not load supplements from MySQL. Start the database, then reopen this page."
            ));
        }
    }

    private void updateHeader(int productCount) {
        heroCountLabel.setText(productCount + " products synced from the back end");
        availableProductsCountLabel.setText(Integer.toString(productCount));
    }

    private void renderProducts(List<Supplement> supplements) {
        productGrid.getChildren().clear();
        if (supplements.isEmpty()) {
            productSummaryLabel.setText("Showing 0 products");
            productHintLabel.setText("Add a supplement from the back end and it will appear here automatically.");
            productGrid.getChildren().add(buildEmptyCard(
                    "No supplements yet",
                    "Use the admin page to create a supplement, then reopen the front end store."
            ));
            return;
        }

        productSummaryLabel.setText("Showing " + supplements.size() + " of " + supplements.size() + " products");
        productHintLabel.setText("These products are loaded directly from the supplements table used by the back end.");

        for (Supplement supplement : supplements) {
            productGrid.getChildren().add(buildProductCard(supplement));
        }
    }

    private VBox buildProductCard(Supplement supplement) {
        VBox card = new VBox(14.0);
        card.setPrefWidth(272.0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 26; -fx-border-color: rgba(8, 67, 58, 0.08); "
                + "-fx-border-radius: 26; -fx-effect: dropshadow(gaussian, rgba(7, 35, 29, 0.12), 22, 0.22, 0, 10);");

        StackPane visualPane = new StackPane();
        visualPane.setPrefHeight(214.0);
        visualPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #D7F5EB, #EEF9F5); -fx-background-radius: 26 26 0 0;");

        Label visualLabel = new Label((valueOrDefault(supplement.getCategory()) + " visual").toUpperCase(Locale.ROOT));
        visualLabel.setStyle("-fx-text-fill: rgba(9, 41, 53, 0.48); -fx-font-size: 18px; -fx-font-weight: 800;");

        Label topBadge = createChip(stockLabel(supplement), stockChipStyle(supplement));
        StackPane.setMargin(topBadge, new Insets(14.0, 14.0, 0.0, 0.0));
        StackPane.setAlignment(topBadge, javafx.geometry.Pos.TOP_RIGHT);
        visualPane.getChildren().addAll(visualLabel, topBadge);

        VBox detailsBox = new VBox(10.0);
        detailsBox.setPadding(new Insets(0.0, 18.0, 18.0, 18.0));

        Label brandLabel = new Label(valueOrDefault(supplement.getBrand()).toUpperCase(Locale.ROOT));
        brandLabel.setStyle("-fx-text-fill: #4E877A; -fx-font-size: 12px; -fx-font-weight: 900; -fx-letter-spacing: 1px;");

        Label nameLabel = new Label(valueOrDefault(supplement.getName()));
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-text-fill: #123748; -fx-font-size: 26px; -fx-font-weight: 900;");

        Label descriptionLabel = new Label(truncate(valueOrDefault(supplement.getDescription()), 60));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-text-fill: #80929C; -fx-font-size: 14px; -fx-font-weight: 700;");

        Label priceLabel = new Label(formatPrice(supplement.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #166E5B; -fx-font-size: 28px; -fx-font-weight: 900;");

        HBox tagRow = new HBox(8.0);
        tagRow.getChildren().add(createChip(valueOrDefault(supplement.getCategory()), "-fx-background-color: #EFF8F4; -fx-text-fill: #4E877A;"));
        if (supplement.getCalories() != null) {
            tagRow.getChildren().add(createChip(supplement.getCalories() + " kcal", "-fx-background-color: #EEF6FF; -fx-text-fill: #245C83;"));
        }

        Button quickViewButton = new Button("Quick View");
        quickViewButton.setPrefHeight(34.0);
        quickViewButton.setMaxWidth(Double.MAX_VALUE);
        quickViewButton.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-border-color: #D4E2EA; "
                + "-fx-border-radius: 12; -fx-text-fill: #496170; -fx-font-size: 13px; -fx-font-weight: 700;");

        Button stockButton = new Button("Stock: " + supplement.getStock());
        stockButton.setPrefHeight(34.0);
        stockButton.setMaxWidth(Double.MAX_VALUE);
        stockButton.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #D4E2EA; "
                + "-fx-border-radius: 12; -fx-text-fill: #496170; -fx-font-size: 13px; -fx-font-weight: 700;");

        Button addToCartButton = new Button("ADD TO CART");
        addToCartButton.setPrefHeight(42.0);
        addToCartButton.setMaxWidth(Double.MAX_VALUE);
        addToCartButton.setDisable(supplement.getStock() <= 0);
        addToCartButton.setStyle("-fx-background-color: linear-gradient(to right, #124A4D, #0F6A58); -fx-background-radius: 14; "
                + "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 900;");
        addToCartButton.setOnAction(event -> showCartPanel());

        detailsBox.getChildren().addAll(
                brandLabel,
                nameLabel,
                descriptionLabel,
                priceLabel,
                tagRow,
                quickViewButton,
                stockButton,
                addToCartButton
        );

        card.getChildren().addAll(visualPane, detailsBox);
        return card;
    }

    private VBox buildEmptyCard(String title, String message) {
        VBox card = new VBox(12.0);
        card.setPrefWidth(958.0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 26; -fx-border-color: rgba(8, 67, 58, 0.08); "
                + "-fx-border-radius: 26; -fx-padding: 24 24 24 24;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #123748; -fx-font-size: 24px; -fx-font-weight: 900;");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: #80929C; -fx-font-size: 15px; -fx-font-weight: 700;");

        card.getChildren().addAll(titleLabel, messageLabel);
        return card;
    }

    private Label createChip(String text, String colors) {
        Label chip = new Label(text);
        chip.setStyle(colors + " -fx-background-radius: 999; -fx-font-size: 11px; -fx-font-weight: 800; -fx-padding: 7 10 7 10;");
        return chip;
    }

    private String stockLabel(Supplement supplement) {
        if (supplement.getStock() <= 0) {
            return "OUT OF STOCK";
        }
        if (supplement.getStock() <= 5) {
            return "LOW STOCK";
        }
        return "IN STOCK";
    }

    private String stockChipStyle(Supplement supplement) {
        if (supplement.getStock() <= 0) {
            return "-fx-background-color: #FFF1F2; -fx-text-fill: #D92D20;";
        }
        if (supplement.getStock() <= 5) {
            return "-fx-background-color: #FFF7E6; -fx-text-fill: #B66905;";
        }
        return "-fx-background-color: #EEF9F1; -fx-text-fill: #1D7E56;";
    }

    private String valueOrDefault(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "-";
        }
        return price.stripTrailingZeros().toPlainString() + " DT";
    }
}
