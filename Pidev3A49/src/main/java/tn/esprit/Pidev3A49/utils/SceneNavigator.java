package tn.esprit.Pidev3A49.utils;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public final class SceneNavigator {

    public static final double WINDOW_WIDTH = 1440.0;
    public static final double WINDOW_HEIGHT = 1024.0;

    public static final ViewState FRONT_END_VIEW = new ViewState("/SupplementCatalogShowcase.fxml", "Supplement Front End");
    public static final ViewState BACK_END_VIEW = new ViewState("/Main.fxml", "Supplement Back End");
    public static final ViewState PROGRESS_VIEW = new ViewState("/SupplementProgress.fxml", "My Supplement Progress");
    public static final ViewState RANKING_VIEW = new ViewState("/MonthlyRanking.fxml", "Monthly Ranking");

    private static final Deque<ViewState> HISTORY = new ArrayDeque<>();

    private SceneNavigator() {
    }

    public static void navigate(ActionEvent event, ViewState currentView, ViewState targetView) throws IOException {
        if (!currentView.fxmlPath().equals(targetView.fxmlPath())) {
            HISTORY.push(currentView);
        }
        load(event, targetView);
    }

    public static void goBackOrClose(ActionEvent event) throws IOException {
        if (HISTORY.isEmpty()) {
            extractStage(event).close();
            return;
        }

        load(event, HISTORY.pop());
    }

    public static void load(ActionEvent event, ViewState targetView) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(targetView.fxmlPath()));
        Parent root = loader.load();

        Stage stage = extractStage(event);
        stage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        stage.setTitle(targetView.title());
        stage.show();
    }

    private static Stage extractStage(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }

    public record ViewState(String fxmlPath, String title) {
        public ViewState {
            Objects.requireNonNull(fxmlPath, "fxmlPath");
            Objects.requireNonNull(title, "title");
        }
    }
}
