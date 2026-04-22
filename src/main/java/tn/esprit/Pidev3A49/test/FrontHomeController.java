package tn.esprit.Pidev3A49.test;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.api.dto.UserDigitalTwinReport;
import tn.esprit.Pidev3A49.services.UserDigitalTwinService;
import tn.esprit.Pidev3A49.services.security.JwtClaims;
import tn.esprit.Pidev3A49.services.security.JwtService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

public class FrontHomeController {
    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;
    @FXML private Label digitalTwinSummaryLabel;
    @FXML private Label goalValueLabel;
    @FXML private Label regimeValueLabel;
    @FXML private Label targetCaloriesValueLabel;
    @FXML private Label avgRealCaloriesValueLabel;
    @FXML private Label totalRealCaloriesValueLabel;
    @FXML private Label mealsCountValueLabel;
    @FXML private Label lastMealDateValueLabel;
    @FXML private Label calorieGapValueLabel;
    @FXML private Label twinScoreValueLabel;
    @FXML private Label alignmentStatusValueLabel;
    @FXML private Label alertLevelValueLabel;
    @FXML private Label jwtSummaryLabel;
    @FXML private Label jwtTokenMaskedLabel;

    private final UserDigitalTwinService digitalTwinService = new UserDigitalTwinService();
    private final JwtService jwtService = new JwtService();

    @FXML
    public void initialize() {
        FitopiaUser current = UserSession.getCurrentUser();
        if (current == null) {
            welcomeLabel.setText("Bienvenue");
            statusLabel.setText("Session vide.");
            renderEmptyDigitalTwin("Aucune session utilisateur active pour lancer l'analyse du jumeau numerique.");
            renderJwtDebug();
            return;
        }

        welcomeLabel.setText("Bienvenue " + current.getFirstName() + " " + current.getLastName());
        statusLabel.setText("Role: " + current.getRole() + " | Email: " + current.getEmail()
                + " | Face ID: " + (current.isFaceIdEnabled() ? "active" : "inactive"));
        renderDigitalTwin(current);
        renderJwtDebug();
    }

    @FXML
    private void handleLogout() {
        UserSession.clear();
        SceneNavigator.goTo(statusLabel, "/SignIn.fxml", "Sign In", 1460, 860);
    }

    private void renderJwtDebug() {
        String token = safe(UserSession.getAccessToken()).trim();
        if (token.isBlank()) {
            jwtSummaryLabel.setText("JWT absent dans la session.");
            jwtTokenMaskedLabel.setText("-");
            return;
        }

        try {
            JwtClaims claims = jwtService.validate(token);
            LocalDateTime expiresAt = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(claims.expiresAtEpochSeconds()),
                    ZoneId.systemDefault()
            );
            jwtSummaryLabel.setText("JWT valide | subject=" + claims.subject()
                    + " | role=" + claims.role()
                    + " | expire le " + expiresAt);
            jwtTokenMaskedLabel.setText(maskToken(token));
        } catch (RuntimeException e) {
            jwtSummaryLabel.setText("JWT present mais invalide: " + e.getMessage());
            jwtTokenMaskedLabel.setText(maskToken(token));
        }
    }

    private void renderDigitalTwin(FitopiaUser current) {
        try {
            Optional<UserDigitalTwinReport> optionalReport = digitalTwinService.generateDigitalTwinAnalysis().stream()
                    .filter(report -> report.getUserId() == current.getId())
                    .findFirst();

            if (optionalReport.isEmpty()) {
                renderEmptyDigitalTwin("Aucun rapport de jumeau numerique n'est disponible pour cet utilisateur.");
                return;
            }

            UserDigitalTwinReport report = optionalReport.get();
            digitalTwinSummaryLabel.setText(
                    "Comparaison entre le modele nutritionnel theorique de l'utilisateur et son comportement alimentaire reel."
            );
            goalValueLabel.setText(orDefault(report.getGoal(), "OBJECTIF_NON_DEFINI"));
            regimeValueLabel.setText(orDefault(report.getRegimeName(), "REGIME_NON_DEFINI"));
            targetCaloriesValueLabel.setText(formatCalories(report.getTargetCalories()));
            avgRealCaloriesValueLabel.setText(formatCalories(report.getAvgRealCalories()));
            totalRealCaloriesValueLabel.setText(formatCalories(report.getTotalRealCalories()));
            mealsCountValueLabel.setText(String.valueOf(report.getMealsCount()));
            lastMealDateValueLabel.setText(orDefault(report.getLastMealDate(), "Aucune activite alimentaire"));
            calorieGapValueLabel.setText(formatCalories(report.getCalorieGap()));
            twinScoreValueLabel.setText(report.getTwinScore() + " / 100");
            alignmentStatusValueLabel.setText(orDefault(report.getAlignmentStatus(), "-"));
            alertLevelValueLabel.setText(orDefault(report.getAlertLevel(), "-"));
        } catch (RuntimeException e) {
            renderEmptyDigitalTwin("Analyse indisponible: " + e.getMessage());
        }
    }

    private void renderEmptyDigitalTwin(String message) {
        digitalTwinSummaryLabel.setText(message);
        goalValueLabel.setText("-");
        regimeValueLabel.setText("-");
        targetCaloriesValueLabel.setText("-");
        avgRealCaloriesValueLabel.setText("-");
        totalRealCaloriesValueLabel.setText("-");
        mealsCountValueLabel.setText("-");
        lastMealDateValueLabel.setText("-");
        calorieGapValueLabel.setText("-");
        twinScoreValueLabel.setText("-");
        alignmentStatusValueLabel.setText("-");
        alertLevelValueLabel.setText("-");
    }

    private String maskToken(String token) {
        String value = safe(token);
        if (value.length() <= 24) {
            return value;
        }
        return value.substring(0, 16) + " ... " + value.substring(value.length() - 16);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String orDefault(String value, String fallback) {
        return safe(value).isBlank() ? fallback : value;
    }

    private String formatCalories(Double value) {
        if (value == null) {
            return "-";
        }
        if (Math.abs(value - Math.rint(value)) < 0.01) {
            return String.format("%.0f kcal", value);
        }
        return String.format("%.2f kcal", value);
    }
}
