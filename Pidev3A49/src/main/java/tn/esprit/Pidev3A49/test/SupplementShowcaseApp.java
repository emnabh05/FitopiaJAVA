package tn.esprit.Pidev3A49.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.Pidev3A49.utils.SceneNavigator;

public class SupplementShowcaseApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                SupplementShowcaseApp.class.getResource(SceneNavigator.START_VIEW.fxmlPath())
        );

        Scene scene = new Scene(loader.load(), SceneNavigator.WINDOW_WIDTH, SceneNavigator.WINDOW_HEIGHT);
        stage.setTitle(SceneNavigator.START_VIEW.title());
        stage.setScene(scene);
        SceneNavigator.applyWindowMode(stage);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
