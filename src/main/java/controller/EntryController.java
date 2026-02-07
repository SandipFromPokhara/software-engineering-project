package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import util.NavigationUtil;

public class EntryController {

    @FXML
    private Button guestButton;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private void onLogin(ActionEvent event) {
        NavigationUtil.navigateTo(event, "/FXML/login_view.fxml", "Login - NoteVault", false);
    }

    @FXML
    private void onRegister(ActionEvent event) {
        NavigationUtil.navigateTo(event, "/FXML/signup.fxml", "Sign Up - Note Vault",false);
    }

    @FXML
    private void onContinueAsGuest(ActionEvent event) {
        NavigationUtil.navigateTo(event, "/FXML/guestDashboard.fxml", "NoteVault - Create Note", true);
    }
}