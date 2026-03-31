package controller;

import dao.user.JpaUserDao;
import entity.UserEntity;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import security.MessageType;
import security.PasswordHasher;
import session.UserSession;
import security.BcryptPasswordHasher;
import util.Localization;
import util.ShowMessageUtil;

public class DeleteUserController {

    private final JpaUserDao userDao = new JpaUserDao();
    private PasswordHasher passwordHasher;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label deleteTitle, deleteWarning, deleteConfirmLabel;

    @FXML
    private Button cancelButton, deleteButton;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {

        // LOCALIZATION BINDINGS
        deleteTitle.textProperty().bind(Localization.bind("account.delete_title"));
        deleteWarning.textProperty().bind(Localization.bind("account.delete_warning"));
        deleteConfirmLabel.textProperty().bind(Localization.bind("account.confirm_password_label"));

        passwordField.promptTextProperty().bind(Localization.bind("account.confirm_password_placeholder"));

        deleteButton.textProperty().bind(Localization.bind("button.delete"));
        cancelButton.textProperty().bind(Localization.bind("button.cancel"));

        ShowMessageUtil.hideMessage(messageLabel);
        passwordHasher = new BcryptPasswordHasher();
    }

    @FXML
    private void handleDelete() {
        UserEntity currentUser = UserSession.getUserInstance().getUser();
        if (currentUser == null) {
            ShowMessageUtil.showMessageKey(messageLabel, Localization.get("delete.no_session"), MessageType.ERROR);
            return;
        }

        String password = passwordField.getText() == null ? "" : passwordField.getText();
        if (password.isBlank()) {
            ShowMessageUtil.showMessageKey(messageLabel, "delete.password_required", MessageType.ERROR);
            return;
        }

        if (!passwordHasher.verify(password, currentUser.getPasswordHash())) {
            ShowMessageUtil.showMessageKey(messageLabel, "delete.incorrect_password", MessageType.ERROR);
            return;
        }

        try {
            userDao.delete(currentUser);
            UserSession.getUserInstance().setUser(null);
            closeWindow();
        } catch (Exception e) {
            ShowMessageUtil.showMessageKey(messageLabel,Localization.get("delete.failed"), MessageType.ERROR);
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
