package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.concurrent.Task;

import entity.entities.UserEntity;
import dao.user.JpaUserDao;
import javafx.stage.Stage;
import security.BcryptPasswordHasher;
import security.IPasswordHasher;
import services.UserService;
import util.Localization;
import util.NavigationUtil;
import session.UserSession;

public class LoginController {

    @FXML
    private Label loginWelcome;

    @FXML
    private Label loginNote;

    @FXML
    private Label statusLabel;

    @FXML
    private Label loginNoAccount;

    @FXML
    private Label privacyLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button backButton;

    @FXML
    private Hyperlink signupLink;

    private UserService userService;

    private Stage stage;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private UserService getUserService() {
        if (userService == null) {
            JpaUserDao userDao = new JpaUserDao();
            IPasswordHasher passwordHasher = new BcryptPasswordHasher();
            userService = new UserService(userDao, passwordHasher);
        }
        return userService;
    }

    @FXML
    void initialize() {
        initView();
    }

    void initView() {
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
        privacyLabel.textProperty().bind(Localization.bind("entry.privacy"));

        usernameField.textProperty().addListener((o, oldV, newV) -> checkFields());
        passwordField.textProperty().addListener((o, oldV, newV) -> checkFields());

        usernameField.setOnAction(this::handleLogin);
        passwordField.setOnAction(this::handleLogin);
        loginButton.setOnAction(this::handleLogin);
    }

    @FXML
    private void handleBack() {
        NavigationUtil.replaceScene(stage, "/FXML/entry.fxml", "entry.window_title", false);
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
    void handleLogin(ActionEvent event) {
        String username = getUsername();
        String password = getPassword();

        loginButton.setDisable(true);

        UserService service = getUserService();

        Task<UserEntity> loginTask = new Task<>() {
            @Override
            protected UserEntity call() {
                return service.login(username, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            UserEntity authenticatedUser = loginTask.getValue();

            if (authenticatedUser != null) {
                UserSession.getUserInstance().setUser(authenticatedUser);
                NavigationUtil.replaceScene(stage, "/FXML/view_dashboard.fxml", "dashboard.window_title", true);
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
    private void handleSignUp() {
        NavigationUtil.replaceScene(stage, "/FXML/signup.fxml", "register.window_title", false);
    }

    // setters for package-private fields required for testing
    void setUsernameField(TextField field) { this.usernameField = field; }
    void setPasswordField(PasswordField field) { this.passwordField = field; }
    void setLoginButton(Button button) { this.loginButton = button; }
    void setStatusLabel(Label label) { this.statusLabel = label; }
    void setSignupLink(Hyperlink link) { this.signupLink = link; }
    void setBackButton(Button button) { this.backButton = button; }
    void setLoginWelcome(Label label) { this.loginWelcome = label; }
    void setLoginNote(Label label) { this.loginNote = label; }
    void setLoginNoAccount(Label label) { this.loginNoAccount = label; }
    void setPrivacyLabel(Label label) { this.privacyLabel = label; }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
