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
import tn.esprit.Pidev3A49.api.AuthController;
import tn.esprit.Pidev3A49.api.dto.LoginRequest;
import tn.esprit.Pidev3A49.api.dto.LoginResponse;
import tn.esprit.Pidev3A49.services.ServiceUser;
import tn.esprit.Pidev3A49.services.security.AuthenticationResult;

import java.io.File;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.stream.Collectors;

public class SignInController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Button faceIdButton;
    @FXML private Button verifyFaceIdButton;
    @FXML private Label botChallengeLabel;
    @FXML private TextField botAnswerField;
    @FXML private VBox faceIdSummaryBox;
    @FXML private ImageView facePreview;
    @FXML private Label faceIdSummaryLabel;

    @FXML private VBox recoveryPanel;
    @FXML private Label recoveryStep1Label;
    @FXML private Label recoveryStep2Label;
    @FXML private Label recoveryStep3Label;
    @FXML private Label recoveryStep4Label;
    @FXML private Label recoveryStatusLabel;
    @FXML private VBox recoveryStep1Box;
    @FXML private VBox recoveryStep2Box;
    @FXML private VBox recoveryStep3Box;
    @FXML private VBox recoveryStep4Box;
    @FXML private TextField recoveryIdentifierField;
    @FXML private Label recoveryAccountSummaryLabel;
    @FXML private Label recoveryChallengeLabel;
    @FXML private TextField recoveryChallengeAnswerField;
    @FXML private Label recoveryRiskLevelLabel;
    @FXML private Label recoveryRiskDetailsLabel;
    @FXML private PasswordField recoveryNewPasswordField;
    @FXML private PasswordField recoveryConfirmPasswordField;
    @FXML private Label recoveryPasswordStrengthLabel;
    @FXML private Label recoveryPasswordRulesLabel;
    @FXML private Label recoveryPasswordFeedbackLabel;
    @FXML private Button recoverySubmitButton;

    private final ServiceUser serviceUser = new ServiceUser();
    private final AuthController authController = new AuthController(serviceUser);
    private final SecureRandom random = new SecureRandom();

    private int expectedBotAnswer;
    private int expectedRecoveryAnswer;
    private int recoveryStep = 0;
    private ServiceUser.PasswordResetOtpInfo activeRecoveryOtp;
    private ServiceUser.PasswordRecoveryPreview activeRecoveryPreview;
    private FitopiaUser activeRecoveryUser;
    private ServiceUser.PasswordResetValidationPreview activePasswordValidation;

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
        } else {
            faceIdSummaryBox.setVisible(false);
            faceIdSummaryBox.setManaged(false);
            statusLabel.setText("Renseignez vos identifiants puis resolvez le challenge anti-robot.");
        }

        refreshBotChallenge();
        resetRecoveryState();
        recoveryNewPasswordField.textProperty().addListener((observable, oldValue, newValue) -> refreshRecoveryPasswordValidation());
        recoveryConfirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> refreshRecoveryPasswordValidation());
    }

    @FXML
    private void handleSignIn() {
        String email = safe(emailField.getText()).trim();
        String password = safe(passwordField.getText()).trim();
        String botAnswer = safe(botAnswerField.getText()).trim();

        if (email.isBlank() || password.isBlank()) {
            statusLabel.setText("Email et mot de passe sont obligatoires.");
            return;
        }
        if (!serviceUser.isAvailable()) {
            statusLabel.setText("Connexion MySQL indisponible. Verifiez fitopiabd.");
            return;
        }
        if (!isHumanVerified(botAnswer)) {
            statusLabel.setText("Verification anti-robot invalide. Reessayez.");
            refreshBotChallenge();
            return;
        }

        LoginResponse response = authController.login(new LoginRequest(email, password));
        if (response.status() != AuthenticationResult.Status.SUCCESS) {
            statusLabel.setText(response.message());
            refreshBotChallenge();
            return;
        }

        Optional<FitopiaUser> connectedOpt = serviceUser.findByIdentifier(email);
        if (connectedOpt.isEmpty()) {
            statusLabel.setText("Utilisateur introuvable apres authentification.");
            refreshBotChallenge();
            return;
        }

        FitopiaUser connected = connectedOpt.get();
        UserSession.setCurrentUser(connected);
        UserSession.setAccessToken(response.accessToken());
        UserSession.setAccessTokenExpiresInSeconds(response.expiresInSeconds() == null ? 0L : response.expiresInSeconds());
        openHomeFor(connected);
    }

    @FXML
    private void handleRefreshBotChallenge() {
        refreshBotChallenge();
        statusLabel.setText("Challenge anti-robot renouvelle.");
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
        recoveryPanel.setVisible(true);
        recoveryPanel.setManaged(true);
        recoveryIdentifierField.setText(safe(emailField.getText()).trim());
        recoveryStatusLabel.setText("Etape 1 active. Verifiez d'abord le compte cible via email ou username.");
        goToRecoveryStep(1);
    }

    @FXML
    private void handleCancelRecovery() {
        resetRecoveryState();
        statusLabel.setText("Workflow de recuperation ferme.");
    }

    @FXML
    private void handleRecoveryVerifyAccount() {
        String identifier = safe(recoveryIdentifierField.getText()).trim();
        if (identifier.isBlank()) {
            recoveryStatusLabel.setText("Entrez un email ou un username pour identifier le compte.");
            return;
        }

        try {
            activeRecoveryUser = serviceUser.findByIdentifier(identifier)
                    .orElseThrow(() -> new RuntimeException("Aucun compte n'est associe a cet identifiant."));
            activeRecoveryOtp = null;
            activeRecoveryPreview = null;
            activePasswordValidation = null;
            recoveryAccountSummaryLabel.setText("Compte detecte: " + activeRecoveryUser.getEmail()
                    + " | role: " + activeRecoveryUser.getRole());
            refreshRecoveryChallenge();
            recoveryStatusLabel.setText("Compte verifie. Passez a l'etape 2 pour la verification de securite.");
            goToRecoveryStep(2);
        } catch (RuntimeException e) {
            recoveryStatusLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleRecoveryRefreshChallenge() {
        refreshRecoveryChallenge();
        recoveryStatusLabel.setText("Challenge de securite renouvelle.");
    }

    @FXML
    private void handleRecoverySecurityCheck() {
        if (activeRecoveryUser == null) {
            recoveryStatusLabel.setText("Commencez par verifier le compte.");
            goToRecoveryStep(1);
            return;
        }
        if (!isRecoveryHumanVerified(safe(recoveryChallengeAnswerField.getText()))) {
            refreshRecoveryChallenge();
            recoveryStatusLabel.setText("Verification de securite invalide. Reessayez.");
            return;
        }

        try {
            activeRecoveryOtp = serviceUser.requestPasswordResetBySmsDemo(activeRecoveryUser.getEmail());
            recoveryAccountSummaryLabel.setText("Compte detecte: " + activeRecoveryOtp.email()
                    + " | expiration token: " + activeRecoveryOtp.expiresAt());
            activeRecoveryPreview = serviceUser.previewPasswordRecovery(
                    activeRecoveryOtp.email(),
                    activeRecoveryOtp.code(),
                    "inline-javafx",
                    "SignInController-InlineRecovery"
            );
            recoveryRiskLevelLabel.setText("Niveau de risque: " + activeRecoveryPreview.riskLevel());
            recoveryRiskDetailsLabel.setText(
                    "Tentatives recentes: " + activeRecoveryPreview.recentAttempts()
                            + " | Echecs: " + activeRecoveryPreview.recentFailures()
                            + " | Expiration token: " + activeRecoveryPreview.expiresAt()
                            + "\n" + activeRecoveryPreview.message()
            );
            recoveryStatusLabel.setText("Verification de securite validee. Analyse de risque calculee.");
            goToRecoveryStep(3);
        } catch (RuntimeException e) {
            recoveryStatusLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleRecoveryProceedToPassword() {
        if (activeRecoveryPreview == null) {
            recoveryStatusLabel.setText("L'analyse de risque doit etre calculee avant de continuer.");
            return;
        }
        recoveryPasswordFeedbackLabel.setText(
                "Risque courant: " + activeRecoveryPreview.riskLevel()
                        + ". Le nouveau mot de passe sera valide sur la force, les fuites et la non-reutilisation."
        );
        recoveryStatusLabel.setText("Definissez maintenant un nouveau mot de passe conforme aux exigences de securite.");
        refreshRecoveryPasswordValidation();
        goToRecoveryStep(4);
    }

    @FXML
    private void handleRecoverySubmitPassword() {
        if (activeRecoveryOtp == null || activeRecoveryPreview == null) {
            recoveryStatusLabel.setText("Le workflow de recuperation n'est pas complet.");
            return;
        }

        String newPassword = safe(recoveryNewPasswordField.getText()).trim();
        String confirmPassword = safe(recoveryConfirmPasswordField.getText()).trim();
        if (newPassword.isBlank() || confirmPassword.isBlank()) {
            recoveryPasswordFeedbackLabel.setText("Le nouveau mot de passe et sa confirmation sont obligatoires.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            recoveryPasswordFeedbackLabel.setText("La confirmation du mot de passe ne correspond pas.");
            return;
        }
        if (activePasswordValidation == null || !activePasswordValidation.accepted()) {
            recoveryPasswordFeedbackLabel.setText("Le mot de passe n'est pas encore valide pour finaliser la recuperation.");
            return;
        }

        try {
            ServiceUser.SmartPasswordRecoveryResult result = serviceUser.resetPasswordWithRiskVerification(
                    activeRecoveryOtp.email(),
                    activeRecoveryOtp.code(),
                    newPassword,
                    "inline-javafx",
                    "SignInController-InlineRecovery"
            );
            recoveryPasswordFeedbackLabel.setText(
                    "Mot de passe mis a jour. Score: " + result.report().score()
                            + "/100 | Force: " + result.report().strengthLabel()
            );
            recoveryStatusLabel.setText(result.confirmationMessage());
            statusLabel.setText("Recuperation terminee pour " + result.email() + ". Vous pouvez maintenant vous connecter.");
            emailField.setText(result.email());
            passwordField.clear();
            showRecoverySuccessAlert(result);
            resetRecoveryState();
        } catch (RuntimeException e) {
            recoveryPasswordFeedbackLabel.setText(e.getMessage());
            recoveryStatusLabel.setText("La finalisation a echoue. Corrigez les points signales.");
        }
    }

    private void showRecoverySuccessAlert(ServiceUser.SmartPasswordRecoveryResult result) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Recuperation confirmee");
        alert.setHeaderText("Smart Password Recovery");
        alert.setContentText("Compte: " + result.email()
                + "\nRisque: " + result.riskLevel()
                + "\nScore mot de passe: " + result.report().score() + "/100"
                + "\nForce: " + result.report().strengthLabel()
                + "\nHistorique securite: evenement enregistre"
                + "\nHorodatage: " + result.changedAt());
        alert.showAndWait();
    }

    private void goToRecoveryStep(int step) {
        recoveryStep = step;
        setStepState(recoveryStep1Box, step == 1);
        setStepState(recoveryStep2Box, step == 2);
        setStepState(recoveryStep3Box, step == 3);
        setStepState(recoveryStep4Box, step == 4);

        applyStepStyle(recoveryStep1Label, step == 1, step > 1);
        applyStepStyle(recoveryStep2Label, step == 2, step > 2);
        applyStepStyle(recoveryStep3Label, step == 3, step > 3);
        applyStepStyle(recoveryStep4Label, step == 4, false);
    }

    private void setStepState(VBox box, boolean visible) {
        box.setVisible(visible);
        box.setManaged(visible);
    }

    private void applyStepStyle(Label label, boolean active, boolean done) {
        if (active) {
            label.setStyle("-fx-background-color: #1f6a6d; -fx-background-radius: 16; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 6 10 6 10;");
            return;
        }
        if (done) {
            label.setStyle("-fx-background-color: #dfeceb; -fx-background-radius: 16; -fx-text-fill: #315057; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 6 10 6 10;");
            return;
        }
        label.setStyle("-fx-background-color: #edf3f2; -fx-background-radius: 16; -fx-text-fill: #7a8e93; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 6 10 6 10;");
    }

    private void resetRecoveryState() {
        activeRecoveryOtp = null;
        activeRecoveryPreview = null;
        activeRecoveryUser = null;
        activePasswordValidation = null;
        expectedRecoveryAnswer = 0;
        recoveryIdentifierField.clear();
        recoveryChallengeAnswerField.clear();
        recoveryNewPasswordField.clear();
        recoveryConfirmPasswordField.clear();
        recoveryAccountSummaryLabel.setText("Compte selectionne");
        recoveryChallengeLabel.setText("Challenge de verification...");
        recoveryRiskLevelLabel.setText("Niveau de risque: -");
        recoveryRiskDetailsLabel.setText("Les metriques de risque apparaitront ici.");
        recoveryPasswordStrengthLabel.setText("Force: -");
        recoveryPasswordRulesLabel.setText("Les regles de validation apparaitront ici.");
        recoveryPasswordFeedbackLabel.setText("Le systeme verifiera la force du mot de passe et la non-reutilisation.");
        recoverySubmitButton.setDisable(true);
        recoveryPanel.setVisible(false);
        recoveryPanel.setManaged(false);
        goToRecoveryStep(1);
    }

    private void refreshRecoveryPasswordValidation() {
        String password = safe(recoveryNewPasswordField.getText()).trim();
        String confirmation = safe(recoveryConfirmPasswordField.getText()).trim();

        if (activeRecoveryOtp == null || recoveryStep != 4) {
            recoverySubmitButton.setDisable(true);
            return;
        }

        try {
            activePasswordValidation = serviceUser.previewRecoveryPasswordValidation(activeRecoveryOtp.email(), password);
            recoveryPasswordStrengthLabel.setText(
                    "Force: " + activePasswordValidation.report().strengthLabel()
                            + " | Score: " + activePasswordValidation.report().score() + "/100"
            );
            String rules = activePasswordValidation.report().feedback().isEmpty()
                    ? "Aucune recommandation supplementaire."
                    : activePasswordValidation.report().feedback().stream().collect(Collectors.joining(" "));
            if (activePasswordValidation.passwordReused()) {
                rules = (rules + " Le mot de passe reutilise un historique precedent.").trim();
            }
            recoveryPasswordRulesLabel.setText(rules);

            boolean confirmationValid = !confirmation.isBlank() && password.equals(confirmation);
            if (password.isBlank()) {
                recoveryPasswordFeedbackLabel.setText("Saisissez un nouveau mot de passe pour lancer la validation.");
            } else if (!confirmation.isBlank() && !confirmationValid) {
                recoveryPasswordFeedbackLabel.setText("La confirmation du mot de passe ne correspond pas.");
            } else {
                recoveryPasswordFeedbackLabel.setText(activePasswordValidation.summary());
            }

            recoverySubmitButton.setDisable(!(activePasswordValidation.accepted() && confirmationValid));
        } catch (RuntimeException e) {
            activePasswordValidation = null;
            recoveryPasswordStrengthLabel.setText("Force: -");
            recoveryPasswordRulesLabel.setText("Validation indisponible: " + e.getMessage());
            recoveryPasswordFeedbackLabel.setText("Impossible de valider le mot de passe pour le moment.");
            recoverySubmitButton.setDisable(true);
        }
    }

    private void refreshRecoveryChallenge() {
        int first = random.nextInt(7) + 3;
        int second = random.nextInt(6) + 2;
        int mode = random.nextInt(2);
        recoveryChallengeAnswerField.clear();
        if (mode == 0) {
            expectedRecoveryAnswer = first + second;
            recoveryChallengeLabel.setText("Verification de securite: combien font " + first + " + " + second + " ?");
        } else {
            expectedRecoveryAnswer = first * second;
            recoveryChallengeLabel.setText("Verification de securite: combien font " + first + " x " + second + " ?");
        }
    }

    private boolean isRecoveryHumanVerified(String answer) {
        if (safe(answer).isBlank()) {
            return false;
        }
        try {
            return Integer.parseInt(answer.trim()) == expectedRecoveryAnswer;
        } catch (NumberFormatException e) {
            return false;
        }
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

    private void refreshBotChallenge() {
        int first = random.nextInt(8) + 2;
        int second = random.nextInt(8) + 2;
        int mode = random.nextInt(3);
        botAnswerField.clear();

        if (mode == 0) {
            expectedBotAnswer = first + second;
            botChallengeLabel.setText("Anti-robot : combien font " + first + " + " + second + " ?");
            return;
        }
        if (mode == 1) {
            expectedBotAnswer = first * second;
            botChallengeLabel.setText("Anti-robot : combien font " + first + " x " + second + " ?");
            return;
        }

        expectedBotAnswer = second;
        botChallengeLabel.setText("Anti-robot : tapez le nombre " + second + " pour continuer.");
    }

    private boolean isHumanVerified(String answer) {
        if (answer.isBlank()) {
            return false;
        }
        try {
            return Integer.parseInt(answer) == expectedBotAnswer;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
