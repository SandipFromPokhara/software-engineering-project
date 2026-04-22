package security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    private static final String FIRSTNAME = "John";
    private static final String LASTNAME = "Doe";
    private static final String USER123 = "User123";
    private static final String USERNAME = "username";
    private static final String EMAIL = "user@example.com";
    private static final String TEST_PASS = "Abc123!";

    @Test
    void testValidateSignupValid() {
        Validation.ValidationResult result = Validation.validateSignup(
                FIRSTNAME, LASTNAME, USER123,
                EMAIL, TEST_PASS, TEST_PASS
        );

        assertTrue(result.success());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void testValidateSignupMissingFields() {
        Validation.ValidationResult result = Validation.validateSignup(
                "", LASTNAME, USER123,
                EMAIL, TEST_PASS, TEST_PASS
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("firstName"));
    }

    @Test
    void testValidateSignupInvalidUsername() {
        Validation.ValidationResult result = Validation.validateSignup(
                FIRSTNAME, LASTNAME, "Us",
                EMAIL, TEST_PASS, TEST_PASS
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey(USERNAME));
    }

    @Test
    void testValidateSignupInvalidEmail() {
        Validation.ValidationResult result = Validation.validateSignup(
                FIRSTNAME, LASTNAME, USER123,
                "invalid-email", TEST_PASS, TEST_PASS
        );

        assertFalse(result.success());
        assertTrue(result.errors().containsKey("email"));
    }

    @Test
    void testValidateSignupPasswordMismatch() {
        Validation.ValidationResult result = Validation.validateSignup(
                FIRSTNAME, LASTNAME, USER123,
                EMAIL, TEST_PASS, "Wrong123!"
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
        String validateMatch = "pass123!";
        assertTrue(Validation.validatePasswordMatch(validateMatch, validateMatch));
        assertFalse(Validation.validatePasswordMatch(validateMatch, "pass124!"));
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
                LASTNAME, USER123, TEST_PASS, TEST_PASS
        );

        assertTrue(result.success());
    }

    @Test
    void testValidateUpdatePasswordMismatch() {
        Validation.ValidationResult result = Validation.validateUpdate(
                LASTNAME, USER123, TEST_PASS, "Wrong123!"
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

    @Test
    void testValidateNameTooShortAndTooLong() {
        Validation.ValidationResult r1 = Validation.validateSignup("A", "Doe", USER123, EMAIL, TEST_PASS, TEST_PASS);

        Validation.ValidationResult r2 = Validation.validateSignup("A".repeat(60), "Doe", USER123, EMAIL, TEST_PASS, TEST_PASS);

        assertFalse(r1.success());
        assertFalse(r2.success());
    }

    @Test
    void testValidateNameInvalidCharacters() {
        Validation.ValidationResult result = Validation.validateSignup("Jo3n", LASTNAME, USER123, EMAIL, TEST_PASS, TEST_PASS);

        assertFalse(result.success());
    }

    @Test
    void testValidatePasswordMissingDigit() {
        Validation.ValidationResult result = Validation.validateSignup(FIRSTNAME, LASTNAME, USER123, EMAIL, "Password!", "Password!");

        assertFalse(result.success());
    }

    @Test
    void testValidatePasswordMissingSpecial() {
        Validation.ValidationResult result = Validation.validateSignup(
                FIRSTNAME, LASTNAME, USER123,
                EMAIL, "Password123", "Password123"
        );

        assertFalse(result.success());
    }

    @Test
    void testValidateRequiredFieldsDirect() {
        assertTrue(Validation.validateRequiredFields("a","b","c","d","e","f"));
        assertFalse(Validation.validateRequiredFields("","b","c","d","e","f"));
    }

    @Test
    void testUpdateInvalidUsername() {
        Validation.ValidationResult result = Validation.validateUpdate("Doe", "!!", "", "");

        assertFalse(result.success());
    }

    @Test
    void testValidationErrorEquality() {
        Validation.ValidationError e1 = new Validation.ValidationError("key", new Object[]{"a"});

        Validation.ValidationError e2 = new Validation.ValidationError("key", new Object[]{"a"});

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }
}
