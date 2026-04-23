package tn.esprit.Pidev3A49.test;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.api.AuthController;
import tn.esprit.Pidev3A49.api.dto.LoginRequest;
import tn.esprit.Pidev3A49.api.dto.LoginResponse;
import tn.esprit.Pidev3A49.services.FitopiaUserService;
import tn.esprit.Pidev3A49.services.security.AuthenticationResult;
import tn.esprit.Pidev3A49.utils.SessionRouter;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Optional;

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

    private final FitopiaUserService fitopiaUserService = new FitopiaUserService();
    private final AuthController authController = new AuthController(fitopiaUserService);
    private final SecureRandom random = new SecureRandom();
    private int expectedBotAnswer;

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
            refreshBotChallenge();
            return;
        }

        faceIdSummaryBox.setVisible(false);
        faceIdSummaryBox.setManaged(false);
        refreshBotChallenge();
        statusLabel.setText("Renseignez vos identifiants puis resolvez le challenge anti-robot.");
    }

    @FXML
    private void handleSignIn() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();
        String botAnswer = botAnswerField.getText() == null ? "" : botAnswerField.getText().trim();

        if (email.isBlank() || password.isBlank()) {
            statusLabel.setText("Email et mot de passe sont obligatoires.");
            return;
        }
        if (!fitopiaUserService.isAvailable()) {
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

        Optional<FitopiaUser> connectedOpt = fitopiaUserService.findByIdentifier(email);
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
        try {
            String identifier = askEmailForReset();
            if (identifier == null) {
                statusLabel.setText("Reinitialisation annulee.");
                return;
            }

            FitopiaUserService.PasswordResetOtpInfo otpInfo = fitopiaUserService.requestPasswordResetBySmsDemo(identifier);
            String resetUrl = PasswordResetWebServer.getInstance().buildResetUrl(otpInfo.email(), otpInfo.code());
            Alert smsAlert = new Alert(Alert.AlertType.INFORMATION);
            smsAlert.setTitle("Lien de reinitialisation");
            smsAlert.setHeaderText("Reinitialisation hors application");
            smsAlert.setContentText("Compte concerne: " + otpInfo.email()
                    + "\nExpiration: " + otpInfo.expiresAt()
                    + "\n\nLien securise:\n" + resetUrl
                    + "\n\nLe formulaire web s'ouvrira automatiquement.");
            smsAlert.showAndWait();

            openResetLink(resetUrl);
            statusLabel.setText("Lien de reinitialisation ouvert. Finalisez le formulaire web.");
        } catch (RuntimeException e) {
            statusLabel.setText(e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de reinitialisation");
            alert.setHeaderText("Mot de passe oublie");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void openHomeFor(FitopiaUser connected) {
        UserSession.setVerifyFaceIdOnly(false);
        SessionRouter.openRoleHome(statusLabel);
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

    private String askEmailForReset() {
        TextInputDialog dialog = new TextInputDialog(emailField.getText() == null ? "" : emailField.getText().trim());
        dialog.setTitle("Mot de passe oublie");
        dialog.setHeaderText("Recuperation du mot de passe");
        dialog.setContentText("Entrez votre email :");
        return dialog.showAndWait().map(String::trim).filter(value -> !value.isBlank()).orElse(null);
    }

    private void openResetLink(String url) {
        try {
            if (!Desktop.isDesktopSupported()) {
                throw new RuntimeException("Ouverture navigateur non supportee. Copiez le lien manuellement.");
            }
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'ouvrir le lien de reinitialisation: " + e.getMessage(), e);
        }
    }
}

