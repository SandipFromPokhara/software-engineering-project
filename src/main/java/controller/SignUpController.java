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
import javafx.scene.control.*;
import org.kordamp.ikonli.javafx.FontIcon;
import util.ShowMessageUtil;
import util.TooltipUtil;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SignUpController {

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

    private static final String ERROR_CLASS = "input-error";
    private static final String FOCUS_CLASS = "focus";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String USERNAME = "username";
    private static final String EMAIL = "email";

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

        bindTexts();
        setupBackButton();
        attachFocusHandlers();

        setupPasswordStrengthUI();
        setupRealtimeValidation();

        signUpButton.setOnAction(e -> handleSignUp());
        signUpButton.setDisable(true);
    }

    private void bindTexts() {
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
        privacyLabel.textProperty().bind(Localization.bind("entry.privacy"));
    }

    private void setupBackButton() {
        backButton.getStyleClass().add("back-button");
        Tooltip tip = TooltipUtil.createLocalizedTooltip("signup.back");
        TooltipUtil.setTooltipDelay(tip);
        backButton.setTooltip(tip);

        try {
            FontIcon icon = new FontIcon("fa-chevron-left");
            icon.getStyleClass().add("back-icon");
            backButton.setGraphic(icon);
        } catch (Exception ignored) {
            //fallback to text-only if icon fails to load
        }
    }

    private void attachFocusHandlers() {
        attachFocusHandling(firstNameField, () -> firstNameTouched = true);
        attachFocusHandling(lastNameField, () -> lastNameTouched = true);
        attachFocusHandling(usernameField, () -> usernameTouched = true);
        attachFocusHandling(emailField, () -> emailTouched = true);
        attachFocusHandling(passwordField, () -> passwordTouched = true);
        attachFocusHandling(confirmPasswordField, () -> confirmPasswordTouched = true);
    }

    private void setupPasswordStrengthUI() {
        passwordStrengthBar.setVisible(false);
        passwordStrengthBar.setManaged(false);
        passwordStrengthBar.setId("passwordStrengthBar");

        passwordStrengthLabel.setVisible(false);
        passwordStrengthLabel.setManaged(false);
    }

    // =========================
    // REDUCED DUPLICATION
    // =========================

    private Validation.ValidationResult validateForm() {
        return Validation.validateSignup(
                safe(firstNameField),
                safe(lastNameField),
                safe(usernameField),
                safe(emailField),
                passwordField.getText(),
                confirmPasswordField.getText()
        );
    }

    private void addValidationListener(TextField field, Runnable validator) {
        field.textProperty().addListener((obs, o, n) -> validator.run());
    }

    private void setupRealtimeValidation() {
        Runnable validator = () -> {
            if (skipValidation) return;
            var result = validateForm();
            showValidationErrors(result);
            updateSignUpButtonState(result);
        };

        addValidationListener(firstNameField, validator);
        addValidationListener(lastNameField, validator);
        addValidationListener(usernameField, validator);
        addValidationListener(emailField, validator);

        passwordField.textProperty().addListener((obs, o, n) -> {
            updatePasswordStrength(n);
            validator.run();
        });

        confirmPasswordField.textProperty().addListener((obs, o, n) -> validator.run());
    }

    private Stage getStage(Control c) {
        if (c == null || c.getScene() == null || c.getScene().getWindow() == null) return null;
        return (Stage) c.getScene().getWindow();
    }

    // =========================

    private String safe(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private void handleSignUp() {
        var result = validateForm();
        showValidationErrors(result);
        if (!result.success()) return;

        try {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();

            if (userDAO.findByUsername(username) != null) {
                ShowMessageUtil.showMessageKey(messageLabel, "signup.username_taken", MessageType.ERROR);
                return;
            }

            if (userDAO.findByEmail(email) != null) {
                ShowMessageUtil.showMessageKey(messageLabel, "signup.email_exists", MessageType.ERROR);
                return;
            }

            UserEntity user = new UserEntity(
                    safe(firstNameField),
                    safe(lastNameField),
                    username,
                    email
            );

            user.changePasswordHash(passwordHasher.hash(passwordField.getText()));

            UserEntity saved = userDAO.save(user);

            if (saved != null && saved.getId() != null) {
                ShowMessageUtil.showMessageKey(messageLabel, "signup.success", MessageType.SUCCESS);
                skipValidation = true;
                clearFields();

                Stage stage = getStage(signUpButton);
                if (stage == null) return;

                PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
                delay.setOnFinished(e -> {
                    skipValidation = false;
                    NavigationUtil.replaceScene(stage, "/FXML/login_view.fxml", "login.window_title", false);
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
            if (handleFieldErrors(entry.getKey(), entry.getValue())) return;
        }
    }

    private void resetStyles() {
        List.of(
                firstNameField,
                lastNameField,
                usernameField,
                emailField,
                passwordField,
                confirmPasswordField
        ).forEach(this::removeErrorStyle);
    }

    private boolean handleFieldErrors(String field, List<?> errors) {
        if (errors == null || errors.isEmpty()) return false;

        Object raw = errors.get(0);
        if (!(raw instanceof IValidationError error)) return false;
        if (error.key() == null || error.key().isBlank()) return false;

        Control control = getControlForField(field);
        if (control != null && isFieldTouched(field)) {
            addErrorStyle(control);
        }

        ShowMessageUtil.showMessage(
                messageLabel,
                Localization.get(error.key(), error.args()),
                MessageType.ERROR
        );

        return true;
    }

    private Control getControlForField(String field) {
        return switch (field) {
            case FIRST_NAME -> firstNameField;
            case LAST_NAME -> lastNameField;
            case USERNAME -> usernameField;
            case EMAIL -> emailField;
            case "password" -> passwordField;
            case "confirmPassword" -> confirmPasswordField;
            default -> null;
        };
    }

    private boolean isFieldTouched(String field) {
        return switch (field) {
            case FIRST_NAME -> firstNameTouched;
            case LAST_NAME -> lastNameTouched;
            case USERNAME -> usernameTouched;
            case EMAIL -> emailTouched;
            case "password" -> passwordTouched;
            case "confirmPassword" -> confirmPasswordTouched;
            default -> true;
        };
    }

    private void attachFocusHandling(TextInputControl control, Runnable markTouched) {
        control.focusedProperty().addListener((obs, oldV, newV) -> {
            boolean focused = Boolean.TRUE.equals(newV);
            if (!focused) markTouched.run();

            if (focused) {
                if (!control.getStyleClass().contains(FOCUS_CLASS)) {
                    control.getStyleClass().add(FOCUS_CLASS);
                }
            } else {
                control.getStyleClass().removeIf(s -> s.equals(FOCUS_CLASS));
            }
        });
    }

    private void addErrorStyle(Control c) {
        if (!c.getStyleClass().contains(ERROR_CLASS)) c.getStyleClass().add(ERROR_CLASS);
    }

    private void removeErrorStyle(Control c) {
        c.getStyleClass().removeIf(s -> s.equals(ERROR_CLASS));
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
        if (password.chars().anyMatch(Character::isUpperCase)) score++;
        if (password.chars().anyMatch(Character::isLowerCase)) score++;
        if (password.chars().anyMatch(Character::isDigit)) score++;
        if (password.chars().anyMatch(c -> "!@#$%^&*()_+=\\-[]{};':\"\\|,.<>/?".indexOf(c) >= 0)) score++;

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
        List.of(
                firstNameField,
                lastNameField,
                usernameField,
                emailField,
                passwordField,
                confirmPasswordField
        ).forEach(TextInputControl::clear);

        firstNameTouched = false;
        lastNameTouched = false;
        usernameTouched = false;
        emailTouched = false;
        passwordTouched = false;
        confirmPasswordTouched = false;
    }

    @FXML
    public void onLogin() {
        ShowMessageUtil.hideMessage(messageLabel);
        Stage stage = getStage(loginLink);
        if (stage != null) {
            NavigationUtil.replaceScene(stage, "/FXML/login_view.fxml", "login.window_title", false);
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = getStage(backButton);
        if (stage != null) {
            NavigationUtil.replaceScene(stage, "/FXML/entry.fxml", "entry.window_title", false);
        }
    }

    public void setUserDAO(IUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void setPasswordHasher(IPasswordHasher hasher) {
        this.passwordHasher = hasher;
    }
}