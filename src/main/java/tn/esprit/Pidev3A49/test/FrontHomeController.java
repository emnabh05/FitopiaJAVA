package tn.esprit.Pidev3A49.test;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.services.UserAiInsightService;
import tn.esprit.Pidev3A49.services.security.JwtClaims;
import tn.esprit.Pidev3A49.services.security.JwtService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class FrontHomeController {
    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;
    @FXML private Label profileSummaryLabel;
    @FXML private Label securitySummaryLabel;
    @FXML private Label apiIdeasLabel;
    @FXML private Label aiIdeasLabel;
    @FXML private Label jwtSummaryLabel;
    @FXML private Label jwtTokenMaskedLabel;

    private final UserAiInsightService insightService = new UserAiInsightService();
    private final JwtService jwtService = new JwtService();

    @FXML
    public void initialize() {
        FitopiaUser current = UserSession.getCurrentUser();
        if (current == null) {
            welcomeLabel.setText("Bienvenue");
            statusLabel.setText("Session vide.");
            UserAiInsightService.UserInsight insight = insightService.buildInsight(null);
            profileSummaryLabel.setText(insight.profileSummary());
            securitySummaryLabel.setText(insight.securitySummary());
            apiIdeasLabel.setText(insight.apiIdeas());
            aiIdeasLabel.setText(insight.aiIdeas());
            renderJwtDebug();
            return;
        }
        UserAiInsightService.UserInsight insight = insightService.buildInsight(current);
        welcomeLabel.setText("Bienvenue " + current.getFirstName() + " " + current.getLastName());
        statusLabel.setText("Role: " + current.getRole() + " | Email: " + current.getEmail()
                + " | Face ID: " + (current.isFaceIdEnabled() ? "active" : "inactive"));
        profileSummaryLabel.setText(insight.profileSummary());
        securitySummaryLabel.setText(insight.securitySummary());
        apiIdeasLabel.setText(insight.apiIdeas());
        aiIdeasLabel.setText(insight.aiIdeas());
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
            jwtSummaryLabel.setText("JWT valide | userId=" + claims.userId()
                    + " | role=" + claims.role()
                    + " | expire le " + expiresAt);
            jwtTokenMaskedLabel.setText(maskToken(token));
        } catch (RuntimeException e) {
            jwtSummaryLabel.setText("JWT present mais invalide: " + e.getMessage());
            jwtTokenMaskedLabel.setText(maskToken(token));
        }
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
}
