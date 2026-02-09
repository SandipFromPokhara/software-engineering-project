package controller;

import dao.user.UserDAO;
import dao.user.JpaUserDao;
import entity.UserEntity;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import util.BcryptPasswordHasher;
import util.NavigationUtil;
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
    @FXML private Button backButton;

    @FXML
    private Button backButton;

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.navigateTo(event, "/FXML/entry.fxml", "Welcome to NoteVault", false);
    }

    private UserDAO userDAO;

    public SignUpController() {
        this.userDAO = new JpaUserDao();
    }

    @FXML
    public void initialize() {
        signUpButton.setOnAction(event -> handleSignUp());
        signUpButton.setDefaultButton(true);
    }

    @FXML
    public void onLogin(ActionEvent event) {
        navigateToLogin(event);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.navigateTo(event, "/FXML/entry.fxml", "Welcome to NoteVault", false);
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
            String hashedPassword = BcryptPasswordHasher.hashPassword(password);

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
                        javafx.application.Platform.runLater(() -> NavigationUtil.navigateTo(currentStage, "/FXML/login_view.fxml", "Login", false));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                Validation.showMessage(messageLabel, "Failed to create account!", Validation.MessageType.ERROR);
            }
        } catch (IllegalArgumentException e) {
            Validation.showMessage(messageLabel, "Validation error: " + e.getMessage(), Validation.MessageType.ERROR);
        } catch (RuntimeException e) {
            Validation.showMessage(messageLabel, "Database error occurred. Please try again.", Validation.MessageType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            Validation.showMessage(messageLabel, "An unexpected error occurred. Please try again.", Validation.MessageType.ERROR);
            e.printStackTrace();
        }
    }

    private void navigateToLogin(ActionEvent event) {
        NavigationUtil.navigateTo(event, "/FXML/login_view.fxml", "NoteVault - LogIn", false);
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