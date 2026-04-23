package tn.esprit.Pidev3A49.test;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import tn.esprit.Pidev3A49.api.AuthController;
import tn.esprit.Pidev3A49.api.dto.RegisterRequest;
import tn.esprit.Pidev3A49.api.dto.RegisterResponse;
import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.services.UserAiInsightService;

public class SignUpController {
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private DatePicker birthDatePicker;
    @FXML private PasswordField passwordField;
    @FXML private TextArea bioArea;
    @FXML private Label aiImprovementsLabel;
    @FXML private Label aiUsernameLabel;
    @FXML private Label aiPhoneLabel;
    @FXML private TextArea aiBioArea;
    @FXML private TextArea aiSummaryArea;
    @FXML private Label passwordGuidanceLabel;
    @FXML private Label statusLabel;
    @FXML private Label selectedRoleBadge;
    @FXML private HBox patientRoleCard;
    @FXML private HBox coachRoleCard;
    @FXML private HBox nutritionistRoleCard;
    @FXML private HBox adminRoleCard;

    private final AuthController authController = new AuthController();
    private final UserAiInsightService userAiInsightService = new UserAiInsightService();
    private String selectedRole = "Patient";
    private UserAiInsightService.ProfileCompletionSuggestion latestSuggestion;

    @FXML
    public void initialize() {
        applyRole("Patient");
        statusLabel.setText("Remplissez le formulaire pour creer un compte.");
        passwordGuidanceLabel.setText("Le mot de passe doit etre long, unique et ne pas contenir vos informations personnelles.");
        resetAiSuggestionPane();
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
        String phone = safe(phoneField.getText());
        String birthDate = birthDatePicker.getValue() == null ? "" : birthDatePicker.getValue().toString();
        String password = safe(passwordField.getText());
        String bio = safe(bioArea.getText());
        String role = safe(selectedRole);

        if (nom.isBlank() || prenom.isBlank() || username.isBlank() || email.isBlank() || password.isBlank()) {
            statusLabel.setText("Nom, prenom, username, email et mot de passe sont obligatoires.");
            return;
        }

        try {
            RegisterResponse response = authController.register(new RegisterRequest(
                    prenom,
                    nom,
                    username,
                    email,
                    password,
                    birthDate,
                    role,
                    phone,
                    "Male",
                    bio
            ));
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Compte cree");
            success.setHeaderText("Creation reussie");
            success.setContentText("Votre compte est cree. Score mot de passe: " + response.passwordScore()
                    + "/100 (" + response.passwordStrength() + "). Statut: " + response.accountStatus() + ".");
            success.showAndWait();
            SceneNavigator.goTo(statusLabel, "/SignIn.fxml", "Sign In", 1460, 860);
        } catch (RuntimeException e) {
            statusLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleGenerateAiSuggestions() {
        FitopiaUser draft = buildDraftUser();
        latestSuggestion = userAiInsightService.buildProfileCompletionSuggestion(draft);
        aiImprovementsLabel.setText(String.join(" | ", latestSuggestion.improvements()));
        aiUsernameLabel.setText(safe(latestSuggestion.suggestedUsername()).isBlank() ? "-" : latestSuggestion.suggestedUsername());
        aiPhoneLabel.setText(safe(latestSuggestion.normalizedPhone()).isBlank() ? "-" : latestSuggestion.normalizedPhone());
        aiBioArea.setText(safe(latestSuggestion.improvedBio()));
        aiSummaryArea.setText(safe(latestSuggestion.profileSummary()));
        statusLabel.setText("Suggestions IA generees pour votre inscription.");
    }

    @FXML
    private void handleApplyAiSuggestions() {
        if (latestSuggestion == null) {
            statusLabel.setText("Cliquez d'abord sur Generer les suggestions IA.");
            return;
        }
        prenomField.setText(safe(latestSuggestion.suggestedFirstName()));
        nomField.setText(safe(latestSuggestion.suggestedLastName()));
        if (!safe(latestSuggestion.suggestedUsername()).isBlank()) {
            usernameField.setText(latestSuggestion.suggestedUsername());
        }
        if (!safe(latestSuggestion.normalizedPhone()).isBlank()) {
            phoneField.setText(latestSuggestion.normalizedPhone());
        }
        if (!safe(latestSuggestion.improvedBio()).isBlank()) {
            bioArea.setText(latestSuggestion.improvedBio());
        }
        statusLabel.setText("Suggestions IA appliquees au formulaire.");
    }

    @FXML
    private void handleBackToSignIn() {
        SceneNavigator.goTo(statusLabel, "/SignIn.fxml", "Sign In", 1460, 860);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private FitopiaUser buildDraftUser() {
        FitopiaUser user = new FitopiaUser();
        user.setFirstName(safe(prenomField.getText()));
        user.setLastName(safe(nomField.getText()));
        user.setUsername(safe(usernameField.getText()));
        user.setEmail(safe(emailField.getText()));
        user.setPhone(safe(phoneField.getText()));
        user.setBirthDate(birthDatePicker.getValue() == null ? "" : birthDatePicker.getValue().toString());
        user.setBio(safe(bioArea.getText()));
        user.setRole(selectedRole);
        return user;
    }

    private void resetAiSuggestionPane() {
        latestSuggestion = null;
        aiImprovementsLabel.setText("Les suggestions IA apparaitront ici.");
        aiUsernameLabel.setText("-");
        aiPhoneLabel.setText("-");
        aiBioArea.clear();
        aiSummaryArea.clear();
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

