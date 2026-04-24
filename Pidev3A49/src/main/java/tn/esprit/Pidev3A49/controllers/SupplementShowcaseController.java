package tn.esprit.Pidev3A49.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import tn.esprit.Pidev3A49.Models.SupplementRecommendation;
import tn.esprit.Pidev3A49.services.ServiceSupplementFavorite;
import tn.esprit.Pidev3A49.services.ServiceSupplementRecommendation;
import tn.esprit.Pidev3A49.services.ServiceSupplement;
import tn.esprit.Pidev3A49.utils.AppSession;
import tn.esprit.Pidev3A49.utils.CartStore;
import tn.esprit.Pidev3A49.utils.SceneNavigator;
import tn.esprit.Pidev3A49.utils.SelectedSupplementStore;
import tn.esprit.Pidev3A49.utils.SessionRouter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SupplementShowcaseController {

    private static final String CART_INFO_STYLE =
            "-fx-text-fill: #0F6A58; -fx-font-size: 13px; -fx-font-weight: 700;";

    private static final String CART_ERROR_STYLE =
            "-fx-text-fill: #D92D20; -fx-font-size: 13px; -fx-font-weight: 700;";

    private static final String FAVORITE_INFO_STYLE =
            "-fx-text-fill: #0F6A58; -fx-font-size: 13px; -fx-font-weight: 700;";

    private static final String FAVORITE_ERROR_STYLE =
            "-fx-text-fill: #D92D20; -fx-font-size: 13px; -fx-font-weight: 700;";

    private static final String SUGGESTION_INFO_STYLE =
            "-fx-text-fill: #0F6A58; -fx-font-size: 13px; -fx-font-weight: 700;";

    private static final String SUGGESTION_ERROR_STYLE =
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
    private VBox favoritesPanel;

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
    private VBox favoriteItemsContainer;

    @FXML
    private Label cartSubtotalLabel;

    @FXML
    private Label cartShippingLabel;

    @FXML
    private Label cartTotalLabel;

    @FXML
    private Label cartStatusLabel;

    @FXML
    private Label favoritesStatusLabel;

    @FXML
    private Label favoritesCountLabel;

    @FXML
    private FlowPane suggestionsGrid;

    @FXML
    private Label suggestionsStatusLabel;

    @FXML
    private Label suggestionsCountLabel;

    @FXML
    private Button proceedToCheckoutButton;

    private final CartStore cartStore = CartStore.getInstance();
    private ServiceSupplement serviceSupplement;
    private ServiceSupplementFavorite serviceSupplementFavorite;
    private ServiceSupplementRecommendation serviceSupplementRecommendation;
    private List<Supplement> allSupplements = List.of();
    private final List<CheckBox> categoryCheckBoxes = new ArrayList<>();
    private final Set<Integer> favoriteSupplementIds = new HashSet<>();
    private List<SupplementRecommendation> currentSuggestions = List.of();
    private String currentUserEmail;

    @FXML
    private void initialize() {
        if (!SessionRouter.ensureAuthenticated(rootScrollPane)) {
            return;
        }
        configureFilterControls();
        initializeFavorites();
        initializeRecommendations();
        loadProducts();
        renderCart();
        renderFavorites();
        loadSuggestionsAsync(false);
    }

    public void openProgressTracker(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.FRONT_END_VIEW, SceneNavigator.PROGRESS_VIEW);
    }

    public void openMonthlyRanking(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.FRONT_END_VIEW, SceneNavigator.RANKING_VIEW);
    }

    public void openBackEnd(ActionEvent event) throws IOException {
        SessionRouter.logoutToSignIn((Node) event.getSource());
    }

    public void openCheckout(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.FRONT_END_VIEW, SceneNavigator.CHECKOUT_VIEW);
    }

    public void openMyOrders(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.FRONT_END_VIEW, SceneNavigator.FRONT_ORDERS_VIEW);
    }

    @FXML
    public void openDietPlanner(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.FRONT_END_VIEW, SceneNavigator.FITNESS_FRONT_VIEW);
    }

    @FXML
    public void openSupplementStore(ActionEvent event) {
        if (rootScrollPane != null) {
            rootScrollPane.setVvalue(0.0);
        }
    }

    @FXML
    public void refreshStore(ActionEvent event) {
        loadProducts();
        renderCart();
        renderFavorites();
        loadSuggestionsAsync(false);
        if (rootScrollPane != null) {
            rootScrollPane.setVvalue(0.0);
        }
    }

    @FXML
    public void openEventsSection(ActionEvent event) {
        if (rootScrollPane != null) {
            rootScrollPane.setVvalue(1.0);
        }
    }

    @FXML
    public void openForumFeed(ActionEvent event) {
        SessionRouter.openFrontHome((Button) event.getSource());
    }

    public void goBackOrExit(ActionEvent event) throws IOException {
        if (SceneNavigator.hasHistory()) {
            SceneNavigator.goBackOrClose(event);
            return;
        }
        SessionRouter.openFrontHome((Node) event.getSource());
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
    private void showFavoritesPanel() {
        renderFavorites();
        favoritesPanel.setManaged(true);
        favoritesPanel.setVisible(true);

        Platform.runLater(() -> {
            double scrollableHeight = contentRoot.getBoundsInLocal().getHeight() - rootScrollPane.getViewportBounds().getHeight();
            if (scrollableHeight <= 0) {
                rootScrollPane.setVvalue(0.0);
                return;
            }

            double targetY = favoritesPanel.getBoundsInParent().getMinY();
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
    private void hideFavoritesPanel() {
        favoritesPanel.setVisible(false);
        favoritesPanel.setManaged(false);
    }

    @FXML
    private void refreshSuggestions() {
        loadSuggestionsAsync(true);
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

    private void initializeFavorites() {
        currentUserEmail = resolveFavoriteEmail();
        if (currentUserEmail == null || currentUserEmail.isBlank()) {
            setFavoriteMessage("Connect with a valid email to use favorites.", true);
            return;
        }

        try {
            serviceSupplementFavorite = new ServiceSupplementFavorite();
            favoriteSupplementIds.clear();
            favoriteSupplementIds.addAll(serviceSupplementFavorite.getFavoriteSupplementIdsByEmail(currentUserEmail));
            setFavoriteMessage("Favorites synced for " + currentUserEmail + ".", false);
        } catch (RuntimeException exception) {
            serviceSupplementFavorite = null;
            favoriteSupplementIds.clear();
            setFavoriteMessage("Favorites unavailable: " + exception.getMessage(), true);
        }
    }

    private void initializeRecommendations() {
        try {
            serviceSupplementRecommendation = new ServiceSupplementRecommendation();
            setSuggestionMessage("Suggestions are ready. Click refresh anytime.", false);
        } catch (RuntimeException exception) {
            serviceSupplementRecommendation = null;
            setSuggestionMessage("Suggestions unavailable: " + exception.getMessage(), true);
        }
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
        renderFavorites();
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

        Button favoriteButton = buildFavoriteToggleButton(supplement);
        StackPane.setAlignment(favoriteButton, Pos.TOP_RIGHT);
        StackPane.setMargin(favoriteButton, new Insets(10.0, 10.0, 0.0, 0.0));
        visualPane.getChildren().addAll(visualLabel, favoriteButton);

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

        Button viewDetailsButton = new Button("View Details");
        viewDetailsButton.setPrefHeight(34.0);
        viewDetailsButton.setMaxWidth(Double.MAX_VALUE);
        viewDetailsButton.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-border-color: #D4E2EA; "
                + "-fx-border-radius: 12; -fx-text-fill: #496170; -fx-font-size: 13px; -fx-font-weight: 700;");
        viewDetailsButton.setOnAction(event -> openProductDetails(event, supplement));

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
                viewDetailsButton,
                addToCartButton
        );

        card.getChildren().addAll(visualPane, detailsBox);
        return card;
    }

    private Button buildFavoriteToggleButton(Supplement supplement) {
        Button favoriteButton = new Button();
        favoriteButton.setPrefHeight(34.0);
        favoriteButton.setPrefWidth(34.0);
        boolean isFavorite = favoriteSupplementIds.contains(supplement.getId());
        updateFavoriteButtonStyle(favoriteButton, isFavorite);
        favoriteButton.setOnAction(event -> toggleFavorite(supplement));
        return favoriteButton;
    }

    private void updateFavoriteButtonStyle(Button favoriteButton, boolean favorite) {
        if (favorite) {
            favoriteButton.setText("Fav");
            favoriteButton.setStyle("-fx-background-color: #FFECEE; -fx-background-radius: 999; -fx-border-color: #F5B9C2; "
                    + "-fx-border-radius: 999; -fx-text-fill: #D92D20; -fx-font-size: 15px; -fx-font-weight: 900;");
            return;
        }

        favoriteButton.setText("+Fav");
        favoriteButton.setStyle("-fx-background-color: rgba(255,255,255,0.88); -fx-background-radius: 999; "
                + "-fx-border-color: #D4E2EA; -fx-border-radius: 999; -fx-text-fill: #4E6874; -fx-font-size: 15px; "
                + "-fx-font-weight: 900;");
    }

    private void toggleFavorite(Supplement supplement) {
        if (supplement == null || supplement.getId() <= 0) {
            setFavoriteMessage("Invalid supplement selected.", true);
            return;
        }
        if (serviceSupplementFavorite == null) {
            setFavoriteMessage("Favorites are unavailable because MySQL is not connected.", true);
            return;
        }
        if (currentUserEmail == null || currentUserEmail.isBlank()) {
            setFavoriteMessage("A valid user email is required to save favorites.", true);
            return;
        }

        boolean currentlyFavorite = favoriteSupplementIds.contains(supplement.getId());
        try {
            if (currentlyFavorite) {
                serviceSupplementFavorite.removeFavorite(currentUserEmail, supplement.getId());
                favoriteSupplementIds.remove(supplement.getId());
                setFavoriteMessage(valueOrDefault(supplement.getName()) + " removed from favorites.", false);
            } else {
                serviceSupplementFavorite.addFavorite(currentUserEmail, supplement.getId());
                favoriteSupplementIds.add(supplement.getId());
                setFavoriteMessage(valueOrDefault(supplement.getName()) + " added to favorites.", false);
            }
            applyFilters();
            renderFavorites();
        } catch (RuntimeException exception) {
            setFavoriteMessage(exception.getMessage(), true);
        }
    }

    private void addToCart(Supplement supplement) {
        try {
            cartStore.addSupplement(supplement);
            setCartMessage(valueOrDefault(supplement.getName()) + " added to cart.", false);
            showCartPanel();
            loadSuggestionsAsync(false);
        } catch (RuntimeException exception) {
            setCartMessage(exception.getMessage(), true);
            showCartPanel();
        }
    }

    private void openProductDetails(ActionEvent event, Supplement supplement) {
        if (supplement == null) {
            setCartMessage("Product details are unavailable right now.", true);
            return;
        }

        SelectedSupplementStore.getInstance().setSelectedSupplement(supplement);
        try {
            SceneNavigator.navigate(event, SceneNavigator.FRONT_END_VIEW, SceneNavigator.PRODUCT_DETAILS_VIEW);
        } catch (IOException exception) {
            setCartMessage("Could not open product details.", true);
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

    private void renderFavorites() {
        if (favoriteItemsContainer == null || favoritesCountLabel == null) {
            return;
        }

        favoriteItemsContainer.getChildren().clear();
        favoritesCountLabel.setText(Integer.toString(favoriteSupplementIds.size()));

        if (favoriteSupplementIds.isEmpty()) {
            favoriteItemsContainer.getChildren().add(buildEmptyFavoritesState(
                    "No favorites yet",
                    "Click the heart icon on any supplement to save it here."
            ));
            return;
        }

        if (allSupplements.isEmpty()) {
            favoriteItemsContainer.getChildren().add(buildEmptyFavoritesState(
                    "Favorites found",
                    "Favorites are saved, but products are not loaded yet."
            ));
            return;
        }

        List<Supplement> favorites = allSupplements.stream()
                .filter(supplement -> favoriteSupplementIds.contains(supplement.getId()))
                .sorted(Comparator.comparing(supplement -> valueOrDefault(supplement.getName()).toLowerCase(Locale.ROOT)))
                .toList();

        if (favorites.isEmpty()) {
            favoriteItemsContainer.getChildren().add(buildEmptyFavoritesState(
                    "Favorites unavailable",
                    "Some favorite products were removed from the catalog."
            ));
            return;
        }

        for (Supplement favorite : favorites) {
            favoriteItemsContainer.getChildren().add(buildFavoriteItemRow(favorite));
        }
    }

    private void loadSuggestionsAsync(boolean manualRefresh) {
        if (suggestionsGrid == null || suggestionsStatusLabel == null || suggestionsCountLabel == null) {
            return;
        }
        if (serviceSupplementRecommendation == null) {
            currentSuggestions = List.of();
            renderSuggestions();
            setSuggestionMessage("Recommendation service is unavailable.", true);
            return;
        }

        String suggestionEmail = resolveSuggestionEmail();
        if (suggestionEmail == null || suggestionEmail.isBlank()) {
            currentSuggestions = List.of();
            renderSuggestions();
            setSuggestionMessage("Place an order first to generate personalized suggestions.", true);
            return;
        }

        Set<Integer> excludedIds = cartStore.getItems().stream()
                .map(item -> item.getSupplement().getId())
                .collect(Collectors.toSet());

        setSuggestionMessage(
                manualRefresh ? "Refreshing AI suggestions..." : "Loading AI suggestions from your orders...",
                false
        );

        CompletableFuture.supplyAsync(() ->
                        serviceSupplementRecommendation.recommendForEmail(suggestionEmail, excludedIds, 6))
                .whenComplete((suggestions, throwable) -> Platform.runLater(() -> {
                    if (throwable != null) {
                        currentSuggestions = List.of();
                        renderSuggestions();
                        setSuggestionMessage("AI suggestion request failed. Showing no suggestions.", true);
                        return;
                    }

                    currentSuggestions = suggestions == null ? List.of() : suggestions;
                    renderSuggestions();

                    if (currentSuggestions.isEmpty()) {
                        setSuggestionMessage("No suggestions yet. Add more order history to improve recommendations.", false);
                    } else {
                        setSuggestionMessage(currentSuggestions.size() + " suggestion(s) ready for you.", false);
                    }
                }));
    }

    private void renderSuggestions() {
        if (suggestionsGrid == null || suggestionsCountLabel == null) {
            return;
        }

        suggestionsGrid.getChildren().clear();
        suggestionsCountLabel.setText(Integer.toString(currentSuggestions.size()));

        if (currentSuggestions.isEmpty()) {
            suggestionsGrid.getChildren().add(buildSuggestionEmptyCard(
                    "No suggestions available",
                    "Place a few orders and refresh to get personalized AI recommendations."
            ));
            return;
        }

        Map<Integer, Supplement> supplementById = allSupplements.stream()
                .collect(Collectors.toMap(Supplement::getId, supplement -> supplement, (left, right) -> left));

        int rendered = 0;
        for (SupplementRecommendation suggestion : currentSuggestions) {
            Supplement supplement = supplementById.get(suggestion.getSupplementId());
            if (supplement == null) {
                continue;
            }
            suggestionsGrid.getChildren().add(buildSuggestionCard(supplement, suggestion));
            rendered++;
        }

        suggestionsCountLabel.setText(Integer.toString(rendered));
        if (rendered == 0) {
            suggestionsGrid.getChildren().add(buildSuggestionEmptyCard(
                    "Suggestions unavailable",
                    "Recommended products are currently missing from catalog or out of stock."
            ));
        }
    }

    private VBox buildSuggestionCard(Supplement supplement, SupplementRecommendation suggestion) {
        VBox card = new VBox(8.0);
        card.setPrefWidth(300.0);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 16; -fx-border-color: #D9E8E2; "
                + "-fx-border-radius: 16; -fx-padding: 12 12 12 12;");

        Label nameLabel = new Label(valueOrDefault(supplement.getName()));
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-text-fill: #113748; -fx-font-size: 15px; -fx-font-weight: 900;");

        Label metaLabel = new Label(valueOrDefault(supplement.getBrand()) + " | " + valueOrDefault(supplement.getCategory()));
        metaLabel.setStyle("-fx-text-fill: #6F8290; -fx-font-size: 12px; -fx-font-weight: 700;");

        Label reasonLabel = new Label("AI: " + valueOrDefault(suggestion.getReason()));
        reasonLabel.setWrapText(true);
        reasonLabel.setStyle("-fx-background-color: #EFF8F4; -fx-background-radius: 10; -fx-text-fill: #2F6F60; "
                + "-fx-font-size: 12px; -fx-font-weight: 700; -fx-padding: 6 8 6 8;");

        HBox footerRow = new HBox(8.0);
        footerRow.setAlignment(Pos.CENTER_LEFT);

        Label priceLabel = new Label(formatPrice(supplement.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #0F6A58; -fx-font-size: 14px; -fx-font-weight: 900;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewButton = new Button("View");
        viewButton.setPrefHeight(30.0);
        viewButton.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #D4E2EA; "
                + "-fx-border-radius: 10; -fx-text-fill: #496170; -fx-font-size: 12px; -fx-font-weight: 700;");
        viewButton.setOnAction(event -> openProductDetails(event, supplement));

        Button addButton = new Button("Add");
        addButton.setDisable(supplement.getStock() <= 0);
        addButton.setPrefHeight(30.0);
        addButton.setStyle("-fx-background-color: linear-gradient(to right, #124A4D, #0F6A58); -fx-background-radius: 10; "
                + "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: 800;");
        addButton.setOnAction(event -> addToCart(supplement));

        footerRow.getChildren().addAll(priceLabel, spacer, viewButton, addButton);
        card.getChildren().addAll(nameLabel, metaLabel, reasonLabel, footerRow);
        return card;
    }

    private VBox buildSuggestionEmptyCard(String title, String hint) {
        VBox card = new VBox(6.0);
        card.setPrefWidth(930.0);
        card.setStyle("-fx-background-color: #F6FAFC; -fx-background-radius: 14; -fx-border-color: #D9E6ED; "
                + "-fx-border-radius: 14; -fx-padding: 12 14 12 14;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #113748; -fx-font-size: 15px; -fx-font-weight: 900;");

        Label hintLabel = new Label(hint);
        hintLabel.setWrapText(true);
        hintLabel.setStyle("-fx-text-fill: #768995; -fx-font-size: 12px; -fx-font-weight: 600;");

        card.getChildren().addAll(titleLabel, hintLabel);
        return card;
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
            loadSuggestionsAsync(false);
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
            loadSuggestionsAsync(false);
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
            loadSuggestionsAsync(false);
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

    private HBox buildFavoriteItemRow(Supplement supplement) {
        HBox row = new HBox(10.0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #F6FAFC; -fx-background-radius: 14; -fx-border-color: #D9E6ED; "
                + "-fx-border-radius: 14; -fx-padding: 10 12 10 12;");

        VBox details = new VBox(2.0);
        Label nameLabel = new Label(valueOrDefault(supplement.getName()));
        nameLabel.setStyle("-fx-text-fill: #113748; -fx-font-size: 14px; -fx-font-weight: 800;");

        Label metaLabel = new Label(valueOrDefault(supplement.getBrand()) + " | " + valueOrDefault(supplement.getCategory()));
        metaLabel.setStyle("-fx-text-fill: #6F8290; -fx-font-size: 12px; -fx-font-weight: 600;");
        details.getChildren().addAll(nameLabel, metaLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label priceLabel = new Label(formatPrice(supplement.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #0F6A58; -fx-font-size: 13px; -fx-font-weight: 800;");

        Button detailsButton = new Button("View");
        detailsButton.setPrefHeight(30.0);
        detailsButton.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #D4E2EA; "
                + "-fx-border-radius: 10; -fx-text-fill: #496170; -fx-font-size: 12px; -fx-font-weight: 700;");
        detailsButton.setOnAction(event -> openProductDetails(event, supplement));

        Button removeButton = new Button("Remove");
        removeButton.setPrefHeight(30.0);
        removeButton.setPrefWidth(34.0);
        removeButton.setStyle("-fx-background-color: #FFECEE; -fx-background-radius: 999; -fx-border-color: #F5B9C2; "
                + "-fx-border-radius: 999; -fx-text-fill: #D92D20; -fx-font-size: 13px; -fx-font-weight: 900;");
        removeButton.setOnAction(event -> toggleFavorite(supplement));

        row.getChildren().addAll(details, spacer, priceLabel, detailsButton, removeButton);
        return row;
    }

    private VBox buildEmptyFavoritesState(String title, String hint) {
        VBox emptyState = new VBox(6.0);
        emptyState.setStyle("-fx-background-color: #F6FAFC; -fx-background-radius: 14; -fx-border-color: #D9E6ED; "
                + "-fx-border-radius: 14; -fx-padding: 14 14 14 14;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #113748; -fx-font-size: 15px; -fx-font-weight: 900;");

        Label hintLabel = new Label(hint);
        hintLabel.setWrapText(true);
        hintLabel.setStyle("-fx-text-fill: #768995; -fx-font-size: 12px; -fx-font-weight: 600;");

        emptyState.getChildren().addAll(titleLabel, hintLabel);
        return emptyState;
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

    private String resolveFavoriteEmail() {
        String sessionEmail = AppSession.getInstance().getEmail();
        if (sessionEmail != null && !sessionEmail.isBlank()) {
            return sessionEmail.trim();
        }

        String checkoutEmail = cartStore.getLastCheckoutEmail();
        if (checkoutEmail != null && !checkoutEmail.isBlank()) {
            return checkoutEmail.trim();
        }
        return null;
    }

    private String resolveSuggestionEmail() {
        String checkoutEmail = cartStore.getLastCheckoutEmail();
        if (checkoutEmail != null && !checkoutEmail.isBlank()) {
            return checkoutEmail.trim();
        }

        String sessionEmail = AppSession.getInstance().getEmail();
        if (sessionEmail != null && !sessionEmail.isBlank()) {
            return sessionEmail.trim();
        }
        return null;
    }

    private void setFavoriteMessage(String message, boolean error) {
        if (favoritesStatusLabel == null) {
            return;
        }
        favoritesStatusLabel.setText(message);
        favoritesStatusLabel.setStyle(error ? FAVORITE_ERROR_STYLE : FAVORITE_INFO_STYLE);
    }

    private void setSuggestionMessage(String message, boolean error) {
        if (suggestionsStatusLabel == null) {
            return;
        }
        suggestionsStatusLabel.setText(message);
        suggestionsStatusLabel.setStyle(error ? SUGGESTION_ERROR_STYLE : SUGGESTION_INFO_STYLE);
    }

    private void setCartMessage(String message, boolean error) {
        cartStatusLabel.setText(message);
        cartStatusLabel.setStyle(error ? CART_ERROR_STYLE : CART_INFO_STYLE);
    }
}
