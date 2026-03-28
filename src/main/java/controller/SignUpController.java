package controller;

import dao.user.UserDAO;
import dao.user.JpaUserDao;
import entity.UserEntity;
import javafx.animation.PauseTransition;
import javafx.stage.Stage;
import javafx.util.Duration;
import security.BcryptPasswordHasher;
import security.MessageType;
import security.PasswordHasher;
import util.Localization;
import util.NavigationUtil;
import security.Validation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import util.ShowMessageUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SignUpController {

    private static final Logger logger = Logger.getLogger(SignUpController.class.getName());
    private PasswordHasher passwordHasher;
    private boolean skipValidation = false;
    private UserDAO userDAO;
    private PauseTransition strengthHideDelay;

    private boolean firstNameTouched = false;
    private boolean lastNameTouched = false;
    private boolean usernameTouched = false;
    private boolean emailTouched = false;
    private boolean passwordTouched = false;
    private boolean confirmPasswordTouched = false;

    @FXML
    private TextField firstNameField, lastNameField, usernameField, emailField;

    @FXML
    private PasswordField passwordField, confirmPasswordField;

    @FXML
    private Button signUpButton, backButton;

    @FXML
    private Hyperlink loginLink;

    @FXML
    private Label messageLabel, createAccount, joinAccount, haveAccount;

    @FXML
    private Label privacyLabel, passwordStrengthLabel;

    @FXML
    private ProgressBar passwordStrengthBar;

    @FXML
    public void initialize() {
        userDAO = new JpaUserDao();
        passwordHasher = new BcryptPasswordHasher();

        // LOCALIZATION
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
        privacyLabel.textProperty().bind(Localization.bind("entry.privacy"));// Text from the left image

        firstNameField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) firstNameTouched = true;
        });

        lastNameField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) lastNameTouched = true;
        });

        usernameField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) usernameTouched = true;
        });

        emailField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) emailTouched = true;
        });

        passwordField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) passwordTouched = true;
        });

        confirmPasswordField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) confirmPasswordTouched = true;
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

    // Real-Time validation
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

        // Final validation
        Validation.ValidationResult result = Validation.validateSignup(
                safe(firstNameField),
                safe(lastNameField),
                safe(usernameField),
                safe(emailField),
                passwordField.getText(),
                confirmPasswordField.getText()
        );

        showValidationErrors(result);

        if (!result.success()) {
            return;
        }

        try {
            UserEntity existingUserByUsername = userDAO.findByUsername(username);
            if (existingUserByUsername != null) {
                ShowMessageUtil.showMessageKey(messageLabel, "signup.username_taken", MessageType.ERROR);
                return;
            }

            UserEntity existingUserByEmail = userDAO.findByEmail(email);
            if (existingUserByEmail != null) {
                ShowMessageUtil.showMessageKey(messageLabel, "signup.email_exists", MessageType.ERROR);
                return;
            }

            String hashedPassword = passwordHasher.hash(password);

            UserEntity newUser = new UserEntity(firstName, lastName, username, email);
            newUser.changePasswordHash(hashedPassword);

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
            String field = entry.getKey();

            if (!entry.getValue().isEmpty()) {

                switch (field) {
                    case "firstName" -> { if (firstNameTouched) firstNameField.setStyle("-fx-border-color: #e74c3c;"); }
                    case "lastName" -> { if (lastNameTouched) lastNameField.setStyle("-fx-border-color: #e74c3c;"); }
                    case "username" -> { if (usernameTouched) usernameField.setStyle("-fx-border-color: #e74c3c;"); }
                    case "email" -> { if (emailTouched) emailField.setStyle("-fx-border-color: #e74c3c;");  }
                    case "password" -> { if (passwordTouched) passwordField.setStyle("-fx-border-color: #e74c3c;"); }
                    case "confirmPassword" -> { if (confirmPasswordTouched) confirmPasswordField.setStyle("-fx-border-color: #e74c3c;"); }
                }

                var firstError = entry.getValue().get(0);
                String key = firstError.key();

                if (key == null || key.isBlank()) {
                    logger.warning("Validation returned empty i18n key");
                    return;
                }

                String message = Localization.get(key, firstError.args().toArray());

                ShowMessageUtil.showMessage(messageLabel, message, MessageType.ERROR);
                return;
            }
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
        if (strengthHideDelay != null) {
            strengthHideDelay.stop();
        }

        if (password == null || password.isEmpty()) {
            passwordStrengthBar.setVisible(false);
            passwordStrengthBar.setManaged(false);

            passwordStrengthLabel.setVisible(false);
            passwordStrengthLabel.setManaged(false);

            passwordStrengthBar.setProgress(0);
            passwordStrengthLabel.setText("");
            return;
        }

        passwordStrengthBar.setVisible(true);
        passwordStrengthBar.setManaged(true);

        passwordStrengthLabel.setVisible(true);
        passwordStrengthLabel.setManaged(true);

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
            strengthHideDelay.setOnFinished(e -> {
                passwordStrengthBar.setVisible(false);
                passwordStrengthBar.setManaged(false);

                passwordStrengthLabel.setVisible(false);
                passwordStrengthLabel.setManaged(false);
            });
            strengthHideDelay.play();
        }
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

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void setPasswordHasher(PasswordHasher hasher) {
        this.passwordHasher = hasher;
    }
}