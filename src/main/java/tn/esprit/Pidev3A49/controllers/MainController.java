package tn.esprit.Pidev3A49.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import tn.esprit.Pidev3A49.Models.Repas;
import tn.esprit.Pidev3A49.Models.RegimeAlimentaire;
import tn.esprit.Pidev3A49.Models.User;
import tn.esprit.Pidev3A49.services.ServiceRepas;
import tn.esprit.Pidev3A49.services.ServiceRegimeAlimentaire;
import tn.esprit.Pidev3A49.services.ServiceUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MainController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<String> TYPE_REPAS_OPTIONS = List.of(
            "petit_de", "breakfast", "dejeuner", "diner", "evening", "collat", "extra_meal"
    );
    private static final List<String> TYPE_SANTE_OPTIONS = List.of(
            "normal", "surpoids", "obesite", "sous_poids", "diabetique", "cardiaque", "autre"
    );

    @FXML private VBox viewRepas;
    @FXML private VBox viewRegimes;
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
    @FXML private TextField tfRepasHeure;
    @FXML private TextField tfRepasHeureEdit;
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

    @FXML
    public void initialize() {
        initialiserColonnes();
        initialiserCombos();
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

        tableRepas.setItems(FXCollections.observableArrayList(repas));
        tableRepasEdit.setItems(FXCollections.observableArrayList(repas));
        tableRepasDelete.setItems(FXCollections.observableArrayList(repas));

        tableRegimes.setItems(FXCollections.observableArrayList(regimes));
        tableRegimesEdit.setItems(FXCollections.observableArrayList(regimes));
        tableRegimesDelete.setItems(FXCollections.observableArrayList(regimes));
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
            serviceRepas.delete(repas);
            rafraichirDonnees();
            viderFormulaireRepasModification();
            lblRepasSelectionDelete.setText("Aucun repas selectionne");
            showInfo("Repas supprime avec succes.");
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
        tfRepasHeure.clear();
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
        tfRepasHeureEdit.clear();
        cbTypeRepasEdit.getSelectionModel().clearSelection();
        tfRepasNomEdit.clear();
        taRepasDescriptionEdit.clear();
        cbRepasRegimeEdit.getSelectionModel().clearSelection();
        tfRepasCaloriesEdit.clear();
        tfRepasProteinesEdit.clear();
        tfRepasGlucidesEdit.clear();
        tfRepasLipidesEdit.clear();
        tableRepasEdit.getSelectionModel().clearSelection();
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
        type.setCellValueFactory(new PropertyValueFactory<>("typeRepas"));
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

    private void initialiserSelections() {
        tableRepasEdit.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) remplirFormulaireRepas(newValue);
        });
        tableRepasDelete.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                lblRepasSelectionDelete.setText(newValue == null ? "Aucun repas selectionne" : "Repas selectionne: " + newValue.getNomRepas()));
        tableRegimesEdit.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) remplirFormulaireRegime(newValue);
        });
        tableRegimesDelete.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                lblRegimeSelectionDelete.setText(newValue == null ? "Aucun regime selectionne" : "Regime selectionne: #" + newValue.getId()));
    }

    private void remplirFormulaireRepas(Repas repas) {
        tfRepasId.setText(String.valueOf(repas.getId()));
        selectionnerUser(cbRepasUserEdit, repas.getUserId());
        if (repas.getDateRepas() != null) {
            dpDateRepasEdit.setValue(repas.getDateRepas().toLocalDate());
            tfRepasHeureEdit.setText(repas.getDateRepas().toLocalTime().format(TIME_FORMATTER));
        } else {
            dpDateRepasEdit.setValue(null);
            tfRepasHeureEdit.clear();
        }
        cbTypeRepasEdit.setValue(repas.getTypeRepas());
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
        TextField heureField = editMode ? tfRepasHeureEdit : tfRepasHeure;
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
        LocalTime heure = parseTime(heureField.getText());
        RegimeAlimentaire regime = regimeCombo.getValue();

        return new Repas(
                user.getId(),
                LocalDateTime.of(datePicker.getValue(), heure),
                typeCombo.getValue(),
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

    private LocalTime parseTime(String value) {
        try {
            if (value == null || value.isBlank()) return LocalTime.of(9, 0);
            return LocalTime.parse(value.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Format heure invalide. Utilisez HH:mm.");
        }
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
        boutonActif.getStyleClass().setAll("sidebar-nav-button", "sidebar-nav-button-active");
        for (Button button : autres) button.getStyleClass().setAll("sidebar-nav-button");
    }

    private void activerBoutonAction(Button boutonActif, Button... boutons) {
        for (Button button : boutons) {
            if (button == boutonActif) button.getStyleClass().setAll("action-card", "action-card-active");
            else button.getStyleClass().setAll("action-card");
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
