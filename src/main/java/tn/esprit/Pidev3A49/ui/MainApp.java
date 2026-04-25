package tn.esprit.Pidev3A49.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FitopiaHome.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/fitopia-home.css").toExternalForm());

        stage.setTitle("Fitopia");
        stage.setScene(scene);
        stage.setWidth(1440);
        stage.setHeight(920);
        stage.show();
    }
}
