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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
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
import java.util.stream.Collectors;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

    private final ServiceRepas serviceRepas = new ServiceRepas();
    private final ServiceRegimeAlimentaire serviceRegime = new ServiceRegimeAlimentaire();
    private final ServiceUser serviceUser = new ServiceUser();
    private final ObservableList<Repas> allRepas = FXCollections.observableArrayList();
    private List<Repas> repasExplorerView = List.of();
    private Integer repasEditSelectionId;

    @FXML
    public void initialize() {
        initialiserColonnes();
        initialiserCombos();
        initialiserExplorateurRepas();
        initialiserSelections();
        rafraichirDonnees();
        afficherModuleRepas();
        afficherCreationRepas();
        afficherCreationRegime();
    }

    public void ouvrirBackRepas() {
        afficherModuleRepas();
        afficherCreationRepas();
    }

    public void ouvrirFrontRegimes() {
        afficherModuleRegimes();
        afficherCreationRegime();
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
    private void rafraichirDonnees() {
        List<User> users = serviceUser.getAll();
        List<RegimeAlimentaire> regimes = serviceRegime.getAll();
        List<Repas> repas = serviceRepas.getAll();

        cbRepasUser.setItems(FXCollections.observableArrayList(users));
        cbRepasUserEdit.setItems(FXCollections.observableArrayList(users));
        cbRegimeUser.setItems(FXCollections.observableArrayList(users));
        cbRegimeUserEdit.setItems(FXCollections.observableArrayList(users));

        cbRepasRegime.setItems(FXCollections.observableArrayList(regimes));
        cbRepasRegimeEdit.setItems(FXCollections.observableArrayList(regimes));

        allRepas.setAll(repas);
        tableRepas.setItems(FXCollections.observableArrayList(repas));
        tableRepasEdit.setItems(FXCollections.observableArrayList(repas));
        tableRepasDelete.setItems(FXCollections.observableArrayList(repas));
        restaurerSelectionRepasEdit(repas);

        tableRegimes.setItems(FXCollections.observableArrayList(regimes));
        tableRegimesEdit.setItems(FXCollections.observableArrayList(regimes));
        tableRegimesDelete.setItems(FXCollections.observableArrayList(regimes));
        actualiserModificationRepas();
        actualiserSuppressionRepas();
        actualiserExplorateurRepas();
    }

    @FXML
    private void ajouterRepas() {
        try {
            serviceRepas.add(construireRepas(false));
            rafraichirDonnees();
            viderFormulaireRepas();
            showInfo("Repas ajoute avec succes.");
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
    private void viderFormulaireRepas() {
        cbRepasUser.getSelectionModel().clearSelection();
        dpDateRepas.setValue(null);
        cbTypeRepas.getSelectionModel().clearSelection();
        tfRepasNom.clear();
        taRepasDescription.clear();
        cbRepasRegime.getSelectionModel().clearSelection();
        tfRepasCalories.clear();
        tfRepasProteines.clear();
        tfRepasGlucides.clear();
        tfRepasLipides.clear();
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
        cbRegimeUser.getSelectionModel().clearSelection();
        cbRegimeTypeSante.getSelectionModel().clearSelection();
        tfRegimeTaille.clear();
        tfRegimePoids.clear();
        tfRegimeAge.clear();
        tfRegimeBmi.clear();
        tfRegimeCaloriesCibles.clear();
        taRegimeRepasAdequats.clear();
    }

    @FXML
    private void viderFormulaireRegimeModification() {
        tfRegimeId.clear();
        cbRegimeUserEdit.getSelectionModel().clearSelection();
        cbRegimeTypeSanteEdit.getSelectionModel().clearSelection();
        tfRegimeTailleEdit.clear();
        tfRegimePoidsEdit.clear();
        tfRegimeAgeEdit.clear();
        tfRegimeBmiEdit.clear();
        tfRegimeCaloriesCiblesEdit.clear();
        taRegimeRepasAdequatsEdit.clear();
        tableRegimesEdit.getSelectionModel().clearSelection();
    }

    private void initialiserColonnes() {
        initialiserColonnesRepas(colRepasId, colRepasUser, colRepasNom, colRepasType, colRepasDate, colRepasCalories, colRepasRegimeId);
        initialiserColonnesRepas(colRepasIdEdit, colRepasUserEdit, colRepasNomEdit, colRepasTypeEdit, colRepasDateEdit, colRepasCaloriesEdit, colRepasRegimeIdEdit);
        initialiserColonnesRepas(colRepasIdDelete, colRepasUserDelete, colRepasNomDelete, colRepasTypeDelete, colRepasDateDelete, colRepasCaloriesDelete, colRepasRegimeIdDelete);
        colRepasProteines.setCellValueFactory(new PropertyValueFactory<>("proteines"));
        colRepasGlucides.setCellValueFactory(new PropertyValueFactory<>("glucides"));
        colRepasLipides.setCellValueFactory(new PropertyValueFactory<>("lipides"));

        initialiserColonnesRegimes(colRegimeId, colRegimeUser, colRegimeType, colRegimeCalories, colRegimeBmi, colRegimeRepas);
        initialiserColonnesRegimes(colRegimeIdEdit, colRegimeUserEdit, colRegimeTypeEdit, colRegimeCaloriesEdit, colRegimeBmiEdit, colRegimeRepasEdit);
        initialiserColonnesRegimes(colRegimeIdDelete, colRegimeUserDelete, colRegimeTypeDelete, colRegimeCaloriesDelete, colRegimeBmiDelete, colRegimeRepasDelete);
        colRegimeTaille.setCellValueFactory(new PropertyValueFactory<>("taille"));
        colRegimePoids.setCellValueFactory(new PropertyValueFactory<>("poids"));
        colRegimeAge.setCellValueFactory(new PropertyValueFactory<>("age"));
    }

    private void initialiserColonnesRepas(TableColumn<Repas, Integer> id,
                                          TableColumn<Repas, String> user,
                                          TableColumn<Repas, String> nom,
                                          TableColumn<Repas, String> type,
                                          TableColumn<Repas, String> date,
                                          TableColumn<Repas, Integer> calories,
                                          TableColumn<Repas, String> regime) {
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        user.setCellValueFactory(new PropertyValueFactory<>("userEmail"));
        nom.setCellValueFactory(new PropertyValueFactory<>("nomRepas"));
        type.setCellValueFactory(cellData -> new SimpleStringProperty(normaliserTypeRepas(cellData.getValue().getTypeRepas())));
        date.setCellValueFactory(new PropertyValueFactory<>("dateDisplay"));
        calories.setCellValueFactory(new PropertyValueFactory<>("calories"));
        regime.setCellValueFactory(new PropertyValueFactory<>("regimeDisplay"));
    }

    private void initialiserColonnesRegimes(TableColumn<RegimeAlimentaire, Integer> id,
                                            TableColumn<RegimeAlimentaire, String> user,
                                            TableColumn<RegimeAlimentaire, String> type,
                                            TableColumn<RegimeAlimentaire, Integer> calories,
                                            TableColumn<RegimeAlimentaire, Double> bmi,
                                            TableColumn<RegimeAlimentaire, String> repas) {
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        user.setCellValueFactory(new PropertyValueFactory<>("userEmail"));
        type.setCellValueFactory(new PropertyValueFactory<>("typeSante"));
        calories.setCellValueFactory(new PropertyValueFactory<>("caloriesCibles"));
        bmi.setCellValueFactory(new PropertyValueFactory<>("bmi"));
        repas.setCellValueFactory(new PropertyValueFactory<>("repasAdequats"));
    }

    private void initialiserCombos() {
        cbTypeRepas.setItems(FXCollections.observableArrayList(TYPE_REPAS_OPTIONS));
        cbTypeRepasEdit.setItems(FXCollections.observableArrayList(TYPE_REPAS_OPTIONS));
        cbRegimeTypeSante.setItems(FXCollections.observableArrayList(TYPE_SANTE_OPTIONS));
        cbRegimeTypeSanteEdit.setItems(FXCollections.observableArrayList(TYPE_SANTE_OPTIONS));
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
        cbRepasUser.setConverter(userConverter);
        cbRepasUserEdit.setConverter(userConverter);
        cbRegimeUser.setConverter(userConverter);
        cbRegimeUserEdit.setConverter(userConverter);

        StringConverter<RegimeAlimentaire> regimeConverter = new StringConverter<>() {
            @Override public String toString(RegimeAlimentaire regime) { return regime == null ? "" : regime.getDisplayLabel(); }
            @Override public RegimeAlimentaire fromString(String string) { return null; }
        };
        cbRepasRegime.setConverter(regimeConverter);
        cbRepasRegimeEdit.setConverter(regimeConverter);
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
        tableRegimesEdit.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) remplirFormulaireRegime(newValue);
        });
        tableRegimesDelete.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                lblRegimeSelectionDelete.setText(newValue == null ? "Aucun regime selectionne" : "Regime selectionne: #" + newValue.getId()));
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

        if (user == null) throw new IllegalArgumentException("Selectionnez un utilisateur pour le regime.");

        return new RegimeAlimentaire(
                user.getId(),
                parseDouble(tailleField.getText()),
                parseDouble(poidsField.getText()),
                parseInteger(ageField.getText()),
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

    private HBox creerCarteSuppressionRepas(Repas repas) {
        Label title = new Label(valeurOuDefaut(repas.getNomRepas(), "Repas sans nom"));
        title.getStyleClass().add("repas-delete-title");

        Label meta = new Label(valeurOuDefaut(repas.getDateDisplay(), "--") + " · " + valeurOuDefaut(repas.getUserEmail(), "Utilisateur inconnu"));
        meta.getStyleClass().add("repas-delete-meta");

        Label type = new Label(normaliserTypeRepas(repas.getTypeRepas()));
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

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) return null;
        return Double.parseDouble(value.trim().replace(",", "."));
    }

    private String toText(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private void afficherPaneRepas(VBox paneActif, Button boutonActif) {
        afficherVue(paneActif, paneRepasCreate, paneRepasEdit, paneRepasDelete, paneRepasExplore);
        activerBoutonAction(boutonActif, btnActionCreerRepas, btnActionModifierRepas, btnActionSupprimerRepas, btnActionExplorerRepas);
    }

    private void afficherPaneRegime(VBox paneActif, Button boutonActif) {
        afficherVue(paneActif, paneRegimeCreate, paneRegimeEdit, paneRegimeDelete, paneRegimeExplore);
        activerBoutonAction(boutonActif, btnActionCreerRegime, btnActionModifierRegime, btnActionSupprimerRegime, btnActionExplorerRegime);
    }

    private void afficherVue(VBox vueActive, VBox... vues) {
        for (VBox vue : vues) {
            boolean active = vue == vueActive;
            vue.setVisible(active);
            vue.setManaged(active);
        }
    }

    private void activerBoutonModule(Button boutonActif, Button... autres) {
        setStyleFlag(boutonActif, "sidebar-nav-button-active", true);
        for (Button button : autres) setStyleFlag(button, "sidebar-nav-button-active", false);
    }

    private void activerBoutonAction(Button boutonActif, Button... boutons) {
        // Keep option cards visually identical; switching only changes the visible panel.
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
}
