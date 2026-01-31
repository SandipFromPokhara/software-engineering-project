package controller;

import javafx.scene.control.*;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

public class GuestDashboardController {
    @FXML
    private TextField titleBox;

    @FXML
    private TextArea contentBox;

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
    private void handleHome() {
        System.out.println("Go to Home page");
    }
    @FXML
    private void handleNewFiles() {
        System.out.println("Go to create new files.");
        // load login.fxml here
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