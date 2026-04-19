package tn.esprit.Pidev3A49.test;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.services.ServiceUser;

import java.io.File;
import java.util.Optional;

public class SignInController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Button faceIdButton;
    @FXML private Button verifyFaceIdButton;
    @FXML private VBox faceIdSummaryBox;
    @FXML private ImageView facePreview;
    @FXML private Label faceIdSummaryLabel;

    private final ServiceUser serviceUser = new ServiceUser();

    @FXML
    public void initialize() {
        FitopiaUser currentUser = UserSession.getCurrentUser();
        if (currentUser != null && currentUser.isFaceIdEnabled()) {
            String identifier = currentUser.getEmail() != null && !currentUser.getEmail().isBlank()
                    ? currentUser.getEmail()
                    : currentUser.getUsername();
            emailField.setText(identifier);
            showFaceIdSummary(currentUser, identifier);
            statusLabel.setText("Face ID deja enregistre pour ce compte. Verifiez l'email et l'apercu, puis cliquez sur VERIFIER MON FACE ID.");
            return;
        }

        faceIdSummaryBox.setVisible(false);
        faceIdSummaryBox.setManaged(false);
        statusLabel.setText("Choisissez votre mode de connexion: email + mot de passe ou Face ID.");
    }

    @FXML
    private void handleSignIn() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (email.isBlank() || password.isBlank()) {
            statusLabel.setText("Email et mot de passe sont obligatoires.");
            return;
        }
        if (!serviceUser.isAvailable()) {
            statusLabel.setText("Connexion MySQL indisponible. Verifiez fitopiabd.");
            return;
        }

        Optional<FitopiaUser> found = serviceUser.authenticate(email, password);
        if (found.isEmpty()) {
            statusLabel.setText("Compte introuvable. Verifiez vos informations.");
            return;
        }

        FitopiaUser connected = found.get();
        UserSession.setCurrentUser(connected);
        openHomeFor(connected);
    }

    @FXML
    private void handleFaceId() {
        UserSession.setVerifyFaceIdOnly(false);
        SceneNavigator.goTo(statusLabel, "/FaceId.fxml", "Face ID", 980, 780);
    }

    @FXML
    private void handleVerifyRegisteredFaceId() {
        FitopiaUser currentUser = UserSession.getCurrentUser();
        if (currentUser == null || !currentUser.isFaceIdEnabled()) {
            statusLabel.setText("Aucun Face ID enregistre a verifier.");
            return;
        }

        UserSession.setVerifyFaceIdOnly(true);
        SceneNavigator.goTo(statusLabel, "/FaceId.fxml", "Verification Face ID", 980, 780);
    }

    @FXML
    private void handleGoSignUp() {
        SceneNavigator.goTo(statusLabel, "/SignUp.fxml", "Creer un compte", 1460, 860);
    }

    @FXML
    private void handleForgot() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("Recuperation de mot de passe");
        alert.setContentText("Fonction a implementer.");
        alert.showAndWait();
    }

    private void openHomeFor(FitopiaUser connected) {
        boolean isAdmin = "admin".equalsIgnoreCase(connected.getRole());
        UserSession.setVerifyFaceIdOnly(false);
        if (isAdmin) {
            SceneNavigator.goTo(statusLabel, "/AdminDashboard.fxml", "Back Office Admin", 1350, 820);
        } else {
            SceneNavigator.goTo(statusLabel, "/FrontHome.fxml", "Front Office", 1460, 760);
        }
    }

    private void showFaceIdSummary(FitopiaUser user, String identifier) {
        faceIdSummaryBox.setVisible(true);
        faceIdSummaryBox.setManaged(true);
        faceIdSummaryLabel.setText("Compte detecte: " + identifier + ". Verifiez que l'apercu correspond bien a votre visage.");

        String faceImagePath = user.getFaceImagePath();
        if (faceImagePath == null || faceImagePath.isBlank()) {
            facePreview.setImage(null);
            faceIdSummaryLabel.setText("Compte detecte: " + identifier + ". Aucun apercu du visage n'est disponible.");
            return;
        }

        File faceFile = new File(faceImagePath);
        if (!faceFile.exists()) {
            facePreview.setImage(null);
            faceIdSummaryLabel.setText("Compte detecte: " + identifier + ". L'image Face ID enregistree est introuvable.");
            return;
        }

        facePreview.setImage(new Image(faceFile.toURI().toString(), true));
    }
}
