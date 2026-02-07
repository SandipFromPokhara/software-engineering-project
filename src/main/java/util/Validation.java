package util;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class Validation {

    // Private constructor to prevent instantiation
    private Validation() {
    }

    // Validates all signup fields
    public static boolean validateSignupFields(String firstName, String lastName, String username,
                                               String email, String password, String confirmPassword, Label messageLabel) {
        // Validate all required fields are filled
        if (!validateRequiredFields(firstName, lastName, username, email, password, confirmPassword, messageLabel)) {
            return false;
        }

        // Validate username format
        if (!validateUsername(username, messageLabel)) {
            return false;
        }

        // Validate email format
        if (!validateEmailFormat(email, messageLabel)) {
            return false;
        }

        // Validate password match
        if (!validatePasswordMatch(password, confirmPassword, messageLabel)) {
            return false;
        }

        // Validate password length
        if (!validatePasswordLength(password, messageLabel)) {
            return false;
        }

        return true;
    }

    // Validates that all required fields are filled
    public static boolean validateRequiredFields(String firstName, String lastName, String username,
                                                 String email, String password, String confirmPassword, Label messageLabel) {
        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() ||
                email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showMessage(messageLabel, "All fields are required", MessageType.ERROR);
            return false;
        }
        return true;
    }

    // Validates the username format
    public static boolean validateUsername(String username, Label messageLabel) {
        String usernameRegex = "^[a-zA-Z0-9_]{3,20}$";
        if (!username.matches(usernameRegex)) {
            showMessage(messageLabel, "Username must be 3-20 characters long", MessageType.ERROR);
            return false;
        }
        return true;
    }

    // Validates the email format using regex pattern
    public static boolean validateEmailFormat(String email, Label messageLabel) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            showMessage(messageLabel, "Invalid email address", MessageType.ERROR);
            return false;
        }
        return true;
    }

    // Validates that password and confirm password fields match
    public static boolean validatePasswordMatch(String password, String confirmPassword, Label messageLabel) {
        if (!password.equals(confirmPassword)) {
            showMessage(messageLabel, "Passwords do not match", MessageType.ERROR);
            return false;
        }
        return true;
    }

    // Validates that password meets minimum length requirement
    public static boolean validatePasswordLength(String password, Label messageLabel) {
        if (password.length() < 6) {
            showMessage(messageLabel, "Password must be at least 6 characters long", MessageType.ERROR);
            return false;
        }
        return true;
    }

    // Displays a message in the provided label
    public static void showMessage(Label label, String message, MessageType type) {
        label.setText(message);
        label.setVisible(true);

        // Set color based on message type
        switch (type) {
            case SUCCESS:
                label.setTextFill(Color.web("#2e7d32")); // Green for success
                break;
            case ERROR:
                label.setTextFill(Color.web("#d32f2f")); // Red for error
                break;
            case INFO:
                label.setTextFill(Color.web("#1976d2")); // Blue for info
                break;
        }
    }

    // Hides the message label
    public static void hideMessage(Label label) {
        label.setVisible(false);
        label.setText("");
    }

    // Enum for message types
    public enum MessageType {
        SUCCESS, ERROR, INFO
    }
}