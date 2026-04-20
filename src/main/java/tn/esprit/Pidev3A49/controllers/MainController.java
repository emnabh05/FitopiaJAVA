package tn.esprit.Pidev3A49.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import tn.esprit.Pidev3A49.Models.Repas;
import tn.esprit.Pidev3A49.Models.RegimeAlimentaire;
import tn.esprit.Pidev3A49.Models.User;
import tn.esprit.Pidev3A49.services.ServiceRepas;
import tn.esprit.Pidev3A49.services.ServiceRegimeAlimentaire;
import tn.esprit.Pidev3A49.services.ServiceUser;

import java.text.Normalizer;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.embed.swing.SwingFXUtils;

public class MainController {

    private static final LocalTime DEFAULT_REPAS_TIME = LocalTime.of(9, 0);
    private static final String TYPE_PETIT_DEJEUNER = "Petit dejeuner";
    private static final String TYPE_DEJEUNER = "Dejeuner";
    private static final String TYPE_DINER = "Diner";
    private static final String TYPE_COLLATION = "Collation";
    private static final String FILTER_ALL_TYPES = "Tous les types";
    private static final String SORT_RECENT = "Date recente ↓";
    private static final String SORT_OLD = "Date ancienne ↑";
    private static final String SORT_CALORIES_HIGH = "Calories elevees";
    private static final String SORT_CALORIES_LOW = "Calories faibles";
    private static final String SORT_NAME = "Nom A-Z";
    private static final List<String> TYPE_REPAS_OPTIONS = List.of(
            TYPE_PETIT_DEJEUNER, TYPE_DEJEUNER, TYPE_DINER, TYPE_COLLATION
    );
    private static final List<String> SORT_REPAS_OPTIONS = List.of(
            SORT_RECENT, SORT_OLD, SORT_CALORIES_HIGH, SORT_CALORIES_LOW, SORT_NAME
    );
    private static final List<String> TYPE_SANTE_OPTIONS = List.of(
            "normal", "surpoids", "obesite", "sous_poids", "diabetique", "cardiaque", "autre"
    );
    private static final Map<String, String> TYPE_COLOR_BY_LABEL = Map.of(
            TYPE_PETIT_DEJEUNER, "#0d4f49",
            TYPE_DEJEUNER, "#14725d",
            TYPE_DINER, "#1f8d6a",
            TYPE_COLLATION, "#39b77d"
    );

    @FXML private VBox paneBarcodeScanner;
    @FXML private ImageView ivBarcodeWebcam;
    @FXML private Label lblBarcodeStatus;
    @FXML private Label lblBarcodeNutriScore;
    @FXML private Label lblBarcodeProductName;
    @FXML private Label lblBarcodeBrand;
    @FXML private Label lblBarcodeCompatibility;
    @FXML private VBox boxBarcodeResult;

    private Webcam webcam = null;
    private boolean isScanning = false;
    private String lastScannedCode = "";

    @FXML private VBox viewRepas;
    @FXML private VBox viewRegimes;
    @FXML private ScrollPane appScrollPane;
    @FXML private VBox paneRepasCreate;
    @FXML private VBox paneRepasEdit;
    @FXML private VBox paneRepasDelete;
    @FXML private VBox paneRepasExplore;
    @FXML private VBox paneRegimeCreate;
    @FXML private VBox paneRegimeEdit;
    @FXML private VBox paneRegimeDelete;
    @FXML private VBox paneRegimeExplore;
    @FXML private VBox paneScanIA;

    @FXML private Label lblHeaderUser;
    @FXML private Label lblPlannerWelcome;
    @FXML private Label lblPlannerHealthBadge;
    @FXML private Label lblPlannerRegimeTitle;
    @FXML private Label lblPlannerRegimeSubtitle;
    @FXML private Label lblPlannerTargetCalories;
    @FXML private Label lblPlannerBmi;
    @FXML private Label lblPlannerMealsHint;
    @FXML private Label lblPlannerRecommendedMeals;
    @FXML private Label lblPlannerGoalTitle;
    @FXML private Label lblPlannerGoalBody;
    @FXML private Label lblPlannerGoalHint;
    @FXML private ProgressBar pbPlannerCalories;
    @FXML private ProgressBar pbPlannerProteines;
    @FXML private ProgressBar pbPlannerGlucides;
    @FXML private ProgressBar pbPlannerLipides;
    @FXML private Label lblPlannerCaloriesProgress;
    @FXML private Label lblPlannerProteinesProgress;
    @FXML private Label lblPlannerGlucidesProgress;
    @FXML private Label lblPlannerLipidesProgress;
    @FXML private Label lblPlannerMealsSubtitle;
    @FXML private VBox plannerMealsListBox;
    @FXML private Label lblPlannerRegimesSubtitle;
    @FXML private Label lblRepasContextTitle;
    @FXML private Label lblRepasContextBody;
    @FXML private Label lblRepasContextTarget;
    @FXML private Label lblRepasRegimeContext;
    @FXML private Label lblRepasRegimeContextEdit;
    
    @FXML private VBox boxResultatIA;
    @FXML private TextField tfIARepasNomParticulier;
    @FXML private Label lblIACalories;
    @FXML private javafx.scene.image.ImageView ivScanImagePreview;
    @FXML private VBox paneWeeklyProgram;
    @FXML private VBox boxWeeklyDaysContainer;
    @FXML private ProgressBar pbScanProgress;
    @FXML private Button btnAppliquerCalories;
    private String lastScannedCalories = "0";
    private String lastScannedNom = "";
    private int lastScannedP = 0, lastScannedC = 0, lastScannedL = 0;


    @FXML private Button btnModuleRepas;
    @FXML private Button btnModuleRegimes;
    @FXML private Button btnActionCreerRepas;
    @FXML private Button btnActionModifierRepas;
    @FXML private Button btnActionSupprimerRepas;
    @FXML private Button btnActionExplorerRepas;
    @FXML private Button btnActionCreerRegime;
    @FXML private Button btnActionModifierRegime;
    @FXML private Button btnActionSupprimerRegime;
    @FXML private Button btnActionExplorerRegime;

    @FXML private Label lblRegimeUser;
    @FXML private Label lblRegimeTaille;
    @FXML private Label lblRegimePoids;
    @FXML private Label lblRegimeAge;
    @FXML private Label lblRegimeUserEdit;
    @FXML private Label lblRegimeTailleEdit;
    @FXML private Label lblRegimePoidsEdit;
    @FXML private Label lblRegimeAgeEdit;
    @FXML private Label lblPlannerTaille;
    @FXML private Label lblPlannerPoids;
    @FXML private Label lblPlannerAge;
    @FXML private Label lblSummaryKcal;
    @FXML private Label lblSummaryP;
    @FXML private Label lblSummaryC;
    @FXML private Label lblSummaryF;
    @FXML private ComboBox<String> cbRegimeSort;
    @FXML private ComboBox<String> cbRegimeTypeSearch;
    @FXML private TextField tfRegimeSearchGlobal;

    @FXML private TextField tfRepasId;
    @FXML private ComboBox<User> cbRepasUser;
    @FXML private ComboBox<User> cbRepasUserEdit;
    @FXML private DatePicker dpDateRepas;
    @FXML private DatePicker dpDateRepasEdit;
    @FXML private ComboBox<String> cbTypeRepas;
    @FXML private ComboBox<String> cbTypeRepasEdit;
    @FXML private TextField tfRepasNom;
    @FXML private TextField tfRepasNomEdit;
    @FXML private TextArea taRepasDescription;
    @FXML private TextArea taRepasDescriptionEdit;
    @FXML private ComboBox<RegimeAlimentaire> cbRepasRegime;
    @FXML private ComboBox<RegimeAlimentaire> cbRepasRegimeEdit;
    @FXML private TextField tfRepasCalories;
    @FXML private TextField tfRepasCaloriesEdit;
    @FXML private TextField tfRepasProteines;
    @FXML private TextField tfRepasProteinesEdit;
    @FXML private TextField tfRepasGlucides;
    @FXML private TextField tfRepasGlucidesEdit;
    @FXML private TextField tfRepasLipides;
    @FXML private TextField tfRepasLipidesEdit;
    @FXML private Label lblRepasSelectionDelete;
    @FXML private VBox tileRepasEditCards;
    @FXML private VBox boxRepasEditEmpty;
    @FXML private VBox boxRepasEditForm;
    @FXML private VBox tileRepasDeleteCards;
    @FXML private VBox boxRepasDeleteEmpty;
    @FXML private TextField tfRepasSearch;
    @FXML private ComboBox<String> cbRepasExploreTypeFilter;
    @FXML private ComboBox<String> cbRepasExploreSort;
    @FXML private Button btnRepasStatistics;
    @FXML private Label lblRegimeExploreCount;
    @FXML private TilePane tileRegimeCards;
    @FXML private TilePane tileRegimeExploreCards;
    @FXML private Label lblRepasExploreCount;
    @FXML private TilePane tileRepasCards;
    @FXML private VBox boxRepasExploreEmpty;

    @FXML private TableView<Repas> tableRepas;
    @FXML private TableView<Repas> tableRepasEdit;
    @FXML private TableView<Repas> tableRepasDelete;
    @FXML private TableColumn<Repas, Integer> colRepasId;
    @FXML private TableColumn<Repas, String> colRepasUser;
    @FXML private TableColumn<Repas, String> colRepasNom;
    @FXML private TableColumn<Repas, String> colRepasType;
    @FXML private TableColumn<Repas, String> colRepasDate;
    @FXML private TableColumn<Repas, Integer> colRepasCalories;
    @FXML private TableColumn<Repas, Integer> colRepasProteines;
    @FXML private TableColumn<Repas, Integer> colRepasGlucides;
    @FXML private TableColumn<Repas, Integer> colRepasLipides;
    @FXML private TableColumn<Repas, String> colRepasRegimeId;
    @FXML private TableColumn<Repas, Integer> colRepasIdEdit;
    @FXML private TableColumn<Repas, String> colRepasUserEdit;
    @FXML private TableColumn<Repas, String> colRepasNomEdit;
    @FXML private TableColumn<Repas, String> colRepasTypeEdit;
    @FXML private TableColumn<Repas, String> colRepasDateEdit;
    @FXML private TableColumn<Repas, Integer> colRepasCaloriesEdit;
    @FXML private TableColumn<Repas, String> colRepasRegimeIdEdit;
    @FXML private TableColumn<Repas, Integer> colRepasIdDelete;
    @FXML private TableColumn<Repas, String> colRepasUserDelete;
    @FXML private TableColumn<Repas, String> colRepasNomDelete;
    @FXML private TableColumn<Repas, String> colRepasTypeDelete;
    @FXML private TableColumn<Repas, String> colRepasDateDelete;
    @FXML private TableColumn<Repas, Integer> colRepasCaloriesDelete;
    @FXML private TableColumn<Repas, String> colRepasRegimeIdDelete;

    @FXML private TextField tfRegimeId;
    @FXML private ComboBox<User> cbRegimeUser;
    @FXML private ComboBox<User> cbRegimeUserEdit;
    @FXML private ComboBox<String> cbRegimeTypeSante;
    @FXML private ComboBox<String> cbRegimeTypeSanteEdit;
    @FXML private TextField tfRegimeTaille;
    @FXML private TextField tfRegimeTailleEdit;
    @FXML private TextField tfRegimePoids;
    @FXML private TextField tfRegimePoidsEdit;
    @FXML private TextField tfRegimeAge;
    @FXML private TextField tfRegimeAgeEdit;
    @FXML private TextField tfRegimeBmi;
    @FXML private TextField tfRegimeBmiEdit;
    @FXML private TextField tfRegimeCaloriesCibles;
    @FXML private TextField tfRegimeCaloriesCiblesEdit;
    @FXML private TextArea taRegimeRepasAdequats;
    @FXML private TextArea taRegimeRepasAdequatsEdit;
    @FXML private Label lblRegimeSelectionDelete;

    @FXML private TableView<RegimeAlimentaire> tableRegimes;
    @FXML private TableView<RegimeAlimentaire> tableRegimesEdit;
    @FXML private TableView<RegimeAlimentaire> tableRegimesDelete;
    @FXML private TableColumn<RegimeAlimentaire, Integer> colRegimeId;
    @FXML private TableColumn<RegimeAlimentaire, String> colRegimeUser;
    @FXML private TableColumn<RegimeAlimentaire, String> colRegimeType;
    @FXML private TableColumn<RegimeAlimentaire, Integer> colRegimeCalories;
    @FXML private TableColumn<RegimeAlimentaire, Double> colRegimeBmi;
    @FXML private TableColumn<RegimeAlimentaire, Double> colRegimeTaille;
    @FXML private TableColumn<RegimeAlimentaire, Double> colRegimePoids;
    @FXML private TableColumn<RegimeAlimentaire, Integer> colRegimeAge;
    @FXML private TableColumn<RegimeAlimentaire, String> colRegimeRepas;
    @FXML private TableColumn<RegimeAlimentaire, Integer> colRegimeIdEdit;
    @FXML private TableColumn<RegimeAlimentaire, String> colRegimeUserEdit;
    @FXML private TableColumn<RegimeAlimentaire, String> colRegimeTypeEdit;
    @FXML private TableColumn<RegimeAlimentaire, Integer> colRegimeCaloriesEdit;
    @FXML private TableColumn<RegimeAlimentaire, Double> colRegimeBmiEdit;
    @FXML private TableColumn<RegimeAlimentaire, String> colRegimeRepasEdit;
    @FXML private TableColumn<RegimeAlimentaire, Integer> colRegimeIdDelete;
    @FXML private TableColumn<RegimeAlimentaire, String> colRegimeUserDelete;
    @FXML private TableColumn<RegimeAlimentaire, String> colRegimeTypeDelete;
    @FXML private TableColumn<RegimeAlimentaire, Integer> colRegimeCaloriesDelete;
    @FXML private TableColumn<RegimeAlimentaire, Double> colRegimeBmiDelete;
    @FXML private TableColumn<RegimeAlimentaire, String> colRegimeRepasDelete;

    @FXML private TableView<Repas> tableRepasSelection;
    @FXML private TableColumn<Repas, Integer> colRepasSelectionId;
    @FXML private TableColumn<Repas, String> colRepasSelectionNom;
    @FXML private TableColumn<Repas, String> colRepasSelectionType;
    @FXML private TableColumn<Repas, Integer> colRepasSelectionCalories;
    @FXML private TableColumn<Repas, String> colRepasSelectionDate;
    @FXML private TextField tfRepasSelectionSearch;
    @FXML private Label lblRepasSelectionInfo;
    @FXML private TilePane tileRepasSelectionCards;

    @FXML private VBox paneBadgeWinStreak;
    @FXML private Label lblBadgeWinStreakIcon;
    @FXML private Label lblBadgeWinStreakTitle;
    @FXML private Label lblBadgeWinStreakDesc;
    
    @FXML private VBox paneBadgeHydration;
    @FXML private Label lblBadgeHydrationIcon;
    @FXML private Label lblBadgeHydrationTitle;
    @FXML private Label lblBadgeHydrationDesc;
    
    @FXML private VBox paneBadgeCalories;
    @FXML private Label lblBadgeCaloriesIcon;
    @FXML private Label lblBadgeCaloriesTitle;
    @FXML private Label lblBadgeCaloriesDesc;


    private final ServiceRepas serviceRepas = new ServiceRepas();
    private final ServiceRegimeAlimentaire serviceRegime = new ServiceRegimeAlimentaire();
    private final ServiceUser serviceUser = new ServiceUser();
    private final ObservableList<Repas> allRepas = FXCollections.observableArrayList();
    private List<User> allUsers = List.of();
    private List<RegimeAlimentaire> allRegimes = List.of();
    private List<Repas> repasExplorerView = List.of();
    private Integer repasEditSelectionId;
    private int verresEau = 0;
    private final java.util.Map<Integer, List<Repas>> sessionMealsByRegime = new java.util.HashMap<>();




