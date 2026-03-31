package security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    @Test
    void testValidateSignup_valid() {
        Validation.ValidationResult result = Validation.validateSignup(
                "John", "Doe", "User123",
                "user@example.com", "Abc123!", "Abc123!"
        );

        assertTrue(result.success());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void testValidateSignup_missingFields() {
        Validation.ValidationResult result = Validation.validateSignup(
                "", "Doe", "User123",
                "user@example.com", "Abc123!", "Abc123!"
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("firstName"));
    }

    @Test
    void testValidateSignup_invalidUsername() {
        Validation.ValidationResult result = Validation.validateSignup(
                "John", "Doe", "Us",
                "user@example.com", "Abc123!", "Abc123!"
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("username"));
    }

    @Test
    void testValidateSignup_invalidEmail() {
        Validation.ValidationResult result = Validation.validateSignup(
                "John", "Doe", "User123",
                "invalid-email", "Abc123!", "Abc123!"
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("email"));
    }

    @Test
    void testValidateSignup_passwordMismatch() {
        Validation.ValidationResult result = Validation.validateSignup(
                "John", "Doe", "User123",
                "user@example.com", "Abc123!", "Wrong123!"
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("confirmPassword"));
    }

    @Test
    void testValidateSignup_weakPassword() {
        Validation.ValidationResult result = Validation.validateSignup(
                "John", "Doe", "User123",
                "user@example.com", "abc", "abc"
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("password"));
    }

    @Test
    void testValidateUsername() {
        assertTrue(Validation.validateUsername("User_123"));
        assertFalse(Validation.validateUsername("Us"));
        assertFalse(Validation.validateUsername("Invalid!"));
    }

    @Test
    void testValidateEmailFormat() {
        assertTrue(Validation.validateEmailFormat("test@example.com"));
        assertFalse(Validation.validateEmailFormat("invalid-email"));
    }

    @Test
    void testValidatePasswordMatch() {
        assertTrue(Validation.validatePasswordMatch("pass123!", "pass123!"));
        assertFalse(Validation.validatePasswordMatch("pass123!", "pass124!"));
    }

    @Test
    void testValidateUpdate_validWithoutPasswordChange() {
        Validation.ValidationResult result = Validation.validateUpdate(
                "Doe", "User123", "", ""
        );

        assertTrue(result.success());
    }

    @Test
    void testValidateUpdate_withPasswordChange_valid() {
        Validation.ValidationResult result = Validation.validateUpdate(
                "Doe", "User123", "Abc123!", "Abc123!"
        );

        assertTrue(result.success());
    }

    @Test
    void testValidateUpdate_passwordMismatch() {
        Validation.ValidationResult result = Validation.validateUpdate(
                "Doe", "User123", "Abc123!", "Wrong123!"
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("confirmPassword"));
    }

    @Test
    void testValidateUpdate_invalidUsername() {
        Validation.ValidationResult result = Validation.validateUpdate(
                "Doe", "Us", "", ""
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("username"));
    }

    @Test
    void testValidateUpdate_missingRequiredFields() {
        Validation.ValidationResult result = Validation.validateUpdate(
                "", "", "", ""
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("lastName"));
        assertTrue(result.errors().containsKey("username"));
    }
}