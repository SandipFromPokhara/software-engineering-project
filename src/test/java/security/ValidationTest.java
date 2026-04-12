package security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    private static final String FIRSTNAME = "John";
    private static final String LASTNAME = "Doe";
    private static final String USERNAME = "User123";
    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "Abc123!";

    @Test
    void testValidateSignupValid() {
        Validation.ValidationResult result = Validation.validateSignup(
                FIRSTNAME, LASTNAME, USERNAME,
                EMAIL, PASSWORD, PASSWORD
        );

        assertTrue(result.success());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void testValidateSignupMissingFields() {
        Validation.ValidationResult result = Validation.validateSignup(
                "", LASTNAME, USERNAME,
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
        assertTrue(result.errors().containsKey("username"));
    }

    @Test
    void testValidateSignupInvalidEmail() {
        Validation.ValidationResult result = Validation.validateSignup(
                FIRSTNAME, LASTNAME, USERNAME,
                "invalid-email", PASSWORD, PASSWORD
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("email"));
    }

    @Test
    void testValidateSignupPasswordMismatch() {
        Validation.ValidationResult result = Validation.validateSignup(
                FIRSTNAME, LASTNAME, USERNAME,
                EMAIL, PASSWORD, "Wrong123!"
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("confirmPassword"));
    }

    @Test
    void testValidateSignupWeakPassword() {
        Validation.ValidationResult result = Validation.validateSignup(
                FIRSTNAME, LASTNAME, USERNAME,
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
        assertTrue(Validation.validatePasswordMatch("pass123!", "pass123!"));
        assertFalse(Validation.validatePasswordMatch("pass123!", "pass124!"));
    }

    @Test
    void testValidateUpdateValidWithoutPasswordChange() {
        Validation.ValidationResult result = Validation.validateUpdate(
                LASTNAME, USERNAME, "", ""
        );

        assertTrue(result.success());
    }

    @Test
    void testValidateUpdateWithPasswordChangeValid() {
        Validation.ValidationResult result = Validation.validateUpdate(
                LASTNAME, USERNAME, PASSWORD, PASSWORD
        );

        assertTrue(result.success());
    }

    @Test
    void testValidateUpdatePasswordMismatch() {
        Validation.ValidationResult result = Validation.validateUpdate(
                LASTNAME, USERNAME, PASSWORD, "Wrong123!"
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
        assertTrue(result.errors().containsKey("username"));
    }

    @Test
    void testValidateUpdateMissingRequiredFields() {
        Validation.ValidationResult result = Validation.validateUpdate(
                "", "", "", ""
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("lastName"));
        assertTrue(result.errors().containsKey("username"));
    }
}
