package tn.esprit.Pidev3A49.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tn.esprit.Pidev3A49.models.Event;
import tn.esprit.Pidev3A49.models.LoyaltyCustomerSummary;
import tn.esprit.Pidev3A49.models.Participation;
import tn.esprit.Pidev3A49.models.Reservation;
import tn.esprit.Pidev3A49.service.EventService;
import tn.esprit.Pidev3A49.service.LoyaltyService;
import tn.esprit.Pidev3A49.service.ParticipationService;
import tn.esprit.Pidev3A49.service.ReservationService;
import tn.esprit.Pidev3A49.service.WaitlistService;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.stage.FileChooser;
import tn.esprit.Pidev3A49.service.PdfExportService;

import java.io.File;
import java.nio.file.Path;
public class MainController implements Initializable {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker dateEventPicker;
    @FXML private TextField lieuField;
    @FXML private TextField capaciteField;
    @FXML private TextField typeEventField;
    @FXML private TextField imageEventField;
    @FXML private TextField prixEventField;
    @FXML private CheckBox premiumEventCheckBox;
    @FXML private Button ajouterButton;

    @FXML private Button tablesButton;
    @FXML private Button participantsNavButton;
    @FXML private Button loyaltyNavButton;
    @FXML private Button eventsManagementButton;
    @FXML private Button dashboardNavButton;
    @FXML private Button createNavButton;
    @FXML private Button eventsWorkspaceButton;
    @FXML private Button participantsWorkspaceButton;
    @FXML private Button loyaltyWorkspaceButton;
    @FXML private Button createCardButton;
    @FXML private Button updateCardButton;
    @FXML private Button deleteCardButton;
    @FXML private Button dashboardCardButton;

    @FXML private VBox tablesLandingSection;
    @FXML private VBox eventsWorkspaceSection;
    @FXML private VBox participantsSection;
    @FXML private VBox loyaltySection;
    @FXML private BorderPane rootPane;
    @FXML private ScrollPane sidebarScrollPane;
    @FXML private VBox contentShell;
    @FXML private HBox heroBanner;
    @FXML private FlowPane statsPane;
    @FXML private HBox workspaceSwitcher;
    @FXML private VBox eventsIntroBanner;

    @FXML private VBox addEventSection;
    @FXML private VBox updateEventsSection;
    @FXML private VBox deleteEventsSection;
    @FXML private VBox dashboardSection;

    @FXML private VBox updateEventsContainer;
    @FXML private VBox deleteEventsContainer;
    @FXML private VBox tablesSubmenu;
    @FXML private VBox eventsSubmenu;

    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, Integer> idColumn;
    @FXML private TableColumn<Event, String> titreColumn;
    @FXML private TableColumn<Event, String> typeEventColumn;
    @FXML private TableColumn<Event, LocalDate> dateColumn;
    @FXML private TableColumn<Event, String> lieuColumn;
    @FXML private TableColumn<Event, Integer> resActivesColumn;
    @FXML private TableColumn<Event, Integer> capaciteColumn;
    @FXML private TableColumn<Event, Integer> restantesColumn;
    @FXML private TableColumn<Event, String> etatColumn;
    @FXML private TableColumn<Event, String> premiumColumn;
    @FXML private TableColumn<Event, Double> prixColumn;
    @FXML private TableColumn<Event, String> descriptionColumn;
    @FXML private TableColumn<Event, String> imageEventColumn;
    @FXML private TableColumn<Event, LocalDateTime> createdAtColumn;
    @FXML private TextField searchField;

    @FXML private TableView<Participation> participationTable;
    @FXML private TableColumn<Participation, Integer> participationIdColumn;
    @FXML private TableColumn<Participation, String> nomParticipantColumn;
    @FXML private TableColumn<Participation, String> emailParticipantColumn;
    @FXML private TableColumn<Participation, LocalDateTime> dateInscriptionColumn;
    @FXML private TableColumn<Participation, String> evenementParticipationColumn;
    @FXML private TableColumn<Participation, String> participantStatusColumn;
    @FXML private TableColumn<Participation, Participation> participantActionColumn;
    @FXML private TextField participantSearchField;
    @FXML private TextField participantEmailFilterField;
    @FXML private ComboBox<String> participantEventFilterCombo;
    @FXML private ComboBox<String> participantStatusFilterCombo;
    @FXML private TextField participantNameField;
    @FXML private TextField participantEmailField;
    @FXML private ComboBox<Event> participantEventCombo;
    @FXML private Label participantFormStatusLabel;
    @FXML private Label participantsTotalCountLabel;
    @FXML private Label participantsConfirmedCountLabel;
    @FXML private Label participantsCancelledCountLabel;

    @FXML private Label totalEventsLabel;
    @FXML private Label totalParticipantsLabel;
    @FXML private Label completeEventsLabel;
    @FXML private Label loyaltyClientCountLabel;
    @FXML private Label statusLabel;
    @FXML private Label pageTitleLabel;
    @FXML private Label pageSubtitleLabel;
    @FXML private FlowPane crudCardsRow;

    @FXML private TableView<LoyaltyCustomerSummary> loyaltyTable;
    @FXML private TableColumn<LoyaltyCustomerSummary, String> loyaltyEmailColumn;
    @FXML private TableColumn<LoyaltyCustomerSummary, String> loyaltyNameColumn;
    @FXML private TableColumn<LoyaltyCustomerSummary, Integer> loyaltyReservationsColumn;
    @FXML private TableColumn<LoyaltyCustomerSummary, Double> loyaltySpentColumn;
    @FXML private TableColumn<LoyaltyCustomerSummary, LocalDateTime> loyaltyLastPurchaseColumn;
    @FXML private TableColumn<LoyaltyCustomerSummary, String> loyaltyTierColumn;
    @FXML private TableColumn<LoyaltyCustomerSummary, Integer> loyaltyScoreColumn;
    @FXML private TableColumn<LoyaltyCustomerSummary, String> loyaltyVipColumn;

    private final EventService eventService = new EventService();
    private final ParticipationService participationService = new ParticipationService();
    private final LoyaltyService loyaltyService = new LoyaltyService();
    private final ReservationService reservationService = new ReservationService();
    private final WaitlistService waitlistService = new WaitlistService();

    private final ObservableList<Event> eventList = FXCollections.observableArrayList();
    private final FilteredList<Event> filteredEvents = new FilteredList<>(eventList, event -> true);

    private final ObservableList<Participation> participationList = FXCollections.observableArrayList();
    private final FilteredList<Participation> filteredParticipations = new FilteredList<>(participationList, participation -> true);
    private final ObservableList<LoyaltyCustomerSummary> loyaltyList = FXCollections.observableArrayList();

    private final Map<Integer, Long> participationCountByEvent = new HashMap<>();
    private final Map<Integer, Integer> waitlistCountByEvent = new HashMap<>();
    private final Map<Integer, String> eventTitleById = new HashMap<>();
    private final Map<String, String> participantStatusByKey = new HashMap<>();
    private final Map<String, LocalDateTime> participantStatusDateByKey = new HashMap<>();
    private final PdfExportService pdfExportService = new PdfExportService();
    private final Timeline liveSyncTimeline = new Timeline();
    private Participation selectedParticipation;
    private enum EventWindowMode {
        CREATE,
        UPDATE,
        DELETE,
        DASHBOARD
    }

