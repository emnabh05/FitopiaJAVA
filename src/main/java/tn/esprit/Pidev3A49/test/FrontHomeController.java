package tn.esprit.Pidev3A49.test;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.services.UserAiInsightService;

public class FrontHomeController {
    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;
    @FXML private Label profileSummaryLabel;
    @FXML private Label securitySummaryLabel;
    @FXML private Label apiIdeasLabel;
    @FXML private Label aiIdeasLabel;

    private final UserAiInsightService insightService = new UserAiInsightService();

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
    }

    @FXML
    private void handleLogout() {
        UserSession.clear();
        SceneNavigator.goTo(statusLabel, "/SignIn.fxml", "Sign In", 1460, 860);
    }
}
