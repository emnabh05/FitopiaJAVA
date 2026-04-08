package tn.esprit.Pidev3A49.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public class MainController {

    @FXML private VBox viewSupplements;
    @FXML private VBox viewRegimes;
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
    @FXML private TextField tfSupplementCategory;
    @FXML private TextField tfSupplementBrand;
    @FXML private TextField tfSupplementPrice;
    @FXML private TextField tfSupplementStock;
    @FXML private TextField tfSupplementCalories;
    @FXML private TextArea taSupplementDescription;
    @FXML private Label lblSelectedImage;
    @FXML private Label lblImagePreviewHint;
    @FXML private ImageView imgSupplementPreview;

    @FXML private TextField tfSupplementId;
    @FXML private TextField tfSupplementNameEdit;
    @FXML private TextField tfSupplementCategoryEdit;
    @FXML private TextField tfSupplementBrandEdit;
    @FXML private TextField tfSupplementPriceEdit;
    @FXML private TextField tfSupplementStockEdit;
    @FXML private TextField tfSupplementCaloriesEdit;
    @FXML private TextArea taSupplementDescriptionEdit;
    @FXML private Label lblSelectedImageEdit;
    @FXML private Label lblImagePreviewHintEdit;
    @FXML private ImageView imgSupplementPreviewEdit;

    @FXML private TextField tfRegimeId;
    @FXML private TextField tfRegimeNom;
    @FXML private TextField tfRegimeObjectifCalorique;
    @FXML private TextArea taRegimeDescription;
    @FXML private CheckBox chkRegimeActif;
    @FXML private Label lblSupplementSelectionDelete;

    private File selectedImageCreate;
    private File selectedImageEdit;

    @FXML
    public void initialize() {
        afficherModuleSupplements();
        afficherCreationSupplement();
        viderFormulaireSupplement();
        viderFormulaireSupplementModification();
        viderFormulaireRegime();
        if (lblSupplementSelectionDelete != null) {
            lblSupplementSelectionDelete.setText("Aucun supplement selectionne");
        }
    }

    @FXML
    private void afficherModuleSupplements() {
        afficherVue(viewSupplements, viewRegimes);
        activerBoutonModule(btnModuleSupplements, btnModuleRegimes);
    }

    @FXML
    private void afficherModuleRegimes() {
        afficherVue(viewRegimes, viewSupplements);
        activerBoutonModule(btnModuleRegimes, btnModuleSupplements);
    }

    @FXML private void afficherCreationSupplement() { afficherPaneSupplement(paneSupplementCreate, btnActionCreerSupplement); }
    @FXML private void afficherModificationSupplement() { afficherPaneSupplement(paneSupplementEdit, btnActionModifierSupplement); }
    @FXML private void afficherSuppressionSupplement() { afficherPaneSupplement(paneSupplementDelete, btnActionSupprimerSupplement); }
    @FXML private void afficherExplorationSupplement() { afficherPaneSupplement(paneSupplementExplore, btnActionExplorerSupplement); }

    @FXML
    private void rafraichirDonnees() {
        showDesignOnlyAlert();
    }

    @FXML
    private void choisirImageSupplement() {
        selectedImageCreate = choisirImage();
        mettreAJourApercu(selectedImageCreate, lblSelectedImage, imgSupplementPreview, lblImagePreviewHint);
    }

    @FXML
    private void choisirImageSupplementModification() {
        selectedImageEdit = choisirImage();
        mettreAJourApercu(selectedImageEdit, lblSelectedImageEdit, imgSupplementPreviewEdit, lblImagePreviewHintEdit);
    }

    @FXML
    private void creerSupplement() {
        showDesignOnlyAlert();
    }

    @FXML
    private void mettreAJourSupplement() {
        showDesignOnlyAlert();
    }

    @FXML
    private void supprimerSupplement() {
        showDesignOnlyAlert();
    }

    @FXML
    private void creerRegime() {
        showDesignOnlyAlert();
    }

    @FXML
    private void mettreAJourRegime() {
        showDesignOnlyAlert();
    }

    @FXML
    private void supprimerRegime() {
        showDesignOnlyAlert();
    }

    @FXML
    private void viderFormulaireSupplement() {
        selectedImageCreate = null;
        clearText(tfSupplementName, tfSupplementCategory, tfSupplementBrand, tfSupplementPrice, tfSupplementStock, tfSupplementCalories);
        clearArea(taSupplementDescription);
        resetPreview(lblSelectedImage, imgSupplementPreview, lblImagePreviewHint);
    }

    @FXML
    private void viderFormulaireSupplementModification() {
        selectedImageEdit = null;
        clearText(tfSupplementId, tfSupplementNameEdit, tfSupplementCategoryEdit, tfSupplementBrandEdit,
                tfSupplementPriceEdit, tfSupplementStockEdit, tfSupplementCaloriesEdit);
        clearArea(taSupplementDescriptionEdit);
        resetPreview(lblSelectedImageEdit, imgSupplementPreviewEdit, lblImagePreviewHintEdit);
    }

    @FXML
    private void viderFormulaireRegime() {
        clearText(tfRegimeId, tfRegimeNom, tfRegimeObjectifCalorique);
        clearArea(taRegimeDescription);
        if (chkRegimeActif != null) {
            chkRegimeActif.setSelected(true);
        }
    }

    private void afficherPaneSupplement(VBox paneActive, Button boutonActif) {
        afficherVue(paneActive, paneSupplementCreate, paneSupplementEdit, paneSupplementDelete, paneSupplementExplore);
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
            if (vue == null) {
                continue;
            }
            boolean active = vue == vueActive;
            vue.setVisible(active);
            vue.setManaged(active);
        }
    }

    private void activerBoutonModule(Button boutonActif, Button autreBouton) {
        if (boutonActif != null) {
            boutonActif.getStyleClass().setAll("sidebar-nav-button", "sidebar-nav-button-active");
        }
        if (autreBouton != null) {
            autreBouton.getStyleClass().setAll("sidebar-nav-button");
        }
    }

    private void activerBoutonAction(Button boutonActif, Button... boutons) {
        for (Button button : boutons) {
            if (button == null) {
                continue;
            }
            if (button == boutonActif) {
                button.getStyleClass().setAll("action-card", "action-card-active");
            } else {
                button.getStyleClass().setAll("action-card");
            }
        }
    }

    private File choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );

        Window window = viewSupplements == null || viewSupplements.getScene() == null
                ? null
                : viewSupplements.getScene().getWindow();
        return fileChooser.showOpenDialog(window);
    }

    private void mettreAJourApercu(File file, Label label, ImageView imageView, Label placeholder) {
        if (file == null) {
            return;
        }
        if (label != null) {
            label.setText(file.getName());
        }
        if (imageView != null && placeholder != null) {
            imageView.setImage(new Image(file.toURI().toString(), true));
            imageView.setVisible(true);
            imageView.setManaged(true);
            placeholder.setVisible(false);
            placeholder.setManaged(false);
        }
    }

    private void resetPreview(Label label, ImageView imageView, Label placeholder) {
        if (label != null) {
            label.setText("Aucune image selectionnee");
        }
        if (imageView != null) {
            imageView.setImage(null);
            imageView.setVisible(false);
            imageView.setManaged(false);
        }
        if (placeholder != null) {
            placeholder.setVisible(true);
            placeholder.setManaged(true);
        }
    }

    private void clearText(TextField... fields) {
        for (TextField field : fields) {
            if (field != null) {
                field.clear();
            }
        }
    }

    private void clearArea(TextArea... areas) {
        for (TextArea area : areas) {
            if (area != null) {
                area.clear();
            }
        }
    }

    private void showDesignOnlyAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Design Only");
        alert.setHeaderText(null);
        alert.setContentText("Cette copie contient uniquement le design. Aucun CRUD n'est active.");
        alert.showAndWait();
    }
}
