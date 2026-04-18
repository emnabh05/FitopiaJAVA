package tn.esprit.Pidev3A49.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import tn.esprit.Pidev3A49.Models.FitnessExercise;
import tn.esprit.Pidev3A49.services.ServiceFitnessExercise;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class FitopiaHomeController {

    private static final List<String> MUSCLE_GROUPS = List.of(
            "Poitrine", "Dos", "Jambes", "Epaules", "Bras", "Abdominaux", "Fessiers", "Cardio"
    );
    private static final List<String> DIFFICULTY_LEVELS = List.of("Debutant", "Intermediaire", "Avance");
    private static final String LOCATION_GYM = "A la salle de sport";
    private static final String LOCATION_HOME = "A la maison";
    private static final int DEFAULT_WORK_SECONDS = 45;
    private static final int DEFAULT_REST_SECONDS = 20;
    private static final int DEFAULT_ROUNDS = 4;
    private static final double DEFAULT_STANDARD_MODAL_WIDTH = 430.0;
    private static final double DEFAULT_TRAINING_MODAL_WIDTH = 980.0;

    private static final Map<String, Map<String, Map<String, List<ProgramExercise>>>> PROGRAM_LIBRARY = createProgramLibrary();
    private static final List<MusicTrack> MUSIC_PLAYLIST = List.of(
            new MusicTrack("Cardio Pulse", "/media/fitness/audio/cardio.mp3"),
            new MusicTrack("HIIT Energy", "/media/fitness/audio/hiit.mp3"),
            new MusicTrack("Strength Drive", "/media/fitness/audio/strength.mp3")
    );

    private static final DateTimeFormatter PLAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ServiceFitnessExercise serviceFitnessExercise = new ServiceFitnessExercise();
    private final List<ProgramExercise> selectedExercisesTemp = new ArrayList<>();
    private final List<ConfirmedPlan> confirmedPlans = new ArrayList<>();
    private String selectedProgram;
    private String selectedLocation;
    private String selectedDifficulty;
    private ProgramExercise selectedProgramExercise;
    private ConfirmedPlan activePlan;
    private String currentTrainingMeta = "";
    private int currentExerciseIndex = -1;
    private int currentMusicIndex;
    private ProgramModalStep currentProgramModalStep = ProgramModalStep.LOCATION;
    private TrainingPhase currentTrainingPhase = TrainingPhase.WORK;
    private Timeline trainingTimeline;
    private MediaPlayer trainingVideoPlayer;
    private MediaPlayer musicPlayer;
    private boolean musicMuted;
    private boolean coachIaEnabled;
    private int workSeconds = DEFAULT_WORK_SECONDS;
    private int restSeconds = DEFAULT_REST_SECONDS;
    private int totalRounds = DEFAULT_ROUNDS;
    private int currentRound = 1;
    private int remainingSeconds = DEFAULT_WORK_SECONDS;

    @FXML private ScrollPane mainScrollPane;
    @FXML private VBox plannerSection;
    @FXML private VBox plannerPlansContent;
    @FXML private VBox plannerProgramsContent;
    @FXML private VBox plannerCrudContent;
    @FXML private FlowPane plansSelectionContainer;
    @FXML private Label lblPlansEmptyState;
    @FXML private Label lblPlannerContentHint;
    @FXML private Button btnPlannerPlans;
    @FXML private Button btnPlannerPrograms;
    @FXML private Button btnPlannerExercises;
    @FXML private Button btnPlannerExplore;
    @FXML private Button btnPlannerCoach;

    @FXML private VBox programModalCard;
    @FXML private Label lblProgramModalTitle;
    @FXML private Label lblSelectedProgram;
    @FXML private VBox programLocationStep;
    @FXML private VBox programDifficultyStep;
    @FXML private VBox programExerciseStep;
    @FXML private VBox programTrainingStep;
    @FXML private VBox exerciseCardsContainer;
    @FXML private Label lblExerciseEmptyState;
    @FXML private Label lblSelectionStatus;
    @FXML private StackPane programModalOverlay;

    @FXML private Label lblTrainingExerciseName;
    @FXML private Label lblTrainingExerciseMeta;
    @FXML private Label lblTrainingStatus;
    @FXML private Label lblCurrentRound;
    @FXML private Label lblTimeRemaining;
    @FXML private ListView<String> playlistListView;
    @FXML private Slider sliderGlobalVolume;
    @FXML private MediaView trainingMediaView;
    @FXML private Button btnMusicPause;
    @FXML private Button btnMuteMusic;
    @FXML private Button btnCoachIa;
    @FXML private Button btnNextExercise;

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
        cbExerciseMuscleGroup.setItems(FXCollections.observableArrayList(MUSCLE_GROUPS));
        cbExerciseDifficulty.setItems(FXCollections.observableArrayList(DIFFICULTY_LEVELS));
        initialiserContraintesDeSaisie();
        initialiserPlaylist();

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
        resetTrainingState();
        showPlannerPlans();
    }

    @FXML
    private void scrollToPlanner() {
        mainScrollPane.applyCss();
        mainScrollPane.layout();
        mainScrollPane.setVvalue(0.42);
        plannerSection.requestFocus();
    }

    @FXML
    private void showPlannerPlans() {
        activatePlannerTab(btnPlannerPlans);
        plannerPlansContent.setVisible(true);
        plannerPlansContent.setManaged(true);
        plannerProgramsContent.setVisible(false);
        plannerProgramsContent.setManaged(false);
        plannerCrudContent.setVisible(false);
        plannerCrudContent.setManaged(false);
        lblPlannerContentHint.setText("Les seances confirmees depuis Programs sont affichees ici.");
        renderPlannedExercises();
    }

    @FXML
    private void showPlannerPrograms() {
        activatePlannerTab(btnPlannerPrograms);
        plannerPlansContent.setVisible(false);
        plannerPlansContent.setManaged(false);
        plannerProgramsContent.setVisible(true);
        plannerProgramsContent.setManaged(true);
        plannerCrudContent.setVisible(false);
        plannerCrudContent.setManaged(false);
        lblPlannerContentHint.setText("Choisissez un programme pour ouvrir le choix du lieu d'entrainement.");
    }

    @FXML
    private void showPlannerExercises() {
        activatePlannerTab(btnPlannerExercises);
        plannerPlansContent.setVisible(false);
        plannerPlansContent.setManaged(false);
        plannerProgramsContent.setVisible(false);
        plannerProgramsContent.setManaged(false);
        plannerCrudContent.setVisible(true);
        plannerCrudContent.setManaged(true);
        lblPlannerContentHint.setText("Retour");
    }

    @FXML
    private void showPlannerExplore() {
        activatePlannerTab(btnPlannerExplore);
        plannerPlansContent.setVisible(false);
        plannerPlansContent.setManaged(false);
        plannerProgramsContent.setVisible(false);
        plannerProgramsContent.setManaged(false);
        plannerCrudContent.setVisible(false);
        plannerCrudContent.setManaged(false);
        lblPlannerContentHint.setText("Section Explore selectionnee. Cliquez sur Exercises pour afficher le CRUD des exercices.");
    }

    @FXML
    private void showPlannerCoach() {
        activatePlannerTab(btnPlannerCoach);
        plannerPlansContent.setVisible(false);
        plannerPlansContent.setManaged(false);
        plannerProgramsContent.setVisible(false);
        plannerProgramsContent.setManaged(false);
        plannerCrudContent.setVisible(false);
        plannerCrudContent.setManaged(false);
        lblPlannerContentHint.setText("Section Coach selectionnee. Cliquez sur Exercises pour afficher le CRUD des exercices.");
    }

    @FXML
    private void openProgramModal(ActionEvent event) {
        Object source = event.getSource();
        if (!(source instanceof Button clickedButton)) {
            return;
        }

        selectedProgram = clickedButton.getUserData() == null ? clickedButton.getText() : clickedButton.getUserData().toString();
        selectedLocation = null;
        selectedDifficulty = null;
        selectedProgramExercise = null;
        currentExerciseIndex = -1;

        programModalOverlay.setVisible(true);
        programModalOverlay.setManaged(true);
        showProgramLocationStep();
    }

    @FXML
    private void navigateProgramModalBack() {
        switch (currentProgramModalStep) {
            case TRAINING -> {
                cleanupTrainingSession(true);
                showProgramExerciseStep();
            }
            case EXERCISES -> showProgramDifficultyStep();
            case DIFFICULTY -> showProgramLocationStep();
            default -> closeProgramModal();
        }
    }

    @FXML
    private void closeProgramModal() {
        cleanupTrainingSession(true);
        programModalOverlay.setVisible(false);
        programModalOverlay.setManaged(false);
        selectedProgram = null;
        selectedLocation = null;
        selectedDifficulty = null;
        selectedProgramExercise = null;
        activePlan = null;
        currentTrainingMeta = "";
        currentExerciseIndex = -1;
        exerciseCardsContainer.getChildren().clear();
        lblExerciseEmptyState.setVisible(false);
        lblExerciseEmptyState.setManaged(false);
        updateNextExerciseButtonState();
        setProgramModalWidth(false);
    }

    @FXML
    private void selectTrainingLocation(ActionEvent event) {
        Object source = event.getSource();
        if (!(source instanceof Button clickedButton)) {
            return;
        }

        selectedLocation = clickedButton.getUserData() == null ? clickedButton.getText() : clickedButton.getUserData().toString();
        selectedDifficulty = null;
        showProgramDifficultyStep();
    }

    @FXML
    private void selectDifficultyLevel(ActionEvent event) {
        Object source = event.getSource();
        if (!(source instanceof Button clickedButton)) {
            return;
        }

        selectedDifficulty = clickedButton.getUserData() == null ? clickedButton.getText() : clickedButton.getUserData().toString();
        showProgramExerciseStep();
    }

    @FXML
    private void confirmSelectedExercises() {
        if (selectedExercisesTemp.isEmpty()) {
            lblSelectionStatus.setText("Ajoutez au moins un exercice avant de confirmer.");
            return;
        }

        ConfirmedPlan confirmedPlan = createConfirmedPlan();
        confirmedPlans.add(0, confirmedPlan);
        int exerciseCount = confirmedPlan.exercises().size();
        selectedExercisesTemp.clear();
        renderExerciseCards();
        renderPlannedExercises();
        lblPlannerContentHint.setText(confirmedPlans.size() + " seance(s) disponible(s) dans Plans.");
        lblSelectionStatus.setText("Seance creee avec " + exerciseCount + " exercice(s).");
        showPlannerPlans();
    }

    @FXML
    private void startTrainingSession() {
        try {
            syncTrainingParametersFromInputs();
            if (currentTrainingPhase == TrainingPhase.COMPLETE) {
                resetTrainingState();
            }
            ensureTrainingTimeline();
            trainingTimeline.play();
            if (trainingVideoPlayer != null) {
                trainingVideoPlayer.play();
            }
            ensureMusicPlayer();
            if (musicPlayer != null && musicPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                musicPlayer.play();
                btnMusicPause.setText("Music Pause");
            }
            afficherTrainingInfo("Session lancee pour " + safeExerciseName() + ".");
        } catch (Exception exception) {
            afficherTrainingErreur(exception.getMessage());
        }
    }

    @FXML
    private void pauseTrainingSession() {
        if (trainingTimeline != null) {
            trainingTimeline.pause();
        }
        if (trainingVideoPlayer != null) {
            trainingVideoPlayer.pause();
        }
        afficherTrainingInfo("Session en pause.");
    }

    @FXML
    private void resetTrainingSession() {
        try {
            syncTrainingParametersFromInputs();
            resetTrainingState();
            if (trainingTimeline != null) {
                trainingTimeline.stop();
            }
            if (trainingVideoPlayer != null) {
                trainingVideoPlayer.pause();
                trainingVideoPlayer.seek(Duration.ZERO);
            }
            afficherTrainingInfo("Session reinitialisee.");
        } catch (Exception exception) {
            afficherTrainingErreur(exception.getMessage());
        }
    }

    @FXML
    private void toggleMusicPlayback() {
        ensureMusicPlayer();
        if (musicPlayer == null) {
            afficherTrainingErreur("Aucune musique disponible.");
            return;
        }

        if (musicPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            musicPlayer.pause();
            btnMusicPause.setText("Music Play");
            afficherTrainingInfo("Musique en pause.");
        } else {
            musicPlayer.play();
            btnMusicPause.setText("Music Pause");
            afficherTrainingInfo("Musique relancee.");
        }
    }

    @FXML
    private void playNextMusicTrack() {
        if (MUSIC_PLAYLIST.isEmpty()) {
            afficherTrainingErreur("Aucune musique disponible.");
            return;
        }
        int nextIndex = (currentMusicIndex + 1) % MUSIC_PLAYLIST.size();
        playMusicTrack(nextIndex, true);
        afficherTrainingInfo("Lecture de " + MUSIC_PLAYLIST.get(nextIndex).name() + ".");
    }

    @FXML
    private void toggleMusicMute() {
        musicMuted = !musicMuted;
        if (musicPlayer != null) {
            musicPlayer.setMute(musicMuted);
        }
        if (trainingVideoPlayer != null) {
            trainingVideoPlayer.setMute(musicMuted);
        }
        btnMuteMusic.setText(musicMuted ? "Unmute" : "Mute");
        afficherTrainingInfo(musicMuted ? "Audio coupe." : "Audio retabli.");
    }

    @FXML
    private void toggleCoachIa() {
        coachIaEnabled = !coachIaEnabled;
        btnCoachIa.setText(coachIaEnabled ? "Coach IA Off" : "Coach IA On");
        String exerciseName = safeExerciseName();
        afficherTrainingInfo(coachIaEnabled
                ? "Coach IA active. Conseil: gardez le tempo sur " + exerciseName + "."
                : "Coach IA desactive.");
    }

    @FXML
    private void openNextExercise() {
        List<ProgramExercise> exercises = getCurrentTrainingExercises();
        if (exercises.isEmpty()) {
            afficherTrainingErreur("Aucun exercice suivant disponible.");
            return;
        }

        int nextIndex = currentExerciseIndex + 1;
        if (nextIndex >= exercises.size()) {
            completeTrainingSession();
            return;
        }

        openTrainingExperience(exercises.get(nextIndex), nextIndex, currentTrainingMeta);
    }

    @FXML
    private void ajouterExercise() {
        try {
            serviceFitnessExercise.add(construireExercise());
            chargerExercises();
            viderFormulaire();
            afficherSucces("Exercice ajoute dans le front et enregistre dans la base.");
        } catch (Exception exception) {
            afficherErreur(exception.getMessage());
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
            afficherErreur(exception.getMessage());
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
            afficherErreur(exception.getMessage());
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
        lblExerciseStatus.setStyle("-fx-text-fill: #50606f;");
    }

    private void showProgramLocationStep() {
        currentProgramModalStep = ProgramModalStep.LOCATION;
        lblProgramModalTitle.setText("Ou s'entrainer");
        lblSelectedProgram.setText(safeText(selectedProgram));
        setProgramModalWidth(false);
        setProgramStepVisibility(true, false, false, false);
    }

    private void showProgramDifficultyStep() {
        currentProgramModalStep = ProgramModalStep.DIFFICULTY;
        lblProgramModalTitle.setText("Niveau de difficulte");
        lblSelectedProgram.setText(selectedProgram + " - " + selectedLocation);
        setProgramModalWidth(false);
        setProgramStepVisibility(false, true, false, false);
    }

    private void showProgramExerciseStep() {
        currentProgramModalStep = ProgramModalStep.EXERCISES;
        lblProgramModalTitle.setText(selectedProgram);
        lblSelectedProgram.setText(selectedLocation + " - " + selectedDifficulty);
        setProgramModalWidth(false);
        setProgramStepVisibility(false, false, true, false);
        renderExerciseCards();
        updateSelectionStatus();
    }

    private void showProgramTrainingStep() {
        currentProgramModalStep = ProgramModalStep.TRAINING;
        lblProgramModalTitle.setText("Session d'entrainement");
        lblSelectedProgram.setText(currentTrainingMeta);
        setProgramModalWidth(true);
        setProgramStepVisibility(false, false, false, true);
        updateNextExerciseButtonState();
    }

    private void setProgramStepVisibility(boolean locationVisible, boolean difficultyVisible, boolean exerciseVisible, boolean trainingVisible) {
        programLocationStep.setVisible(locationVisible);
        programLocationStep.setManaged(locationVisible);
        programDifficultyStep.setVisible(difficultyVisible);
        programDifficultyStep.setManaged(difficultyVisible);
        programExerciseStep.setVisible(exerciseVisible);
        programExerciseStep.setManaged(exerciseVisible);
        programTrainingStep.setVisible(trainingVisible);
        programTrainingStep.setManaged(trainingVisible);
    }

    private void setProgramModalWidth(boolean trainingMode) {
        double width = trainingMode ? DEFAULT_TRAINING_MODAL_WIDTH : DEFAULT_STANDARD_MODAL_WIDTH;
        programModalCard.setPrefWidth(width);
        programModalCard.setMaxWidth(width);
    }

    private void renderExerciseCards() {
        exerciseCardsContainer.getChildren().clear();
        List<ProgramExercise> exercises = getProgramExercises();
        if (exercises.isEmpty()) {
            lblExerciseEmptyState.setVisible(true);
            lblExerciseEmptyState.setManaged(true);
            return;
        }

        lblExerciseEmptyState.setVisible(false);
        lblExerciseEmptyState.setManaged(false);
        for (ProgramExercise exercise : exercises) {
            exerciseCardsContainer.getChildren().add(createExerciseCard(exercise));
        }
        updateSelectionStatus();
    }

    private HBox createExerciseCard(ProgramExercise exercise) {
        HBox card = new HBox(14);
        card.getStyleClass().add("exercise-item-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setOnMouseClicked(event -> openPreviewTrainingExperience(exercise));

        ImageView imageView = new ImageView();
        imageView.setFitWidth(68);
        imageView.setFitHeight(68);
        imageView.setPreserveRatio(true);
        Image image = loadImageResource(exercise.imagePath());
        if (image != null) {
            imageView.setImage(image);
        }

        VBox textBox = new VBox(6);
        Label title = new Label(exercise.name());
        title.getStyleClass().add("exercise-item-title");
        title.setWrapText(true);
        Label subtitle = new Label("Video, timer et playlist");
        subtitle.getStyleClass().add("exercise-item-subtitle");
        textBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Button actionButton = new Button("Ouvrir");
        actionButton.getStyleClass().add("video-action-button");
        actionButton.setOnAction(event -> openPreviewTrainingExperience(exercise));

        Button addToSessionButton = buildAddToSessionButton(exercise);
        VBox actionBox = new VBox(8, actionButton, addToSessionButton);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(imageView, textBox, actionBox);
        return card;
    }

    private void openPreviewTrainingExperience(ProgramExercise exercise) {
        activePlan = null;
        currentTrainingMeta = selectedProgram + " - " + selectedLocation + " - " + selectedDifficulty;
        openTrainingExperience(exercise, getProgramExercises().indexOf(exercise), currentTrainingMeta);
    }

    private void openTrainingExperience(ProgramExercise exercise, int exerciseIndex, String trainingMeta) {
        selectedProgramExercise = exercise;
        currentExerciseIndex = exerciseIndex;
        currentTrainingMeta = trainingMeta;
        cleanupVideoAndTimer();
        resetTrainingState();
        loadTrainingVideo(exercise.videoPath());
        lblTrainingExerciseName.setText(exercise.name());
        lblTrainingExerciseMeta.setText(trainingMeta);
        showProgramTrainingStep();
        afficherTrainingInfo("Pret pour " + exercise.name() + ".");
    }

    private Button buildAddToSessionButton(ProgramExercise exercise) {
        Button button = new Button();
        button.getStyleClass().add("exercise-cart-button");
        button.setWrapText(true);
        button.setMaxWidth(Double.MAX_VALUE);

        int selectedCount = countSelectedOccurrences(exercise);
        button.setText(selectedCount == 0
                ? "+ Ajouter a ma seance"
                : "+ Ajouter a ma seance (" + selectedCount + ")");
        button.getStyleClass().add("exercise-cart-button-add");
        button.setOnAction(event -> {
            selectedExercisesTemp.add(exercise);
            updateSelectionStatus();
            renderExerciseCards();
        });
        return button;
    }

    private void updateSelectionStatus() {
        if (lblSelectionStatus == null) {
            return;
        }
        if (selectedExercisesTemp.isEmpty()) {
            lblSelectionStatus.setText("Aucun exercice selectionne.");
            return;
        }
        lblSelectionStatus.setText(selectedExercisesTemp.size() + " exercice(s) en attente de confirmation.");
    }

    private void renderPlannedExercises() {
        plansSelectionContainer.getChildren().clear();
        if (confirmedPlans.isEmpty()) {
            lblPlansEmptyState.setVisible(true);
            lblPlansEmptyState.setManaged(true);
            return;
        }

        lblPlansEmptyState.setVisible(false);
        lblPlansEmptyState.setManaged(false);
        for (ConfirmedPlan plan : confirmedPlans) {
            plansSelectionContainer.getChildren().add(createPlanCard(plan));
        }
    }

    private VBox createPlanCard(ConfirmedPlan plan) {
        VBox card = new VBox(16);
        card.getStyleClass().add("plan-session-card");

        Label title = new Label(plan.title());
        title.getStyleClass().add("plan-session-title");
        title.setWrapText(true);

        Label subtitle = new Label(plan.subtitle());
        subtitle.getStyleClass().add("plan-session-subtitle");
        subtitle.setWrapText(true);

        VBox exercisesBox = new VBox(10);
        exercisesBox.getStyleClass().add("plan-session-list");
        for (ProgramExercise exercise : plan.exercises()) {
            HBox exerciseRow = new HBox(8);
            exerciseRow.setAlignment(Pos.TOP_LEFT);

            Label bullet = new Label("•");
            bullet.getStyleClass().add("plan-session-bullet");

            Label exerciseLabel = new Label(exercise.name());
            exerciseLabel.getStyleClass().add("plan-session-item");
            exerciseLabel.setWrapText(true);
            HBox.setHgrow(exerciseLabel, Priority.ALWAYS);

            exerciseRow.getChildren().addAll(bullet, exerciseLabel);
            exercisesBox.getChildren().add(exerciseRow);
        }

        Button startButton = new Button("Commencer");
        startButton.getStyleClass().addAll("plan-card-action-button", "plan-card-start-button");
        startButton.setOnAction(event -> startConfirmedPlan(plan));

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().addAll("plan-card-action-button", "plan-card-delete-button");
        deleteButton.setOnAction(event -> deleteConfirmedPlan(plan));

        VBox actions = new VBox(10, startButton, deleteButton);
        card.getChildren().addAll(title, subtitle, exercisesBox, actions);
        return card;
    }

    private ConfirmedPlan createConfirmedPlan() {
        List<ProgramExercise> exercises = List.copyOf(selectedExercisesTemp);
        int exerciseCount = exercises.size();
        return new ConfirmedPlan(
                UUID.randomUUID().toString(),
                "Ma seance (" + exerciseCount + " exercice" + (exerciseCount > 1 ? "s" : "") + ")",
                selectedLocation == null ? LOCATION_GYM : selectedLocation,
                selectedDifficulty == null ? "Debutant" : selectedDifficulty,
                exercises,
                estimateDurationMinutes(exercises),
                LocalDateTime.now().format(PLAN_DATE_FORMAT)
        );
    }

    private int estimateDurationMinutes(List<ProgramExercise> exercises) {
        return Math.max(2, exercises.size() + 1);
    }

    private int countSelectedOccurrences(ProgramExercise exercise) {
        int count = 0;
        for (ProgramExercise selectedExercise : selectedExercisesTemp) {
            if (selectedExercise.equals(exercise)) {
                count++;
            }
        }
        return count;
    }

    private void startConfirmedPlan(ConfirmedPlan plan) {
        if (plan.exercises().isEmpty()) {
            afficherTrainingErreur("Cette seance ne contient aucun exercice.");
            return;
        }

        activePlan = plan;
        selectedProgram = plan.title();
        selectedLocation = plan.environment();
        selectedDifficulty = plan.level();
        currentTrainingMeta = plan.subtitle();
        currentExerciseIndex = 0;

        programModalOverlay.setVisible(true);
        programModalOverlay.setManaged(true);
        openTrainingExperience(plan.exercises().get(0), 0, currentTrainingMeta);
    }

    private void deleteConfirmedPlan(ConfirmedPlan plan) {
        confirmedPlans.removeIf(existingPlan -> existingPlan.id().equals(plan.id()));
        if (activePlan != null && activePlan.id().equals(plan.id())) {
            activePlan = null;
        }
        renderPlannedExercises();
        lblPlannerContentHint.setText(confirmedPlans.isEmpty()
                ? "Les seances confirmees depuis Programs sont affichees ici."
                : confirmedPlans.size() + " seance(s) disponible(s) dans Plans.");
    }

    private List<ProgramExercise> getCurrentTrainingExercises() {
        if (activePlan != null) {
            return activePlan.exercises();
        }
        return getProgramExercises();
    }

    private void updateNextExerciseButtonState() {
        if (btnNextExercise == null) {
            return;
        }

        List<ProgramExercise> exercises = getCurrentTrainingExercises();
        boolean isTrainingReady = !exercises.isEmpty() && currentExerciseIndex >= 0;
        boolean hasNextExercise = isTrainingReady && currentExerciseIndex < exercises.size() - 1;
        boolean trainingComplete = currentTrainingPhase == TrainingPhase.COMPLETE && !hasNextExercise;

        btnNextExercise.setDisable(!isTrainingReady || trainingComplete);
        btnNextExercise.setText(hasNextExercise ? "Exercice suivant" : "Seance terminee");
    }

    private void completeTrainingSession() {
        currentTrainingPhase = TrainingPhase.COMPLETE;
        if (trainingTimeline != null) {
            trainingTimeline.stop();
        }
        if (trainingVideoPlayer != null) {
            trainingVideoPlayer.pause();
        }
        btnNextExercise.setDisable(true);
        btnNextExercise.setText("Seance terminee");
        afficherTrainingInfo("Seance terminee.");
    }

    private void initialiserPlaylist() {
        playlistListView.setItems(FXCollections.observableArrayList(
                MUSIC_PLAYLIST.stream().map(MusicTrack::name).toList()
        ));
        if (!MUSIC_PLAYLIST.isEmpty()) {
            playlistListView.getSelectionModel().select(0);
            currentMusicIndex = 0;
        }
        playlistListView.setOnMouseClicked(event -> {
            if (event.getClickCount() >= 2) {
                int index = playlistListView.getSelectionModel().getSelectedIndex();
                if (index >= 0) {
                    playMusicTrack(index, true);
                }
            }
        });
        sliderGlobalVolume.valueProperty().addListener((observable, oldValue, newValue) -> updateGlobalVolume(newValue.doubleValue()));
    }

    private void ensureTrainingTimeline() {
        if (trainingTimeline != null) {
            return;
        }
        trainingTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> advanceTrainingTimer()));
        trainingTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void advanceTrainingTimer() {
        if (currentTrainingPhase == TrainingPhase.COMPLETE) {
            if (trainingTimeline != null) {
                trainingTimeline.stop();
            }
            return;
        }

        if (remainingSeconds > 0) {
            remainingSeconds--;
        }

        if (remainingSeconds == 0) {
            if (currentTrainingPhase == TrainingPhase.WORK) {
                if (currentRound >= totalRounds) {
                    currentTrainingPhase = TrainingPhase.COMPLETE;
                    if (trainingTimeline != null) {
                        trainingTimeline.stop();
                    }
                    if (trainingVideoPlayer != null) {
                        trainingVideoPlayer.pause();
                    }
                    if (currentExerciseIndex >= getCurrentTrainingExercises().size() - 1) {
                        afficherTrainingInfo("Dernier exercice termine. Seance terminee.");
                    } else {
                        afficherTrainingInfo("Exercice termine. Passez a l'exercice suivant.");
                    }
                } else {
                    currentTrainingPhase = TrainingPhase.REST;
                    remainingSeconds = restSeconds;
                    afficherTrainingInfo("Phase de repos.");
                }
            } else if (currentTrainingPhase == TrainingPhase.REST) {
                currentRound++;
                currentTrainingPhase = TrainingPhase.WORK;
                remainingSeconds = workSeconds;
                afficherTrainingInfo("Round " + currentRound + " en cours.");
            }
        }

        updateTrainingStats();
        updateNextExerciseButtonState();
    }

    private void syncTrainingParametersFromInputs() {
        workSeconds = DEFAULT_WORK_SECONDS;
        restSeconds = DEFAULT_REST_SECONDS;
        totalRounds = DEFAULT_ROUNDS;
        if (currentRound > totalRounds) {
            currentRound = totalRounds;
        }
        if (currentTrainingPhase == TrainingPhase.WORK || currentTrainingPhase == TrainingPhase.COMPLETE) {
            remainingSeconds = Math.min(remainingSeconds, workSeconds);
        } else {
            remainingSeconds = Math.min(remainingSeconds, restSeconds);
        }
        if (remainingSeconds <= 0 || currentTrainingPhase == TrainingPhase.COMPLETE) {
            remainingSeconds = workSeconds;
            currentTrainingPhase = TrainingPhase.WORK;
            currentRound = 1;
        }
        updateTrainingStats();
        updateNextExerciseButtonState();
    }

    private void resetTrainingState() {
        workSeconds = DEFAULT_WORK_SECONDS;
        restSeconds = DEFAULT_REST_SECONDS;
        totalRounds = DEFAULT_ROUNDS;
        currentRound = 1;
        currentTrainingPhase = TrainingPhase.WORK;
        remainingSeconds = workSeconds;
        coachIaEnabled = false;
        if (btnCoachIa != null) {
            btnCoachIa.setText("Coach IA On");
        }
        updateTrainingStats();
        updateNextExerciseButtonState();
    }

    private void updateTrainingStats() {
        lblCurrentRound.setText(currentRound + " / " + totalRounds);
        lblTimeRemaining.setText(formatSeconds(remainingSeconds));
    }

    private String formatSeconds(int totalSeconds) {
        int minutes = Math.max(totalSeconds, 0) / 60;
        int seconds = Math.max(totalSeconds, 0) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void ensureMusicPlayer() {
        if (musicPlayer == null && !MUSIC_PLAYLIST.isEmpty()) {
            playMusicTrack(currentMusicIndex, false);
        }
    }

    private void playMusicTrack(int index, boolean autoPlay) {
        if (index < 0 || index >= MUSIC_PLAYLIST.size()) {
            return;
        }
        currentMusicIndex = index;
        playlistListView.getSelectionModel().select(index);
        disposeMusicPlayer();
        Media media = loadMedia(MUSIC_PLAYLIST.get(index).resourcePath());
        if (media == null) {
            afficherTrainingErreur("Fichier audio introuvable.");
            return;
        }
        musicPlayer = new MediaPlayer(media);
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayer.setMute(musicMuted);
        musicPlayer.setVolume(sliderGlobalVolume.getValue());
        if (autoPlay) {
            musicPlayer.play();
            btnMusicPause.setText("Music Pause");
        }
    }

    private void updateGlobalVolume(double volume) {
        if (musicPlayer != null) {
            musicPlayer.setVolume(volume);
        }
        if (trainingVideoPlayer != null) {
            trainingVideoPlayer.setVolume(volume);
        }
    }

    private void loadTrainingVideo(String resourcePath) {
        disposeVideoPlayer();
        Media media = loadMedia(resourcePath);
        if (media == null) {
            trainingMediaView.setMediaPlayer(null);
            afficherTrainingErreur("Video introuvable pour cet exercice.");
            return;
        }
        trainingVideoPlayer = new MediaPlayer(media);
        trainingVideoPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        trainingVideoPlayer.setMute(musicMuted);
        trainingVideoPlayer.setVolume(sliderGlobalVolume.getValue());
        trainingMediaView.setMediaPlayer(trainingVideoPlayer);
    }

    private Media loadMedia(String resourcePath) {
        try {
            URL resourceUrl = getClass().getResource(resourcePath);
            return resourceUrl == null ? null : new Media(resourceUrl.toExternalForm());
        } catch (Exception exception) {
            return null;
        }
    }

    private void cleanupVideoAndTimer() {
        if (trainingTimeline != null) {
            trainingTimeline.stop();
        }
        disposeVideoPlayer();
        trainingMediaView.setMediaPlayer(null);
    }

    private void cleanupTrainingSession(boolean includeMusic) {
        cleanupVideoAndTimer();
        if (includeMusic) {
            disposeMusicPlayer();
            btnMusicPause.setText("Music Pause");
        }
    }

    private void disposeVideoPlayer() {
        if (trainingVideoPlayer != null) {
            trainingVideoPlayer.stop();
            trainingVideoPlayer.dispose();
            trainingVideoPlayer = null;
        }
    }

    private void disposeMusicPlayer() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.dispose();
            musicPlayer = null;
        }
    }

    private void afficherTrainingInfo(String message) {
        lblTrainingStatus.setText(message);
        lblTrainingStatus.setStyle("-fx-text-fill: #3d4e5c;");
    }

    private void afficherTrainingErreur(String message) {
        lblTrainingStatus.setText(message);
        lblTrainingStatus.setStyle("-fx-text-fill: #b33f48;");
    }

    private String safeExerciseName() {
        return selectedProgramExercise == null ? "cet exercice" : selectedProgramExercise.name();
    }

    private List<ProgramExercise> getProgramExercises() {
        if (selectedProgram == null || selectedLocation == null || selectedDifficulty == null) {
            return List.of();
        }
        return PROGRAM_LIBRARY.getOrDefault(selectedProgram, Map.of())
                .getOrDefault(selectedLocation, Map.of())
                .getOrDefault(selectedDifficulty, List.of());
    }

    private Image loadImageResource(String resourcePath) {
        try {
            URL resourceUrl = getClass().getResource(resourcePath);
            return resourceUrl == null ? null : new Image(resourceUrl.toExternalForm(), true);
        } catch (Exception exception) {
            return null;
        }
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
        lblExerciseStatus.setStyle("-fx-text-fill: #50606f;");
    }

    private FitnessExercise construireExercise() {
        return new FitnessExercise(
                requiredText(tfExerciseName.getText(), "Le nom de l'exercice", 3, 120),
                requiredText(taExerciseDescription.getText(), "La description de l'exercice", 10, 800),
                requireSelection(cbExerciseMuscleGroup.getValue(), "Le groupe musculaire"),
                requireSelection(cbExerciseDifficulty.getValue(), "Le niveau de difficulte"),
                parsePositiveInteger(tfExerciseSets.getText(), "Les series", 1, 20),
                parsePositiveInteger(tfExerciseRepetitions.getText(), "Les repetitions", 1, 200),
                parsePositiveInteger(tfExerciseDuration.getText(), "La duree", 1, 300),
                optionalUrl(tfExerciseVideoUrl.getText(), "L'URL video"),
                optionalUrl(tfExerciseImageUrl.getText(), "L'URL image")
        );
    }

    private void chargerExercises() {
        tableExercises.setItems(FXCollections.observableArrayList(serviceFitnessExercise.getAll()));
    }

    private void initialiserContraintesDeSaisie() {
        UnaryOperator<TextFormatter.Change> integerFilter = change ->
                change.getControlNewText().matches("\\d*") ? change : null;
        tfExerciseSets.setTextFormatter(new TextFormatter<>(integerFilter));
        tfExerciseRepetitions.setTextFormatter(new TextFormatter<>(integerFilter));
        tfExerciseDuration.setTextFormatter(new TextFormatter<>(integerFilter));
    }

    private int parsePositiveInteger(String value, String label, int min, int max) {
        String trimmedValue = trimToNull(value);
        if (trimmedValue == null) {
            throw new IllegalArgumentException(label + " sont obligatoires.");
        }
        try {
            int parsedValue = Integer.parseInt(trimmedValue);
            if (parsedValue < min || parsedValue > max) {
                throw new IllegalArgumentException(label + " doivent etre comprises entre " + min + " et " + max + ".");
            }
            return parsedValue;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " doivent etre des nombres valides.");
        }
    }

    private String requiredText(String value, String label, int minLength, int maxLength) {
        String trimmedValue = trimToNull(value);
        if (trimmedValue == null) {
            throw new IllegalArgumentException(label + " est obligatoire.");
        }
        if (trimmedValue.length() < minLength) {
            throw new IllegalArgumentException(label + " doit contenir au moins " + minLength + " caracteres.");
        }
        if (trimmedValue.length() > maxLength) {
            throw new IllegalArgumentException(label + " ne doit pas depasser " + maxLength + " caracteres.");
        }
        return trimmedValue;
    }

    private String requireSelection(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " est obligatoire.");
        }
        return value;
    }

    private String optionalUrl(String value, String label) {
        String trimmedValue = trimToNull(value);
        if (trimmedValue == null) {
            return null;
        }
        if (trimmedValue.length() > 500) {
            throw new IllegalArgumentException(label + " ne doit pas depasser 500 caracteres.");
        }
        try {
            URI uri = new URI(trimmedValue);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException(label + " doit commencer par http:// ou https://.");
            }
        } catch (Exception exception) {
            throw new IllegalArgumentException(label + " est invalide.");
        }
        return trimmedValue;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private void afficherSucces(String message) {
        lblExerciseStatus.setText(message);
        lblExerciseStatus.setStyle("-fx-text-fill: #1a7b5b;");
    }

    private void afficherErreur(String message) {
        lblExerciseStatus.setText(message);
        lblExerciseStatus.setStyle("-fx-text-fill: #b33f48;");
    }

    private void activatePlannerTab(Button activeButton) {
        closeProgramModal();
        List<Button> plannerButtons = List.of(
                btnPlannerPlans,
                btnPlannerPrograms,
                btnPlannerExercises,
                btnPlannerExplore,
                btnPlannerCoach
        );
        for (Button button : plannerButtons) {
            if (button == activeButton) {
                button.getStyleClass().setAll("planner-tab-button", "planner-tab-button-active");
            } else {
                button.getStyleClass().setAll("planner-tab-button");
            }
        }
    }

    private static Map<String, Map<String, Map<String, List<ProgramExercise>>>> createProgramLibrary() {
        Map<String, Map<String, Map<String, List<ProgramExercise>>>> library = new LinkedHashMap<>();

        Map<String, Map<String, List<ProgramExercise>>> poitrine = new LinkedHashMap<>();
        Map<String, List<ProgramExercise>> poitrineGym = new LinkedHashMap<>();
        poitrineGym.put("Debutant", List.of(
                new ProgramExercise("Developpe couche avec barre", "/media/fitness/exercises/developpe-incline-machine.gif", "/media/fitness/videos/developpe-couche-avec-barre.mp4"),
                new ProgramExercise("Developpe couche allonge elastique", "/media/fitness/exercises/developpe-assis-prise-marteau.gif", "/media/fitness/videos/developpe-couche-allonge-elastique.mp4"),
                new ProgramExercise("Dips pectoraux", "/media/fitness/exercises/dips-pectoraux.png", "/media/fitness/videos/dips-pectoraux.mp4")
        ));
        poitrineGym.put("Avance", List.of(
                new ProgramExercise("Developpe couche avec barre", "/media/fitness/exercises/arm-chest-flyes-poulie.jpg", "/media/fitness/videos/developpe-couche-avec-barre.mp4"),
                new ProgramExercise("Developpe incline machine", "/media/fitness/exercises/developpe-incline-machine.gif", "/media/fitness/videos/developpe-incline-machine.mp4"),
                new ProgramExercise("Arm chest flyes a la poulie", "/media/fitness/exercises/arm-chest-flyes-poulie.jpg", "/media/fitness/videos/arm-chest-flyes-poulie.mp4")
        ));
        poitrineGym.put("Expert", List.of(
                new ProgramExercise("Developpe couche poulie vis-a-vis", "/media/fitness/exercises/arm-chest-flyes-poulie.jpg", "/media/fitness/videos/developpe-couche-poulie-vis-a-vis.mp4"),
                new ProgramExercise("Developpe incline machine", "/media/fitness/exercises/developpe-incline-machine.gif", "/media/fitness/videos/developpe-incline-machine.mp4"),
                new ProgramExercise("Dips pectoraux", "/media/fitness/exercises/dips-pectoraux.png", "/media/fitness/videos/dips-pectoraux.mp4")
        ));
        poitrine.put(LOCATION_GYM, poitrineGym);
        poitrine.put(LOCATION_HOME, Map.of());
        library.put("Poitrine", poitrine);

        return library;
    }

    private enum ProgramModalStep {
        LOCATION,
        DIFFICULTY,
        EXERCISES,
        TRAINING
    }

    private enum TrainingPhase {
        WORK("Work"),
        REST("Rest"),
        COMPLETE("Complete");

        private final String label;

        TrainingPhase(String label) {
            this.label = label;
        }
    }

    private record ProgramExercise(String name, String imagePath, String videoPath) {
    }

    private record ConfirmedPlan(
            String id,
            String title,
            String environment,
            String level,
            List<ProgramExercise> exercises,
            int estimatedDuration,
            String createdAt
    ) {
        private String subtitle() {
            return "Programme libre - " + environment + " - " + estimatedDuration + " min";
        }
    }

    private record MusicTrack(String name, String resourcePath) {
    }
}
