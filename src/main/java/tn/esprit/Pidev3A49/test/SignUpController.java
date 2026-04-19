package tn.esprit.Pidev3A49.test;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.services.ServiceUser;

public class SignUpController {
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Label selectedRoleBadge;
    @FXML private HBox patientRoleCard;
    @FXML private HBox coachRoleCard;
    @FXML private HBox nutritionistRoleCard;
    @FXML private HBox adminRoleCard;

    private final ServiceUser serviceUser = new ServiceUser();
    private String selectedRole = "Patient";

    @FXML
    public void initialize() {
        applyRole("Patient");
        statusLabel.setText("Remplissez le formulaire pour creer un compte.");
    }

    @FXML private void selectPatientRole() { applyRole("Patient"); }
    @FXML private void selectCoachRole() { applyRole("Coach"); }
    @FXML private void selectNutritionistRole() { applyRole("Nutritionist"); }
    @FXML private void selectAdminRole() { applyRole("Admin"); }

    @FXML
    private void handleCreateAccount() {
        String nom = safe(nomField.getText());
        String prenom = safe(prenomField.getText());
        String username = safe(usernameField.getText());
        String email = safe(emailField.getText());
        String password = safe(passwordField.getText());
        String role = safe(selectedRole);

        if (nom.isBlank() || prenom.isBlank() || username.isBlank() || email.isBlank() || password.isBlank()) {
            statusLabel.setText("Nom, prenom, username, email et mot de passe sont obligatoires.");
            return;
        }
        if (!serviceUser.isAvailable()) {
            statusLabel.setText("Connexion MySQL indisponible. Verifiez fitopiabd.");
            return;
        }
        if (serviceUser.emailExists(email) || serviceUser.usernameExists(username)) {
            Alert duplicate = new Alert(Alert.AlertType.WARNING);
            duplicate.setTitle("Compte existant");
            duplicate.setHeaderText("Ce compte est deja cree");
            duplicate.setContentText("Connectez-vous depuis la page Sign In.");
            duplicate.showAndWait();
            SceneNavigator.goTo(statusLabel, "/SignIn.fxml", "Sign In", 1460, 860);
            return;
        }

        FitopiaUser user = new FitopiaUser();
        user.setFirstName(prenom);
        user.setLastName(nom);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role.isBlank() ? "Patient" : role);
        user.setPhone("");
        user.setBirthDate("");
        user.setGender("Male");
        user.setAvatarPath("");
        user.setProfessionalTitle("");
        user.setSpecialization("");
        user.setQualification("");
        user.setYearsExperience("");
        user.setBio("");
        user.setLicenseNumber("");
        user.setHeight("");
        user.setWeight("");
        user.setTargetWeight("");
        user.setFitnessLevel("");
        user.setHealthConditions("");
        user.setDietaryPreferences("");
        user.setFitnessGoals("");
        user.setFaceIdEnabled(false);
        user.setFaceImagePath("");

        try {
            serviceUser.add(user);
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Compte cree");
            success.setHeaderText("Creation reussie");
            success.setContentText("Votre compte est cree. Connectez-vous maintenant.");
            success.showAndWait();
            SceneNavigator.goTo(statusLabel, "/SignIn.fxml", "Sign In", 1460, 860);
        } catch (RuntimeException e) {
            statusLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleBackToSignIn() {
        SceneNavigator.goTo(statusLabel, "/SignIn.fxml", "Sign In", 1460, 860);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void applyRole(String role) {
        selectedRole = role;
        selectedRoleBadge.setText(role.toUpperCase());
        updateRoleCardStyle(patientRoleCard, "Patient".equals(role));
        updateRoleCardStyle(coachRoleCard, "Coach".equals(role));
        updateRoleCardStyle(nutritionistRoleCard, "Nutritionist".equals(role));
        updateRoleCardStyle(adminRoleCard, "Admin".equals(role));
    }

    private void updateRoleCardStyle(HBox card, boolean selected) {
        String base = "-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 8 10 8 10; "
                + "-fx-border-color: rgba(255,255,255,0.18); -fx-border-width: 1;";
        card.setStyle(selected ? base + "-fx-background-color: white;" : base + "-fx-background-color: rgba(255,255,255,0.08);");
    }
}