    @FXML
    public void initialize() {
        System.out.println("🚀 [DEBUG] Initialisation du MainController...");
        try {
            initialiserColonnes();
            initialiserCombos();
            initialiserExplorateurRepas();
            initialiserCalculsRegimeAutomatiques();
            initialiserValidationRepas();
            initialiserInteractionsRepasRegime();
            initialiserSelections();
            initialiserSelectionRepasJointure();
            rafraichirDonnees();
            masquerTousLesFormulaires();
            setupRealTimeValidation();
            setupDynamicFilters();
            System.out.println("✅ [DEBUG] Initialisation terminée avec succès.");
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Échec de l'initialisation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupDynamicFilters() {
        if (tfRegimeSearchGlobal != null) {
            tfRegimeSearchGlobal.textProperty().addListener((obs, old, val) -> rafraichirDonnees());
        }
        if (cbRegimeTypeSearch != null) {
            cbRegimeTypeSearch.valueProperty().addListener((obs, old, val) -> rafraichirDonnees());
        }
        if (cbRegimeSort != null) {
            cbRegimeSort.valueProperty().addListener((obs, old, val) -> rafraichirDonnees());
        }
    }


    public void ouvrirBackRepas() {
        afficherModuleRepas();
        afficherCreationRepas();
    }

    public void ouvrirFrontRegimes() {
        afficherModuleRegimes();
        masquerTousLesFormulaires();
    }

    @FXML private void afficherModuleRepas() { afficherVue(viewRepas, viewRegimes); activerBoutonModule(btnModuleRepas, btnModuleRegimes); }
    @FXML private void afficherModuleRegimes() { afficherVue(viewRegimes, viewRepas); activerBoutonModule(btnModuleRegimes, btnModuleRepas); }
    @FXML private void afficherCreationRepas() { afficherPaneRepas(paneRepasCreate, btnActionCreerRepas); }
    @FXML private void afficherModificationRepas() { afficherPaneRepas(paneRepasEdit, btnActionModifierRepas); }
    @FXML private void afficherSuppressionRepas() { afficherPaneRepas(paneRepasDelete, btnActionSupprimerRepas); }
    @FXML private void afficherExplorationRepas() { afficherPaneRepas(paneRepasExplore, btnActionExplorerRepas); }
    @FXML private void afficherCreationRegime() { afficherPaneRegime(paneRegimeCreate, btnActionCreerRegime); }
    @FXML private void afficherModificationRegime() { afficherPaneRegime(paneRegimeEdit, btnActionModifierRegime); }
    @FXML private void afficherSuppressionRegime() { afficherPaneRegime(paneRegimeDelete, btnActionSupprimerRegime); }
    @FXML private void afficherExplorationRegime() { afficherPaneRegime(paneRegimeExplore, btnActionExplorerRegime); }

    @FXML
    private void masquerTousLesFormulaires() {
        // Force unlock interface
        if (appScrollPane != null) appScrollPane.setMouseTransparent(false);
        if (paneOverlay != null) paneOverlay.setMouseTransparent(true);
        
        if (paneRegimeCreate != null) { paneRegimeCreate.setVisible(false); paneRegimeCreate.setManaged(false); }
        if (paneRegimeEdit != null) { paneRegimeEdit.setVisible(false); paneRegimeEdit.setManaged(false); }
        if (paneRegimeDelete != null) { paneRegimeDelete.setVisible(false); paneRegimeDelete.setManaged(false); }
        if (paneRegimeExplore != null) { paneRegimeExplore.setVisible(false); paneRegimeExplore.setManaged(false); }
        if (paneRepasCreate != null) { paneRepasCreate.setVisible(false); paneRepasCreate.setManaged(false); }
        if (paneRepasEdit != null) { paneRepasEdit.setVisible(false); paneRepasEdit.setManaged(false); }
        if (paneRepasDelete != null) { paneRepasDelete.setVisible(false); paneRepasDelete.setManaged(false); }
        if (paneRepasExplore != null) { paneRepasExplore.setVisible(false); paneRepasExplore.setManaged(false); }
        if (paneScanIA != null) { paneScanIA.setVisible(false); paneScanIA.setManaged(false); }
        if (paneBarcodeScanner != null) { paneBarcodeScanner.setVisible(false); paneBarcodeScanner.setManaged(false); }
        if (paneWeeklyProgram != null) { paneWeeklyProgram.setVisible(false); paneWeeklyProgram.setManaged(false); }
    }

    @FXML
    private void rafraichirDonnees() {
        System.out.println("🔄 [DEBUG] Rafraîchissement des données...");
        List<User> users = serviceUser.getAll();
        List<RegimeAlimentaire> regimes = serviceRegime.getAll();
        List<Repas> repas = serviceRepas.getAll();
        
        allUsers = (users != null) ? List.copyOf(users) : List.of();
        allRegimes = (regimes != null) ? List.copyOf(regimes) : List.of();

        if (cbRepasUser != null) cbRepasUser.setItems(FXCollections.observableArrayList(allUsers));
        if (cbRepasUserEdit != null) cbRepasUserEdit.setItems(FXCollections.observableArrayList(allUsers));
        if (cbRegimeUser != null) cbRegimeUser.setItems(FXCollections.observableArrayList(allUsers));
        if (cbRegimeUserEdit != null) cbRegimeUserEdit.setItems(FXCollections.observableArrayList(allUsers));

        if (cbRepasRegime != null) cbRepasRegime.setItems(FXCollections.observableArrayList(allRegimes));
        if (cbRepasRegimeEdit != null) cbRepasRegimeEdit.setItems(FXCollections.observableArrayList(allRegimes));

        allRepas.setAll(repas != null ? repas : List.of());
        if (tableRepas != null) tableRepas.setItems(FXCollections.observableArrayList(allRepas));
        if (tableRepasEdit != null) tableRepasEdit.setItems(FXCollections.observableArrayList(allRepas));
        if (tableRepasDelete != null) tableRepasDelete.setItems(FXCollections.observableArrayList(allRepas));
        restaurerSelectionRepasEdit(allRepas);

        if (tableRegimes != null) tableRegimes.setItems(FXCollections.observableArrayList(allRegimes));
        if (tableRegimesEdit != null) tableRegimesEdit.setItems(FXCollections.observableArrayList(allRegimes));
        if (tableRegimesDelete != null) tableRegimesDelete.setItems(FXCollections.observableArrayList(allRegimes));
        
        System.out.println("✨ [DEBUG] Données rafraîchies : " + allUsers.size() + " users, " + allRegimes.size() + " régimes, " + allRepas.size() + " repas.");

        if (cbRegimeSort != null && cbRegimeSort.getItems().isEmpty()) {
            cbRegimeSort.setItems(FXCollections.observableArrayList("Calories ↓", "Calories ↑", "Recent"));
            cbRegimeSort.setValue("Calories ↓");
        }
        if (cbRegimeTypeSearch != null && cbRegimeTypeSearch.getItems().isEmpty()) {
            List<String> types = new java.util.ArrayList<>(List.of("Tous les types"));
            types.addAll(TYPE_SANTE_OPTIONS);
            cbRegimeTypeSearch.setItems(FXCollections.observableArrayList(types));
            cbRegimeTypeSearch.setValue("Tous les types");
        }

        actualiserModificationRepas();
        actualiserSuppressionRepas();
        actualiserExplorateurRepas();
        actualiserDashboardPlanner();
        actualiserContexteRepasSelectionne(false);
        actualiserContexteRepasSelectionne(true);
        actualiserSelectionRepasJointure();
        
        // Refresh Diet list in Dashboard
        if (tileRegimeCards != null) {
            String search = tfRegimeSearchGlobal == null ? "" : tfRegimeSearchGlobal.getText().toLowerCase();
            String typeFilter = cbRegimeTypeSearch == null ? "Tous les types" : cbRegimeTypeSearch.getValue();
            String sortValue = cbRegimeSort == null ? "Recent" : cbRegimeSort.getValue();
            
            List<RegimeAlimentaire> filtered = new java.util.ArrayList<>(regimes.stream()
                .filter(r -> search.isEmpty() || (r.getTypeSante() != null && r.getTypeSante().toLowerCase().contains(search)) || String.valueOf(r.getId()).contains(search))
                .filter(r -> typeFilter == null || typeFilter.equals("Tous les types") || typeFilter.equals(r.getTypeSante()))
                .toList());

            if ("Calories ↓".equals(sortValue)) {
                filtered.sort((a,b) -> Integer.compare(b.getCaloriesCibles()==null?0:b.getCaloriesCibles(), a.getCaloriesCibles()==null?0:a.getCaloriesCibles()));
            } else if ("Calories ↑".equals(sortValue)) {
                filtered.sort((a,b) -> Integer.compare(a.getCaloriesCibles()==null?0:a.getCaloriesCibles(), b.getCaloriesCibles()==null?0:b.getCaloriesCibles()));
            } else {
                filtered.sort((a,b) -> Integer.compare(b.getId(), a.getId()));
            }
            
            tileRegimeCards.getChildren().setAll(filtered.stream().map(this::creerCarteRegime).toList());
        }
    }

    private void initialiserSelectionRepasJointure() {
        if (tableRepasSelection == null) return;
        
        colRepasSelectionId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRepasSelectionNom.setCellValueFactory(new PropertyValueFactory<>("nomRepas"));
        colRepasSelectionType.setCellValueFactory(new PropertyValueFactory<>("typeRepas"));
        colRepasSelectionCalories.setCellValueFactory(new PropertyValueFactory<>("calories"));
        colRepasSelectionDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateDisplay()));
        
        tableRepasSelection.getSelectionModel().selectedItemProperty().addListener((obs, old, newValue) -> {
            if (lblRepasSelectionInfo != null) {
                lblRepasSelectionInfo.setText(newValue == null ? "Aucun repas sélectionné" : "Repas choisi: " + newValue.getNomRepas());
            }
        });
        
