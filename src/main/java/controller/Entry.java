package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import util.NavigationUtil;

//@SuppressWarnings("unused")
public class Entry {

    @FXML
    private Button guestButton;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private void onLogin(ActionEvent event) {
        NavigationUtil.navigateTo(
                event,
                "/FXML/login_view.fxml",
                "login",
                false
        );
    }

    @FXML
    private void onRegister(ActionEvent event) {
        NavigationUtil.navigateTo(
                event,
                "/FXML/signup.fxml",
                "Sign Up",
                false
        );
    }

    @FXML
    private void onContinueAsGuest(ActionEvent event) {
        NavigationUtil.navigateTo(
                event,
                "/FXML/guestDashboard.fxml",
                " Guest Dashboard",
                true
        );
    }

}
