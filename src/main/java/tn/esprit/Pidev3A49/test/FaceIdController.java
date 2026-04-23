package tn.esprit.Pidev3A49.test;

import com.github.sarxos.webcam.Webcam;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.services.FaceRecognitionService;
import tn.esprit.Pidev3A49.services.FitopiaUserService;
import tn.esprit.Pidev3A49.services.security.AuthenticationResult;
import tn.esprit.Pidev3A49.utils.SessionRouter;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FaceIdController {

    @FXML private ImageView cameraPreview;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> cameraSelector;
    @FXML private Label cameraDetailsLabel;
    @FXML private TextField identifierField;
    @FXML private PasswordField passwordField;
    @FXML private Button registerFaceButton;
    @FXML private Label previewPlaceholderLabel;

    private final FitopiaUserService fitopiaUserService = new FitopiaUserService();
    private final FaceRecognitionService faceRecognitionService = new FaceRecognitionService();
    private final List<Webcam> detectedWebcams = new ArrayList<>();

    private Webcam webcam;
    private ScheduledExecutorService executor;
    private BufferedImage currentFrame;
    private int emptyFrameCount;
    private String selectedCameraName;
    private boolean verifyFaceIdOnly;

    @FXML
    public void initialize() {
        statusLabel.setText("Cliquez sur Activer camera pour lancer Face ID. Vous pouvez aussi charger une photo.");
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopCamera));
        Platform.runLater(this::bindStageCloseHandler);
        previewPlaceholderLabel.setVisible(true);
        previewPlaceholderLabel.setManaged(true);
        previewPlaceholderLabel.setText("Camera eteinte. Cliquez sur Activer camera.");
        cameraDetailsLabel.setText("La camera reste eteinte jusqu'au clic sur Activer camera.");
        verifyFaceIdOnly = UserSession.isVerifyFaceIdOnly();

        FitopiaUser currentUser = UserSession.getCurrentUser();
        if (currentUser != null && currentUser.isFaceIdEnabled()) {
            String identifier = currentUser.getEmail() != null && !currentUser.getEmail().isBlank()
                    ? currentUser.getEmail()
                    : currentUser.getUsername();
            identifierField.setText(identifier);
            if (verifyFaceIdOnly) {
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                registerFaceButton.setVisible(false);
                registerFaceButton.setManaged(false);
                statusLabel.setText("Verification obligatoire. Remettez votre visage puis cliquez sur Connexion directe.");
            } else {
                statusLabel.setText("Compte Face ID detecte. Cliquez sur Activer camera puis sur Connexion directe pour verification.");
            }
        }
    }

    @FXML
    private void handleSelectCamera() {
        if (detectedWebcams.isEmpty()) {
            loadAvailableCameras();
        }
        activateSelectedCamera();
    }

    @FXML
    private void handleCapture() {
        if (!fitopiaUserService.isAvailable()) {
            statusLabel.setText("Connexion MySQL indisponible. Verifiez fitopiabd.");
            return;
        }
        if (currentFrame == null) {
            statusLabel.setText("Aucune image disponible. Activez la camera ou choisissez une photo.");
            return;
        }

        try {
            String identifier = safe(identifierField.getText()).trim();
            FitopiaUser authenticated;
            FaceRecognitionService.MatchResult result;

            if (!identifier.isBlank()) {
                FitopiaUser user = fitopiaUserService.findByIdentifier(identifier).orElse(null);
                if (user == null) {
                    statusLabel.setText("Aucun compte ne correspond a cet email/username.");
                    return;
                }
                if (!user.isFaceIdEnabled()) {
                    statusLabel.setText("Face ID n'est pas active pour ce compte.");
                    return;
                }
                result = faceRecognitionService.compareWithUser(currentFrame, user).orElse(null);
                authenticated = faceRecognitionService.isStrictMatch(result) ? user : null;
            } else {
                result = faceRecognitionService.findBestMatchResult(currentFrame, fitopiaUserService.getAll()).orElse(null);
                authenticated = result == null ? null : result.user();
            }

            if (authenticated == null) {
                int similarity = result == null ? 0 : (int) Math.round(result.confidence() * 100);
                statusLabel.setText("Verification terminee. Similarite detectee: " + similarity + "%. Face ID incompatible.");
                showFaceIdMismatchAlert(similarity);
                return;
            }

            UserSession.setCurrentUser(authenticated);
            statusLabel.setText("Connexion Face ID reussie pour " + authenticated.getUsername()
                    + " (score " + Math.round(result.confidence() * 100) + "%).");
            openHomeFor(authenticated);
        } catch (RuntimeException e) {
            statusLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleRegisterFace() {
        String identifier = safe(identifierField.getText()).trim();
        String password = safe(passwordField.getText()).trim();

        if (identifier.isBlank() || password.isBlank()) {
            statusLabel.setText("Saisissez email/username et mot de passe pour enregistrer le visage.");
            return;
        }
        if (!fitopiaUserService.isAvailable()) {
            statusLabel.setText("Connexion MySQL indisponible. Verifiez fitopiabd.");
            return;
        }
        if (currentFrame == null) {
            statusLabel.setText("Aucune image disponible. Activez la camera ou choisissez une photo.");
            return;
        }

        try {
            AuthenticationResult authResult = fitopiaUserService.authenticateSecure(identifier, password);
            if (authResult.status() != AuthenticationResult.Status.SUCCESS) {
                statusLabel.setText(authResult.message());
                return;
            }
            FitopiaUser authenticated = authResult.user();

            Path output = Paths.get("faces", "users", "user_" + authenticated.getId() + "_face.png").toAbsolutePath();
            faceRecognitionService.saveFaceImage(currentFrame, output);
            fitopiaUserService.enableFaceId(authenticated.getId(), output.toString());
            authenticated.setFaceIdEnabled(true);
            authenticated.setFaceImagePath(output.toString());
            UserSession.setCurrentUser(authenticated);
            returnToSignInAfterRegistration(authenticated);
        } catch (IOException e) {
            statusLabel.setText("Erreur de sauvegarde: " + e.getMessage());
        } catch (RuntimeException e) {
            statusLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleRetry() {
        stopCamera();
        PauseTransition pause = new PauseTransition(Duration.millis(250));
        pause.setOnFinished(event -> {
            loadAvailableCameras();
            activateSelectedCamera();
        });
        pause.play();
    }

    @FXML
    private void handleLoadImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une photo du visage");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );

        Stage stage = (Stage) statusLabel.getScene().getWindow();
        File selectedFile = chooser.showOpenDialog(stage);
        if (selectedFile == null) {
            return;
        }

        try {
            BufferedImage image = ImageIO.read(selectedFile);
            if (image == null) {
                statusLabel.setText("Le fichier choisi n'est pas une image valide.");
                return;
            }

            stopCamera();
            currentFrame = image;
            cameraPreview.setImage(SwingFXUtils.toFXImage(image, null));
            previewPlaceholderLabel.setVisible(false);
            previewPlaceholderLabel.setManaged(false);
            cameraDetailsLabel.setText("Photo chargee: " + selectedFile.getAbsolutePath());
            statusLabel.setText("Photo chargee. Vous pouvez vous connecter ou enregistrer le visage.");
        } catch (IOException e) {
            statusLabel.setText("Impossible de lire l'image: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        stopCamera();
        UserSession.setVerifyFaceIdOnly(false);
        SceneNavigator.goTo(statusLabel, "/SignIn.fxml", "Sign In", 1460, 860);
    }

    private void loadAvailableCameras() {
        stopCamera();
        currentFrame = null;
        previewPlaceholderLabel.setVisible(true);
        previewPlaceholderLabel.setManaged(true);
        previewPlaceholderLabel.setText("Chargement des cameras...");

        try {
            Webcam.resetDriver();
            List<Webcam> webcams = Webcam.getWebcams(5, TimeUnit.SECONDS);
            detectedWebcams.clear();
            if (webcams != null) {
                detectedWebcams.addAll(webcams);
            }

            List<String> cameraNames = detectedWebcams.stream()
                    .filter(cam -> cam != null && cam.getName() != null && !cam.getName().isBlank())
                    .map(Webcam::getName)
                    .sorted(Comparator.naturalOrder())
                    .toList();

            cameraSelector.getItems().setAll(cameraNames);
            if (cameraNames.isEmpty()) {
                selectedCameraName = null;
                cameraDetailsLabel.setText("Aucune webcam detectee par Java.");
                statusLabel.setText("Aucune camera disponible.");
                previewPlaceholderLabel.setText("Aucune camera detectee");
                return;
            }

            if (selectedCameraName == null || !cameraNames.contains(selectedCameraName)) {
                selectedCameraName = cameraNames.get(0);
            }
            cameraSelector.setValue(selectedCameraName);
            cameraDetailsLabel.setText("Webcams detectees: " + String.join(" | ", cameraNames));
            statusLabel.setText("Camera detectee. Cliquez sur Activer camera.");
            previewPlaceholderLabel.setText("Selectionnez une camera puis cliquez sur Activer camera");
        } catch (Exception e) {
            cameraDetailsLabel.setText("Erreur technique camera: " + sanitizeCameraError(e));
            statusLabel.setText("Impossible de charger la liste des cameras.");
            previewPlaceholderLabel.setText("Erreur chargement camera");
        }
    }

    private void activateSelectedCamera() {
        stopCamera();
        currentFrame = null;
        selectedCameraName = cameraSelector.getValue();
        if (selectedCameraName == null || selectedCameraName.isBlank()) {
            statusLabel.setText("Choisissez d'abord une camera.");
            return;
        }

        try {
            webcam = detectedWebcams.stream()
                    .filter(cam -> cam != null && selectedCameraName.equals(cam.getName()))
                    .findFirst()
                    .orElse(null);

            if (webcam == null) {
                statusLabel.setText("Camera selectionnee introuvable. Rechargez la liste.");
                return;
            }

            Dimension selectedSize = chooseViewSize(webcam.getViewSizes());
            if (selectedSize != null) {
                webcam.setViewSize(selectedSize);
            }

            webcam.open();
            emptyFrameCount = 0;
            cameraDetailsLabel.setText("Camera active: " + webcam.getName()
                    + " | Resolution: " + webcam.getViewSize().width + "x" + webcam.getViewSize().height);
            statusLabel.setText("Camera ouverte. Attente de l'image...");
            previewPlaceholderLabel.setText("Connexion a la camera...");
            previewPlaceholderLabel.setVisible(true);
            previewPlaceholderLabel.setManaged(true);

            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(() -> {
                try {
                    if (webcam == null || !webcam.isOpen()) {
                        return;
                    }

                    BufferedImage image = webcam.getImage();
                    if (image != null) {
                        currentFrame = image;
                        emptyFrameCount = 0;
                        Platform.runLater(() -> {
                            cameraPreview.setImage(SwingFXUtils.toFXImage(image, null));
                            previewPlaceholderLabel.setVisible(false);
                            previewPlaceholderLabel.setManaged(false);
                            statusLabel.setText("Camera active. Pret pour capture.");
                        });
                    } else {
                        emptyFrameCount++;
                        if (emptyFrameCount >= 15) {
                            Platform.runLater(() -> {
                                previewPlaceholderLabel.setVisible(true);
                                previewPlaceholderLabel.setManaged(true);
                                previewPlaceholderLabel.setText("La camera est ouverte mais aucune image n'arrive");
                                statusLabel.setText("La camera ne renvoie pas d'image. Fermez Camera/Teams/Zoom ou utilisez Choisir une photo.");
                            });
                        }
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        previewPlaceholderLabel.setVisible(true);
                        previewPlaceholderLabel.setManaged(true);
                        previewPlaceholderLabel.setText("Erreur camera");
                        statusLabel.setText("Erreur camera: " + sanitizeCameraError(e));
                        cameraDetailsLabel.setText("Diagnostic: " + webcam.getName() + " | " + sanitizeCameraError(e));
                    });
                }
            }, 0, 120, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            String message = sanitizeCameraError(e);
            cameraDetailsLabel.setText("Diagnostic: " + selectedCameraName + " | " + message);
            if (message.toLowerCase().contains("locked")) {
                statusLabel.setText("Cette camera est deja verrouillee par une autre application.");
            } else {
                statusLabel.setText("Impossible d'ouvrir la camera selectionnee.");
            }
            previewPlaceholderLabel.setText("Camera indisponible. Utilisez Retry Camera ou Choisir une photo.");
            previewPlaceholderLabel.setVisible(true);
            previewPlaceholderLabel.setManaged(true);
        }
    }

    private Dimension chooseViewSize(Dimension[] sizes) {
        if (sizes == null || sizes.length == 0) {
            return new Dimension(640, 480);
        }

        Dimension best = null;
        for (Dimension size : sizes) {
            if (size.width <= 1280 && size.height <= 720) {
                if (best == null || (size.width * size.height) > (best.width * best.height)) {
                    best = size;
                }
            }
        }
        return best == null ? sizes[0] : best;
    }

    private void stopCamera() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        if (webcam != null) {
            try {
                if (webcam.isOpen()) {
                    webcam.close();
                }
            } catch (Exception ignored) {
            }
            webcam = null;
        }
        currentFrame = null;
        emptyFrameCount = 0;
        Platform.runLater(() -> {
            cameraPreview.setImage(null);
            previewPlaceholderLabel.setVisible(true);
            previewPlaceholderLabel.setManaged(true);
            previewPlaceholderLabel.setText("Camera eteinte. Cliquez sur Activer camera.");
        });
    }

    private void openHomeFor(FitopiaUser user) {
        stopCamera();
        UserSession.setVerifyFaceIdOnly(false);
        SessionRouter.openRoleHome(statusLabel);
    }

    private void returnToSignInAfterRegistration(FitopiaUser user) {
        stopCamera();
        UserSession.setCurrentUser(user);
        UserSession.setVerifyFaceIdOnly(false);
        SceneNavigator.goTo(statusLabel, "/SignIn.fxml", "Sign In", 1460, 860);
    }

    private String sanitizeCameraError(Exception e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            return e.getClass().getSimpleName();
        }
        return message.replace('\n', ' ').replace('\r', ' ').trim();
    }

    private void bindStageCloseHandler() {
        if (statusLabel.getScene() == null || statusLabel.getScene().getWindow() == null) {
            return;
        }
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.setOnCloseRequest(event -> stopCamera());
        stage.iconifiedProperty().addListener((obs, oldValue, minimized) -> {
            if (Boolean.TRUE.equals(minimized)) {
                stopCamera();
            }
        });
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showFaceIdMismatchAlert(int similarity) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Face ID incorrect");
        alert.setHeaderText("Authentification refusee");
        alert.setContentText("Verification terminee. Similarite detectee: " + similarity + "%.\nCe Face ID n'est pas compatible avec le visage enregistre.");
        alert.showAndWait();
    }
}

