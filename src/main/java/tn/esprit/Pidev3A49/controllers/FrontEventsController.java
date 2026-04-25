package tn.esprit.Pidev3A49.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
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
import tn.esprit.Pidev3A49.models.LoyaltyStatus;
import tn.esprit.Pidev3A49.models.Participation;
import tn.esprit.Pidev3A49.models.RecommendationResult;
import tn.esprit.Pidev3A49.service.EventService;
import tn.esprit.Pidev3A49.service.FavoriteService;
import tn.esprit.Pidev3A49.service.LoyaltyService;
import tn.esprit.Pidev3A49.service.ParticipationService;
import tn.esprit.Pidev3A49.service.RecommendationService;
import tn.esprit.Pidev3A49.service.ReservationService;
import tn.esprit.Pidev3A49.service.ReviewService;
import tn.esprit.Pidev3A49.service.WaitlistService;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class FrontEventsController implements Initializable {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);

    @FXML private VBox upcomingEventCard;
    @FXML private Label upcomingTitleLabel;
    @FXML private Label upcomingDateLabel;
    @FXML private Label upcomingLocationLabel;
    @FXML private Label upcomingParticipantsLabel;
    @FXML private Label upcomingPriceLabel;
    @FXML private Label upcomingDescriptionLabel;

    @FXML private Label resultCountLabel;
    @FXML private Label heroTotalEventsLabel;
    @FXML private Label heroOpenEventsLabel;
    @FXML private Label heroFavoriteEventsLabel;
    @FXML private Label favoriteCountLabel;
    @FXML private Label recommendationCountLabel;
    @FXML private Label loyaltyTierLabel;
    @FXML private Label loyaltyReservationsLabel;
    @FXML private Label loyaltySpentLabel;
    @FXML private Label loyaltyProgressLabel;
    @FXML private Label loyaltyAccessLabel;
    @FXML private Label loyaltyBenefitsLabel;
    @FXML private Label loyaltyScoreLabel;
    @FXML private Label loyaltyIdentityLabel;

    @FXML private TextField currentUserEmailField;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private DatePicker dateFilter;
    @FXML private ComboBox<String> priceFilter;
    @FXML private ComboBox<String> sortFilter;
    @FXML private ProgressBar loyaltyProgressBar;

    @FXML private FlowPane cardsContainer;
    @FXML private FlowPane recommendationCardsContainer;
    @FXML private FlowPane favoriteCardsContainer;
    @FXML private StackPane recommendationsEmptyStateBox;
    @FXML private StackPane favoritesEmptyStateBox;
    @FXML private StackPane emptyStateBox;
    @FXML private VBox eventsSectionAnchor;
    @FXML private VBox favoritesSectionAnchor;

    private final EventService eventService = new EventService();
    private final ParticipationService participationService = new ParticipationService();
    private final FavoriteService favoriteService = new FavoriteService();
    private final ReviewService reviewService = new ReviewService();
    private final RecommendationService recommendationService = new RecommendationService();
    private final LoyaltyService loyaltyService = new LoyaltyService();
    private final ReservationService reservationService = new ReservationService();
    private final WaitlistService waitlistService = new WaitlistService();

    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> currentFilteredEvents = new ArrayList<>();
    private final List<RecommendationResult> recommendationResults = new ArrayList<>();
    private final Map<Integer, Long> participationCountByEvent = new HashMap<>();
    private final Map<Integer, Long> validReservationCountByEvent = new HashMap<>();
    private final Map<Integer, Integer> favoriteCountByEvent = new HashMap<>();
    private final Map<Integer, Double> averageRatingByEvent = new HashMap<>();
    private final Map<Integer, Integer> reviewCountByEvent = new HashMap<>();
    private final Map<Integer, Integer> waitlistCountByEvent = new HashMap<>();
    private final Set<Integer> favoriteEventIds = new HashSet<>();
    private final Set<Integer> waitlistedEventIds = new HashSet<>();
    private boolean favoritesOnlyMode = false;
    private LoyaltyStatus currentLoyaltyStatus;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureFilters();
        currentUserEmailField.setText("client@fitopia.tn");
        currentUserEmailField.textProperty().addListener((observable, oldValue, newValue) -> {
            refreshFavoriteData();
            refreshReviewData();
            refreshWaitlistData();
            refreshLoyaltyStatus();
            refreshRecommendations();
            applyFilters();
        });
        loadData();
    }

    @FXML
    private void resetFilters() {
        favoritesOnlyMode = false;
        searchField.clear();
        categoryFilter.getSelectionModel().selectFirst();
        dateFilter.setValue(null);
        priceFilter.getSelectionModel().selectFirst();
        sortFilter.getSelectionModel().selectFirst();
        applyFilters();
    }

    @FXML
    private void showAllEvents() {
        favoritesOnlyMode = false;
        applyFilters();
    }

    @FXML
    private void showFavoriteEvents() {
        favoritesOnlyMode = true;
        applyFilters();
        Platform.runLater(() -> {
            if (favoritesSectionAnchor != null) {
                favoritesSectionAnchor.requestFocus();
            }
        });
    }

    @FXML
    private void scrollToEvents() {
        Platform.runLater(() -> {
            if (eventsSectionAnchor != null) {
                eventsSectionAnchor.requestFocus();
            } else {
                cardsContainer.requestFocus();
            }
        });
    }

    @FXML
    private void focusUpcomingCard() {
        Platform.runLater(() -> {
            Event nextEvent = getNextUpcomingEvent(currentFilteredEvents);
            if (nextEvent != null) {
                openReservationWindow(nextEvent);
            } else if (upcomingEventCard != null) {
                upcomingEventCard.requestFocus();
            }
        });
    }

    @FXML
    private void openReservationHistoryView() {
        try {
            String email = requireCurrentUserEmail();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReservationHistoryView.fxml"));
            Parent root = loader.load();

            ReservationHistoryController controller = loader.getController();
            controller.loadHistory(email);

            Scene scene = new Scene(root, 1200, 760);
            scene.getStylesheets().add(getClass().getResource("/styles/front-events.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Fitopia - Mes reservations");
            stage.setScene(scene);
            stage.setMinWidth(1020);
            stage.setMinHeight(700);
            stage.setOnHidden(windowEvent -> loadData());
            stage.show();
        } catch (IllegalArgumentException e) {
            showInfo("Mes reservations", e.getMessage());
        } catch (Exception e) {
            showInfo("Mes reservations", "La vue historique n'a pas pu etre ouverte.");
            e.printStackTrace();
        }
    }

    private void configureFilters() {
        categoryFilter.getItems().add("All categories");
        categoryFilter.getSelectionModel().selectFirst();

        priceFilter.getItems().addAll(
                "All prices",
                "Free",
                "Under 50",
                "50 to 100",
                "100+"
        );
        priceFilter.getSelectionModel().selectFirst();

        sortFilter.getItems().addAll(
                "Sort: Date (soonest)",
                "Newest",
                "Price low to high",
                "Price high to low"
        );
        sortFilter.getSelectionModel().selectFirst();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        categoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        dateFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        priceFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        sortFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void loadData() {
        try {
            allEvents.clear();
            allEvents.addAll(eventService.getAll());
            List<Participation> participations = participationService.getAll();
            rebuildParticipationMap(participations);
            refreshReservationCapacityData();
            rebuildCategoryFilter();
            refreshFavoriteData();
            refreshReviewData();
            refreshWaitlistData();
            refreshLoyaltyStatus();
            refreshRecommendations();
            applyFilters();
        } catch (Exception e) {
            showInfo("Loading error", "Unable to load front events from the existing backend.");
            e.printStackTrace();
        }
    }

    private void rebuildParticipationMap(List<Participation> participations) {
        participationCountByEvent.clear();
        for (Participation participation : participations) {
            participationCountByEvent.merge(participation.getIdEvent(), 1L, Long::sum);
        }
    }

    private void rebuildCategoryFilter() {
        String selected = categoryFilter.getValue();

        List<String> categories = allEvents.stream()
                .map(Event::getTypeEvent)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());

        categoryFilter.getItems().setAll("All categories");
        categoryFilter.getItems().addAll(categories);

        if (selected != null && categoryFilter.getItems().contains(selected)) {
            categoryFilter.setValue(selected);
        } else {
            categoryFilter.getSelectionModel().selectFirst();
        }
    }

    private void refreshFavoriteData() {
        try {
            favoriteCountByEvent.clear();
            favoriteCountByEvent.putAll(favoriteService.countFavoritesByEvent());

            favoriteEventIds.clear();
            String currentEmail = getCurrentUserEmailOrNull();
            if (currentEmail != null) {
                favoriteEventIds.addAll(favoriteService.getFavoriteEventIdsByEmail(currentEmail));
                favoriteCountLabel.setText(favoriteService.countFavoritesByEmail(currentEmail) + " favoris");
            } else {
                favoriteCountLabel.setText("0 favoris");
            }
        } catch (Exception e) {
            favoriteCountByEvent.clear();
            favoriteEventIds.clear();
            favoriteCountLabel.setText("0 favoris");
            System.err.println("Impossible de charger les favoris.");
            e.printStackTrace();
        }

        if (!currentFilteredEvents.isEmpty()) {
            renderCards(currentFilteredEvents);
            renderFavoriteSection();
            updateHeroStats(currentFilteredEvents);
        }
    }

    private void refreshReviewData() {
        try {
            averageRatingByEvent.clear();
            averageRatingByEvent.putAll(reviewService.getAverageNotesByEvent());

            reviewCountByEvent.clear();
            reviewCountByEvent.putAll(reviewService.getReviewCountsByEvent());
        } catch (Exception e) {
            averageRatingByEvent.clear();
            reviewCountByEvent.clear();
            System.err.println("Impossible de charger les avis.");
            e.printStackTrace();
        }
    }

    private void refreshReservationCapacityData() {
        try {
            validReservationCountByEvent.clear();
            validReservationCountByEvent.putAll(reservationService.countValidReservationsByEvent());
        } catch (Exception e) {
            validReservationCountByEvent.clear();
            System.err.println("Impossible de charger les capacites depuis les reservations.");
            e.printStackTrace();
        }
    }

    private void refreshWaitlistData() {
        try {
            waitlistCountByEvent.clear();
            waitlistCountByEvent.putAll(waitlistService.countActiveByEvent());

            waitlistedEventIds.clear();
            String currentEmail = getCurrentUserEmailOrNull();
            if (currentEmail != null) {
                waitlistedEventIds.addAll(waitlistService.getActiveWaitlistEventIdsByEmail(currentEmail));
            }
        } catch (Exception e) {
            waitlistCountByEvent.clear();
            waitlistedEventIds.clear();
            System.err.println("Impossible de charger la liste d'attente.");
            e.printStackTrace();
        }
    }

    private void refreshRecommendations() {
        recommendationResults.clear();

        String currentEmail = getCurrentUserEmailOrNull();
        if (currentEmail == null) {
            recommendationCountLabel.setText("0 recommandations");
            if (recommendationCardsContainer != null) {
                recommendationCardsContainer.getChildren().clear();
            }
            if (recommendationsEmptyStateBox != null) {
                recommendationsEmptyStateBox.setVisible(true);
                recommendationsEmptyStateBox.setManaged(true);
            }
            return;
        }

        Set<Integer> completeEventIds = allEvents.stream()
                .filter(event -> getRemainingPlaces(event) <= 0)
                .map(Event::getIdEvent)
                .collect(Collectors.toSet());

        try {
            boolean isVip = currentLoyaltyStatus != null && currentLoyaltyStatus.hasVipAccess();
            recommendationResults.addAll(recommendationService.recommendEvents(
                    currentEmail,
                    isVip,
                    allEvents,
                    favoriteEventIds,
                    completeEventIds,
                    4
            ));
        } catch (Exception e) {
            System.err.println("Impossible de calculer les recommandations.");
            e.printStackTrace();
        }

        renderRecommendations();
    }

    private void refreshLoyaltyStatus() {
        String currentEmail = getCurrentUserEmailOrNull();
        if (currentEmail == null) {
            currentLoyaltyStatus = null;
            renderLoyaltyStatus(null);
            return;
        }

        try {
            currentLoyaltyStatus = loyaltyService.getStatusByEmail(currentEmail);
        } catch (Exception e) {
            currentLoyaltyStatus = null;
            System.err.println("Impossible de charger le statut fidelite.");
            e.printStackTrace();
        }
        renderLoyaltyStatus(currentLoyaltyStatus);
    }

    private void renderLoyaltyStatus(LoyaltyStatus status) {
        if (loyaltyTierLabel == null) {
            return;
        }

        if (status == null) {
            loyaltyIdentityLabel.setText("Aucun client connecte");
            loyaltyTierLabel.setText("Bronze");
            loyaltyReservationsLabel.setText("0 reservations confirmees");
            loyaltySpentLabel.setText("0.00 DT depenses");
            loyaltyProgressLabel.setText("Saisis un email client valide pour calculer la fidelite.");
            loyaltyAccessLabel.setText("Acces VIP inactif");
            loyaltyBenefitsLabel.setText("Priorite sur les evenements premium, recommendations VIP, avantages exclusifs.");
            loyaltyScoreLabel.setText("Score 0");
            loyaltyProgressBar.setProgress(0);
            return;
        }

        loyaltyIdentityLabel.setText(status.getNomParticipant() == null || status.getNomParticipant().isBlank()
                ? status.getEmailParticipant()
                : status.getNomParticipant() + " • " + status.getEmailParticipant());
        loyaltyTierLabel.setText(status.getTier());
        loyaltyReservationsLabel.setText(status.getValidReservations()
                + (status.getValidReservations() == 1 ? " reservation confirmee/utilisee" : " reservations confirmees/utilisees"));
        loyaltySpentLabel.setText(String.format(Locale.US, "%.2f DT depenses", status.getTotalSpent()));
        loyaltyProgressLabel.setText(status.getProgressText());
        loyaltyAccessLabel.setText(status.hasVipAccess() ? "Acces VIP actif" : "Acces VIP verrouille");
        loyaltyBenefitsLabel.setText(status.hasVipAccess()
                ? "Avantages actifs: recommandations premium, priorite sur les experiences VIP et statut prioritaire."
                : "Encore " + status.getReservationsToVip() + " reservation(s) valide(s) pour debloquer le statut VIP.");
        loyaltyScoreLabel.setText("Score " + status.getScore());
        loyaltyProgressBar.setProgress(status.getProgressRatio());
    }

    private void applyFilters() {
        List<Event> filtered = allEvents.stream()
                .filter(this::matchesSearch)
                .filter(this::matchesCategory)
                .filter(this::matchesDate)
                .filter(this::matchesPrice)
                .filter(event -> !favoritesOnlyMode || favoriteEventIds.contains(event.getIdEvent()))
                .sorted(getComparator())
                .collect(Collectors.toList());

        currentFilteredEvents.clear();
        currentFilteredEvents.addAll(filtered);
        renderUpcomingEvent(filtered);
        renderCards(filtered);
        renderRecommendations();
        renderFavoriteSection();
        updateHeroStats(filtered);

        resultCountLabel.setText(filtered.size() + (filtered.size() == 1 ? " evenement" : " evenements")
                + (favoritesOnlyMode ? " en favoris" : ""));
        emptyStateBox.setVisible(filtered.isEmpty());
        emptyStateBox.setManaged(filtered.isEmpty());
    }

    private boolean matchesSearch(Event event) {
        String rawSearch = searchField.getText();
        if (rawSearch == null || rawSearch.isBlank()) {
            return true;
        }

        String search = rawSearch.toLowerCase(Locale.ROOT).trim();
        return contains(event.getTitre(), search)
                || contains(event.getDescription(), search)
                || contains(event.getLieu(), search)
                || contains(event.getTypeEvent(), search);
    }

    private boolean matchesCategory(Event event) {
        String selected = categoryFilter.getValue();
        return selected == null
                || "All categories".equals(selected)
                || selected.equalsIgnoreCase(nullSafe(event.getTypeEvent()));
    }

    private boolean matchesDate(Event event) {
        LocalDate selectedDate = dateFilter.getValue();
        return selectedDate == null || selectedDate.equals(event.getDateEvent());
    }

    private boolean matchesPrice(Event event) {
        String selected = priceFilter.getValue();
        double price = event.getPrixEvent();

        if (selected == null || "All prices".equals(selected)) {
            return true;
        }

        return switch (selected) {
            case "Free" -> price <= 0.0;
            case "Under 50" -> price > 0.0 && price < 50.0;
            case "50 to 100" -> price >= 50.0 && price <= 100.0;
            case "100+" -> price > 100.0;
            default -> true;
        };
    }

    private Comparator<Event> getComparator() {
        String selected = sortFilter.getValue();

        if ("Newest".equals(selected)) {
            return Comparator.comparing(Event::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
        }
        if ("Price low to high".equals(selected)) {
            return Comparator.comparingDouble(Event::getPrixEvent);
        }
        if ("Price high to low".equals(selected)) {
            return Comparator.comparingDouble(Event::getPrixEvent).reversed();
        }
        return Comparator.comparing(Event::getDateEvent, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Event::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private void renderUpcomingEvent(List<Event> filtered) {
        Event next = getNextUpcomingEvent(filtered);

        if (next == null) {
            upcomingTitleLabel.setText("Aucun evenement disponible");
            upcomingDateLabel.setText("Date: -");
            upcomingLocationLabel.setText("Lieu: -");
            upcomingParticipantsLabel.setText("Participants: -");
            upcomingPriceLabel.setText("Prix: -");
            upcomingDescriptionLabel.setText("Elargis les filtres pour afficher le prochain evenement disponible.");
            return;
        }

        upcomingTitleLabel.setText(nullSafe(next.getTitre()));
        upcomingDateLabel.setText("Date: " + formatDate(next.getDateEvent()));
        upcomingLocationLabel.setText("Lieu: " + nullSafe(next.getLieu()));
        upcomingParticipantsLabel.setText("Participants: " + getParticipantCount(next) + "/" + next.getCapacite());
        upcomingPriceLabel.setText("Prix: " + formatPrice(next.getPrixEvent()));
        upcomingDescriptionLabel.setText(trimDescription(next.getDescription(), 120));
    }

    private void updateHeroStats(List<Event> filtered) {
        long openEvents = filtered.stream()
                .filter(event -> getRemainingPlaces(event) > 0)
                .count();

        heroTotalEventsLabel.setText(filtered.size() + (filtered.size() == 1 ? " evenement" : " evenements"));
        heroOpenEventsLabel.setText(openEvents + (openEvents == 1 ? " ouvert" : " ouverts"));
        heroFavoriteEventsLabel.setText(favoriteEventIds.size() + (favoriteEventIds.size() == 1 ? " favori" : " favoris"));
    }

    private void renderCards(List<Event> events) {
        cardsContainer.getChildren().clear();
        for (Event event : events) {
            cardsContainer.getChildren().add(buildEventCard(event));
        }
    }

    private void renderRecommendations() {
        if (recommendationCardsContainer == null || recommendationsEmptyStateBox == null || recommendationCountLabel == null) {
            return;
        }

        recommendationCardsContainer.getChildren().clear();
        for (RecommendationResult result : recommendationResults) {
            recommendationCardsContainer.getChildren().add(buildRecommendationCard(result));
        }

        recommendationCountLabel.setText(recommendationResults.size() + (recommendationResults.size() == 1 ? " recommandation" : " recommandations"));
        boolean empty = recommendationResults.isEmpty();
        recommendationsEmptyStateBox.setVisible(empty);
        recommendationsEmptyStateBox.setManaged(empty);
    }

    private VBox buildRecommendationCard(RecommendationResult result) {
        Event event = result.getEvent();
        VBox card = new VBox(8);
        card.getStyleClass().addAll("front-event-card", "recommended-card", "recommended-card-compact");
        card.setPrefWidth(330);
        card.setMinWidth(330);

        HBox contentRow = new HBox(10);
        contentRow.setAlignment(Pos.CENTER_LEFT);

        StackPane thumbnail = new StackPane();
        thumbnail.getStyleClass().add("recommendation-thumbnail");
        thumbnail.getChildren().add(createRecommendationMedia(event));

        VBox infoColumn = new VBox(4);
        infoColumn.setAlignment(Pos.CENTER_LEFT);

        Label badge = new Label("Recommande");
        badge.getStyleClass().addAll("front-badge", "front-badge-recommended", "recommendation-badge-compact");

        Label title = new Label(nullSafe(event.getTitre()));
        title.getStyleClass().add("recommendation-title-compact");
        title.setWrapText(true);

        Label typeLabel = new Label(nullSafe(event.getTypeEvent()));
        typeLabel.getStyleClass().add("recommendation-meta-compact");

        Label dateLabel = new Label(formatDate(event.getDateEvent()));
        dateLabel.getStyleClass().add("recommendation-meta-compact");

        Button detailsButton = new Button("Voir");
        detailsButton.getStyleClass().add("recommendation-action-compact");
        detailsButton.setOnAction(actionEvent -> openEventDetailsWindow(event));

        infoColumn.getChildren().addAll(badge, title, typeLabel, dateLabel, detailsButton);
        contentRow.getChildren().addAll(thumbnail, infoColumn);
        card.getChildren().add(contentRow);

        return card;
    }

    private Node createRecommendationMedia(Event event) {
        String imageSource = event.getImageEvent();
        if (imageSource == null || imageSource.isBlank()) {
            return createRecommendationPlaceholder(event);
        }

        try {
            String normalizedSource = imageSource.trim();
            String url = normalizedSource.startsWith("http://")
                    || normalizedSource.startsWith("https://")
                    || normalizedSource.startsWith("file:/")
                    ? normalizedSource
                    : new File(normalizedSource).toURI().toString();

            Image image = new Image(url, true);
            if (image.isError()) {
                return createRecommendationPlaceholder(event);
            }

            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(102);
            imageView.setFitHeight(72);
            imageView.setPreserveRatio(false);
            return imageView;
        } catch (Exception ignored) {
            return createRecommendationPlaceholder(event);
        }
    }

    private StackPane createRecommendationPlaceholder(Event event) {
        StackPane placeholder = new StackPane();
        placeholder.getStyleClass().add("recommendation-placeholder");
        placeholder.setPrefSize(102, 72);

        Label title = new Label(nullSafe(event.getTypeEvent()));
        title.getStyleClass().add("recommendation-placeholder-text");
        title.setWrapText(true);
        placeholder.getChildren().add(title);
        return placeholder;
    }

    private void renderFavoriteSection() {
        if (favoriteCardsContainer == null || favoritesEmptyStateBox == null) {
            return;
        }

        favoriteCardsContainer.getChildren().clear();
        List<Event> favoriteEvents = allEvents.stream()
                .filter(event -> favoriteEventIds.contains(event.getIdEvent()))
                .sorted(Comparator.comparing(Event::getDateEvent, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        for (Event event : favoriteEvents) {
            favoriteCardsContainer.getChildren().add(buildEventCard(event));
        }

        boolean empty = favoriteEvents.isEmpty();
        favoritesEmptyStateBox.setVisible(empty);
        favoritesEmptyStateBox.setManaged(empty);
    }

    private VBox buildEventCard(Event event) {
        VBox card = new VBox(14);
        card.getStyleClass().add("front-event-card");
        card.setPrefWidth(680);
        card.setMinWidth(680);

        StackPane mediaPane = new StackPane();
        mediaPane.getStyleClass().add("front-card-media");
        mediaPane.getChildren().add(createCardMedia(event));

        FlowPane badgeBar = new FlowPane();
        badgeBar.getStyleClass().add("front-badge-bar");
        badgeBar.setHgap(8);
        badgeBar.setVgap(8);

        for (String badge : resolveBadges(event)) {
            badgeBar.getChildren().add(createBadge(badge));
        }

        StackPane.setAlignment(badgeBar, Pos.TOP_LEFT);
        StackPane.setMargin(badgeBar, new Insets(14, 14, 0, 14));
        mediaPane.getChildren().add(badgeBar);

        VBox content = new VBox(12);
        content.setPadding(new Insets(0, 18, 18, 18));

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(nullSafe(event.getTitre()));
        title.getStyleClass().add("front-card-title");
        title.setWrapText(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusChip = new Label(getRemainingPlaces(event) <= 0 ? "COMPLET" : "OUVERT");
        statusChip.getStyleClass().add(getRemainingPlaces(event) <= 0 ? "front-status-full" : "front-status-open");

        titleRow.getChildren().addAll(title, spacer, statusChip);

        HBox socialRow = new HBox(10);
        socialRow.setAlignment(Pos.CENTER_LEFT);
        socialRow.getChildren().addAll(
                createSoftBadge(buildRatingText(event)),
                createSoftBadge(getReviewCount(event) + (getReviewCount(event) == 1 ? " avis" : " avis")),
                createSoftBadge(getFavoriteCount(event) + (getFavoriteCount(event) == 1 ? " favori" : " favoris")),
                createSoftBadge(getWaitlistCount(event) + " en attente")
        );

        HBox infoRow = new HBox(10);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        infoRow.getChildren().addAll(
                createSoftBadge(formatDate(event.getDateEvent())),
                createSoftBadge(nullSafe(event.getLieu())),
                createSoftBadge(nullSafe(event.getTypeEvent())),
                createSoftBadge(getRemainingPlaces(event) + " places restantes")
        );

        Label description = new Label(trimDescription(event.getDescription(), 180));
        description.getStyleClass().add("front-card-description");
        description.setWrapText(true);

        HBox bottomRow = new HBox(14);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        VBox priceBox = new VBox(2);
        Label price = new Label(formatPrice(event.getPrixEvent()));
        price.getStyleClass().add("front-big-price");
        priceBox.getChildren().add(price);

        Button infoButton = new Button("INFORMATIONS");
        infoButton.getStyleClass().add("front-card-info-button");
        infoButton.setOnAction(actionEvent -> openEventDetailsWindow(event));

        Button favoriteButton = new Button(isFavorite(event) ? "RETIRER FAVORI" : "AJOUTER FAVORI");
        favoriteButton.getStyleClass().add(isFavorite(event) ? "front-card-favorite-button-active" : "front-card-favorite-button");
        favoriteButton.setOnAction(actionEvent -> toggleFavorite(event));

        Button reserveButton = new Button("RESERVER");
        reserveButton.getStyleClass().add("front-card-primary-button");
        boolean premiumLocked = event.isPremium() && !isCurrentUserVip();
        boolean eventFull = isEventFull(event);
        boolean alreadyWaitlisted = isWaitlisted(event);
        if (premiumLocked) {
            reserveButton.setText("RESERVE VIP");
            reserveButton.setDisable(true);
            reserveButton.setOnAction(actionEvent -> openReservationWindow(event));
        } else if (eventFull) {
            reserveButton.setText(alreadyWaitlisted ? "EN ATTENTE" : "LISTE D'ATTENTE");
            reserveButton.setDisable(alreadyWaitlisted);
            reserveButton.setOnAction(actionEvent -> joinWaitlist(event));
        } else {
            reserveButton.setText("RESERVER");
            reserveButton.setDisable(false);
            reserveButton.setOnAction(actionEvent -> openReservationWindow(event));
        }

        bottomRow.getChildren().addAll(priceBox, infoButton, favoriteButton, reserveButton);

        content.getChildren().addAll(titleRow, socialRow, infoRow, description, bottomRow);
        card.getChildren().addAll(mediaPane, content);
        return card;
    }

    private Node createCardMedia(Event event) {
        String imageSource = event.getImageEvent();
        if (imageSource == null || imageSource.isBlank()) {
            return createImagePlaceholder(event);
        }

        try {
            String normalizedSource = imageSource.trim();
            String url = normalizedSource.startsWith("http://")
                    || normalizedSource.startsWith("https://")
                    || normalizedSource.startsWith("file:/")
                    ? normalizedSource
                    : new File(normalizedSource).toURI().toString();

            Image image = new Image(url, true);
            if (image.isError()) {
                return createImagePlaceholder(event);
            }

            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(680);
            imageView.setFitHeight(340);
            imageView.setPreserveRatio(false);
            return imageView;
        } catch (Exception ignored) {
            return createImagePlaceholder(event);
        }
    }

    private StackPane createImagePlaceholder(Event event) {
        StackPane placeholder = new StackPane();
        placeholder.getStyleClass().add("front-card-placeholder");
        placeholder.setPrefSize(680, 340);

        VBox placeholderContent = new VBox(8);
        placeholderContent.setAlignment(Pos.CENTER_LEFT);
        placeholderContent.setPadding(new Insets(22));

        Label type = new Label(nullSafe(event.getTypeEvent()));
        type.getStyleClass().add("front-placeholder-kicker");

        Label title = new Label(nullSafe(event.getTitre()));
        title.getStyleClass().add("front-placeholder-title");
        title.setWrapText(true);

        placeholderContent.getChildren().addAll(type, title);
        placeholder.getChildren().add(placeholderContent);
        return placeholder;
    }

    private Label createBadge(String badge) {
        Label label = new Label(badge);
        label.getStyleClass().add("front-badge");

        switch (badge) {
            case "Complet" -> label.getStyleClass().add("front-badge-full");
            case "Tendance" -> label.getStyleClass().add("front-badge-trending");
            case "Nouveau" -> label.getStyleClass().add("front-badge-new");
            case "Premium" -> label.getStyleClass().add("front-badge-premium");
            case "En attente" -> label.getStyleClass().add("front-badge-waitlist");
            default -> label.getStyleClass().add("front-badge-default");
        }
        return label;
    }

    private Label createSoftBadge(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("front-soft-badge");
        return label;
    }

    private List<String> resolveBadges(Event event) {
        List<String> badges = new ArrayList<>();

        if (isRecent(event)) {
            badges.add("Nouveau");
        }
        if (event.isPremium()) {
            badges.add("Premium");
        }
        if (getRemainingPlaces(event) <= 0) {
            badges.add("Complet");
        }
        if (isWaitlisted(event)) {
            badges.add("En attente");
        } else if (getParticipantCount(event) >= Math.max(3, event.getCapacite() / 2)) {
            badges.add("Tendance");
        }

        return badges;
    }

    private boolean isRecent(Event event) {
        LocalDateTime createdAt = event.getCreatedAt();
        return createdAt != null && createdAt.isAfter(LocalDateTime.now().minusDays(21));
    }

    private int getParticipantCount(Event event) {
        return validReservationCountByEvent
                .getOrDefault(event.getIdEvent(), participationCountByEvent.getOrDefault(event.getIdEvent(), 0L))
                .intValue();
    }

    private int getRemainingPlaces(Event event) {
        return Math.max(0, event.getCapacite() - getParticipantCount(event));
    }

    private boolean isEventFull(Event event) {
        return getRemainingPlaces(event) <= 0;
    }

    private int getFavoriteCount(Event event) {
        return favoriteCountByEvent.getOrDefault(event.getIdEvent(), 0);
    }

    private int getReviewCount(Event event) {
        return reviewCountByEvent.getOrDefault(event.getIdEvent(), 0);
    }

    private boolean isFavorite(Event event) {
        return favoriteEventIds.contains(event.getIdEvent());
    }

    private boolean isWaitlisted(Event event) {
        return waitlistedEventIds.contains(event.getIdEvent());
    }

    private int getWaitlistCount(Event event) {
        return waitlistCountByEvent.getOrDefault(event.getIdEvent(), 0);
    }

    private boolean isCurrentUserVip() {
        return currentLoyaltyStatus != null && currentLoyaltyStatus.hasVipAccess();
    }

    private void joinWaitlist(Event event) {
        try {
            String currentEmail = requireCurrentUserEmail();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/WaitlistSignupView.fxml"));
            Parent root = loader.load();

            WaitlistSignupController controller = loader.getController();
            controller.setContext(event, currentEmail, () -> {
                refreshWaitlistData();
                refreshRecommendations();
                applyFilters();
            });

            Scene scene = new Scene(root, 560, 560);
            scene.getStylesheets().add(getClass().getResource("/styles/waitlist-signup.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Fitopia - Liste d'attente");
            stage.setScene(scene);
            stage.setMinWidth(520);
            stage.setMinHeight(520);
            stage.setOnHidden(windowEvent -> {
                refreshWaitlistData();
                applyFilters();
            });
            stage.show();
        } catch (IllegalArgumentException e) {
            System.out.println("[FrontEventsController] joinWaitlist refus = " + e.getMessage());
            showInfo("Liste d'attente", e.getMessage());
            refreshWaitlistData();
            applyFilters();
        } catch (Exception e) {
            String message = e.getMessage() == null ? "Erreur inconnue" : e.getMessage();
            System.err.println("[FrontEventsController] joinWaitlist erreur technique = " + message);
            e.printStackTrace();
            showInfo("Liste d'attente", "Impossible de rejoindre la liste d'attente : " + message);
        }
    }

    private String buildWaitlistName(String email) {
        if (email == null || !email.contains("@")) {
            return "Client Fitopia";
        }
        return email.substring(0, email.indexOf('@'));
    }

    private void toggleFavorite(Event event) {
        try {
            String currentEmail = requireCurrentUserEmail();
            if (favoriteService.isFavorite(event.getIdEvent(), currentEmail)) {
                favoriteService.removeFavorite(event.getIdEvent(), currentEmail);
            } else {
                favoriteService.addFavorite(event.getIdEvent(), currentEmail);
            }
            refreshFavoriteData();
            applyFilters();
        } catch (IllegalArgumentException e) {
            showInfo("Favoris", e.getMessage());
        } catch (Exception e) {
            showInfo("Favoris", "Impossible de mettre a jour les favoris pour cet utilisateur.");
            e.printStackTrace();
        }
    }

    private void openEventDetailsWindow(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
            Parent root = loader.load();

            EventDetailsController controller = loader.getController();
            controller.setContext(event, getCurrentUserEmailOrNull(), this::reloadFrontMetaData);

            Scene scene = new Scene(root, 1120, 760);
            scene.getStylesheets().add(getClass().getResource("/styles/event-details.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Fitopia - Avis evenement");
            stage.setScene(scene);
            stage.setMinWidth(980);
            stage.setMinHeight(700);
            stage.setOnHidden(windowEvent -> reloadFrontMetaData());
            stage.show();
        } catch (Exception e) {
            showInfo("Evenement", "La fiche evenement n'a pas pu etre ouverte.");
            e.printStackTrace();
        }
    }

    private void reloadFrontMetaData() {
        refreshFavoriteData();
        refreshReviewData();
        refreshLoyaltyStatus();
        refreshRecommendations();
        applyFilters();
    }

    private void openReservationWindow(Event event) {
        if (event.isPremium() && !isCurrentUserVip()) {
            showInfo("Reserve aux VIP", "Reserve aux clients VIP. Continue tes reservations pour debloquer l'acces VIP.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReservationView.fxml"));
            Parent root = loader.load();

            ReservationController controller = loader.getController();
            controller.setContext(
                    event,
                    getRemainingPlaces(event),
                    getCurrentUserEmailOrNull(),
                    savedEmail -> currentUserEmailField.setText(savedEmail)
            );

            Scene scene = new Scene(root, 1180, 760);
            scene.getStylesheets().add(getClass().getResource("/styles/reservation-view.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Fitopia - Reservation");
            stage.setScene(scene);
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.setOnHidden(windowEvent -> loadData());
            stage.show();
        } catch (Exception e) {
            showInfo("Opening error", "The reservation view could not be opened.");
            e.printStackTrace();
        }
    }

    private Event getNextUpcomingEvent(List<Event> events) {
        return events.stream()
                .filter(event -> event.getDateEvent() != null)
                .min(Comparator.comparing(Event::getDateEvent))
                .orElse(events.isEmpty() ? null : events.get(0));
    }

    private boolean contains(String source, String search) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(search);
    }

    private String formatDate(LocalDate date) {
        return date == null ? "Date TBD" : DATE_FORMATTER.format(date);
    }

    private String formatPrice(double price) {
        return price <= 0 ? "Free" : String.format(Locale.US, "%.2f DT", price);
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }

    private String trimDescription(String description, int maxLength) {
        if (description == null || description.isBlank()) {
            return "Un evenement wellness pense pour le mouvement, la communaute et une reservation rapide.";
        }
        String trimmed = description.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength - 3) + "...";
    }

    private String buildRatingText(Event event) {
        double average = averageRatingByEvent.getOrDefault(event.getIdEvent(), 0.0);
        return String.format(Locale.US, "%.1f/5 note", average);
    }

    private String requireCurrentUserEmail() {
        String email = getCurrentUserEmailOrNull();
        if (email == null) {
            throw new IllegalArgumentException("Saisis un email utilisateur valide pour charger les donnees client.");
        }
        return email;
    }

    private String getCurrentUserEmailOrNull() {
        if (currentUserEmailField == null || currentUserEmailField.getText() == null) {
            return null;
        }
        String email = currentUserEmailField.getText().trim();
        if (email.isEmpty() || !email.contains("@")) {
            return null;
        }
        return email;
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fitopia Front");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
