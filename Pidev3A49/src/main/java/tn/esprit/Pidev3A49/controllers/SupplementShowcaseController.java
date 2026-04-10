package tn.esprit.Pidev3A49.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import tn.esprit.Pidev3A49.utils.SceneNavigator;

import java.io.IOException;

public class SupplementShowcaseController {

    @FXML
    private ScrollPane rootScrollPane;

    @FXML
    private VBox contentRoot;

    @FXML
    private VBox cartPanel;

    public void openProgressTracker(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.FRONT_END_VIEW, SceneNavigator.PROGRESS_VIEW);
    }

    public void openMonthlyRanking(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.FRONT_END_VIEW, SceneNavigator.RANKING_VIEW);
    }

    public void openBackEnd(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.FRONT_END_VIEW, SceneNavigator.BACK_END_VIEW);
    }

    public void goBackOrExit(ActionEvent event) throws IOException {
        SceneNavigator.goBackOrClose(event);
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
}
