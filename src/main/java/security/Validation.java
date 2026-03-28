package security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Validation {

    // Private constructor to prevent instantiation
    private Validation() {
    }

    public record ValidationError(String key, List<Object> args) {}

    public record ValidationResult(boolean success, Map<String, List<ValidationError>> errors) {}

    private static void addError(Map<String, List<ValidationError>> errors,
                                 String field,
                                 String key,
                                 Object... args) {

        errors.computeIfAbsent(field, k -> new java.util.ArrayList<>())
                .add(new ValidationError(key, List.of(args)));
    }

    public static ValidationResult validateSignup(String firstName, String lastName,
                                                  String username, String email,
                                                  String password, String confirmPassword) {

        Map<String, List<ValidationError>> errors = new HashMap<>();

        firstName = firstName == null ? "" : firstName.trim();
        lastName = lastName == null ? "" : lastName.trim();
        username = username == null ? "" : username.trim();
        email = email == null ? "" : email.trim();
        password = password == null ? "" : password;
        confirmPassword = confirmPassword == null ? "" : confirmPassword;

        if (!validateRequiredFields(firstName, lastName, username, email, password, confirmPassword)) {
            addError(errors, "firstName", "signup.all_fields");
            addError(errors, "lastName", "signup.all_fields");
            addError(errors, "username", "signup.all_fields");
            addError(errors, "email", "signup.all_fields");
            addError(errors, "password", "signup.all_fields");
            addError(errors, "confirmPassword", "signup.all_fields");
        }

        // First name
        validateName(firstName, "firstName", errors, "First name");

        // Last name
        validateName(lastName, "lastName", errors, "Last name");

        // Username
        if (!username.matches("^[\\p{L}0-9_]{3,20}$")) {
            addError(errors,"username", "signup.username_chars");
        }

        // Email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            addError(errors,"email", "signup.email_invalid");
        }

        // Password match
        if (!password.equals(confirmPassword)) {
            addError(errors,"confirmPassword", "signup.password_mismatch");
        }

        // Password rules
        if (password.length() < 6) {
            addError(errors,"password", "signup.password_too_short");
        }

        if (!password.matches(".*\\d.*")) {
            addError(errors,"password", "signup.password_missing_number");
        }

        if (!password.matches(".*[!@#$%^&*()_+=\\-\\[\\]{};':\"\\\\|,.<>/?].*")) {
            addError(errors,"password", "signup.password_missing_special");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    // Validates that all required fields are filled
    public static boolean validateRequiredFields(String firstName, String lastName, String username,
                                                 String email, String password, String confirmPassword) {
        return !(firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() ||
                email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty());
    }

    // Validates name format
    public static void validateName(String value, String field, Map<String, List<ValidationError>> errors, String label) {
        if (value == null || value.isBlank()) {
            return;
        }

        if (value.length() < 2 || value.length() > 50) {
            addError(errors, field, "signup.name_length", label);
            return;
        }

        if (!value.matches("^[\\p{L}\\s\\-'/]+$")) {
            addError(errors, field, "signup.name_invalid", label);
        }
    }

    // Validates the username format
    public static boolean validateUsername(String username) {
        // Allow letters (including Nordic chars), numbers, and underscores
        return username.matches("^[\\p{L}0-9_]{3,20}$");
    }

    public static void validatePassword(String password,
                                        Map<String, List<ValidationError>> errors,
                                        String field) {

        if (password == null || password.isBlank()) {
            return;
        }

        if (password.length() < 6) {
            addError(errors, field, "signup.password_too_short");
        }

        if (!password.matches(".*\\d.*")) {
            addError(errors, field, "signup.password_missing_number");
        }

        if (!password.matches(".*[!@#$%^&*()_+=\\-\\[\\]{};':\"\\\\|,.<>/?].*")) {
            addError(errors, field, "signup.password_missing_special");
        }
    }

    // Validates the email format using regex pattern
    public static boolean validateEmailFormat(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // Validates that password and confirm password fields match
    public static boolean validatePasswordMatch(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }
}