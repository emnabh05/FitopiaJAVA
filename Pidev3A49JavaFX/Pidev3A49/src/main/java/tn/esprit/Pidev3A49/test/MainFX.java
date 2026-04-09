package tn.esprit.Pidev3A49.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainFX.class.getResource("/Main.fxml"));
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double sceneWidth = Math.min(1400, Math.max(1180, visualBounds.getWidth() - 80));
        double sceneHeight = Math.min(820, Math.max(720, visualBounds.getHeight() - 80));

        Scene scene = new Scene(loader.load(), sceneWidth, sceneHeight);
        stage.setTitle("Fitopia");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setWidth(sceneWidth);
        stage.setHeight(sceneHeight);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
