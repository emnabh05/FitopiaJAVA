package tn.esprit.Pidev3A49.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import tn.esprit.Pidev3A49.utils.SceneNavigator;

import java.io.IOException;

public class AdminDashboardController {

    @FXML
    public void openFrontEnd(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.BACK_END_VIEW, SceneNavigator.FRONT_END_VIEW);
    }

    @FXML
    public void goBackOrExit(ActionEvent event) throws IOException {
        SceneNavigator.goBackOrClose(event);
    }
}
