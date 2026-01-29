package controller;

import dao.UserDAO;
import dao.MockUserDAOImpl;
import model.User;
import util.PasswordHasher;
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

    private UserDAO userDAO;

    public SignUpController() {
        // Using Mock DAO
        this.userDAO = new MockUserDAOImpl();
    }

    // Sets up event handlers for UI components
    @FXML
    public void initialize() {
        signUpButton.setOnAction(event -> handleSignUp());
        loginLink.setOnAction(event -> navigateToLogin());
    }

    // Validates input fields and creates a new user
    private void handleSignUp() {
        // Get input values
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate all fields are filled
        if (!validateRequiredFields(firstName, lastName, username, email, password, confirmPassword)) {
            return;
        }

        // Validate username format
        if (!validateUsername(username)) {
            return;
        }

        // Validate email format
        if (!validateEmailFormat(email)) {
            return;
        }

        // Validate password match
        if (!validatePasswordMatch(password, confirmPassword)) {
            return;
        }

        // Validate password length
        if (!validatePasswordLength(password)) {
            return;
        }

        // Check if username already exists
        if (userDAO.usernameExists(username)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error",
                    "This username is already taken. Please choose a different username.");
            return;
        }

        // Check if email already exists
        if (userDAO.emailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error",
                    "An account with this email already exists. Please use a different email.");
            return;
        }

        // Hash the password before storing
        String hashedPassword = PasswordHasher.hashPassword(password);

        // Create user object
        User user = new User(firstName, lastName, username, email, hashedPassword);

        // Attempt to create user in database
        boolean success = userDAO.createUser(user);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Account created successfully! You can now log in.");
            clearFields();
            navigateToLogin();
        } else {
            showAlert(Alert.AlertType.ERROR, "Registration Error",
                    "Failed to create account. Please try again later.");
        }
    }

    //Validates that all required fields are filled
    private boolean validateRequiredFields(String firstName, String lastName, String username,
                                           String email, String password, String confirmPassword) {
        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() ||
                email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "All fields are required. Please fill in all the information.");
            return false;
        }
        return true;
    }

    //Validates the username format
    private boolean validateUsername(String username) {
        String usernameRegex = "^[a-zA-Z0-9_]{3,20}$";
        if (!username.matches(usernameRegex)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Username must be 3-20 characters and contain only letters, numbers, and underscores.");
            return false;
        }
        return true;
    }

    //Validates the email format using regex pattern
    private boolean validateEmailFormat(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Invalid email format. Please enter a valid email address.");
            return false;
        }
        return true;
    }

    //Validates that password and confirm password fields match
    private boolean validatePasswordMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Passwords do not match. Please make sure both passwords are identical.");
            return false;
        }
        return true;
    }

    //Validates that password meets minimum length requirement
    private boolean validatePasswordLength(String password) {
        if (password.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Password must be at least 6 characters long.");
            return false;
        }
        return true;
    }

    //Navigates to the login page
    private void navigateToLogin() {

        System.out.println("Navigating to login page...");
    }

    //Clears all input fields after successful registration
    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    //Displays an alert dialog with the specified type, title, and message
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //Sets a custom UserDAO implementation for testing purposes
    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
}
