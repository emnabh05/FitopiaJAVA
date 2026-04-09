package tn.esprit.Pidev3A49.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;

public class SupplementShowcaseController {

    @FXML
    private ScrollPane rootScrollPane;

    @FXML
    private VBox cartPanel;

    @FXML
    private void showCartPanel() {
        cartPanel.setManaged(true);
        cartPanel.setVisible(true);

        Platform.runLater(() -> rootScrollPane.setVvalue(1.0));
    }

    @FXML
    private void hideCartPanel() {
        cartPanel.setVisible(false);
        cartPanel.setManaged(false);
    }
}
