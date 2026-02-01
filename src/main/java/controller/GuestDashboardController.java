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
    private void handleHome() {
        System.out.println("Go to Home page");
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
        loadContent("CreateFilesGuest.fxml");
    }

    @FXML
    private void handleLogin() {
        System.out.println("Go to Login page");
    }

    @FXML
    private void handleRegister() {
        System.out.println("Go to Register page");
    }
}
