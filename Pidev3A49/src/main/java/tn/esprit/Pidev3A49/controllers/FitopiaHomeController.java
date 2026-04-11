package tn.esprit.Pidev3A49.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import tn.esprit.Pidev3A49.Models.FitnessExercise;
import tn.esprit.Pidev3A49.services.ServiceFitnessExercise;
import tn.esprit.Pidev3A49.utils.SceneNavigator;

import java.io.IOException;
import java.util.List;

public class FitopiaHomeController {

    private static final List<String> MUSCLE_GROUPS = List.of(
            "Poitrine", "Dos", "Jambes", "Epaules", "Bras", "Abdominaux", "Fessiers", "Cardio"
    );
    private static final List<String> DIFFICULTY_LEVELS = List.of("Debutant", "Intermediaire", "Avance");

    private ServiceFitnessExercise serviceFitnessExercise;

    @FXML private ScrollPane mainScrollPane;
    @FXML private VBox plannerSection;

    @FXML private TextField tfExerciseId;
    @FXML private TextField tfExerciseName;
    @FXML private ComboBox<String> cbExerciseMuscleGroup;
    @FXML private ComboBox<String> cbExerciseDifficulty;
    @FXML private TextField tfExerciseSets;
    @FXML private TextField tfExerciseRepetitions;
    @FXML private TextField tfExerciseDuration;
    @FXML private TextField tfExerciseImageUrl;
    @FXML private TextField tfExerciseVideoUrl;
    @FXML private TextArea taExerciseDescription;
    @FXML private Label lblExerciseStatus;

    @FXML private TableView<FitnessExercise> tableExercises;
    @FXML private TableColumn<FitnessExercise, Integer> colExerciseId;
    @FXML private TableColumn<FitnessExercise, String> colExerciseName;
    @FXML private TableColumn<FitnessExercise, String> colExerciseMuscleGroup;
    @FXML private TableColumn<FitnessExercise, String> colExerciseDifficulty;
    @FXML private TableColumn<FitnessExercise, Integer> colExerciseSets;
    @FXML private TableColumn<FitnessExercise, Integer> colExerciseRepetitions;
    @FXML private TableColumn<FitnessExercise, Integer> colExerciseDuration;

