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
import util.NavigationUtil;
import javafx.event.ActionEvent;


public class GuestDashboardController {

    @FXML
    private Button home;

    @FXML
    private Button newFiles;

    @FXML
    private Button login;

    @FXML
    private Button register;

    @FXML
    private HBox topBar;

    @FXML
    private Label title;

    @FXML
    private Label content;

    @FXML
    private VBox centerPane;

    @FXML
    private void handleHome(ActionEvent event) {
        NavigationUtil.navigateTo(
                event,
                "/FXML/entry.fxml",
                "Welcome to NoteVault",
                false
        );
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    @FXML
    private void handleNewFiles() {
        loadContent("createFilesGuest.fxml");
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        NavigationUtil.navigateTo(
                event,
                "/FXML/login_view.fxml",
                "NoteVault - LogIn",
                false
        );


    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        NavigationUtil.navigateTo(
                event,
                "/FXML/signup.fxml",
                "NoteVault - Register",
                false
        );

    }
}
