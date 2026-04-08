package tn.esprit.Pidev3A49.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationController {

    public void ouvrirBack(ActionEvent event) throws IOException {
        chargerVue(event, "/backrepas.fxml", true);
    }

    public void ouvrirFront(ActionEvent event) throws IOException {
        chargerVue(event, "/frontregime.fxml", false);
    }

    private void chargerVue(ActionEvent event, String resource, boolean backMode) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
        Parent root = loader.load();

        MainController controller = loader.getController();
        if (backMode) {
            controller.ouvrirBackRepas();
        } else {
            controller.ouvrirFrontRegimes();
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
    }
}