        if (tfRepasSelectionSearch != null) {
            tfRepasSelectionSearch.textProperty().addListener((obs, old, newValue) -> {
                if (newValue == null || newValue.isBlank()) {
                    tableRepasSelection.setItems(allRepas);
                } else {
                    tableRepasSelection.setItems(allRepas.filtered(repas -> 
                        (repas.getNomRepas() != null && repas.getNomRepas().toLowerCase().contains(newValue.toLowerCase())) ||
                        (repas.getTypeRepas() != null && repas.getTypeRepas().toLowerCase().contains(newValue.toLowerCase()))
                    ));
                }
            });
        }
    }

    private void actualiserSelectionRepasJointure() {
        if (tileRepasSelectionCards == null) return;
        
        RegimeAlimentaire activeRegime = getActualActiveRegime();
        final Integer activeRid = activeRegime != null ? activeRegime.getId() : null;
        
        String search = tfRepasSelectionSearch == null ? "" : tfRepasSelectionSearch.getText();
        List<Repas> selectionList = allRepas.stream()
                .filter(repas -> activeRid == null || (repas.getRegimeId() != null && repas.getRegimeId().equals(activeRid)))
                .filter(repas -> correspondRechercheRepas(repas, search))
                .limit(40) 
                .toList();

        tileRepasSelectionCards.getChildren().setAll(selectionList.stream()
                .map(this::creerCarteSelectionRepas)
                .toList());
    }

    @FXML
    private void associerRepasAuRegime() {
        try {
            Repas repas = tableRepasSelection.getSelectionModel().getSelectedItem();
            if (repas == null) throw new IllegalArgumentException("Veuillez sélectionner un repas dans la liste.");
            clonerRepasPourAujourdhui(repas);
        } catch (Exception exception) {
            showError("Liaison Repas", exception.getMessage());
        }
    }

    private void clonerRepasPourAujourdhui(Repas source) {
        try {
            User refUser = determinerUtilisateurReference();
            RegimeAlimentaire activeRegime = getActualActiveRegime();
            
            if (activeRegime == null) throw new IllegalStateException("Aucun regime actif trouve. Creez-en un d'abord.");
            
            Repas nouveau = new Repas(
                refUser.getId(),
                LocalDateTime.now(),
                source.getTypeRepas(),
                source.getNomRepas(),
                source.getCalories(),
                source.getProteines(),
                source.getGlucides(),
                source.getLipides(),
                "Ajoute via historique",
                activeRegime.getId()
            );
            
            fusionnerRepasSiExiste(nouveau);
            rafraichirDonnees();
            showInfo("Calories ajoutées à votre suivi !");
            masquerTousLesFormulaires();
        } catch (Exception exception) {
            showError("Erreur ajout", exception.getMessage());
        }
    }

    @FXML
    private void ajouterRepas() {
        try {
            fusionnerRepasSiExiste(construireRepas(false));
            rafraichirDonnees();
            viderFormulaireRepas();
            showInfo("Données nutritionnelles mises à jour.");
            masquerTousLesFormulaires();
        } catch (Exception exception) {
            showError("Erreur repas", exception.getMessage());
        }
    }

    @FXML
    private void modifierRepas() {
        try {
            if (tfRepasId.getText().isBlank()) throw new IllegalArgumentException("Selectionnez un repas a modifier.");
            Repas repas = construireRepas(true);
            repas.setId(Integer.parseInt(tfRepasId.getText()));
            serviceRepas.update(repas);
            rafraichirDonnees();
            viderFormulaireRepasModification();
            showInfo("Repas mis a jour avec succes.");
        } catch (Exception exception) {
            showError("Erreur repas", exception.getMessage());
        }
    }

    @FXML
    private void supprimerRepas() {
        try {
            Repas repas = tableRepasDelete.getSelectionModel().getSelectedItem();
            if (repas == null) throw new IllegalArgumentException("Selectionnez un repas a supprimer.");
            supprimerRepasSelectionne(repas);
        } catch (Exception exception) {
            showError("Erreur repas", exception.getMessage());
        }
    }

    @FXML
    private void ajouterRegime() {
        try {
            serviceRegime.add(construireRegime(false));
            rafraichirDonnees();
            viderFormulaireRegime();
            showInfo("Regime ajoute avec succes.");
            masquerTousLesFormulaires();
        } catch (Exception exception) {
            showError("Erreur regime", exception.getMessage());
        }
    }

    @FXML
    private void modifierRegime() {
        try {
            if (tfRegimeId.getText().isBlank()) throw new IllegalArgumentException("Selectionnez un regime a modifier.");
            RegimeAlimentaire regime = construireRegime(true);
            regime.setId(Integer.parseInt(tfRegimeId.getText()));
            serviceRegime.update(regime);
            rafraichirDonnees();
            viderFormulaireRegimeModification();
            showInfo("Regime mis a jour avec succes.");
            masquerTousLesFormulaires();
        } catch (Exception exception) {
            showError("Erreur regime", exception.getMessage());
        }
    }

    @FXML
    private void supprimerRegime() {
        try {
            RegimeAlimentaire regime = tableRegimesDelete.getSelectionModel().getSelectedItem();
            if (regime == null) throw new IllegalArgumentException("Selectionnez un regime a supprimer.");
            serviceRegime.delete(regime);
            rafraichirDonnees();
            viderFormulaireRegimeModification();
            lblRegimeSelectionDelete.setText("Aucun regime selectionne");
            showInfo("Regime supprime avec succes.");
        } catch (Exception exception) {
            showError("Erreur regime", exception.getMessage());
        }
    }

    @FXML
    private void ajouterVerreEau() {
        if (verresEau < 5) {
            verresEau++;
            actualiserDashboardPlanner();
        }
    }


    private void fusionnerRepasSiExiste(Repas nouveau) {
        if (nouveau == null || nouveau.getRegimeId() == null) return;
        sessionMealsByRegime.computeIfAbsent(nouveau.getRegimeId(), k -> new java.util.ArrayList<>()).add(nouveau);
    }

    private Integer safeAdd(Integer a, Integer b) {
        return ((a == null) ? 0 : a) + ((b == null) ? 0 : b);
    }

    @FXML
    private void viderFormulaireRepas() {
        if (cbRepasUser != null) cbRepasUser.getSelectionModel().clearSelection();
        if (dpDateRepas != null) dpDateRepas.setValue(null);
        if (cbTypeRepas != null) cbTypeRepas.getSelectionModel().clearSelection();
        if (tfRepasNom != null) tfRepasNom.clear();
        if (taRepasDescription != null) taRepasDescription.clear();
        if (cbRepasRegime != null) cbRepasRegime.getSelectionModel().clearSelection();
        if (tfRepasCalories != null) tfRepasCalories.clear();
        if (tfRepasProteines != null) tfRepasProteines.clear();
        if (tfRepasGlucides != null) tfRepasGlucides.clear();
        if (tfRepasLipides != null) tfRepasLipides.clear();
    }

    @FXML
    private void viderFormulaireRepasModification() {
        tfRepasId.clear();
        cbRepasUserEdit.getSelectionModel().clearSelection();
        dpDateRepasEdit.setValue(null);
        cbTypeRepasEdit.getSelectionModel().clearSelection();
        tfRepasNomEdit.clear();
        taRepasDescriptionEdit.clear();
        cbRepasRegimeEdit.getSelectionModel().clearSelection();
        tfRepasCaloriesEdit.clear();
        tfRepasProteinesEdit.clear();
        tfRepasGlucidesEdit.clear();
        tfRepasLipidesEdit.clear();
        repasEditSelectionId = null;
        tableRepasEdit.getSelectionModel().clearSelection();
        if (boxRepasEditForm != null) {
            boxRepasEditForm.setManaged(false);
            boxRepasEditForm.setVisible(false);
        }
        actualiserModificationRepas();
    }

    @FXML
    private void viderFormulaireRegime() {
        if (cbRegimeUser != null) cbRegimeUser.getSelectionModel().clearSelection();
        if (tfRegimeTaille != null) tfRegimeTaille.clear();
        if (tfRegimePoids != null) tfRegimePoids.clear();
        if (tfRegimeAge != null) tfRegimeAge.clear();
        if (tfRegimeBmi != null) tfRegimeBmi.clear();
        if (cbRegimeTypeSante != null) cbRegimeTypeSante.getSelectionModel().clearSelection();
        if (tfRegimeCaloriesCibles != null) tfRegimeCaloriesCibles.clear();
        if (taRegimeRepasAdequats != null) taRegimeRepasAdequats.clear();
        masquerTousLesFormulaires();
    }

    @FXML
    private void viderFormulaireRegimeModification() {
        if (tfRegimeId != null) tfRegimeId.clear();
        if (cbRegimeUserEdit != null) cbRegimeUserEdit.getSelectionModel().clearSelection();
        if (tfRegimeTailleEdit != null) tfRegimeTailleEdit.clear();
        if (tfRegimePoidsEdit != null) tfRegimePoidsEdit.clear();
        if (tfRegimeAgeEdit != null) tfRegimeAgeEdit.clear();
        if (tfRegimeBmiEdit != null) tfRegimeBmiEdit.clear();
        if (cbRegimeTypeSanteEdit != null) cbRegimeTypeSanteEdit.getSelectionModel().clearSelection();
        if (tfRegimeCaloriesCiblesEdit != null) tfRegimeCaloriesCiblesEdit.clear();
        if (taRegimeRepasAdequatsEdit != null) taRegimeRepasAdequatsEdit.clear();
        masquerTousLesFormulaires();
    }

    private void initialiserColonnes() {
        if (colRepasId != null && colRepasUser != null && colRepasNom != null && colRepasType != null && colRepasDate != null && colRepasCalories != null) {
            initialiserColonnesRepas(colRepasId, colRepasUser, colRepasNom, colRepasType, colRepasDate, colRepasCalories, colRepasRegimeId);
        }
        if (colRepasIdEdit != null && colRepasUserEdit != null && colRepasNomEdit != null && colRepasTypeEdit != null && colRepasDateEdit != null && colRepasCaloriesEdit != null) {
            initialiserColonnesRepas(colRepasIdEdit, colRepasUserEdit, colRepasNomEdit, colRepasTypeEdit, colRepasDateEdit, colRepasCaloriesEdit, colRepasRegimeIdEdit);
        }
        if (colRepasIdDelete != null && colRepasUserDelete != null && colRepasNomDelete != null && colRepasTypeDelete != null && colRepasDateDelete != null && colRepasCaloriesDelete != null) {
            initialiserColonnesRepas(colRepasIdDelete, colRepasUserDelete, colRepasNomDelete, colRepasTypeDelete, colRepasDateDelete, colRepasCaloriesDelete, colRepasRegimeIdDelete);
        }
        
        if (colRepasProteines != null) colRepasProteines.setCellValueFactory(new PropertyValueFactory<>("proteines"));
        if (colRepasGlucides != null) colRepasGlucides.setCellValueFactory(new PropertyValueFactory<>("glucides"));
        if (colRepasLipides != null) colRepasLipides.setCellValueFactory(new PropertyValueFactory<>("lipides"));

        if (colRegimeId != null && colRegimeUser != null && colRegimeType != null && colRegimeCalories != null && colRegimeBmi != null && colRegimeRepas != null) {
             initialiserColonnesRegimes(colRegimeId, colRegimeUser, colRegimeType, colRegimeCalories, colRegimeBmi, colRegimeRepas);
        }
        if (colRegimeIdEdit != null && colRegimeUserEdit != null && colRegimeTypeEdit != null && colRegimeCaloriesEdit != null && colRegimeBmiEdit != null && colRegimeRepasEdit != null) {
            initialiserColonnesRegimes(colRegimeIdEdit, colRegimeUserEdit, colRegimeTypeEdit, colRegimeCaloriesEdit, colRegimeBmiEdit, colRegimeRepasEdit);
        }
        if (colRegimeIdDelete != null && colRegimeUserDelete != null && colRegimeTypeDelete != null && colRegimeCaloriesDelete != null && colRegimeBmiDelete != null && colRegimeRepasDelete != null) {
            initialiserColonnesRegimes(colRegimeIdDelete, colRegimeUserDelete, colRegimeTypeDelete, colRegimeCaloriesDelete, colRegimeBmiDelete, colRegimeRepasDelete);
        }
        
        if (colRegimeTaille != null) colRegimeTaille.setCellValueFactory(new PropertyValueFactory<>("taille"));
        if (colRegimePoids != null) colRegimePoids.setCellValueFactory(new PropertyValueFactory<>("poids"));
        if (colRegimeAge != null) colRegimeAge.setCellValueFactory(new PropertyValueFactory<>("age"));
    }

    private void setupRealTimeValidation() {
        if (tfRegimeTaille != null) tfRegimeTaille.textProperty().addListener((o, v, n) -> mettreAJourStyleValidation(lblRegimeTaille, n, 1, 300));
        if (tfRegimePoids != null) tfRegimePoids.textProperty().addListener((o, v, n) -> mettreAJourStyleValidation(lblRegimePoids, n, 1, 300));
        if (tfRegimeAge != null) tfRegimeAge.textProperty().addListener((o, v, n) -> mettreAJourStyleValidation(lblRegimeAge, n, 1, 100));
        if (cbRegimeUser != null) cbRegimeUser.valueProperty().addListener((o, v, n) -> mettreAJourStyleValidation(lblRegimeUser, n != null));

        if (tfRegimeTailleEdit != null) tfRegimeTailleEdit.textProperty().addListener((o, v, n) -> mettreAJourStyleValidation(lblRegimeTailleEdit, n, 1, 300));
        if (tfRegimePoidsEdit != null) tfRegimePoidsEdit.textProperty().addListener((o, v, n) -> mettreAJourStyleValidation(lblRegimePoidsEdit, n, 1, 300));
        if (tfRegimeAgeEdit != null) tfRegimeAgeEdit.textProperty().addListener((o, v, n) -> mettreAJourStyleValidation(lblRegimeAgeEdit, n, 1, 100));
        if (cbRegimeUserEdit != null) cbRegimeUserEdit.valueProperty().addListener((o, v, n) -> mettreAJourStyleValidation(lblRegimeUserEdit, n != null));
    }

    private void mettreAJourStyleValidation(Label label, String value, double min, double max) {
        if (label == null) return;
        try {
            double val = Double.parseDouble(value);
            boolean valid = val >= min && val <= max;
            label.setStyle("-fx-text-fill: " + (valid ? "#10b981" : "#ef4444") + "; -fx-font-weight: 800;");
        } catch (Exception e) {
            label.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 800;");
        }
    }

    private void mettreAJourStyleValidation(Label label, boolean valid) {
        if (label == null) return;
        label.setStyle("-fx-text-fill: " + (valid ? "#10b981" : "#ef4444") + "; -fx-font-weight: 800;");
    }

    private void initialiserColonnesRepas(TableColumn<Repas, Integer> id,
                                          TableColumn<Repas, String> user,
                                          TableColumn<Repas, String> nom,
                                          TableColumn<Repas, String> type,
                                          TableColumn<Repas, String> date,
                                          TableColumn<Repas, Integer> calories,
                                          TableColumn<Repas, String> regime) {
        if (id != null) id.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (user != null) user.setCellValueFactory(new PropertyValueFactory<>("userEmail"));
        if (nom != null) nom.setCellValueFactory(new PropertyValueFactory<>("nomRepas"));
        if (type != null) type.setCellValueFactory(cellData -> new SimpleStringProperty(normaliserTypeRepas(cellData.getValue().getTypeRepas())));
        if (date != null) date.setCellValueFactory(new PropertyValueFactory<>("dateDisplay"));
        if (calories != null) calories.setCellValueFactory(new PropertyValueFactory<>("calories"));
        if (regime != null) regime.setCellValueFactory(new PropertyValueFactory<>("regimeDisplay"));
    }

    private void initialiserColonnesRegimes(TableColumn<RegimeAlimentaire, Integer> id,
                                            TableColumn<RegimeAlimentaire, String> user,
                                            TableColumn<RegimeAlimentaire, String> type,
                                            TableColumn<RegimeAlimentaire, Integer> calories,
                                            TableColumn<RegimeAlimentaire, Double> bmi,
                                            TableColumn<RegimeAlimentaire, String> repas) {
        if (id != null) id.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (user != null) user.setCellValueFactory(new PropertyValueFactory<>("userEmail"));
        if (type != null) type.setCellValueFactory(new PropertyValueFactory<>("typeSante"));
        if (calories != null) calories.setCellValueFactory(new PropertyValueFactory<>("caloriesCibles"));
        if (bmi != null) bmi.setCellValueFactory(new PropertyValueFactory<>("bmi"));
        if (repas != null) repas.setCellValueFactory(new PropertyValueFactory<>("repasAdequats"));
    }

    private void initialiserCombos() {
        if (cbTypeRepas != null) cbTypeRepas.setItems(FXCollections.observableArrayList(TYPE_REPAS_OPTIONS));
        if (cbTypeRepasEdit != null) cbTypeRepasEdit.setItems(FXCollections.observableArrayList(TYPE_REPAS_OPTIONS));
        if (cbRegimeTypeSante != null) cbRegimeTypeSante.setItems(FXCollections.observableArrayList(TYPE_SANTE_OPTIONS));
        if (cbRegimeTypeSanteEdit != null) cbRegimeTypeSanteEdit.setItems(FXCollections.observableArrayList(TYPE_SANTE_OPTIONS));
        if (cbRepasExploreTypeFilter != null) {
            cbRepasExploreTypeFilter.setItems(FXCollections.observableArrayList(FILTER_ALL_TYPES));
            cbRepasExploreTypeFilter.getItems().addAll(TYPE_REPAS_OPTIONS);
            cbRepasExploreTypeFilter.setValue(FILTER_ALL_TYPES);
        }
        if (cbRepasExploreSort != null) {
            cbRepasExploreSort.setItems(FXCollections.observableArrayList(SORT_REPAS_OPTIONS));
            cbRepasExploreSort.setValue(SORT_RECENT);
        }

        StringConverter<User> userConverter = new StringConverter<>() {
            @Override public String toString(User user) { return user == null ? "" : user.getDisplayName(); }
            @Override public User fromString(String string) { return null; }
        };
        if (cbRepasUser != null) cbRepasUser.setConverter(userConverter);
        if (cbRepasUserEdit != null) cbRepasUserEdit.setConverter(userConverter);
        if (cbRegimeUser != null) cbRegimeUser.setConverter(userConverter);
        if (cbRegimeUserEdit != null) cbRegimeUserEdit.setConverter(userConverter);

        StringConverter<RegimeAlimentaire> regimeConverter = new StringConverter<>() {
            @Override public String toString(RegimeAlimentaire regime) { return regime == null ? "" : regime.getDisplayLabel(); }
            @Override public RegimeAlimentaire fromString(String string) { return null; }
        };
        if (cbRepasRegime != null) cbRepasRegime.setConverter(regimeConverter);
        if (cbRepasRegimeEdit != null) cbRepasRegimeEdit.setConverter(regimeConverter);
    }

    private void initialiserCalculsRegimeAutomatiques() {
        configurerCalculAutomatiqueRegime(tfRegimeTaille, tfRegimePoids, tfRegimeAge, tfRegimeBmi, cbRegimeTypeSante, tfRegimeCaloriesCibles, taRegimeRepasAdequats);
        configurerCalculAutomatiqueRegime(tfRegimeTailleEdit, tfRegimePoidsEdit, tfRegimeAgeEdit, tfRegimeBmiEdit, cbRegimeTypeSanteEdit, tfRegimeCaloriesCiblesEdit, taRegimeRepasAdequatsEdit);
    }

    private void initialiserValidationRepas() {
        configurerValidationRepas(tfRepasNom, taRepasDescription, tfRepasProteines, tfRepasGlucides, tfRepasLipides, tfRepasCalories);
        configurerValidationRepas(tfRepasNomEdit, taRepasDescriptionEdit, tfRepasProteinesEdit, tfRepasGlucidesEdit, tfRepasLipidesEdit, tfRepasCaloriesEdit);
    }

    private void configurerValidationRepas(TextField nomField, TextArea descField, TextField protField, TextField glucField, TextField lipField, TextField calField) {
        if (nomField == null || descField == null || protField == null || glucField == null || lipField == null || calField == null) return;
        
        calField.setEditable(false);

        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() < 5) {
                nomField.setStyle("-fx-border-color: #bb2d22; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-font-weight: 900;");
            } else {
                nomField.setStyle("-fx-border-color: #168163; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-font-weight: 900;");
            }
        });

        descField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() < 10) {
                descField.setStyle("-fx-border-color: #bb2d22; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-font-weight: 900;");
            } else {
                descField.setStyle("-fx-border-color: #168163; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-font-weight: 900;");
            }
        });

        Runnable calculCalories = () -> {
            try {
                int prot = protField.getText().isEmpty() ? 0 : Integer.parseInt(protField.getText());
                int gluc = glucField.getText().isEmpty() ? 0 : Integer.parseInt(glucField.getText());
                int lip = lipField.getText().isEmpty() ? 0 : Integer.parseInt(lipField.getText());
                int total = (prot * 4) + (gluc * 4) + (lip * 9);
                calField.setText(String.valueOf(total));
            } catch (NumberFormatException ignored) { }
        };

        protField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                protField.setText(newValue.replaceAll("\\D", ""));
            } else {
                calculCalories.run();
                protField.setStyle(newValue.isEmpty() ? "-fx-border-color: #bb2d22; -fx-font-weight: 900;" : "-fx-border-color: #168163; -fx-font-weight: 900;");
            }
        });

        glucField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                glucField.setText(newValue.replaceAll("\\D", ""));
            } else {
                calculCalories.run();
                glucField.setStyle(newValue.isEmpty() ? "-fx-border-color: #bb2d22; -fx-font-weight: 900;" : "-fx-border-color: #168163; -fx-font-weight: 900;");
            }
        });

        lipField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                lipField.setText(newValue.replaceAll("\\D", ""));
            } else {
                calculCalories.run();
                lipField.setStyle(newValue.isEmpty() ? "-fx-border-color: #bb2d22; -fx-font-weight: 900;" : "-fx-border-color: #168163; -fx-font-weight: 900;");
            }
        });
    }

    private void configurerCalculAutomatiqueRegime(TextField tailleField,
                                                   TextField poidsField,
                                                   TextField ageField,
                                                   TextField bmiField,
                                                   ComboBox<String> typeField,
                                                   TextField caloriesField,
                                                   TextArea repasField) {
        if (tailleField == null || poidsField == null || ageField == null || bmiField == null || typeField == null || caloriesField == null || repasField == null) {
            return;
        }
        Runnable refresh = () -> mettreAJourCalculRegime(tailleField, poidsField, ageField, bmiField, typeField, caloriesField, repasField);
        tailleField.textProperty().addListener((observable, oldValue, newValue) -> refresh.run());
        poidsField.textProperty().addListener((observable, oldValue, newValue) -> refresh.run());
        ageField.textProperty().addListener((observable, oldValue, newValue) -> refresh.run());
    }

    private void mettreAJourCalculRegime(TextField tailleField,
                                         TextField poidsField,
                                         TextField ageField,
                                         TextField bmiField,
                                         ComboBox<String> typeField,
                                         TextField caloriesField,
                                         TextArea repasField) {
        Double taille = parseDoubleSafe(tailleField.getText());
        Double poids = parseDoubleSafe(poidsField.getText());
        Integer age = parseIntegerSafe(ageField.getText());
        if (taille == null || poids == null || taille <= 0 || poids <= 0) {
            bmiField.clear();
            if (caloriesField != null) {
                caloriesField.clear();
            }
            if (repasField != null && (repasField.getText() == null || repasField.getText().isBlank())) {
                repasField.clear();
            }
            return;
        }

        double bmi = poids / Math.pow(taille / 100.0, 2);
        String typeSante = determinerTypeSanteAutomatique(bmi);
        int calories = calculerCaloriesCibles(poids, taille, age, bmi);

        bmiField.setText(String.format(Locale.US, "%.1f", bmi));
        typeField.setValue(typeSante);
        caloriesField.setText(String.valueOf(calories));
        repasField.setText(genererRepasAdequats(typeSante, age));
    }

    private void initialiserInteractionsRepasRegime() {
        if (cbRepasUser != null) {
            cbRepasUser.valueProperty().addListener((observable, oldValue, newValue) -> {
                filtrerRegimesPourUtilisateur(newValue, cbRepasRegime);
                actualiserContexteRepasSelectionne(false);
            });
        }
        if (cbRepasUserEdit != null) {
            cbRepasUserEdit.valueProperty().addListener((observable, oldValue, newValue) -> {
                filtrerRegimesPourUtilisateur(newValue, cbRepasRegimeEdit);
                actualiserContexteRepasSelectionne(true);
            });
        }
        if (cbRepasRegime != null) {
            cbRepasRegime.valueProperty().addListener((observable, oldValue, newValue) -> actualiserContexteRepasSelectionne(false));
        }
        if (cbRepasRegimeEdit != null) {
            cbRepasRegimeEdit.valueProperty().addListener((observable, oldValue, newValue) -> actualiserContexteRepasSelectionne(true));
        }
    }

    private void filtrerRegimesPourUtilisateur(User user, ComboBox<RegimeAlimentaire> comboBox) {
        if (comboBox == null) {
            return;
        }
        Integer currentRegimeId = comboBox.getValue() == null ? null : comboBox.getValue().getId();
        List<RegimeAlimentaire> filtered = allRegimes.stream()
                .filter(regime -> user == null || (regime.getUserId() != null && regime.getUserId() == user.getId()))
                .toList();
        comboBox.setItems(FXCollections.observableArrayList(filtered));
        if (currentRegimeId != null) {
            selectionnerRegime(comboBox, currentRegimeId);
        }
        if (comboBox.getValue() == null && !filtered.isEmpty()) {
            comboBox.setValue(filtered.get(0));
        }
    }

    private void initialiserExplorateurRepas() {
        if (tfRepasSearch != null) {
            tfRepasSearch.textProperty().addListener((observable, oldValue, newValue) -> actualiserExplorateurRepas());
        }
        if (cbRepasExploreTypeFilter != null) {
            cbRepasExploreTypeFilter.valueProperty().addListener((observable, oldValue, newValue) -> actualiserExplorateurRepas());
        }
        if (cbRepasExploreSort != null) {
            cbRepasExploreSort.valueProperty().addListener((observable, oldValue, newValue) -> actualiserExplorateurRepas());
        }
    }

    private void initialiserSelections() {
        if (tableRepasEdit != null) {
            tableRepasEdit.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                repasEditSelectionId = newValue == null ? null : newValue.getId();
                if (newValue != null) {
                    remplirFormulaireRepas(newValue);
                }
                if (boxRepasEditForm != null) {
                    boolean hasSelection = newValue != null;
                    boxRepasEditForm.setManaged(hasSelection);
                    boxRepasEditForm.setVisible(hasSelection);
                }
                actualiserModificationRepas();
            });
        }
        if (tableRegimesEdit != null) {
            tableRegimesEdit.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) remplirFormulaireRegime(newValue);
            });
        }
        if (tableRegimesDelete != null) {
            tableRegimesDelete.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                    lblRegimeSelectionDelete.setText(newValue == null ? "Aucun regime selectionne" : "Regime selectionne: #" + newValue.getId()));
        }
    }

    private void actualiserDashboardPlanner() {
        User referenceUser = determinerUtilisateurReference();
        RegimeAlimentaire activeRegime = getActualActiveRegime();
        List<Repas> repasDuJour = (activeRegime == null) ? new java.util.ArrayList<>() : 
                sessionMealsByRegime.getOrDefault(activeRegime.getId(), new java.util.ArrayList<>());


        
        List<Repas> repasAffiches = repasDuJour;

        if (lblHeaderUser != null) {
            lblHeaderUser.setText(referenceUser == null ? "Nutrition profile" : valeurOuDefaut(referenceUser.getEmail(), "Nutrition profile"));
        }
        if (lblPlannerWelcome != null) {
            lblPlannerWelcome.setText(referenceUser == null
                    ? "Créez un régime puis reliez vos repas pour lancer le suivi nutritionnel."
                    : "Page personnalisée pour " + valeurOuDefaut(referenceUser.getEmail(), "utilisateur") + ".");
        }

        if (activeRegime == null) {
            setTextIfPresent(lblPlannerHealthBadge, "No regime");
            setTextIfPresent(lblPlannerRegimeTitle, "My Regime");
            setTextIfPresent(lblPlannerTargetCalories, "-- kcal/j");
            setTextIfPresent(lblPlannerBmi, "--");
            setTextIfPresent(lblPlannerTaille, "-- cm");
            setTextIfPresent(lblPlannerPoids, "-- kg");
            setTextIfPresent(lblPlannerAge, "-- ans");
            setTextIfPresent(lblPlannerRecommendedMeals, "Aucune recommandation disponible.");
            setTextIfPresent(lblSummaryKcal, "0 kcal");
            setTextIfPresent(lblSummaryP, "P 0 g");
            setTextIfPresent(lblSummaryC, "C 0 g");
            setTextIfPresent(lblSummaryF, "F 0 g");
            mettreAJourProgression("0 / 0 kcal", 0, pbPlannerCalories, lblPlannerCaloriesProgress);
            
            styleBadge(paneBadgeWinStreak, lblBadgeWinStreakIcon, lblBadgeWinStreakTitle, lblBadgeWinStreakDesc, false);
            styleBadge(paneBadgeHydration, lblBadgeHydrationIcon, lblBadgeHydrationTitle, lblBadgeHydrationDesc, false);
            styleBadge(paneBadgeCalories, lblBadgeCaloriesIcon, lblBadgeCaloriesTitle, lblBadgeCaloriesDesc, false);
        } else {
            int target = activeRegime.getCaloriesCibles() == null ? 2000 : activeRegime.getCaloriesCibles();
            int curKcal = sommeRepas(repasDuJour, Repas::getCalories);
            int curP = sommeRepas(repasDuJour, Repas::getProteines);
            int curC = sommeRepas(repasDuJour, Repas::getGlucides);
            int curF = sommeRepas(repasDuJour, Repas::getLipides);
            int rem = Math.max(0, target - curKcal);

            setTextIfPresent(lblPlannerHealthBadge, humaniserTypeSante(activeRegime.getTypeSante()));
            setTextIfPresent(lblPlannerRegimeTitle, "My Regime");
            setTextIfPresent(lblPlannerTargetCalories, target + " kcal/j");
            setTextIfPresent(lblPlannerBmi, activeRegime.getBmi() == null ? "--" : String.format(Locale.US, "%.0f", activeRegime.getBmi()));
            setTextIfPresent(lblPlannerTaille, (activeRegime.getTaille() == null ? "--" : activeRegime.getTaille()) + " cm");
            setTextIfPresent(lblPlannerPoids, (activeRegime.getPoids() == null ? "--" : activeRegime.getPoids()) + " kg");
            setTextIfPresent(lblPlannerAge, (activeRegime.getAge() == null ? "--" : activeRegime.getAge()) + " ans");
            setTextIfPresent(lblPlannerRecommendedMeals, valeurOuDefaut(activeRegime.getRepasAdequats(), "Suggestions disponibles..."));
            
            setTextIfPresent(lblSummaryKcal, curKcal + " kcal");
            setTextIfPresent(lblSummaryP, "P " + curP + " g");
            setTextIfPresent(lblSummaryC, "C " + curC + " g");
            setTextIfPresent(lblSummaryF, "F " + curF + " g");

            mettreAJourProgression(curKcal + " / " + target + " kcal (" + rem + " restantes)", ratio(curKcal, target), pbPlannerCalories, lblPlannerCaloriesProgress);
            
            boolean caloriesRespectees = target > 0 && curKcal >= (target * 0.8);
            boolean hydratationOk = verresEau >= 5;
            if (lblBadgeHydrationDesc != null) {
                lblBadgeHydrationDesc.setText("Objectif : 1L par jour (" + verresEau + "/5 verres)");
            }

            boolean semaineParfaite = curKcal > 0 && Math.abs(curKcal - target) <= (target * 0.05);
            
            styleBadge(paneBadgeWinStreak, lblBadgeWinStreakIcon, lblBadgeWinStreakTitle, lblBadgeWinStreakDesc, semaineParfaite);
            styleBadge(paneBadgeHydration, lblBadgeHydrationIcon, lblBadgeHydrationTitle, lblBadgeHydrationDesc, hydratationOk);
            styleBadge(paneBadgeCalories, lblBadgeCaloriesIcon, lblBadgeCaloriesTitle, lblBadgeCaloriesDesc, caloriesRespectees);
        }

        if (lblPlannerMealsSubtitle != null) {
            lblPlannerMealsSubtitle.setText(repasDuJour.isEmpty() ? "Aucun repas aujourd'hui." : repasDuJour.size() + " repas aujourd'hui.");
        }

        if (plannerMealsListBox != null) {
            plannerMealsListBox.getChildren().setAll(repasAffiches.isEmpty()
                    ? List.of(creerMessagePlanner("Aucun repas disponible."))
                    : repasAffiches.stream().map(this::creerCarteMiniRepas).toList());
        }

        rafraichirListesDashboard();
        actualiserSelectionRepasJointure();
    }

    private void rafraichirListesDashboard() {
        if (tileRegimeCards != null) {
            String search = tfRegimeSearchGlobal == null ? "" : tfRegimeSearchGlobal.getText().toLowerCase();
            String typeFilter = cbRegimeTypeSearch == null ? "Tous les types" : cbRegimeTypeSearch.getValue();
            
            List<RegimeAlimentaire> filtered = allRegimes.stream()
                .filter(r -> search.isEmpty() || (r.getTypeSante() != null && r.getTypeSante().toLowerCase().contains(search)) || String.valueOf(r.getId()).contains(search))
                .filter(r -> typeFilter.equals("Tous les types") || typeFilter.equals(r.getTypeSante()))
                .toList();
            
            tileRegimeCards.getChildren().setAll(filtered.stream().map(this::creerCarteRegime).toList());
        }
    }

    private void actualiserContexteRepasSelectionne(boolean editMode) {
        Label label = editMode ? lblRepasRegimeContextEdit : lblRepasRegimeContext;
        ComboBox<User> userCombo = editMode ? cbRepasUserEdit : cbRepasUser;
        ComboBox<RegimeAlimentaire> regimeCombo = editMode ? cbRepasRegimeEdit : cbRepasRegime;
        if (label == null) {
            return;
        }
        User user = userCombo == null ? null : userCombo.getValue();
        RegimeAlimentaire regime = regimeCombo == null ? null : regimeCombo.getValue();
        if (regime != null) {
            label.setText("Regime #" + regime.getId() + " | " + humaniserTypeSante(regime.getTypeSante())
                    + " | " + (regime.getCaloriesCibles() == null ? 0 : regime.getCaloriesCibles()) + " kcal | "
                    + valeurOuDefaut(regime.getRepasAdequats(), "Aucune suggestion"));
            return;
        }
        if (user != null) {
            long count = allRegimes.stream().filter(item -> item.getUserId() != null && item.getUserId() == user.getId()).count();
            label.setText(count == 0
                    ? "Aucun regime n'est disponible pour cet utilisateur. Creez d'abord un regime."
                    : count + " regime(s) disponible(s) pour cet utilisateur. Selectionnez-en un pour relier le repas au planner.");
            return;
        }
        label.setText("Selectionnez un utilisateur puis un regime pour afficher le contexte nutritionnel.");
    }

    private void restaurerSelectionRepasEdit(List<Repas> repas) {
        if (tableRepasEdit == null) {
            return;
        }
        if (repasEditSelectionId == null) {
            tableRepasEdit.getSelectionModel().clearSelection();
            return;
        }
        for (Repas item : repas) {
            if (item.getId() == repasEditSelectionId) {
                tableRepasEdit.getSelectionModel().select(item);
                return;
            }
        }
        repasEditSelectionId = null;
        tableRepasEdit.getSelectionModel().clearSelection();
    }

    private void remplirFormulaireRepas(Repas repas) {
        tfRepasId.setText(String.valueOf(repas.getId()));
        selectionnerUser(cbRepasUserEdit, repas.getUserId());
        if (repas.getDateRepas() != null) {
            dpDateRepasEdit.setValue(repas.getDateRepas().toLocalDate());
        } else {
            dpDateRepasEdit.setValue(null);
        }
        cbTypeRepasEdit.setValue(normaliserTypeRepas(repas.getTypeRepas()));
        tfRepasNomEdit.setText(repas.getNomRepas());
        taRepasDescriptionEdit.setText(repas.getCommentaire());
        selectionnerRegime(cbRepasRegimeEdit, repas.getRegimeId());
        tfRepasCaloriesEdit.setText(toText(repas.getCalories()));
        tfRepasProteinesEdit.setText(toText(repas.getProteines()));
        tfRepasGlucidesEdit.setText(toText(repas.getGlucides()));
        tfRepasLipidesEdit.setText(toText(repas.getLipides()));
    }

    private void remplirFormulaireRegime(RegimeAlimentaire regime) {
        tfRegimeId.setText(String.valueOf(regime.getId()));
        selectionnerUser(cbRegimeUserEdit, regime.getUserId());
        cbRegimeTypeSanteEdit.setValue(regime.getTypeSante());
        tfRegimeTailleEdit.setText(toText(regime.getTaille()));
        tfRegimePoidsEdit.setText(toText(regime.getPoids()));
        tfRegimeAgeEdit.setText(toText(regime.getAge()));
        tfRegimeBmiEdit.setText(toText(regime.getBmi()));
        tfRegimeCaloriesCiblesEdit.setText(toText(regime.getCaloriesCibles()));
        taRegimeRepasAdequatsEdit.setText(regime.getRepasAdequats());
    }

    private Repas construireRepas(boolean editMode) {
        User user = editMode ? cbRepasUserEdit.getValue() : cbRepasUser.getValue();
        DatePicker datePicker = editMode ? dpDateRepasEdit : dpDateRepas;
        ComboBox<String> typeCombo = editMode ? cbTypeRepasEdit : cbTypeRepas;
        TextField nomField = editMode ? tfRepasNomEdit : tfRepasNom;
        TextArea commentaireField = editMode ? taRepasDescriptionEdit : taRepasDescription;
        ComboBox<RegimeAlimentaire> regimeCombo = editMode ? cbRepasRegimeEdit : cbRepasRegime;
        TextField caloriesField = editMode ? tfRepasCaloriesEdit : tfRepasCalories;
        TextField proteinesField = editMode ? tfRepasProteinesEdit : tfRepasProteines;
        TextField glucidesField = editMode ? tfRepasGlucidesEdit : tfRepasGlucides;
        TextField lipidesField = editMode ? tfRepasLipidesEdit : tfRepasLipides;

        if (user == null) throw new IllegalArgumentException("Selectionnez un utilisateur.");
        if (datePicker.getValue() == null) throw new IllegalArgumentException("Selectionnez une date.");
        if (typeCombo.getValue() == null || typeCombo.getValue().isBlank()) throw new IllegalArgumentException("Selectionnez un type de repas.");
        RegimeAlimentaire regime = regimeCombo.getValue();

        return new Repas(
                user.getId(),
                resolveRepasDateTime(editMode, datePicker.getValue()),
                normaliserTypeRepas(typeCombo.getValue()),
                nomField.getText(),
                parseInteger(caloriesField.getText()),
                parseInteger(proteinesField.getText()),
                parseInteger(glucidesField.getText()),
                parseInteger(lipidesField.getText()),
                commentaireField.getText(),
                regime == null ? null : regime.getId()
        );
    }

    private RegimeAlimentaire construireRegime(boolean editMode) {
        User user = editMode ? cbRegimeUserEdit.getValue() : cbRegimeUser.getValue();
        ComboBox<String> typeCombo = editMode ? cbRegimeTypeSanteEdit : cbRegimeTypeSante;
        TextField tailleField = editMode ? tfRegimeTailleEdit : tfRegimeTaille;
        TextField poidsField = editMode ? tfRegimePoidsEdit : tfRegimePoids;
        TextField ageField = editMode ? tfRegimeAgeEdit : tfRegimeAge;
        TextField bmiField = editMode ? tfRegimeBmiEdit : tfRegimeBmi;
        TextField caloriesField = editMode ? tfRegimeCaloriesCiblesEdit : tfRegimeCaloriesCibles;
        TextArea repasField = editMode ? taRegimeRepasAdequatsEdit : taRegimeRepasAdequats;

        if (user == null) {
            throw new IllegalArgumentException("La selection d'un utilisateur est obligatoire.");
        }

        double taille = parseDouble(tailleField.getText());
        if (taille <= 0 || taille > 300) {
            throw new IllegalArgumentException("La taille doit etre comprise entre 1 et 300 cm.");
        }

        double poids = parseDouble(poidsField.getText());
        if (poids <= 0 || poids > 300) {
            throw new IllegalArgumentException("Le poids doit etre compris entre 1 et 300 kg.");
        }

        int age = parseInteger(ageField.getText());
        if (age <= 0 || age > 100) {
            throw new IllegalArgumentException("L'age doit etre compris entre 1 et 100 ans.");
        }

        return new RegimeAlimentaire(
                user.getId(),
                taille,
                poids,
                age,
                parseDouble(bmiField.getText()),
                typeCombo.getValue(),
                parseInteger(caloriesField.getText()),
                repasField.getText()
        );
    }

    private void selectionnerUser(ComboBox<User> comboBox, Integer userId) {
        if (userId == null) { comboBox.getSelectionModel().clearSelection(); return; }
        for (User user : comboBox.getItems()) {
            if (user.getId() == userId) { comboBox.setValue(user); return; }
        }
    }

    private void selectionnerRegime(ComboBox<RegimeAlimentaire> comboBox, Integer regimeId) {
        if (regimeId == null) { comboBox.getSelectionModel().clearSelection(); return; }
        for (RegimeAlimentaire regime : comboBox.getItems()) {
            if (regime.getId() == regimeId) { comboBox.setValue(regime); return; }
        }
    }

    private User determinerUtilisateurReference() {
        Integer userId = allRegimes.stream()
                .map(RegimeAlimentaire::getUserId)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseGet(() -> allRepas.stream()
                        .map(Repas::getUserId)
                        .filter(java.util.Objects::nonNull)
                        .findFirst()
                        .orElse(null));
        if (userId == null) {
            return allUsers.isEmpty() ? null : allUsers.get(0);
        }
        return allUsers.stream()
                .filter(user -> user.getId() == userId)
                .findFirst()
                .orElse(allUsers.isEmpty() ? null : allUsers.get(0));
    }

    private RegimeAlimentaire getActualActiveRegime() {
        User referenceUser = determinerUtilisateurReference();
        if (tfRegimeId != null && !tfRegimeId.getText().isBlank()) {
            try {
                int selectedId = Integer.parseInt(tfRegimeId.getText());
                return allRegimes.stream()
                        .filter(r -> r.getId() == selectedId)
                        .findFirst()
                        .orElse(null);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private int sommeRepas(List<Repas> repas, Function<Repas, Integer> extractor) {
        return repas.stream()
                .map(extractor)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private String determinerTypeSanteAutomatique(double bmi) {
        if (bmi < 18.5) {
            return "sous_poids";
        }
        if (bmi < 25) {
            return "normal";
        }
        if (bmi < 30) {
            return "surpoids";
        }
        return "obesite";
    }

    private int calculerCaloriesCibles(double poids, double taille, Integer age, double bmi) {
        int valAge = (age != null && age > 0) ? age : 30;
        double bmr = (10.0 * poids) + (6.25 * taille) - (5.0 * valAge) + 5;
        
        double maintenance = bmr * 1.375;
        int calories = (int) Math.round(maintenance);

        if (bmi < 18.5) {
            calories += 400;
        } else if (bmi >= 30) {
            calories -= 800;
        } else if (bmi >= 25) {
            calories -= 400;
        }

        int caloriesFinales = Math.max(calories, 1200);
        return Math.min(caloriesFinales, 4000);
    }

    private String genererRepasAdequats(String typeSante, Integer age) {
        String base = switch (typeSante == null ? "" : typeSante) {
            case "sous_poids" -> "Petit dejeuner complet; collation proteinee; dejeuner riche en glucides complexes; diner equilibre";
            case "surpoids" -> "Petit dejeuner riche en fibres; dejeuner leger; diner pauvre en sucres; collation fruit";
            case "obesite" -> "Petit dejeuner controle; legumes et proteines maigres; diner tres leger; collation yaourt nature";
            default -> "Petit dejeuner equilibre; dejeuner complet; collation saine; diner leger";
        };
        if (age != null && age >= 45) {
            return base + "; hydratation et fibres renforcees";
        }
        return base;
    }

    private String humaniserTypeSante(String value) {
        if (value == null || value.isBlank()) {
            return "Non defini";
        }
        String normalized = value.replace('_', ' ');
        return normalized.substring(0, 1).toUpperCase(Locale.ROOT) + normalized.substring(1);
    }

    private double ratio(int value, int target) {
        if (target <= 0) {
            return 0;
        }
        return Math.min(1.0, value / (double) target);
    }

    private void mettreAJourProgression(String text, double progress, ProgressBar bar, Label label) {
        if (bar != null) {
            bar.setProgress(progress);
        }
        setTextIfPresent(label, text);
    }

    private void styleBadge(VBox pane, Label icon, Label title, Label desc, boolean active) {
        if (pane == null) return;
        pane.getStyleClass().removeAll("badge-locked", "badge-unlocked");
        pane.getStyleClass().add(active ? "badge-unlocked" : "badge-locked");
        
        if (icon != null) {
            icon.getStyleClass().removeAll("badge-icon-locked", "badge-icon-unlocked");
            icon.getStyleClass().add(active ? "badge-icon-unlocked" : "badge-icon-locked");
            icon.setText(active ? "✔" : "🔒"); 
        }
        
        if (title != null) {
            title.getStyleClass().removeAll("badge-title-locked", "badge-title-unlocked");
            title.getStyleClass().add(active ? "badge-title-unlocked" : "badge-title-locked");
        }
        
        if (desc != null) {
            desc.getStyleClass().removeAll("badge-desc-locked", "badge-desc-unlocked");
            desc.getStyleClass().add(active ? "badge-desc-unlocked" : "badge-desc-locked");
        }
    }

    private void setTextIfPresent(Label label, String value) {
        if (label != null) {
            label.setText(value);
        }
    }

    private VBox creerCarteMiniRepas(Repas repas) {
        Label title = new Label(valeurOuDefaut(repas.getNomRepas(), "Repas sans nom"));
        title.getStyleClass().add("planner-meal-title");

        Label meta = new Label(libelleBadgeTypeRepas(repas.getTypeRepas()) + " | " + valeurOuDefaut(repas.getDateDisplay(), "--"));
        meta.getStyleClass().add("planner-meal-meta");

        Label calories = new Label(toMetricValue(repas.getCalories(), "kcal"));
        calories.getStyleClass().add("planner-meal-kcal");
        calories.setStyle("-fx-text-fill: #059669;");

        ProgressBar pb = new ProgressBar(1.0);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setPrefHeight(6);
        pb.setStyle("-fx-accent: #10b981;");
        
        VBox box = new VBox(6, title, meta, calories, pb);
        box.getStyleClass().add("planner-meal-card");
        box.setStyle("-fx-border-color: #10b981; -fx-border-width: 0 0 0 6; -fx-background-color: #f0fdf4; -fx-background-radius: 12;");
        return box;
    }

    private VBox creerCarteRegime(RegimeAlimentaire regime) {
        HBox container = new HBox(12);
        container.getStyleClass().add("regime-list-card");
        container.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 12; -fx-border-color: #f1f5f9; -fx-alignment: CENTER_LEFT;");
        
        StackPane iconBox = new StackPane(new Label("💖"));
        iconBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8; -fx-padding: 10;");
        
        VBox texts = new VBox(2);
        Label title = new Label("Régime #" + regime.getId());
        title.setStyle("-fx-font-weight: 800; -fx-text-fill: #1e293b; -fx-font-size: 14px;");
        
        Label meta = new Label(humaniserTypeSante(regime.getTypeSante()) + " · " + (regime.getCaloriesCibles()==null?0:regime.getCaloriesCibles()) + " kcal/j");
        meta.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        texts.getChildren().addAll(title, meta);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button voirBtn = new Button("Voir");
        voirBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6 12; -fx-cursor: hand;");
        voirBtn.setOnAction(e -> {
            verresEau = 0;
            tfRegimeId.setText(String.valueOf(regime.getId()));
            actualiserDashboardPlanner();
            if (appScrollPane != null) {
                appScrollPane.setVvalue(0);
            }
        });
        
        Button modBtn = new Button("Modifier");
        modBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #1e293b; -fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6 12; -fx-cursor: hand;");
        modBtn.setOnAction(e -> modifierRegimeDepuisCarte(regime));

        Button pdfBtn = new Button("PDF");
        pdfBtn.setStyle("-fx-background-color: #ecfdf5; -fx-text-fill: #059669; -fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6 12; -fx-cursor: hand;");
        pdfBtn.setOnAction(e -> genererPdfRegime(regime));
        
        Button supBtn = new Button("Supprimer");
        supBtn.setStyle("-fx-background-color: #fdf2f2; -fx-text-fill: #ef4444; -fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6 12; -fx-cursor: hand;");
        supBtn.setOnAction(e -> confirmerSuppressionRegimeDepuisCarte(regime));
        container.getChildren().addAll(iconBox, texts, spacer, voirBtn, modBtn, pdfBtn, supBtn);
        VBox card = new VBox(container);
        card.setStyle("-fx-padding: 2;");
        return card;
    }

    private void genererPdfRegime(RegimeAlimentaire regime) {
        try {
            String desktop = System.getProperty("user.home") + java.io.File.separator + "Desktop";
            String fileName = desktop + java.io.File.separator + "Regime_" + regime.getId() + ".pdf";

            float[] darkGreen  = {0.04f, 0.36f, 0.29f};
            float[] lightGreen = {0.88f, 0.97f, 0.93f};
            float[] white      = {1f, 1f, 1f};
            float[] darkText   = {0.07f, 0.13f, 0.20f};

            try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument()) {
                org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage(
                        org.apache.pdfbox.pdmodel.common.PDRectangle.A4);
                doc.addPage(page);

                float W = page.getMediaBox().getWidth();
                float H = page.getMediaBox().getHeight();

                org.apache.pdfbox.pdmodel.PDPageContentStream cs =
                        new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page);

                org.apache.pdfbox.pdmodel.font.PDType1Font bold    = org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD;
                org.apache.pdfbox.pdmodel.font.PDType1Font regular = org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA;

                cs.setNonStrokingColor(darkGreen[0], darkGreen[1], darkGreen[2]);
                cs.addRect(0, H - 110, W, 110);
                cs.fill();

                cs.beginText();
                cs.setFont(bold, 26);
                cs.setNonStrokingColor(white[0], white[1], white[2]);
                cs.newLineAtOffset(40, H - 54);
                cs.showText("FITOPIA - Rapport Regime");
                cs.endText();

                cs.beginText();
                cs.setFont(regular, 13);
                cs.setNonStrokingColor(0.7f, 0.95f, 0.85f);
                cs.newLineAtOffset(40, H - 80);
                String typeSante = valeurOuDefaut(regime.getTypeSante(), "N/A");
                cs.showText("Regime #" + regime.getId() + "   Type : " + typeSante + "   Genere le : " + java.time.LocalDate.now());
                cs.endText();

                cs.setNonStrokingColor(lightGreen[0], lightGreen[1], lightGreen[2]);
                cs.addRect(0, H - 116, W, 6);
                cs.fill();

                float y = H - 160;
                cs.beginText();
                cs.setFont(bold, 14);
                cs.setNonStrokingColor(darkGreen[0], darkGreen[1], darkGreen[2]);
                cs.newLineAtOffset(40, y);
                cs.showText("Donnees du regime");
                cs.endText();

                cs.setStrokingColor(darkGreen[0], darkGreen[1], darkGreen[2]);
                cs.setLineWidth(1.5f);
                cs.moveTo(40, y - 4);
                cs.lineTo(W - 40, y - 4);
                cs.stroke();

                String[][] rows = {
                    {"Utilisateur",      valeurOuDefaut(determinerUtilisateurReference() == null ? null : determinerUtilisateurReference().getEmail(), "N/A")},
                    {"Type de sante",    valeurOuDefaut(regime.getTypeSante(), "N/A")},
                    {"Taille",           (regime.getTaille() == null ? "--" : regime.getTaille()) + " cm"},
                    {"Poids",            (regime.getPoids() == null ? "--" : regime.getPoids()) + " kg"},
                    {"Age",              (regime.getAge() == null ? "--" : regime.getAge()) + " ans"},
                    {"BMI",              regime.getBmi() == null ? "--" : String.format("%.2f", regime.getBmi())},
                    {"Calories cibles",  (regime.getCaloriesCibles() == null ? "--" : regime.getCaloriesCibles()) + " kcal/j"},
                    {"Repas adequats",   valeurOuDefaut(regime.getRepasAdequats(), "Aucune recommandation")},
                };

                float rowH = 30, rowY = y - 24;
                for (int i = 0; i < rows.length; i++) {
                    if (i % 2 == 0) {
                        cs.setNonStrokingColor(lightGreen[0], lightGreen[1], lightGreen[2]);
                    } else {
                        cs.setNonStrokingColor(white[0], white[1], white[2]);
                    }
                    cs.addRect(40, rowY - rowH + 8, W - 80, rowH);
                    cs.fill();

                    cs.beginText();
                    cs.setFont(bold, 11);
                    cs.setNonStrokingColor(darkGreen[0], darkGreen[1], darkGreen[2]);
                    cs.newLineAtOffset(50, rowY - 4);
                    cs.showText(rows[i][0]);
                    cs.endText();

                    cs.beginText();
                    cs.setFont(regular, 11);
                    cs.setNonStrokingColor(darkText[0], darkText[1], darkText[2]);
                    cs.newLineAtOffset(220, rowY - 4);
                    String val = rows[i][1];
                    if (val != null && val.length() > 60) val = val.substring(0, 57) + "...";
                    cs.showText(val == null ? "--" : val);
                    cs.endText();

                    rowY -= rowH;
                }

                cs.setNonStrokingColor(darkGreen[0], darkGreen[1], darkGreen[2]);
                cs.addRect(0, 0, W, 40);
                cs.fill();

                cs.beginText();
                cs.setFont(regular, 10);
                cs.setNonStrokingColor(white[0], white[1], white[2]);
                cs.newLineAtOffset(40, 14);
                cs.showText("FITOPIA - Nutrition & Bien-etre   |   Document confidentiel");
                cs.endText();

                cs.close();
                doc.save(fileName);
            }

            showInfo("PDF exporté !", "Fichier sauvegardé sur le Bureau :\n" + "Regime_" + regime.getId() + ".pdf");
            java.awt.Desktop.getDesktop().open(new java.io.File(fileName));

        } catch (Exception ex) {
            showError("Erreur PDF", "Impossible de générer le PDF : " + ex.getMessage());
        }
    }

    private void modifierRegimeDepuisCarte(RegimeAlimentaire r) {
        remplirFormulaireRegime(r);
        afficherVue(paneRegimeEdit, paneRegimeCreate, paneRegimeEdit, paneRegimeDelete, paneRegimeExplore);
    }
    
    private void confirmerSuppressionRegimeDepuisCarte(RegimeAlimentaire r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Supprimer le régime #" + r.getId() + " ?");
        alert.showAndWait().ifPresent(rs -> {
            if (rs == ButtonType.OK) {
                serviceRegime.delete(r);
                rafraichirDonnees();
            }
        });
    }

    private VBox creerMessagePlanner(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("planner-meal-meta");
        VBox box = new VBox(label);
        box.getStyleClass().add("planner-meal-card");
        return box;
    }

    @FXML
    private void afficherStatistiquesRepas() {
        if (repasExplorerView.isEmpty()) {
            showInfo("Aucun repas a analyser pour le moment.");
            return;
        }

        Map<String, Long> stats = TYPE_REPAS_OPTIONS.stream()
                .collect(Collectors.toMap(type -> type, this::compterRepasParType, (left, right) -> left, LinkedHashMap::new));

        PieChart chart = new PieChart();
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);
        stats.forEach((type, count) -> {
            if (count > 0) {
                chart.getData().add(new PieChart.Data(type, count));
            }
        });

        Label title = new Label("Repartition des repas par type");
        title.getStyleClass().add("repas-chart-title");

        Label subtitle = new Label(repasExplorerView.size() + " repas analyses");
        subtitle.getStyleClass().add("repas-chart-subtitle");

        Button closeButton = new Button("Fermer");
        closeButton.getStyleClass().add("repas-chart-close-button");

        VBox root = new VBox(18, title, subtitle, chart, closeButton);
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.TOP_LEFT);
        root.getStyleClass().add("repas-chart-dialog");

        Scene scene = new Scene(root, 720, 760);
        if (paneRepasExplore.getScene() != null) {
            scene.getStylesheets().addAll(paneRepasExplore.getScene().getStylesheets());
        }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (paneRepasExplore.getScene() != null && paneRepasExplore.getScene().getWindow() instanceof Stage owner) {
            stage.initOwner(owner);
        }
        stage.setTitle("Statistiques repas");
        stage.setScene(scene);
        closeButton.setOnAction(event -> stage.close());
        stage.show();

        Platform.runLater(() -> appliquerCouleursChart(chart));
    }

    private void actualiserExplorateurRepas() {
        if (tileRepasCards == null) {
            return;
        }

        String search = tfRepasSearch == null ? "" : tfRepasSearch.getText();
        String typeFilter = cbRepasExploreTypeFilter == null ? FILTER_ALL_TYPES : cbRepasExploreTypeFilter.getValue();
        String sortValue = cbRepasExploreSort == null ? SORT_RECENT : cbRepasExploreSort.getValue();

        repasExplorerView = allRepas.stream()
                .filter(repas -> correspondRechercheRepas(repas, search))
                .filter(repas -> correspondFiltreType(repas, typeFilter))
                .sorted(comparateurRepas(sortValue))
                .toList();

        if (tileRepasCards == null) return;
        
        tileRepasCards.getChildren().setAll(repasExplorerView.stream()
                .map(this::creerCarteRepas)
                .toList());

        boolean empty = repasExplorerView.isEmpty();
        if (boxRepasExploreEmpty != null) {
            boxRepasExploreEmpty.setManaged(empty);
            boxRepasExploreEmpty.setVisible(empty);
        }
        if (btnRepasStatistics != null) {
            btnRepasStatistics.setDisable(empty);
        }
        mettreAJourCompteurRepas();
    }

    private VBox creerCarteSelectionRepas(Repas repas) {
        Label title = new Label(valeurOuDefaut(repas.getNomRepas(), "Sans nom"));
        title.getStyleClass().add("repas-card-title");
        title.setStyle("-fx-font-size: 16px; -fx-text-fill: #1e293b;");

        Label type = new Label(libelleBadgeTypeRepas(repas.getTypeRepas()));
        type.getStyleClass().addAll("repas-type-pill", "repas-type-pill-" + slugTypeRepas(repas.getTypeRepas()));
        type.setStyle("-fx-font-size: 10px; -fx-padding: 3 8;");

        HBox header = new HBox(8, title, new Region(), type);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);

        Label kcal = new Label("🔥 " + toMetricValue(repas.getCalories(), "kcal"));
        kcal.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0c3f44;");

        HBox macros = new HBox(6);
        macros.getChildren().addAll(
            creerPill("P " + toText(repas.getProteines()) + "g", "#eef2ff", "#4338ca"),
            creerPill("C " + toText(repas.getGlucides()) + "g", "#f0fdf4", "#15803d"),
            creerPill("F " + toText(repas.getLipides()) + "g", "#fff7ed", "#c2410c")
        );

        User refUser = determinerUtilisateurReference();
        RegimeAlimentaire activeRegime = getActualActiveRegime();
        int target = activeRegime != null && activeRegime.getCaloriesCibles() != null ? activeRegime.getCaloriesCibles() : 2000;
        
        Label impactLabel = new Label("📈 Impact sur la journée");
        impactLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        
        ProgressBar pb = new ProgressBar(ratio(repas.getCalories() != null ? repas.getCalories() : 0, target));
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setPrefHeight(6);
        pb.getStyleClass().add("impact-pb");
        pb.setStyle("-fx-accent: #0c3f44;");

        Label impactInfo = new Label(toText(repas.getCalories()) + " / " + target + " kcal");
        impactInfo.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");

        Button addBtn = new Button("Ajouter ce repas");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.getStyleClass().add("primary-button");
        addBtn.setStyle("-fx-background-color: #0c3f44; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8;");
        addBtn.setOnAction(e -> clonerRepasPourAujourdhui(repas));

        VBox card = new VBox(12, header, kcal, macros, new VBox(4, impactLabel, pb, impactInfo), addBtn);
        card.getStyleClass().add("selection-repas-card");
        card.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        card.setPrefWidth(210);
        return card;
    }

    private Label creerPill(String text, String bg, String fg) {
        Label l = new Label(text);
        l.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-padding: 2 6; -fx-background-radius: 6; -fx-font-size: 10px; -fx-font-weight: bold;");
        return l;
    }

    private void actualiserModificationRepas() {
        if (tileRepasEditCards == null) {
            return;
        }

        List<Repas> repasToEdit = allRepas.stream()
                .sorted(comparateurRepas(SORT_RECENT))
                .toList();

        tileRepasEditCards.getChildren().setAll(repasToEdit.stream()
                .map(this::creerCarteModificationRepas)
                .toList());

        boolean empty = repasToEdit.isEmpty();
        if (boxRepasEditEmpty != null) {
            boxRepasEditEmpty.setManaged(empty);
            boxRepasEditEmpty.setVisible(empty);
        }
        if (boxRepasEditForm != null) {
            boolean hasSelection = repasEditSelectionId != null && !empty;
            boxRepasEditForm.setManaged(hasSelection);
            boxRepasEditForm.setVisible(hasSelection);
        }
    }

    private VBox creerCarteRepas(Repas repas) {
        Label title = new Label(valeurOuDefaut(repas.getNomRepas(), "Repas sans nom"));
        title.getStyleClass().add("repas-card-title");

        Label email = new Label(valeurOuDefaut(repas.getUserEmail(), "Utilisateur inconnu"));
        email.getStyleClass().add("repas-card-email");

        Label separatorBeforeType = new Label("·");
        separatorBeforeType.getStyleClass().add("repas-card-separator");

        Label type = new Label(libelleBadgeTypeRepas(repas.getTypeRepas()));
        type.getStyleClass().addAll("repas-type-pill", "repas-type-pill-" + slugTypeRepas(repas.getTypeRepas()));

        Label separatorBeforeCalories = new Label("·");
        separatorBeforeCalories.getStyleClass().add("repas-card-separator");

        Label calories = new Label(toMetricValue(repas.getCalories(), "kcal"));
        calories.getStyleClass().add("repas-card-calories");

        HBox metaRow = new HBox(10, email, separatorBeforeType, type, separatorBeforeCalories, calories);
        metaRow.getStyleClass().add("repas-card-meta-row");
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label date = new Label(repas.getDateDisplay());
        date.getStyleClass().add("repas-card-date");

        Button pdfButton = new Button("Exporter PDF");
        pdfButton.getStyleClass().add("repas-pdf-button");
        pdfButton.setOnAction(event -> exporterRepasEnPdf(repas));

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        HBox footer = new HBox(12, date, spacer, pdfButton);
        footer.getStyleClass().add("repas-card-footer");
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(18, title, metaRow, footer);
        card.getStyleClass().add("repas-browser-card");
        card.setPrefWidth(520);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private HBox creerCarteModificationRepas(Repas repas) {
        Label title = new Label(valeurOuDefaut(repas.getNomRepas(), "Repas sans nom"));
        title.getStyleClass().add("repas-delete-title");

        Label meta = new Label(valeurOuDefaut(repas.getDateDisplay(), "--") + " · " + valeurOuDefaut(repas.getUserEmail(), "Utilisateur inconnu"));
        meta.getStyleClass().add("repas-delete-meta");

        Label type = new Label(libelleBadgeTypeRepas(repas.getTypeRepas()));
        type.getStyleClass().addAll("repas-type-pill", "repas-type-pill-" + slugTypeRepas(repas.getTypeRepas()));

        Label calories = new Label(toMetricValue(repas.getCalories(), "kcal"));
        calories.getStyleClass().add("repas-delete-calories-pill");

        HBox badges = new HBox(10, type, calories);
        badges.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(10, title, meta, badges);
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(content, javafx.scene.layout.Priority.ALWAYS);

        Button editButton = new Button("Modifier");
        editButton.getStyleClass().add("repas-edit-button");
        editButton.setOnAction(event -> selectionnerRepasPourModification(repas));

        HBox card = new HBox(16, content, editButton);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("repas-edit-card");
        card.setOnMouseClicked(event -> selectionnerRepasPourModification(repas));
        if (repasEditSelectionId != null && repasEditSelectionId.equals(repas.getId())) {
            card.getStyleClass().add("repas-edit-card-selected");
        }
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private void selectionnerRepasPourModification(Repas repas) {
        afficherModificationRepas();
        repasEditSelectionId = repas.getId();
        if (tableRepasEdit != null) {
            tableRepasEdit.getSelectionModel().select(repas);
        }
        remplirFormulaireRepas(repas);
        if (boxRepasEditForm != null) {
            boxRepasEditForm.setManaged(true);
            boxRepasEditForm.setVisible(true);
            scrollToNode(boxRepasEditForm);
        }
        actualiserModificationRepas();
    }

    private void scrollToNode(Node node) {
        if (appScrollPane == null || node == null || appScrollPane.getContent() == null) {
            return;
        }

        Platform.runLater(() -> {
            Node content = appScrollPane.getContent();
            double contentHeight = content.getBoundsInLocal().getHeight();
            double viewportHeight = appScrollPane.getViewportBounds().getHeight();
            double scrollableHeight = contentHeight - viewportHeight;
            if (scrollableHeight <= 0) {
                appScrollPane.setVvalue(0);
                return;
            }

            double targetY = content.sceneToLocal(node.localToScene(node.getBoundsInLocal())).getMinY();
            double targetValue = targetY / scrollableHeight;
            appScrollPane.setVvalue(Math.max(0, Math.min(targetValue, 1)));
        });
    }

    private HBox creerCarteSuppressionRepasStylee(Repas repas) {
        Label title = new Label(valeurOuDefaut(repas.getNomRepas(), "Repas sans nom"));
        title.getStyleClass().add("repas-delete-title");

        Label meta = new Label(valeurOuDefaut(repas.getDateDisplay(), "--") + " · " + valeurOuDefaut(repas.getUserEmail(), "Utilisateur inconnu"));
        meta.getStyleClass().add("repas-delete-meta");

        Label type = new Label(libelleBadgeTypeRepas(repas.getTypeRepas()));
        type.getStyleClass().addAll("repas-type-pill", "repas-type-pill-" + slugTypeRepas(repas.getTypeRepas()));

        Label calories = new Label(toMetricValue(repas.getCalories(), "kcal"));
        calories.getStyleClass().add("repas-delete-calories-pill");

        HBox badges = new HBox(10, type, calories);
        badges.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(10, title, meta, badges);
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(content, javafx.scene.layout.Priority.ALWAYS);

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("repas-delete-button");
        deleteButton.setOnAction(event -> confirmerSuppressionDepuisCarte(repas));

        HBox card = new HBox(16, content, deleteButton);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("repas-delete-card");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private void actualiserSuppressionRepas() {
        if (tileRepasDeleteCards == null) {
            return;
        }

        List<Repas> repasToDelete = allRepas.stream()
                .sorted(comparateurRepas(SORT_RECENT))
                .toList();

        tileRepasDeleteCards.getChildren().setAll(repasToDelete.stream()
                .map(this::creerCarteSuppressionRepasStylee)
                .toList());

        boolean empty = repasToDelete.isEmpty();
        if (boxRepasDeleteEmpty != null) {
            boxRepasDeleteEmpty.setManaged(empty);
            boxRepasDeleteEmpty.setVisible(empty);
        }
    }

    private void confirmerSuppressionDepuisCarte(Repas repas) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer un repas");
        alert.setHeaderText("Confirmer la suppression");
        alert.setContentText("Voulez-vous supprimer le repas \"" + valeurOuDefaut(repas.getNomRepas(), "Sans nom") + "\" ?");
        alert.showAndWait()
                .filter(response -> response.getButtonData().isDefaultButton())
                .ifPresent(response -> supprimerRepasSelectionne(repas));
    }

    private void supprimerRepasSelectionne(Repas repas) {
        try {
            if (repasEditSelectionId != null && repasEditSelectionId.equals(repas.getId())) {
                repasEditSelectionId = null;
            }
            serviceRepas.delete(repas);
            rafraichirDonnees();
            viderFormulaireRepasModification();
            if (lblRepasSelectionDelete != null) {
                lblRepasSelectionDelete.setText("Aucun repas selectionne");
            }
            showInfo("Repas supprime avec succes.");
        } catch (Exception exception) {
            showError("Erreur repas", exception.getMessage());
        }
    }

    private void mettreAJourCompteurRepas() {
        int total = repasExplorerView.size();
        if (lblRepasExploreCount != null) {
            lblRepasExploreCount.setText(total == 0 ? "Aucun repas affiche" : total + " repas affiches");
        }
    }

    private VBox creerMetricChip(String label, String value) {
        Label chipLabel = new Label(label);
        chipLabel.getStyleClass().add("repas-metric-chip-label");

        Label chipValue = new Label(value);
        chipValue.getStyleClass().add("repas-metric-chip-value");

        VBox chip = new VBox(2, chipLabel, chipValue);
        chip.getStyleClass().add("repas-metric-chip");
        return chip;
    }

    private String toMetricValue(Integer value, String unit) {
        return (value == null ? 0 : value) + " " + unit;
    }

    private void exporterRepasEnPdf(Repas repas) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer le PDF du repas");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        chooser.setInitialFileName(genererNomPdfRepas(repas));

        Stage owner = paneRepasExplore != null && paneRepasExplore.getScene() != null && paneRepasExplore.getScene().getWindow() instanceof Stage stage
                ? stage
                : null;
        File file = chooser.showSaveDialog(owner);
        if (file == null) {
            return;
        }

        try {
            genererPdfRepas(repas, file);
            showInfo("PDF genere avec succes : " + file.getName());
        } catch (IOException exception) {
            showError("Erreur PDF", "Impossible de generer le PDF : " + exception.getMessage());
        }
    }

    private String genererNomPdfRepas(Repas repas) {
        String mealName = normaliserNomFichier(valeurOuDefaut(repas.getNomRepas(), "repas"));
        return mealName + "-" + repas.getId() + ".pdf";
    }

    private void genererPdfRepas(Repas repas, File file) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float width = page.getMediaBox().getWidth();
                float height = page.getMediaBox().getHeight();
                float margin = 52f;
                float y = height - margin;

                dessinerFondEntete(content, width, height);

                y = ecrireTexte(content, PDType1Font.HELVETICA_BOLD, 26, margin, y, new Color(0.07, 0.20, 0.23, 1.0), "Fiche repas");
                y -= 8;
                y = ecrireTexte(content, PDType1Font.HELVETICA, 13, margin, y, new Color(0.42, 0.50, 0.57, 1.0),
                        "Document nutritionnel exporte depuis Fitopia");

                y -= 36;
                dessinerBlocInfo(content, margin, y, width - (margin * 2), 116, "Informations generales");
                y -= 26;
                y = ecrireLigneInfo(content, margin + 18, y, "Repas", valeurOuDefaut(repas.getNomRepas(), "--"));
                y = ecrireLigneInfo(content, margin + 18, y, "Utilisateur", valeurOuDefaut(repas.getUserEmail(), "--"));
                y = ecrireLigneInfo(content, margin + 18, y, "Type", normaliserTypeRepas(repas.getTypeRepas()));
                y = ecrireLigneInfo(content, margin + 18, y, "Date", valeurOuDefaut(repas.getDateDisplay(), "--"));

                y -= 26;
                dessinerBlocInfo(content, margin, y, width - (margin * 2), 116, "Valeurs nutritionnelles");
                y -= 26;
                y = ecrireLigneInfo(content, margin + 18, y, "Calories", toMetricValue(repas.getCalories(), "kcal"));
                y = ecrireLigneInfo(content, margin + 18, y, "Proteines", toMetricValue(repas.getProteines(), "g"));
                y = ecrireLigneInfo(content, margin + 18, y, "Glucides", toMetricValue(repas.getGlucides(), "g"));
                y = ecrireLigneInfo(content, margin + 18, y, "Lipides", toMetricValue(repas.getLipides(), "g"));

                y -= 26;
                dessinerBlocInfo(content, margin, y, width - (margin * 2), 138, "Commentaires");
                y -= 26;
                y = ecrireParagraphe(content, margin + 18, y, width - (margin * 2) - 36,
                        valeurOuDefaut(construireDetailsRepas(repas), "Aucun commentaire"));

                ecrireTexte(content, PDType1Font.HELVETICA_OBLIQUE, 10, margin, 56, new Color(0.47, 0.55, 0.61, 1.0),
                        "Fitopia • Export genere le " + LocalDateTime.now().withNano(0));
            }

            document.save(file);
        }
    }

    private void dessinerFondEntete(PDPageContentStream content, float width, float height) throws IOException {
        content.setNonStrokingColor(12, 77, 74);
        content.addRect(0, height - 110, width, 110);
        content.fill();

        content.setNonStrokingColor(24, 136, 104);
        content.addRect(0, height - 124, width, 14);
        content.fill();
    }

    private void dessinerBlocInfo(PDPageContentStream content, float x, float yTop, float width, float height, String title) throws IOException {
        float bottom = yTop - height;
        content.setNonStrokingColor(250, 252, 251);
        content.addRect(x, bottom, width, height);
        content.fill();

        content.setStrokingColor(220, 234, 229);
        content.addRect(x, bottom, width, height);
        content.stroke();

        ecrireTexte(content, PDType1Font.HELVETICA_BOLD, 16, x + 18, yTop - 18, new Color(0.09, 0.23, 0.27, 1.0), title);
    }

    private float ecrireLigneInfo(PDPageContentStream content, float x, float y, String label, String value) throws IOException {
        ecrireTexte(content, PDType1Font.HELVETICA_BOLD, 12, x, y, new Color(0.11, 0.25, 0.29, 1.0), label + " :");
        ecrireTexte(content, PDType1Font.HELVETICA, 12, x + 120, y, new Color(0.28, 0.35, 0.40, 1.0), value);
        return y - 20;
    }

    private float ecrireParagraphe(PDPageContentStream content, float x, float y, float maxWidth, String text) throws IOException {
        List<String> lines = decouperLignes(text, maxWidth, PDType1Font.HELVETICA, 12);
        float currentY = y;
        for (String line : lines) {
            ecrireTexte(content, PDType1Font.HELVETICA, 12, x, currentY, new Color(0.28, 0.35, 0.40, 1.0), line);
            currentY -= 17;
        }
        return currentY;
    }

    private float ecrireTexte(PDPageContentStream content, PDType1Font font, float fontSize, float x, float y, Color color, String text) throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.setNonStrokingColor((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue());
        content.newLineAtOffset(x, y);
        content.showText(safePdfText(text));
        content.endText();
        return y;
    }

    private List<String> decouperLignes(String text, float maxWidth, PDType1Font font, float fontSize) throws IOException {
        String[] words = safePdfText(valeurOuDefaut(text, "")).split("\\s+");
        List<String> lines = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            float width = font.getStringWidth(candidate) / 1000 * fontSize;
            if (width > maxWidth && !current.isEmpty()) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }

    private String normaliserNomFichier(String value) {
        return normaliserTexte(value).replace(" ", "-").replaceAll("[^a-z0-9\\-]", "");
    }

    private String safePdfText(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("•", "-")
                .replace("’", "'")
                .replace("“", "\"")
                .replace("”", "\"");
    }

    private boolean correspondRechercheRepas(Repas repas, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String query = normaliserTexte(search);
        return normaliserTexte(repas.getNomRepas()).contains(query)
                || normaliserTexte(repas.getUserEmail()).contains(query)
                || normaliserTexte(normaliserTypeRepas(repas.getTypeRepas())).contains(query)
                || normaliserTexte(repas.getCommentaire()).contains(query)
                || normaliserTexte(repas.getDateDisplay()).contains(query);
    }

    private boolean correspondFiltreType(Repas repas, String filter) {
        return filter == null || FILTER_ALL_TYPES.equals(filter) || normaliserTypeRepas(repas.getTypeRepas()).equals(filter);
    }

    private Comparator<Repas> comparateurRepas(String sortValue) {
        Comparator<Repas> byDate = Comparator.comparing(
                Repas::getDateRepas,
                Comparator.nullsLast(Comparator.naturalOrder())
        );
        Comparator<Repas> byCalories = Comparator.comparing(
                Repas::getCalories,
                Comparator.nullsLast(Comparator.naturalOrder())
        );
        Comparator<Repas> byName = Comparator.comparing(
                repas -> valeurOuDefaut(repas.getNomRepas(), ""),
                String.CASE_INSENSITIVE_ORDER
        );

        if (SORT_OLD.equals(sortValue)) {
            return byDate;
        }
        if (SORT_CALORIES_HIGH.equals(sortValue)) {
            return byCalories.reversed();
        }
        if (SORT_CALORIES_LOW.equals(sortValue)) {
            return byCalories;
        }
        if (SORT_NAME.equals(sortValue)) {
            return byName;
        }
        return byDate.reversed();
    }

    private long compterRepasParType(String type) {
        return repasExplorerView.stream()
                .filter(repas -> normaliserTypeRepas(repas.getTypeRepas()).equals(type))
                .count();
    }

    private void appliquerCouleursChart(PieChart chart) {
        for (int i = 0; i < chart.getData().size(); i++) {
            PieChart.Data data = chart.getData().get(i);
            String color = TYPE_COLOR_BY_LABEL.getOrDefault(data.getName(), "#178764");
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-pie-color: " + color + ";");
            }
            chart.lookupAll(".default-color" + i + ".chart-legend-item-symbol")
                    .forEach(node -> node.setStyle("-fx-background-color: " + color + ";"));
        }
    }

    private String construireDetailsRepas(Repas repas) {
        String commentaire = valeurOuDefaut(repas.getCommentaire(), "");
        String regime = valeurOuDefaut(repas.getRegimeDisplay(), "");
        if (!commentaire.isBlank() && !regime.isBlank()) {
            return commentaire + " • " + regime;
        }
        if (!commentaire.isBlank()) {
            return commentaire;
        }
        if (!regime.isBlank()) {
            return regime;
        }
        return "Aucun detail supplementaire";
    }

    private String normaliserTypeRepas(String value) {
        String normalized = normaliserTexte(value).replace("_", " ").replace("-", " ");
        return switch (normalized) {
            case "petit de", "petit dej", "petit dejeuner", "breakfast" -> TYPE_PETIT_DEJEUNER;
            case "dejeuner", "dej", "lunch" -> TYPE_DEJEUNER;
            case "diner", "dinner", "evening", "souper" -> TYPE_DINER;
            case "collation", "collat", "snack", "extra meal", "extra meal " , "extra_meal" -> TYPE_COLLATION;
            default -> value == null || value.isBlank() ? TYPE_COLLATION : value.trim();
        };
    }

    private String libelleBadgeTypeRepas(String value) {
        return switch (normaliserTypeRepas(value)) {
            case TYPE_PETIT_DEJEUNER -> "breakfast";
            case TYPE_DEJEUNER -> "lunch";
            case TYPE_DINER -> "evening";
            default -> "collat";
        };
    }

    private String slugTypeRepas(String value) {
        return switch (normaliserTypeRepas(value)) {
            case TYPE_PETIT_DEJEUNER -> "petit-dejeuner";
            case TYPE_DEJEUNER -> "dejeuner";
            case TYPE_DINER -> "diner";
            default -> "collation";
        };
    }

    private String normaliserTexte(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    private String valeurOuDefaut(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private LocalDateTime resolveRepasDateTime(boolean editMode, LocalDate date) {
        LocalTime time = DEFAULT_REPAS_TIME;
        if (editMode) {
            Repas selectedRepas = tableRepasEdit.getSelectionModel().getSelectedItem();
            if (selectedRepas != null && selectedRepas.getDateRepas() != null) {
                time = selectedRepas.getDateRepas().toLocalTime();
            }
        }
        return LocalDateTime.of(date, time);
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) return null;
        return Integer.parseInt(value.trim());
    }

    private Integer parseIntegerSafe(String value) {
        try {
            return parseInteger(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) return null;
        return Double.parseDouble(value.trim().replace(",", "."));
    }

    private Double parseDoubleSafe(String value) {
        try {
            return parseDouble(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String toText(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private void afficherPaneRepas(VBox paneActif, Button boutonActif) {
        masquerTousLesFormulaires();
        if (paneOverlay != null) paneOverlay.setMouseTransparent(false);
        afficherVue(paneActif, paneRepasCreate, paneRepasEdit, paneRepasDelete, paneRepasExplore);
        activerBoutonAction(boutonActif, btnActionCreerRepas, btnActionModifierRepas, btnActionSupprimerRepas, btnActionExplorerRepas);
    }

    private void afficherPaneRegime(VBox paneActif, Button boutonActif) {
        masquerTousLesFormulaires();
        if (paneOverlay != null) paneOverlay.setMouseTransparent(false);
        afficherVue(paneActif, paneRegimeCreate, paneRegimeEdit, paneRegimeDelete, paneRegimeExplore);
        activerBoutonAction(boutonActif, btnActionCreerRegime, btnActionModifierRegime, btnActionSupprimerRegime, btnActionExplorerRegime);
    }

    private void afficherVue(VBox vueActive, VBox... vues) {
        if (vueActive != null) {
            vueActive.setVisible(true);
            vueActive.setManaged(true);
        }
        for (VBox vue : vues) {
            boolean active = vue == vueActive;
            vue.setVisible(active);
            vue.setManaged(active);
        }
    }

    private void activerBoutonModule(Button boutonActif, Button... autres) {
        setStyleFlag(boutonActif, "sidebar-nav-button-active", true);
        setStyleFlag(boutonActif, "planner-nav-button-active", true);
        for (Button button : autres) setStyleFlag(button, "sidebar-nav-button-active", false);
        for (Button button : autres) setStyleFlag(button, "planner-nav-button-active", false);
    }

    private void activerBoutonAction(Button boutonActif, Button... boutons) {
        for (Button button : boutons) {
            setStyleFlag(button, "action-card-active", button == boutonActif);
            setStyleFlag(button, "planner-switch-button-active", button == boutonActif);
        }
    }

    private void setStyleFlag(Button button, String styleClass, boolean active) {
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

    private void showInfo(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Succes", message);
    }

    private void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    private void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static final String API_KEY_SPOONACULAR = "02582f34796041079ba6f1943bf1e028"; // Clef Spoonacular IA
    
    @FXML private BorderPane rootPane;
    @FXML private StackPane paneOverlay;

    @FXML
    private void afficherPopupScanIA() {
        masquerTousLesFormulaires();
        if (paneScanIA != null) {
            paneScanIA.setVisible(true);
            paneScanIA.setManaged(true);
            if (boxResultatIA != null) { boxResultatIA.setVisible(false); boxResultatIA.setManaged(false); }
            if (btnAppliquerCalories != null) { btnAppliquerCalories.setVisible(false); btnAppliquerCalories.setManaged(false); }
            if (ivScanImagePreview != null) ivScanImagePreview.setImage(null);
            if (pbScanProgress != null) pbScanProgress.setVisible(false);
        }
    }

    @FXML
    void chargerImagePourScan(javafx.event.ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir l'image du repas");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images (JPG, PNG, WebP, HEIC, etc.)", 
                "*.jpg", "*.jpeg", "*.png", "*.webp", "*.heic", "*.heif", "*.jfif", "*.bmp", "*.gif", "*.tiff", "*.avif",
                "*.JPG", "*.JPEG", "*.PNG", "*.WEBP", "*.HEIC", "*.HEIF", "*.JFIF", "*.BMP", "*.GIF", "*.TIFF", "*.AVIF"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        
        Stage stage = (Stage) viewRepas.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                ivScanImagePreview.setImage(new javafx.scene.image.Image(file.toURI().toString()));
            } catch(Exception e) {}

            boxResultatIA.setVisible(false); boxResultatIA.setManaged(false);
            btnAppliquerCalories.setVisible(false); btnAppliquerCalories.setManaged(false);
            pbScanProgress.setVisible(true);

            new Thread(() -> {
                try {
                    // 1. Détection de l'aliment avec Spoonacular Vision
                    String detectedCategory = detecterFoodSpoonacular(file);
                    
                    if (detectedCategory == null || detectedCategory.equalsIgnoreCase("Plat inconnu")) {
                        Platform.runLater(() -> {
                            pbScanProgress.setVisible(false);
                            showError("Erreur IA", "L'IA n'a pas pu identifier le plat. Essayez une image plus nette.");
                        });
                        return;
                    }

                    System.out.println("Plat détecté via Spoonacular : " + detectedCategory);
                    
                    // 2. Calcul des nutriments (Toujours via Spoonacular)
                    double[] macros = calculerNutritionSpoonacular(detectedCategory);

                    long caloriesArrondies = Math.round(macros[0]);
                    int p = (int) Math.round(macros[1]);
                    int c = (int) Math.round(macros[2]);
                    int f = (int) Math.round(macros[3]);

                    String finalFoodsStr = preTraduire(detectedCategory);
                    Platform.runLater(() -> {
                        pbScanProgress.setVisible(false);
                        lblIACalories.setText("Énergie estimée : " + caloriesArrondies + " kcal (P:" + p + "g | C:" + c + "g | F:" + f + "g)");
                        boxResultatIA.setVisible(true); boxResultatIA.setManaged(true);
                        if (tfIARepasNomParticulier != null) tfIARepasNomParticulier.setText(finalFoodsStr);
                        
                        lastScannedCalories = String.valueOf(caloriesArrondies);
                        lastScannedNom = finalFoodsStr;
                        lastScannedP = p;
                        lastScannedC = c;
                        lastScannedL = f;
                        btnAppliquerCalories.setVisible(true); btnAppliquerCalories.setManaged(true);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        pbScanProgress.setVisible(false);
                        if (e.getMessage() != null && (e.getMessage().contains("401") || e.getMessage().contains("403"))) {
                            showError("Erreur Authentification", "Votre clé API Spoonacular est invalide ou expirée.\nVeuillez en générer une nouvelle sur spoonacular.com");
                        } else if (e.getMessage() != null && e.getMessage().contains("402")) {
                            showError("Quota Épuisé", "Vous avez atteint la limite de scans gratuits pour aujourd'hui.");
                        } else {
                            showError("Erreur Scan", "Problème de connexion à l'IA : " + e.getMessage());
                        }
                    });
                }
            }).start();
        }
    }

    @FXML
    void appliquerCaloriesScan(javafx.event.ActionEvent event) {
        String nomFinal = (tfIARepasNomParticulier != null) ? tfIARepasNomParticulier.getText() : lastScannedNom;

        if (tfRepasCaloriesEdit != null) tfRepasCaloriesEdit.setText(lastScannedCalories);
        if (tfRepasCalories != null) tfRepasCalories.setText(lastScannedCalories);
        
        if (tfRepasNomEdit != null) tfRepasNomEdit.setText(nomFinal);
        if (tfRepasNom != null) tfRepasNom.setText(nomFinal);
        
        // --- Injection directe dans le tracking du jour ---
        RegimeAlimentaire regimeActif = getActualActiveRegime();
        if (regimeActif != null) {
            Repas repasScanne = new Repas();
            repasScanne.setNomRepas(nomFinal);
            try { repasScanne.setCalories(Integer.parseInt(lastScannedCalories)); } catch(Exception e) { repasScanne.setCalories(0); }
            repasScanne.setTypeRepas("Déjeuner"); // Type par défaut pour l'insert auto
            repasScanne.setProteines(lastScannedP);
            repasScanne.setGlucides(lastScannedC);
            repasScanne.setLipides(lastScannedL);
            repasScanne.setDateRepas(LocalDateTime.now());
            User u = determinerUtilisateurReference();
            if (u != null) repasScanne.setUserId(u.getId());
            repasScanne.setRegimeId(regimeActif.getId());
            
            // On l'ajoute dans le système et on relance la checklist/barre de progression
            fusionnerRepasSiExiste(repasScanne);
            actualiserDashboardPlanner();
            
            showInfo("Scan Appliqué", "Génial ! Le repas (" + lastScannedNom + ") a été directement ajouté à votre checklist d'aujourd'hui, et les " + lastScannedCalories + " kcal ont mis à jour votre barre d'avancement !");
        } else {
            showInfo("Scan Appliqué", "Les informations ont été copiées dans le formulaire, mais aucun régime n'étant ouvert, le repas n'a pas pu s'ajouter tout seul à la checklist.");
        }
        // ---------------------------------------------------

        masquerTousLesFormulaires();
    }

    @FXML
    private void afficherProgrammeHebdomadaire() {
        masquerTousLesFormulaires();
        RegimeAlimentaire regime = getActualActiveRegime();
        if (regime == null) {
            showError("Régime Introuvable", "Veuillez d'abord sélectionner un régime actif dans l'explorateur.");
            return;
        }

        paneWeeklyProgram.setVisible(true);
        paneWeeklyProgram.setManaged(true);
        boxWeeklyDaysContainer.getChildren().clear();

        // Récupérer les repas de la semaine pour ce régime
        List<Repas> tousLesRepas = serviceRepas.getAll();
        Map<LocalDate, List<Repas>> mealsByDate = tousLesRepas.stream()
                .filter(r -> r.getRegimeId() != null && r.getRegimeId().equals(regime.getId()))
                .filter(r -> r.getDateRepas() != null)
                .collect(Collectors.groupingBy(r -> r.getDateRepas().toLocalDate()));

        // Trier les dates
        List<LocalDate> dates = mealsByDate.keySet().stream().sorted().toList();

        if (dates.isEmpty()) {
            Label placeholder = new Label("Aucun programme généré pour cette semaine. Cliquez sur 'Re-générer' pour commencer !");
            placeholder.setStyle("-fx-text-fill: #94a3b8; -fx-padding: 40; -fx-font-style: italic;");
            boxWeeklyDaysContainer.getChildren().add(placeholder);
            return;
        }

        for (LocalDate date : dates) {
            boxWeeklyDaysContainer.getChildren().add(creerRangeeJour(date, mealsByDate.get(date)));
        }
    }

    private VBox creerRangeeJour(LocalDate date, List<Repas> meals) {
        VBox dayCard = new VBox(15);
        dayCard.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 12; -fx-padding: 20; -fx-border-color: #10b981; -fx-border-width: 0 0 0 6;");

        // Header du jour (Date + Jour de la semaine)
        String dayName = date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.FRENCH);
        Label lblDay = new Label(dayName.toUpperCase() + " - " + date.format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM")));
        lblDay.setStyle("-fx-font-weight: 900; -fx-text-fill: #065f46; -fx-font-size: 16px;");
        dayCard.getChildren().add(lblDay);

        HBox mealsRow = new HBox(20);
        for (Repas meal : meals) {
            VBox mealPill = new VBox(5);
            mealPill.setMinWidth(280);
            mealPill.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 5);");

            HBox titleRow = new HBox(10);
            titleRow.setAlignment(Pos.CENTER_LEFT);
            
            VBox textCol = new VBox(2);
            Label lblType = new Label(meal.getTypeRepas());
            lblType.setStyle("-fx-font-size: 10px; -fx-text-fill: #10b981; -fx-font-weight: 800; -fx-background-color: #ecfdf5; -fx-padding: 2 6; -fx-background-radius: 4;");
            
            Label lblNom = new Label(meal.getNomRepas());
            lblNom.setStyle("-fx-font-weight: 700; -fx-text-fill: #1e293b;");
            
            Label lblCals = new Label(meal.getCalories() + " kcal");
            lblCals.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
            
            textCol.getChildren().addAll(lblType, lblNom, lblCals);
            textCol.setPrefWidth(200);

            Button btnAdd = new Button("+");
            btnAdd.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 50; -fx-min-width: 32; -fx-min-height: 32; -fx-font-weight: 900; -fx-cursor: hand;");
            btnAdd.setOnAction(e -> {
                fusionnerRepasSiExiste(meal);
                // Synchronisation forcée pour que le prof voie le changement direct
                actualiserDashboardPlanner();
                rafraichirDonnees();
                
                // Feedback visuel : Rouge pour indiquer que c'est mange
                mealPill.setStyle("-fx-background-color: #fee2e2; -fx-background-radius: 10; -fx-padding: 12; -fx-border-color: #ef4444; -fx-border-width: 2;");
                lblNom.setStyle("-fx-font-weight: 700; -fx-text-fill: #b91c1c;");
                btnAdd.setText("✔");
                btnAdd.setDisable(true); // Eviter les doubles clics accidentels
                btnAdd.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 50; -fx-min-width: 32; -fx-min-height: 32; -fx-font-weight: 900;");
            });

            titleRow.getChildren().addAll(textCol, btnAdd);
            mealPill.getChildren().add(titleRow);
            mealsRow.getChildren().add(mealPill);
        }

        dayCard.getChildren().add(mealsRow);
        
        // Animation au survol
        dayCard.setOnMouseEntered(e -> dayCard.setStyle("-fx-background-color: #dcfce7; -fx-background-radius: 12; -fx-padding: 20; -fx-border-color: #10b981; -fx-border-width: 0 0 0 10;"));
        dayCard.setOnMouseExited(e -> dayCard.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 12; -fx-padding: 20; -fx-border-color: #10b981; -fx-border-width: 0 0 0 6;"));

        return dayCard;
    }

    @FXML
    void genererSemaineIARegimeActif(javafx.event.ActionEvent event) {
        RegimeAlimentaire regimeActif = getActualActiveRegime();
        if (regimeActif == null) {
            showError("Erreur IA", "Vous devez sélectionner un Régime Actif dans l'explorateur ou en créer un avant de lancer l'IA.");
            return;
        }
        
        int cibles = regimeActif.getCaloriesCibles() != null ? regimeActif.getCaloriesCibles() : 2000;
        
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Planificateur Magique IA");
        dialog.setHeaderText("Générer 7 Jours de Repas par l'Intelligence Artificielle");
        dialog.setContentText("Avez-vous un régime particulier ? (Ex: Vegetarian, Gluten Free, Paleo... laissez vide sinon) :");
        
        dialog.showAndWait().ifPresent(diet -> {
            // Afficher une alerte ou barre de progression
            Alert alertLoading = new Alert(Alert.AlertType.INFORMATION);
            alertLoading.setTitle("Création...");
            alertLoading.setHeaderText("L'IA consulte plus de 300 000 recettes...");
            alertLoading.setContentText("Veuillez patienter pendant l'importation de la base de données.");
            alertLoading.show();

            new Thread(() -> {
                try {
                    OkHttpClient client = new OkHttpClient.Builder()
                       .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                       .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                       .build();
    
                    HttpUrl parsedUrl = HttpUrl.parse("https://api.spoonacular.com/mealplanner/generate");
                    if (parsedUrl == null) throw new IOException("URL API Invalide");
                    HttpUrl.Builder urlBuilder = parsedUrl.newBuilder();
                    urlBuilder.addQueryParameter("timeFrame", "week");
                    urlBuilder.addQueryParameter("targetCalories", String.valueOf(cibles));
                    String dietParam = diet.toLowerCase().trim();
                    if (dietParam.contains("végétarien")) dietParam = "vegetarian";
                    if (dietParam.contains("végétalien") || dietParam.contains("vegan")) dietParam = "status:vegan";
                    if (dietParam.contains("sans gluten")) dietParam = "gluten free";
                    if (dietParam.contains("cétogène") || dietParam.contains("keto")) dietParam = "ketogenic";
                    if (dietParam.contains("paléo")) dietParam = "paleo";
                    
                    if (!dietParam.isEmpty()) {
                        urlBuilder.addQueryParameter("diet", dietParam); 
                    }
                    urlBuilder.addQueryParameter("apiKey", API_KEY_SPOONACULAR);
    
                    Request request = new Request.Builder().url(urlBuilder.build().toString()).build();
    
                    try (Response response = client.newCall(request).execute()) {
                        if (!response.isSuccessful()) throw new IOException("Erreur API IA Planificateur: " + response.code());
                        
                        String jsonResp = response.body().string();
                        JSONObject root = new JSONObject(jsonResp);
                        JSONObject week = root.getJSONObject("week");
                        
                        String[] days = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
                        int dayOffset = 0;
                        int totalMealsGenerated = 0;
                        
                        for (String day : days) {
                            JSONObject dayObj = week.optJSONObject(day);
                            if (dayObj != null) {
                                JSONArray meals = dayObj.getJSONArray("meals");
                                JSONObject nutrients = dayObj.getJSONObject("nutrients");
                                
                                // Force l'objectif exact de l'utilisateur
                                double dailyCals = (double) cibles;
                                double[] repartitions = {0.25, 0.45, 0.30};
                                
                                double dailyP = nutrients.optDouble("protein", 60);
                                double dailyC = nutrients.optDouble("carbohydrates", 250);
                                double dailyF = nutrients.optDouble("fat", 70);
                                
                                for (int i = 0; i < meals.length(); i++) {
                                    JSONObject meal = meals.getJSONObject(i);
                                    String title = meal.optString("title", "Repas");
                                    
                                    Repas r = new Repas();
                                    r.setNomRepas(preTraduire(title)); 
                                    
                                    double factor = (i < repartitions.length) ? repartitions[i] : 0.33;
                                    r.setCalories((int) Math.round(dailyCals * factor));
                                    r.setProteines((int) Math.round(dailyP * factor));
                                    r.setGlucides((int) Math.round(dailyC * factor));
                                    r.setLipides((int) Math.round(dailyF * factor));
                                    r.setRegimeId(regimeActif.getId());
                                    
                                    String type = i == 0 ? "Petit dejeuner" : (i == 1 ? "Dejeuner" : "Diner");
                                    r.setTypeRepas(type);
                                    r.setDateRepas(LocalDateTime.now().plusDays(dayOffset));
                                    
                                    User u = determinerUtilisateurReference();
                                    if (u != null) r.setUserId(u.getId());
                                    
                                    // 1. Sauvegarde en BDD SQL pour le planning (persistant)
                                    serviceRepas.add(r);
                                    
                                    totalMealsGenerated++;
                                }
                            }
                            dayOffset++;
                        }
                        
                        final int finalCount = totalMealsGenerated;
                        Platform.runLater(() -> {
                            alertLoading.hide();
                            actualiserDashboardPlanner();
                            rafraichirDonnees();
                            afficherProgrammeHebdomadaire();
                            showInfo("✨ Magie IA Réussie ✨", "L'IA Spoonacular vient de générer et insérer exactement " + finalCount + " repas structurés sur 7 jours ! Ils complètent parfaitement vos " + cibles + " kcal quotidiennes.");
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        alertLoading.hide();
                        String msg = e.getMessage() != null ? e.getMessage() : e.toString();
                        if (msg.contains("502")) {
                            showError("Serveur Surchargé (502)", "Le serveur de nutrition (Spoonacular) est temporairement indisponible. Veuillez réessayer dans quelques instants.");
                        } else {
                            showError("Erreur Algorithme", "L'IA a échoué : " + msg);
                        }
                    });
                }
            }).start();
        });
    }

    private String preTraduire(String source) {
        if (source == null || source.isEmpty()) return "Repas";
        String s = source.toLowerCase();
        
        // Dictionnaire de traduction étendu
        Map<String, String> dict = new LinkedHashMap<>();
        dict.put("chicken", "Poulet");
        dict.put("rice", "Riz");
        dict.put("pasta", "Pâtes");
        dict.put("salad", "Salade");
        dict.put("steak", "Steak");
        dict.put("fish", "Poisson");
        dict.put("salmon", "Saumon");
        dict.put("soup", "Soupe");
        dict.put("with", "avec");
        dict.put("and", "et");
        dict.put("beef", "Bœuf");
        dict.put("pork", "Porc");
        dict.put("veggie", "Végétarien");
        dict.put("roasted", "Grillé");
        dict.put("baked", "Au four");
        dict.put("fried", "Frit");
        dict.put("stew", "Ragoût");
        dict.put("breakfast", "Petit-déjeuner");
        dict.put("burrito", "Burrito");
        dict.put("sauce", "Sauce");
        dict.put("garlic", "Ail");
        dict.put("lemon", "Citron");
        dict.put("potato", "Pomme de terre");
        dict.put("tomato", "Tomate");
        dict.put("cheese", "Fromage");
        dict.put("egg", "Œuf");
        dict.put("meat", "Viande");
        dict.put("pancake", "Crêpe");
        dict.put("spaghetti", "Spaghetti");
        dict.put("noodle", "Nouilles");
        dict.put("bread", "Pain");
        dict.put("cake", "Gâteau");
        dict.put("fruit", "Fruit");
        dict.put("apple", "Pomme");
        dict.put("banana", "Banane");
        dict.put("orange", "Orange");
        dict.put("carrot", "Carotte");
        dict.put("spinach", "Épinards");
        dict.put("mushroom", "Champignons");
        dict.put("shrimp", "Crevettes");
        dict.put("tuna", "Thon");
        dict.put("brown", "Complet");
        dict.put("white", "Blanc");
        dict.put("sweet", "Doux");
        dict.put("spicy", "Épicé");
        dict.put("milk", "Lait");
        dict.put("yogurt", "Yaourt");
        dict.put("yoghourt", "Yaourt");
        dict.put("sandwich", "Sandwich");
        dict.put("pizza", "Pizza");
        dict.put("hot dog", "Hot-Dog");

        for (Map.Entry<String, String> entry : dict.entrySet()) {
            s = s.replace(entry.getKey(), entry.getValue());
        }

        // Capitaliser la première lettre
        if (s.length() > 1) {
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
        return s;
    }

    private String detecterFoodSpoonacular(File file) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        
        // --- Lecture via JavaFX Image (supporte plus de formats : WebP, etc.) ---
        javafx.scene.image.Image fxImage = new javafx.scene.image.Image(file.toURI().toString());
        if (fxImage.isError() || fxImage.getWidth() <= 0) {
            throw new IOException("Impossible de lire l'image. Assurez-vous qu'il s'agit d'un JPG, PNG ou WebP valide.");
        }
        
        // --- Conversion JavaFX -> BufferedImage ---
        int width = (int) fxImage.getWidth();
        int height = (int) fxImage.getHeight();
        
        // Calcul du redimensionnement (max 600px)
        double scale = Math.min(1.0, 600.0 / Math.max(width, height));
        int targetWidth = (int) (width * scale);
        int targetHeight = (int) (height * scale);
        
        BufferedImage bufferedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        javafx.scene.image.PixelReader pr = fxImage.getPixelReader();
        
        // On dessine l'image avec redimensionnement
        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                int srcX = (int) (x / scale);
                int srcY = (int) (y / scale);
                if (srcX < width && srcY < height) {
                    javafx.scene.paint.Color col = pr.getColor(srcX, srcY);
                    int argb = ((int) (col.getOpacity() * 255) << 24) |
                               ((int) (col.getRed() * 255) << 16) |
                               ((int) (col.getGreen() * 255) << 8) |
                               ((int) (col.getBlue() * 255));
                    bufferedImage.setRGB(x, y, argb);
                }
            }
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.jpg",
                        RequestBody.create(imageBytes, MediaType.parse("image/jpeg")))
                .build();

        Request request = new Request.Builder()
                .url("https://api.spoonacular.com/food/images/classify?apiKey=" + API_KEY_SPOONACULAR.trim())
                .addHeader("x-api-key", API_KEY_SPOONACULAR.trim())
                .addHeader("User-Agent", "Mozilla/5.0")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String jsonResp = response.body() != null ? response.body().string() : "{}";
            if (!response.isSuccessful()) throw new IOException("Erreur Spoonacular (" + response.code() + ") : " + jsonResp);
            
            JSONObject root = new JSONObject(jsonResp);
            return root.optString("category", "Plat inconnu");
        }
    }

    private double[] calculerNutritionSpoonacular(String category) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        String cleanCategory = category.replace(" ", "+");
        
        Request request = new Request.Builder()
                .url("https://api.spoonacular.com/recipes/guessNutrition?title=" + cleanCategory)
                .addHeader("x-api-key", API_KEY_SPOONACULAR.trim())
                .build();

        try (Response response = client.newCall(request).execute()) {
            String jsonResp = response.body() != null ? response.body().string() : "{}";
            if (response.code() == 401 || response.code() == 403) throw new IOException("401 Unauthorized : Clé invalide.");
            if (!response.isSuccessful()) throw new IOException("Erreur Nutrition Spoonacular " + response.code() + " : " + jsonResp);
            
            JSONObject root = new JSONObject(jsonResp);
            
            double cal = root.has("calories") ? root.getJSONObject("calories").optDouble("value", 0.0) : 0;
            double prot = root.has("protein") ? root.getJSONObject("protein").optDouble("value", 0.0) : 0;
            double carbs = root.has("carbs") ? root.getJSONObject("carbs").optDouble("value", 0.0) : 0;
            double fat = root.has("fat") ? root.getJSONObject("fat").optDouble("value", 0.0) : 0;

            return new double[]{cal, prot, carbs, fat};
        }
    }

    @FXML
    private void afficherBarcodeScanner() {
        masquerTousLesFormulaires();
        if (paneBarcodeScanner != null) {
            paneBarcodeScanner.setVisible(true);
            paneBarcodeScanner.setManaged(true);
            if (boxBarcodeResult != null) { boxBarcodeResult.setVisible(false); boxBarcodeResult.setManaged(false); }
            if (lblBarcodeStatus != null) lblBarcodeStatus.setText("Initialisation de la webcam...");
            isScanning = true;
            lastScannedCode = "";
        }

        new Thread(() -> {
            try {
                // Safety: make sure to release any previous lock
                for (Webcam w : Webcam.getWebcams()) { if (w.isOpen()) w.close(); }
                
                webcam = Webcam.getDefault();
                if (webcam == null) {
                    Platform.runLater(() -> {
                        showError("Erreur Webcam", "Aucune webcam détectée.");
                        stopperBarcodeScanner();
                    });
                    return;
                }
                
                webcam.setViewSize(WebcamResolution.VGA.getSize());
                try {
                    webcam.open();
                } catch (Exception e) {
                    System.err.println("Webcam locked, trying to re-open...");
                    Webcam.getWebcams().forEach(Webcam::close);
                    webcam.open();
                }

                Platform.runLater(() -> lblBarcodeStatus.setText("En attente d'un code-barres..."));

                while (isScanning) {
                    BufferedImage image = webcam.getImage();
                    if (image != null) {
                        Platform.runLater(() -> ivBarcodeWebcam.setImage(SwingFXUtils.toFXImage(image, null)));
                        
                        try {
                            LuminanceSource source = new BufferedImageLuminanceSource(image);
                            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                            Result result = new MultiFormatReader().decode(bitmap);
                            
                            String code = result.getText();
                            if (!code.equals(lastScannedCode)) {
                                lastScannedCode = code;
                                Platform.runLater(() -> lblBarcodeStatus.setText("Code détecté : " + code));
                                fetchOpenFoodFacts(code);
                            }
                        } catch (NotFoundException e) {
                            // Pas de code détecté dans cette frame
                        }
                    }
                    try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (webcam != null && webcam.isOpen()) webcam.close();
            }
        }).start();
    }

    @FXML
    private void stopperBarcodeScanner() {
        isScanning = false;
        // Hide UI immediately
        if (paneBarcodeScanner != null) {
            paneBarcodeScanner.setVisible(false);
            paneBarcodeScanner.setManaged(false);
        }
        // Unlock scroll
        if (appScrollPane != null) appScrollPane.setMouseTransparent(false);
        
        // Release webcam asynchronously
        new Thread(() -> {
            try {
                if (webcam != null && webcam.isOpen()) {
                    webcam.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        masquerTousLesFormulaires();
    }

    private void fetchOpenFoodFacts(String barcode) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json")
                .header("User-Agent", "Fitopia - Windows - Version 1.0")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> lblBarcodeStatus.setText("Erreur réseau OpenFoodFacts"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    JSONObject root = new JSONObject(json);
                    
                    if (root.getInt("status") == 1) {
                        JSONObject product = root.getJSONObject("product");
                        String name = product.optString("product_name", "Produit inconnu");
                        String brand = product.optString("brands", "Marque inconnue");
                        String nutriScore = product.optString("nutriscore_grade", "?").toUpperCase();
                        
                        double caloriesPer100 = product.optJSONObject("nutriments") != null ? 
                                product.optJSONObject("nutriments").optDouble("energy-kcal_100g", 0.0) : 0.0;

                        Platform.runLater(() -> {
                            lblBarcodeProductName.setText(name);
                            lblBarcodeBrand.setText(brand);
                            lblBarcodeNutriScore.setText(nutriScore);
                            
                            // Style Nutri-Score
                            String color = "#9ca3af"; // Gris par défaut
                            if (nutriScore.equals("A")) color = "#166534"; // Vert foncé
                            if (nutriScore.equals("B")) color = "#22c55e"; // Vert
                            if (nutriScore.equals("C")) color = "#eab308"; // Jaune
                            if (nutriScore.equals("D")) color = "#f97316"; // Orange
                            if (nutriScore.equals("E")) color = "#ef4444"; // Rouge
                            lblBarcodeNutriScore.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 8;");
                            
                            // Vérification compatibilité (Demo simple)
                            if (nutriScore.equals("D") || nutriScore.equals("E")) {
                                lblBarcodeCompatibility.setText("⚠️ Attention : Nutri-Score élevé !");
                                lblBarcodeCompatibility.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                            } else {
                                lblBarcodeCompatibility.setText("✅ Produit sain et compatible.");
                                lblBarcodeCompatibility.setStyle("-fx-text-fill: #166534; -fx-font-weight: bold;");
                            }

                            boxBarcodeResult.setVisible(true);
                            boxBarcodeResult.setManaged(true);
                            
                            // On stocke pour l'application finale
                            lastScannedNom = name;
                            lastScannedCalories = String.valueOf(Math.round(caloriesPer100)); // On prend pour 100g par défaut
                        });
                    } else {
                        Platform.runLater(() -> lblBarcodeStatus.setText("Produit non trouvé : " + barcode));
                    }
                }
            }
        });
    }

    @FXML
    private void appliquerProduitBarcode() {
        // Appliquer les données au formulaire de repas
        if (tfRepasNom != null) tfRepasNom.setText(lastScannedNom);
        if (tfRepasCalories != null) tfRepasCalories.setText(lastScannedCalories);
        
        stopperBarcodeScanner();
        afficherCreationRepas(); // Ouvre le formulaire pour finir la saisie
    }

    @FXML
    void hoverScannerOn(javafx.scene.input.MouseEvent event) {
        javafx.scene.Node btn = (javafx.scene.Node) event.getSource();
        btn.setScaleX(1.05);
        btn.setScaleY(1.05);
    }

    @FXML
    void hoverScannerOff(javafx.scene.input.MouseEvent event) {
        javafx.scene.Node btn = (javafx.scene.Node) event.getSource();
        btn.setScaleX(1.0);
        btn.setScaleY(1.0);
    }
}
