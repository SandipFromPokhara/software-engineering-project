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
import util.Localization;
import util.NavigationUtil;
import session.UserSession;

public class LoginController {

    private UserService userService;
    private JpaUserDao userDao;
    private PasswordHasher passwordHasher;

    @FXML
    private Label loginWelcome;
    @FXML
    private Label loginNote;
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
    private Label loginNoAccount;

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        NavigationUtil.replaceScene(stage, "/FXML/entry.fxml", Localization.get("entry.window_title"), false);
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
                NavigationUtil.replaceScene(stage, "/FXML/view_dashboard.fxml", Localization.get("dashboard.window_title"), true);
            } else {
                loginButton.setDisable(false);
                statusLabel.setTextFill(Color.RED);
                statusLabel.setText(Localization.get("login.invalid"));
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


        // LOCALIZATION BINDINGS
        loginWelcome.textProperty().bind(Localization.bind("login.welcome_label"));
        loginNote.textProperty().bind(Localization.bind("login.note_label"));
        usernameField.promptTextProperty().bind(Localization.bind("login.placeholder_name"));
        passwordField.promptTextProperty().bind(Localization.bind("login.placeholder_password"));
        loginButton.textProperty().bind(Localization.bind("login.button"));
        loginNoAccount.textProperty().bind(Localization.bind("login.noAccount_label"));
        signupLink.textProperty().bind(Localization.bind("login.signup"));
        backButton.textProperty().bind(Localization.bind("login.back"));

        usernameField.textProperty().addListener((o, oldV, newV) -> checkFields());
        passwordField.textProperty().addListener((o, oldV, newV) -> checkFields());

        usernameField.setOnAction(this::handleLogin);
        passwordField.setOnAction(this::handleLogin);

        loginButton.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                stage.titleProperty().bind(Localization.bind("login.window_title"));
            }
        });
    }


    @FXML
    private void handleSignUp() {
        Stage stage = (Stage) signupLink.getScene().getWindow();
        NavigationUtil.replaceScene(stage, "/FXML/signup.fxml", Localization.get("register.window_title"), false);
    }
}