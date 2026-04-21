package tn.esprit.Pidev3A49.test;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.api.AuthController;
import tn.esprit.Pidev3A49.api.dto.LoginRequest;
import tn.esprit.Pidev3A49.api.dto.LoginResponse;
import tn.esprit.Pidev3A49.api.dto.ResetPasswordRequest;
import tn.esprit.Pidev3A49.services.ServiceUser;
import tn.esprit.Pidev3A49.services.security.AuthenticationResult;

import java.io.File;
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

    private final ServiceUser serviceUser = new ServiceUser();
    private final AuthController authController = new AuthController(serviceUser);
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
        try {
            String identifier = askEmailForReset();
            if (identifier == null) {
                return;
            }

            ServiceUser.PasswordResetOtpInfo otpInfo = serviceUser.requestPasswordResetBySmsDemo(identifier);
            Alert smsAlert = new Alert(Alert.AlertType.INFORMATION);
            smsAlert.setTitle("Code SMS demo");
            smsAlert.setHeaderText("Code de reinitialisation genere");
            smsAlert.setContentText("Compte concerne: " + otpInfo.email()
                    + "\nNumero cible: " + otpInfo.phone()
                    + "\n\nSMS demo envoye au numero " + otpInfo.phone()
                    + ".\nCode OTP: " + otpInfo.code()
                    + "\nExpiration: " + otpInfo.expiresAt()
                    + "\n\nUtilisez ce code dans la fenetre suivante.");
            smsAlert.showAndWait();

            String code = askResetCode();
            if (code == null) {
                return;
            }

            String newPassword = askNewPassword();
            if (newPassword == null) {
                return;
            }

            authController.resetPassword(new ResetPasswordRequest(otpInfo.email(), code, newPassword));
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Mot de passe mis a jour");
            alert.setHeaderText("Reinitialisation terminee");
            alert.setContentText("Votre mot de passe a ete mis a jour. Vous pouvez maintenant vous connecter.");
            alert.showAndWait();
            statusLabel.setText("Mot de passe reinitialise avec succes.");
        } catch (RuntimeException e) {
            statusLabel.setText(e.getMessage());
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

    private String askEmailForReset() {
        TextInputDialog dialog = new TextInputDialog(emailField.getText() == null ? "" : emailField.getText().trim());
        dialog.setTitle("Mot de passe oublie");
        dialog.setHeaderText("Recuperation du mot de passe");
        dialog.setContentText("Entrez votre email :");
        return dialog.showAndWait().map(String::trim).filter(value -> !value.isBlank()).orElse(null);
    }

    private String askResetCode() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Code de reinitialisation");
        dialog.setHeaderText("Verification par email");
        dialog.setContentText("Entrez le code recu par email :");
        return dialog.showAndWait().map(String::trim).filter(value -> !value.isBlank()).orElse(null);
    }

    private String askNewPassword() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Nouveau mot de passe");
        dialog.setHeaderText("Choisissez un nouveau mot de passe");

        ButtonType confirmButton = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButton, ButtonType.CANCEL);

        PasswordField password = new PasswordField();
        password.setPromptText("Nouveau mot de passe");
        PasswordField confirm = new PasswordField();
        confirm.setPromptText("Confirmer le mot de passe");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nouveau mot de passe"), 0, 0);
        grid.add(password, 0, 1);
        grid.add(new Label("Confirmation"), 0, 2);
        grid.add(confirm, 0, 3);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType != confirmButton) {
                return null;
            }
            return (password.getText() == null ? "" : password.getText().trim())
                    + "\n"
                    + (confirm.getText() == null ? "" : confirm.getText().trim());
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return null;
        }
        String[] values = result.get().split("\\n", -1);
        String first = values.length > 0 ? values[0].trim() : "";
        String second = values.length > 1 ? values[1].trim() : "";
        if (first.isBlank()) {
            throw new RuntimeException("Le nouveau mot de passe est obligatoire.");
        }
        if (!first.equals(second)) {
            throw new RuntimeException("La confirmation du mot de passe ne correspond pas.");
        }
        return first;
    }
}
