package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.Localization;
import util.NavigationUtil;

public class GuestDashboardController {

    @FXML
    private Button home, login, register;

    @FXML
    private Button newFiles;

    @FXML
    private HBox topBar;

    @FXML
    private Label title;

    @FXML
    private Label content;

    @FXML
    private VBox centerPane;

    @FXML
    public void initialize() {
        login.textProperty().bind(Localization.bind("guest.login"));
        register.textProperty().bind(Localization.bind("guest.register"));
    }

    @FXML
    private void handleHome() {
        Stage stage = (Stage) home.getScene().getWindow();
        NavigationUtil.replaceScene(stage, "/FXML/entry.fxml", "entry.window_title", false);
    }

    private void loadContent(String fxmlFile) {
        NavigationUtil.openWindow(
                (Stage) centerPane.getScene().getWindow(),
                "/FXML/" + fxmlFile,
                "some.title.key",
                true,
                false,
                null
        );
    }

    @FXML
    public void handleNewFiles() {
        loadContent("create_files_guest.fxml");
    }

    @FXML
    private void handleLogin() {
        Stage stage = (Stage) login.getScene().getWindow();
        NavigationUtil.replaceScene(
                stage,
                "/FXML/login_view.fxml",
                "login.window_title",
                false
        );
    }

    @FXML
    private void handleSignUp() {
        Stage stage = (Stage) register.getScene().getWindow();
        NavigationUtil.replaceScene(stage, "/FXML/signup.fxml", "register.window_title", false);
    }
}
