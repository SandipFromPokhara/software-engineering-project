package controller;

import dao.user.JpaUserDao;
import entity.entities.UserEntity;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import security.MessageType;
import security.IPasswordHasher;
import session.UserSession;
import security.BcryptPasswordHasher;
import util.Localization;
import util.ShowMessageUtil;

public class DeleteUserController {

    private final JpaUserDao userDao = new JpaUserDao();
    private IPasswordHasher passwordHasher;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label deleteTitle;

    @FXML
    private Label deleteConfirmLabel;

    @FXML
    private Label deleteWarning;

    @FXML
    private Button cancelButton;

    @FXML
    Button deleteButton;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {

        // LOCALIZATION BINDINGS
        if (deleteTitle != null) {
            deleteTitle.textProperty().bind(Localization.bind("account.delete_title"));
        }
        if (deleteWarning != null) {
            deleteWarning.textProperty().bind(Localization.bind("account.delete_warning"));
        }
        if (deleteConfirmLabel != null) {
            deleteConfirmLabel.textProperty().bind(Localization.bind("account.confirm_password_label"));
        }

        if (passwordField != null) {
            passwordField.promptTextProperty().bind(Localization.bind("account.confirm_password_placeholder"));
        }

        if (deleteButton != null) {
            deleteButton.textProperty().bind(Localization.bind("button.delete"));
        }
        if (cancelButton != null) {
            cancelButton.textProperty().bind(Localization.bind("button.cancel"));
        }

        if (messageLabel != null) {
            ShowMessageUtil.hideMessage(messageLabel);
        }

        // Only create a BcryptPasswordHasher when no hasher was injected (tests inject a mock)
        if (passwordHasher == null) {
            passwordHasher = new BcryptPasswordHasher();
        }
    }

    @FXML
    private void handleDelete() {
        UserEntity currentUser = UserSession.getUserInstance().getUser();
        if (currentUser == null) {
            if (messageLabel != null) {
                ShowMessageUtil.showMessageKey(messageLabel, Localization.get("delete.no_session"), MessageType.ERROR);
            }
            return;
        }

        String password = passwordField == null || passwordField.getText() == null ? "" : passwordField.getText();
        if (password.isBlank()) {
            if (messageLabel != null) {
                ShowMessageUtil.showMessageKey(messageLabel, "delete.password_required", MessageType.ERROR);
            }
            return;
        }

        if (!passwordHasher.verify(password, currentUser.getPasswordHash())) {
            if (messageLabel != null) {
                ShowMessageUtil.showMessageKey(messageLabel, "delete.incorrect_password", MessageType.ERROR);
            }
            return;
        }

        try {
            userDao.delete(currentUser);
            UserSession.getUserInstance().setUser(null);
            closeWindow();
        } catch (Exception e) {
            if (messageLabel != null) {
                ShowMessageUtil.showMessageKey(messageLabel, Localization.get("delete.failed"), MessageType.ERROR);
            }
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        if (passwordField == null) return;
        var scene = passwordField.getScene();
        if (scene == null) return;
        var window = scene.getWindow();
        if (window == null) return;
        Stage stage = (Stage) window;
        stage.close();
    }
}
