package controller;

import javafx.scene.control.*;
import javafx.fxml.FXML;
import model.Note;

public class LoginController {

    @FXML private TextField usernameField;

    @FXML private PasswordField passwordField;

    @FXML private Button loginButton;

    @FXML private Label errorLabel;

    @FXML private Hyperlink signupLink;

    private void handleLogin() {

    }

    private void initialize() {
        loginButton.setDisable(true);
        errorLabel.setDisable(true);

    }
}
