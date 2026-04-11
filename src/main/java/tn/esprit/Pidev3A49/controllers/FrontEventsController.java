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
import tn.esprit.Pidev3A49.models.Participation;
import tn.esprit.Pidev3A49.service.EventService;
import tn.esprit.Pidev3A49.service.ParticipationService;

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
    @FXML private Label heroPremiumEventsLabel;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private DatePicker dateFilter;
    @FXML private ComboBox<String> priceFilter;
    @FXML private ComboBox<String> sortFilter;

    @FXML private FlowPane cardsContainer;
    @FXML private StackPane emptyStateBox;
    @FXML private VBox eventsSectionAnchor;

    private final EventService eventService = new EventService();
    private final ParticipationService participationService = new ParticipationService();

    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> currentFilteredEvents = new ArrayList<>();
    private final Map<Integer, Long> participationCountByEvent = new HashMap<>();
    private final Set<Integer> favoriteEventIds = new HashSet<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureFilters();
        loadData();
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        categoryFilter.getSelectionModel().selectFirst();
        dateFilter.setValue(null);
        priceFilter.getSelectionModel().selectFirst();
        sortFilter.getSelectionModel().selectFirst();
        applyFilters();
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
                "Price high to low",
                "Premium first"
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
            rebuildCategoryFilter();
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

    private void applyFilters() {
        List<Event> filtered = allEvents.stream()
                .filter(this::matchesSearch)
                .filter(this::matchesCategory)
                .filter(this::matchesDate)
                .filter(this::matchesPrice)
                .sorted(getComparator())
                .collect(Collectors.toList());

        currentFilteredEvents.clear();
        currentFilteredEvents.addAll(filtered);
        renderUpcomingEvent(filtered);
        renderCards(filtered);
        updateHeroStats(filtered);

        resultCountLabel.setText(filtered.size() + (filtered.size() == 1 ? " evenement" : " evenements"));
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
        if ("Premium first".equals(selected)) {
            return Comparator.comparing(Event::isPremium).reversed()
                    .thenComparing(Event::getDateEvent, Comparator.nullsLast(Comparator.naturalOrder()));
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

        long premiumEvents = filtered.stream()
                .filter(Event::isPremium)
                .count();

        heroTotalEventsLabel.setText(filtered.size() + (filtered.size() == 1 ? " evenement" : " evenements"));
        heroOpenEventsLabel.setText(openEvents + (openEvents == 1 ? " ouvert" : " ouverts"));
        heroPremiumEventsLabel.setText(premiumEvents + " premium");
    }

    private void renderCards(List<Event> events) {
        cardsContainer.getChildren().clear();
        for (Event event : events) {
            cardsContainer.getChildren().add(buildEventCard(event));
        }
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
                createSoftBadge((favoriteEventIds.contains(event.getIdEvent()) ? "1" : "0") + " favoris"),
                createSoftBadge(buildRatingText(event))
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
        infoButton.setOnAction(actionEvent ->
                showInfo(nullSafe(event.getTitre()), trimDescription(event.getDescription(), 220))
        );

        Button reserveButton = new Button(getRemainingPlaces(event) > 0 ? "RESERVER" : "WAITLIST");
        reserveButton.getStyleClass().add(
                getRemainingPlaces(event) > 0
                        ? "front-card-primary-button"
                        : "front-card-waitlist-button"
        );
        reserveButton.setOnAction(actionEvent -> openReservationWindow(event));

        Button priorityButton = new Button(getRemainingPlaces(event) > 0 ? "Reservation prioritaire" : "Liste d'attente");
        priorityButton.getStyleClass().add("front-card-priority-button");
        priorityButton.setDisable(getRemainingPlaces(event) > 0);

        bottomRow.getChildren().addAll(priceBox, infoButton, reserveButton, priorityButton);

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
            case "Premium" -> label.getStyleClass().add("front-badge-premium");
            case "Complet" -> label.getStyleClass().add("front-badge-full");
            case "Tendance" -> label.getStyleClass().add("front-badge-trending");
            case "Recommande" -> label.getStyleClass().add("front-badge-recommended");
            case "Nouveau" -> label.getStyleClass().add("front-badge-new");
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
        } else if (getParticipantCount(event) >= Math.max(3, event.getCapacite() / 2)) {
            badges.add("Tendance");
        }
        if (event.getPrixEvent() > 0 && event.getPrixEvent() < 50) {
            badges.add("Recommande");
        }

        return badges;
    }

    private boolean isRecent(Event event) {
        LocalDateTime createdAt = event.getCreatedAt();
        return createdAt != null && createdAt.isAfter(LocalDateTime.now().minusDays(21));
    }

    private int getParticipantCount(Event event) {
        return participationCountByEvent.getOrDefault(event.getIdEvent(), 0L).intValue();
    }

    private int getRemainingPlaces(Event event) {
        return Math.max(0, event.getCapacite() - getParticipantCount(event));
    }

    private void openReservationWindow(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReservationView.fxml"));
            Parent root = loader.load();

            ReservationController controller = loader.getController();
            controller.setEvent(event, getRemainingPlaces(event));

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
        if (event.isPremium()) {
            return "Top note 5.0/5";
        }
        return "Nouvel event 0.0/5";
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fitopia Front");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
