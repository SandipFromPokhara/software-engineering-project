package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class Login {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private void onLogin(ActionEvent event) {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        System.out.println("Login attempted: " + user + " / " + (pass == null ? "" : "[hidden]"));
        // TODO: validate credentials and navigate to main app screen
        if (!user.isEmpty() && !pass.isEmpty()) {
            System.out.println("Login would succeed - navigate to main app");
            // loadScreen("/FXML/main_app.fxml", "Note Vault", event);
        }
    }

    @FXML
    private void onSignUp(ActionEvent event) {
        System.out.println("Sign-up link clicked - navigating to Signup screen");
        loadScreen("/FXML/signup.fxml", "Sign Up - Note Vault", event);
    }

    private void loadScreen(String fxmlPath, String title, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (IOException e) {
            System.err.println("Failed to load screen: " + fxmlPath);
            e.printStackTrace();
        }
    }

}
