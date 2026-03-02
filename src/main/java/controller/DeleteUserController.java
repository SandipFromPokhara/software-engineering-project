package controller;

import dao.user.JpaUserDao;
import entity.UserEntity;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import security.PasswordHasher;
import session.UserSession;
import security.BcryptPasswordHasher;
import security.Validation;

public class DeleteUserController {

    private final JpaUserDao userDao = new JpaUserDao();
    private PasswordHasher passwordHasher;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {

        Validation.hideMessage(messageLabel);
        passwordHasher = new BcryptPasswordHasher();
    }

    @FXML
    private void handleDelete() {
        UserEntity currentUser = UserSession.getUserInstance().getUser();
        if (currentUser == null) {
            Validation.showMessage(messageLabel, "No active user session found", Validation.MessageType.ERROR);
            return;
        }

        String password = passwordField.getText() == null ? "" : passwordField.getText();
        if (password.isBlank()) {
            Validation.showMessage(messageLabel, "Password is required", Validation.MessageType.ERROR);
            return;
        }

        if (!passwordHasher.verify(password, currentUser.getPasswordHash())) {
            Validation.showMessage(messageLabel, "Incorrect password", Validation.MessageType.ERROR);
            return;
        }

        try {
            userDao.delete(currentUser);
            UserSession.getUserInstance().setUser(null);
            closeWindow();
        } catch (Exception e) {
            Validation.showMessage(messageLabel, "Failed to delete account", Validation.MessageType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) passwordField.getScene().getWindow();
        stage.close();
    }
}
