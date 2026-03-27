package security;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    private static Label messageLabel;

    @BeforeAll
    static void setup() {
        // Initializes JavaFX toolkit
        new JFXPanel();
        messageLabel = new Label();
    }

    @Test
    void testValidateName() {
        // Valid
        assertTrue(Validation.validateName("John", "First Name", messageLabel));
        assertFalse(messageLabel.isVisible());

        // Invalid chars
        assertFalse(Validation.validateName("John123", "First Name", messageLabel));
        assertTrue(messageLabel.isVisible());
        assertEquals("First Name contains invalid characters", messageLabel.getText());

        // Too short
        assertFalse(Validation.validateName("J", "First Name", messageLabel));
        assertTrue(messageLabel.isVisible());
        assertEquals("First Name must be 2-50 characters long", messageLabel.getText());

        // Too long
        String longName = "A".repeat(51);
        assertFalse(Validation.validateName(longName, "First Name", messageLabel));
        assertTrue(messageLabel.isVisible());
        assertEquals("First Name must be 2-50 characters long", messageLabel.getText());
    }

    @Test
    void testValidateRequiredFields() {
        // All filled
        assertTrue(Validation.validateRequiredFields("a","b","c","d","e","f", messageLabel));

        // One empty
        assertFalse(Validation.validateRequiredFields("","b","c","d","e","f", messageLabel));
        assertEquals("All fields are required", messageLabel.getText());
    }

    @Test
    void testValidateUsername() {
        assertTrue(Validation.validateUsername("User_123", messageLabel));
        assertFalse(Validation.validateUsername("Us", messageLabel));
        assertEquals("Username must be 3-20 characters", messageLabel.getText());
        assertFalse(Validation.validateUsername("Invalid!", messageLabel));
        assertEquals("Username must be 3-20 characters", messageLabel.getText());
    }

    @Test
    void testValidateEmailFormat() {
        assertTrue(Validation.validateEmailFormat("test@example.com", messageLabel));
        assertFalse(Validation.validateEmailFormat("invalid-email", messageLabel));
        assertEquals("Invalid email address", messageLabel.getText());
    }

    @Test
    void testValidatePasswordMatch() {
        assertTrue(Validation.validatePasswordMatch("pass123!", "pass123!", messageLabel));
        assertFalse(Validation.validatePasswordMatch("pass123!", "pass124!", messageLabel));
        assertEquals("Passwords do not match", messageLabel.getText());
    }

    @Test
    void testValidatePasswordStrength() {
        // Valid
        assertTrue(Validation.validatePasswordStrength("Abc123!", messageLabel));

        // Too short
        assertFalse(Validation.validatePasswordStrength("Ab1!", messageLabel));
        assertEquals("Password must be at least 6 characters long", messageLabel.getText());

        // Missing number
        assertFalse(Validation.validatePasswordStrength("Abcdef!", messageLabel));
        assertEquals("Password must contain at least 1 number", messageLabel.getText());

        // Missing special char
        assertFalse(Validation.validatePasswordStrength("Abc1234", messageLabel));
        assertEquals("Password must contain at least 1 special character", messageLabel.getText());
    }

    @Test
    void testValidateSignupFields() {
        // Everything valid
        assertTrue(Validation.validateSignupFields(
                "John", "Doe", "User123", "user@example.com", "Abc123!", "Abc123!", messageLabel));

        // Missing required field
        assertFalse(Validation.validateSignupFields(
                "", "Doe", "User123", "user@example.com", "Abc123!", "Abc123!", messageLabel));

        // Invalid username
        assertFalse(Validation.validateSignupFields(
                "John", "Doe", "Us", "user@example.com", "Abc123!", "Abc123!", messageLabel));

        // Invalid email
        assertFalse(Validation.validateSignupFields(
                "John", "Doe", "User123", "userexample.com", "Abc123!", "Abc123!", messageLabel));

        // Password mismatch
        assertFalse(Validation.validateSignupFields(
                "John", "Doe", "User123", "user@example.com", "Abc123!", "Abc1234!", messageLabel));

        // Weak password
        assertFalse(Validation.validateSignupFields(
                "John", "Doe", "User123", "user@example.com", "abc", "abc", messageLabel));
    }

    @Test
    void testShowAndHideMessage() {
        // Show error
        Validation.showMessage(messageLabel, "Error!", Validation.MessageType.ERROR);
        assertTrue(messageLabel.isVisible());
        assertEquals("Error!", messageLabel.getText());

        // Hide
        Validation.hideMessage(messageLabel);
        assertFalse(messageLabel.isVisible());
        assertEquals("", messageLabel.getText());

        // Show success
        Validation.showMessage(messageLabel, "Success!", Validation.MessageType.SUCCESS);
        assertTrue(messageLabel.isVisible());
        assertEquals("Success!", messageLabel.getText());

        // Show info
        Validation.showMessage(messageLabel, "Info!", Validation.MessageType.INFO);
        assertTrue(messageLabel.isVisible());
        assertEquals("Info!", messageLabel.getText());
    }
}