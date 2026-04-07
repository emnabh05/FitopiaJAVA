package tn.esprit.Pidev3A49.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import tn.esprit.Pidev3A49.Models.Repas;
import tn.esprit.Pidev3A49.Models.RegimeAlimentaire;
import tn.esprit.Pidev3A49.Models.TypeRepas;
import tn.esprit.Pidev3A49.services.ServiceRepas;
import tn.esprit.Pidev3A49.services.ServiceRegimeAlimentaire;

public class MainController {

    @FXML
    private TextField tfRegimeId;
    @FXML
    private TextField tfRegimeNom;
    @FXML
    private TextArea taRegimeDescription;
    @FXML
    private TextField tfRegimeObjectifCalorique;
    @FXML
    private CheckBox chkRegimeActif;
    @FXML
    private TableView<RegimeAlimentaire> tableRegimes;
    @FXML
    private TableColumn<RegimeAlimentaire, Integer> colRegimeId;
    @FXML
    private TableColumn<RegimeAlimentaire, String> colRegimeNom;
    @FXML
    private TableColumn<RegimeAlimentaire, String> colRegimeDescription;
    @FXML
    private TableColumn<RegimeAlimentaire, Integer> colRegimeObjectifCalorique;
    @FXML
    private TableColumn<RegimeAlimentaire, Boolean> colRegimeActif;

    @FXML
    private TextField tfRepasId;
    @FXML
    private TextField tfRepasNom;
    @FXML
    private TextArea taRepasDescription;
    @FXML
    private TextField tfRepasCalories;
    @FXML
    private ComboBox<TypeRepas> cbTypeRepas;
    @FXML
    private DatePicker dpDateRepas;
    @FXML
    private ComboBox<RegimeAlimentaire> cbRepasRegime;
    @FXML
    private TableView<Repas> tableRepas;
    @FXML
    private TableColumn<Repas, Integer> colRepasId;
    @FXML
    private TableColumn<Repas, String> colRepasNom;
    @FXML
    private TableColumn<Repas, String> colRepasDescription;
    @FXML
    private TableColumn<Repas, Integer> colRepasCalories;
    @FXML
    private TableColumn<Repas, TypeRepas> colRepasType;
    @FXML
    private TableColumn<Repas, Object> colRepasDate;
    @FXML
    private TableColumn<Repas, Integer> colRepasRegimeId;

    private final ServiceRegimeAlimentaire serviceRegime = new ServiceRegimeAlimentaire();
    private final ServiceRepas serviceRepas = new ServiceRepas();

    @FXML
    public void initialize() {
        initialiserTableRegimes();
        initialiserTableRepas();
        initialiserComboBoxes();
        initialiserSelections();
        chargerRegimes();
        chargerRepas();
    }

    @FXML
    private void ajouterRegime() {
        try {
            RegimeAlimentaire regime = construireRegimeDepuisFormulaire();
            serviceRegime.add(regime);
            chargerRegimes();
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

            RegimeAlimentaire regime = construireRegimeDepuisFormulaire();
            regime.setId(Integer.parseInt(tfRegimeId.getText()));
            serviceRegime.update(regime);
            chargerRegimes();
            chargerRepas();
            rafraichirComboRegimes();
            viderFormulaireRegime();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Regime alimentaire modifie avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur regime", exception.getMessage());
        }
    }

    @FXML
    private void supprimerRegime() {
        try {
            RegimeAlimentaire regime = tableRegimes.getSelectionModel().getSelectedItem();

            if (regime == null) {
                if (tfRegimeId.getText().isBlank()) {
                    throw new IllegalArgumentException("Selectionne un regime alimentaire a supprimer.");
                }

                regime = serviceRegime.getById(Integer.parseInt(tfRegimeId.getText()));
            }

            if (regime == null) {
                throw new IllegalArgumentException("Regime alimentaire introuvable.");
            }

            serviceRegime.delete(regime);
            chargerRegimes();
            chargerRepas();
            rafraichirComboRegimes();
            viderFormulaireRegime();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Regime alimentaire supprime avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur regime", exception.getMessage());
        }
    }

    @FXML
    private void viderFormulaireRegime() {
        tfRegimeId.clear();
        tfRegimeNom.clear();
        taRegimeDescription.clear();
        tfRegimeObjectifCalorique.clear();
        chkRegimeActif.setSelected(true);
        tableRegimes.getSelectionModel().clearSelection();
    }

    @FXML
    private void ajouterRepas() {
        try {
            Repas repas = construireRepasDepuisFormulaire();
            serviceRepas.add(repas);
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

            Repas repas = construireRepasDepuisFormulaire();
            repas.setId(Integer.parseInt(tfRepasId.getText()));
            serviceRepas.update(repas);
            chargerRepas();
            viderFormulaireRepas();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Repas modifie avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur repas", exception.getMessage());
        }
    }

