package security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    private static final String FIRSTNAME = "John";
    private static final String LASTNAME = "Doe";
    private static final String USER123 = "User123";
    private static final String USERNAME = "username";
    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "Abc123!";

    @Test
    void testValidateSignupValid() {
        Validation.ValidationResult result = Validation.validateSignup(
                FIRSTNAME, LASTNAME, USER123,
                EMAIL, PASSWORD, PASSWORD
        );

        assertTrue(result.success());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void testValidateSignupMissingFields() {
        Validation.ValidationResult result = Validation.validateSignup(
                "", LASTNAME, USER123,
                EMAIL, PASSWORD, PASSWORD
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("firstName"));
    }

    @Test
    void testValidateSignupInvalidUsername() {
        Validation.ValidationResult result = Validation.validateSignup(
                FIRSTNAME, LASTNAME, "Us",
                EMAIL, PASSWORD, PASSWORD
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey(USERNAME));
    }

    @Test
    void testValidateSignupInvalidEmail() {
        Validation.ValidationResult result = Validation.validateSignup(
                FIRSTNAME, LASTNAME, USER123,
                "invalid-email", PASSWORD, PASSWORD
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("email"));
    }

    @Test
    void testValidateSignupPasswordMismatch() {
        Validation.ValidationResult result = Validation.validateSignup(
                FIRSTNAME, LASTNAME, USER123,
                EMAIL, PASSWORD, "Wrong123!"
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("confirmPassword"));
    }

    @Test
    void testValidateSignupWeakPassword() {
        Validation.ValidationResult result = Validation.validateSignup(
                FIRSTNAME, LASTNAME, USER123,
                EMAIL, "abc", "abc"
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
        String validatePassword = "pass123!";
        assertTrue(Validation.validatePasswordMatch(validatePassword, validatePassword));
        assertFalse(Validation.validatePasswordMatch(validatePassword, "pass124!"));
    }

    @Test
    void testValidateUpdateValidWithoutPasswordChange() {
        Validation.ValidationResult result = Validation.validateUpdate(
                LASTNAME, USER123, "", ""
        );

        assertTrue(result.success());
    }

    @Test
    void testValidateUpdateWithPasswordChangeValid() {
        Validation.ValidationResult result = Validation.validateUpdate(
                LASTNAME, USER123, PASSWORD, PASSWORD
        );

        assertTrue(result.success());
    }

    @Test
    void testValidateUpdatePasswordMismatch() {
        Validation.ValidationResult result = Validation.validateUpdate(
                LASTNAME, USER123, PASSWORD, "Wrong123!"
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("confirmPassword"));
    }

    @Test
    void testValidateUpdateInvalidUsername() {
        Validation.ValidationResult result = Validation.validateUpdate(
                LASTNAME, "Us", "", ""
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey(USERNAME));
    }

    @Test
    void testValidateUpdateMissingRequiredFields() {
        Validation.ValidationResult result = Validation.validateUpdate(
                "", "", "", ""
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("lastName"));
        assertTrue(result.errors().containsKey(USERNAME));
    }
}
