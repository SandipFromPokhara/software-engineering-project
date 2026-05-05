package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Tooltip;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
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
import util.TooltipUtil;

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
    private Label usernameLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private Label passwordLabel;

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

    private static final String FOCUS_CLASS = "focus";

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Helper to bind a label's text property to the localization key if the label is present.
     */
    private void bindLabel(Label label, String key) {
        if (label != null) {
            label.textProperty().bind(Localization.bind(key));
        }
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
        setupInitialState();
        setupLocalization();
        setupBackButton();
        setupFieldListeners();
        setupActions();
    }

    private void setupInitialState() {
        if (loginButton != null) loginButton.setDisable(true);
        if (statusLabel != null) statusLabel.setVisible(false);
    }

    private void setupLocalization() {
        bindLabel(loginWelcome, "login.welcome_label");
        bindLabel(loginNote, "login.note_label");
        bindLabel(usernameLabel, "login.placeholder_name");
        if (usernameField != null) usernameField.promptTextProperty().bind(Localization.bind("login.placeholder_name"));
        bindLabel(passwordLabel, "login.placeholder_password");
        if (passwordField != null) passwordField.promptTextProperty().bind(Localization.bind("login.placeholder_password"));
        if (loginButton != null) loginButton.textProperty().bind(Localization.bind("login.button"));
        bindLabel(loginNoAccount, "login.noAccount_label");
        if (signupLink != null) signupLink.textProperty().bind(Localization.bind("login.signup"));
        bindLabel(privacyLabel, "entry.privacy");
    }

    private void setupBackButton() {
        if (backButton == null) return;

        backButton.getStyleClass().add("back-button");
        Tooltip backTip = TooltipUtil.createLocalizedTooltip("login.back");
        TooltipUtil.setTooltipDelay(backTip);
        backButton.setTooltip(backTip);

        // Add Ikonli FontIcon if available
        try {
            FontIcon icon = new FontIcon("fa-chevron-left");
            icon.getStyleClass().add("back-icon");
            backButton.setGraphic(icon);
        } catch (Exception ignored) {
            // If ikonli is not available, fall back to text-only button.
        }
    }

    private void setupFieldListeners() {
        setupFieldListener(usernameField);
        setupFieldListener(passwordField);
    }

    private void setupFieldListener(TextInputControl field) {
        if (field == null) return;

        field.textProperty().addListener((o, oldV, newV) -> checkFields());
        field.focusedProperty().addListener((o, oldV, newV) -> {
            boolean focused = Boolean.TRUE.equals(newV);
            if (focused) {
                if (!field.getStyleClass().contains(FOCUS_CLASS)) field.getStyleClass().add(FOCUS_CLASS);
            } else {
                field.getStyleClass().removeIf(s -> s.equals(FOCUS_CLASS));
            }
        });
    }

    private void setupActions() {
        if (usernameField != null) usernameField.setOnAction(this::handleLogin);
        if (passwordField != null) passwordField.setOnAction(this::handleLogin);
        if (loginButton != null) loginButton.setOnAction(this::handleLogin);
    }

    @FXML
    private void handleBack() {
        NavigationUtil.replaceScene(resolveStage(), "/FXML/entry.fxml", "entry.window_title", false);
    }

    private String getUsername() {
        return usernameField == null ? "" : usernameField.getText().trim();
    }

    private String getPassword() {
        return passwordField == null ? "" : passwordField.getText().trim();
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

                // Feedback for successful login while the dashboard FXML is loading.
                statusLabel.setTextFill(Color.GREEN);
                statusLabel.setText(Localization.get("login.success"));
                statusLabel.setVisible(true);

                // Prevent further input while navigation is in progress
                usernameField.setDisable(true);
                passwordField.setDisable(true);
                loginButton.setDisable(true);
                signupLink.setDisable(true);
                backButton.setDisable(true);

                // Pause UI to render the success message before navigation
                PauseTransition pause = new PauseTransition(Duration.millis(1000));
                pause.setOnFinished(ev -> NavigationUtil.replaceScene(resolveStage(), "/FXML/view_dashboard.fxml", "dashboard.window_title", true));
                pause.play();
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
        NavigationUtil.replaceScene(resolveStage(), "/FXML/signup.fxml", "register.window_title", false);
    }

    /* Resolve a Stage for navigation. Prefer the controller's stage field if set,
      otherwise try to obtain the Stage from a known control (loginButton or usernameField).
     */
    private Stage resolveStage() {
        if (this.stage != null) return this.stage;

        if (loginButton != null && loginButton.getScene() != null) {
            return (Stage) loginButton.getScene().getWindow();
        }

        if (usernameField != null && usernameField.getScene() != null) {
            return (Stage) usernameField.getScene().getWindow();
        }

        if (passwordField != null && passwordField.getScene() != null) {
            return (Stage) passwordField.getScene().getWindow();
        }

        throw new IllegalStateException("Unable to resolve Stage. Make sure to call setStage(stage) in tests or that this controller's controls are attached to a Scene before navigation.");
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
