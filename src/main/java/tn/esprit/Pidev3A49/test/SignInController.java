package tn.esprit.Pidev3A49.test;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.services.ServiceUser;

import java.util.Optional;

public class SignInController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private final ServiceUser serviceUser = new ServiceUser();

    @FXML
    public void initialize() {
        statusLabel.setText("Connectez-vous avec email + mot de passe.");
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
        boolean isAdmin = "admin".equalsIgnoreCase(connected.getRole());
        if (isAdmin) {
            SceneNavigator.goTo(statusLabel, "/AdminDashboard.fxml", "Back Office Admin", 1350, 820);
        } else {
            SceneNavigator.goTo(statusLabel, "/FrontHome.fxml", "Front Office", 1460, 760);
        }
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
}
