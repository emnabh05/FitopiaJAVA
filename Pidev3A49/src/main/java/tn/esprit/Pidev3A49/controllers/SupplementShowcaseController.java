package tn.esprit.Pidev3A49.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class SupplementShowcaseController {

    @FXML
    private ScrollPane rootScrollPane;

    @FXML
    private VBox contentRoot;

    @FXML
    private VBox cartPanel;

    public void openMonthlyRanking(ActionEvent event) throws IOException {
        switchScene(event, "/MonthlyRanking.fxml", "Monthly Ranking");
    }

    @FXML
    private void showCartPanel() {
        cartPanel.setManaged(true);
        cartPanel.setVisible(true);

        Platform.runLater(() -> {
            double scrollableHeight = contentRoot.getBoundsInLocal().getHeight() - rootScrollPane.getViewportBounds().getHeight();
            if (scrollableHeight <= 0) {
                rootScrollPane.setVvalue(0.0);
                return;
            }

            double targetY = cartPanel.getBoundsInParent().getMinY();
            double targetValue = Math.max(0.0, Math.min(1.0, targetY / scrollableHeight));
            rootScrollPane.setVvalue(targetValue);
        });
    }

    @FXML
    private void hideCartPanel() {
        cartPanel.setVisible(false);
        cartPanel.setManaged(false);
    }

    private void switchScene(ActionEvent event, String resourcePath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 1440, 1024));
        stage.setTitle(title);
        stage.show();
    }
}
