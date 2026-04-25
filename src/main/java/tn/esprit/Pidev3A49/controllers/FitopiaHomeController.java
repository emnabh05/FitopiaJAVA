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
import tn.esprit.Pidev3A49.services.CoachTtsService;
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
    private final CoachTtsService coachTts = new CoachTtsService();
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
    @FXML private VBox plannerCoachContent;
    @FXML private VBox plannerExploreContent;
    @FXML private javafx.scene.media.MediaView exploreMediaView;
    @FXML private javafx.scene.layout.StackPane explorePlayerPane;
    @FXML private VBox explorePlaylistBox;
    @FXML private Label lblExploreVideoTitle;
    @FXML private Label lblExploreDesc;
    @FXML private Label lblExploreCount;
    @FXML private Label lblExploreNowPlaying;
    @FXML private Button btnExplorePlay;
    @FXML private Button btnExploreMute;
    @FXML private Button btnExploreLike;
    @FXML private TextField tfExploreFilter;
    @FXML private Slider sliderExploreVolume;
    @FXML private FlowPane plansSelectionContainer;
    @FXML private FlowPane coachCardsContainer;
    @FXML private FlowPane exerciseCardsGrid;
    // ── Performance tracker fields ──
    @FXML private VBox  perfFormBox;
    @FXML private VBox  perfExercisesBox;
    @FXML private VBox  perfChartBox;
    @FXML private VBox  perfHistoryBox;
    @FXML private TextField tfPerfDate;
    @FXML private TextField tfPerfLabel;
    @FXML private javafx.scene.control.ComboBox<String> cbPerfExercise;
    @FXML private Label lblPerfStatus;
    @FXML private Label lblPerfChartEmpty;

    // In-memory performance data (persists during app session)
    private record PerfSet(double weightKg, int reps) {}
    private record PerfExEntry(String exerciseName, List<PerfSet> sets) {}
    private record PerfSession(String date, String label, List<PerfExEntry> exercises) {}
    private final List<PerfSession> perfHistory = new java.util.ArrayList<>();
    private final List<PerfExEntry> perfCurrentExercises = new java.util.ArrayList<>();
    private String perfChartMetric = "weight"; // "weight" | "volume"
    @FXML private VBox coachBookingForm;
    @FXML private Label lblBookingCoachName;
    @FXML private Label lblBookingCoachSlots;
    @FXML private Label lblBookingStatus;
    @FXML private Label lblSelectedSlot;
    @FXML private TextField tfBookingName;
    @FXML private TextField tfBookingEmail;
    @FXML private TextField tfBookingPhone;
    @FXML private TextArea taBookingMessage;
    @FXML private FlowPane bookingDateButtons;
    @FXML private javafx.scene.control.ComboBox<String> cbBookingSlot;
    private CoachData selectedBookingCoach = null;
    private String selectedBookingDate = null;
    @FXML private Label lblPlansEmptyState;
    @FXML private Label lblPlannerContentHint;
    @FXML private Button btnPlannerPlans;
    @FXML private Button btnPlannerPrograms;
    @FXML private Button btnPlannerExercises;
    @FXML private Button btnPlannerExplore;
    @FXML private Button btnPlannerCoach;

    @FXML private VBox programModalCard;
    // ── Feature 2 : IMC ──
    @FXML private StackPane imcModalOverlay;
    @FXML private VBox      imcResultBox;
    @FXML private TextField tfImcPoids;
    @FXML private TextField tfImcTaille;
    @FXML private TextField tfImcAge;
    @FXML private javafx.scene.control.ComboBox<String> cbImcSexe;
    @FXML private Label lblImcValeur;
    @FXML private Label lblImcCategorie;
    @FXML private Label lblImcConseil;
    @FXML private Label lblImcProgramme;
    @FXML private javafx.scene.control.ProgressBar pbImc;
    private String imcRecommendedProgram = null;
    // ── Feature 3 : Timer repos (supprimé) ──
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
        // Init performance tracker
        renderPerfHistory();
        if (cbPerfExercise != null) {
            cbPerfExercise.setOnAction(e -> {
                String sel = cbPerfExercise.getValue();
                if (sel != null && !sel.isEmpty()) renderPerfChart(sel);
            });
        }
        // Init IMC sexe combo
        if (cbImcSexe != null) cbImcSexe.getItems().addAll("Homme", "Femme");
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
        stopExplorePlayer();
        plannerPlansContent.setVisible(true);     plannerPlansContent.setManaged(true);
        plannerProgramsContent.setVisible(false); plannerProgramsContent.setManaged(false);
        plannerCrudContent.setVisible(false);     plannerCrudContent.setManaged(false);
        plannerCoachContent.setVisible(false);    plannerCoachContent.setManaged(false);
        plannerExploreContent.setVisible(false);  plannerExploreContent.setManaged(false);
        lblPlannerContentHint.setText("Les seances confirmees depuis Programs sont affichees ici.");
        renderPlannedExercises();
    }

    @FXML
    private void showPlannerPrograms() {
        activatePlannerTab(btnPlannerPrograms);
        stopExplorePlayer();
        plannerPlansContent.setVisible(false);    plannerPlansContent.setManaged(false);
        plannerProgramsContent.setVisible(true);  plannerProgramsContent.setManaged(true);
        plannerCrudContent.setVisible(false);     plannerCrudContent.setManaged(false);
        plannerCoachContent.setVisible(false);    plannerCoachContent.setManaged(false);
        plannerExploreContent.setVisible(false);  plannerExploreContent.setManaged(false);
        lblPlannerContentHint.setText("Choisissez un programme pour ouvrir le choix du lieu d'entrainement.");
    }

    @FXML
    private void showPlannerExercises() {
        activatePlannerTab(btnPlannerExercises);
        stopExplorePlayer();
        plannerPlansContent.setVisible(false);    plannerPlansContent.setManaged(false);
        plannerProgramsContent.setVisible(false); plannerProgramsContent.setManaged(false);
        plannerCrudContent.setVisible(true);      plannerCrudContent.setManaged(true);
        plannerCoachContent.setVisible(false);    plannerCoachContent.setManaged(false);
        plannerExploreContent.setVisible(false);  plannerExploreContent.setManaged(false);
        lblPlannerContentHint.setText("Retour");
    }

    @FXML
    private void showPlannerExplore() {
        activatePlannerTab(btnPlannerExplore);
        plannerPlansContent.setVisible(false);    plannerPlansContent.setManaged(false);
        plannerProgramsContent.setVisible(false); plannerProgramsContent.setManaged(false);
        plannerCrudContent.setVisible(false);     plannerCrudContent.setManaged(false);
        plannerCoachContent.setVisible(false);    plannerCoachContent.setManaged(false);
        plannerExploreContent.setVisible(true);   plannerExploreContent.setManaged(true);
        lblPlannerContentHint.setText("Feed vidéo fitness — locales + YouTube.");
        initExploreFeed();
    }

    @FXML
    private void showPlannerCoach() {
        activatePlannerTab(btnPlannerCoach);
        stopExplorePlayer();
        plannerPlansContent.setVisible(false);    plannerPlansContent.setManaged(false);
        plannerProgramsContent.setVisible(false); plannerProgramsContent.setManaged(false);
        plannerCrudContent.setVisible(false);     plannerCrudContent.setManaged(false);
        plannerExploreContent.setVisible(false);  plannerExploreContent.setManaged(false);
        plannerCoachContent.setVisible(true);     plannerCoachContent.setManaged(true);
        lblPlannerContentHint.setText("Choisissez un coach et reservez votre seance.");
        renderCoachCards();
    }

    private void stopExplorePlayer() {
        if (explorePlayer != null) {
            explorePlayer.stop();
            explorePlayer.dispose();
            explorePlayer = null;
        }
        if (exploreMediaView != null) exploreMediaView.setMediaPlayer(null);
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
        coachTts.setEnabled(coachIaEnabled);
        btnCoachIa.setText(coachIaEnabled ? "Coach IA Off" : "Coach IA On");
        String exerciseName = safeExerciseName();
        if (coachIaEnabled) {
            String[] tips = COACH_WORK_TIPS[coachTipCycle % COACH_WORK_TIPS.length];
            String tip = tips[currentRound > 0 ? (currentRound - 1) % tips.length : 0];
            afficherTrainingInfo("Coach IA: " + tip + " (" + exerciseName + ")");
        } else {
            coachTts.cancelCurrent();
            afficherTrainingInfo("Coach IA desactive.");
        }
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
        sliderGlobalVolume.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateGlobalVolume(newValue.doubleValue());
            coachTts.setVolume(newValue.doubleValue());
        });
    }

    private void ensureTrainingTimeline() {
        if (trainingTimeline != null) {
            return;
        }
        trainingTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> advanceTrainingTimer()));
        trainingTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    private static final String[][] COACH_WORK_TIPS = {
        {"Gardez le dos bien plaque.", "Respirez : expirez a l'effort.", "Contractez les muscles cibles."},
        {"Contrôlez la descente lentement.", "Gardez les coudes alignes.", "Poussez jusqu'au bout du mouvement."},
        {"Restez concentre sur la forme.", "Ne bloquez pas votre respiration.", "Serrez les abdos pour stabiliser."}
    };
    private static final String[] COACH_REST_TIPS = {
        "Repos actif : respirez profondement.",
        "Hydratez-vous, prochain round bientot.",
        "Detendez les muscles, restez concentre."
    };
    private static final String[] COACH_HALFWAY_TIPS = {
        "Mi-parcours ! Gardez l'intensite.",
        "Vous etes a mi-chemin, continuez !",
        "Bonne progression, ne lachez pas !"
    };
    private int coachTipCycle = 0;

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

        // Coach IA: tips at key moments during WORK phase
        if (coachIaEnabled && currentTrainingPhase == TrainingPhase.WORK) {
            int halfWork = workSeconds / 2;
            if (remainingSeconds == halfWork) {
                afficherTrainingInfo("Coach IA: " + COACH_HALFWAY_TIPS[currentRound % COACH_HALFWAY_TIPS.length]);
            } else if (remainingSeconds == workSeconds - 5 && workSeconds > 10) {
                String[] tips = COACH_WORK_TIPS[coachTipCycle % COACH_WORK_TIPS.length];
                afficherTrainingInfo("Coach IA: " + tips[(currentRound - 1) % tips.length]);
                coachTipCycle++;
            } else if (remainingSeconds == 3) {
                afficherTrainingInfo("Coach IA: Plus que 3 secondes, donnez tout !");
            }
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
                        afficherTrainingInfo(coachIaEnabled
                            ? "Coach IA: Excellent travail ! Seance terminee."
                            : "Dernier exercice termine. Seance terminee.");
                    } else {
                        afficherTrainingInfo(coachIaEnabled
                            ? "Coach IA: Bien joue ! Passez a l'exercice suivant."
                            : "Exercice termine. Passez a l'exercice suivant.");
                    }
                } else {
                    currentTrainingPhase = TrainingPhase.REST;
                    remainingSeconds = restSeconds;
                    afficherTrainingInfo(coachIaEnabled
                        ? "Coach IA: " + COACH_REST_TIPS[currentRound % COACH_REST_TIPS.length]
                        : "Phase de repos.");
                }
            } else if (currentTrainingPhase == TrainingPhase.REST) {
                currentRound++;
                currentTrainingPhase = TrainingPhase.WORK;
                remainingSeconds = workSeconds;
                afficherTrainingInfo(coachIaEnabled
                    ? "Coach IA: Round " + currentRound + " - Allez, on repart !"
                    : "Round " + currentRound + " en cours.");
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
        coachTipCycle = 0;
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
        coachTts.cancelCurrent();
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
        // Speak coach messages aloud
        if (coachIaEnabled && message.startsWith("Coach IA:")) {
            String spoken = message.substring("Coach IA:".length()).trim();
            coachTts.speak(spoken);
        }
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
        List<FitnessExercise> list = serviceFitnessExercise.getAll();
        tableExercises.setItems(FXCollections.observableArrayList(list));
        renderExerciseCards(list);
    }

    private void renderExerciseCards(List<FitnessExercise> exercises) {
        if (exerciseCardsGrid == null) return;
        exerciseCardsGrid.getChildren().clear();

        if (exercises.isEmpty()) {
            javafx.scene.control.Label empty = new javafx.scene.control.Label("Aucun exercice enregistré.");
            empty.setStyle("-fx-text-fill:#64748b;-fx-font-size:13px;");
            exerciseCardsGrid.getChildren().add(empty);
            return;
        }

        for (FitnessExercise ex : exercises) {
            javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(6);
            card.setPrefWidth(200);
            card.setMaxWidth(220);
            card.setStyle("-fx-background-color:#fff;-fx-border-color:#dce9ee;-fx-border-radius:12;"
                + "-fx-background-radius:12;-fx-padding:12;"
                + "-fx-effect:dropshadow(gaussian,rgba(10,36,54,0.07),8,0,0,2);-fx-cursor:hand;");

            // Couleur badge difficulté
            String badgeColor = switch (ex.getDifficulty() == null ? "" : ex.getDifficulty().toLowerCase()) {
                case "avance", "advanced", "intermediaire" -> "#f59e0b";
                case "expert" -> "#ef4444";
                default -> "#22c55e";
            };

            javafx.scene.layout.HBox header = new javafx.scene.layout.HBox(6);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(ex.getName());
            nameLabel.setStyle("-fx-font-weight:800;-fx-font-size:12px;-fx-text-fill:#0b3a3d;");
            nameLabel.setWrapText(true);
            nameLabel.setMaxWidth(140);
            javafx.scene.layout.HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);

            javafx.scene.control.Label badge = new javafx.scene.control.Label(ex.getDifficulty() != null ? ex.getDifficulty() : "");
            badge.setStyle("-fx-background-color:" + badgeColor + ";-fx-text-fill:#fff;"
                + "-fx-font-size:9px;-fx-font-weight:700;-fx-background-radius:6;-fx-padding:2 6;");

            header.getChildren().addAll(nameLabel, badge);

            javafx.scene.control.Label muscleLabel = new javafx.scene.control.Label("💪 " + (ex.getMuscleGroup() != null ? ex.getMuscleGroup() : ""));
            muscleLabel.setStyle("-fx-font-size:11px;-fx-text-fill:#1a6b5a;-fx-font-weight:600;");

            javafx.scene.layout.HBox stats = new javafx.scene.layout.HBox(10);
            if (ex.getSets() > 0)
                stats.getChildren().add(styledStat(ex.getSets() + " séries"));
            if (ex.getRepetitions() > 0)
                stats.getChildren().add(styledStat(ex.getRepetitions() + " reps"));
            if (ex.getDuration() > 0)
                stats.getChildren().add(styledStat(ex.getDuration() + " min"));

            card.getChildren().addAll(header, muscleLabel);
            if (!stats.getChildren().isEmpty()) card.getChildren().add(stats);

            // Clic → remplir le formulaire
            card.setOnMouseClicked(e -> {
                remplirFormulaire(ex);
                tableExercises.getSelectionModel().select(ex);
            });

            exerciseCardsGrid.getChildren().add(card);
        }
    }

    private javafx.scene.control.Label styledStat(String text) {
        javafx.scene.control.Label l = new javafx.scene.control.Label(text);
        l.setStyle("-fx-background-color:#f1f5f9;-fx-text-fill:#475467;-fx-font-size:10px;"
            + "-fx-background-radius:5;-fx-padding:2 6;");
        return l;
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

    // ── Explore Feed ─────────────────────────────────────────────────────────
    private record VideoEntry(String title, String desc, String[] tags, String resourcePath, String youtubeId) {
        boolean isLocal() { return resourcePath != null && !resourcePath.isBlank(); }
    }

    private static final List<VideoEntry> ALL_VIDEOS = List.of(
        new VideoEntry("Back Intense at Home — 5 min",      "Workout dos intense 5 min sans équipement.",          new String[]{"Dos","Maison","5 min"},       "/media/fitness/local/back-home-5min.mp4",          null),
        new VideoEntry("Circuit Abdos Complet — 20 min",    "Circuit abdos complet à suivre en temps réel.",       new String[]{"Abdos","Core","20 min"},       "/media/fitness/local/abdos-complet-20min.mp4",     null),
        new VideoEntry("Pectoraux Musclés — 5 min",         "Entraînement pectoraux à la maison.",                 new String[]{"Poitrine","Maison","5 min"},   "/media/fitness/local/pectoraux-5min.mp4",          null),
        new VideoEntry("Triceps Workout — Dumbbells 10 min","10 min de triceps avec haltères.",                    new String[]{"Triceps","Haltères","10 min"}, "/media/fitness/local/triceps-dumbbells-10min.mp4", null),
        new VideoEntry("Biceps Burning — 15 min",           "15 min de biceps avec haltères par Caroline Girvan.", new String[]{"Biceps","Haltères","15 min"},  "/media/fitness/local/biceps-15min.mp4",            null),
        new VideoEntry("Chest Workout — Poitrine Builder",  "Séance poitrine complète pour débutants.",            new String[]{"Poitrine","YouTube","Gym"},    null, "IODxDxX7oi4"),
        new VideoEntry("Dos Strong Flow",                   "Renforcement du dos, colonne neutre.",                new String[]{"Dos","YouTube","Posture"},     null, "roCP6wCXPqo"),
        new VideoEntry("Jambes Power Squat",                "Squats et fentes pour des jambes puissantes.",        new String[]{"Jambes","YouTube","Force"},    null, "aclHkVaku9U"),
        new VideoEntry("Fessier Focus — Hip Thrust",        "Contracte les fessiers en haut du mouvement.",       new String[]{"Fessier","YouTube","Maison"},  null, "Xyd_fa5zoEU"),
        new VideoEntry("Épaules Sculpt",                    "Amplitude contrôlée, ne monte pas les épaules.",     new String[]{"Épaules","YouTube","Haltères"},null, "qEwKCR5JCog"),
        new VideoEntry("Abdos Core 10 min",                 "10 min de core intense, dos collé au sol.",           new String[]{"Abdos","YouTube","Core"},      null, "AnYl6Nk9GOA"),
        new VideoEntry("Corps Entier HIIT",                 "Full body haute intensité, respire en rythme.",       new String[]{"HIIT","YouTube","Corps entier"},null,"ml6cT4AZdqI")
    );

    private List<VideoEntry> exploreFiltered = new java.util.ArrayList<>(ALL_VIDEOS);
    private int exploreIndex = 0;
    private javafx.scene.media.MediaPlayer explorePlayer = null;
    private boolean exploreMuted = false;
    private int exploreLikes = 0;

    private void initExploreFeed() {
        exploreFiltered = new java.util.ArrayList<>(ALL_VIDEOS);
        exploreIndex = 0;
        if (sliderExploreVolume != null) {
            sliderExploreVolume.valueProperty().addListener((obs, o, n) -> {
                if (explorePlayer != null) explorePlayer.setVolume(n.doubleValue());
            });
        }
        renderExplorePlaylist();
        loadExploreVideo(0);
    }

    private void renderExplorePlaylist() {
        if (explorePlaylistBox == null) return;
        explorePlaylistBox.getChildren().clear();
        for (int i = 0; i < exploreFiltered.size(); i++) {
            final int idx = i;
            VideoEntry v = exploreFiltered.get(i);
            javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(8);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color:" + (i == exploreIndex ? "#e8f5f0" : "#fff")
                + ";-fx-border-color:#dce9ee;-fx-border-radius:10;-fx-background-radius:10;-fx-padding:8 10;-fx-cursor:hand;");

            // Badge source
            javafx.scene.control.Label badge = new javafx.scene.control.Label(v.isLocal() ? "📁" : "▶");
            badge.setStyle("-fx-font-size:13px;");

            javafx.scene.layout.VBox info = new javafx.scene.layout.VBox(2);
            javafx.scene.control.Label title = new javafx.scene.control.Label(v.title());
            title.setStyle("-fx-font-weight:700;-fx-font-size:11px;-fx-text-fill:#0b3a3d;");
            title.setWrapText(true);
            javafx.scene.control.Label tags = new javafx.scene.control.Label(String.join(" · ", v.tags()));
            tags.setStyle("-fx-font-size:10px;-fx-text-fill:#64748b;");
            info.getChildren().addAll(title, tags);
            javafx.scene.layout.HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

            row.getChildren().addAll(badge, info);
            row.setOnMouseClicked(e -> loadExploreVideo(idx));
            explorePlaylistBox.getChildren().add(row);
        }
        if (lblExploreCount != null)
            lblExploreCount.setText(exploreFiltered.size() + " vidéo(s) disponible(s)");
    }

    private void loadExploreVideo(int index) {
        if (exploreFiltered.isEmpty()) return;
        exploreIndex = Math.max(0, Math.min(index, exploreFiltered.size() - 1));
        VideoEntry v = exploreFiltered.get(exploreIndex);

        // Stop previous
        if (explorePlayer != null) {
            explorePlayer.stop();
            explorePlayer.dispose();
            explorePlayer = null;
        }
        if (exploreMediaView != null) exploreMediaView.setMediaPlayer(null);

        // Update labels
        if (lblExploreVideoTitle != null) lblExploreVideoTitle.setText(v.title());
        if (lblExploreDesc != null) lblExploreDesc.setText(v.desc() + "  |  Tags : " + String.join(", ", v.tags()));
        if (lblExploreNowPlaying != null) lblExploreNowPlaying.setText("▶ " + (exploreIndex + 1) + "/" + exploreFiltered.size() + " — " + v.title());
        if (btnExploreLike != null) { exploreLikes = 0; btnExploreLike.setText("❤ 0"); }

        // Highlight playlist row
        renderExplorePlaylist();

        if (v.isLocal()) {
            // Local MP4
            URL url = getClass().getResource(v.resourcePath());
            if (url == null) {
                if (lblExploreDesc != null) lblExploreDesc.setText("⚠ Fichier introuvable : " + v.resourcePath());
                return;
            }
            javafx.scene.media.Media media = new javafx.scene.media.Media(url.toExternalForm());
            explorePlayer = new javafx.scene.media.MediaPlayer(media);
            explorePlayer.setAutoPlay(true);
            explorePlayer.setCycleCount(javafx.scene.media.MediaPlayer.INDEFINITE);
            explorePlayer.setVolume(sliderExploreVolume != null ? sliderExploreVolume.getValue() : 0.8);
            explorePlayer.setMute(exploreMuted);
            explorePlayer.setOnEndOfMedia(() -> loadExploreVideo(exploreIndex + 1));
            if (exploreMediaView != null) exploreMediaView.setMediaPlayer(explorePlayer);
            if (btnExplorePlay != null) btnExplorePlay.setText("⏸ Pause");
        } else {
            // YouTube — ouvre dans le navigateur
            if (lblExploreDesc != null)
                lblExploreDesc.setText("▶ YouTube : " + v.title() + "\n" + v.desc() + "\nCliquez sur 'Ouvrir YouTube' pour regarder.");
            if (btnExplorePlay != null) btnExplorePlay.setText("▶ Ouvrir YouTube");
            // Show thumbnail via background
            if (explorePlayerPane != null) {
                String thumbUrl = "https://img.youtube.com/vi/" + v.youtubeId() + "/hqdefault.jpg";
                try {
                    javafx.scene.image.ImageView thumb = new javafx.scene.image.ImageView(
                        new javafx.scene.image.Image(thumbUrl, true));
                    thumb.setFitWidth(700); thumb.setFitHeight(420);
                    thumb.setPreserveRatio(false);
                    // Replace or add thumbnail
                    explorePlayerPane.getChildren().removeIf(n -> n instanceof javafx.scene.image.ImageView);
                    explorePlayerPane.getChildren().add(0, thumb);
                } catch (Exception ignored) {}
            }
        }
    }

    @FXML
    private void explorePlayPause() {
        VideoEntry v = exploreFiltered.isEmpty() ? null : exploreFiltered.get(exploreIndex);
        if (v != null && !v.isLocal()) {
            // YouTube → open browser
            try { java.awt.Desktop.getDesktop().browse(new java.net.URI("https://www.youtube.com/watch?v=" + v.youtubeId())); }
            catch (Exception ignored) {}
            return;
        }
        if (explorePlayer == null) return;
        if (explorePlayer.getStatus() == javafx.scene.media.MediaPlayer.Status.PLAYING) {
            explorePlayer.pause();
            if (btnExplorePlay != null) btnExplorePlay.setText("▶ Play");
        } else {
            explorePlayer.play();
            if (btnExplorePlay != null) btnExplorePlay.setText("⏸ Pause");
        }
    }

    @FXML private void exploreNext() { loadExploreVideo(exploreIndex + 1); }
    @FXML private void explorePrev() { loadExploreVideo(exploreIndex - 1); }

    @FXML
    private void exploreMute() {
        exploreMuted = !exploreMuted;
        if (explorePlayer != null) explorePlayer.setMute(exploreMuted);
        if (btnExploreMute != null) btnExploreMute.setText(exploreMuted ? "🔊 Son" : "🔇 Mute");
    }

    @FXML
    private void exploreLike() {
        exploreLikes++;
        if (btnExploreLike != null) btnExploreLike.setText("❤ " + exploreLikes);
    }

    @FXML
    private void filtrerExploreFeed() {
        String q = tfExploreFilter != null ? tfExploreFilter.getText().trim().toLowerCase() : "";
        if (q.isEmpty()) { resetExploreFeed(); return; }
        exploreFiltered = ALL_VIDEOS.stream().filter(v -> {
            String combined = v.title().toLowerCase() + " " + v.desc().toLowerCase()
                + " " + String.join(" ", v.tags()).toLowerCase();
            return combined.contains(q);
        }).collect(java.util.stream.Collectors.toList());
        exploreIndex = 0;
        renderExplorePlaylist();
        if (!exploreFiltered.isEmpty()) loadExploreVideo(0);
        else { if (lblExploreCount != null) lblExploreCount.setText("Aucune vidéo pour ce filtre."); }
    }

    @FXML
    private void resetExploreFeed() {
        if (tfExploreFilter != null) tfExploreFilter.clear();
        exploreFiltered = new java.util.ArrayList<>(ALL_VIDEOS);
        exploreIndex = 0;
        renderExplorePlaylist();
        loadExploreVideo(0);
    }

    // ── Performance Tracker ───────────────────────────────────────────────────

    private static final List<String> PERF_EXERCISES = List.of(
        "Développé couché avec barre", "Développé incliné haltères", "Dips pectoraux",
        "Tractions", "Rowing barre", "Tirage poulie haute",
        "Squat barre", "Presse à cuisses", "Fentes haltères",
        "Développé militaire", "Élévations latérales",
        "Curl biceps haltères", "Curl barre EZ",
        "Extension triceps poulie", "Dips triceps banc",
        "Crunch abdominaux", "Planche", "Course à pied"
    );

    @FXML
    private void togglePerfForm() {
        if (perfFormBox == null) return;
        boolean visible = perfFormBox.isVisible();
        perfFormBox.setVisible(!visible);
        perfFormBox.setManaged(!visible);
        if (!visible) {
            perfCurrentExercises.clear();
            perfAddExerciseBlock();
            if (tfPerfDate != null) tfPerfDate.setText(java.time.LocalDate.now().toString());
            if (tfPerfLabel != null) tfPerfLabel.clear();
            if (lblPerfStatus != null) lblPerfStatus.setText("");
        }
    }

    @FXML
    private void perfCancelForm() {
        if (perfFormBox != null) { perfFormBox.setVisible(false); perfFormBox.setManaged(false); }
    }

    @FXML
    private void perfAddExercise() { perfAddExerciseBlock(); }

    private void perfAddExerciseBlock() {
        perfCurrentExercises.add(new PerfExEntry("", new java.util.ArrayList<>(List.of(new PerfSet(0, 0)))));
        renderPerfExerciseBlocks();
    }

    private void renderPerfExerciseBlocks() {
        if (perfExercisesBox == null) return;
        perfExercisesBox.getChildren().clear();

        for (int exIdx = 0; exIdx < perfCurrentExercises.size(); exIdx++) {
            final int ei = exIdx;
            PerfExEntry entry = perfCurrentExercises.get(ei);

            VBox block = new VBox(6);
            block.setStyle("-fx-background-color:#f8fafc;-fx-border-color:#dce9ee;-fx-border-radius:10;-fx-background-radius:10;-fx-padding:10;");

            // Exercise selector
            HBox selRow = new HBox(6);
            selRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            javafx.scene.control.ComboBox<String> exSel = new javafx.scene.control.ComboBox<>();
            exSel.getItems().addAll(PERF_EXERCISES);
            exSel.setPromptText("Choisir un exercice...");
            exSel.setPrefWidth(280);
            exSel.setStyle("-fx-font-size:11px;");
            if (!entry.exerciseName().isEmpty()) exSel.setValue(entry.exerciseName());
            exSel.setOnAction(e -> {
                if (exSel.getValue() != null) {
                    perfCurrentExercises.set(ei, new PerfExEntry(exSel.getValue(), new java.util.ArrayList<>(perfCurrentExercises.get(ei).sets())));
                }
            });

            javafx.scene.control.Button removeEx = new javafx.scene.control.Button("✕");
            removeEx.setStyle("-fx-background-color:#fee2e2;-fx-text-fill:#991b1b;-fx-font-weight:700;-fx-background-radius:6;-fx-border-radius:6;-fx-padding:3 7;");
            removeEx.setOnAction(e -> { perfCurrentExercises.remove(ei); renderPerfExerciseBlocks(); });
            selRow.getChildren().addAll(exSel, removeEx);
            block.getChildren().add(selRow);

            // Sets
            List<PerfSet> sets = entry.sets();
            for (int si = 0; si < sets.size(); si++) {
                final int sIdx = si;
                HBox setRow = new HBox(6);
                setRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                javafx.scene.control.Label setLbl = new javafx.scene.control.Label("S" + (si + 1));
                setLbl.setStyle("-fx-font-size:10px;-fx-font-weight:700;-fx-text-fill:#64748b;-fx-min-width:20;");

                javafx.scene.control.TextField weightFld = new javafx.scene.control.TextField(sets.get(si).weightKg() > 0 ? String.valueOf(sets.get(si).weightKg()) : "");
                weightFld.setPromptText("Poids kg");
                weightFld.setPrefWidth(80);
                weightFld.setStyle("-fx-font-size:11px;-fx-border-color:#dce9ee;-fx-border-radius:7;-fx-background-radius:7;-fx-padding:4 6;");

                javafx.scene.control.TextField repsFld = new javafx.scene.control.TextField(sets.get(si).reps() > 0 ? String.valueOf(sets.get(si).reps()) : "");
                repsFld.setPromptText("Reps");
                repsFld.setPrefWidth(60);
                repsFld.setStyle("-fx-font-size:11px;-fx-border-color:#dce9ee;-fx-border-radius:7;-fx-background-radius:7;-fx-padding:4 6;");

                // Update on change
                weightFld.textProperty().addListener((obs, o, n) -> updatePerfSet(ei, sIdx, n, repsFld.getText()));
                repsFld.textProperty().addListener((obs, o, n) -> updatePerfSet(ei, sIdx, weightFld.getText(), n));

                javafx.scene.control.Button delSet = new javafx.scene.control.Button("−");
                delSet.setStyle("-fx-background-color:#f1f5f9;-fx-border-radius:6;-fx-background-radius:6;-fx-padding:3 7;-fx-font-weight:700;");
                delSet.setOnAction(e -> {
                    if (perfCurrentExercises.get(ei).sets().size() > 1) {
                        perfCurrentExercises.get(ei).sets().remove(sIdx);
                        renderPerfExerciseBlocks();
                    }
                });

                setRow.getChildren().addAll(setLbl, weightFld, repsFld, delSet);
                block.getChildren().add(setRow);
            }

            // Add set button
            javafx.scene.control.Button addSet = new javafx.scene.control.Button("+ Série");
            addSet.setStyle("-fx-background-color:#e8f5f0;-fx-text-fill:#1a6b5a;-fx-font-weight:700;-fx-font-size:10px;-fx-background-radius:7;-fx-border-radius:7;-fx-padding:3 8;");
            addSet.setOnAction(e -> {
                perfCurrentExercises.get(ei).sets().add(new PerfSet(0, 0));
                renderPerfExerciseBlocks();
            });
            block.getChildren().add(addSet);
            perfExercisesBox.getChildren().add(block);
        }
    }

    private void updatePerfSet(int exIdx, int setIdx, String weightStr, String repsStr) {
        try {
            double w = weightStr.isBlank() ? 0 : Double.parseDouble(weightStr.replace(",", "."));
            int r    = repsStr.isBlank()   ? 0 : Integer.parseInt(repsStr.trim());
            List<PerfSet> sets = perfCurrentExercises.get(exIdx).sets();
            if (setIdx < sets.size()) sets.set(setIdx, new PerfSet(w, r));
        } catch (NumberFormatException ignored) {}
    }

    @FXML
    private void perfSaveSession() {
        if (lblPerfStatus == null) return;
        String date  = tfPerfDate  != null ? tfPerfDate.getText().trim()  : java.time.LocalDate.now().toString();
        String label = tfPerfLabel != null ? tfPerfLabel.getText().trim()  : "Séance du " + date;
        if (label.isEmpty()) label = "Séance du " + date;

        List<PerfExEntry> valid = perfCurrentExercises.stream()
            .filter(e -> !e.exerciseName().isEmpty())
            .filter(e -> e.sets().stream().anyMatch(s -> s.weightKg() > 0 || s.reps() > 0))
            .toList();

        if (valid.isEmpty()) {
            lblPerfStatus.setStyle("-fx-text-fill:#991b1b;");
            lblPerfStatus.setText("⚠ Ajoute au moins un exercice avec des données.");
            return;
        }

        perfHistory.add(0, new PerfSession(date, label, valid));
        lblPerfStatus.setStyle("-fx-text-fill:#065f46;-fx-font-weight:700;");
        lblPerfStatus.setText("✅ Séance enregistrée !");

        // Refresh UI
        renderPerfHistory();
        refreshPerfChart();
        populatePerfExerciseCombo();

        // Close form after 1.5s
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
        pause.setOnFinished(e -> {
            if (perfFormBox != null) { perfFormBox.setVisible(false); perfFormBox.setManaged(false); }
        });
        pause.play();
    }

    private void populatePerfExerciseCombo() {
        if (cbPerfExercise == null) return;
        String current = cbPerfExercise.getValue();
        cbPerfExercise.getItems().clear();
        // Only exercises that have data
        perfHistory.stream()
            .flatMap(s -> s.exercises().stream())
            .map(PerfExEntry::exerciseName)
            .distinct()
            .sorted()
            .forEach(name -> cbPerfExercise.getItems().add(name));
        if (current != null && cbPerfExercise.getItems().contains(current))
            cbPerfExercise.setValue(current);
    }

    @FXML
    private void perfShowWeight() { perfChartMetric = "weight"; refreshPerfChart(); }

    @FXML
    private void perfShowVolume() { perfChartMetric = "volume"; refreshPerfChart(); }

    private void refreshPerfChart() {
        if (cbPerfExercise == null) return;
        String selected = cbPerfExercise.getValue();
        if (selected == null || selected.isEmpty()) return;
        renderPerfChart(selected);
    }

    private void renderPerfChart(String exerciseName) {
        if (perfChartBox == null) return;
        perfChartBox.getChildren().clear();

        // Collect weekly data: week → max weight or total volume
        java.util.Map<String, Double> weekData = new java.util.LinkedHashMap<>();
        for (PerfSession session : perfHistory) {
            for (PerfExEntry ex : session.exercises()) {
                if (!ex.exerciseName().equals(exerciseName)) continue;
                // Compute week label
                String weekLabel;
                try {
                    java.time.LocalDate d = java.time.LocalDate.parse(session.date());
                    weekLabel = "S" + d.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR) + " " + d.getYear();
                } catch (Exception e) { weekLabel = session.date(); }

                double value;
                if ("weight".equals(perfChartMetric)) {
                    value = ex.sets().stream().mapToDouble(PerfSet::weightKg).max().orElse(0);
                } else {
                    value = ex.sets().stream().mapToDouble(s -> s.weightKg() * s.reps()).sum();
                }
                weekData.merge(weekLabel, value, Math::max);
            }
        }

        if (weekData.isEmpty()) {
            if (lblPerfChartEmpty != null) {
                lblPerfChartEmpty.setText("Aucune donnée pour " + exerciseName + ".");
                lblPerfChartEmpty.setVisible(true);
            }
            return;
        }
        if (lblPerfChartEmpty != null) lblPerfChartEmpty.setVisible(false);

        double maxVal = weekData.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
        String unit   = "weight".equals(perfChartMetric) ? " kg" : " kg×reps";
        double barMaxWidth = 420.0;

        // Title
        javafx.scene.control.Label chartTitle = new javafx.scene.control.Label(
            exerciseName + " — " + ("weight".equals(perfChartMetric) ? "Poids max" : "Volume total"));
        chartTitle.setStyle("-fx-font-weight:800;-fx-font-size:12px;-fx-text-fill:#0b3a3d;-fx-padding:0 0 4 0;");
        perfChartBox.getChildren().add(chartTitle);

        // Bars
        for (java.util.Map.Entry<String, Double> entry : weekData.entrySet()) {
            double val     = entry.getValue();
            double ratio   = maxVal > 0 ? val / maxVal : 0;
            double barW    = Math.max(4, ratio * barMaxWidth);

            HBox row = new HBox(8);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            // Week label
            javafx.scene.control.Label weekLbl = new javafx.scene.control.Label(entry.getKey());
            weekLbl.setStyle("-fx-font-size:10px;-fx-text-fill:#64748b;-fx-min-width:60;");

            // Bar
            javafx.scene.layout.StackPane barPane = new javafx.scene.layout.StackPane();
            barPane.setPrefHeight(22);
            barPane.setPrefWidth(barMaxWidth);
            barPane.setStyle("-fx-background-color:#f1f5f9;-fx-background-radius:6;");

            javafx.scene.layout.Region bar = new javafx.scene.layout.Region();
            bar.setPrefHeight(22);
            bar.setPrefWidth(barW);
            bar.setStyle("-fx-background-color:linear-gradient(to right,#0b3a3d,#1a6b5a);-fx-background-radius:6;");
            barPane.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            barPane.getChildren().add(bar);

            // Value label
            javafx.scene.control.Label valLbl = new javafx.scene.control.Label(
                String.format("%.1f", val) + unit);
            valLbl.setStyle("-fx-font-size:10px;-fx-font-weight:700;-fx-text-fill:#0b3a3d;");

            row.getChildren().addAll(weekLbl, barPane, valLbl);
            perfChartBox.getChildren().add(row);
        }
    }

    private void renderPerfHistory() {
        if (perfHistoryBox == null) return;
        perfHistoryBox.getChildren().clear();

        if (perfHistory.isEmpty()) {
            javafx.scene.control.Label empty = new javafx.scene.control.Label("Aucune séance enregistrée.");
            empty.setStyle("-fx-font-size:11px;-fx-text-fill:#94a3b8;");
            perfHistoryBox.getChildren().add(empty);
            return;
        }

        for (PerfSession session : perfHistory) {
            VBox card = new VBox(5);
            card.setStyle("-fx-background-color:#fff;-fx-border-color:#dce9ee;-fx-border-radius:10;-fx-background-radius:10;-fx-padding:8 10;");

            HBox header = new HBox(8);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            javafx.scene.control.Label dateLbl = new javafx.scene.control.Label(session.label());
            dateLbl.setStyle("-fx-font-weight:700;-fx-font-size:11px;-fx-text-fill:#0b3a3d;");
            javafx.scene.layout.HBox.setHgrow(dateLbl, javafx.scene.layout.Priority.ALWAYS);

            // Week badge
            String weekBadge = "";
            try {
                java.time.LocalDate d = java.time.LocalDate.parse(session.date());
                weekBadge = "S" + d.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            } catch (Exception ignored) { weekBadge = session.date(); }
            javafx.scene.control.Label weekLbl = new javafx.scene.control.Label(weekBadge);
            weekLbl.setStyle("-fx-font-size:10px;-fx-background-color:#f1f5f9;-fx-text-fill:#64748b;-fx-background-radius:999;-fx-padding:2 7;");
            header.getChildren().addAll(dateLbl, weekLbl);

            // Exercise tags
            javafx.scene.layout.FlowPane tags = new javafx.scene.layout.FlowPane(5, 4);
            for (PerfExEntry ex : session.exercises()) {
                double maxW = ex.sets().stream().mapToDouble(PerfSet::weightKg).max().orElse(0);
                javafx.scene.control.Label tag = new javafx.scene.control.Label(
                    ex.exerciseName() + (maxW > 0 ? " · " + String.format("%.1f", maxW) + "kg" : ""));
                tag.setStyle("-fx-font-size:10px;-fx-background-color:#e8f5f0;-fx-text-fill:#1a6b5a;-fx-background-radius:999;-fx-padding:2 8;-fx-font-weight:600;");
                tags.getChildren().add(tag);
            }

            card.getChildren().addAll(header, tags);
            perfHistoryBox.getChildren().add(card);
        }
    }

    // ── renderCoachCards ──────────────────────────────────────────────────────
    private record CoachData(String id, String name, String specialty, String phone, String slots, String imagePath, String email) {
        // slots format: "Lun-Ven 18h-21h | Sam 10h-13h"
        // Returns list of (dayLabel, timeRange) pairs for the next 14 days
        List<String[]> availableDates() {
            List<String[]> result = new java.util.ArrayList<>();
            String[] parts = slots.split("\\|");
            java.time.LocalDate today = java.time.LocalDate.now();
            String[] daysFr = {"Dim","Lun","Mar","Mer","Jeu","Ven","Sam"};
            String[] monthsFr = {"Jan","Fév","Mar","Avr","Mai","Jun","Jul","Aoû","Sep","Oct","Nov","Déc"};

            for (int offset = 0; offset <= 20; offset++) {
                java.time.LocalDate date = today.plusDays(offset);
                String dayShort = daysFr[date.getDayOfWeek().getValue() % 7];
                for (String part : parts) {
                    String p = part.trim();
                    // Extract day range like "Lun-Ven" or "Sam" or "Dim"
                    String[] tokens = p.split("\\s+", 2);
                    if (tokens.length < 2) continue;
                    String dayRange = tokens[0];
                    String timeRange = tokens[1];
                    if (matchesDay(dayShort, dayRange)) {
                        String label = dayShort + " " + date.getDayOfMonth() + " " + monthsFr[date.getMonthValue()-1];
                        result.add(new String[]{label, timeRange.trim(), date.toString()});
                        break;
                    }
                }
            }
            return result;
        }

        private boolean matchesDay(String dayShort, String dayRange) {
            String[] daysFr = {"Dim","Lun","Mar","Mer","Jeu","Ven","Sam"};
            if (dayRange.contains("-")) {
                String[] bounds = dayRange.split("-");
                if (bounds.length < 2) return false;
                int start = indexOf(daysFr, bounds[0].trim());
                int end   = indexOf(daysFr, bounds[1].trim());
                int cur   = indexOf(daysFr, dayShort);
                if (start < 0 || end < 0 || cur < 0) return false;
                if (start <= end) return cur >= start && cur <= end;
                else return cur >= start || cur <= end; // wrap (ex: Sam-Lun)
            }
            return dayRange.trim().equalsIgnoreCase(dayShort);
        }

        private int indexOf(String[] arr, String val) {
            for (int i = 0; i < arr.length; i++) if (arr[i].equalsIgnoreCase(val)) return i;
            return -1;
        }

        // Returns time slots (every 30 min) within the range like "18h-21h"
        List<String> timeSlots(String timeRange) {
            List<String> slots = new java.util.ArrayList<>();
            try {
                String[] bounds = timeRange.replace("h", ":00").split("-");
                if (bounds.length < 2) return slots;
                java.time.LocalTime start = java.time.LocalTime.parse(bounds[0].trim().replace("h","").length() <= 2
                    ? bounds[0].trim().replace("h","") + ":00" : bounds[0].trim().replace("h",":"));
                java.time.LocalTime end = java.time.LocalTime.parse(bounds[1].trim().replace("h","").length() <= 2
                    ? bounds[1].trim().replace("h","") + ":00" : bounds[1].trim().replace("h",":"));
                java.time.LocalTime cur = start;
                while (cur.isBefore(end)) {
                    slots.add(String.format("%02d:%02d", cur.getHour(), cur.getMinute()));
                    cur = cur.plusMinutes(30);
                }
            } catch (Exception ignored) {}
            return slots;
        }
    }

    private static final List<CoachData> COACH_LIST = List.of(
        new CoachData("ahmed", "Ahmed Chebbi",  "Boxe & Cardio",           "+216 25 010 582", "Lun-Ven 18h-21h | Sam 10h-13h", "/media/fitness/coaches/ahmed.jpg", "ahmedchebbi323@gmail.com"),
        new CoachData("ali",   "Ali Ben Salah", "Musculation & Force",     "+216 53 919 881", "Lun-Jeu 16h-20h | Dim 09h-12h", "/media/fitness/coaches/ali.jpg",   "ahmedchebbi323@gmail.com"),
        new CoachData("omar",  "Omar Khelifi",  "Judo & Prep physique",    "+216 58 936 689", "Mar-Ven 17h-20h | Sam 15h-18h", "/media/fitness/coaches/omar.jpg",  "ahmedchebbi323@gmail.com"),
        new CoachData("sarah", "Sarah Mansouri","Yoga & Mobilite",         "+216 28 759 998", "Lun-Ven 07h-10h | Sam 08h-11h", "/media/fitness/coaches/sarah.jpg", "ahmedchebbi323@gmail.com"),
        new CoachData("rania", "Rania Trabelsi","Danse orientale & Cardio","+216 58 860 916", "Mer-Ven 18h-21h | Dim 16h-19h", "/media/fitness/coaches/rania.jpg", "ahmedchebbi323@gmail.com"),
        new CoachData("emna",  "Emna Gharbi",   "Pilates & Gainage",       "+216 55 487 965", "Lun-Jeu 10h-13h | Sam 16h-19h", "/media/fitness/coaches/emna.jpg",  "ahmedchebbi323@gmail.com")
    );

    private void renderCoachCards() {
        if (coachCardsContainer == null) return;
        coachCardsContainer.getChildren().clear();

        for (CoachData coach : COACH_LIST) {
            // Photo
            javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(8);
            card.setPrefWidth(220);
            card.setMaxWidth(240);
            card.getStyleClass().add("coach-card-item");
            card.setStyle("-fx-background-color:#fff;-fx-border-color:#dce9ee;-fx-border-radius:14;-fx-background-radius:14;-fx-padding:0;-fx-effect:dropshadow(gaussian,rgba(10,36,54,0.08),10,0,0,3);");

            // Image
            javafx.scene.image.ImageView photo = new javafx.scene.image.ImageView();
            photo.setFitWidth(220);
            photo.setFitHeight(160);
            photo.setPreserveRatio(false);
            photo.setStyle("-fx-background-radius:14 14 0 0;");
            try {
                URL imgUrl = getClass().getResource(coach.imagePath());
                if (imgUrl != null) {
                    photo.setImage(new javafx.scene.image.Image(imgUrl.toExternalForm(), true));
                }
            } catch (Exception ignored) {}

            // Info block
            javafx.scene.layout.VBox info = new javafx.scene.layout.VBox(5);
            info.setStyle("-fx-padding:10 12 12 12;");

            javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(coach.name());
            nameLabel.setStyle("-fx-font-weight:800;-fx-font-size:13px;-fx-text-fill:#0b3a3d;");
            nameLabel.setWrapText(true);

            javafx.scene.control.Label specLabel = new javafx.scene.control.Label(coach.specialty());
            specLabel.setStyle("-fx-font-size:11px;-fx-text-fill:#1a6b5a;-fx-font-weight:600;");

            javafx.scene.control.Label phoneLabel = new javafx.scene.control.Label("\u260E " + coach.phone());
            phoneLabel.setStyle("-fx-font-size:11px;-fx-text-fill:#64748b;");

            javafx.scene.control.Label slotsLabel = new javafx.scene.control.Label("\uD83D\uDCC5 " + coach.slots());
            slotsLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#64748b;");
            slotsLabel.setWrapText(true);

            javafx.scene.control.Button bookBtn = new javafx.scene.control.Button("Reserver une seance");
            bookBtn.setMaxWidth(Double.MAX_VALUE);
            bookBtn.setStyle("-fx-background-color:linear-gradient(to right,#0b3a3d,#1a6b5a);-fx-text-fill:#fff;-fx-font-weight:700;-fx-font-size:11px;-fx-background-radius:8;-fx-padding:7 10;-fx-cursor:hand;");
            bookBtn.setOnAction(e -> ouvrirFormulaireReservation(coach));

            info.getChildren().addAll(nameLabel, specLabel, phoneLabel, slotsLabel, bookBtn);
            card.getChildren().addAll(photo, info);
            coachCardsContainer.getChildren().add(card);
        }
    }

    private void ouvrirFormulaireReservation(CoachData coach) {
        selectedBookingCoach = coach;
        selectedBookingDate = null;

        if (lblBookingCoachName != null)
            lblBookingCoachName.setText("Réserver avec " + coach.name() + " — " + coach.specialty());
        if (lblBookingCoachSlots != null)
            lblBookingCoachSlots.setText("📞 " + coach.phone() + "   |   🗓 Disponibilités : " + coach.slots());
        if (lblSelectedSlot != null) lblSelectedSlot.setText("");
        if (lblBookingStatus != null) lblBookingStatus.setText("");
        if (tfBookingName != null) tfBookingName.clear();
        if (tfBookingEmail != null) tfBookingEmail.clear();
        if (tfBookingPhone != null) tfBookingPhone.clear();
        if (taBookingMessage != null) taBookingMessage.clear();
        if (cbBookingSlot != null) { cbBookingSlot.getItems().clear(); cbBookingSlot.setDisable(true); }

        // Générer les boutons de dates disponibles
        if (bookingDateButtons != null) {
            bookingDateButtons.getChildren().clear();
            List<String[]> dates = coach.availableDates();
            for (String[] entry : dates) {
                // entry = [label, timeRange, isoDate]
                javafx.scene.control.Button btn = new javafx.scene.control.Button(entry[0]);
                btn.setStyle("-fx-background-color:#fff;-fx-border-color:#0b3a3d;-fx-border-radius:8;"
                    + "-fx-background-radius:8;-fx-font-size:11px;-fx-font-weight:600;"
                    + "-fx-text-fill:#0b3a3d;-fx-padding:5 12;-fx-cursor:hand;");
                btn.setOnAction(e -> {
                    // Désélectionner tous
                    bookingDateButtons.getChildren().forEach(n -> n.setStyle(
                        "-fx-background-color:#fff;-fx-border-color:#0b3a3d;-fx-border-radius:8;"
                        + "-fx-background-radius:8;-fx-font-size:11px;-fx-font-weight:600;"
                        + "-fx-text-fill:#0b3a3d;-fx-padding:5 12;-fx-cursor:hand;"));
                    // Sélectionner ce bouton
                    btn.setStyle("-fx-background-color:#0b3a3d;-fx-border-color:#0b3a3d;-fx-border-radius:8;"
                        + "-fx-background-radius:8;-fx-font-size:11px;-fx-font-weight:700;"
                        + "-fx-text-fill:#fff;-fx-padding:5 12;-fx-cursor:hand;");
                    selectedBookingDate = entry[0] + " — " + entry[1];
                    // Remplir les heures
                    if (cbBookingSlot != null) {
                        cbBookingSlot.getItems().clear();
                        cbBookingSlot.getItems().addAll(coach.timeSlots(entry[1]));
                        cbBookingSlot.setDisable(false);
                        if (!cbBookingSlot.getItems().isEmpty())
                            cbBookingSlot.getSelectionModel().selectFirst();
                    }
                    if (lblSelectedSlot != null)
                        lblSelectedSlot.setText("✓ " + entry[0]);
                });
                bookingDateButtons.getChildren().add(btn);
            }
            if (dates.isEmpty()) {
                javafx.scene.control.Label noDate = new javafx.scene.control.Label("Aucun créneau disponible dans les 3 prochaines semaines.");
                noDate.setStyle("-fx-text-fill:#64748b;-fx-font-size:11px;");
                bookingDateButtons.getChildren().add(noDate);
            }
        }

        if (coachBookingForm != null) {
            coachBookingForm.setVisible(true);
            coachBookingForm.setManaged(true);
        }
    }

    @FXML
    private void annulerReservationCoach() {
        selectedBookingCoach = null;
        if (coachBookingForm != null) {
            coachBookingForm.setVisible(false);
            coachBookingForm.setManaged(false);
        }
    }

    @FXML
    private void confirmerReservationCoach() {
        if (selectedBookingCoach == null) return;

        String name  = tfBookingName  != null ? tfBookingName.getText().trim()  : "";
        String email = tfBookingEmail != null ? tfBookingEmail.getText().trim()  : "";
        String phone = tfBookingPhone != null ? tfBookingPhone.getText().trim()  : "";
        String msg   = taBookingMessage != null ? taBookingMessage.getText().trim() : "";
        String slot  = cbBookingSlot != null ? cbBookingSlot.getValue() : null;

        // Validation
        if (name.isEmpty()) {
            if (lblBookingStatus != null) { lblBookingStatus.setStyle("-fx-text-fill:#991b1b;"); lblBookingStatus.setText("⚠ Le nom est obligatoire."); }
            return;
        }
        if (email.isEmpty() || !email.contains("@")) {
            if (lblBookingStatus != null) { lblBookingStatus.setStyle("-fx-text-fill:#991b1b;"); lblBookingStatus.setText("⚠ Email invalide."); }
            return;
        }
        if (phone.isEmpty()) {
            if (lblBookingStatus != null) { lblBookingStatus.setStyle("-fx-text-fill:#991b1b;"); lblBookingStatus.setText("⚠ Le téléphone est obligatoire."); }
            return;
        }
        if (selectedBookingDate == null || selectedBookingDate.isEmpty()) {
            if (lblBookingStatus != null) { lblBookingStatus.setStyle("-fx-text-fill:#991b1b;"); lblBookingStatus.setText("⚠ Veuillez choisir un créneau disponible."); }
            return;
        }
        if (slot == null || slot.isEmpty()) {
            if (lblBookingStatus != null) { lblBookingStatus.setStyle("-fx-text-fill:#991b1b;"); lblBookingStatus.setText("⚠ Veuillez sélectionner une heure."); }
            return;
        }

        String dateComplete = selectedBookingDate + " à " + slot;
        boolean sent = envoyerEmailReservation(selectedBookingCoach, name, email, phone, dateComplete, msg);

        if (sent) {
            if (lblBookingStatus != null) {
                lblBookingStatus.setStyle("-fx-text-fill:#065f46;-fx-font-weight:700;");
                lblBookingStatus.setText("✅ Réservation envoyée ! " + selectedBookingCoach.name() + " vous contactera bientôt.");
            }
            if (tfBookingName != null) tfBookingName.clear();
            if (tfBookingEmail != null) tfBookingEmail.clear();
            if (tfBookingPhone != null) tfBookingPhone.clear();
            if (taBookingMessage != null) taBookingMessage.clear();
            if (cbBookingSlot != null) cbBookingSlot.getSelectionModel().clearSelection();
            if (bookingDateButtons != null)
                bookingDateButtons.getChildren().forEach(n -> n.setStyle(
                    "-fx-background-color:#fff;-fx-border-color:#0b3a3d;-fx-border-radius:8;"
                    + "-fx-background-radius:8;-fx-font-size:11px;-fx-font-weight:600;"
                    + "-fx-text-fill:#0b3a3d;-fx-padding:5 12;-fx-cursor:hand;"));
            selectedBookingDate = null;
            if (lblSelectedSlot != null) lblSelectedSlot.setText("");
        } else {
            if (lblBookingStatus != null) {
                lblBookingStatus.setStyle("-fx-text-fill:#991b1b;");
                lblBookingStatus.setText("❌ Échec envoi email. Vérifiez la configuration SMTP.");
            }
        }
    }

    private boolean envoyerEmailReservation(CoachData coach, String clientName, String clientEmail,
                                             String clientPhone, String date, String message) {
        try {
            String smtpUser = System.getenv("SMTP_USERNAME");
            String smtpPass = System.getenv("SMTP_PASSWORD");
            if (smtpUser == null || smtpUser.isBlank()) smtpUser = "ahmedchebbi323@gmail.com";
            if (smtpPass == null || smtpPass.isBlank()) smtpPass = "jsnxpiuqpzmojsxm";

            java.util.Properties props = new java.util.Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

            final String user = smtpUser;
            final String pass = smtpPass;
            javax.mail.Session session = javax.mail.Session.getInstance(props, new javax.mail.Authenticator() {
                protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new javax.mail.PasswordAuthentication(user, pass);
                }
            });

            javax.mail.internet.MimeMessage mimeMsg = new javax.mail.internet.MimeMessage(session);
            mimeMsg.setFrom(new javax.mail.internet.InternetAddress(smtpUser, "Fitopia Reservations"));
            mimeMsg.setRecipient(javax.mail.Message.RecipientType.TO,
                    new javax.mail.internet.InternetAddress(coach.email(), coach.name()));
            mimeMsg.setReplyTo(new javax.mail.Address[]{
                    new javax.mail.internet.InternetAddress(clientEmail, clientName)
            });
            mimeMsg.setSubject("Nouvelle réservation — " + clientName, "UTF-8");

            String html = "<div style='font-family:Arial,sans-serif;max-width:600px;'>"
                + "<h2 style='color:#0b3a3d;'>Nouvelle demande de réservation</h2>"
                + "<p><b>Coach :</b> " + coach.name() + " (" + coach.specialty() + ")</p>"
                + "<hr/>"
                + "<p><b>Client :</b> " + clientName + "</p>"
                + "<p><b>Email :</b> " + clientEmail + "</p>"
                + "<p><b>Téléphone :</b> " + clientPhone + "</p>"
                + "<p><b>Date souhaitée :</b> " + date + "</p>"
                + (message.isEmpty() ? "" : "<p><b>Message :</b> " + message + "</p>")
                + "<br/><a href='mailto:" + clientEmail + "' style='background:#0b3a3d;color:#fff;padding:10px 20px;border-radius:8px;text-decoration:none;font-weight:bold;'>Répondre au client</a>"
                + "</div>";

            mimeMsg.setContent(html, "text/html; charset=UTF-8");
            javax.mail.Transport.send(mimeMsg);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Feature 2 : Calculateur IMC ──────────────────────────────────────────

    @FXML private void openImcModal() {
        if (imcModalOverlay != null) { imcModalOverlay.setVisible(true); imcModalOverlay.setManaged(true); }
    }
    @FXML private void closeImcModal() {
        if (imcModalOverlay != null) { imcModalOverlay.setVisible(false); imcModalOverlay.setManaged(false); }
    }

    @FXML
    private void calculerImc() {
        try {
            double poids  = Double.parseDouble(tfImcPoids.getText().replace(",", ".").trim());
            double taille = Double.parseDouble(tfImcTaille.getText().replace(",", ".").trim()) / 100.0;
            if (poids <= 0 || taille <= 0) throw new NumberFormatException();

            double imc = poids / (taille * taille);
            String sexe = cbImcSexe.getValue() != null ? cbImcSexe.getValue() : "Homme";

            // Catégorie + couleur + conseil
            String categorie, couleur, conseil, programme;
            double progress;
            if (imc < 18.5) {
                categorie = "Insuffisance pondérale"; couleur = "#3b82f6"; progress = 0.15;
                conseil   = "Augmentez votre apport calorique et faites de la musculation.";
                programme = "Prise de masse — Corps entier (Débutant)";
                imcRecommendedProgram = "Corps entier";
            } else if (imc < 25.0) {
                categorie = "Poids normal ✓"; couleur = "#22c55e"; progress = 0.45;
                conseil   = "Excellent ! Maintenez votre condition avec un programme équilibré.";
                programme = "Programme équilibré — Poitrine + Dos (Intermédiaire)";
                imcRecommendedProgram = "Poitrine";
            } else if (imc < 30.0) {
                categorie = "Surpoids"; couleur = "#f59e0b"; progress = 0.65;
                conseil   = "Combinez cardio et musculation pour perdre du poids progressivement.";
                programme = "Cardio + Jambes (Débutant) — 3x/semaine";
                imcRecommendedProgram = "Jambes";
            } else if (imc < 35.0) {
                categorie = "Obésité modérée"; couleur = "#f97316"; progress = 0.80;
                conseil   = "Commencez doucement avec des exercices à faible impact.";
                programme = "Corps entier léger (Débutant) — 2x/semaine";
                imcRecommendedProgram = "Corps entier";
            } else {
                categorie = "Obésité sévère"; couleur = "#ef4444"; progress = 0.95;
                conseil   = "Consultez un médecin avant de commencer. Marche et mobilité recommandées.";
                programme = "Mobilité & Abdos (Débutant) — 2x/semaine";
                imcRecommendedProgram = "Abdos";
            }

            // Afficher résultat
            if (lblImcValeur    != null) lblImcValeur.setText(String.format("%.1f", imc));
            if (lblImcCategorie != null) { lblImcCategorie.setText(categorie); lblImcCategorie.setStyle("-fx-font-size:16px;-fx-font-weight:800;-fx-text-fill:" + couleur + ";"); }
            if (lblImcConseil   != null) lblImcConseil.setText(conseil);
            if (lblImcProgramme != null) lblImcProgramme.setText(programme);
            if (pbImc           != null) { pbImc.setProgress(progress); pbImc.setStyle("-fx-accent:" + couleur + ";"); }
            if (imcResultBox    != null) { imcResultBox.setVisible(true); imcResultBox.setManaged(true); }

        } catch (NumberFormatException e) {
            if (lblImcConseil != null) { lblImcConseil.setText("⚠ Entrez des valeurs valides (poids en kg, taille en cm)."); lblImcConseil.setStyle("-fx-text-fill:#ef4444;"); }
        }
    }

    @FXML
    private void lancerProgrammeImc() {
        closeImcModal();
        if (imcRecommendedProgram != null) {
            selectedProgram    = imcRecommendedProgram;
            selectedLocation   = null;
            selectedDifficulty = null;
            programModalOverlay.setVisible(true);
            programModalOverlay.setManaged(true);
            showProgramLocationStep();
        }
    }

    // ── Feature 3 : Timer repos (supprimé) ──

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
