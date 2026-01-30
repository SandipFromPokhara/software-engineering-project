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

@SuppressWarnings("unused")
public class Signup {

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
    private Button signUpButton;

    @FXML
    private void onSignUp(ActionEvent event) {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        System.out.println("Sign-up attempted for: " + username);

        // TODO: Add validation (non-empty fields, password match, email format, etc.)
        // TODO: Create user account
        // TODO: Navigate to login or main app

        if (!password.equals(confirmPassword)) {
            System.err.println("Passwords do not match!");
            return;
        }

        System.out.println("Sign-up would succeed - navigate to login or main app");
    }

    @FXML
    private void onLogin(ActionEvent event) {
        System.out.println("Login hyperlink clicked - navigating to Login screen");
        loadScreen("/FXML/login.fxml", "Login - Note Vault", event);
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
