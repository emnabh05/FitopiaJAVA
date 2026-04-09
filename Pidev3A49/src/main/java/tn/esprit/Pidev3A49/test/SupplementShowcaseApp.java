package tn.esprit.Pidev3A49.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SupplementShowcaseApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                SupplementShowcaseApp.class.getResource("/SupplementCatalogShowcase.fxml")
        );

        Scene scene = new Scene(loader.load(), 1440, 1024);
        stage.setTitle("Supplement Front End");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
