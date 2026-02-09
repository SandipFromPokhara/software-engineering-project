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
        NavigationUtil.navigateTo(
                event,
                "/FXML/login_view.fxml",
                "NoteVault - LogIn",
                false);
    }

    @FXML
    private void onRegister(ActionEvent event) {
        NavigationUtil.navigateTo(
                event,
                "/FXML/signup.fxml",
                "NoteVault - Register",false);
    }

    @FXML
    private void onContinueAsGuest(ActionEvent event) {
        NavigationUtil.navigateTo(
                event,
                "/FXML/guestDashboard.fxml",
                "NoteVault - Guest Mode", true);
    }
}