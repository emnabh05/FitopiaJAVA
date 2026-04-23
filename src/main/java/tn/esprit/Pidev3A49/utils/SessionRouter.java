package tn.esprit.Pidev3A49.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.controllers.MainController;
import tn.esprit.Pidev3A49.test.UserSession;

import java.io.IOException;

public final class SessionRouter {

    private SessionRouter() {
    }

    public static boolean hasAuthenticatedUser() {
        return UserSession.getCurrentUser() != null;
    }

    public static boolean isAdminSession() {
        FitopiaUser currentUser = UserSession.getCurrentUser();
        return currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
    }

    public static void openRoleHome(Node source) {
        if (isAdminSession()) {
            openBackHome(source);
            return;
        }
        openFrontHome(source);
    }

    public static void openFrontHome(Node source) {
        try {
            FXMLLoader loader = new FXMLLoader(SessionRouter.class.getResource("/SupplementCatalogShowcase.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = extractStage(source);
            stage.setTitle("Fitopia Front Office");
            stage.setScene(scene);
            stage.show();
            SceneNavigator.applyWindowMode(stage);
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible d'ouvrir le front office.", exception);
        }
    }

    public static void openBackHome(Node source) {
        try {
            FXMLLoader loader = new FXMLLoader(SessionRouter.class.getResource("/backrepas.fxml"));
            Scene scene = new Scene(loader.load());
            MainController controller = loader.getController();
            controller.ouvrirBackRepas();

            Stage stage = extractStage(source);
            stage.setTitle("Fitopia Back Office");
            stage.setScene(scene);
            stage.show();
            SceneNavigator.applyWindowMode(stage);
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible d'ouvrir le back office.", exception);
        }
    }

    public static void logoutToSignIn(Node source) {
        UserSession.clear();
        openSignIn(source);
    }

    public static void openSignIn(Node source) {
        try {
            FXMLLoader loader = new FXMLLoader(SessionRouter.class.getResource("/SignIn.fxml"));
            Scene scene = new Scene(loader.load(), 1460, 860);
            Stage stage = extractStage(source);
            stage.setTitle("Sign In");
            stage.setScene(scene);
            stage.setMinWidth(1280);
            stage.setMinHeight(760);
            stage.show();
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible d'ouvrir la page de connexion.", exception);
        }
    }

    public static boolean ensureAuthenticated(Node source) {
        if (hasAuthenticatedUser()) {
            return true;
        }

        showAccessAlert("Veuillez vous connecter pour acceder a cette page.");
        openSignIn(source);
        return false;
    }

    public static boolean ensureAdmin(Node source) {
        if (!ensureAuthenticated(source)) {
            return false;
        }
        if (isAdminSession()) {
            return true;
        }

        showAccessAlert("Acces reserve aux administrateurs.");
        openFrontHome(source);
        return false;
    }

    private static Stage extractStage(Node source) {
        return (Stage) source.getScene().getWindow();
    }

    private static void showAccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Acces refuse");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
