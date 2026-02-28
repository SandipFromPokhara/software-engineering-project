package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.concurrent.Task;

import entity.UserEntity;
import dao.user.JpaUserDao;
import javafx.stage.Stage;
import security.BcryptPasswordHasher;
import security.PasswordHasher;
import services.UserService;
import util.NavigationUtil;
import session.UserSession;

public class LoginController {

    private UserService userService;
    private JpaUserDao userDao;
    private PasswordHasher passwordHasher;

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
    private Button backButton;

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        NavigationUtil.replaceScene(stage, "/FXML/entry.fxml", "Welcome to NoteVault", false);
    }

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

        Task<UserEntity> loginTask = new Task<>() {
            @Override
            protected UserEntity call() throws Exception {
                return userService.login(username, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            UserEntity authenticatedUser = loginTask.getValue();
            if (authenticatedUser != null) {
                UserSession.getUserInstance().setUser(authenticatedUser);
                Stage stage = (Stage) loginButton.getScene().getWindow();
                NavigationUtil.replaceScene(stage, "/FXML/view_dashboard.fxml", "User Dashboard", true);
            } else {
                loginButton.setDisable(false);
                statusLabel.setTextFill(Color.RED);
                statusLabel.setText("Invalid username or password");
                statusLabel.setVisible(true);
            }
        });
        new Thread(loginTask).start();
    }

    @FXML
    private void initialize() {
        userDao = new JpaUserDao();
        passwordHasher = new BcryptPasswordHasher();
        userService = new UserService(userDao, passwordHasher);
        loginButton.setDisable(true);
        statusLabel.setVisible(false);

        usernameField.textProperty().addListener((observable, oldValue, newValue) -> checkFields());

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> checkFields());

        usernameField.setOnAction(this::handleLogin);
        passwordField.setOnAction(this::handleLogin);
    }

    @FXML
    private void handleSignUp() {
        Stage stage = (Stage) signupLink.getScene().getWindow();
        NavigationUtil.replaceScene(stage, "/FXML/signup.fxml", "NoteVault - Register", false);
    }
}