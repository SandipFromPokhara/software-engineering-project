package security;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    // ---------- validateSignup ----------
    @Test
    void validateSignup_shouldPass_whenAllValid() {
        Validation.ValidationResult result =
                Validation.validateSignup("John", "Doe", "User123",
                        "user@example.com", "Abc123!", "Abc123!");

        assertTrue(result.success());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validateSignup_shouldFail_whenFieldsEmpty() {
        Validation.ValidationResult result =
                Validation.validateSignup("", "", "", "", "", "");

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("firstName"));
        assertTrue(result.errors().containsKey("password"));
    }

    @Test
    void validateSignup_shouldFail_invalidEmail() {
        Validation.ValidationResult result =
                Validation.validateSignup("John", "Doe", "User123",
                        "invalid-email", "Abc123!", "Abc123!");

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("email"));
    }

    @Test
    void validateSignup_shouldFail_passwordMismatch() {
        Validation.ValidationResult result =
                Validation.validateSignup("John", "Doe", "User123",
                        "user@example.com", "Abc123!", "Different!");

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("confirmPassword"));
    }

    @Test
    void validateSignup_shouldFail_weakPassword() {
        Validation.ValidationResult result =
                Validation.validateSignup("John", "Doe", "User123",
                        "user@example.com", "abc", "abc");

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("password"));
    }

    // ---------- validateRequiredFields ----------
    @Test
    void validateRequiredFields_shouldPass() {
        assertTrue(Validation.validateRequiredFields("a","b","c","d","e","f"));
    }

    @Test
    void validateRequiredFields_shouldFail() {
        assertFalse(Validation.validateRequiredFields("","b","c","d","e","f"));
    }

    // ---------- validateName ----------
    @Test
    void validateName_shouldAddError_forInvalidName() {
        Map<String, java.util.List<Validation.ValidationError>> errors = new java.util.HashMap<>();

        Validation.validateName("J", "firstName", errors, "First name");

        assertTrue(errors.containsKey("firstName"));
    }

    @Test
    void validateName_shouldPass_forValidName() {
        Map<String, java.util.List<Validation.ValidationError>> errors = new java.util.HashMap<>();

        Validation.validateName("John", "firstName", errors, "First name");

        assertTrue(errors.isEmpty());
    }

    // ---------- validateUsername ----------
    @Test
    void validateUsername_shouldPass() {
        assertTrue(Validation.validateUsername("User_123"));
    }

    @Test
    void validateUsername_shouldFail() {
        assertFalse(Validation.validateUsername("Us"));
        assertFalse(Validation.validateUsername("Invalid!"));
    }

    // ---------- validateEmailFormat ----------
    @Test
    void validateEmail_shouldPass() {
        assertTrue(Validation.validateEmailFormat("test@example.com"));
    }

    @Test
    void validateEmail_shouldFail() {
        assertFalse(Validation.validateEmailFormat("invalid-email"));
    }

    // ---------- validatePasswordMatch ----------
    @Test
    void validatePasswordMatch_shouldPass() {
        assertTrue(Validation.validatePasswordMatch("pass123!", "pass123!"));
    }

    @Test
    void validatePasswordMatch_shouldFail() {
        assertFalse(Validation.validatePasswordMatch("pass123!", "pass124!"));
    }

    // ---------- validateUpdate ----------
    @Test
    void validateUpdate_shouldPass_whenValid() {
        Validation.ValidationResult result =
                Validation.validateUpdate("Doe", "User123", "Abc123!", "Abc123!");

        assertTrue(result.success());
    }

    @Test
    void validateUpdate_shouldFail_missingFields() {
        Validation.ValidationResult result =
                Validation.validateUpdate("", "", "", "");

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("lastName"));
        assertTrue(result.errors().containsKey("username"));
    }

    @Test
    void validateUpdate_shouldFail_passwordMismatch() {
        Validation.ValidationResult result =
                Validation.validateUpdate("Doe", "User123", "Abc123!", "wrong");

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("confirmPassword"));
    }

    @Test
    void validateUpdate_shouldAllow_emptyPassword() {
        Validation.ValidationResult result =
                Validation.validateUpdate("Doe", "User123", "", "");

        assertTrue(result.success());
    }
}