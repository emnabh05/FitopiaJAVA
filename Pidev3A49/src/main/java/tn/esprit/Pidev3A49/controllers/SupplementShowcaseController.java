package tn.esprit.Pidev3A49.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.Pidev3A49.Models.CartItem;
import tn.esprit.Pidev3A49.Models.Supplement;
import tn.esprit.Pidev3A49.services.ServiceSupplement;
import tn.esprit.Pidev3A49.utils.CartStore;
import tn.esprit.Pidev3A49.utils.SceneNavigator;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class SupplementShowcaseController {

    private static final String CART_INFO_STYLE =
            "-fx-text-fill: #0F6A58; -fx-font-size: 13px; -fx-font-weight: 700;";

    private static final String CART_ERROR_STYLE =
            "-fx-text-fill: #D92D20; -fx-font-size: 13px; -fx-font-weight: 700;";

    private static final String SORT_DEFAULT = "Sort by...";
    private static final String SORT_DATE_NEWEST = "Date posted (Newest)";
    private static final String SORT_DATE_OLDEST = "Date posted (Oldest)";
    private static final String SORT_NAME_ASC = "Name (A-Z)";
    private static final String SORT_NAME_DESC = "Name (Z-A)";
    private static final String SORT_PRICE_ASC = "Price (Low to High)";
    private static final String SORT_PRICE_DESC = "Price (High to Low)";

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
    private TextField searchField;

    @FXML
    private VBox categoryFiltersContainer;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private Label heroCountLabel;

    @FXML
    private Label availableProductsCountLabel;

    @FXML
    private VBox cartItemsContainer;

    @FXML
    private Label cartSubtotalLabel;

    @FXML
    private Label cartShippingLabel;

    @FXML
    private Label cartTotalLabel;

    @FXML
    private Label cartStatusLabel;

    @FXML
    private Button proceedToCheckoutButton;

    private final CartStore cartStore = CartStore.getInstance();
    private ServiceSupplement serviceSupplement;
    private List<Supplement> allSupplements = List.of();
    private final List<CheckBox> categoryCheckBoxes = new ArrayList<>();

    @FXML
    private void initialize() {
        configureFilterControls();
        loadProducts();
        renderCart();
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

    public void openFitnessFront(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.FRONT_END_VIEW, SceneNavigator.FITNESS_FRONT_VIEW);
    }

    public void openCheckout(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.FRONT_END_VIEW, SceneNavigator.CHECKOUT_VIEW);
    }

    public void openMyOrders(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.FRONT_END_VIEW, SceneNavigator.FRONT_ORDERS_VIEW);
    }

    public void goBackOrExit(ActionEvent event) throws IOException {
        SceneNavigator.goBackOrClose(event);
    }

    @FXML
    private void showCartPanel() {
        renderCart();
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

    @FXML
    private void clearFilters() {
        if (searchField != null) {
            searchField.clear();
        }
        for (CheckBox categoryCheckBox : categoryCheckBoxes) {
            categoryCheckBox.setSelected(false);
        }
        if (sortComboBox != null) {
            sortComboBox.getSelectionModel().select(SORT_DEFAULT);
        }
        applyFilters();
    }

    private void configureFilterControls() {
        sortComboBox.setItems(FXCollections.observableArrayList(
                SORT_DEFAULT,
                SORT_DATE_NEWEST,
                SORT_DATE_OLDEST,
                SORT_NAME_ASC,
                SORT_NAME_DESC,
                SORT_PRICE_ASC,
                SORT_PRICE_DESC
        ));
        sortComboBox.getSelectionModel().select(SORT_DEFAULT);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        sortComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void loadProducts() {
        try {
            serviceSupplement = new ServiceSupplement();
            List<Supplement> supplements = serviceSupplement.getAll();
            allSupplements = supplements;
            updateHeader(supplements.size());
            renderCategoryFilters(supplements);
            applyFilters();
        } catch (RuntimeException exception) {
            allSupplements = List.of();
            updateHeader(0);
            categoryCheckBoxes.clear();
            categoryFiltersContainer.getChildren().setAll(buildCategoryHintLabel("No category data available."));
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

    private void applyFilters() {
        if (allSupplements.isEmpty()) {
            renderProducts(List.of(), 0, "", 0);
            return;
        }

        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        Set<String> selectedCategories = categoryCheckBoxes.stream()
                .filter(CheckBox::isSelected)
                .map(checkBox -> normalizeCategory(checkBox.getText()))
                .collect(Collectors.toSet());

        Comparator<Supplement> comparator = resolveComparator(sortComboBox.getValue());

        List<Supplement> filteredSupplements = allSupplements.stream()
                .filter(supplement -> matchesSearch(supplement, query))
                .filter(supplement -> selectedCategories.isEmpty()
                        || selectedCategories.contains(normalizeCategory(supplement.getCategory())))
                .sorted(comparator)
                .toList();

        renderProducts(filteredSupplements, allSupplements.size(), query, selectedCategories.size());
    }

    private void renderCategoryFilters(List<Supplement> supplements) {
        categoryCheckBoxes.clear();
        categoryFiltersContainer.getChildren().clear();

        TreeSet<String> uniqueCategories = supplements.stream()
                .map(Supplement::getCategory)
                .filter(category -> category != null && !category.isBlank())
                .map(String::trim)
                .collect(Collectors.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)));

        if (uniqueCategories.isEmpty()) {
            categoryFiltersContainer.getChildren().add(buildCategoryHintLabel("No categories yet."));
            return;
        }

        for (String category : uniqueCategories) {
            CheckBox categoryCheckBox = new CheckBox(category);
            categoryCheckBox.setStyle("-fx-font-size: 13px; -fx-text-fill: #667A86;");
            categoryCheckBox.setOnAction(event -> applyFilters());
            categoryCheckBoxes.add(categoryCheckBox);
            categoryFiltersContainer.getChildren().add(categoryCheckBox);
        }
    }

    private void renderProducts(List<Supplement> supplements, int totalProducts, String query, int selectedCategoryCount) {
        productGrid.getChildren().clear();
        if (totalProducts == 0) {
            productSummaryLabel.setText("Showing 0 products");
            productHintLabel.setText("Add a supplement from the back end and it will appear here automatically.");
            productGrid.getChildren().add(buildEmptyCard(
                    "No supplements yet",
                    "Use the admin page to create a supplement, then reopen the front end store."
            ));
            return;
        }

        productSummaryLabel.setText("Showing " + supplements.size() + " of " + totalProducts + " products");

        if (supplements.isEmpty()) {
            productHintLabel.setText("No product matches your search/filter selection.");
            productGrid.getChildren().add(buildEmptyCard(
                    "No matching supplements",
                    "Try a different keyword, clear some categories, or change the sorting mode."
            ));
            return;
        }

        productHintLabel.setText(buildHintMessage(query, selectedCategoryCount));

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
        StackPane.setAlignment(topBadge, Pos.TOP_RIGHT);
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
        addToCartButton.setOnAction(event -> addToCart(supplement));

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

    private void addToCart(Supplement supplement) {
        try {
            cartStore.addSupplement(supplement);
            setCartMessage(valueOrDefault(supplement.getName()) + " added to cart.", false);
            showCartPanel();
        } catch (RuntimeException exception) {
            setCartMessage(exception.getMessage(), true);
            showCartPanel();
        }
    }

    private void renderCart() {
        cartItemsContainer.getChildren().clear();

        List<CartItem> items = cartStore.getItems();
        if (items.isEmpty()) {
            cartItemsContainer.getChildren().add(buildEmptyCartState());
            cartSubtotalLabel.setText("0.00 DT");
            cartShippingLabel.setText("0.00 DT");
            cartTotalLabel.setText("0.00 DT");
            proceedToCheckoutButton.setDisable(true);
            if (cartStatusLabel.getText() == null || cartStatusLabel.getText().isBlank()) {
                setCartMessage("Your cart is empty.", false);
            }
            return;
        }

        for (CartItem item : items) {
            cartItemsContainer.getChildren().add(buildCartItemRow(item));
        }

        cartSubtotalLabel.setText(formatPrice(cartStore.getSubtotal()));
        cartShippingLabel.setText(formatPrice(cartStore.getShippingCost()));
        cartTotalLabel.setText(formatPrice(cartStore.getTotal(BigDecimal.ZERO)));
        proceedToCheckoutButton.setDisable(false);
    }

    private VBox buildEmptyCartState() {
        VBox emptyState = new VBox(8.0);
        emptyState.setStyle("-fx-background-color: #F6FAFC; -fx-background-radius: 18; -fx-border-color: #D9E6ED; "
                + "-fx-border-radius: 18; -fx-padding: 18 18 18 18;");

        Label titleLabel = new Label("Your cart is empty");
        titleLabel.setStyle("-fx-text-fill: #113748; -fx-font-size: 18px; -fx-font-weight: 900;");

        Label hintLabel = new Label("Add one or more supplements from the product grid, then proceed to checkout.");
        hintLabel.setWrapText(true);
        hintLabel.setStyle("-fx-text-fill: #768995; -fx-font-size: 13px; -fx-font-weight: 600;");

        emptyState.getChildren().addAll(titleLabel, hintLabel);
        return emptyState;
    }

    private VBox buildCartItemRow(CartItem item) {
        Supplement supplement = item.getSupplement();

        VBox wrapper = new VBox(12.0);

        HBox row = new HBox(16.0);
        row.setAlignment(Pos.CENTER_LEFT);

        StackPane visual = new StackPane();
        visual.setPrefSize(60.0, 60.0);
        visual.setStyle("-fx-background-color: linear-gradient(to bottom right, #D8F0FF, #F1FAFF); -fx-background-radius: 14;");
        Label visualLabel = new Label(valueOrDefault(supplement.getBrand()).substring(0, Math.min(3, valueOrDefault(supplement.getBrand()).length())).toUpperCase(Locale.ROOT));
        visualLabel.setStyle("-fx-text-fill: #557181; -fx-font-size: 13px; -fx-font-weight: 900;");
        visual.getChildren().add(visualLabel);

        VBox details = new VBox(4.0);
        details.setPrefWidth(250.0);
        Label nameLabel = new Label(valueOrDefault(supplement.getName()));
        nameLabel.setStyle("-fx-text-fill: #0D1B25; -fx-font-size: 18px; -fx-font-weight: 800;");
        Label metaLabel = new Label(valueOrDefault(supplement.getBrand()) + " | " + valueOrDefault(supplement.getCategory()));
        metaLabel.setStyle("-fx-text-fill: #6F8290; -fx-font-size: 13px;");
        details.getChildren().addAll(nameLabel, metaLabel);

        Label unitPriceLabel = new Label(formatPrice(supplement.getPrice()));
        unitPriceLabel.setStyle("-fx-text-fill: #0F3444; -fx-font-size: 16px; -fx-font-weight: 700;");

        HBox quantityBox = new HBox(8.0);
        quantityBox.setAlignment(Pos.CENTER);
        Button decreaseButton = buildCartControlButton("-");
        decreaseButton.setOnAction(event -> {
            cartStore.decreaseQuantity(supplement.getId());
            setCartMessage("Cart updated.", false);
            renderCart();
        });

        Label quantityLabel = new Label(Integer.toString(item.getQuantity()));
        quantityLabel.setStyle("-fx-text-fill: #0D1B25; -fx-font-size: 16px; -fx-font-weight: 700;");

        Button increaseButton = buildCartControlButton("+");
        increaseButton.setOnAction(event -> {
            try {
                cartStore.increaseQuantity(supplement.getId());
                setCartMessage("Cart updated.", false);
            } catch (RuntimeException exception) {
                setCartMessage(exception.getMessage(), true);
            }
            renderCart();
        });
        quantityBox.getChildren().addAll(decreaseButton, quantityLabel, increaseButton);

        Label lineTotalLabel = new Label(formatPrice(item.getLineTotal()));
        lineTotalLabel.setStyle("-fx-text-fill: #0F3444; -fx-font-size: 16px; -fx-font-weight: 800;");

        Button removeButton = new Button("DEL");
        removeButton.setPrefWidth(44.0);
        removeButton.setPrefHeight(32.0);
        removeButton.setStyle("-fx-background-color: #FFE9E7; -fx-background-radius: 11; -fx-text-fill: #F25752; -fx-font-size: 12px; -fx-font-weight: 900;");
        removeButton.setOnAction(event -> {
            cartStore.removeSupplement(supplement.getId());
            setCartMessage(valueOrDefault(supplement.getName()) + " removed from cart.", false);
            renderCart();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(visual, details, spacer, unitPriceLabel, quantityBox, lineTotalLabel, removeButton);

        Region separator = new Region();
        separator.setPrefHeight(1.0);
        separator.setStyle("-fx-background-color: #E4EDF1;");

        wrapper.getChildren().addAll(row, separator);
        return wrapper;
    }

    private Button buildCartControlButton(String text) {
        Button button = new Button(text);
        button.setPrefSize(28.0, 28.0);
        button.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #D4E2EA; "
                + "-fx-border-radius: 10; -fx-text-fill: #183948; -fx-font-size: 14px; -fx-font-weight: 800;");
        return button;
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

    private String buildHintMessage(String query, int selectedCategoryCount) {
        boolean hasQuery = query != null && !query.isBlank();
        boolean hasCategoryFilter = selectedCategoryCount > 0;

        if (!hasQuery && !hasCategoryFilter) {
            return "These products are loaded directly from the supplements table used by the back end.";
        }
        if (hasQuery && hasCategoryFilter) {
            return "Search and category filters are active (" + selectedCategoryCount + " categories selected).";
        }
        if (hasQuery) {
            return "Search filter is active. Clear it to see the full catalog.";
        }
        return "Category filter is active (" + selectedCategoryCount + " selected).";
    }

    private Comparator<Supplement> resolveComparator(String selectedSort) {
        String sort = selectedSort == null ? SORT_DEFAULT : selectedSort;
        return switch (sort) {
            case SORT_DATE_OLDEST -> Comparator.comparing(this::postedAtDate);
            case SORT_NAME_ASC -> Comparator.comparing(
                    supplement -> valueOrDefault(supplement.getName()).toLowerCase(Locale.ROOT)
            );
            case SORT_NAME_DESC -> Comparator.comparing(
                    (Supplement supplement) -> valueOrDefault(supplement.getName()).toLowerCase(Locale.ROOT)
            ).reversed();
            case SORT_PRICE_ASC -> Comparator.comparing(this::safePrice);
            case SORT_PRICE_DESC -> Comparator.comparing(this::safePrice).reversed();
            default -> Comparator.comparing(this::postedAtDate).reversed();
        };
    }

    private LocalDateTime postedAtDate(Supplement supplement) {
        if (supplement.getCreatedAt() != null) {
            return supplement.getCreatedAt();
        }
        if (supplement.getUpdatedAt() != null) {
            return supplement.getUpdatedAt();
        }
        return LocalDateTime.MIN;
    }

    private BigDecimal safePrice(Supplement supplement) {
        return supplement.getPrice() == null ? BigDecimal.ZERO : supplement.getPrice();
    }

    private boolean matchesSearch(Supplement supplement, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        return containsIgnoreCase(supplement.getName(), query)
                || containsIgnoreCase(supplement.getBrand(), query)
                || containsIgnoreCase(supplement.getCategory(), query)
                || containsIgnoreCase(supplement.getDescription(), query);
    }

    private boolean containsIgnoreCase(String value, String query) {
        if (value == null || query == null) {
            return false;
        }
        return value.toLowerCase(Locale.ROOT).contains(query);
    }

    private String normalizeCategory(String category) {
        if (category == null) {
            return "";
        }
        return category.trim().toLowerCase(Locale.ROOT);
    }

    private Label buildCategoryHintLabel(String message) {
        Label hintLabel = new Label(message);
        hintLabel.setWrapText(true);
        hintLabel.setStyle("-fx-text-fill: #7C909C; -fx-font-size: 12px; -fx-font-weight: 600;");
        return hintLabel;
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
        return price.setScale(2, RoundingMode.HALF_UP).toPlainString() + " DT";
    }

    private void setCartMessage(String message, boolean error) {
        cartStatusLabel.setText(message);
        cartStatusLabel.setStyle(error ? CART_ERROR_STYLE : CART_INFO_STYLE);
    }
}
