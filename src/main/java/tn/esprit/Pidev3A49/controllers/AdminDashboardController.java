package tn.esprit.Pidev3A49.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import tn.esprit.Pidev3A49.Models.Supplement;
import tn.esprit.Pidev3A49.services.ServiceSupplement;
import tn.esprit.Pidev3A49.utils.SceneNavigator;
import tn.esprit.Pidev3A49.utils.SupplementImageStorage;

import java.io.File;
import java.math.BigDecimal;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AdminDashboardController {

    private static final List<String> SUPPLEMENT_CATEGORIES = List.of(
            "Whey Protein",
            "Isolate Protein",
            "Mass Gainer",
            "Creatine",
            "Pre-Workout",
            "BCAA / EAA",
            "Post-Workout Recovery",
            "Fat Burner",
            "Vitamins & Minerals",
            "Hydration / Electrolytes"
    );

    private static final List<String> SUPPLEMENT_BRANDS = List.of(
            "Impact Nutrition",
            "HN Labs",
            "More Life Nutrition",
            "Optimum Nutrition",
            "MyProtein",
            "BioTechUSA",
            "Scitec Nutrition",
            "Muscletech",
            "Applied Nutrition",
            "USN"
    );

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private ComboBox<String> brandComboBox;

    @FXML
    private TextField priceField;

    @FXML
    private TextField stockField;

    @FXML
    private TextField caloriesField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Label imageNameLabel;

    @FXML
    private Label formStatusLabel;

    @FXML
    private Label supplementCountLabel;

    @FXML
    private FlowPane adminSupplementGrid;

    @FXML
    private ScrollPane adminScrollPane;

    @FXML
    private VBox adminContentRoot;

    @FXML
    private VBox supplementFormSection;

    @FXML
    private VBox syncedSupplementsSection;

    private ServiceSupplement serviceSupplement;
    private File selectedImageFile;
    private Supplement selectedSupplement;

    @FXML
    private void initialize() {
        categoryComboBox.getItems().setAll(SUPPLEMENT_CATEGORIES);
        brandComboBox.getItems().setAll(SUPPLEMENT_BRANDS);
        resetAddForm();
        initializeService();
    }

    @FXML
    public void openFrontEnd(ActionEvent event) {
        try {
            SceneNavigator.navigate(event, SceneNavigator.BACK_END_VIEW, SceneNavigator.FRONT_END_VIEW);
        } catch (IOException exception) {
            setErrorMessage("Impossible d'ouvrir le front end: " + exception.getMessage());
        }
    }

    @FXML
    public void openSupplementBackEnd(ActionEvent event) {
        try {
            SceneNavigator.navigate(event, SceneNavigator.BACK_END_VIEW, SceneNavigator.BACK_END_VIEW);
        } catch (IOException exception) {
            setErrorMessage("Impossible d'ouvrir l'administration supplements: " + exception.getMessage());
        }
    }

    @FXML
    public void openOrderdPage(ActionEvent event) {
        try {
            SceneNavigator.navigate(event, SceneNavigator.BACK_END_VIEW, SceneNavigator.ORDERD_VIEW);
        } catch (IOException exception) {
            setErrorMessage("Impossible d'ouvrir la liste des commandes: " + exception.getMessage());
        }
    }

    @FXML
    public void openFitnessBackEnd(ActionEvent event) {
        try {
            SceneNavigator.navigate(event, SceneNavigator.BACK_END_VIEW, SceneNavigator.FITNESS_BACK_VIEW);
        } catch (IOException exception) {
            setErrorMessage("Impossible d'ouvrir le module fitness: " + exception.getMessage());
        }
    }

    @FXML
    public void goBackOrExit(ActionEvent event) {
        try {
            SceneNavigator.goBackOrClose(event);
        } catch (IOException exception) {
            setErrorMessage("Impossible de revenir a la page precedente.");
        }
    }

    @FXML
    public void openChangeSessions(ActionEvent event) {
        try {
            SceneNavigator.navigate(event, SceneNavigator.BACK_END_VIEW, SceneNavigator.FITNESS_FRONT_VIEW);
        } catch (IOException exception) {
            setErrorMessage("Impossible d'ouvrir frontregime.fxml.");
        }
    }

    @FXML
    private void focusCreateSupplement(MouseEvent event) {
        scrollTo(supplementFormSection);
        setInfoMessage("Remplis le formulaire ci-dessous pour creer un supplement.");
    }

    @FXML
    private void focusUpdateSupplement(MouseEvent event) {
        scrollTo(supplementFormSection);
        setInfoMessage("Selectionne un supplement dans la liste, modifie le formulaire puis clique sur Modifier.");
    }

    @FXML
    private void focusDeleteSupplement(MouseEvent event) {
        scrollTo(syncedSupplementsSection);
        setInfoMessage("Selectionne un supplement dans la liste puis clique sur Supprimer.");
    }

    @FXML
    private void openFrontEndFromCard(MouseEvent event) {
        try {
            SceneNavigator.navigate((Node) event.getSource(), SceneNavigator.BACK_END_VIEW, SceneNavigator.FRONT_END_VIEW);
        } catch (IOException exception) {
            setErrorMessage("Impossible d'ouvrir le front end: " + exception.getMessage());
        }
    }

    @FXML
    private void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image de supplement");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );

        Window window = imageNameLabel.getScene() == null ? null : imageNameLabel.getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        if (file == null) {
            return;
        }

        try {
            SupplementImageStorage.validateImageFile(file);
            selectedImageFile = file;
            imageNameLabel.setText(file.getName());
            setInfoMessage("Image selectionnee: " + file.getName());
        } catch (RuntimeException exception) {
            setErrorMessage(exception.getMessage());
        }
    }

    @FXML
    private void handleCreateSupplement(ActionEvent event) {
        ensureServiceAvailable();

        String storedImageName = null;
        try {
            if (selectedImageFile != null) {
                storedImageName = SupplementImageStorage.store(selectedImageFile);
            }

            serviceSupplement.add(buildSupplement(storedImageName));
            selectedSupplement = null;
            resetAddForm();
            loadSupplements();
            setSuccessMessage("Supplement ajoute avec succes.");
        } catch (RuntimeException exception) {
            if (storedImageName != null) {
                SupplementImageStorage.deleteImage(storedImageName);
            }
            setErrorMessage(exception.getMessage());
        }
    }

    @FXML
    private void handleUpdateSupplement(ActionEvent event) {
        ensureServiceAvailable();
        if (selectedSupplement == null) {
            setErrorMessage("Selectionne d'abord un supplement a modifier.");
            return;
        }

        String previousImageName = selectedSupplement.getImage();
        String newStoredImageName = null;
        try {
            String imageToPersist = previousImageName;
            if (selectedImageFile != null) {
                newStoredImageName = SupplementImageStorage.store(selectedImageFile);
                imageToPersist = newStoredImageName;
            }

            Supplement updatedSupplement = buildSupplement(imageToPersist);
            updatedSupplement.setId(selectedSupplement.getId());
            serviceSupplement.update(updatedSupplement);

            if (newStoredImageName != null && previousImageName != null && !previousImageName.isBlank()
                    && !previousImageName.equals(newStoredImageName)) {
                SupplementImageStorage.deleteImage(previousImageName);
            }

            selectedSupplement = null;
            resetAddForm();
            loadSupplements();
            setSuccessMessage("Supplement modifie avec succes.");
        } catch (RuntimeException exception) {
            if (newStoredImageName != null) {
                SupplementImageStorage.deleteImage(newStoredImageName);
            }
            setErrorMessage(exception.getMessage());
        }
    }

    @FXML
    private void handleDeleteSupplement(ActionEvent event) {
        ensureServiceAvailable();
        if (selectedSupplement == null) {
            setErrorMessage("Selectionne d'abord un supplement a supprimer.");
            return;
        }

        try {
            String imageName = selectedSupplement.getImage();
            serviceSupplement.delete(selectedSupplement);
            if (imageName != null && !imageName.isBlank()) {
                SupplementImageStorage.deleteImage(imageName);
            }

            selectedSupplement = null;
            resetAddForm();
            loadSupplements();
            setSuccessMessage("Supplement supprime avec succes.");
        } catch (RuntimeException exception) {
            setErrorMessage(exception.getMessage());
        }
    }

    @FXML
    private void resetAddForm() {
        selectedImageFile = null;
        selectedSupplement = null;
        if (nameField != null) {
            nameField.clear();
        }
        if (categoryComboBox != null) {
            categoryComboBox.getSelectionModel().clearSelection();
        }
        if (brandComboBox != null) {
            brandComboBox.getSelectionModel().clearSelection();
        }
        if (priceField != null) {
            priceField.clear();
        }
        if (stockField != null) {
            stockField.clear();
        }
        if (caloriesField != null) {
            caloriesField.clear();
        }
        if (descriptionArea != null) {
            descriptionArea.clear();
        }
        if (imageNameLabel != null) {
            imageNameLabel.setText("Aucun fichier choisi");
        }
        if (formStatusLabel != null) {
            formStatusLabel.setText("");
        }
    }

    private void initializeService() {
        try {
            serviceSupplement = new ServiceSupplement();
            loadSupplements();
            setInfoMessage("Connecte a MySQL. Les supplements crees ici apparaitront dans le front.");
        } catch (RuntimeException exception) {
            serviceSupplement = null;
            supplementCountLabel.setText("Connexion MySQL indisponible");
            adminSupplementGrid.getChildren().setAll(buildEmptyCard(
                    "Base de donnees indisponible",
                    "Demarre MySQL puis recharge l'application pour voir et ajouter des produits."
            ));
            setErrorMessage(exception.getMessage());
        }
    }

    private void loadSupplements() {
        ensureServiceAvailable();

        List<Supplement> supplements = serviceSupplement.getAll();
        supplementCountLabel.setText("Supplements enregistres: " + supplements.size());
        adminSupplementGrid.getChildren().clear();

        if (supplements.isEmpty()) {
            adminSupplementGrid.getChildren().add(buildEmptyCard(
                    "Aucun supplement",
                    "Ajoute un supplement ici puis ouvre le front end pour le voir dans la boutique."
            ));
            return;
        }

        for (Supplement supplement : supplements) {
            adminSupplementGrid.getChildren().add(buildSupplementCard(supplement));
        }
    }

    private Supplement buildSupplement(String imageName) {
        return new Supplement(
                requireText(nameField, "Le nom du supplement est obligatoire."),
                requireComboValue(categoryComboBox, "La categorie est obligatoire."),
                requireComboValue(brandComboBox, "La marque est obligatoire."),
                parsePrice(priceField),
                parseInteger(stockField, "Le stock doit etre un entier valide."),
                parseOptionalInteger(caloriesField, "Les calories doivent etre un entier valide."),
                requireText(descriptionArea, "La description est obligatoire."),
                imageName
        );
    }

    private VBox buildSupplementCard(Supplement supplement) {
        VBox card = new VBox(10.0);
        card.setPrefWidth(245.0);
        boolean isSelected = selectedSupplement != null && selectedSupplement.getId() == supplement.getId();
        card.setStyle(isSelected
                ? "-fx-background-color: #ECFDF3; -fx-background-radius: 18; -fx-border-color: #118265; -fx-border-width: 1.6; -fx-border-radius: 18; -fx-padding: 16 16 16 16;"
                : "-fx-background-color: #FCFFFE; -fx-background-radius: 18; -fx-border-color: #D9E8E1; -fx-border-radius: 18; -fx-padding: 16 16 16 16;");
        card.setOnMouseClicked(event -> selectSupplement(supplement));

        Label brandLabel = new Label(valueOrDefault(supplement.getBrand()).toUpperCase(Locale.ROOT));
        brandLabel.setStyle("-fx-text-fill: #4E877A; -fx-font-size: 11px; -fx-font-weight: 900; -fx-letter-spacing: 1px;");

        Label nameLabel = new Label(valueOrDefault(supplement.getName()));
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-text-fill: #123748; -fx-font-size: 20px; -fx-font-weight: 900;");

        Label descriptionLabel = new Label(truncate(valueOrDefault(supplement.getDescription()), 80));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-text-fill: #7A8C97; -fx-font-size: 13px; -fx-font-weight: 600;");

        HBox metaRow = new HBox(8.0);
        Label categoryChip = new Label(valueOrDefault(supplement.getCategory()));
        categoryChip.setStyle("-fx-background-color: #EEF8F4; -fx-background-radius: 999; -fx-text-fill: #4E877A; " +
                "-fx-font-size: 11px; -fx-font-weight: 800; -fx-padding: 7 10 7 10;");
        Label stockChip = new Label(supplement.getStock() > 0 ? "Stock " + supplement.getStock() : "Rupture");
        stockChip.setStyle(supplement.getStock() > 0
                ? "-fx-background-color: #EEF9F1; -fx-background-radius: 999; -fx-text-fill: #1D7E56; -fx-font-size: 11px; -fx-font-weight: 900; -fx-padding: 7 10 7 10;"
                : "-fx-background-color: #FFF1F2; -fx-background-radius: 999; -fx-text-fill: #D92D20; -fx-font-size: 11px; -fx-font-weight: 900; -fx-padding: 7 10 7 10;");
        metaRow.getChildren().addAll(categoryChip, stockChip);

        Label priceLabel = new Label(supplement.getPrice().toPlainString() + " DT");
        priceLabel.setStyle("-fx-text-fill: #166E5B; -fx-font-size: 24px; -fx-font-weight: 900;");

        card.getChildren().addAll(brandLabel, nameLabel, descriptionLabel, metaRow, priceLabel);
        return card;
    }

    private void selectSupplement(Supplement supplement) {
        selectedSupplement = supplement;
        selectedImageFile = null;

        nameField.setText(supplement.getName() == null ? "" : supplement.getName());
        categoryComboBox.setValue(supplement.getCategory());
        brandComboBox.setValue(supplement.getBrand());
        priceField.setText(supplement.getPrice() == null ? "" : supplement.getPrice().toPlainString());
        stockField.setText(Integer.toString(supplement.getStock()));
        caloriesField.setText(supplement.getCalories() == null ? "" : supplement.getCalories().toString());
        descriptionArea.setText(supplement.getDescription() == null ? "" : supplement.getDescription());
        imageNameLabel.setText((supplement.getImage() == null || supplement.getImage().isBlank())
                ? "Aucun fichier choisi"
                : supplement.getImage());

        scrollTo(supplementFormSection);
        loadSupplements();
        setInfoMessage("Supplement selectionne: " + supplement.getName() + ". Tu peux maintenant modifier ou supprimer.");
    }

    private VBox buildEmptyCard(String title, String message) {
        VBox card = new VBox(10.0);
        card.setPrefWidth(520.0);
        card.setStyle("-fx-background-color: #FCFFFE; -fx-background-radius: 18; -fx-border-color: #D9E8E1; " +
                "-fx-border-radius: 18; -fx-padding: 18 18 18 18;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #123748; -fx-font-size: 20px; -fx-font-weight: 900;");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: #7A8C97; -fx-font-size: 14px; -fx-font-weight: 600;");

        card.getChildren().addAll(titleLabel, messageLabel);
        return card;
    }

    private void ensureServiceAvailable() {
        if (serviceSupplement == null) {
            throw new IllegalStateException("MySQL est indisponible. Demarre le service puis relance l'application.");
        }
    }

    private BigDecimal parsePrice(TextField field) {
        String value = requireText(field, "Le prix est obligatoire.").replace(',', '.');
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Le prix doit etre un nombre decimal valide.");
        }
    }

    private int parseInteger(TextField field, String message) {
        try {
            return Integer.parseInt(requireText(field, message));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(message);
        }
    }

    private Integer parseOptionalInteger(TextField field, String message) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(message);
        }
    }

    private String requireText(TextField field, String message) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private String requireText(TextArea area, String message) {
        String value = area.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private String requireComboValue(ComboBox<String> comboBox, String message) {
        String value = comboBox.getValue();
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
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

    private void setSuccessMessage(String message) {
        formStatusLabel.setText(message);
        formStatusLabel.setStyle("-fx-text-fill: #13866A; -fx-font-size: 14px; -fx-font-weight: 800;");
    }

    private void setInfoMessage(String message) {
        formStatusLabel.setText(message);
        formStatusLabel.setStyle("-fx-text-fill: #255D71; -fx-font-size: 14px; -fx-font-weight: 700;");
    }

    private void setErrorMessage(String message) {
        formStatusLabel.setText(message);
        formStatusLabel.setStyle("-fx-text-fill: #D92D20; -fx-font-size: 14px; -fx-font-weight: 800;");
    }

    private void scrollTo(Node node) {
        if (adminScrollPane == null || adminContentRoot == null || node == null) {
            return;
        }

        Platform.runLater(() -> {
            double scrollableHeight = adminContentRoot.getBoundsInLocal().getHeight() - adminScrollPane.getViewportBounds().getHeight();
            if (scrollableHeight <= 0) {
                adminScrollPane.setVvalue(0.0);
                return;
            }

            double targetY = node.getBoundsInParent().getMinY();
            double targetValue = Math.max(0.0, Math.min(1.0, targetY / scrollableHeight));
            adminScrollPane.setVvalue(targetValue);
        });
    }
}
