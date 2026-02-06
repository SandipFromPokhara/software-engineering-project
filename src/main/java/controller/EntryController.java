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
        System.out.println("Login button clicked - navigating to Login screen");
        NavigationUtil.navigateTo(event, "/FXML/login_view.fxml", "Login - NoteVault");
    }

    @FXML
    private void onRegister(ActionEvent event) {
        System.out.println("Register button clicked - navigating to Signup screen");
        NavigationUtil.navigateTo(event, "/FXML/signup.fxml", "Sign Up - Note Vault");
    }

    @FXML
    private void onContinueAsGuest(ActionEvent event) {
        System.out.println("Continue as guest clicked - navigating to Create Note");
        NavigationUtil.navigateTo(event, "/FXML/create_note.fxml", "NoteVault - Create Note");
    }
}
