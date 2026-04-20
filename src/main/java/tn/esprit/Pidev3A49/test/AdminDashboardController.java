package tn.esprit.Pidev3A49.test;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import tn.esprit.Pidev3A49.api.AdminSecurityController;
import tn.esprit.Pidev3A49.api.UserArchiveController;
import tn.esprit.Pidev3A49.api.UserSecurityController;
import tn.esprit.Pidev3A49.api.dto.ChangePasswordRequest;
import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.services.ServiceUser;
import tn.esprit.Pidev3A49.services.UserAiInsightService;
import tn.esprit.Pidev3A49.services.security.AdminSecurityAlert;
import tn.esprit.Pidev3A49.services.security.UserSecuritySnapshot;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AdminDashboardController {
    @FXML private Label statusLabel;
    @FXML private Label adminLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private ComboBox<String> archiveFilterCombo;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField newPasswordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Button modifierButton;
    @FXML private Button enregistrerButton;
    @FXML private Button archiveViewButton;
    @FXML private TableView<FitopiaUser> usersTable;
    @FXML private TableColumn<FitopiaUser, Number> idCol;
    @FXML private TableColumn<FitopiaUser, String> nomCol;
    @FXML private TableColumn<FitopiaUser, String> prenomCol;
    @FXML private TableColumn<FitopiaUser, String> usernameCol;
    @FXML private TableColumn<FitopiaUser, String> emailCol;
    @FXML private TableColumn<FitopiaUser, String> roleCol;
    @FXML private TableColumn<FitopiaUser, String> phoneCol;
    @FXML private TableColumn<FitopiaUser, Number> riskCol;
    @FXML private TableColumn<FitopiaUser, String> accountStatusCol;
    @FXML private TableColumn<FitopiaUser, String> securityCol;
    @FXML private TableColumn<FitopiaUser, String> archivedAtCol;
    @FXML private Label securityDetailsLabel;
    @FXML private Label duplicateScoreLabel;
    @FXML private Label duplicateRecommendationLabel;
    @FXML private Label duplicateFindingsLabel;

    private final ServiceUser serviceUser = new ServiceUser();
    private final UserSecurityController userSecurityController = new UserSecurityController(serviceUser);
    private final AdminSecurityController adminSecurityController = new AdminSecurityController(serviceUser);
    private final UserAiInsightService userAiInsightService = new UserAiInsightService();
    private final UserArchiveController userArchiveController = new UserArchiveController(serviceUser, userAiInsightService);
    private final ObservableList<FitopiaUser> users = FXCollections.observableArrayList();
    private FitopiaUser selectedUser;
    private boolean editingMode;

    @FXML
    public void initialize() {
        FitopiaUser current = UserSession.getCurrentUser();
        adminLabel.setText(current == null ? "Admin" : current.getEmail());
        roleCombo.setItems(FXCollections.observableArrayList("Patient", "Coach", "Nutritionist", "Admin"));
        sortCombo.setItems(FXCollections.observableArrayList("ID desc", "ID asc", "Nom A-Z", "Role A-Z"));
        sortCombo.getSelectionModel().select("ID desc");
        archiveFilterCombo.setItems(FXCollections.observableArrayList("Actifs", "Archives", "Tous"));
        archiveFilterCombo.getSelectionModel().select("Actifs");
        archiveViewButton.setText("Voir archives");
        setupTable();
        loadUsers();
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> populateEditor(newV));
        setEditingMode(false);
        duplicateScoreLabel.setText("-");
        duplicateRecommendationLabel.setText("Selectionnez un utilisateur pour analyser les doublons.");
        duplicateFindingsLabel.setText("Aucune analyse en cours.");
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    @FXML
    private void handleLogout() {
        UserSession.clear();
        SceneNavigator.goTo(statusLabel, "/SignIn.fxml", "Sign In", 1460, 860);
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleSort() {
        applyFilters();
    }

    @FXML
    private void handleArchiveFilter() {
        applyFilters();
    }

    @FXML
    private void handleUpdateUser() {
        if (selectedUser == null) {
            statusLabel.setText("Selectionnez un utilisateur a modifier.");
            return;
        }
        setEditingMode(true);
        statusLabel.setText("Mode edition actif. Cliquez sur Enregistrer pour valider.");
    }

    @FXML
    private void handleSaveUser() {
        if (selectedUser == null) {
            statusLabel.setText("Selectionnez un utilisateur a modifier.");
            return;
        }
        if (!editingMode) {
            statusLabel.setText("Cliquez d'abord sur Modifier.");
            return;
        }
        String nom = safe(nomField.getText()).trim();
        String prenom = safe(prenomField.getText()).trim();
        String username = safe(usernameField.getText()).trim();
        String email = safe(emailField.getText()).trim();
        String phone = safe(phoneField.getText()).trim();
        String newPassword = safe(newPasswordField.getText()).trim();
        String role = safe(roleCombo.getValue()).trim();

        if (nom.isBlank() || prenom.isBlank() || username.isBlank() || email.isBlank() || role.isBlank()) {
            statusLabel.setText("Nom, prenom, username, email et role sont obligatoires.");
            return;
        }
        if (serviceUser.emailExistsForOtherUser(email, selectedUser.getId())) {
            statusLabel.setText("Cet email est deja utilise par un autre compte.");
            return;
        }
        if (serviceUser.usernameExistsForOtherUser(username, selectedUser.getId())) {
            statusLabel.setText("Ce username est deja utilise par un autre compte.");
            return;
        }

        selectedUser.setLastName(nom);
        selectedUser.setFirstName(prenom);
        selectedUser.setUsername(username);
        selectedUser.setEmail(email);
        selectedUser.setPhone(phone);
        selectedUser.setRole(role);
        try {
            serviceUser.updateProfile(selectedUser);
            if (!newPassword.isBlank()) {
                var response = userSecurityController.changePassword(selectedUser.getId(), new ChangePasswordRequest(
                        newPassword,
                        selectedUser.getFirstName(),
                        selectedUser.getLastName(),
                        selectedUser.getUsername(),
                        selectedUser.getEmail(),
                        selectedUser.getBirthDate()
                ));
                statusLabel.setText("Utilisateur modifie. Nouveau mot de passe: " + response.passwordScore() + "/100.");
            } else {
                statusLabel.setText("Utilisateur modifie avec succes.");
            }
            setEditingMode(false);
            loadUsers();
        } catch (RuntimeException e) {
            statusLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleDeleteUser() {
        if (selectedUser == null) {
            statusLabel.setText("Selectionnez un utilisateur a supprimer.");
            return;
        }
        try {
            int id = selectedUser.getId();
            UserAiInsightService.ArchiveSuggestion suggestion = userArchiveController.suggestArchive(id, 90);
            userArchiveController.archiveUser(id);
            selectedUser = null;
            setEditingMode(false);
            clearEditor();
            loadUsers();
            statusLabel.setText("Utilisateur #" + id + " archive via suppression logique. IA: "
                    + suggestion.score() + "/100.");
        } catch (RuntimeException e) {
            statusLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleRestoreUser() {
        if (selectedUser == null) {
            statusLabel.setText("Selectionnez un utilisateur a restaurer.");
            return;
        }
        try {
            int id = selectedUser.getId();
            userArchiveController.restoreUser(id);
            selectedUser = null;
            setEditingMode(false);
            clearEditor();
            loadUsers();
            statusLabel.setText("Utilisateur #" + id + " restaure.");
        } catch (RuntimeException e) {
            statusLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleShowArchivedUsers() {
        boolean showingArchives = Boolean.TRUE.equals(resolveArchiveFilter());
        archiveFilterCombo.setValue(showingArchives ? "Actifs" : "Archives");
        archiveViewButton.setText(showingArchives ? "Voir archives" : "Voir actifs");
        applyFilters();
    }

    @FXML
    private void handleClearEditor() {
        selectedUser = null;
        usersTable.getSelectionModel().clearSelection();
        setEditingMode(false);
        clearEditor();
        statusLabel.setText("Formulaire vide.");
    }

    private void setupTable() {
        idCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        nomCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(safe(c.getValue().getLastName())));
        prenomCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(safe(c.getValue().getFirstName())));
        usernameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(safe(c.getValue().getUsername())));
        emailCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(safe(c.getValue().getEmail())));
        roleCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(safe(c.getValue().getRole())));
        phoneCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(safe(c.getValue().getPhone())));
        riskCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getRiskScore()));
        accountStatusCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(safe(c.getValue().getAccountStatus())));
        securityCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(safe(c.getValue().getSecurityAlertSummary())));
        archivedAtCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(safe(c.getValue().getArchivedAt())));
        usersTable.setItems(users);
    }

    private void loadUsers() {
        if (!serviceUser.isAvailable()) {
            users.clear();
            statusLabel.setText("Connexion MySQL indisponible. Verifiez fitopiabd.");
            return;
        }
        applyFilters();
    }

    private void applyFilters() {
        List<FitopiaUser> list = userArchiveController.listUsers(resolveArchiveFilter());
        List<AdminSecurityAlert> alertEntries = adminSecurityController.listSecurityAlerts(null, null, false);
        String search = safe(searchField.getText()).trim().toLowerCase();
        if (!search.isEmpty()) {
            list = list.stream().filter(u ->
                    safe(u.getFirstName()).toLowerCase().contains(search)
                            || safe(u.getLastName()).toLowerCase().contains(search)
                            || safe(u.getUsername()).toLowerCase().contains(search)
                            || safe(u.getEmail()).toLowerCase().contains(search)
                            || safe(u.getRole()).toLowerCase().contains(search)
                            || String.valueOf(u.getId()).contains(search)
            ).collect(Collectors.toList());
        }

        String sort = sortCombo.getValue();
        if ("ID asc".equals(sort)) {
            list.sort(Comparator.comparingInt(FitopiaUser::getId));
        } else if ("Nom A-Z".equals(sort)) {
            list.sort(Comparator.comparing(u -> safe(u.getLastName()).toLowerCase()));
        } else if ("Role A-Z".equals(sort)) {
            list.sort(Comparator.comparing(u -> safe(u.getRole()).toLowerCase()));
        } else {
            list.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
        }

        list.forEach(user -> alertEntries.stream()
                .filter(alert -> alert.userId() == user.getId())
                .findFirst()
                .ifPresent(alert -> user.setSecurityAlertSummary(alert.securityAlertSummary())));

        users.setAll(list);
        String mode = Boolean.TRUE.equals(resolveArchiveFilter()) ? "archives" : Boolean.FALSE.equals(resolveArchiveFilter()) ? "actifs" : "tous";
        statusLabel.setText("Total utilisateurs " + mode + ": " + users.size());
    }

    private void populateEditor(FitopiaUser user) {
        selectedUser = user;
        if (user == null) {
            return;
        }
        nomField.setText(safe(user.getLastName()));
        prenomField.setText(safe(user.getFirstName()));
        usernameField.setText(safe(user.getUsername()));
        emailField.setText(safe(user.getEmail()));
        phoneField.setText(safe(user.getPhone()));
        newPasswordField.clear();
        roleCombo.setValue(safe(user.getRole()).isBlank() ? "Patient" : user.getRole());
        UserSecuritySnapshot snapshot = userSecurityController.getSecuritySnapshot(user.getId());
        UserAiInsightService.DuplicateDetectionResult duplicateResult =
                userAiInsightService.detectPotentialDuplicates(user, serviceUser.getAll());
        securityDetailsLabel.setText(buildSecuritySnapshotText(snapshot, duplicateResult));
        duplicateScoreLabel.setText(duplicateResult.duplicateScore() + "/100");
        duplicateRecommendationLabel.setText(duplicateResult.recommendation());
        duplicateFindingsLabel.setText(String.join(" | ", duplicateResult.findings()));
        setEditingMode(false);
    }

    private void clearEditor() {
        nomField.clear();
        prenomField.clear();
        usernameField.clear();
        emailField.clear();
        phoneField.clear();
        newPasswordField.clear();
        roleCombo.setValue("Patient");
        securityDetailsLabel.setText("Selectionnez un utilisateur pour afficher les alertes de securite.");
        duplicateScoreLabel.setText("-");
        duplicateRecommendationLabel.setText("Selectionnez un utilisateur pour analyser les doublons.");
        duplicateFindingsLabel.setText("Aucune analyse en cours.");
    }

    private void setEditingMode(boolean enabled) {
        editingMode = enabled;
        setEditorDisabled(!enabled);
        enregistrerButton.setDisable(!enabled);
        modifierButton.setDisable(enabled);
    }

    private void setEditorDisabled(boolean disabled) {
        nomField.setDisable(disabled);
        prenomField.setDisable(disabled);
        usernameField.setDisable(disabled);
        emailField.setDisable(disabled);
        phoneField.setDisable(disabled);
        newPasswordField.setDisable(disabled);
        roleCombo.setDisable(disabled);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private Boolean resolveArchiveFilter() {
        String filter = safe(archiveFilterCombo.getValue());
        if ("Archives".equalsIgnoreCase(filter)) {
            archiveViewButton.setText("Voir actifs");
            return true;
        }
        if ("Tous".equalsIgnoreCase(filter)) {
            archiveViewButton.setText("Voir archives");
            return null;
        }
        archiveViewButton.setText("Voir archives");
        return false;
    }

    private String buildSecuritySnapshotText(UserSecuritySnapshot snapshot) {
        return buildSecuritySnapshotText(snapshot, null);
    }

    private String buildSecuritySnapshotText(UserSecuritySnapshot snapshot, UserAiInsightService.DuplicateDetectionResult duplicateResult) {
        StringBuilder builder = new StringBuilder();
        builder.append("Score mot de passe: ").append(snapshot.passwordScore()).append("/100 (").append(snapshot.passwordStrength()).append(")")
                .append(" | Risque: ").append(snapshot.riskScore())
                .append(" | Echecs: ").append(snapshot.failedLoginAttempts())
                .append(" | Statut: ").append(snapshot.accountStatus())
                .append(" | Dernier changement: ").append(safe(snapshot.passwordLastChangedAt()))
                .append(" | Alertes: ").append(String.join(" / ", snapshot.alerts()));

        if (duplicateResult != null) {
            builder.append("\n\nDoublons IA -> Score: ")
                    .append(duplicateResult.duplicateScore())
                    .append("/100 | Recommandation: ")
                    .append(duplicateResult.recommendation())
                    .append(" | Details: ")
                    .append(String.join(" / ", duplicateResult.findings()));
        }
        return builder.toString();
    }
}
