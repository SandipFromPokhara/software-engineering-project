package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

@SuppressWarnings("unused")
public class Entry {

    @FXML
    private Button guestButton;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private void onLogin() {
        System.out.println("Login button clicked");
        // TODO: navigate to login screen
    }

    @FXML
    private void onRegister() {
        System.out.println("Register button clicked");
        // TODO: navigate to register screen
    }

    @FXML
    private void onContinueAsGuest() {
        System.out.println("Continue as guest clicked");
        // TODO: proceed as guest
    }

}
