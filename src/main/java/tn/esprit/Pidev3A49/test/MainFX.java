package tn.esprit.Pidev3A49.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFX extends Application {

    // Main JavaFX entry point.
    @Override
    public void start(Stage stage) throws IOException {
        // Load the root scene from the shared FXML file.
        FXMLLoader loader = new FXMLLoader(MainFX.class.getResource("/Main.fxml"));
        Scene scene = new Scene(loader.load());
        // Keep the application title consistent across launches.
        stage.setTitle("Fitopia");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
