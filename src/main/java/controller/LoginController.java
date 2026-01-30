package controller;

import javafx.scene.control.*;
import javafx.fxml.FXML;

import repository.UserRepository;

public class LoginController {

    private UserRepository userRepository;

    @FXML private TextField usernameField;

    @FXML private PasswordField passwordField;

    @FXML private Button loginButton;

    @FXML private Label errorLabel;

    @FXML private Hyperlink signupLink;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Invalid username or password");
            return;
        }

        boolean authenticated = userRepository.authenticateUser(username, password);
        if (authenticated) {
            errorLabel.setText("");
        } else {
            errorLabel.setText("Invalid username or password");
        }
    }

    @FXML
    private void initialize() {
        userRepository = new UserRepository();
        loginButton.setDisable(true);
        errorLabel.setVisible(false);

    }
}
