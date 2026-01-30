package controller;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.fxml.FXML;

import javafx.scene.image.ImageView;
import model.User;
import repository.UserRepository;

public class LoginController {

    public ImageView leftImage;

    public ImageView logo;

    private UserRepository userRepository;

    @FXML private TextField usernameField;

    @FXML private PasswordField passwordField;

    @FXML private Button loginButton;

    @FXML private Label errorLabel;

    @FXML private Hyperlink signupLink;

    private String getUsername() {
        return usernameField.getText().trim();
    }

    private String getPassword() {
        return passwordField.getText().trim();
    }

    private void checkFields() {
        String username = getUsername();
        String password = getPassword();
        loginButton.setDisable(username.isEmpty() || password.isEmpty());
    }

    @FXML
    private void handleLogin() {
        String username = getUsername();
        String password = getPassword();

        boolean authenticated = userRepository.authenticateUser(username, password);
        if (authenticated) {
            errorLabel.setVisible(true);
            errorLabel.setText("Login successful");

            // navigate(dashboard);
        } else {
            errorLabel.setVisible(true);
            errorLabel.setText("Invalid username or password");
        }
    }

    @FXML
    private void initialize() {
        userRepository = new UserRepository();
        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        usernameField.textProperty().addListener((observable, oldValue, newValue) -> checkFields());

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> checkFields());

        User user = new User("test", "first", "testuser", "abcd");
        userRepository.addUser(user);
    }

    @FXML
    private void handleSignUp(ActionEvent event) {

    }

}
