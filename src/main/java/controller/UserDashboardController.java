package controller;

import dao.user.JpaUserDao;
import entity.UserEntity;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import session.UserSession;
import security.BcryptPasswordHasher;
import security.PasswordHasher;
import security.Validation;

public class UserDashboardController {

    private final JpaUserDao userDao = new JpaUserDao();
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
    private Label messageLabel;

    @FXML
    public void initialize() {
        passwordHasher = new BcryptPasswordHasher();
        Validation.hideMessage(messageLabel);

        UserEntity currentUser = UserSession.getUserInstance().getUser();
        if (currentUser == null) {
            Validation.showMessage(messageLabel, "No active user session found", Validation.MessageType.ERROR);
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
            Validation.showMessage(messageLabel, "No active user session found", Validation.MessageType.ERROR);
            return;
        }

        String newLastName = lastNameField.getText() == null ? "" : lastNameField.getText().trim();
        String newUsername = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String newPassword = passwordField.getText() == null ? "" : passwordField.getText();
        String confirmPassword = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();

        if (newLastName.isEmpty() || newUsername.isEmpty()) {
            Validation.showMessage(messageLabel, "Last name and username are required", Validation.MessageType.ERROR);
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
            Validation.showMessage(messageLabel, "Username already exists", Validation.MessageType.ERROR);
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

        Validation.showMessage(messageLabel, "Account updated successfully", Validation.MessageType.SUCCESS);
        passwordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }
}
