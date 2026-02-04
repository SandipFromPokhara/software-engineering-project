package controller;

import dao.UserDAO;
import dao.MockUserDAOImpl;
import model.User;
import util.BcryptPasswordHasher;
import util.Validation;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SignUpController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button signUpButton;
    @FXML private Hyperlink loginLink;
    @FXML private Label messageLabel;

    private UserDAO userDAO;

    public SignUpController() {
        this.userDAO = new MockUserDAOImpl();
    }

    @FXML
    public void initialize() {
        signUpButton.setOnAction(event -> handleSignUp());
    }

    @FXML
    public void onLogin() {
        navigateToLogin();
    }

    private void handleSignUp() {
        // Hide any previous messages
        Validation.hideMessage(messageLabel);

        // Get input values
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate all fields
        if (!Validation.validateSignupFields(firstName, lastName, username, email, password, confirmPassword, messageLabel)) {
            return;
        }

        // Check if username already exists
        if (userDAO.usernameExists(username)) {
            Validation.showMessage(messageLabel, "The username is already taken.", Validation.MessageType.ERROR);
            return;
        }

        // Check if email already exists
        if (userDAO.emailExists(email)) {
            Validation.showMessage(messageLabel, "The email already exists.", Validation.MessageType.ERROR);
            return;
        }

        try {
            // Hash the password using BCrypt
            String hashedPassword = BcryptPasswordHasher.hashPassword(password);

            // Create and save user
            User user = new User(firstName, lastName, username, email, hashedPassword);
            boolean success = userDAO.createUser(user);

            if (success) {
                Validation.showMessage(messageLabel, "Account created successfully!", Validation.MessageType.SUCCESS);
                clearFields();
                navigateToLogin();
            } else {
                Validation.showMessage(messageLabel, "Failed to create account!", Validation.MessageType.ERROR);
            }
        } catch (IllegalArgumentException e) {
            Validation.showMessage(messageLabel, "Password validation failed: " + e.getMessage(), Validation.MessageType.ERROR);
        } catch (Exception e) {
            Validation.showMessage(messageLabel, "An unexpected error occurred during registration. Please try again.", Validation.MessageType.ERROR);
            e.printStackTrace();
        }
    }

    private void navigateToLogin() {
        System.out.println("Navigating to login page...");
    }

    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
}