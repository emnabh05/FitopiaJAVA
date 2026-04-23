package tn.esprit.Pidev3A49.test;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public final class SceneNavigator {
    private SceneNavigator() {
    }

    public static void goTo(Label sourceLabel, String fxmlPath, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), width, height);
            Stage stage = (Stage) sourceLabel.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setMinWidth(Math.min(width, 1000));
            stage.setMinHeight(Math.min(height, 650));
            stage.centerOnScreen();
        } catch (IOException e) {
            sourceLabel.setText("Impossible d'ouvrir la page " + fxmlPath + " : " + e.getMessage());
        }
    }
}
