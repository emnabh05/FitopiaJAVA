package tn.esprit.Pidev3A49.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import tn.esprit.Pidev3A49.Models.RegimeAlimentaire;
import tn.esprit.Pidev3A49.Models.Supplement;
import tn.esprit.Pidev3A49.services.ServiceRegimeAlimentaire;
import tn.esprit.Pidev3A49.services.ServiceSupplement;
import tn.esprit.Pidev3A49.utils.SupplementImageStorage;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final List<String> SUPPLEMENT_CATEGORIES = List.of(
            "Whey Protein",
            "Isolate Protein",
            "Mass Gainer",
            "Creatine",
            "Pre-Workout",
            "BCAA / EAA",
            "Intra-Workout",
            "Post-Workout Recovery",
            "Fat Burner",
            "Vitamins & Minerals",
            "Omega-3 / Fish Oil",
            "Collagen",
            "Meal Replacement",
            "Protein Bar / Healthy Snack",
            "Hydration / Electrolytes",
            "Wellness Support"
    );
    private static final List<String> SUPPLEMENT_BRANDS = List.of(
            "Impact Nutrition",
            "HN Labs",
            "More Life Nutrition",
            "Optimum Nutrition",
            "MyProtein",
            "BioTechUSA",
            "Scitec Nutrition",
            "Dymatize",
            "Muscletech",
            "Applied Nutrition",
            "Rule 1",
            "BSN",
            "Olimp Sport Nutrition",
            "Kevin Levrone Signature",
            "USN"
    );

    @FXML private VBox viewSupplements;
    @FXML private VBox viewRegimes;
    @FXML private VBox supplementWorkspace;
    @FXML private VBox regimeWorkspace;
    @FXML private VBox paneSupplementCreate;
    @FXML private VBox paneSupplementEdit;
    @FXML private VBox paneSupplementDelete;
    @FXML private VBox paneSupplementExplore;

    @FXML private Button btnModuleSupplements;
    @FXML private Button btnModuleRegimes;
    @FXML private Button btnActionCreerSupplement;
    @FXML private Button btnActionModifierSupplement;
    @FXML private Button btnActionSupprimerSupplement;
    @FXML private Button btnActionExplorerSupplement;

    @FXML private TextField tfSupplementName;
    @FXML private ComboBox<String> cbSupplementCategory;
    @FXML private ComboBox<String> cbSupplementBrand;
    @FXML private TextField tfSupplementPrice;
    @FXML private TextField tfSupplementStock;
    @FXML private TextField tfSupplementCalories;
    @FXML private TextArea taSupplementDescription;
    @FXML private Label lblSelectedImage;
    @FXML private Label lblImagePreviewHint;
    @FXML private ImageView imgSupplementPreview;

    @FXML private TextField tfSupplementId;
    @FXML private TextField tfSupplementNameEdit;
    @FXML private ComboBox<String> cbSupplementCategoryEdit;
    @FXML private ComboBox<String> cbSupplementBrandEdit;
    @FXML private TextField tfSupplementPriceEdit;
    @FXML private TextField tfSupplementStockEdit;
    @FXML private TextField tfSupplementCaloriesEdit;
    @FXML private TextArea taSupplementDescriptionEdit;
    @FXML private Label lblSelectedImageEdit;
    @FXML private Label lblImagePreviewHintEdit;
    @FXML private ImageView imgSupplementPreviewEdit;

    @FXML private Label lblSupplementSelectionDelete;
    @FXML private Label lblDetailName;
    @FXML private Label lblDetailCategory;
    @FXML private Label lblDetailBrand;
    @FXML private Label lblDetailPrice;
    @FXML private Label lblDetailStock;
    @FXML private Label lblDetailCalories;
    @FXML private Label lblDetailImage;
    @FXML private Label lblDetailCreatedAt;
    @FXML private Label lblDetailUpdatedAt;
    @FXML private Label lblDetailOrderUsage;
    @FXML private Label lblDetailImageHint;
    @FXML private TextArea taSupplementDetailDescription;
    @FXML private ImageView imgSupplementDetail;

    @FXML private TableView<Supplement> tableSupplements;
    @FXML private TableView<Supplement> tableSupplementsEdit;
    @FXML private TableView<Supplement> tableSupplementsDelete;
    @FXML private TableColumn<Supplement, Integer> colSupplementId;
    @FXML private TableColumn<Supplement, String> colSupplementName;
    @FXML private TableColumn<Supplement, String> colSupplementCategory;
    @FXML private TableColumn<Supplement, String> colSupplementBrand;
    @FXML private TableColumn<Supplement, BigDecimal> colSupplementPrice;
    @FXML private TableColumn<Supplement, Integer> colSupplementStock;
    @FXML private TableColumn<Supplement, String> colSupplementImage;
    @FXML private TableColumn<Supplement, Integer> colSupplementIdEdit;
    @FXML private TableColumn<Supplement, String> colSupplementNameEdit;
    @FXML private TableColumn<Supplement, String> colSupplementCategoryEdit;
    @FXML private TableColumn<Supplement, String> colSupplementBrandEdit;
    @FXML private TableColumn<Supplement, BigDecimal> colSupplementPriceEdit;
    @FXML private TableColumn<Supplement, Integer> colSupplementStockEdit;
    @FXML private TableColumn<Supplement, String> colSupplementImageEdit;
    @FXML private TableColumn<Supplement, Integer> colSupplementIdDelete;
    @FXML private TableColumn<Supplement, String> colSupplementNameDelete;
    @FXML private TableColumn<Supplement, String> colSupplementCategoryDelete;
    @FXML private TableColumn<Supplement, String> colSupplementBrandDelete;
    @FXML private TableColumn<Supplement, BigDecimal> colSupplementPriceDelete;
    @FXML private TableColumn<Supplement, Integer> colSupplementStockDelete;
    @FXML private TableColumn<Supplement, String> colSupplementImageDelete;

    @FXML private TextField tfRegimeId;
    @FXML private TextField tfRegimeNom;
    @FXML private TextField tfRegimeObjectifCalorique;
    @FXML private TextArea taRegimeDescription;
    @FXML private CheckBox chkRegimeActif;
    @FXML private Label lblRegimeSelectionHint;
    @FXML private TableView<RegimeAlimentaire> tableRegimes;
    @FXML private TableColumn<RegimeAlimentaire, Integer> colRegimeId;
    @FXML private TableColumn<RegimeAlimentaire, String> colRegimeNom;
    @FXML private TableColumn<RegimeAlimentaire, String> colRegimeDescription;
    @FXML private TableColumn<RegimeAlimentaire, Integer> colRegimeObjectifCalorique;
    @FXML private TableColumn<RegimeAlimentaire, Boolean> colRegimeActif;

    private ServiceSupplement serviceSupplement;
    private ServiceRegimeAlimentaire serviceRegime;
    private boolean databaseAvailable;
    private File selectedImageFileCreate;
    private File selectedImageFileEdit;
    private RegimeAlimentaire selectedRegime;

    @FXML
    public void initialize() {
        initialiserTableSupplements();
        initialiserTableRegimes();
        initialiserComboBoxes();
        initialiserSelections();
        afficherModuleSupplements();
        afficherCreationSupplement();
        viderFormulaireSupplement();
        viderFormulaireSupplementModification();
        viderDetailsSupplement();
        viderFormulaireRegime();
        initialiserServices();
    }

    @FXML
    private void afficherModuleSupplements() {
        viewSupplements.setVisible(true);
        viewSupplements.setManaged(true);
        viewRegimes.setVisible(false);
        viewRegimes.setManaged(false);
        activerBoutonModule(btnModuleSupplements, btnModuleRegimes);
    }

    @FXML
    private void afficherModuleRegimes() {
        viewSupplements.setVisible(false);
        viewSupplements.setManaged(false);
        viewRegimes.setVisible(true);
        viewRegimes.setManaged(true);
        activerBoutonModule(btnModuleRegimes, btnModuleSupplements);
    }

    @FXML private void afficherCreationSupplement() { afficherPaneSupplement(paneSupplementCreate, btnActionCreerSupplement); }
    @FXML private void afficherModificationSupplement() { afficherPaneSupplement(paneSupplementEdit, btnActionModifierSupplement); }
    @FXML private void afficherSuppressionSupplement() { afficherPaneSupplement(paneSupplementDelete, btnActionSupprimerSupplement); }
    @FXML private void afficherExplorationSupplement() { afficherPaneSupplement(paneSupplementExplore, btnActionExplorerSupplement); }

    @FXML
    private void rafraichirDonnees() {
        initialiserServices();
    }

    @FXML
    private void choisirImageSupplement() {
        choisirImage(true);
    }

    @FXML
    private void choisirImageSupplementModification() {
        choisirImage(false);
    }

    @FXML
    private void creerSupplement() {
        String storedImageName = null;
        try {
            ensureDatabaseAvailable();
            if (selectedImageFileCreate != null) {
                storedImageName = SupplementImageStorage.store(selectedImageFileCreate);
            }

            serviceSupplement.add(construireSupplementDepuisCreation(storedImageName));
            chargerSupplements();
            viderFormulaireSupplement();
            afficherExplorationSupplement();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Supplement ajoute avec succes.");
        } catch (Exception exception) {
            if (storedImageName != null) {
                SupplementImageStorage.deleteImage(storedImageName);
            }
            showAlert(Alert.AlertType.ERROR, "Erreur supplement", exception.getMessage());
        }
    }

    @FXML
    private void mettreAJourSupplement() {
        String storedImageName = null;
        try {
            ensureDatabaseAvailable();
            Supplement selectedSupplement = tableSupplementsEdit.getSelectionModel().getSelectedItem();
            if (selectedSupplement == null || tfSupplementId.getText().isBlank()) {
                throw new IllegalArgumentException("Selectionne un supplement a modifier.");
            }

            String imageName = selectedSupplement.getImage();
            if (selectedImageFileEdit != null) {
                storedImageName = SupplementImageStorage.store(selectedImageFileEdit);
                imageName = storedImageName;
            }

            Supplement supplement = construireSupplementDepuisModification(imageName);
            supplement.setId(Integer.parseInt(tfSupplementId.getText()));
            serviceSupplement.update(supplement);
            if (storedImageName != null) {
                SupplementImageStorage.deleteImage(selectedSupplement.getImage());
            }

            chargerSupplements();
            viderFormulaireSupplementModification();
            afficherExplorationSupplement();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Supplement modifie avec succes.");
        } catch (Exception exception) {
            if (storedImageName != null) {
                SupplementImageStorage.deleteImage(storedImageName);
            }
            showAlert(Alert.AlertType.ERROR, "Erreur supplement", exception.getMessage());
        }
    }

    @FXML
    private void supprimerSupplement() {
        try {
            ensureDatabaseAvailable();
            Supplement supplement = tableSupplementsDelete.getSelectionModel().getSelectedItem();
            if (supplement == null) {
                throw new IllegalArgumentException("Selectionne un supplement a supprimer.");
            }

            serviceSupplement.delete(supplement);
            SupplementImageStorage.deleteImage(supplement.getImage());
            chargerSupplements();
            viderFormulaireSupplementModification();
            viderDetailsSupplement();
            lblSupplementSelectionDelete.setText("Aucun supplement selectionne");
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Supplement supprime avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur supplement", exception.getMessage());
        }
    }

    @FXML
    private void viderFormulaireSupplement() {
        selectedImageFileCreate = null;
        tfSupplementName.clear();
        cbSupplementCategory.getSelectionModel().clearSelection();
        cbSupplementBrand.getSelectionModel().clearSelection();
        tfSupplementPrice.clear();
        tfSupplementStock.clear();
        tfSupplementCalories.clear();
        taSupplementDescription.clear();
        lblSelectedImage.setText("Aucune image selectionnee");
        masquerImage(imgSupplementPreview, lblImagePreviewHint);
    }

    @FXML
    private void viderFormulaireSupplementModification() {
        selectedImageFileEdit = null;
        tfSupplementId.clear();
        tfSupplementNameEdit.clear();
        cbSupplementCategoryEdit.getSelectionModel().clearSelection();
        cbSupplementBrandEdit.getSelectionModel().clearSelection();
        tfSupplementPriceEdit.clear();
        tfSupplementStockEdit.clear();
        tfSupplementCaloriesEdit.clear();
        taSupplementDescriptionEdit.clear();
        lblSelectedImageEdit.setText("Aucune image selectionnee");
        tableSupplementsEdit.getSelectionModel().clearSelection();
        masquerImage(imgSupplementPreviewEdit, lblImagePreviewHintEdit);
    }

    @FXML
    private void creerRegime() {
        try {
            ensureDatabaseAvailable();
            serviceRegime.add(construireRegimeDepuisFormulaire());
            chargerRegimes();
            viderFormulaireRegime();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Regime ajoute avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur regime", exception.getMessage());
        }
    }

    @FXML
    private void mettreAJourRegime() {
        try {
            ensureDatabaseAvailable();
            if (selectedRegime == null) {
                throw new IllegalArgumentException("Selectionne un regime a modifier dans le tableau.");
            }

            RegimeAlimentaire regime = construireRegimeDepuisFormulaire();
            regime.setId(selectedRegime.getId());
            serviceRegime.update(regime);
            chargerRegimes();
            viderFormulaireRegime();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Regime modifie avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur regime", exception.getMessage());
        }
    }

    @FXML
    private void supprimerRegime() {
        try {
            ensureDatabaseAvailable();
            if (selectedRegime == null) {
                throw new IllegalArgumentException("Selectionne un regime a supprimer dans le tableau.");
            }

            serviceRegime.delete(selectedRegime);
            chargerRegimes();
            viderFormulaireRegime();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Regime supprime avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur regime", exception.getMessage());
        }
    }

    @FXML
    private void viderFormulaireRegime() {
        selectedRegime = null;
        tfRegimeId.clear();
        tfRegimeNom.clear();
        tfRegimeObjectifCalorique.clear();
        taRegimeDescription.clear();
        chkRegimeActif.setSelected(true);
        lblRegimeSelectionHint.setText("Selectionne un regime pour modifier ou supprimer.");
        tableRegimes.getSelectionModel().clearSelection();
    }

    private boolean initialiserServices() {
        try {
            serviceSupplement = new ServiceSupplement();
            serviceRegime = new ServiceRegimeAlimentaire();
            databaseAvailable = true;
            setContentDisabled(false);
            chargerSupplements();
            chargerRegimes();
            return true;
        } catch (RuntimeException exception) {
            serviceSupplement = null;
            serviceRegime = null;
            databaseAvailable = false;
            setContentDisabled(true);
            viderDonnees();
            showAlert(
                    Alert.AlertType.ERROR,
                    "Connexion MySQL impossible",
                    "L'application n'a pas pu se connecter a MySQL. Demarre MySQL puis clique sur \"Actualiser les donnees\"."
            );
            return false;
        }
    }

    private void initialiserTableSupplements() {
        initialiserColonnesSupplements(
                colSupplementId, colSupplementName, colSupplementCategory, colSupplementBrand,
                colSupplementPrice, colSupplementStock, colSupplementImage
        );
        initialiserColonnesSupplements(
                colSupplementIdEdit, colSupplementNameEdit, colSupplementCategoryEdit, colSupplementBrandEdit,
                colSupplementPriceEdit, colSupplementStockEdit, colSupplementImageEdit
        );
        initialiserColonnesSupplements(
                colSupplementIdDelete, colSupplementNameDelete, colSupplementCategoryDelete, colSupplementBrandDelete,
                colSupplementPriceDelete, colSupplementStockDelete, colSupplementImageDelete
        );
    }

    private void initialiserTableRegimes() {
        colRegimeId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRegimeNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colRegimeDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colRegimeObjectifCalorique.setCellValueFactory(new PropertyValueFactory<>("objectifCalorique"));
        colRegimeActif.setCellValueFactory(new PropertyValueFactory<>("actif"));
    }

    private void initialiserColonnesSupplements(
            TableColumn<Supplement, Integer> id,
            TableColumn<Supplement, String> name,
            TableColumn<Supplement, String> category,
            TableColumn<Supplement, String> brand,
            TableColumn<Supplement, BigDecimal> price,
            TableColumn<Supplement, Integer> stock,
            TableColumn<Supplement, String> image
    ) {
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        category.setCellValueFactory(new PropertyValueFactory<>("category"));
        brand.setCellValueFactory(new PropertyValueFactory<>("brand"));
        price.setCellValueFactory(new PropertyValueFactory<>("price"));
        stock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        image.setCellValueFactory(new PropertyValueFactory<>("image"));
    }

    private void initialiserComboBoxes() {
        cbSupplementCategory.setItems(FXCollections.observableArrayList(SUPPLEMENT_CATEGORIES));
        cbSupplementBrand.setItems(FXCollections.observableArrayList(SUPPLEMENT_BRANDS));
        cbSupplementCategoryEdit.setItems(FXCollections.observableArrayList(SUPPLEMENT_CATEGORIES));
        cbSupplementBrandEdit.setItems(FXCollections.observableArrayList(SUPPLEMENT_BRANDS));
    }

    private void initialiserSelections() {
        tableSupplementsEdit.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                remplirFormulaireSupplementModification(newValue);
            }
        });

        tableSupplementsDelete.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                lblSupplementSelectionDelete.setText(
                        newValue == null ? "Aucun supplement selectionne" : "Supplement selectionne: " + newValue.getName()
                )
        );

        tableSupplements.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                viderDetailsSupplement();
                return;
            }
            afficherDetailsSupplement(newValue);
        });

        tableRegimes.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedRegime = newValue;
            if (newValue == null) {
                lblRegimeSelectionHint.setText("Selectionne un regime pour modifier ou supprimer.");
                return;
            }

            tfRegimeId.setText(String.valueOf(newValue.getId()));
            tfRegimeNom.setText(newValue.getNom());
            taRegimeDescription.setText(newValue.getDescription());
            tfRegimeObjectifCalorique.setText(String.valueOf(newValue.getObjectifCalorique()));
            chkRegimeActif.setSelected(newValue.isActif());
            lblRegimeSelectionHint.setText("Regime selectionne: " + newValue.getNom());
        });
    }

    private void chargerSupplements() {
        ensureDatabaseAvailable();
        List<Supplement> supplements = serviceSupplement.getAll();
        tableSupplements.setItems(FXCollections.observableArrayList(supplements));
        tableSupplementsEdit.setItems(FXCollections.observableArrayList(supplements));
        tableSupplementsDelete.setItems(FXCollections.observableArrayList(supplements));
    }

    private void chargerRegimes() {
        ensureDatabaseAvailable();
        List<RegimeAlimentaire> regimes = serviceRegime.getAll();
        tableRegimes.setItems(FXCollections.observableArrayList(regimes));
    }

    private Supplement construireSupplementDepuisCreation(String imageName) {
        return new Supplement(
                tfSupplementName.getText().trim(),
                lireValeurCombo(cbSupplementCategory),
                lireValeurCombo(cbSupplementBrand),
                lirePrix(tfSupplementPrice),
                lireEntierObligatoire(tfSupplementStock, "Le stock doit etre un entier valide."),
                lireEntierOptionnel(tfSupplementCalories, "Les calories doivent etre un entier valide."),
                taSupplementDescription.getText().trim(),
                imageName
        );
    }

    private Supplement construireSupplementDepuisModification(String imageName) {
        return new Supplement(
                tfSupplementNameEdit.getText().trim(),
                lireValeurCombo(cbSupplementCategoryEdit),
                lireValeurCombo(cbSupplementBrandEdit),
                lirePrix(tfSupplementPriceEdit),
                lireEntierObligatoire(tfSupplementStockEdit, "Le stock doit etre un entier valide."),
                lireEntierOptionnel(tfSupplementCaloriesEdit, "Les calories doivent etre un entier valide."),
                taSupplementDescriptionEdit.getText().trim(),
                imageName
        );
    }

    private RegimeAlimentaire construireRegimeDepuisFormulaire() {
        return new RegimeAlimentaire(
                tfRegimeNom.getText().trim(),
                taRegimeDescription.getText().trim(),
                lireEntierObligatoire(tfRegimeObjectifCalorique, "L'objectif calorique doit etre un entier valide."),
                chkRegimeActif.isSelected()
        );
    }

    private void remplirFormulaireSupplementModification(Supplement supplement) {
        tfSupplementId.setText(String.valueOf(supplement.getId()));
        tfSupplementNameEdit.setText(supplement.getName());
        cbSupplementCategoryEdit.setValue(supplement.getCategory());
        cbSupplementBrandEdit.setValue(supplement.getBrand());
        tfSupplementPriceEdit.setText(supplement.getPrice() == null ? "" : supplement.getPrice().toPlainString());
        tfSupplementStockEdit.setText(String.valueOf(supplement.getStock()));
        tfSupplementCaloriesEdit.setText(supplement.getCalories() == null ? "" : String.valueOf(supplement.getCalories()));
        taSupplementDescriptionEdit.setText(supplement.getDescription());
        selectedImageFileEdit = null;

        if (supplement.getImage() == null || supplement.getImage().isBlank()) {
            lblSelectedImageEdit.setText("Aucune image enregistree");
            masquerImage(imgSupplementPreviewEdit, lblImagePreviewHintEdit);
            return;
        }

        lblSelectedImageEdit.setText(supplement.getImage());
        afficherImageStockee(supplement.getImage(), imgSupplementPreviewEdit, lblImagePreviewHintEdit);
    }

    private void afficherDetailsSupplement(Supplement supplement) {
        lblDetailName.setText(valeurTexte(supplement.getName()));
        lblDetailCategory.setText(valeurTexte(supplement.getCategory()));
        lblDetailBrand.setText(valeurTexte(supplement.getBrand()));
        lblDetailPrice.setText(supplement.getPrice() == null ? "-" : supplement.getPrice().toPlainString() + " TND");
        lblDetailStock.setText(String.valueOf(supplement.getStock()));
        lblDetailCalories.setText(supplement.getCalories() == null ? "Optionnel" : String.valueOf(supplement.getCalories()));
        lblDetailImage.setText(valeurTexte(supplement.getImage()));
        lblDetailCreatedAt.setText(formaterDate(supplement.getCreatedAt()));
        lblDetailUpdatedAt.setText(formaterDate(supplement.getUpdatedAt()));
        lblDetailOrderUsage.setText(String.valueOf(serviceSupplement.countLinkedOrderItems(supplement.getId())));
        taSupplementDetailDescription.setText(valeurTexte(supplement.getDescription()));

        if (supplement.getImage() == null || supplement.getImage().isBlank()) {
            masquerImage(imgSupplementDetail, lblDetailImageHint);
        } else {
            afficherImageStockee(supplement.getImage(), imgSupplementDetail, lblDetailImageHint);
        }
    }

    private void viderDetailsSupplement() {
        lblDetailName.setText("-");
        lblDetailCategory.setText("-");
        lblDetailBrand.setText("-");
        lblDetailPrice.setText("-");
        lblDetailStock.setText("-");
        lblDetailCalories.setText("-");
        lblDetailImage.setText("-");
        lblDetailCreatedAt.setText("-");
        lblDetailUpdatedAt.setText("-");
        lblDetailOrderUsage.setText("-");
        taSupplementDetailDescription.clear();
        masquerImage(imgSupplementDetail, lblDetailImageHint);
    }

    private void choisirImage(boolean creation) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image de supplement");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );

        Window window = viewSupplements.getScene() == null ? null : viewSupplements.getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        if (file == null) {
            return;
        }

        try {
            SupplementImageStorage.validateImageFile(file);
            if (creation) {
                selectedImageFileCreate = file;
                lblSelectedImage.setText(file.getName());
                afficherImage(file.toURI().toString(), imgSupplementPreview, lblImagePreviewHint);
            } else {
                selectedImageFileEdit = file;
                lblSelectedImageEdit.setText(file.getName());
                afficherImage(file.toURI().toString(), imgSupplementPreviewEdit, lblImagePreviewHintEdit);
            }
        } catch (RuntimeException exception) {
            showAlert(Alert.AlertType.ERROR, "Image invalide", exception.getMessage());
        }
    }

    private void afficherImageStockee(String imageName, ImageView imageView, Label placeholder) {
        File imageFile = SupplementImageStorage.resolveImagePath(imageName).toFile();
        if (!imageFile.isFile() || !Files.exists(imageFile.toPath())) {
            masquerImage(imageView, placeholder);
            return;
        }

        afficherImage(imageFile.toURI().toString(), imageView, placeholder);
    }

    private void afficherImage(String url, ImageView imageView, Label placeholder) {
        imageView.setImage(new Image(url, true));
        imageView.setVisible(true);
        imageView.setManaged(true);
        placeholder.setVisible(false);
        placeholder.setManaged(false);
    }

    private void masquerImage(ImageView imageView, Label placeholder) {
        imageView.setImage(null);
        imageView.setVisible(false);
        imageView.setManaged(false);
        placeholder.setVisible(true);
        placeholder.setManaged(true);
    }

    private BigDecimal lirePrix(TextField textField) {
        String value = textField.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Le prix est obligatoire.");
        }

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Le prix doit etre un nombre decimal valide.");
        }
    }

    private int lireEntierObligatoire(TextField textField, String message) {
        String value = textField.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(message);
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(message);
        }
    }

    private Integer lireEntierOptionnel(TextField textField, String message) {
        String value = textField.getText().trim();
        if (value.isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(message);
        }
    }

    private String lireValeurCombo(ComboBox<String> comboBox) {
        String value = comboBox.getValue();
        return value == null ? "" : value.trim();
    }

    private String formaterDate(LocalDateTime value) {
        return value == null ? "-" : DATE_TIME_FORMATTER.format(value);
    }

    private String valeurTexte(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void afficherPaneSupplement(VBox paneActif, Button boutonActif) {
        afficherVue(paneActif, paneSupplementCreate, paneSupplementEdit, paneSupplementDelete, paneSupplementExplore);
        activerBoutonAction(
                boutonActif,
                btnActionCreerSupplement,
                btnActionModifierSupplement,
                btnActionSupprimerSupplement,
                btnActionExplorerSupplement
        );
    }

    private void afficherVue(VBox vueActive, VBox... vues) {
        for (VBox vue : vues) {
            boolean active = vue == vueActive;
            vue.setVisible(active);
            vue.setManaged(active);
        }
    }

    private void activerBoutonModule(Button boutonActif, Button autreBouton) {
        boutonActif.getStyleClass().setAll("sidebar-nav-button", "sidebar-nav-button-active");
        autreBouton.getStyleClass().setAll("sidebar-nav-button");
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

    private void ensureDatabaseAvailable() {
        if (!databaseAvailable || serviceSupplement == null || serviceRegime == null) {
            throw new IllegalStateException(
                    "La base de donnees est indisponible. Demarre MySQL puis clique sur \"Actualiser les donnees\"."
            );
        }
    }

    private void setContentDisabled(boolean disabled) {
        supplementWorkspace.setDisable(disabled);
        regimeWorkspace.setDisable(disabled);
    }

    private void viderDonnees() {
        tableSupplements.setItems(FXCollections.observableArrayList());
        tableSupplementsEdit.setItems(FXCollections.observableArrayList());
        tableSupplementsDelete.setItems(FXCollections.observableArrayList());
        tableRegimes.setItems(FXCollections.observableArrayList());
        viderFormulaireSupplement();
        viderFormulaireSupplementModification();
        viderDetailsSupplement();
        viderFormulaireRegime();
        lblSupplementSelectionDelete.setText("Aucun supplement selectionne");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
