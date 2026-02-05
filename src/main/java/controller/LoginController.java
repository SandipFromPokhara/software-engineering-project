package controller;

import dao.user.JpaUserDao;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.control.TextField;
import services.UserService;
import util.NavigationUtil;

public class LoginController {

    private UserService userService;
    private JpaUserDao userDao = new JpaUserDao();

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
    private void handleLogin(ActionEvent event) {
        String username = getUsername();
        String password = getPassword();

        loginButton.setDisable(true);

        Task<Boolean> loginTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return userService.login(username, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            boolean authenticated = loginTask.getValue();
            if (authenticated) {
                statusLabel.setTextFill(Color.GREEN);
                statusLabel.setText("Login successful");
                NavigationUtil.navigateTo(event, "/FXML/view_dashboard.fxml", "User Dashboard", true);
            } else {
                statusLabel.setTextFill(Color.RED);
                statusLabel.setText("Invalid username or password");
            }
            statusLabel.setVisible(true);
        });
        new Thread(loginTask).start();
    }

    @FXML
    private void initialize() {
        userService = new UserService(userDao);
        loginButton.setDisable(true);
        statusLabel.setVisible(false);

        usernameField.textProperty().addListener((observable, oldValue, newValue) -> checkFields());

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> checkFields());
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        NavigationUtil.navigateTo(event, "/FXML/signup.fxml", "SignUp window", false);
    }
}