    @FXML
    public void initialize() {
        serviceFitnessExercise = new ServiceFitnessExercise();
        cbExerciseMuscleGroup.setItems(FXCollections.observableArrayList(MUSCLE_GROUPS));
        cbExerciseDifficulty.setItems(FXCollections.observableArrayList(DIFFICULTY_LEVELS));

        colExerciseId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colExerciseName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colExerciseMuscleGroup.setCellValueFactory(new PropertyValueFactory<>("muscleGroup"));
        colExerciseDifficulty.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        colExerciseSets.setCellValueFactory(new PropertyValueFactory<>("sets"));
        colExerciseRepetitions.setCellValueFactory(new PropertyValueFactory<>("repetitions"));
        colExerciseDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));

        tableExercises.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                remplirFormulaire(newValue);
            }
        });

        chargerExercises();
    }

    @FXML
    private void scrollToPlanner() {
        mainScrollPane.applyCss();
        mainScrollPane.layout();
        mainScrollPane.setVvalue(0.42);
        plannerSection.requestFocus();
    }

    @FXML
    private void openSupplementFront(ActionEvent event) {
        try {
            SceneNavigator.navigate(event, SceneNavigator.FITNESS_FRONT_VIEW, SceneNavigator.FRONT_END_VIEW);
        } catch (IOException exception) {
            showAlert(Alert.AlertType.ERROR, "Navigation", "Impossible d'ouvrir le front supplement.");
        }
    }

    @FXML
    private void openFitnessBack(ActionEvent event) {
        try {
            SceneNavigator.navigate(event, SceneNavigator.FITNESS_FRONT_VIEW, SceneNavigator.FITNESS_BACK_VIEW);
        } catch (IOException exception) {
            showAlert(Alert.AlertType.ERROR, "Navigation", "Impossible d'ouvrir le back fitness.");
        }
    }

    @FXML
    private void openProgressTracker(ActionEvent event) {
        try {
            SceneNavigator.navigate(event, SceneNavigator.FITNESS_FRONT_VIEW, SceneNavigator.PROGRESS_VIEW);
        } catch (IOException exception) {
            showAlert(Alert.AlertType.ERROR, "Navigation", "Impossible d'ouvrir la progression.");
        }
    }

    @FXML
    private void openCart(ActionEvent event) {
        try {
            SceneNavigator.navigate(event, SceneNavigator.FITNESS_FRONT_VIEW, SceneNavigator.FRONT_END_VIEW);
        } catch (IOException exception) {
            showAlert(Alert.AlertType.ERROR, "Navigation", "Impossible d'ouvrir le panier.");
        }
    }

    @FXML
    private void openMonthlyRanking(ActionEvent event) {
        try {
            SceneNavigator.navigate(event, SceneNavigator.FITNESS_FRONT_VIEW, SceneNavigator.RANKING_VIEW);
        } catch (IOException exception) {
            showAlert(Alert.AlertType.ERROR, "Navigation", "Impossible d'ouvrir le classement mensuel.");
        }
    }

    @FXML
    private void openCheckout(ActionEvent event) {
        try {
            SceneNavigator.navigate(event, SceneNavigator.FITNESS_FRONT_VIEW, SceneNavigator.CHECKOUT_VIEW);
        } catch (IOException exception) {
            showAlert(Alert.AlertType.ERROR, "Navigation", "Impossible d'ouvrir le checkout.");
        }
    }

    @FXML
    private void openMyOrders(ActionEvent event) {
        try {
            SceneNavigator.navigate(event, SceneNavigator.FITNESS_FRONT_VIEW, SceneNavigator.FRONT_ORDERS_VIEW);
        } catch (IOException exception) {
            showAlert(Alert.AlertType.ERROR, "Navigation", "Impossible d'ouvrir mes commandes.");
        }
    }

    @FXML
    private void ajouterExercise() {
        try {
            serviceFitnessExercise.add(construireExercise());
            chargerExercises();
            viderFormulaire();
            afficherSucces("Exercice ajoute dans le front et enregistre dans la base.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur exercice", exception.getMessage());
        }
    }

    @FXML
    private void modifierExercise() {
        try {
            if (tfExerciseId.getText().isBlank()) {
                throw new IllegalArgumentException("Selectionnez un exercice dans le tableau.");
            }

            FitnessExercise exercise = construireExercise();
            exercise.setId(Integer.parseInt(tfExerciseId.getText()));
            serviceFitnessExercise.update(exercise);
            chargerExercises();
            afficherSucces("Exercice modifie dans le front et mis a jour dans la base.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur exercice", exception.getMessage());
        }
    }

    @FXML
    private void supprimerExercise() {
        try {
            FitnessExercise selectedExercise = tableExercises.getSelectionModel().getSelectedItem();
            if (selectedExercise == null) {
                throw new IllegalArgumentException("Selectionnez un exercice a supprimer.");
            }

            serviceFitnessExercise.delete(selectedExercise);
            chargerExercises();
            viderFormulaire();
            afficherSucces("Exercice supprime du front et de la base.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur exercice", exception.getMessage());
        }
    }

    @FXML
    private void viderFormulaire() {
        tfExerciseId.clear();
        tfExerciseName.clear();
        cbExerciseMuscleGroup.getSelectionModel().clearSelection();
        cbExerciseDifficulty.getSelectionModel().clearSelection();
        tfExerciseSets.clear();
        tfExerciseRepetitions.clear();
        tfExerciseDuration.clear();
        tfExerciseImageUrl.clear();
        tfExerciseVideoUrl.clear();
        taExerciseDescription.clear();
        tableExercises.getSelectionModel().clearSelection();
        lblExerciseStatus.setText("Selectionnez un exercice pour modifier ou supprimer.");
    }

    private void remplirFormulaire(FitnessExercise exercise) {
        tfExerciseId.setText(String.valueOf(exercise.getId()));
        tfExerciseName.setText(exercise.getName());
        cbExerciseMuscleGroup.setValue(exercise.getMuscleGroup());
        cbExerciseDifficulty.setValue(exercise.getDifficulty());
        tfExerciseSets.setText(String.valueOf(exercise.getSets()));
        tfExerciseRepetitions.setText(String.valueOf(exercise.getRepetitions()));
        tfExerciseDuration.setText(String.valueOf(exercise.getDuration()));
        tfExerciseImageUrl.setText(safeText(exercise.getImageUrl()));
        tfExerciseVideoUrl.setText(safeText(exercise.getVideoUrl()));
        taExerciseDescription.setText(safeText(exercise.getDescription()));
        lblExerciseStatus.setText("Exercice charge: " + exercise.getName());
    }

    private FitnessExercise construireExercise() {
        return new FitnessExercise(
                tfExerciseName.getText(),
                taExerciseDescription.getText(),
                cbExerciseMuscleGroup.getValue(),
                cbExerciseDifficulty.getValue(),
                parsePositiveInteger(tfExerciseSets.getText(), "Les series"),
                parsePositiveInteger(tfExerciseRepetitions.getText(), "Les repetitions"),
                parsePositiveInteger(tfExerciseDuration.getText(), "La duree"),
                optionalText(tfExerciseVideoUrl.getText()),
                optionalText(tfExerciseImageUrl.getText())
        );
    }

    private void chargerExercises() {
        tableExercises.setItems(FXCollections.observableArrayList(serviceFitnessExercise.getAll()));
    }

    private int parsePositiveInteger(String value, String label) {
        try {
            int parsedValue = Integer.parseInt(value);
            if (parsedValue <= 0) {
                throw new IllegalArgumentException(label + " doivent etre superieures a 0.");
            }
            return parsedValue;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " doivent etre des nombres valides.");
        }
    }

    private String optionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private void afficherSucces(String message) {
        lblExerciseStatus.setText(message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