    @FXML
    private void exportPdfPlaceholder() {
        if (eventList.isEmpty()) {
            showError("Export PDF impossible", "Aucun evenement a exporter.", null);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la liste des evenements en PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF files", "*.pdf")
        );
        fileChooser.setInitialFileName("fitopia-events-dashboard.pdf");

        File selectedFile = fileChooser.showSaveDialog(eventTable.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        try {
            pdfExportService.exportEvents(eventList, selectedFile.toPath());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export PDF");
            alert.setHeaderText("Export reussi");
            alert.setContentText("Le PDF des evenements a ete genere avec succes.");
            alert.showAndWait();
        } catch (Exception e) {
            showError("Export PDF impossible", "Erreur lors de la generation du PDF des evenements.", e);
        }
    }

    @FXML
    private void exportParticipantsPdf() {
        if (participationList.isEmpty()) {
            showError("Export PDF impossible", "Aucune participation a exporter.", null);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la liste des participations en PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        fileChooser.setInitialFileName("fitopia-participations.pdf");

        File selectedFile = fileChooser.showSaveDialog(participationTable.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        try {
            pdfExportService.exportParticipations(participationList, selectedFile.toPath());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export PDF");
            alert.setHeaderText("Export reussi");
            alert.setContentText("Le PDF des participations a ete genere avec succes.");
            alert.showAndWait();
        } catch (Exception e) {
            showError("Export PDF impossible", "Erreur lors de la generation du PDF des participations.", e);
        }
    }

    private Event selectedEvent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureDatePicker();
        configureEventTable();
        configureParticipationTable();
        configureParticipantFilters();
        configureLoyaltyTable();
        configureSearches();
        configureEventSelection();
        configureParticipantCrud();
        configureLiveSync();
        syncSidebarMenus();
        refreshAllData();
        showTablesLanding();
    }

    private void configureParticipantCrud() {
        participantEventCombo.setItems(eventList);
        participantEventCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Event event) {
                return event == null ? "" : event.getTitre();
            }

            @Override
            public Event fromString(String string) {
                if (string == null || string.isBlank()) {
                    return null;
                }
                return eventList.stream()
                        .filter(event -> string.equalsIgnoreCase(event.getTitre()))
                        .findFirst()
                        .orElse(null);
            }
        });

        participationTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedParticipation = newValue;
            if (newValue != null) {
                populateParticipantForm(newValue);
            } else {
                participantNameField.clear();
                participantEmailField.clear();
                participantEventCombo.getSelectionModel().clearSelection();
                participantFormStatusLabel.setText("Selectionne un participant pour modifier ou supprimer.");
            }
        });
    }

    private void configureLiveSync() {
        liveSyncTimeline.getKeyFrames().setAll(
                new KeyFrame(javafx.util.Duration.seconds(5), event -> refreshAllDataSilently())
        );
        liveSyncTimeline.setCycleCount(Timeline.INDEFINITE);
        liveSyncTimeline.play();
    }

    private void configureDatePicker() {
        dateEventPicker.setPromptText("jj/mm/aaaa");
        dateEventPicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : DATE_FORMATTER.format(date);
            }

            @Override
            public LocalDate fromString(String value) {
                if (value == null || value.trim().isEmpty()) {
                    return null;
                }
                return LocalDate.parse(value.trim(), DATE_FORMATTER);
            }
        });
    }

    private void configureEventTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idEvent"));
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        typeEventColumn.setCellValueFactory(new PropertyValueFactory<>("typeEvent"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateEvent"));
        lieuColumn.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        capaciteColumn.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prixEvent"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        imageEventColumn.setCellValueFactory(new PropertyValueFactory<>("imageEvent"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        resActivesColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(getActiveParticipants(cellData.getValue())));
        restantesColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(getRemainingPlaces(cellData.getValue())));
        etatColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(getEventStatus(cellData.getValue())));
        premiumColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().isPremium() ? "Premium" : "Standard"));
        premiumColumn.setCellFactory(column -> new PremiumTableCell());

        dateColumn.setCellFactory(column -> new FormattedTableCell<>(DATE_FORMATTER));
        createdAtColumn.setCellFactory(column -> new FormattedTableCell<>(DATE_TIME_FORMATTER));
        prixColumn.setCellFactory(column -> new NumberTableCell());

        SortedList<Event> sortedEvents = new SortedList<>(filteredEvents);
        sortedEvents.comparatorProperty().bind(eventTable.comparatorProperty());
        eventTable.setItems(sortedEvents);
    }

    private void configureParticipationTable() {
        participationIdColumn.setCellValueFactory(new PropertyValueFactory<>("idParticipation"));
        nomParticipantColumn.setCellValueFactory(new PropertyValueFactory<>("nomParticipant"));
        emailParticipantColumn.setCellValueFactory(new PropertyValueFactory<>("emailParticipant"));
        dateInscriptionColumn.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));
        evenementParticipationColumn.setCellValueFactory(new PropertyValueFactory<>("evenement"));
        participantStatusColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(getParticipantStatus(cellData.getValue())));
        participantActionColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue()));
        dateInscriptionColumn.setCellFactory(column -> new FormattedTableCell<>(DATE_TIME_FORMATTER));
        participantStatusColumn.setCellFactory(column -> new ParticipantStatusTableCell());
        participantActionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button detailsButton = new Button("Voir details");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox container = new HBox(6, detailsButton, deleteButton);

            {
                detailsButton.getStyleClass().add("participants-action-view");
                deleteButton.getStyleClass().add("participants-action-delete");
                detailsButton.setOnAction(event -> {
                    Participation participation = getItem();
                    if (participation != null) {
                        participationTable.getSelectionModel().select(participation);
                        populateParticipantForm(participation);
                        participantFormStatusLabel.setText("Participant charge : ID " + participation.getIdParticipation());
                    }
                });
                deleteButton.setOnAction(event -> {
                    Participation participation = getItem();
                    if (participation != null) {
                        selectedParticipation = participation;
                        deleteParticipant();
                    }
                });
            }

            @Override
            protected void updateItem(Participation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(container);
            }
        });

        participationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        participationTable.setMaxWidth(Double.MAX_VALUE);

        SortedList<Participation> sortedParticipations = new SortedList<>(filteredParticipations);
        sortedParticipations.comparatorProperty().bind(participationTable.comparatorProperty());
        participationTable.setItems(sortedParticipations);
    }

    private void configureParticipantFilters() {
        if (participantStatusFilterCombo != null) {
            participantStatusFilterCombo.getItems().setAll("Tous", "Confirmee", "Annulee", "Utilisee");
            participantStatusFilterCombo.getSelectionModel().selectFirst();
            participantStatusFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> applyParticipantFilter());
        }
        if (participantEventFilterCombo != null) {
            participantEventFilterCombo.getItems().setAll("Tous les evenements");
            participantEventFilterCombo.getSelectionModel().selectFirst();
            participantEventFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> applyParticipantFilter());
        }
        if (participantEmailFilterField != null) {
            participantEmailFilterField.textProperty().addListener((observable, oldValue, newValue) -> applyParticipantFilter());
        }
    }

    private void configureLoyaltyTable() {
        if (loyaltyTable == null) {
            return;
        }

        loyaltyEmailColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getEmailParticipant()));
        loyaltyNameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getNomParticipant()));
        loyaltyReservationsColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getValidReservations()));
        loyaltySpentColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getTotalSpent()));
        loyaltyLastPurchaseColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getLastPurchase()));
        loyaltyTierColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getTier()));
        loyaltyScoreColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getScore()));
        loyaltyVipColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().isVipAccess() ? "Oui" : "Non"));

        loyaltySpentColumn.setCellFactory(column -> new NumberTableCell());
        loyaltyLastPurchaseColumn.setCellFactory(column -> new FormattedTableCell<>(DATE_TIME_FORMATTER));
        loyaltyTable.setItems(loyaltyList);
    }

    private void configureSearches() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyEventFilter());
        participantSearchField.textProperty().addListener((observable, oldValue, newValue) -> applyParticipantFilter());
    }

    private void configureEventSelection() {
        eventTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedEvent = newValue;
            if (newValue != null) {
                populateForm(newValue);
                statusLabel.setText("Evenement selectionne : ID " + newValue.getIdEvent() + " - " + newValue.getTitre());
            }
        });
    }

    @FXML
    private void refreshAllData() {
        loadEvents();
        loadParticipations();
        loadLoyaltyData();
        updateDashboardStats();
    }

    private void refreshAllDataSilently() {
        try {
            List<Event> events = eventService.getAll();
            eventList.setAll(events);
            rebuildEventTitleMap(events);
            refreshWaitlistStats();
            sortEventTableByIdDesc();
            renderUpdateSection(events);
            renderDeleteSection(events);

            List<Participation> participations = participationService.getAll();
            participationList.setAll(participations);
            rebuildParticipationCountMap(participations);
            rebuildParticipantStatusMap(participations);
            sortParticipationTableByIdDesc();

            loyaltyList.setAll(loyaltyService.getAllCustomerSummaries());

            eventTable.refresh();
            participationTable.refresh();
            if (loyaltyTable != null) {
                loyaltyTable.refresh();
            }
            updateDashboardStats();
        } catch (Exception ignored) {
        }
    }

    @FXML
    private void openFrontEventsWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontEvents.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1480, 920);
            scene.getStylesheets().add(getClass().getResource("/styles/front-events.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Fitopia - Front Events");
            stage.setScene(scene);
            stage.setMinWidth(1280);
            stage.setMinHeight(820);
            stage.setOnHidden(event -> refreshAllData());
            stage.show();
        } catch (Exception e) {
            showError("Ouverture impossible", "La vue front des evenements n'a pas pu etre chargee.", e);
        }
    }

    @FXML
    private void openWaitlistAdminWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/WaitlistAdminView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 980, 680);
            scene.getStylesheets().add(getClass().getResource("/styles/waitlist-admin.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Fitopia - Waitlist Admin");
            stage.setScene(scene);
            stage.setMinWidth(900);
            stage.setMinHeight(620);
            stage.setOnHidden(event -> refreshAllData());
            stage.show();
        } catch (Exception e) {
            showError("Ouverture impossible", "La vue admin waitlist n'a pas pu etre chargee.", e);
        }
    }

    @FXML
    private void openEventStatsWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventStatsView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 980, 680);
            scene.getStylesheets().add(getClass().getResource("/styles/event-stats.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Fitopia - Analyse des evenements");
            stage.setScene(scene);
            stage.setMinWidth(900);
            stage.setMinHeight(620);
            stage.setOnHidden(event -> refreshAllData());
            stage.show();
        } catch (Exception e) {
            showError("Ouverture impossible", "La vue d'analyse des evenements n'a pas pu etre chargee.", e);
        }
    }

    @FXML
    private void refreshEvents() {
        loadEvents();
        updateDashboardStats();
    }

    @FXML
    private void refreshParticipations() {
        loadParticipations();
        updateDashboardStats();
    }

    @FXML
    private void refreshLoyalty() {
        loadLoyaltyData();
        updateDashboardStats();
    }

    private void loadEvents() {
        try {
            List<Event> events = eventService.getAll();
            eventList.setAll(events);
            rebuildEventTitleMap(events);
            refreshWaitlistStats();
            sortEventTableByIdDesc();
            renderUpdateSection(events);
            renderDeleteSection(events);
            eventTable.refresh();
            restoreSelectedParticipantEvent();
            System.out.println("Evenements charges : " + events.size());
        } catch (Exception e) {
            showError("Chargement impossible", "Erreur lors du chargement des evenements.", e);
        }
    }

    private void loadParticipations() {
        try {
            List<Participation> participations = participationService.getAll();
            participationList.setAll(participations);
            rebuildParticipationCountMap(participations);
            rebuildParticipantStatusMap(participations);
            sortParticipationTableByIdDesc();
            participationTable.refresh();
            eventTable.refresh();
            if (selectedParticipation != null) {
                selectedParticipation = participationList.stream()
                        .filter(participation -> participation.getIdParticipation() == selectedParticipation.getIdParticipation())
                        .findFirst()
                        .orElse(null);
                if (selectedParticipation != null) {
                    populateParticipantForm(selectedParticipation);
                } else {
                    clearParticipantForm();
                }
            }
            updateParticipantCounters();
            refreshParticipantEventFilterOptions();
            System.out.println("Participations chargees : " + participations.size());
        } catch (Exception e) {
            showError("Chargement impossible", "Erreur lors du chargement des participations.", e);
        }
    }

    private void loadLoyaltyData() {
        try {
            loyaltyList.setAll(loyaltyService.getAllCustomerSummaries());
            if (loyaltyTable != null) {
                loyaltyTable.refresh();
            }
            System.out.println("Clients fidelite charges : " + loyaltyList.size());
        } catch (Exception e) {
            showError("Chargement impossible", "Erreur lors du chargement des donnees fidelite.", e);
        }
    }

    @FXML
    private void addParticipant() {
        try {
            Participation participation = buildParticipationFromForm();
            participationService.add(participation);
            refreshParticipations();
            clearParticipantForm();
            participantFormStatusLabel.setText("Participant ajoute avec succes.");
            statusLabel.setText("Participant ajoute avec succes : " + participation.getNomParticipant());
        } catch (IllegalArgumentException e) {
            participantFormStatusLabel.setText(e.getMessage());
        } catch (Exception e) {
            showError("Ajout impossible", "Erreur lors de l'ajout du participant.", e);
        }
    }

    @FXML
    private void updateParticipant() {
        if (selectedParticipation == null) {
            participantFormStatusLabel.setText("Selectionne un participant a modifier.");
            return;
        }

        try {
            Participation updatedParticipation = buildParticipationFromForm();
            updatedParticipation.setIdParticipation(selectedParticipation.getIdParticipation());
            updatedParticipation.setDateInscription(selectedParticipation.getDateInscription());
            participationService.update(updatedParticipation);
            refreshParticipations();
            clearParticipantForm();
            participantFormStatusLabel.setText("Participant modifie avec succes.");
            statusLabel.setText("Participant modifie avec succes : ID " + updatedParticipation.getIdParticipation());
        } catch (IllegalArgumentException e) {
            participantFormStatusLabel.setText(e.getMessage());
        } catch (Exception e) {
            showError("Modification impossible", "Erreur lors de la modification du participant.", e);
        }
    }

    @FXML
    private void deleteParticipant() {
        if (selectedParticipation == null) {
            participantFormStatusLabel.setText("Selectionne un participant a supprimer.");
            return;
        }

        try {
            int idParticipation = selectedParticipation.getIdParticipation();
            String participantName = selectedParticipation.getNomParticipant();
            participationService.delete(idParticipation);
            refreshParticipations();
            clearParticipantForm();
            participantFormStatusLabel.setText("Participant supprime avec succes.");
            statusLabel.setText("Participant supprime avec succes : ID " + idParticipation + " - " + participantName);
        } catch (Exception e) {
            showError("Suppression impossible", "Erreur lors de la suppression du participant.", e);
        }
    }

    @FXML
    private void clearParticipantForm() {
        selectedParticipation = null;
        participantNameField.clear();
        participantEmailField.clear();
        participantEventCombo.getSelectionModel().clearSelection();
        participationTable.getSelectionModel().clearSelection();
        participantFormStatusLabel.setText("Selectionne un participant pour modifier ou supprimer.");
    }

    private void rebuildEventTitleMap(List<Event> events) {
        eventTitleById.clear();
        for (Event event : events) {
            eventTitleById.put(event.getIdEvent(), event.getTitre());
        }
    }

    private void rebuildParticipationCountMap(List<Participation> participations) {
        participationCountByEvent.clear();
        for (Participation participation : participations) {
            participationCountByEvent.merge(participation.getIdEvent(), 1L, Long::sum);
        }
    }

    private void refreshWaitlistStats() {
        try {
            waitlistCountByEvent.clear();
            waitlistCountByEvent.putAll(waitlistService.countActiveByEvent());
        } catch (Exception e) {
            waitlistCountByEvent.clear();
            System.err.println("Impossible de charger les compteurs waitlist.");
        }
    }

    private void updateDashboardStats() {
        totalEventsLabel.setText(String.valueOf(eventList.size()));
        totalParticipantsLabel.setText(String.valueOf(participationList.size()));
        if (loyaltyClientCountLabel != null) {
            loyaltyClientCountLabel.setText(String.valueOf(loyaltyList.size()));
        }

        long completeCount = eventList.stream()
                .filter(event -> getRemainingPlaces(event) <= 0)
                .count();

        completeEventsLabel.setText(String.valueOf(completeCount));
    }

    private void sortEventTableByIdDesc() {
        eventTable.getSortOrder().clear();
        idColumn.setSortType(TableColumn.SortType.DESCENDING);
        eventTable.getSortOrder().add(idColumn);
        eventTable.sort();
    }

    private void sortParticipationTableByIdDesc() {
        participationTable.getSortOrder().clear();
        participationIdColumn.setSortType(TableColumn.SortType.DESCENDING);
        participationTable.getSortOrder().add(participationIdColumn);
        participationTable.sort();
    }

    private int getActiveParticipants(Event event) {
        return participationCountByEvent.getOrDefault(event.getIdEvent(), 0L).intValue();
    }

    private int getRemainingPlaces(Event event) {
        return event.getCapacite() - getActiveParticipants(event);
    }

    private String getEventStatus(Event event) {
        return getRemainingPlaces(event) <= 0 ? "Complet" : "Ouvert";
    }

    @FXML
    private void applyEventFilter() {
        String rawValue = searchField.getText();
        filteredEvents.setPredicate(event -> {
            if (rawValue == null || rawValue.trim().isEmpty()) {
                return true;
            }

            String value = rawValue.toLowerCase(Locale.ROOT).trim();
            return contains(event.getTitre(), value)
                    || contains(event.getDescription(), value)
                    || contains(event.getLieu(), value)
                    || contains(event.getTypeEvent(), value)
                    || contains(event.getImageEvent(), value)
                    || contains(event.isPremium() ? "premium vip" : "standard normal", value)
                    || contains(getEventStatus(event), value);
        });
    }

    @FXML
    private void resetEventFilter() {
        searchField.clear();
        filteredEvents.setPredicate(event -> true);
    }

    @FXML
    private void applyParticipantFilter() {
        String rawValue = participantSearchField.getText();
        String emailFilterValue = participantEmailFilterField == null ? null : participantEmailFilterField.getText();
        String selectedEvent = participantEventFilterCombo == null ? null : participantEventFilterCombo.getValue();
        String selectedStatus = participantStatusFilterCombo == null ? null : participantStatusFilterCombo.getValue();
        filteredParticipations.setPredicate(participation -> {
            if (rawValue == null || rawValue.trim().isEmpty()) {
                return matchesAdditionalParticipantFilters(participation, emailFilterValue, selectedEvent, selectedStatus);
            }

            String value = rawValue.toLowerCase(Locale.ROOT).trim();
            String eventTitle = participation.getEvenement() == null ? "" : participation.getEvenement();
            boolean matchesSearch = contains(participation.getNomParticipant(), value)
                    || contains(participation.getEmailParticipant(), value)
                    || contains(eventTitle, value);

            return matchesSearch && matchesAdditionalParticipantFilters(participation, emailFilterValue, selectedEvent, selectedStatus);
        });
        updateParticipantCounters();
    }

    @FXML
    private void resetParticipantFilter() {
        participantSearchField.clear();
        if (participantEmailFilterField != null) {
            participantEmailFilterField.clear();
        }
        if (participantEventFilterCombo != null) {
            participantEventFilterCombo.getSelectionModel().selectFirst();
        }
        if (participantStatusFilterCombo != null) {
            participantStatusFilterCombo.getSelectionModel().selectFirst();
        }
        filteredParticipations.setPredicate(participation -> true);
        updateParticipantCounters();
    }

    private boolean matchesAdditionalParticipantFilters(
            Participation participation,
            String emailFilterValue,
            String selectedEvent,
            String selectedStatus
    ) {
        if (emailFilterValue != null && !emailFilterValue.isBlank()) {
            String normalized = emailFilterValue.toLowerCase(Locale.ROOT).trim();
            if (!contains(participation.getEmailParticipant(), normalized)) {
                return false;
            }
        }

        if (selectedEvent != null && !selectedEvent.isBlank() && !"Tous les evenements".equals(selectedEvent)) {
            String eventTitle = participation.getEvenement() == null ? "" : participation.getEvenement();
            if (!selectedEvent.equalsIgnoreCase(eventTitle)) {
                return false;
            }
        }

        if (selectedStatus != null && !selectedStatus.isBlank() && !"Tous".equals(selectedStatus)) {
            String status = getParticipantStatus(participation);
            if (!selectedStatus.equalsIgnoreCase(status)) {
                return false;
            }
        }

        return true;
    }

    private void refreshParticipantEventFilterOptions() {
        if (participantEventFilterCombo == null) {
            return;
        }
        String currentValue = participantEventFilterCombo.getValue();
        List<String> eventNames = participationList.stream()
                .map(Participation::getEvenement)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();
        participantEventFilterCombo.getItems().setAll("Tous les evenements");
        participantEventFilterCombo.getItems().addAll(eventNames);
        if (currentValue != null && participantEventFilterCombo.getItems().contains(currentValue)) {
            participantEventFilterCombo.setValue(currentValue);
        } else {
            participantEventFilterCombo.getSelectionModel().selectFirst();
        }
    }

    private void rebuildParticipantStatusMap(List<Participation> participations) {
        participantStatusByKey.clear();
        participantStatusDateByKey.clear();
        Map<String, List<Participation>> participationsByEmail = participations.stream()
                .filter(participation -> participation.getEmailParticipant() != null && !participation.getEmailParticipant().isBlank())
                .collect(java.util.stream.Collectors.groupingBy(participation -> participation.getEmailParticipant().trim().toLowerCase(Locale.ROOT)));

        for (Map.Entry<String, List<Participation>> entry : participationsByEmail.entrySet()) {
            String email = entry.getKey();
            List<Reservation> reservations;
            try {
                reservations = reservationService.getByEmail(email);
            } catch (Exception e) {
                continue;
            }

            for (Reservation reservation : reservations) {
                String key = buildParticipantKey(reservation.getIdEvent(), reservation.getEmailParticipant());
                LocalDateTime reservationDate = reservation.getDateReservation() == null ? LocalDateTime.MIN : reservation.getDateReservation();
                LocalDateTime currentDate = participantStatusDateByKey.get(key);
                if (currentDate == null || reservationDate.isAfter(currentDate)) {
                    participantStatusDateByKey.put(key, reservationDate);
                    participantStatusByKey.put(key, normalizeParticipantStatus(reservation.getStatut()));
                }
            }
        }
    }

    private String getParticipantStatus(Participation participation) {
        if (participation == null) {
            return "Confirmee";
        }
        String key = buildParticipantKey(participation.getIdEvent(), participation.getEmailParticipant());
        return participantStatusByKey.getOrDefault(key, "Confirmee");
    }

    private String buildParticipantKey(int idEvent, String email) {
        return idEvent + "|" + (email == null ? "" : email.trim().toLowerCase(Locale.ROOT));
    }

    private String normalizeParticipantStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return "Confirmee";
        }
        String normalized = rawStatus.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "annulee", "annulée", "cancelled" -> "Annulee";
            case "utilisee", "utilisée", "used" -> "Utilisee";
            case "payee", "payée", "paid", "confirmee", "confirmée", "confirmed" -> "Confirmee";
            default -> "Confirmee";
        };
    }

    private void updateParticipantCounters() {
        if (participantsTotalCountLabel == null) {
            return;
        }
        List<Participation> currentItems = filteredParticipations.stream().toList();
        long total = currentItems.size();
        long confirmed = currentItems.stream()
                .filter(participation -> "Confirmee".equalsIgnoreCase(getParticipantStatus(participation)))
                .count();
        long cancelled = currentItems.stream()
                .filter(participation -> "Annulee".equalsIgnoreCase(getParticipantStatus(participation)))
                .count();

        participantsTotalCountLabel.setText(String.valueOf(total));
        participantsConfirmedCountLabel.setText(String.valueOf(confirmed));
        participantsCancelledCountLabel.setText(String.valueOf(cancelled));
    }

    private boolean contains(String source, String value) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(value);
    }

    private Participation buildParticipationFromForm() {
        String participantName = requireNonBlank(participantNameField.getText(), "Le nom du participant est obligatoire.");
        String participantEmail = requireNonBlank(participantEmailField.getText(), "L'email du participant est obligatoire.");
        if (!participantEmail.contains("@")) {
            throw new IllegalArgumentException("L'email du participant est invalide.");
        }

        Event selectedEventForParticipant = participantEventCombo.getValue();
        if (selectedEventForParticipant == null) {
            throw new IllegalArgumentException("Choisis un evenement pour ce participant.");
        }

        Participation participation = new Participation(
                selectedEventForParticipant.getIdEvent(),
                participantName,
                participantEmail,
                selectedParticipation != null ? selectedParticipation.getDateInscription() : LocalDateTime.now()
        );
        participation.setEvenement(selectedEventForParticipant.getTitre());
        return participation;
    }

    private void populateParticipantForm(Participation participation) {
        participantNameField.setText(participation.getNomParticipant());
        participantEmailField.setText(participation.getEmailParticipant());
        participantEventCombo.setValue(findEventById(participation.getIdEvent()));
        participantFormStatusLabel.setText("Participant selectionne : ID " + participation.getIdParticipation());
    }

    private void restoreSelectedParticipantEvent() {
        if (selectedParticipation == null) {
            return;
        }
        participantEventCombo.setValue(findEventById(selectedParticipation.getIdEvent()));
    }

    private Event findEventById(int idEvent) {
        return eventList.stream()
                .filter(event -> event.getIdEvent() == idEvent)
                .findFirst()
                .orElse(null);
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private void renderUpdateSection(List<Event> events) {
        updateEventsContainer.getChildren().clear();
        if (events.isEmpty()) {
            updateEventsContainer.getChildren().add(buildEmptyState("Aucun evenement a modifier pour le moment."));
            return;
        }

        for (Event event : events) {
            updateEventsContainer.getChildren().add(buildEventCard(event, false));
        }
    }

    private void renderDeleteSection(List<Event> events) {
        deleteEventsContainer.getChildren().clear();
        if (events.isEmpty()) {
            deleteEventsContainer.getChildren().add(buildEmptyState("Aucun evenement a supprimer pour le moment."));
            return;
        }

        for (Event event : events) {
            deleteEventsContainer.getChildren().add(buildEventCard(event, true));
        }
    }

    private VBox buildEmptyState(String message) {
        VBox box = new VBox();
        box.getStyleClass().add("list-card");
        box.setPadding(new Insets(16));

        Label label = new Label(message);
        label.getStyleClass().add("list-card-meta");
        label.setWrapText(true);

        box.getChildren().add(label);
        return box;
    }

    private VBox buildEventCard(Event event, boolean deleteMode) {
        VBox card = new VBox(10);
        card.getStyleClass().add("list-card");
        card.setPadding(new Insets(14));

        HBox titleRow = new HBox(8);
        Label title = new Label(event.getTitre());
        title.getStyleClass().add("list-card-title");
        title.setWrapText(true);

        Label eventId = new Label("ID " + event.getIdEvent());
        eventId.getStyleClass().add("mini-chip");

        titleRow.getChildren().addAll(title, eventId);

        Label subtitle = new Label(formatDate(event.getDateEvent()) + "  |  " + nullSafe(event.getLieu()) + "  |  " + nullSafe(event.getTypeEvent()));
        subtitle.getStyleClass().add("list-card-meta");
        subtitle.setWrapText(true);

        Label description = new Label(nullSafe(event.getDescription()));
        description.getStyleClass().add("list-card-text");
        description.setWrapText(true);

        FlowPane badges = new FlowPane();
        badges.setHgap(6);
        badges.setVgap(6);
        badges.getChildren().addAll(
                buildBadge("Participants actifs: " + getActiveParticipants(event), "soft-badge"),
                buildBadge("Waitlist: " + waitlistCountByEvent.getOrDefault(event.getIdEvent(), 0), "soft-badge"),
                buildBadge("Places restantes: " + getRemainingPlaces(event), "soft-badge"),
                buildBadge(getEventStatus(event), getRemainingPlaces(event) <= 0 ? "danger-badge" : "success-badge")
        );
        if (event.isPremium()) {
            badges.getChildren().add(buildBadge("Premium", "premium-badge"));
        }

        HBox actions = new HBox(8);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button actionButton = new Button(deleteMode ? "Delete" : "Edit");
        actionButton.getStyleClass().add(deleteMode ? "danger-button" : "card-button");
        actionButton.setOnAction(e -> {
            if (deleteMode) {
                deleteEventFromCard(event);
            } else {
                editEventFromCard(event);
            }
        });

        actions.getChildren().addAll(spacer, actionButton);
        card.getChildren().addAll(titleRow, subtitle, badges, description, actions);
        return card;
    }

    private Label buildBadge(String text, String styleClass) {
        Label badge = new Label(text);
        badge.getStyleClass().add(styleClass);
        return badge;
    }

    @FXML
    private void toggleTablesMenu() {
        showTablesLanding();
    }

    @FXML
    private void toggleEventsManagementMenu() {
        showEventsWorkspace();
    }

    @FXML
    private void showTablesLanding() {
        setMainView(true, false, false, false);
        hideAllEventSections();
        showCrudCards();
        setWorkspaceMode("events");
        setSidebarContext(true, false, false, false);
        setQuickAccessMode(null);
        updatePageHero(
                "Admin Dashboard",
                "Welcome to Fitopia Admin. Manage your platform, monitor key areas, and keep Fitopia running smoothly."
        );
        statusLabel.setText("Page Tables active. Selectionne une section de gestion depuis la sidebar.");
    }

    @FXML
    private void showEventsWorkspace() {
        setMainView(false, true, false, false);
        hideAllEventSections();
        showCrudCards();
        setWorkspaceMode("events");
        setSidebarContext(false, true, false, false);
        setQuickAccessMode(null);
        updatePageHero(
                "Gestion des evenements",
                "Creez, modifiez ou supprimez les evenements enregistres dans le systeme."
        );
        statusLabel.setText("Gestion des evenements ouverte. Choisis Create, Update, Delete ou Dashboard.");
    }

    @FXML
    private void showParticipantsSection() {
        refreshParticipations();
        setMainView(false, false, true, false);
        setWorkspaceMode("participants");
        setSidebarContext(false, false, true, false);
        setQuickAccessMode(null);
        updatePageHero(
                "Participation Dashboard",
                "Review registrations and participant activity linked to your events."
        );
        participantSearchField.requestFocus();
        statusLabel.setText("Section participation active.");
    }

    @FXML
    private void showLoyaltySection() {
        refreshLoyalty();
        setMainView(false, false, false, true);
        setWorkspaceMode("loyalty");
        setSidebarContext(false, false, false, true);
        setQuickAccessMode(null);
        updatePageHero(
                "Fidelite Clients",
                "Suivez les clients actifs, leurs depenses, leur tier et leur acces VIP."
        );
        if (loyaltyTable != null) {
            loyaltyTable.requestFocus();
        }
        statusLabel.setText("Section fidelite active.");
    }

    @FXML
    private void showCreateSection() {
        activateEventsWorkspace();
        showCrudCards();
        showEventSection(addEventSection, createCardButton);
        setQuickAccessMode(createNavButton);
        titreField.requestFocus();
        statusLabel.setText("Section Add Event active.");
    }

    @FXML
    private void showUpdateSection() {
        activateEventsWorkspace();
        showCrudCards();
        showEventSection(updateEventsSection, updateCardButton);
        setQuickAccessMode(null);
        statusLabel.setText("Section Update Events active.");
    }

    @FXML
    private void showDeleteSection() {
        activateEventsWorkspace();
        showCrudCards();
        showEventSection(deleteEventsSection, deleteCardButton);
        setQuickAccessMode(null);
        statusLabel.setText("Section Delete Events active.");
    }

    @FXML
    private void showDashboardSection() {
        activateEventsWorkspace();
        hideCrudCards();
        showEventSection(dashboardSection, dashboardCardButton);
        setQuickAccessMode(dashboardNavButton);
        searchField.requestFocus();
        statusLabel.setText("Section Events Dashboard & List active.");
    }

    @FXML
    private void openCreateWindow() {
        openEventWindow(EventWindowMode.CREATE);
    }

    @FXML
    private void openUpdateWindow() {
        openEventWindow(EventWindowMode.UPDATE);
    }

    @FXML
    private void openDeleteWindow() {
        openEventWindow(EventWindowMode.DELETE);
    }

    @FXML
    private void openDashboardWindow() {
        openEventWindow(EventWindowMode.DASHBOARD);
    }

    private void activateEventsWorkspace() {
        setMainView(false, true, false, false);
        setWorkspaceMode("events");
        setSidebarContext(false, true, false, false);
    }

    private void setMainView(boolean showTablesLanding, boolean showEventsWorkspace, boolean showParticipants, boolean showLoyalty) {
        tablesLandingSection.setVisible(showTablesLanding);
        tablesLandingSection.setManaged(showTablesLanding);

        eventsWorkspaceSection.setVisible(showEventsWorkspace);
        eventsWorkspaceSection.setManaged(showEventsWorkspace);

        participantsSection.setVisible(showParticipants);
        participantsSection.setManaged(showParticipants);

        if (loyaltySection != null) {
            loyaltySection.setVisible(showLoyalty);
            loyaltySection.setManaged(showLoyalty);
        }
    }

    private void hideAllEventSections() {
        VBox[] sections = {addEventSection, updateEventsSection, deleteEventsSection, dashboardSection};
        for (VBox section : sections) {
            section.setVisible(false);
            section.setManaged(false);
        }
        clearActionCards();
    }

    private void showEventSection(VBox sectionToShow, Button activeCard) {
        VBox[] sections = {addEventSection, updateEventsSection, deleteEventsSection, dashboardSection};
        for (VBox section : sections) {
            boolean visible = section == sectionToShow;
            section.setVisible(visible);
            section.setManaged(visible);
        }
        clearActionCards();
        setCardActive(activeCard, true);
    }

    private void clearActionCards() {
        setCardActive(createCardButton, false);
        setCardActive(updateCardButton, false);
        setCardActive(deleteCardButton, false);
        setCardActive(dashboardCardButton, false);
    }

    private void setCardActive(Button button, boolean active) {
        if (active) {
            if (!button.getStyleClass().contains("active-action-card")) {
                button.getStyleClass().add("active-action-card");
            }
        } else {
            button.getStyleClass().remove("active-action-card");
        }
    }

    private void setWorkspaceMode(String workspace) {
        toggleStyleClass(eventsWorkspaceButton, "active-workspace", "events".equals(workspace));
        toggleStyleClass(participantsWorkspaceButton, "active-workspace", "participants".equals(workspace));
        toggleStyleClass(loyaltyWorkspaceButton, "active-workspace", "loyalty".equals(workspace));
    }

    private void setSidebarContext(boolean tablesActive, boolean eventsActive, boolean participationActive, boolean loyaltyActive) {
        toggleStyleClass(tablesButton, "active-nav", tablesActive);
        toggleStyleClass(eventsManagementButton, "active-nav", eventsActive);
        toggleStyleClass(participantsNavButton, "active-nav", participationActive);
        toggleStyleClass(loyaltyNavButton, "active-nav", loyaltyActive);
    }

    private void syncSidebarMenus() {
        setSubmenuVisible(tablesSubmenu, true);
        setSubmenuVisible(eventsSubmenu, true);
    }

    private void setSubmenuVisible(VBox submenu, boolean visible) {
        submenu.setVisible(visible);
        submenu.setManaged(visible);
    }

    private void setQuickAccessMode(Button activeButton) {
        toggleStyleClass(dashboardNavButton, "active-nav", activeButton == dashboardNavButton);
        toggleStyleClass(createNavButton, "active-nav", activeButton == createNavButton);
    }

    private void toggleStyleClass(Button button, String styleClass, boolean active) {
        if (button == null) {
            return;
        }
        if (active) {
            if (!button.getStyleClass().contains(styleClass)) {
                button.getStyleClass().add(styleClass);
            }
        } else {
            button.getStyleClass().remove(styleClass);
        }
    }

    private void editEventFromCard(Event event) {
        selectedEvent = event;
        populateForm(event);
        showCreateSection();
        statusLabel.setText("Evenement charge pour modification : ID " + event.getIdEvent() + ".");
    }

    private void deleteEventFromCard(Event event) {
        selectedEvent = event;
        supprimerEvent();
        showDeleteSection();
    }

    @FXML
    private void ajouterEvent() {
        try {
            Event event = buildEventFromForm();
            eventService.add(event);
            refreshAllData();
            clearSelectionAndForm();
            statusLabel.setText("Evenement ajoute avec succes : " + event.getTitre());
            showDashboardSection();
        } catch (IllegalArgumentException e) {
            showError("Saisie invalide", e.getMessage(), null);
        } catch (Exception e) {
            showError("Insertion impossible", "Erreur lors de l'ajout de l'evenement.", e);
        }
    }

    @FXML
    private void modifierEvent() {
        if (selectedEvent == null) {
            showError("Modification impossible", "Selectionne ou charge d'abord un evenement a modifier.", null);
            return;
        }

        try {
            Event updatedEvent = buildEventFromForm();
            updatedEvent.setIdEvent(selectedEvent.getIdEvent());
            updatedEvent.setCreatedAt(selectedEvent.getCreatedAt());
            eventService.update(updatedEvent);
            refreshAllData();
            clearSelectionAndForm();
            statusLabel.setText("Evenement modifie avec succes : ID " + updatedEvent.getIdEvent());
            showUpdateSection();
        } catch (IllegalArgumentException e) {
            showError("Saisie invalide", e.getMessage(), null);
        } catch (Exception e) {
            showError("Modification impossible", "Erreur lors de la modification de l'evenement.", e);
        }
    }

    @FXML
    private void supprimerEvent() {
        if (selectedEvent == null) {
            showError("Suppression impossible", "Selectionne d'abord un evenement a supprimer.", null);
            return;
        }

        try {
            int idToDelete = selectedEvent.getIdEvent();
            String titre = selectedEvent.getTitre();
            eventService.delete(idToDelete);
            refreshAllData();
            clearSelectionAndForm();
            statusLabel.setText("Evenement supprime avec succes : ID " + idToDelete + " - " + titre);
        } catch (Exception e) {
            showError("Suppression impossible", "Erreur lors de la suppression de l'evenement.", e);
        }
    }

    @FXML
    private void viderFormulaire() {
        clearSelectionAndForm();
        statusLabel.setText("Formulaire reinitialise. Pret pour un nouvel evenement.");
    }



    private void populateForm(Event event) {
        titreField.setText(event.getTitre());
        descriptionArea.setText(event.getDescription());
        dateEventPicker.setValue(event.getDateEvent());
        lieuField.setText(event.getLieu());
        capaciteField.setText(String.valueOf(event.getCapacite()));
        typeEventField.setText(event.getTypeEvent());
        imageEventField.setText(event.getImageEvent());
        prixEventField.setText(String.valueOf(event.getPrixEvent()));
        premiumEventCheckBox.setSelected(event.isPremium());
    }

    private void clearSelectionAndForm() {
        selectedEvent = null;
        eventTable.getSelectionModel().clearSelection();
        titreField.clear();
        descriptionArea.clear();
        dateEventPicker.setValue(null);
        lieuField.clear();
        capaciteField.clear();
        typeEventField.clear();
        imageEventField.clear();
        prixEventField.clear();
        premiumEventCheckBox.setSelected(false);
    }

    private Event buildEventFromForm() {
        String titre = requireText(titreField, "Le titre est obligatoire.");
        String description = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();
        LocalDate dateEvent = readDatePickerValue();
        String lieu = requireText(lieuField, "Le lieu est obligatoire.");
        String typeEvent = typeEventField.getText() == null ? "" : typeEventField.getText().trim();
        String imageEvent = imageEventField.getText() == null ? "" : imageEventField.getText().trim();
        int capacite = parseInteger(capaciteField.getText(), "capacite");
        double prix = parseDouble(prixEventField.getText(), "prix");
        boolean premium = premiumEventCheckBox.isSelected();

        return new Event(
                titre,
                description,
                dateEvent,
                lieu,
                capacite,
                typeEvent,
                imageEvent,
                prix,
                premium
        );
    }

    private LocalDate readDatePickerValue() {
        LocalDate selectedDate = dateEventPicker.getValue();
        if (selectedDate != null) {
            return selectedDate;
        }

        String editorText = dateEventPicker.getEditor().getText();
        if (editorText == null || editorText.trim().isEmpty()) {
            throw new IllegalArgumentException("La date de l'evenement est obligatoire. Utilisez le calendrier ou le format jj/mm/aaaa.");
        }

        try {
            LocalDate parsedDate = LocalDate.parse(editorText.trim(), DATE_FORMATTER);
            dateEventPicker.setValue(parsedDate);
            return parsedDate;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Date invalide. Utilisez le format jj/mm/aaaa, par exemple 01/05/2026.");
        }
    }

    private String requireText(TextField field, String errorMessage) {
        String value = field.getText();
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value.trim();
    }

    private int parseInteger(String value, String fieldName) {
        String normalizedValue = value == null ? "" : value.trim();
        if (normalizedValue.isEmpty()) {
            throw new IllegalArgumentException("Le champ " + fieldName + " est obligatoire.");
        }

        try {
            int parsedValue = Integer.parseInt(normalizedValue);
            if (parsedValue < 0) {
                throw new IllegalArgumentException("Le champ " + fieldName + " doit etre positif.");
            }
            return parsedValue;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La valeur du champ " + fieldName + " est invalide. Entrez un nombre entier.");
        }
    }

    private double parseDouble(String value, String fieldName) {
        String normalizedValue = value == null ? "" : value.trim().replace(',', '.');
        if (normalizedValue.isEmpty()) {
            throw new IllegalArgumentException("Le champ " + fieldName + " est obligatoire.");
        }

        try {
            double parsedValue = Double.parseDouble(normalizedValue);
            if (parsedValue < 0) {
                throw new IllegalArgumentException("Le champ " + fieldName + " doit etre positif.");
            }
            return parsedValue;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La valeur du champ " + fieldName + " est invalide. Entrez un nombre decimal.");
        }
    }

    private String formatDate(LocalDate localDate) {
        return localDate == null ? "Date non definie" : DATE_FORMATTER.format(localDate);
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value;
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

    private static final class FormattedTableCell<S, T> extends TableCell<S, T> {
        private final DateTimeFormatter formatter;

        private FormattedTableCell(DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                return;
            }

            if (item instanceof LocalDate localDate) {
                setText(formatter.format(localDate));
            } else if (item instanceof LocalDateTime localDateTime) {
                setText(formatter.format(localDateTime));
            } else {
                setText(item.toString());
            }
        }
    }

    private static final class BooleanTableCell extends TableCell<Event, Boolean> {
        @Override
        protected void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null : (item ? "Oui" : "Non"));
        }
    }

    private static final class PremiumTableCell extends TableCell<Event, String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            getStyleClass().removeAll("premium-table-cell", "standard-table-cell");
            if (empty || item == null) {
                setText(null);
                return;
            }

            setText(item);
            getStyleClass().add("Premium".equals(item) ? "premium-table-cell" : "standard-table-cell");
        }
    }

    private static final class ParticipantStatusTableCell extends TableCell<Participation, String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            getStyleClass().removeAll(
                    "participants-status-badge",
                    "participants-status-confirmed",
                    "participants-status-cancelled",
                    "participants-status-used"
            );
            if (empty || item == null) {
                setText(null);
                setBackground(Background.EMPTY);
                return;
            }
            setText(item);
            getStyleClass().add("participants-status-badge");
            if ("Annulee".equalsIgnoreCase(item)) {
                getStyleClass().add("participants-status-cancelled");
            } else if ("Utilisee".equalsIgnoreCase(item)) {
                getStyleClass().add("participants-status-used");
            } else {
                getStyleClass().add("participants-status-confirmed");
            }
        }
    }

    private static final class NumberTableCell<S> extends TableCell<S, Double> {
        @Override
        protected void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null : String.format(Locale.US, "%.2f", item));
        }
    }
    private void showCrudCards() {
        crudCardsRow.setVisible(true);
        crudCardsRow.setManaged(true);
    }

    private void hideCrudCards() {
        crudCardsRow.setVisible(false);
        crudCardsRow.setManaged(false);
    }

    private void updatePageHero(String title, String subtitle) {
        if (pageTitleLabel != null) {
            pageTitleLabel.setText(title);
        }
        if (pageSubtitleLabel != null) {
            pageSubtitleLabel.setText(subtitle);
        }
    }

    private void openEventWindow(EventWindowMode mode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();

            switch (mode) {
                case CREATE -> controller.showCreateSection();
                case UPDATE -> controller.showUpdateSection();
                case DELETE -> controller.showDeleteSection();
                case DASHBOARD -> controller.showDashboardSection();
            }

            controller.enableStandaloneMode(mode);

            Scene scene = new Scene(root, 1180, 820);
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Fitopia - " + getWindowTitle(mode));
            stage.setScene(scene);
            stage.setMinWidth(980);
            stage.setMinHeight(720);
            stage.show();
        } catch (Exception e) {
            showError("Ouverture impossible", "La fenetre demandee n'a pas pu etre chargee.", e);
        }
    }

    private String getWindowTitle(EventWindowMode mode) {
        return switch (mode) {
            case CREATE -> "Creer un evenement";
            case UPDATE -> "Modifier un evenement";
            case DELETE -> "Supprimer un evenement";
            case DASHBOARD -> "Dashboard des evenements";
        };
    }

    private void enableStandaloneMode(EventWindowMode mode) {
        if (rootPane != null) {
            rootPane.setLeft(null);
        }
        hideNode(sidebarScrollPane);
        hideNode(statsPane);
        hideNode(workspaceSwitcher);
        hideNode(eventsIntroBanner);
        hideCrudCards();

        if (contentShell != null) {
            contentShell.setPadding(new Insets(22, 22, 22, 22));
        }

        configureStandaloneHeader(mode);

        setMainView(false, true, false, false);

        switch (mode) {
            case CREATE -> {
                showEventSection(addEventSection, createCardButton);
                titreField.requestFocus();
            }
            case UPDATE -> showEventSection(updateEventsSection, updateCardButton);
            case DELETE -> showEventSection(deleteEventsSection, deleteCardButton);
            case DASHBOARD -> showEventSection(dashboardSection, dashboardCardButton);
        }
    }

    private void hideNode(Node node) {
        if (node != null) {
            node.setVisible(false);
            node.setManaged(false);
        }
    }

    private void configureStandaloneHeader(EventWindowMode mode) {
        if (heroBanner == null) {
            return;
        }

        heroBanner.setVisible(true);
        heroBanner.setManaged(true);
        if (!heroBanner.getStyleClass().contains("standalone-hero-banner")) {
            heroBanner.getStyleClass().add("standalone-hero-banner");
        }

        switch (mode) {
            case CREATE -> updatePageHero(
                    "Creer un evenement",
                    "Ajoutez un nouvel evenement avec une interface claire et dediee."
            );
            case UPDATE -> updatePageHero(
                    "Modifier un evenement",
                    "Retrouvez vos evenements existants et mettez leurs informations a jour."
            );
            case DELETE -> updatePageHero(
                    "Supprimer un evenement",
                    "Supprimez rapidement un evenement depuis une fenetre dediee."
            );
            case DASHBOARD -> updatePageHero(
                    "Dashboard des evenements",
                    "Explorez, filtrez et suivez tous les evenements dans une vue unique."
            );
        }
    }
}
