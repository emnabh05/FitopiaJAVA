package tn.esprit.Pidev3A49.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainFX.class.getResource("/SignIn.fxml"));
        Scene scene = new Scene(loader.load(), 1460, 860);
        stage.setTitle("Sign In");
        stage.setScene(scene);
        stage.setMinWidth(1280);
        stage.setMinHeight(760);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
