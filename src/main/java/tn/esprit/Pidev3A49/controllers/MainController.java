package tn.esprit.Pidev3A49.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import tn.esprit.Pidev3A49.Models.FitnessExercise;
import tn.esprit.Pidev3A49.Models.Repas;
import tn.esprit.Pidev3A49.Models.RegimeAlimentaire;
import tn.esprit.Pidev3A49.Models.TypeRepas;
import tn.esprit.Pidev3A49.services.ServiceFitnessExercise;
import tn.esprit.Pidev3A49.services.ServiceRepas;
import tn.esprit.Pidev3A49.services.ServiceRegimeAlimentaire;
import tn.esprit.Pidev3A49.utils.AppSession;

import java.awt.Desktop;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainController {

    // Static filter options used by the exercise view.
    private static final List<String> MUSCLE_GROUPS = List.of(
            "Poitrine", "Dos", "Jambes", "Epaules", "Bras", "Abdominaux", "Fessiers", "Cardio"
    );
    private static final List<String> DIFFICULTY_LEVELS = List.of("Debutant", "Intermediaire", "Avance");
    private static final String FILTER_ALL = "Tous";
    private static final int EXERCISE_PAGE_SIZE = 6;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private VBox viewRepas;
    @FXML private VBox viewRegimes;
    @FXML private VBox viewExercises;
    @FXML private VBox paneRepasCreate;
    @FXML private VBox paneRepasEdit;
    @FXML private VBox paneRepasDelete;
    @FXML private VBox paneRepasExplore;
    @FXML private VBox paneRegimeCreate;
    @FXML private VBox paneRegimeEdit;
    @FXML private VBox paneRegimeDelete;
    @FXML private VBox paneRegimeExplore;
    @FXML private VBox paneExerciseCreate;
    @FXML private VBox paneExerciseEdit;
    @FXML private VBox paneExerciseDelete;
    @FXML private VBox paneExerciseExplore;

    @FXML private Button btnModuleRepas;
    @FXML private Button btnModuleRegimes;
    @FXML private Button btnModuleExercises;
    @FXML private Button btnActionCreerRepas;
    @FXML private Button btnActionModifierRepas;
    @FXML private Button btnActionSupprimerRepas;
    @FXML private Button btnActionExplorerRepas;
    @FXML private Button btnActionCreerRegime;
    @FXML private Button btnActionModifierRegime;
    @FXML private Button btnActionSupprimerRegime;
    @FXML private Button btnActionExplorerRegime;
    @FXML private Button btnActionCreerExercise;
    @FXML private Button btnActionModifierExercise;
    @FXML private Button btnActionSupprimerExercise;
    @FXML private Button btnActionExplorerExercise;
    @FXML private Button btnExercisePreviousPage;
    @FXML private Button btnExerciseNextPage;

    @FXML private Label lblCurrentUserEmail;
    @FXML private Label lblCurrentUserRole;

    @FXML private TextField tfRegimeId;
    @FXML private TextField tfRegimeNom;
    @FXML private TextArea taRegimeDescription;
    @FXML private TextField tfRegimeObjectifCalorique;
    @FXML private CheckBox chkRegimeActif;
    @FXML private TextField tfRegimeNomEdit;
    @FXML private TextArea taRegimeDescriptionEdit;
    @FXML private TextField tfRegimeObjectifCaloriqueEdit;
    @FXML private CheckBox chkRegimeActifEdit;
    @FXML private Label lblRegimeSelectionDelete;
    @FXML private TableView<RegimeAlimentaire> tableRegimes;
    @FXML private TableView<RegimeAlimentaire> tableRegimesEdit;
    @FXML private TableView<RegimeAlimentaire> tableRegimesDelete;
    @FXML private TableColumn<RegimeAlimentaire, Integer> colRegimeId;
    @FXML private TableColumn<RegimeAlimentaire, String> colRegimeNom;
    @FXML private TableColumn<RegimeAlimentaire, String> colRegimeDescription;
    @FXML private TableColumn<RegimeAlimentaire, Integer> colRegimeObjectifCalorique;
    @FXML private TableColumn<RegimeAlimentaire, Boolean> colRegimeActif;
    @FXML private TableColumn<RegimeAlimentaire, Integer> colRegimeIdEdit;
    @FXML private TableColumn<RegimeAlimentaire, String> colRegimeNomEdit;
    @FXML private TableColumn<RegimeAlimentaire, String> colRegimeDescriptionEdit;
    @FXML private TableColumn<RegimeAlimentaire, Integer> colRegimeObjectifCaloriqueEdit;
    @FXML private TableColumn<RegimeAlimentaire, Boolean> colRegimeActifEdit;
    @FXML private TableColumn<RegimeAlimentaire, Integer> colRegimeIdDelete;
    @FXML private TableColumn<RegimeAlimentaire, String> colRegimeNomDelete;
    @FXML private TableColumn<RegimeAlimentaire, String> colRegimeDescriptionDelete;
    @FXML private TableColumn<RegimeAlimentaire, Integer> colRegimeObjectifCaloriqueDelete;
    @FXML private TableColumn<RegimeAlimentaire, Boolean> colRegimeActifDelete;

    @FXML private TextField tfRepasId;
    @FXML private TextField tfRepasNom;
    @FXML private TextArea taRepasDescription;
    @FXML private TextField tfRepasCalories;
    @FXML private ComboBox<TypeRepas> cbTypeRepas;
    @FXML private DatePicker dpDateRepas;
    @FXML private ComboBox<RegimeAlimentaire> cbRepasRegime;
    @FXML private TextField tfRepasNomEdit;
    @FXML private TextArea taRepasDescriptionEdit;
    @FXML private TextField tfRepasCaloriesEdit;
    @FXML private ComboBox<TypeRepas> cbTypeRepasEdit;
    @FXML private DatePicker dpDateRepasEdit;
    @FXML private ComboBox<RegimeAlimentaire> cbRepasRegimeEdit;
    @FXML private Label lblRepasSelectionDelete;
    @FXML private TableView<Repas> tableRepas;
    @FXML private TableView<Repas> tableRepasEdit;
    @FXML private TableView<Repas> tableRepasDelete;
    @FXML private TableColumn<Repas, Integer> colRepasId;
    @FXML private TableColumn<Repas, String> colRepasNom;
    @FXML private TableColumn<Repas, String> colRepasDescription;
    @FXML private TableColumn<Repas, Integer> colRepasCalories;
    @FXML private TableColumn<Repas, TypeRepas> colRepasType;
    @FXML private TableColumn<Repas, Object> colRepasDate;
    @FXML private TableColumn<Repas, Integer> colRepasRegimeId;
    @FXML private TableColumn<Repas, Integer> colRepasIdEdit;
    @FXML private TableColumn<Repas, String> colRepasNomEdit;
    @FXML private TableColumn<Repas, String> colRepasDescriptionEdit;
    @FXML private TableColumn<Repas, Integer> colRepasCaloriesEdit;
    @FXML private TableColumn<Repas, TypeRepas> colRepasTypeEdit;
    @FXML private TableColumn<Repas, Object> colRepasDateEdit;
    @FXML private TableColumn<Repas, Integer> colRepasRegimeIdEdit;
    @FXML private TableColumn<Repas, Integer> colRepasIdDelete;
    @FXML private TableColumn<Repas, String> colRepasNomDelete;
    @FXML private TableColumn<Repas, String> colRepasDescriptionDelete;
    @FXML private TableColumn<Repas, Integer> colRepasCaloriesDelete;
    @FXML private TableColumn<Repas, TypeRepas> colRepasTypeDelete;
    @FXML private TableColumn<Repas, Object> colRepasDateDelete;
    @FXML private TableColumn<Repas, Integer> colRepasRegimeIdDelete;

    @FXML private TextField tfExerciseId;
    @FXML private TextField tfExerciseName;
    @FXML private TextArea taExerciseDescription;
    @FXML private ComboBox<String> cbExerciseMuscleGroup;
    @FXML private ComboBox<String> cbExerciseDifficulty;
    @FXML private TextField tfExerciseSets;
    @FXML private TextField tfExerciseRepetitions;
    @FXML private TextField tfExerciseDuration;
    @FXML private TextField tfExerciseVideoUrl;
    @FXML private TextField tfExerciseImageUrl;
    @FXML private TextField tfExerciseNameEdit;
    @FXML private TextArea taExerciseDescriptionEdit;
    @FXML private ComboBox<String> cbExerciseMuscleGroupEdit;
    @FXML private ComboBox<String> cbExerciseDifficultyEdit;
    @FXML private TextField tfExerciseSetsEdit;
    @FXML private TextField tfExerciseRepetitionsEdit;
    @FXML private TextField tfExerciseDurationEdit;
    @FXML private TextField tfExerciseVideoUrlEdit;
    @FXML private TextField tfExerciseImageUrlEdit;
    @FXML private ComboBox<String> cbExerciseFilterMuscleGroup;
    @FXML private ComboBox<String> cbExerciseFilterDifficulty;
    @FXML private Label lblExerciseSelectionDelete;
    @FXML private Label lblExercisePageInfo;
    @FXML private Label lblExerciseResultCount;
    @FXML private Label lblExerciseDetailName;
    @FXML private Label lblExerciseDetailMuscleGroup;
    @FXML private Label lblExerciseDetailDifficulty;
    @FXML private Label lblExerciseDetailPrescription;
    @FXML private Label lblExerciseDetailDates;
    @FXML private Label lblExerciseDetailRole;
    @FXML private TextArea taExerciseDetailDescription;
    @FXML private TextField tfExerciseDetailVideoUrl;
    @FXML private TextField tfExerciseDetailImageUrl;
    @FXML private ImageView imgExercisePreview;
    @FXML private TableView<FitnessExercise> tableExercises;
    @FXML private TableView<FitnessExercise> tableExercisesEdit;
    @FXML private TableView<FitnessExercise> tableExercisesDelete;
    @FXML private TableColumn<FitnessExercise, Integer> colExerciseId;
    @FXML private TableColumn<FitnessExercise, String> colExerciseName;
    @FXML private TableColumn<FitnessExercise, String> colExerciseMuscleGroup;
    @FXML private TableColumn<FitnessExercise, String> colExerciseDifficulty;
    @FXML private TableColumn<FitnessExercise, Integer> colExerciseSets;
    @FXML private TableColumn<FitnessExercise, Integer> colExerciseRepetitions;
    @FXML private TableColumn<FitnessExercise, Integer> colExerciseDuration;
    @FXML private TableColumn<FitnessExercise, LocalDateTime> colExerciseUpdatedAt;
    @FXML private TableColumn<FitnessExercise, Integer> colExerciseIdEdit;
    @FXML private TableColumn<FitnessExercise, String> colExerciseNameEdit;
    @FXML private TableColumn<FitnessExercise, String> colExerciseMuscleGroupEdit;
    @FXML private TableColumn<FitnessExercise, String> colExerciseDifficultyEdit;
    @FXML private TableColumn<FitnessExercise, Integer> colExerciseSetsEdit;
    @FXML private TableColumn<FitnessExercise, Integer> colExerciseRepetitionsEdit;
    @FXML private TableColumn<FitnessExercise, Integer> colExerciseDurationEdit;
    @FXML private TableColumn<FitnessExercise, LocalDateTime> colExerciseUpdatedAtEdit;
    @FXML private TableColumn<FitnessExercise, Integer> colExerciseIdDelete;
    @FXML private TableColumn<FitnessExercise, String> colExerciseNameDelete;
    @FXML private TableColumn<FitnessExercise, String> colExerciseMuscleGroupDelete;
    @FXML private TableColumn<FitnessExercise, String> colExerciseDifficultyDelete;
    @FXML private TableColumn<FitnessExercise, Integer> colExerciseSetsDelete;
    @FXML private TableColumn<FitnessExercise, Integer> colExerciseRepetitionsDelete;
    @FXML private TableColumn<FitnessExercise, Integer> colExerciseDurationDelete;
    @FXML private TableColumn<FitnessExercise, LocalDateTime> colExerciseUpdatedAtDelete;

    private final AppSession appSession = AppSession.getInstance();
    private final ServiceRegimeAlimentaire serviceRegime = new ServiceRegimeAlimentaire();
    private final ServiceRepas serviceRepas = new ServiceRepas();
    private final ServiceFitnessExercise serviceFitnessExercise = new ServiceFitnessExercise();

    private List<FitnessExercise> filteredExercises = new ArrayList<>();
    private int currentExercisePage = 1;

    @FXML
    public void initialize() {
        initialiserSession();
        initialiserTableRegimes();
        initialiserTableRepas();
        initialiserTableExercises();
        initialiserComboBoxes();
        initialiserSelections();
        chargerRegimes();
        chargerRepas();
        chargerExercises();
        afficherModuleRepas();
        afficherCreationRepas();
        afficherCreationRegime();
        afficherCreationExercise();
    }

    @FXML
    private void afficherModuleRepas() {
        afficherVue(viewRepas, viewRepas, viewRegimes, viewExercises);
        activerBoutonModule(btnModuleRepas, btnModuleRegimes, btnModuleExercises);
    }

    @FXML
    private void afficherModuleRegimes() {
        afficherVue(viewRegimes, viewRepas, viewRegimes, viewExercises);
        activerBoutonModule(btnModuleRegimes, btnModuleRepas, btnModuleExercises);
    }

    @FXML
    private void afficherModuleExercises() {
        verifierAuthentification();
        afficherVue(viewExercises, viewRepas, viewRegimes, viewExercises);
        activerBoutonModule(btnModuleExercises, btnModuleRepas, btnModuleRegimes);
    }

    @FXML private void afficherCreationRepas() { afficherPaneRepas(paneRepasCreate, btnActionCreerRepas); }
    @FXML private void afficherModificationRepas() { afficherPaneRepas(paneRepasEdit, btnActionModifierRepas); }
    @FXML private void afficherSuppressionRepas() { afficherPaneRepas(paneRepasDelete, btnActionSupprimerRepas); }
    @FXML private void afficherExplorationRepas() { afficherPaneRepas(paneRepasExplore, btnActionExplorerRepas); }
    @FXML private void afficherCreationRegime() { afficherPaneRegime(paneRegimeCreate, btnActionCreerRegime); }
    @FXML private void afficherModificationRegime() { afficherPaneRegime(paneRegimeEdit, btnActionModifierRegime); }
    @FXML private void afficherSuppressionRegime() { afficherPaneRegime(paneRegimeDelete, btnActionSupprimerRegime); }
    @FXML private void afficherExplorationRegime() { afficherPaneRegime(paneRegimeExplore, btnActionExplorerRegime); }
    @FXML private void afficherCreationExercise() { afficherPaneExercise(paneExerciseCreate, btnActionCreerExercise); }
    @FXML private void afficherModificationExercise() { afficherPaneExercise(paneExerciseEdit, btnActionModifierExercise); }
    @FXML private void afficherSuppressionExercise() { afficherPaneExercise(paneExerciseDelete, btnActionSupprimerExercise); }
    @FXML private void afficherExplorationExercise() { afficherPaneExercise(paneExerciseExplore, btnActionExplorerExercise); }

    @FXML
    private void rafraichirDonnees() {
        chargerRegimes();
        chargerRepas();
        chargerExercises();
    }

    @FXML
    private void ajouterRegime() {
        try {
            serviceRegime.add(construireRegimeDepuisCreation());
            chargerRegimes();
            chargerRepas();
            viderFormulaireRegime();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Regime alimentaire ajoute avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur regime", exception.getMessage());
        }
    }

    @FXML
    private void modifierRegime() {
        try {
            if (tfRegimeId.getText().isBlank()) {
                throw new IllegalArgumentException("Selectionne un regime alimentaire a modifier.");
            }
            RegimeAlimentaire regime = construireRegimeDepuisModification();
            regime.setId(Integer.parseInt(tfRegimeId.getText()));
            serviceRegime.update(regime);
            chargerRegimes();
            chargerRepas();
            viderFormulaireRegimeModification();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Regime alimentaire modifie avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur regime", exception.getMessage());
        }
    }

    @FXML
    private void supprimerRegime() {
        try {
            RegimeAlimentaire regime = tableRegimesDelete.getSelectionModel().getSelectedItem();
            if (regime == null) {
                throw new IllegalArgumentException("Selectionne un regime alimentaire a supprimer.");
            }
            serviceRegime.delete(regime);
            chargerRegimes();
            chargerRepas();
            viderFormulaireRegimeModification();
            lblRegimeSelectionDelete.setText("Aucun regime selectionne");
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Regime alimentaire supprime avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur regime", exception.getMessage());
        }
    }

    @FXML
    private void viderFormulaireRegime() {
        tfRegimeNom.clear();
        taRegimeDescription.clear();
        tfRegimeObjectifCalorique.clear();
        chkRegimeActif.setSelected(true);
    }

    @FXML
    private void viderFormulaireRegimeModification() {
        tfRegimeId.clear();
        tfRegimeNomEdit.clear();
        taRegimeDescriptionEdit.clear();
        tfRegimeObjectifCaloriqueEdit.clear();
        chkRegimeActifEdit.setSelected(true);
        tableRegimesEdit.getSelectionModel().clearSelection();
    }

    @FXML
    private void ajouterRepas() {
        try {
            serviceRepas.add(construireRepasDepuisCreation());
            chargerRepas();
            viderFormulaireRepas();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Repas ajoute avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur repas", exception.getMessage());
        }
    }

    @FXML
    private void modifierRepas() {
        try {
            if (tfRepasId.getText().isBlank()) {
                throw new IllegalArgumentException("Selectionne un repas a modifier.");
            }
            Repas repas = construireRepasDepuisModification();
            repas.setId(Integer.parseInt(tfRepasId.getText()));
            serviceRepas.update(repas);
            chargerRepas();
            viderFormulaireRepasModification();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Repas modifie avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur repas", exception.getMessage());
        }
    }

    @FXML
    private void supprimerRepas() {
        try {
            Repas repas = tableRepasDelete.getSelectionModel().getSelectedItem();
            if (repas == null) {
                throw new IllegalArgumentException("Selectionne un repas a supprimer.");
            }
            serviceRepas.delete(repas);
            chargerRepas();
            viderFormulaireRepasModification();
            lblRepasSelectionDelete.setText("Aucun repas selectionne");
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Repas supprime avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur repas", exception.getMessage());
        }
    }

    @FXML
    private void viderFormulaireRepas() {
        tfRepasNom.clear();
        taRepasDescription.clear();
        tfRepasCalories.clear();
        cbTypeRepas.getSelectionModel().clearSelection();
        cbRepasRegime.getSelectionModel().clearSelection();
        dpDateRepas.setValue(null);
    }

    @FXML
    private void viderFormulaireRepasModification() {
        tfRepasId.clear();
        tfRepasNomEdit.clear();
        taRepasDescriptionEdit.clear();
        tfRepasCaloriesEdit.clear();
        cbTypeRepasEdit.getSelectionModel().clearSelection();
        cbRepasRegimeEdit.getSelectionModel().clearSelection();
        dpDateRepasEdit.setValue(null);
        tableRepasEdit.getSelectionModel().clearSelection();
    }

    @FXML
    private void ajouterExercise() {
        try {
            verifierPermissionGestionExercises();
            serviceFitnessExercise.add(construireExerciseDepuisCreation());
            chargerExercises();
            viderFormulaireExercise();
            afficherExplorationExercise();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Exercice ajoute avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur exercice", exception.getMessage());
        }
    }

    @FXML
    private void modifierExercise() {
        try {
            verifierPermissionGestionExercises();
            if (tfExerciseId.getText().isBlank()) {
                throw new IllegalArgumentException("Selectionne un exercice a modifier.");
            }
            FitnessExercise exercise = construireExerciseDepuisModification();
            exercise.setId(Integer.parseInt(tfExerciseId.getText()));
            serviceFitnessExercise.update(exercise);
            chargerExercises();
            viderFormulaireExerciseModification();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Exercice modifie avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur exercice", exception.getMessage());
        }
    }

    @FXML
    private void supprimerExercise() {
        try {
            verifierPermissionGestionExercises();
            FitnessExercise exercise = tableExercisesDelete.getSelectionModel().getSelectedItem();
            if (exercise == null) {
                throw new IllegalArgumentException("Selectionne un exercice a supprimer.");
            }
            serviceFitnessExercise.delete(exercise);
            chargerExercises();
            viderFormulaireExerciseModification();
            lblExerciseSelectionDelete.setText("Aucun exercice selectionne");
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Exercice supprime avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur exercice", exception.getMessage());
        }
    }

    @FXML
    private void viderFormulaireExercise() {
        tfExerciseName.clear();
        taExerciseDescription.clear();
        cbExerciseMuscleGroup.getSelectionModel().clearSelection();
        cbExerciseDifficulty.getSelectionModel().clearSelection();
        tfExerciseSets.clear();
        tfExerciseRepetitions.clear();
        tfExerciseDuration.clear();
        tfExerciseVideoUrl.clear();
        tfExerciseImageUrl.clear();
    }

    @FXML
    private void viderFormulaireExerciseModification() {
        tfExerciseId.clear();
        tfExerciseNameEdit.clear();
        taExerciseDescriptionEdit.clear();
        cbExerciseMuscleGroupEdit.getSelectionModel().clearSelection();
        cbExerciseDifficultyEdit.getSelectionModel().clearSelection();
        tfExerciseSetsEdit.clear();
        tfExerciseRepetitionsEdit.clear();
        tfExerciseDurationEdit.clear();
        tfExerciseVideoUrlEdit.clear();
        tfExerciseImageUrlEdit.clear();
        tableExercisesEdit.getSelectionModel().clearSelection();
    }

    @FXML
    private void filtrerExercises() {
        currentExercisePage = 1;
        appliquerFiltresExercises();
    }

    @FXML
    private void reinitialiserFiltresExercises() {
        cbExerciseFilterMuscleGroup.setValue(FILTER_ALL);
        cbExerciseFilterDifficulty.setValue(FILTER_ALL);
        currentExercisePage = 1;
        appliquerFiltresExercises();
    }

    @FXML
    private void pageExercisePrecedente() {
        if (currentExercisePage > 1) {
            currentExercisePage--;
            rafraichirPageExercises();
        }
    }

    @FXML
    private void pageExerciseSuivante() {
        if (currentExercisePage < getExerciseTotalPages()) {
            currentExercisePage++;
            rafraichirPageExercises();
        }
    }

    @FXML
    private void ouvrirVideoExercise() {
        ouvrirLienDepuisChamp(tfExerciseDetailVideoUrl.getText(), "Aucune video disponible pour cet exercice.");
    }

    @FXML
    private void ouvrirImageExercise() {
        ouvrirLienDepuisChamp(tfExerciseDetailImageUrl.getText(), "Aucune image disponible pour cet exercice.");
    }

    private void initialiserSession() {
        lblCurrentUserEmail.setText(appSession.getEmail());
        lblCurrentUserRole.setText(appSession.getPrimaryRole());
    }

    private void initialiserTableRegimes() {
        initialiserColonnesRegime(colRegimeId, colRegimeNom, colRegimeDescription, colRegimeObjectifCalorique, colRegimeActif);
        initialiserColonnesRegime(colRegimeIdEdit, colRegimeNomEdit, colRegimeDescriptionEdit, colRegimeObjectifCaloriqueEdit, colRegimeActifEdit);
        initialiserColonnesRegime(colRegimeIdDelete, colRegimeNomDelete, colRegimeDescriptionDelete, colRegimeObjectifCaloriqueDelete, colRegimeActifDelete);
    }

    private void initialiserTableRepas() {
        initialiserColonnesRepas(colRepasId, colRepasNom, colRepasDescription, colRepasCalories, colRepasType, colRepasDate, colRepasRegimeId);
        initialiserColonnesRepas(colRepasIdEdit, colRepasNomEdit, colRepasDescriptionEdit, colRepasCaloriesEdit, colRepasTypeEdit, colRepasDateEdit, colRepasRegimeIdEdit);
        initialiserColonnesRepas(colRepasIdDelete, colRepasNomDelete, colRepasDescriptionDelete, colRepasCaloriesDelete, colRepasTypeDelete, colRepasDateDelete, colRepasRegimeIdDelete);
    }

    private void initialiserTableExercises() {
        initialiserColonnesExercise(colExerciseId, colExerciseName, colExerciseMuscleGroup, colExerciseDifficulty,
                colExerciseSets, colExerciseRepetitions, colExerciseDuration, colExerciseUpdatedAt);
        initialiserColonnesExercise(colExerciseIdEdit, colExerciseNameEdit, colExerciseMuscleGroupEdit, colExerciseDifficultyEdit,
                colExerciseSetsEdit, colExerciseRepetitionsEdit, colExerciseDurationEdit, colExerciseUpdatedAtEdit);
        initialiserColonnesExercise(colExerciseIdDelete, colExerciseNameDelete, colExerciseMuscleGroupDelete, colExerciseDifficultyDelete,
                colExerciseSetsDelete, colExerciseRepetitionsDelete, colExerciseDurationDelete, colExerciseUpdatedAtDelete);
    }

    private void initialiserColonnesRegime(TableColumn<RegimeAlimentaire, Integer> id,
                                           TableColumn<RegimeAlimentaire, String> nom,
                                           TableColumn<RegimeAlimentaire, String> description,
                                           TableColumn<RegimeAlimentaire, Integer> objectif,
                                           TableColumn<RegimeAlimentaire, Boolean> actif) {
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        nom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        description.setCellValueFactory(new PropertyValueFactory<>("description"));
        objectif.setCellValueFactory(new PropertyValueFactory<>("objectifCalorique"));
        actif.setCellValueFactory(new PropertyValueFactory<>("actif"));
    }

    private void initialiserColonnesRepas(TableColumn<Repas, Integer> id,
                                          TableColumn<Repas, String> nom,
                                          TableColumn<Repas, String> description,
                                          TableColumn<Repas, Integer> calories,
                                          TableColumn<Repas, TypeRepas> type,
                                          TableColumn<Repas, Object> date,
                                          TableColumn<Repas, Integer> regimeId) {
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        nom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        description.setCellValueFactory(new PropertyValueFactory<>("description"));
        calories.setCellValueFactory(new PropertyValueFactory<>("calories"));
        type.setCellValueFactory(new PropertyValueFactory<>("typeRepas"));
        date.setCellValueFactory(new PropertyValueFactory<>("dateRepas"));
        regimeId.setCellValueFactory(new PropertyValueFactory<>("regimeId"));
    }

    private void initialiserColonnesExercise(TableColumn<FitnessExercise, Integer> id,
                                             TableColumn<FitnessExercise, String> name,
                                             TableColumn<FitnessExercise, String> muscleGroup,
                                             TableColumn<FitnessExercise, String> difficulty,
                                             TableColumn<FitnessExercise, Integer> sets,
                                             TableColumn<FitnessExercise, Integer> repetitions,
                                             TableColumn<FitnessExercise, Integer> duration,
                                             TableColumn<FitnessExercise, LocalDateTime> updatedAt) {
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        muscleGroup.setCellValueFactory(new PropertyValueFactory<>("muscleGroup"));
        difficulty.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        sets.setCellValueFactory(new PropertyValueFactory<>("sets"));
        repetitions.setCellValueFactory(new PropertyValueFactory<>("repetitions"));
        duration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        updatedAt.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));
    }

    private void initialiserComboBoxes() {
        cbTypeRepas.setItems(FXCollections.observableArrayList(TypeRepas.values()));
        cbTypeRepasEdit.setItems(FXCollections.observableArrayList(TypeRepas.values()));

        StringConverter<RegimeAlimentaire> converter = new StringConverter<>() {
            @Override
            public String toString(RegimeAlimentaire regimeAlimentaire) {
                return regimeAlimentaire == null ? "" : regimeAlimentaire.getId() + " - " + regimeAlimentaire.getNom();
            }

            @Override
            public RegimeAlimentaire fromString(String string) {
                return null;
            }
        };

        cbRepasRegime.setConverter(converter);
        cbRepasRegimeEdit.setConverter(converter);

        cbExerciseMuscleGroup.setItems(FXCollections.observableArrayList(MUSCLE_GROUPS));
        cbExerciseMuscleGroupEdit.setItems(FXCollections.observableArrayList(MUSCLE_GROUPS));
        cbExerciseDifficulty.setItems(FXCollections.observableArrayList(DIFFICULTY_LEVELS));
        cbExerciseDifficultyEdit.setItems(FXCollections.observableArrayList(DIFFICULTY_LEVELS));

        List<String> muscleFilters = new ArrayList<>();
        muscleFilters.add(FILTER_ALL);
        muscleFilters.addAll(MUSCLE_GROUPS);
        cbExerciseFilterMuscleGroup.setItems(FXCollections.observableArrayList(muscleFilters));
        cbExerciseFilterMuscleGroup.setValue(FILTER_ALL);

        List<String> difficultyFilters = new ArrayList<>();
        difficultyFilters.add(FILTER_ALL);
        difficultyFilters.addAll(DIFFICULTY_LEVELS);
        cbExerciseFilterDifficulty.setItems(FXCollections.observableArrayList(difficultyFilters));
        cbExerciseFilterDifficulty.setValue(FILTER_ALL);

        rafraichirComboRegimes();
    }

    private void initialiserSelections() {
        tableRegimesEdit.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                remplirFormulaireRegimeModification(newValue);
            }
        });

        tableRegimesDelete.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                lblRegimeSelectionDelete.setText(newValue == null ? "Aucun regime selectionne" : "Regime selectionne: " + newValue.getNom()));

        tableRepasEdit.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                remplirFormulaireRepasModification(newValue);
            }
        });

        tableRepasDelete.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                lblRepasSelectionDelete.setText(newValue == null ? "Aucun repas selectionne" : "Repas selectionne: " + newValue.getNom()));

        tableExercisesEdit.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                remplirFormulaireExerciseModification(newValue);
            }
        });

        tableExercisesDelete.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                lblExerciseSelectionDelete.setText(newValue == null ? "Aucun exercice selectionne" : "Exercice selectionne: " + newValue.getName()));

        tableExercises.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                mettreAJourDetailsExercise(newValue));
    }

    private void chargerRegimes() {
        List<RegimeAlimentaire> regimes = serviceRegime.getAll();
        tableRegimes.setItems(FXCollections.observableArrayList(regimes));
        tableRegimesEdit.setItems(FXCollections.observableArrayList(regimes));
        tableRegimesDelete.setItems(FXCollections.observableArrayList(regimes));
        rafraichirComboRegimes();
    }

    private void chargerRepas() {
        List<Repas> repas = serviceRepas.getAll();
        tableRepas.setItems(FXCollections.observableArrayList(repas));
        tableRepasEdit.setItems(FXCollections.observableArrayList(repas));
        tableRepasDelete.setItems(FXCollections.observableArrayList(repas));
    }

    private void chargerExercises() {
        List<FitnessExercise> exercises = serviceFitnessExercise.getAll();
        tableExercisesEdit.setItems(FXCollections.observableArrayList(exercises));
        tableExercisesDelete.setItems(FXCollections.observableArrayList(exercises));
        appliquerFiltresExercises();
    }

    private void appliquerFiltresExercises() {
        String selectedMuscleGroup = cbExerciseFilterMuscleGroup.getValue();
        String selectedDifficulty = cbExerciseFilterDifficulty.getValue();

        filteredExercises = serviceFitnessExercise.getAll().stream()
                .filter(exercise -> FILTER_ALL.equals(selectedMuscleGroup) || exercise.getMuscleGroup().equalsIgnoreCase(selectedMuscleGroup))
                .filter(exercise -> FILTER_ALL.equals(selectedDifficulty) || exercise.getDifficulty().equalsIgnoreCase(selectedDifficulty))
                .collect(Collectors.toList());

        if (currentExercisePage > getExerciseTotalPages()) {
            currentExercisePage = getExerciseTotalPages();
        }
        if (currentExercisePage <= 0) {
            currentExercisePage = 1;
        }
        rafraichirPageExercises();
    }

    private void rafraichirPageExercises() {
        if (filteredExercises.isEmpty()) {
            tableExercises.setItems(FXCollections.observableArrayList());
            lblExerciseResultCount.setText("0 exercice");
            lblExercisePageInfo.setText("Page 0 / 0");
            btnExercisePreviousPage.setDisable(true);
            btnExerciseNextPage.setDisable(true);
            mettreAJourDetailsExercise(null);
            return;
        }

        int fromIndex = Math.max(0, (currentExercisePage - 1) * EXERCISE_PAGE_SIZE);
        int toIndex = Math.min(filteredExercises.size(), fromIndex + EXERCISE_PAGE_SIZE);
        List<FitnessExercise> pageItems = filteredExercises.subList(fromIndex, toIndex);
        tableExercises.setItems(FXCollections.observableArrayList(pageItems));
        tableExercises.getSelectionModel().selectFirst();

        lblExerciseResultCount.setText(filteredExercises.size() + (filteredExercises.size() > 1 ? " exercices" : " exercice"));
        lblExercisePageInfo.setText("Page " + currentExercisePage + " / " + getExerciseTotalPages());
        btnExercisePreviousPage.setDisable(currentExercisePage <= 1);
        btnExerciseNextPage.setDisable(currentExercisePage >= getExerciseTotalPages());
    }

    private int getExerciseTotalPages() {
        return Math.max(1, (int) Math.ceil((double) filteredExercises.size() / EXERCISE_PAGE_SIZE));
    }

    private void rafraichirComboRegimes() {
        List<RegimeAlimentaire> regimes = serviceRegime.getAll();
        cbRepasRegime.setItems(FXCollections.observableArrayList(regimes));
        cbRepasRegimeEdit.setItems(FXCollections.observableArrayList(regimes));
    }

    private void remplirFormulaireRegimeModification(RegimeAlimentaire regime) {
        tfRegimeId.setText(String.valueOf(regime.getId()));
        tfRegimeNomEdit.setText(regime.getNom());
        taRegimeDescriptionEdit.setText(regime.getDescription());
        tfRegimeObjectifCaloriqueEdit.setText(String.valueOf(regime.getObjectifCalorique()));
        chkRegimeActifEdit.setSelected(regime.isActif());
    }

    private void remplirFormulaireRepasModification(Repas repas) {
        tfRepasId.setText(String.valueOf(repas.getId()));
        tfRepasNomEdit.setText(repas.getNom());
        taRepasDescriptionEdit.setText(repas.getDescription());
        tfRepasCaloriesEdit.setText(String.valueOf(repas.getCalories()));
        cbTypeRepasEdit.setValue(repas.getTypeRepas());
        dpDateRepasEdit.setValue(repas.getDateRepas());
        selectionnerRegimeDansCombo(cbRepasRegimeEdit, repas.getRegimeId());
    }

    private void remplirFormulaireExerciseModification(FitnessExercise exercise) {
        tfExerciseId.setText(String.valueOf(exercise.getId()));
        tfExerciseNameEdit.setText(exercise.getName());
        taExerciseDescriptionEdit.setText(exercise.getDescription());
        cbExerciseMuscleGroupEdit.setValue(exercise.getMuscleGroup());
        cbExerciseDifficultyEdit.setValue(exercise.getDifficulty());
        tfExerciseSetsEdit.setText(String.valueOf(exercise.getSets()));
        tfExerciseRepetitionsEdit.setText(String.valueOf(exercise.getRepetitions()));
        tfExerciseDurationEdit.setText(String.valueOf(exercise.getDuration()));
        tfExerciseVideoUrlEdit.setText(safeText(exercise.getVideoUrl()));
        tfExerciseImageUrlEdit.setText(safeText(exercise.getImageUrl()));
    }

    private void mettreAJourDetailsExercise(FitnessExercise exercise) {
        if (exercise == null) {
            lblExerciseDetailName.setText("Aucun exercice selectionne");
            lblExerciseDetailMuscleGroup.setText("Groupe musculaire: -");
            lblExerciseDetailDifficulty.setText("Difficulte: -");
            lblExerciseDetailPrescription.setText("Series / repetitions / duree: -");
            lblExerciseDetailDates.setText("Creation: - | Mise a jour: -");
            lblExerciseDetailRole.setText("Acces reserve aux roles ROLE_ADMIN et ROLE_COACH");
            taExerciseDetailDescription.setText("Selectionnez un exercice dans la liste pour afficher ses details.");
            tfExerciseDetailVideoUrl.clear();
            tfExerciseDetailImageUrl.clear();
            imgExercisePreview.setImage(null);
            return;
        }

        lblExerciseDetailName.setText(exercise.getName());
        lblExerciseDetailMuscleGroup.setText("Groupe musculaire: " + exercise.getMuscleGroup());
        lblExerciseDetailDifficulty.setText("Difficulte: " + exercise.getDifficulty());
        lblExerciseDetailPrescription.setText("Series: " + exercise.getSets()
                + " | Repetitions: " + exercise.getRepetitions()
                + " | Duree: " + exercise.getDuration() + " min");
        lblExerciseDetailDates.setText("Creation: " + formatDateTime(exercise.getCreatedAt())
                + " | Mise a jour: " + formatDateTime(exercise.getUpdatedAt()));
        lblExerciseDetailRole.setText("Utilisateur authentifie: " + appSession.getEmail()
                + " (" + appSession.getPrimaryRole() + ")");
        taExerciseDetailDescription.setText(safeText(exercise.getDescription()));
        tfExerciseDetailVideoUrl.setText(safeText(exercise.getVideoUrl()));
        tfExerciseDetailImageUrl.setText(safeText(exercise.getImageUrl()));
        chargerImageExercise(exercise.getImageUrl());
    }

    private void chargerImageExercise(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            imgExercisePreview.setImage(null);
            return;
        }

        try {
            Image image = new Image(imageUrl, true);
            if (!image.isError()) {
                imgExercisePreview.setImage(image);
                return;
            }
        } catch (Exception ignored) {
        }

        imgExercisePreview.setImage(null);
    }

    private void selectionnerRegimeDansCombo(ComboBox<RegimeAlimentaire> comboBox, Integer regimeId) {
        if (regimeId == null) {
            comboBox.getSelectionModel().clearSelection();
            return;
        }
        for (RegimeAlimentaire regime : comboBox.getItems()) {
            if (regime.getId() == regimeId) {
                comboBox.setValue(regime);
                return;
            }
        }
        comboBox.getSelectionModel().clearSelection();
    }

    private RegimeAlimentaire construireRegimeDepuisCreation() {
        return new RegimeAlimentaire(
                tfRegimeNom.getText(),
                taRegimeDescription.getText(),
                parsePositiveInteger(tfRegimeObjectifCalorique.getText(), "L'objectif calorique"),
                chkRegimeActif.isSelected()
        );
    }

    private RegimeAlimentaire construireRegimeDepuisModification() {
        return new RegimeAlimentaire(
                tfRegimeNomEdit.getText(),
                taRegimeDescriptionEdit.getText(),
                parsePositiveInteger(tfRegimeObjectifCaloriqueEdit.getText(), "L'objectif calorique"),
                chkRegimeActifEdit.isSelected()
        );
    }

    private Repas construireRepasDepuisCreation() {
        RegimeAlimentaire selectedRegime = cbRepasRegime.getValue();
        return new Repas(
                tfRepasNom.getText(),
                taRepasDescription.getText(),
                parsePositiveInteger(tfRepasCalories.getText(), "Les calories"),
                cbTypeRepas.getValue(),
                dpDateRepas.getValue(),
                selectedRegime != null ? selectedRegime.getId() : null
        );
    }

    private Repas construireRepasDepuisModification() {
        RegimeAlimentaire selectedRegime = cbRepasRegimeEdit.getValue();
        return new Repas(
                tfRepasNomEdit.getText(),
                taRepasDescriptionEdit.getText(),
                parsePositiveInteger(tfRepasCaloriesEdit.getText(), "Les calories"),
                cbTypeRepasEdit.getValue(),
                dpDateRepasEdit.getValue(),
                selectedRegime != null ? selectedRegime.getId() : null
        );
    }

    private FitnessExercise construireExerciseDepuisCreation() {
        return new FitnessExercise(
                tfExerciseName.getText(),
                taExerciseDescription.getText(),
                cbExerciseMuscleGroup.getValue(),
                cbExerciseDifficulty.getValue(),
                parsePositiveInteger(tfExerciseSets.getText(), "Le nombre de series"),
                parsePositiveInteger(tfExerciseRepetitions.getText(), "Le nombre de repetitions"),
                parsePositiveInteger(tfExerciseDuration.getText(), "La duree"),
                optionalText(tfExerciseVideoUrl.getText()),
                optionalText(tfExerciseImageUrl.getText())
        );
    }

    private FitnessExercise construireExerciseDepuisModification() {
        return new FitnessExercise(
                tfExerciseNameEdit.getText(),
                taExerciseDescriptionEdit.getText(),
                cbExerciseMuscleGroupEdit.getValue(),
                cbExerciseDifficultyEdit.getValue(),
                parsePositiveInteger(tfExerciseSetsEdit.getText(), "Le nombre de series"),
                parsePositiveInteger(tfExerciseRepetitionsEdit.getText(), "Le nombre de repetitions"),
                parsePositiveInteger(tfExerciseDurationEdit.getText(), "La duree"),
                optionalText(tfExerciseVideoUrlEdit.getText()),
                optionalText(tfExerciseImageUrlEdit.getText())
        );
    }

    private int parsePositiveInteger(String value, String label) {
        try {
            int parsedValue = Integer.parseInt(value);
            if (parsedValue <= 0) {
                throw new IllegalArgumentException(label + " doit etre superieur a 0.");
            }
            return parsedValue;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " doit etre un nombre valide.");
        }
    }

    private String optionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : DATE_TIME_FORMATTER.format(value);
    }

    private void afficherPaneRepas(VBox paneActif, Button boutonActif) {
        afficherVue(paneActif, paneRepasCreate, paneRepasEdit, paneRepasDelete, paneRepasExplore);
        activerBoutonAction(boutonActif, btnActionCreerRepas, btnActionModifierRepas, btnActionSupprimerRepas, btnActionExplorerRepas);
    }

    private void afficherPaneRegime(VBox paneActif, Button boutonActif) {
        afficherVue(paneActif, paneRegimeCreate, paneRegimeEdit, paneRegimeDelete, paneRegimeExplore);
        activerBoutonAction(boutonActif, btnActionCreerRegime, btnActionModifierRegime, btnActionSupprimerRegime, btnActionExplorerRegime);
    }

    private void afficherPaneExercise(VBox paneActif, Button boutonActif) {
        verifierAuthentification();
        afficherVue(paneActif, paneExerciseCreate, paneExerciseEdit, paneExerciseDelete, paneExerciseExplore);
        activerBoutonAction(boutonActif, btnActionCreerExercise, btnActionModifierExercise, btnActionSupprimerExercise, btnActionExplorerExercise);
    }

    private void afficherVue(VBox vueActive, VBox... vues) {
        for (VBox vue : vues) {
            boolean active = vue == vueActive;
            vue.setVisible(active);
            vue.setManaged(active);
        }
    }

    private void activerBoutonModule(Button boutonActif, Button... autres) {
        boutonActif.getStyleClass().setAll("sidebar-nav-button", "sidebar-nav-button-active");
        for (Button button : autres) {
            button.getStyleClass().setAll("sidebar-nav-button");
        }
    }

    private void activerBoutonAction(Button boutonActif, Button... boutons) {
        for (Button button : boutons) {
            if (button == boutonActif) {
                button.getStyleClass().setAll("action-card", "action-card-active");
            } else {
                button.getStyleClass().setAll("action-card");
            }
        }
    }

    private void verifierAuthentification() {
        if (!appSession.isAuthenticated()) {
            throw new IllegalStateException("L'utilisateur doit etre authentifie.");
        }
    }

    private void verifierPermissionGestionExercises() {
        verifierAuthentification();
        if (!appSession.hasAnyRole("ROLE_ADMIN", "ROLE_COACH")) {
            throw new IllegalStateException("Acces refuse. Les roles ROLE_ADMIN ou ROLE_COACH sont requis.");
        }
    }

    private void ouvrirLienDepuisChamp(String url, String emptyMessage) {
        try {
            verifierAuthentification();
            if (url == null || url.isBlank()) {
                throw new IllegalArgumentException(emptyMessage);
            }

            if (!Desktop.isDesktopSupported()) {
                throw new IllegalStateException("Le bureau local ne permet pas l'ouverture de liens.");
            }

            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Lien media", exception.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
