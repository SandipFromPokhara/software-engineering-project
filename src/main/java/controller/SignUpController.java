package controller;

import dao.user.IUserDAO;
import dao.user.JpaUserDao;
import entity.entities.UserEntity;
import javafx.animation.PauseTransition;
import javafx.stage.Stage;
import javafx.util.Duration;
import security.BcryptPasswordHasher;
import security.MessageType;
import security.IPasswordHasher;
import util.Localization;
import util.NavigationUtil;
import security.Validation;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import util.ShowMessageUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SignUpController {

    private static final String BORDER_ERROR_STYLE = "-fx-border-color: #e74c3c;";

    private static final Logger logger = Logger.getLogger(SignUpController.class.getName());

    private IPasswordHasher passwordHasher;
    private boolean skipValidation = false;
    private IUserDAO userDAO;
    private PauseTransition strengthHideDelay;

    private boolean firstNameTouched = false;
    private boolean lastNameTouched = false;
    private boolean usernameTouched = false;
    private boolean emailTouched = false;
    private boolean passwordTouched = false;
    private boolean confirmPasswordTouched = false;


    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;

    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Button signUpButton;
    @FXML private Button backButton;

    @FXML private Hyperlink loginLink;

    @FXML private Label messageLabel;
    @FXML private Label createAccount;
    @FXML private Label joinAccount;
    @FXML private Label haveAccount;
    @FXML private Label privacyLabel;
    @FXML private Label passwordStrengthLabel;

    @FXML private ProgressBar passwordStrengthBar;

    @FXML
    public void initialize() {
        userDAO = new JpaUserDao();
        passwordHasher = new BcryptPasswordHasher();

        createAccount.textProperty().bind(Localization.bind("signup.createAccount"));
        joinAccount.textProperty().bind(Localization.bind("signup.joinAccount"));

        firstNameField.promptTextProperty().bind(Localization.bind("signup.placeholder_firstname"));
        lastNameField.promptTextProperty().bind(Localization.bind("signup.placeholder_lastname"));
        usernameField.promptTextProperty().bind(Localization.bind("signup.placeholder_username"));
        emailField.promptTextProperty().bind(Localization.bind("signup.placeholder_email"));
        passwordField.promptTextProperty().bind(Localization.bind("signup.placeholder_password"));
        confirmPasswordField.promptTextProperty().bind(Localization.bind("signup.placeholder_confirm_password"));

        signUpButton.textProperty().bind(Localization.bind("signup.button"));
        haveAccount.textProperty().bind(Localization.bind("signup.haveAccount"));
        loginLink.textProperty().bind(Localization.bind("signup.login"));
        backButton.textProperty().bind(Localization.bind("signup.back"));
        privacyLabel.textProperty().bind(Localization.bind("entry.privacy"));

        firstNameField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (Boolean.FALSE.equals(newV)) firstNameTouched = true;
        });
        lastNameField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (Boolean.FALSE.equals(newV)) lastNameTouched = true;
        });
        usernameField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (Boolean.FALSE.equals(newV)) usernameTouched = true;
        });
        emailField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (Boolean.FALSE.equals(newV)) emailTouched = true;
        });
        passwordField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (Boolean.FALSE.equals(newV)) passwordTouched = true;
        });
        confirmPasswordField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (Boolean.FALSE.equals(newV)) confirmPasswordTouched = true;
        });

        passwordStrengthBar.setVisible(false);
        passwordStrengthBar.setManaged(false);
        passwordStrengthBar.setMinHeight(12);
        passwordStrengthBar.setPrefHeight(12);

        passwordStrengthLabel.setVisible(false);
        passwordStrengthLabel.setManaged(false);

        setupRealtimeValidation();

        signUpButton.setOnAction(event -> handleSignUp());
        signUpButton.setDisable(true);
    }

    private void setupRealtimeValidation() {
        Runnable validator = () -> {
            if (skipValidation) return;
            Validation.ValidationResult result = Validation.validateSignup(
                    safe(firstNameField),
                    safe(lastNameField),
                    safe(usernameField),
                    safe(emailField),
                    passwordField.getText(),
                    confirmPasswordField.getText()
            );
            showValidationErrors(result);
            updateSignUpButtonState(result);
        };

        firstNameField.textProperty().addListener((obs, o, n) -> validator.run());
        lastNameField.textProperty().addListener((obs, o, n) -> validator.run());
        usernameField.textProperty().addListener((obs, o, n) -> validator.run());
        emailField.textProperty().addListener((obs, o, n) -> validator.run());
        passwordField.textProperty().addListener((obs, o, n) -> {
            updatePasswordStrength(n);
            validator.run();
        });
        confirmPasswordField.textProperty().addListener((obs, o, n) -> validator.run());
    }

    private String safe(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private void handleSignUp() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        Validation.ValidationResult result = Validation.validateSignup(
                safe(firstNameField),
                safe(lastNameField),
                safe(usernameField),
                safe(emailField),
                passwordField.getText(),
                confirmPasswordField.getText()
        );

        showValidationErrors(result);
        if (!result.success()) return;

        try {
            if (userDAO.findByUsername(username) != null) {
                ShowMessageUtil.showMessageKey(messageLabel, "signup.username_taken", MessageType.ERROR);
                return;
            }
            if (userDAO.findByEmail(email) != null) {
                ShowMessageUtil.showMessageKey(messageLabel, "signup.email_exists", MessageType.ERROR);
                return;
            }

            UserEntity newUser = new UserEntity(firstName, lastName, username, email);
            newUser.changePasswordHash(passwordHasher.hash(password));

            UserEntity savedUser = userDAO.save(newUser);

            if (savedUser != null && savedUser.getId() != null) {
                ShowMessageUtil.showMessageKey(messageLabel, "signup.success", MessageType.SUCCESS);
                skipValidation = true;
                clearFields();

                Stage currentStage = (Stage) signUpButton.getScene().getWindow();
                PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
                delay.setOnFinished(event -> {
                    skipValidation = false;
                    NavigationUtil.replaceScene(currentStage, "/FXML/login_view.fxml", "login.window_title", false);
                });
                delay.play();
            } else {
                ShowMessageUtil.showMessageKey(messageLabel, "signup.failed", MessageType.ERROR);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Signup error", e);
            ShowMessageUtil.showMessageKey(messageLabel, "signup.unexpected_error", MessageType.ERROR);
        }
    }

    private void updateSignUpButtonState(Validation.ValidationResult result) {
        signUpButton.setDisable(!result.success());
    }

    private void showValidationErrors(Validation.ValidationResult result) {
        ShowMessageUtil.hideMessage(messageLabel);
        resetStyles();

        for (var entry : result.errors().entrySet()) {
            if (entry.getValue().isEmpty()) continue;

            applyErrorStyle(entry.getKey());

            var firstError = entry.getValue().getFirst();
            String key = firstError.key();

            if (key == null || key.isBlank()) {
                logger.warning("Validation returned empty i18n key");
                return;
            }

            ShowMessageUtil.showMessage(
                    messageLabel,
                    Localization.get(key, firstError.args().toArray()),
                    MessageType.ERROR
            );
            return;
        }
    }

    private void applyErrorStyle(String field) {
        switch (field) {
            case String f when ("firstName".equals(f) && firstNameTouched) -> firstNameField.setStyle(BORDER_ERROR_STYLE);
            case String f when ("lastName".equals(f) && lastNameTouched) -> lastNameField.setStyle(BORDER_ERROR_STYLE);
            case String f when ("username".equals(f) && usernameTouched) -> usernameField.setStyle(BORDER_ERROR_STYLE);
            case String f when ("email".equals(f) && emailTouched) -> emailField.setStyle(BORDER_ERROR_STYLE);
            case String f when ("password".equals(f) && passwordTouched) -> passwordField.setStyle(BORDER_ERROR_STYLE);
            case String f when ("confirmPassword".equals(f) && confirmPasswordTouched) -> confirmPasswordField.setStyle(BORDER_ERROR_STYLE);

            case "firstName", "lastName", "username", "email", "password", "confirmPassword" ->
                    // Intentional no-op.
                    logger.log(Level.FINER, "Field not touched yet: {0}", field);

            default -> logger.log(Level.FINE, "Unknown validation field: {0}", field);
        }
    }

    private void resetStyles() {
        firstNameField.setStyle("");
        lastNameField.setStyle("");
        usernameField.setStyle("");
        emailField.setStyle("");
        passwordField.setStyle("");
        confirmPasswordField.setStyle("");
    }

    private void updatePasswordStrength(String password) {
        if (strengthHideDelay != null) strengthHideDelay.stop();

        if (password == null || password.isEmpty()) {
            setStrengthBarVisible(false);
            passwordStrengthBar.setProgress(0);
            passwordStrengthLabel.setText("");
            return;
        }

        setStrengthBarVisible(true);

        int score = 0;
        if (password.length() >= 6) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[!@#$%^&*()_+=\\-\\[\\]{};':\"\\\\|,.<>/?].*")) score++;

        double progress = score / 5.0;
        passwordStrengthBar.setProgress(progress);

        if (progress < 0.4) {
            passwordStrengthLabel.setText(Localization.get("password.weak"));
        } else if (progress < 0.7) {
            passwordStrengthLabel.setText(Localization.get("password.medium"));
        } else {
            passwordStrengthLabel.setText(Localization.get("password.strong"));
            strengthHideDelay = new PauseTransition(Duration.seconds(2.5));
            strengthHideDelay.setOnFinished(e -> setStrengthBarVisible(false));
            strengthHideDelay.play();
        }
    }

    private void setStrengthBarVisible(boolean visible) {
        passwordStrengthBar.setVisible(visible);
        passwordStrengthBar.setManaged(visible);
        passwordStrengthLabel.setVisible(visible);
        passwordStrengthLabel.setManaged(visible);
    }

    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();

        firstNameTouched = false;
        lastNameTouched = false;
        usernameTouched = false;
        emailTouched = false;
        passwordTouched = false;
        confirmPasswordTouched = false;
    }

    private void navigateToLogin() {
        Stage stage = (Stage) loginLink.getScene().getWindow();
        NavigationUtil.replaceScene(stage, "/FXML/login_view.fxml", "login.window_title", false);
    }

    @FXML
    public void onLogin() {
        ShowMessageUtil.hideMessage(messageLabel);
        navigateToLogin();
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        NavigationUtil.replaceScene(stage, "/FXML/entry.fxml", "entry.window_title", false);
    }

    public void setUserDAO(IUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void setPasswordHasher(IPasswordHasher hasher) {
        this.passwordHasher = hasher;
    }
}
