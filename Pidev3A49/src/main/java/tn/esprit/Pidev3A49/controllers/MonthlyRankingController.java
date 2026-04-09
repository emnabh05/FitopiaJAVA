package tn.esprit.Pidev3A49.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MonthlyRankingController {

    public void openSupplementShowcase(ActionEvent event) throws IOException {
        switchScene(event, "/SupplementCatalogShowcase.fxml", "Supplement Front End");
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