    @FXML
    private void supprimerRepas() {
        try {
            Repas repas = tableRepas.getSelectionModel().getSelectedItem();

            if (repas == null) {
                if (tfRepasId.getText().isBlank()) {
                    throw new IllegalArgumentException("Selectionne un repas a supprimer.");
                }

                repas = serviceRepas.getById(Integer.parseInt(tfRepasId.getText()));
            }

            if (repas == null) {
                throw new IllegalArgumentException("Repas introuvable.");
            }

            serviceRepas.delete(repas);
            chargerRepas();
            viderFormulaireRepas();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Repas supprime avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur repas", exception.getMessage());
        }
    }

    @FXML
    private void viderFormulaireRepas() {
        tfRepasId.clear();
        tfRepasNom.clear();
        taRepasDescription.clear();
        tfRepasCalories.clear();
        cbTypeRepas.getSelectionModel().clearSelection();
        cbRepasRegime.getSelectionModel().clearSelection();
        dpDateRepas.setValue(null);
        tableRepas.getSelectionModel().clearSelection();
    }

    private void initialiserTableRegimes() {
        colRegimeId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRegimeNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colRegimeDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colRegimeObjectifCalorique.setCellValueFactory(new PropertyValueFactory<>("objectifCalorique"));
        colRegimeActif.setCellValueFactory(new PropertyValueFactory<>("actif"));
    }

    private void initialiserTableRepas() {
        colRepasId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRepasNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colRepasDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colRepasCalories.setCellValueFactory(new PropertyValueFactory<>("calories"));
        colRepasType.setCellValueFactory(new PropertyValueFactory<>("typeRepas"));
        colRepasDate.setCellValueFactory(new PropertyValueFactory<>("dateRepas"));
        colRepasRegimeId.setCellValueFactory(new PropertyValueFactory<>("regimeId"));
    }

    private void initialiserComboBoxes() {
        cbTypeRepas.setItems(FXCollections.observableArrayList(TypeRepas.values()));
        cbRepasRegime.setConverter(new StringConverter<>() {
            @Override
            public String toString(RegimeAlimentaire regimeAlimentaire) {
                return regimeAlimentaire == null ? "" : regimeAlimentaire.getId() + " - " + regimeAlimentaire.getNom();
            }

            @Override
            public RegimeAlimentaire fromString(String string) {
                return null;
            }
        });
        rafraichirComboRegimes();
    }

    private void initialiserSelections() {
        tableRegimes.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                remplirFormulaireRegime(newValue);
            }
        });

        tableRepas.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                remplirFormulaireRepas(newValue);
            }
        });
    }

    private void chargerRegimes() {
        tableRegimes.setItems(FXCollections.observableArrayList(serviceRegime.getAll()));
        rafraichirComboRegimes();
    }

    private void chargerRepas() {
        tableRepas.setItems(FXCollections.observableArrayList(serviceRepas.getAll()));
    }

    private void rafraichirComboRegimes() {
        cbRepasRegime.setItems(FXCollections.observableArrayList(serviceRegime.getAll()));
    }

    private void remplirFormulaireRegime(RegimeAlimentaire regime) {
        tfRegimeId.setText(String.valueOf(regime.getId()));
        tfRegimeNom.setText(regime.getNom());
        taRegimeDescription.setText(regime.getDescription());
        tfRegimeObjectifCalorique.setText(String.valueOf(regime.getObjectifCalorique()));
        chkRegimeActif.setSelected(regime.isActif());
    }

    private void remplirFormulaireRepas(Repas repas) {
        tfRepasId.setText(String.valueOf(repas.getId()));
        tfRepasNom.setText(repas.getNom());
        taRepasDescription.setText(repas.getDescription());
        tfRepasCalories.setText(String.valueOf(repas.getCalories()));
        cbTypeRepas.setValue(repas.getTypeRepas());
        dpDateRepas.setValue(repas.getDateRepas());
        selectionnerRegimeDansCombo(repas.getRegimeId());
    }

    private void selectionnerRegimeDansCombo(Integer regimeId) {
        if (regimeId == null) {
            cbRepasRegime.getSelectionModel().clearSelection();
            return;
        }

        for (RegimeAlimentaire regime : cbRepasRegime.getItems()) {
            if (regime.getId() == regimeId) {
                cbRepasRegime.setValue(regime);
                return;
            }
        }

        cbRepasRegime.getSelectionModel().clearSelection();
    }

    private RegimeAlimentaire construireRegimeDepuisFormulaire() {
        return new RegimeAlimentaire(
                tfRegimeNom.getText(),
                taRegimeDescription.getText(),
                Integer.parseInt(tfRegimeObjectifCalorique.getText()),
                chkRegimeActif.isSelected()
        );
    }

    private Repas construireRepasDepuisFormulaire() {
        RegimeAlimentaire selectedRegime = cbRepasRegime.getValue();

        return new Repas(
                tfRepasNom.getText(),
                taRepasDescription.getText(),
                Integer.parseInt(tfRepasCalories.getText()),
                cbTypeRepas.getValue(),
                dpDateRepas.getValue(),
                selectedRegime != null ? selectedRegime.getId() : null
        );
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
