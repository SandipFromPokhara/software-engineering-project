package controller;

import dao.user.JpaUserDao;
import dao.user.UserDAO;
import entity.UserEntity;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import session.UserSession;
import security.BcryptPasswordHasher;
import security.PasswordHasher;
import security.Validation;
import util.Localization;
import util.WindowUtil;

import java.util.Timer;
import java.util.TimerTask;

public class UserDashboardController {

    private UserDAO userDao = new JpaUserDao();
    private PasswordHasher passwordHasher;
    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label confirm;

    @FXML
    private Label messageLabel;
    @FXML
    private Label manageAccount;
    @FXML
    private Label note;
    @FXML
    private Label firstLock;
    @FXML
    private Label lastNameLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label emailLock;
    @FXML
    private Label newPassword;
    @FXML
    private Button manageCancel;

    @FXML
    private Button manageUpdate;

    @FXML
    public void initialize() {
        passwordHasher = new BcryptPasswordHasher();
        Validation.hideMessage(messageLabel);

        // LOCALIZATION BINDINGS
        manageAccount.textProperty().bind(Localization.bind("account.title"));
        note.textProperty().bind(Localization.bind("account.note"));
        confirm.textProperty().bind(Localization.bind("account.confirm_password"));

        firstLock.textProperty().bind(Localization.bind("account.first_name"));
        lastNameLabel.textProperty().bind(Localization.bind("account.last_name"));
        usernameLabel.textProperty().bind(Localization.bind("account.username"));
        emailLock.textProperty().bind(Localization.bind("account.email"));
        newPassword.textProperty().bind(Localization.bind("account.new_password"));
        confirm.textProperty().bind(Localization.bind("account.confirm_password"));
        passwordField.promptTextProperty().bind(Localization.bind("account.password_hint"));
        confirmPasswordField.promptTextProperty().bind(Localization.bind("account.password_repeat"));
        manageCancel.textProperty().bind(Localization.bind("account.cancel"));
        manageUpdate.textProperty().bind(Localization.bind("account.update"));



        // Load user data
        UserEntity currentUser = UserSession.getUserInstance().getUser();
        if (currentUser == null) {
            Validation.showMessage(messageLabel, Localization.get("dashboard.no_session"), Validation.MessageType.ERROR);
            return;
        }

        firstNameField.setText(currentUser.getFirstName());
        lastNameField.setText(currentUser.getLastName());
        usernameField.setText(currentUser.getUsername());
        emailField.setText(currentUser.getEmail());

    }


    @FXML
    private void handleUpdate() {
        UserEntity currentUser = UserSession.getUserInstance().getUser();
        if (currentUser == null) {
            Validation.showMessage(messageLabel,Localization.get("dashboard.no_session") , Validation.MessageType.ERROR);
            return;
        }

        String newLastName = lastNameField.getText() == null ? "" : lastNameField.getText().trim();
        String newUsername = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String newPassword = passwordField.getText() == null ? "" : passwordField.getText();
        String confirmPassword = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();

        if (newLastName.isEmpty() || newUsername.isEmpty()) {
            Validation.showMessage(messageLabel, Localization.get("dashboard.required_fields"), Validation.MessageType.ERROR);
            return;
        }

        if (!Validation.validateName(newLastName, "Last name", messageLabel)) {
            return;
        }

        if (!Validation.validateUsername(newUsername, messageLabel)) {
            return;
        }

        UserEntity existingUserWithUsername = userDao.findByUsername(newUsername);
        if (existingUserWithUsername != null && !existingUserWithUsername.getId().equals(currentUser.getId())) {
            Validation.showMessage(messageLabel, Localization.get("dashboard.username_exists"), Validation.MessageType.ERROR);
            return;
        }

        if (!newPassword.isBlank()) {
            if (!Validation.validatePasswordMatch(newPassword, confirmPassword, messageLabel)) {
                return;
            }
            if (!Validation.validatePasswordStrength(newPassword, messageLabel)) {
                return;
            }
            currentUser.changePasswordHash(passwordHasher.hash(newPassword));
        }

        currentUser.setLastName(newLastName);
        currentUser.setUsername(newUsername);

        userDao.update(currentUser);
        UserSession.getUserInstance().setUser(currentUser);

        Validation.showMessage(messageLabel,Localization.get("dashboard.update_success"), Validation.MessageType.SUCCESS);
        passwordField.clear();
        confirmPasswordField.clear();

        // Close the window after a short delay to show the success message
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> WindowUtil.closeWindow(firstNameField));
            }
        }, 1500); // 1.5 second delay
    }

    @FXML
    private void handleCancel() {
        WindowUtil.closeWindow(firstNameField);
    }

    public void setUserDao(UserDAO userDao) {
        this.userDao = userDao;
    }

    public void setPasswordHasher(PasswordHasher passwordHasher) {
        this.passwordHasher = passwordHasher;
    }
}
