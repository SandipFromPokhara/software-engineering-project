package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.Localization;
import util.NavigationUtil;

import java.util.logging.Level;

import static controller.ViewDashboardController.logger;


public class GuestDashboardController {

    @FXML
    private Button home, login, register;

    @FXML
    private Button newFiles;

    @FXML
    private HBox topBar;

    @FXML
    private Label title, content;

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
        try {
            Parent view = FXMLLoader.load(
                    getClass().getResource("/FXML/" + fxmlFile)
            );

            StackPane wrapper = new StackPane(view);
            VBox.setVgrow(wrapper, Priority.ALWAYS);

            //To make responsive
            wrapper.prefWidthProperty().bind(centerPane.widthProperty());
            wrapper.prefHeightProperty().bind(centerPane.heightProperty());

            centerPane.getChildren().setAll(wrapper);

        //logger
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load content" + fxmlFile, e);
        }
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
