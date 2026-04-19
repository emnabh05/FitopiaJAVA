package tn.esprit.Pidev3A49.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainFX.class.getResource("/Main.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Fitopia");
        stage.setScene(scene);
        stage.setMaximized(true); // Permet de respecter la barre de tache de Windows
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
