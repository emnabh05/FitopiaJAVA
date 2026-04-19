package tn.esprit.Pidev3A49.test;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.services.ServiceUser;
import tn.esprit.Pidev3A49.services.UserAiInsightService;
import tn.esprit.Pidev3A49.services.security.PasswordPolicyReport;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class UserCrudController {

    @FXML private Label statusLabel;
    @FXML private Label selectedRoleBadge;
    @FXML private HBox patientRoleCard;
    @FXML private HBox coachRoleCard;
    @FXML private HBox nutritionistRoleCard;
    @FXML private HBox adminRoleCard;
    @FXML private VBox patientSection;
    @FXML private VBox professionalSection;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField phoneField;
    @FXML private DatePicker birthDatePicker;
    @FXML private RadioButton maleRadio;
    @FXML private RadioButton femaleRadio;
    @FXML private TextField avatarField;
    @FXML private TextField professionalTitleField;
    @FXML private TextField specializationField;
    @FXML private TextField qualificationField;
    @FXML private TextField yearsExperienceField;
    @FXML private TextArea bioArea;
    @FXML private TextField licenseNumberField;
    @FXML private TextField heightField;
    @FXML private TextField weightField;
    @FXML private TextField targetWeightField;
    @FXML private ComboBox<String> fitnessLevelCombo;
    @FXML private TextArea healthConditionsArea;
    @FXML private TextArea dietaryPreferencesArea;
    @FXML private TextArea fitnessGoalsArea;
    @FXML private Label aiImprovementsLabel;
    @FXML private Label aiUsernameLabel;
    @FXML private Label aiPhoneLabel;
    @FXML private TextArea aiBioArea;
    @FXML private TextArea aiSummaryArea;
    @FXML private TableView<FitopiaUser> userTable;
    @FXML private TableColumn<FitopiaUser, Number> idColumn;
    @FXML private TableColumn<FitopiaUser, String> fullNameColumn;
    @FXML private TableColumn<FitopiaUser, String> usernameColumn;
    @FXML private TableColumn<FitopiaUser, String> emailColumn;
    @FXML private TableColumn<FitopiaUser, String> roleColumn;
    @FXML private TableColumn<FitopiaUser, String> phoneColumn;

    private final ServiceUser serviceUser = new ServiceUser();
    private final UserAiInsightService userAiInsightService = new UserAiInsightService();
    private final ObservableList<FitopiaUser> users = FXCollections.observableArrayList();
    private String selectedRole = "Patient";
    private FitopiaUser selectedUser;
    private UserAiInsightService.ProfileCompletionSuggestion latestSuggestion;

    @FXML
    public void initialize() {
        fitnessLevelCombo.setItems(FXCollections.observableArrayList("None", "Beginner", "Intermediate", "Advanced"));
        fitnessLevelCombo.getSelectionModel().select("Beginner");
        birthDatePicker.setValue(LocalDate.now().minusYears(20));
        configureTable();
        applyRole("Patient");
        resetAiSuggestionPane();
        loadUsers();
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> populateForm(newValue));
    }

    @FXML private void selectPatientRole() { applyRole("Patient"); }
    @FXML private void selectCoachRole() { applyRole("Coach"); }
    @FXML private void selectNutritionistRole() { applyRole("Nutritionist"); }
    @FXML private void selectAdminRole() { applyRole("Admin"); }

    @FXML
    private void handleBrowseAvatar() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select avatar");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            avatarField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleAddUser() {
        try {
            FitopiaUser user = buildUserFromForm();
            PasswordPolicyReport report = serviceUser.registerUser(user, passwordField.getText().trim());
            setStatus("Utilisateur ajoute avec succes. Score mot de passe: " + report.score() + "/100.");
            clearForm();
            loadUsers();
        } catch (RuntimeException e) {
            setStatus(e.getMessage());
        }
    }

    @FXML
    private void handleUpdateUser() {
        if (selectedUser == null) {
            setStatus("Selectionnez un utilisateur a modifier.");
            return;
        }
        try {
            FitopiaUser updated = buildUserFromForm();
            updated.setId(selectedUser.getId());
            serviceUser.updateProfile(updated);
            String newPassword = passwordField.getText().trim();
            if (!newPassword.isBlank()) {
                PasswordPolicyReport report = serviceUser.changePassword(updated.getId(), newPassword, updated);
                setStatus("Utilisateur modifie. Nouveau mot de passe: " + report.score() + "/100.");
            } else {
                setStatus("Utilisateur modifie avec succes.");
            }
            clearForm();
            loadUsers();
        } catch (RuntimeException e) {
            setStatus(e.getMessage());
        }
    }

    @FXML
    private void handleDeleteUser() {
        if (selectedUser == null) {
            setStatus("Selectionnez un utilisateur a supprimer.");
            return;
        }
        try {
            serviceUser.delete(selectedUser.getId());
            setStatus("Utilisateur supprime avec succes.");
            clearForm();
            loadUsers();
        } catch (RuntimeException e) {
            setStatus(e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
        setStatus("Liste actualisee.");
    }

    @FXML
    private void handleClear() {
        clearForm();
        setStatus("Formulaire reinitialise.");
    }

    @FXML
    private void handleGetStarted() {
        clearForm();
        setStatus("Pret pour creer un nouvel utilisateur.");
        firstNameField.requestFocus();
    }

    @FXML
    private void handleGenerateAiSuggestions() {
        try {
            FitopiaUser draft = buildUserFromFormDraft();
            latestSuggestion = userAiInsightService.buildProfileCompletionSuggestion(draft);
            aiUsernameLabel.setText(valueOrEmpty(latestSuggestion.suggestedUsername()));
            aiPhoneLabel.setText(valueOrEmpty(latestSuggestion.normalizedPhone()));
            aiBioArea.setText(valueOrEmpty(latestSuggestion.improvedBio()));
            aiSummaryArea.setText(valueOrEmpty(latestSuggestion.profileSummary()));
            aiImprovementsLabel.setText(String.join(" | ", latestSuggestion.improvements()));
            setStatus("Suggestions IA generees pour le profil.");
        } catch (RuntimeException e) {
            setStatus(e.getMessage());
        }
    }

    @FXML
    private void handleApplyAiSuggestions() {
        if (latestSuggestion == null) {
            setStatus("Generez d'abord les suggestions IA.");
            return;
        }
        firstNameField.setText(valueOrEmpty(latestSuggestion.suggestedFirstName()));
        lastNameField.setText(valueOrEmpty(latestSuggestion.suggestedLastName()));
        if (!valueOrEmpty(latestSuggestion.suggestedUsername()).isBlank()) {
            usernameField.setText(latestSuggestion.suggestedUsername());
        }
        if (!valueOrEmpty(latestSuggestion.normalizedPhone()).isBlank()) {
            phoneField.setText(latestSuggestion.normalizedPhone());
        }
        if (!valueOrEmpty(latestSuggestion.improvedBio()).isBlank()) {
            bioArea.setText(latestSuggestion.improvedBio());
        }
        aiSummaryArea.setText(valueOrEmpty(latestSuggestion.profileSummary()));
        setStatus("Suggestions IA appliquees au formulaire.");
    }

    @FXML
    private void handleHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/User.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 640);
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setTitle("Fitopia Login");
            stage.setScene(scene);
            stage.setMinWidth(1100);
            stage.setMinHeight(620);
            stage.setWidth(1280);
            stage.setHeight(640);
            stage.centerOnScreen();
        } catch (IOException e) {
            setStatus("Impossible de revenir a la page d'entree: " + e.getMessage());
        }
    }

    private void configureTable() {
        idColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        fullNameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getFirstName() + " " + data.getValue().getLastName()));
        usernameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getUsername()));
        emailColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getEmail()));
        roleColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getRole()));
        phoneColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrEmpty(data.getValue().getPhone())));
        userTable.setItems(users);
    }

    private void loadUsers() {
        if (!serviceUser.isAvailable()) {
            users.clear();
            setStatus("Connexion MySQL indisponible. Verifiez fitopiabd.");
            return;
        }
        users.setAll(serviceUser.getAll());
        setStatus(users.size() + " utilisateur(s) charges depuis fitopiabd.");
    }

    private void populateForm(FitopiaUser user) {
        selectedUser = user;
        if (user == null) {
            return;
        }
        firstNameField.setText(valueOrEmpty(user.getFirstName()));
        lastNameField.setText(valueOrEmpty(user.getLastName()));
        usernameField.setText(valueOrEmpty(user.getUsername()));
        emailField.setText(valueOrEmpty(user.getEmail()));
        passwordField.clear();
        phoneField.setText(valueOrEmpty(user.getPhone()));
        birthDatePicker.setValue(parseDate(user.getBirthDate()));
        maleRadio.setSelected(!"Female".equalsIgnoreCase(user.getGender()));
        femaleRadio.setSelected("Female".equalsIgnoreCase(user.getGender()));
        avatarField.setText(valueOrEmpty(user.getAvatarPath()));
        professionalTitleField.setText(valueOrEmpty(user.getProfessionalTitle()));
        specializationField.setText(valueOrEmpty(user.getSpecialization()));
        qualificationField.setText(valueOrEmpty(user.getQualification()));
        yearsExperienceField.setText(valueOrEmpty(user.getYearsExperience()));
        bioArea.setText(valueOrEmpty(user.getBio()));
        licenseNumberField.setText(valueOrEmpty(user.getLicenseNumber()));
        heightField.setText(valueOrEmpty(user.getHeight()));
        weightField.setText(valueOrEmpty(user.getWeight()));
        targetWeightField.setText(valueOrEmpty(user.getTargetWeight()));
        if (user.getFitnessLevel() != null && !user.getFitnessLevel().isBlank()) {
            fitnessLevelCombo.getSelectionModel().select(user.getFitnessLevel());
        }
        healthConditionsArea.setText(valueOrEmpty(user.getHealthConditions()));
        dietaryPreferencesArea.setText(valueOrEmpty(user.getDietaryPreferences()));
        fitnessGoalsArea.setText(valueOrEmpty(user.getFitnessGoals()));
        applyRole(valueOrEmpty(user.getRole()).isBlank() ? "Patient" : user.getRole());
        latestSuggestion = null;
        resetAiSuggestionPane();
        setStatus("Edition utilisateur #" + user.getId());
    }

    private FitopiaUser buildUserFromForm() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || email.isEmpty()) {
            throw new RuntimeException("First name, last name, username et email sont obligatoires.");
        }
        if (selectedUser == null && password.isEmpty()) {
            throw new RuntimeException("Le mot de passe est obligatoire a la creation.");
        }

        FitopiaUser user = new FitopiaUser();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(selectedUser == null ? password : "");
        user.setPhone(phoneField.getText().trim());
        user.setBirthDate(birthDatePicker.getValue() == null ? "" : birthDatePicker.getValue().toString());
        user.setGender(femaleRadio.isSelected() ? "Female" : "Male");
        user.setRole(selectedRole);
        user.setAvatarPath(avatarField.getText().trim());
        user.setProfessionalTitle(professionalTitleField.getText().trim());
        user.setSpecialization(specializationField.getText().trim());
        user.setQualification(qualificationField.getText().trim());
        user.setYearsExperience(yearsExperienceField.getText().trim());
        user.setBio(bioArea.getText().trim());
        user.setLicenseNumber(licenseNumberField.getText().trim());
        user.setHeight(heightField.getText().trim());
        user.setWeight(weightField.getText().trim());
        user.setTargetWeight(targetWeightField.getText().trim());
        user.setFitnessLevel(fitnessLevelCombo.getValue());
        user.setHealthConditions(healthConditionsArea.getText().trim());
        user.setDietaryPreferences(dietaryPreferencesArea.getText().trim());
        user.setFitnessGoals(fitnessGoalsArea.getText().trim());
        if (selectedUser != null) {
            user.setFaceIdEnabled(selectedUser.isFaceIdEnabled());
            user.setFaceImagePath(selectedUser.getFaceImagePath());
        }
        return user;
    }

    private FitopiaUser buildUserFromFormDraft() {
        FitopiaUser user = new FitopiaUser();
        user.setFirstName(valueOrEmpty(firstNameField.getText()).trim());
        user.setLastName(valueOrEmpty(lastNameField.getText()).trim());
        user.setUsername(valueOrEmpty(usernameField.getText()).trim());
        user.setEmail(valueOrEmpty(emailField.getText()).trim());
        user.setPhone(valueOrEmpty(phoneField.getText()).trim());
        user.setBirthDate(birthDatePicker.getValue() == null ? "" : birthDatePicker.getValue().toString());
        user.setGender(femaleRadio.isSelected() ? "Female" : "Male");
        user.setRole(selectedRole);
        user.setAvatarPath(valueOrEmpty(avatarField.getText()).trim());
        user.setProfessionalTitle(valueOrEmpty(professionalTitleField.getText()).trim());
        user.setSpecialization(valueOrEmpty(specializationField.getText()).trim());
        user.setQualification(valueOrEmpty(qualificationField.getText()).trim());
        user.setYearsExperience(valueOrEmpty(yearsExperienceField.getText()).trim());
        user.setBio(valueOrEmpty(bioArea.getText()).trim());
        user.setLicenseNumber(valueOrEmpty(licenseNumberField.getText()).trim());
        user.setHeight(valueOrEmpty(heightField.getText()).trim());
        user.setWeight(valueOrEmpty(weightField.getText()).trim());
        user.setTargetWeight(valueOrEmpty(targetWeightField.getText()).trim());
        user.setFitnessLevel(fitnessLevelCombo.getValue());
        user.setHealthConditions(valueOrEmpty(healthConditionsArea.getText()).trim());
        user.setDietaryPreferences(valueOrEmpty(dietaryPreferencesArea.getText()).trim());
        user.setFitnessGoals(valueOrEmpty(fitnessGoalsArea.getText()).trim());
        return user;
    }

    private void clearForm() {
        selectedUser = null;
        userTable.getSelectionModel().clearSelection();
        firstNameField.clear();
        lastNameField.clear();
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        phoneField.clear();
        birthDatePicker.setValue(LocalDate.now().minusYears(20));
        maleRadio.setSelected(true);
        femaleRadio.setSelected(false);
        avatarField.clear();
        professionalTitleField.clear();
        specializationField.clear();
        qualificationField.clear();
        yearsExperienceField.clear();
        bioArea.clear();
        licenseNumberField.clear();
        heightField.clear();
        weightField.clear();
        targetWeightField.clear();
        fitnessLevelCombo.getSelectionModel().select("Beginner");
        healthConditionsArea.clear();
        dietaryPreferencesArea.clear();
        fitnessGoalsArea.clear();
        latestSuggestion = null;
        resetAiSuggestionPane();
        applyRole("Patient");
    }

    private void resetAiSuggestionPane() {
        aiImprovementsLabel.setText("Les recommandations IA s'afficheront ici.");
        aiUsernameLabel.setText("-");
        aiPhoneLabel.setText("-");
        aiBioArea.clear();
        aiSummaryArea.clear();
    }

    private void applyRole(String role) {
        selectedRole = role;
        selectedRoleBadge.setText(role.toUpperCase());
        boolean patient = "Patient".equals(role);
        patientSection.setManaged(patient);
        patientSection.setVisible(patient);
        professionalSection.setManaged(!patient);
        professionalSection.setVisible(!patient);
        updateRoleCardStyle(patientRoleCard, patient);
        updateRoleCardStyle(coachRoleCard, "Coach".equals(role));
        updateRoleCardStyle(nutritionistRoleCard, "Nutritionist".equals(role));
        updateRoleCardStyle(adminRoleCard, "Admin".equals(role));
    }

    private void updateRoleCardStyle(HBox card, boolean selected) {
        String base = "-fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 10 14 10 14; "
                + "-fx-border-color: rgba(255,255,255,0.14); -fx-border-width: 1;";
        card.setStyle(selected ? base + "-fx-background-color: white;" : base + "-fx-background-color: rgba(255,255,255,0.08);");
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private LocalDate parseDate(String value) {
        try {
            return value == null || value.isBlank() ? LocalDate.now().minusYears(20) : LocalDate.parse(value);
        } catch (Exception e) {
            return LocalDate.now().minusYears(20);
        }
    }
}
