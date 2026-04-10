package tn.esprit.Pidev3A49.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.HBox;
import tn.esprit.Pidev3A49.utils.SceneNavigator;

import java.io.IOException;

public class CheckoutController {

    private static final String PAYMENT_OPTION_BASE_STYLE =
            "-fx-alignment: CENTER_LEFT; " +
            "-fx-spacing: 16; " +
            "-fx-background-radius: 18; " +
            "-fx-border-radius: 18; " +
            "-fx-padding: 18 20 18 20;";

    private static final String PAYMENT_OPTION_DEFAULT_STYLE =
            PAYMENT_OPTION_BASE_STYLE +
            "-fx-background-color: white; " +
            "-fx-border-color: #D5E2EA; " +
            "-fx-border-width: 1;";

    private static final String PAYMENT_OPTION_SELECTED_STYLE =
            PAYMENT_OPTION_BASE_STYLE +
            "-fx-background-color: #E8F7F1; " +
            "-fx-border-color: #118465; " +
            "-fx-border-width: 1.4;";

    @FXML
    private HBox visaOption;

    @FXML
    private HBox mastercardOption;

    @FXML
    private HBox paypalOption;

    @FXML
    private HBox cashOnDeliveryOption;

    @FXML
    private RadioButton visaRadio;

    @FXML
    private RadioButton mastercardRadio;

    @FXML
    private RadioButton paypalRadio;

    @FXML
    private RadioButton cashOnDeliveryRadio;

    @FXML
    private void initialize() {
        updatePaymentStyles();
    }

    public void openSupplementShowcase(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.CHECKOUT_VIEW, SceneNavigator.FRONT_END_VIEW);
    }

    public void openBackEnd(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.CHECKOUT_VIEW, SceneNavigator.BACK_END_VIEW);
    }

    public void goBackOrExit(ActionEvent event) throws IOException {
        SceneNavigator.goBackOrClose(event);
    }

    @FXML
    private void updatePaymentStyles() {
        applyPaymentStyle(visaOption, visaRadio);
        applyPaymentStyle(mastercardOption, mastercardRadio);
        applyPaymentStyle(paypalOption, paypalRadio);
        applyPaymentStyle(cashOnDeliveryOption, cashOnDeliveryRadio);
    }

    private void applyPaymentStyle(HBox option, RadioButton radioButton) {
        option.setStyle(radioButton.isSelected() ? PAYMENT_OPTION_SELECTED_STYLE : PAYMENT_OPTION_DEFAULT_STYLE);
    }
}
