package controller;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.control.TextField;
import model.UserModel;
import services.UserService;


public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Hyperlink signupLink;

    @FXML
    private StackPane imagePane;

    @FXML
    private ImageView bgImage;

    @FXML
    private HBox rootHBox;

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

        boolean authenticated = userService.authenticateUser(username, password);
        if (authenticated) {
            statusLabel.setVisible(true);
            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setText("Login successful");

            // navigate(dashboard);
        } else {
            statusLabel.setVisible(true);
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Invalid username or password");
        }
    }

    @FXML
    private void initialize() {
        bgImage.fitWidthProperty().bind(imagePane.widthProperty());
        bgImage.fitHeightProperty().bind(imagePane.heightProperty());

        userService = new UserService();
        loginButton.setDisable(true);
        statusLabel.setVisible(false);

        usernameField.textProperty().addListener((observable, oldValue, newValue) -> checkFields());

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> checkFields());

        UserModel user = new UserModel("test", "first", "testuser", "abcd");
        userService.addUser(user);
    }

    @FXML
    private void handleSignUp(ActionEvent event) {

    }

}
