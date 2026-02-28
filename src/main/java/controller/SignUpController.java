package controller;

import dao.user.UserDAO;
import dao.user.JpaUserDao;
import entity.UserEntity;
import javafx.stage.Stage;
import security.BcryptPasswordHasher;
import security.PasswordHasher;
import util.NavigationUtil;
import security.Validation;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SignUpController {
    private static final Logger logger = Logger.getLogger(SignUpController.class.getName());
    private PasswordHasher passwordHasher;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button signUpButton;
    @FXML private Hyperlink loginLink;
    @FXML private Label messageLabel;
    @FXML private Button backButton;

    private UserDAO userDAO;

    @FXML
    public void initialize() {
        userDAO = new JpaUserDao();
        passwordHasher = new BcryptPasswordHasher();
        signUpButton.setOnAction(event -> handleSignUp());
        signUpButton.setDefaultButton(true);
    }

    @FXML
    public void onLogin() {
        navigateToLogin();
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        NavigationUtil.replaceScene(stage, "/FXML/entry.fxml", "Welcome to NoteVault", false);
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

        try {
            // Check if username already exists
            UserEntity existingUserByUsername = userDAO.findByUsername(username);
            if (existingUserByUsername != null) {
                Validation.showMessage(messageLabel, "The username is already taken.", Validation.MessageType.ERROR);
                return;
            }

            // Check if email already exists
            UserEntity existingUserByEmail = userDAO.findByEmail(email);
            if (existingUserByEmail != null) {
                Validation.showMessage(messageLabel, "The email already exists.", Validation.MessageType.ERROR);
                return;
            }

            // Hash the password using BCrypt
            String hashedPassword = passwordHasher.hash(password);

            // Create new user entity
            UserEntity newUser = new UserEntity(firstName, lastName, username, email);
            newUser.changePasswordHash(hashedPassword);

            // Save user to database
            UserEntity savedUser = userDAO.save(newUser);

            if (savedUser != null && savedUser.getId() != null) {
                Validation.showMessage(messageLabel, "Account created successfully!", Validation.MessageType.SUCCESS);
                clearFields();

                // Get stage before entering the thread
                Stage currentStage = (Stage) signUpButton.getScene().getWindow();

                // Add a small delay before navigating to show success message
                new Thread(() -> {
                    try {
                        Thread.sleep(1500); // 1.5-second delay
                        javafx.application.Platform.runLater(() -> NavigationUtil.replaceScene(currentStage, "/FXML/login_view.fxml", "NoteVault - Login", false));
                    } catch (InterruptedException e) {
                        logger.log(Level.SEVERE, "Navigation thread interrupted for user: " + username, e);
                    }
                }).start();
            } else {
                Validation.showMessage(messageLabel, "Failed to create account!", Validation.MessageType.ERROR);
            }
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Illegal argument during sign-up for username: " + username, e);
            Validation.showMessage(messageLabel, "Validation error: " + e.getMessage(), Validation.MessageType.ERROR);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Runtime exception during sign-up for username: " + username + ", email: " + email, e);
            Validation.showMessage(messageLabel, "Database error occurred. Please try again.", Validation.MessageType.ERROR);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected exception during sign-up for username: " + username + ", email: " + email, e);
            Validation.showMessage(messageLabel, "An unexpected error occurred. Please try again.", Validation.MessageType.ERROR);
        }
    }

    private void navigateToLogin() {
        Stage stage = (Stage) loginLink.getScene().getWindow();
        NavigationUtil.replaceScene(stage, "/FXML/login_view.fxml", "NoteVault - LogIn", false);
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

    public void setPasswordHasher(PasswordHasher hasher) { this.passwordHasher = hasher; }
}