package tn.esprit.Pidev3A49.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class UserApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(UserApplication.class.getResource("/SignIn.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1460, 860);
        stage.setTitle("Sign In");
        stage.setScene(scene);
        stage.setMinWidth(1280);
        stage.setMinHeight(760);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
