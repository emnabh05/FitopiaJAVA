package tn.esprit.Pidev3A49.utils;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public final class SceneNavigator {

    public static final double WINDOW_WIDTH = 1440.0;
    public static final double WINDOW_HEIGHT = 1024.0;
    public static final boolean OPEN_MAXIMIZED = true;

    public static final ViewState START_VIEW = new ViewState("/Start.fxml", "Fitopia");
    public static final ViewState FRONT_END_VIEW = new ViewState("/SupplementCatalogShowcase.fxml", "Supplement Front End");
    public static final ViewState FRONT_ORDERS_VIEW = new ViewState("/FrontOrders.fxml", "My Orders");
    public static final ViewState BACK_END_VIEW = new ViewState("/Main.fxml", "Supplement Back End");
    public static final ViewState ORDERD_VIEW = new ViewState("/OrderedCustomers.fxml", "Orderd Customers");
    public static final ViewState PROGRESS_VIEW = new ViewState("/SupplementProgress.fxml", "My Supplement Progress");
    public static final ViewState RANKING_VIEW = new ViewState("/MonthlyRanking.fxml", "Monthly Ranking");
    public static final ViewState CHECKOUT_VIEW = new ViewState("/Checkout.fxml", "Secure Checkout");
    public static final ViewState CARD_PAYMENT_VIEW = new ViewState("/CardPayment.fxml", "Card Payment");
    public static final ViewState ORDER_CONFIRMATION_VIEW = new ViewState("/OrderConfirmation.fxml", "Commande confirmee");
    public static final ViewState FITNESS_FRONT_VIEW = new ViewState("/FitopiaHome.fxml", "Fitness Front End");
    public static final ViewState FITNESS_BACK_VIEW = new ViewState("/FitnessCrudAdmin.fxml", "Fitness Back End");

    private static final Deque<ViewState> HISTORY = new ArrayDeque<>();

    private SceneNavigator() {
    }

    public static void navigate(ActionEvent event, ViewState currentView, ViewState targetView) throws IOException {
        if (!currentView.fxmlPath().equals(targetView.fxmlPath())) {
            HISTORY.push(currentView);
        }
        load(event, targetView);
    }

    public static void navigate(Node source, ViewState currentView, ViewState targetView) throws IOException {
        if (!currentView.fxmlPath().equals(targetView.fxmlPath())) {
            HISTORY.push(currentView);
        }
        load(source, targetView);
    }

    public static void goBackOrClose(ActionEvent event) throws IOException {
        if (HISTORY.isEmpty()) {
            extractStage(event).close();
            return;
        }

        load(event, HISTORY.pop());
    }

    public static void goBackOrClose(Node source) throws IOException {
        if (HISTORY.isEmpty()) {
            extractStage(source).close();
            return;
        }

        load(source, HISTORY.pop());
    }

    public static void load(ActionEvent event, ViewState targetView) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(targetView.fxmlPath()));
        Parent root = loader.load();

        Stage stage = extractStage(event);
        stage.setScene(createScene(root));
        stage.setTitle(targetView.title());
        stage.show();
        applyWindowMode(stage);
    }

    public static void load(Node source, ViewState targetView) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(targetView.fxmlPath()));
        Parent root = loader.load();

        Stage stage = extractStage(source);
        stage.setScene(createScene(root));
        stage.setTitle(targetView.title());
        stage.show();
        applyWindowMode(stage);
    }

    public static void applyWindowMode(Stage stage) {
        Rectangle2D visualBounds = getVisualBounds();
        stage.setResizable(true);
        stage.setX(visualBounds.getMinX());
        stage.setY(visualBounds.getMinY());
        stage.setWidth(visualBounds.getWidth());
        stage.setHeight(visualBounds.getHeight());

        if (OPEN_MAXIMIZED) {
            stage.setMaximized(false);
            stage.setMaximized(true);
        }

        Platform.runLater(() -> {
            stage.setX(visualBounds.getMinX());
            stage.setY(visualBounds.getMinY());
            stage.setWidth(visualBounds.getWidth());
            stage.setHeight(visualBounds.getHeight());

            if (OPEN_MAXIMIZED) {
                stage.setMaximized(true);
            }
        });
    }

    public static Scene createScene(Parent root) {
        Rectangle2D visualBounds = getVisualBounds();
        return new Scene(root, visualBounds.getWidth(), visualBounds.getHeight());
    }

    private static Stage extractStage(ActionEvent event) {
        return extractStage((Node) event.getSource());
    }

    private static Stage extractStage(Node source) {
        return (Stage) source.getScene().getWindow();
    }

    private static Rectangle2D getVisualBounds() {
        return Screen.getPrimary().getVisualBounds();
    }

    public record ViewState(String fxmlPath, String title) {
        public ViewState {
            Objects.requireNonNull(fxmlPath, "fxmlPath");
            Objects.requireNonNull(title, "title");
        }
    }
}
