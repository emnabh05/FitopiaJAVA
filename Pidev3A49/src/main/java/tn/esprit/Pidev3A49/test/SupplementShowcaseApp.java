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
                SupplementShowcaseApp.class.getResource(SceneNavigator.FRONT_END_VIEW.fxmlPath())
        );

        Scene scene = new Scene(loader.load(), SceneNavigator.WINDOW_WIDTH, SceneNavigator.WINDOW_HEIGHT);
        stage.setTitle(SceneNavigator.FRONT_END_VIEW.title());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
