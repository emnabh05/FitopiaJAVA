package tn.esprit.Pidev3A49.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FitopiaHomeApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(FitopiaHomeApp.class.getResource("/FitopiaHome.fxml"));
        Scene scene = new Scene(loader.load(), 1440, 920);
        stage.setTitle("Fitopia - Front Office");
        stage.setMinWidth(1180);
        stage.setMinHeight(760);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
