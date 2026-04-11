package tn.esprit.Pidev3A49.test;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.services.ServiceUser;

import java.io.IOException;
import java.util.List;

public class UserController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private CheckBox robotCheckBox;

    private final ServiceUser serviceUser = new ServiceUser();
    private boolean faceIdMode;

    @FXML
    public void initialize() {
        statusLabel.setText("Sign in to access user management.");
    }

    @FXML
    private void handleLoginMode() {
        faceIdMode = false;
        passwordField.setDisable(false);
        passwordField.setPromptText("Password");
        statusLabel.setText("Login + Password mode enabled.");
    }

    @FXML
    private void handleFaceIdMode() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FaceId.fxml"));
            Scene scene = new Scene(loader.load(), 980, 720);
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setTitle("Fitopia Face ID");
            stage.setScene(scene);
            stage.setMinWidth(900);
            stage.setMinHeight(680);
        } catch (IOException e) {
            statusLabel.setText("Impossible d'ouvrir Face ID: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        if (!ensureRobotVerified()) {
            return;
        }

        String identifier = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();
        if (identifier.isBlank() || password.isBlank()) {
            statusLabel.setText("Email/username et password sont obligatoires.");
            return;
        }

        if (!serviceUser.isAvailable()) {
            statusLabel.setText("Connexion MySQL indisponible. Ouverture directe de la page user.");
            openCrudPage();
            return;
        }

        List<FitopiaUser> users = serviceUser.getAll();
        if (users.isEmpty()) {
            statusLabel.setText("Aucun compte trouve. Ouverture de la page pour creer le premier utilisateur.");
            openCrudPage();
            return;
        }

        boolean matches = users.stream().anyMatch(user ->
                (identifier.equalsIgnoreCase(user.getEmail()) || identifier.equalsIgnoreCase(user.getUsername()))
                        && password.equals(user.getPassword()));

        if (!matches) {
            statusLabel.setText("Compte non trouve dans la base. Ouverture de la page user.");
            openCrudPage();
            return;
        }

        statusLabel.setText("Connexion reussie.");
        openCrudPage();
    }

    @FXML
    private void handleCreateAccount() {
        openCrudPage();
    }

    @FXML
    private void handleGetStarted() {
        openCrudPage();
    }

    private boolean ensureRobotVerified() {
        if (!robotCheckBox.isSelected()) {
            statusLabel.setText("Cochez d'abord I'm not a robot.");
            return false;
        }
        return true;
    }

    private void openCrudPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserCrud.fxml"));
            Scene scene = new Scene(loader.load(), 1450, 920);
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setTitle("Fitopia User CRUD");
            stage.setScene(scene);
            stage.setMinWidth(1200);
            stage.setMinHeight(820);
        } catch (IOException e) {
            statusLabel.setText("Impossible d'ouvrir la page CRUD: " + e.getMessage());
        }
    }
}
