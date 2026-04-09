package tn.esprit.Pidev3A49.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class SupplementShowcaseController {

    @FXML
    private ScrollPane rootScrollPane;

    @FXML
    private VBox contentRoot;

    @FXML
    private VBox cartPanel;

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
