package tn.esprit.Pidev3A49.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.Pidev3A49.controllers.MainController;

import java.io.IOException;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainFX.class.getResource("/backrepas.fxml"));
        Scene scene = new Scene(loader.load());
        MainController controller = loader.getController();
        controller.ouvrirBackRepas();
        stage.setTitle("Fitopia");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
