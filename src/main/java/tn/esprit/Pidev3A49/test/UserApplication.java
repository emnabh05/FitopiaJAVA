package tn.esprit.Pidev3A49.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class UserApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(UserApplication.class.getResource("/User.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1440, 920);
        stage.setTitle("User");
        stage.setScene(scene);
        stage.setMinWidth(1200);
        stage.setMinHeight(820);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
