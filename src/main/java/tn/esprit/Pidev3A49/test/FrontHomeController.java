package tn.esprit.Pidev3A49.test;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tn.esprit.Pidev3A49.Models.FitopiaUser;

public class FrontHomeController {
    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        FitopiaUser current = UserSession.getCurrentUser();
        if (current == null) {
            welcomeLabel.setText("Bienvenue");
            statusLabel.setText("Session vide.");
            return;
        }
        welcomeLabel.setText("Bienvenue " + current.getFirstName() + " " + current.getLastName());
        statusLabel.setText("Role: " + current.getRole() + " | Email: " + current.getEmail());
    }

    @FXML
    private void handleLogout() {
        UserSession.clear();
        SceneNavigator.goTo(statusLabel, "/SignIn.fxml", "Sign In", 1460, 860);
    }
}
