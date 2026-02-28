package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import util.NavigationUtil;

public class EntryController {

    @FXML
    private Button guestButton;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private void onLogin() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        NavigationUtil.replaceScene(
                stage,
                "/FXML/login_view.fxml",
                "NoteVault - LogIn",
                false);
    }

    @FXML
    private void onRegister() {
        Stage stage = (Stage) registerButton.getScene().getWindow();
        NavigationUtil.replaceScene(
                stage,
                "/FXML/signup.fxml",
                "NoteVault - Register",false);
    }

    @FXML
    private void onContinueAsGuest() {
        Stage stage = (Stage) guestButton.getScene().getWindow();
        NavigationUtil.replaceScene(
                stage,
                "/FXML/guestDashboard.fxml",
                "NoteVault - Guest Mode", true);
    }
}