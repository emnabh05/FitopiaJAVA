package tn.esprit.Pidev3A49.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import tn.esprit.Pidev3A49.utils.SceneNavigator;

import java.io.IOException;

public class MonthlyRankingController {

    public void openProgressTracker(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.RANKING_VIEW, SceneNavigator.PROGRESS_VIEW);
    }

    public void openSupplementShowcase(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.RANKING_VIEW, SceneNavigator.FRONT_END_VIEW);
    }

    @FXML
    public void openBackEnd(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.RANKING_VIEW, SceneNavigator.BACK_END_VIEW);
    }

    @FXML
    public void goBackOrExit(ActionEvent event) throws IOException {
        SceneNavigator.goBackOrClose(event);
    }
}
