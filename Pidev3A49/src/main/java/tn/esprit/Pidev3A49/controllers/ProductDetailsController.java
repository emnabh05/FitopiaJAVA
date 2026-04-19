package tn.esprit.Pidev3A49.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import tn.esprit.Pidev3A49.Models.Supplement;
import tn.esprit.Pidev3A49.utils.CartStore;
import tn.esprit.Pidev3A49.utils.SceneNavigator;
import tn.esprit.Pidev3A49.utils.SelectedSupplementStore;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ProductDetailsController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.ENGLISH);
    private static final String STATUS_OK_STYLE = "-fx-text-fill: #0F6A58; -fx-font-size: 13px; -fx-font-weight: 700;";
    private static final String STATUS_ERROR_STYLE = "-fx-text-fill: #D92D20; -fx-font-size: 13px; -fx-font-weight: 700;";

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

    private final CartStore cartStore = CartStore.getInstance();
    private final SelectedSupplementStore selectedSupplementStore = SelectedSupplementStore.getInstance();

    private Supplement selectedSupplement;

    @FXML
    private void initialize() {
        selectedSupplement = selectedSupplementStore.getSelectedSupplement();
        renderProduct();
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
            setStatus("No product loaded from catalog.", true);
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

        if (selectedSupplement.getStock() <= 0) {
            setStatus("This product is currently out of stock.", true);
        } else {
            setStatus("Product loaded. You can add it to cart.", false);
        }
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
}
