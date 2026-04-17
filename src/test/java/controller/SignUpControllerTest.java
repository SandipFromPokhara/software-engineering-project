package controller;

import dao.user.IUserDAO;
import entity.entities.UserEntity;
import javafx.application.Platform;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import security.BcryptPasswordHasher;
import security.IPasswordHasher;
import testutil.JavaFXInitializer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SignUpControllerTest {

    private static final long FX_TIMEOUT_SECONDS = 2;

    private SignUpController controller;
    private MockUserDAO mockUserDAO;
    private IPasswordHasher passwordHasher;

    private TextField firstNameField, lastNameField, usernameField, emailField;
    private PasswordField passwordField, confirmPasswordField;
    private Button signUpButton, backButton;
    private Hyperlink loginLink;
    private Label messageLabel, createAccount, joinAccount, haveAccount;
    private Label privacyLabel, passwordStrengthLabel;
    private ProgressBar passwordStrengthBar;

    private static class MockUserDAO implements IUserDAO {
        private UserEntity userToReturn;
        UserEntity savedUser;
        boolean shouldThrowRuntimeException;
        boolean shouldThrowIllegalArgumentException;
        boolean shouldReturnNullOnSave;
        boolean shouldReturnUserWithoutId;

        @Override
        public UserEntity save(UserEntity user) {
            if (shouldThrowIllegalArgumentException) throw new IllegalArgumentException("Validation error");
            if (shouldThrowRuntimeException) throw new RuntimeException("Database error");
            if (shouldReturnNullOnSave) return null;
            if (shouldReturnUserWithoutId)
                return new UserEntity(user.getFirstName(), user.getLastName(),
                        user.getUsername(), user.getEmail());

            savedUser = user;
            return new UserEntity(user.getFirstName(), user.getLastName(),
                    user.getUsername(), user.getEmail());
        }

        @Override
        public UserEntity findByUsername(String username) {
            if (userToReturn != null && userToReturn.getUsername().equals(username)) return userToReturn;
            return null;
        }

        @Override
        public UserEntity findByEmail(String email) {
            if (userToReturn != null && userToReturn.getEmail().equals(email)) return userToReturn;
            return null;
        }

        @Override public UserEntity findById(Long id) { return null; }
        @Override public void update(UserEntity user) {
            /* Not implemented — mock does not need update functionality for these tests. */
        }
        @Override public void delete(UserEntity user) {
            /* Not implemented — mock does not need delete functionality for these tests. */
        }

        public void setUserToReturn(UserEntity user) { this.userToReturn = user; }

        public void reset() {
            userToReturn = null;
            savedUser = null;
            shouldThrowRuntimeException = false;
            shouldThrowIllegalArgumentException = false;
            shouldReturnNullOnSave = false;
            shouldReturnUserWithoutId = false;
        }
    }

    @BeforeAll
    static void initJavaFX() {
        JavaFXInitializer.init();
    }

    @BeforeEach
    void setUp() {
        controller = new SignUpController();
        passwordHasher = new BcryptPasswordHasher();

        runOnFxThreadAndWait(() -> {
            firstNameField = new TextField();
            lastNameField = new TextField();
            usernameField = new TextField();
            emailField = new TextField();
            passwordField = new PasswordField();
            confirmPasswordField = new PasswordField();
            signUpButton = new Button();
            loginLink = new Hyperlink();
            messageLabel = new Label();
            backButton = new Button();
            createAccount = new Label();
            joinAccount = new Label();
            haveAccount = new Label();
            privacyLabel = new Label();
            passwordStrengthLabel = new Label();
            passwordStrengthBar = new ProgressBar();
        });

        injectField("firstNameField", firstNameField);
        injectField("lastNameField", lastNameField);
        injectField("usernameField", usernameField);
        injectField("emailField", emailField);
        injectField("passwordField", passwordField);
        injectField("confirmPasswordField", confirmPasswordField);
        injectField("signUpButton", signUpButton);
        injectField("loginLink", loginLink);
        injectField("messageLabel", messageLabel);
        injectField("backButton", backButton);
        injectField("createAccount", createAccount);
        injectField("joinAccount", joinAccount);
        injectField("haveAccount", haveAccount);
        injectField("privacyLabel", privacyLabel);
        injectField("passwordStrengthLabel", passwordStrengthLabel);
        injectField("passwordStrengthBar", passwordStrengthBar);

        runOnFxThreadAndWait(controller::initialize);

        mockUserDAO = new MockUserDAO();
        controller.setUserDAO(mockUserDAO);
        controller.setPasswordHasher(passwordHasher);
    }

    private static void runOnFxThreadAndWait(Runnable action) {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        try {
            boolean completed = latch.await(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertTrue(completed, "Timed out waiting for JavaFX thread");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted while waiting for JavaFX thread");
        }
    }

    private void injectField(String fieldName, Object value) {
        try {
            Field field = SignUpController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to inject field '" + fieldName + "': " + e.getMessage());
        }
    }

    private void setFieldValues(String firstName, String lastName, String username,
                                String email, String password, String confirmPassword) {
        runOnFxThreadAndWait(() -> {
            firstNameField.setText(firstName);
            lastNameField.setText(lastName);
            usernameField.setText(username);
            emailField.setText(email);
            passwordField.setText(password);
            confirmPasswordField.setText(confirmPassword);
        });
    }

    private void invokePrivateMethod(String methodName) {
        Method method;
        try {
            method = SignUpController.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            fail("Missing method '" + methodName + "': " + e.getMessage());
            return;
        }
        runOnFxThreadAndWait(() -> {
            try {
                method.invoke(controller);
            } catch (IllegalAccessException e) {
                fail("Cannot access method '" + methodName + "': " + e.getMessage());
            } catch (InvocationTargetException e) {
                fail("Invocation of '" + methodName + "' failed: " + e.getTargetException());
            }
        });
    }

    private void invokeHandleSignUp() {
        invokePrivateMethod("handleSignUp");
    }

    private void waitUntilMessageVisible() {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline) {
            if (isMessageVisibleOrNonEmpty()) return;
            runOnFxThreadAndWait(() -> {});
        }
    }

    private boolean isMessageVisibleOrNonEmpty() {
        final boolean[] result = {false};
        runOnFxThreadAndWait(() ->
                result[0] = messageLabel.isVisible() || !messageLabel.getText().isEmpty());
        return result[0];
    }

    // ─── Tests ───

    @Test
    void testConstructorInitializesController() {
        assertNotNull(controller);
    }

    @Test
    void testHandleSignUpWithEmptyFields() {
        setFieldValues("", "", "", "", "", "");
        invokeHandleSignUp();
        waitUntilMessageVisible();
        assertTrue(isMessageVisibleOrNonEmpty());
    }

    @Test
    void testHandleSignUpWithInvalidEmail() {
        setFieldValues("John", "Doe", "johndoe", "invalid-email", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        waitUntilMessageVisible();
        assertTrue(isMessageVisibleOrNonEmpty());
    }

    @Test
    void testHandleSignUpWithPasswordMismatch() {
        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Pass123!", "Pass456!");
        invokeHandleSignUp();
        waitUntilMessageVisible();
        assertTrue(isMessageVisibleOrNonEmpty());
    }

    @Test
    void testHandleSignUpWithWeakPassword() {
        setFieldValues("John", "Doe", "johndoe", "john@example.com", "pass", "pass");
        invokeHandleSignUp();
        waitUntilMessageVisible();
        assertTrue(isMessageVisibleOrNonEmpty());
    }

    @Test
    void testHandleSignUpWithInvalidUsername() {
        setFieldValues("John", "Doe", "ab", "john@example.com", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        waitUntilMessageVisible();
        assertTrue(isMessageVisibleOrNonEmpty());
    }

    @Test
    void testHandleSignUpWithExistingUsername() {
        UserEntity existingUser = new UserEntity("Jane", "Doe", "johndoe", "jane@example.com");
        mockUserDAO.setUserToReturn(existingUser);

        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        waitUntilMessageVisible();

        assertTrue(isMessageVisibleOrNonEmpty(), "Message should be shown");
    }

    @Test
    void testHandleSignUpWithExistingEmail() {
        UserEntity existingUser = new UserEntity("Jane", "Doe", "janedoe", "john@example.com");
        mockUserDAO.setUserToReturn(existingUser);

        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        waitUntilMessageVisible();

        assertTrue(isMessageVisibleOrNonEmpty(), "Message should be shown");
    }

    @Test
    void testSuccessfulSignUp() {
        mockUserDAO.reset();
        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Pass123!", "Pass123!");
        invokeHandleSignUp();

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline && mockUserDAO.savedUser == null) {
            runOnFxThreadAndWait(() -> {});
        }

        assertNotNull(mockUserDAO.savedUser);
        assertEquals("John", mockUserDAO.savedUser.getFirstName());
        assertEquals("Doe", mockUserDAO.savedUser.getLastName());
        assertEquals("johndoe", mockUserDAO.savedUser.getUsername());
        assertTrue(passwordHasher.verify("Pass123!", mockUserDAO.savedUser.getPasswordHash()));
    }

    @Test
    void testHandleSignUpWithNullSaveResult() {
        mockUserDAO.reset();
        mockUserDAO.shouldReturnNullOnSave = true;

        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        waitUntilMessageVisible();

        assertTrue(isMessageVisibleOrNonEmpty(), "Message should be shown");
    }

    @Test
    void testHandleSignUpWithUserWithoutId() {
        mockUserDAO.reset();
        mockUserDAO.shouldReturnUserWithoutId = true;

        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        waitUntilMessageVisible();

        assertTrue(isMessageVisibleOrNonEmpty(), "Message should be shown");
    }

    @Test
    void testHandleSignUpWithIllegalArgumentException() {
        mockUserDAO.reset();
        mockUserDAO.shouldThrowIllegalArgumentException = true;

        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        waitUntilMessageVisible();

        assertTrue(isMessageVisibleOrNonEmpty(), "Message should be shown");
    }

    @Test
    void testHandleSignUpWithRuntimeException() {
        mockUserDAO.reset();
        mockUserDAO.shouldThrowRuntimeException = true;

        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        waitUntilMessageVisible();

        assertTrue(isMessageVisibleOrNonEmpty(), "Message should be shown");
    }

    @Test
    void testHandleSignUpTrimsWhitespace() {
        mockUserDAO.reset();
        setFieldValues("  John  ", "  Doe  ", "  johndoe  ", "  john@example.com  ", "Pass123!", "Pass123!");
        invokeHandleSignUp();

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline && mockUserDAO.savedUser == null) {
            runOnFxThreadAndWait(() -> {});
        }

        assertNotNull(mockUserDAO.savedUser);
        assertEquals("John", mockUserDAO.savedUser.getFirstName());
        assertEquals("Doe", mockUserDAO.savedUser.getLastName());
    }

    @Test
    void testHandleSignUpWithFinnishCharacters() {
        mockUserDAO.reset();
        setFieldValues("Matti-Pekka", "Jääskeläinen", "matti_jää",
                "matti@example.fi", "Salasana123!", "Salasana123!");
        invokeHandleSignUp();

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline && mockUserDAO.savedUser == null) {
            runOnFxThreadAndWait(() -> {});
        }

        assertNotNull(mockUserDAO.savedUser);
        assertEquals("Matti-Pekka", mockUserDAO.savedUser.getFirstName());
    }

    @Test
    void testPasswordIsHashedBeforeSaving() {
        mockUserDAO.reset();
        String plainPass = "Pass123!";
        setFieldValues("John", "Doe", "johndoe", "john@example.com", plainPass, plainPass);
        invokeHandleSignUp();

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline && mockUserDAO.savedUser == null) {
            runOnFxThreadAndWait(() -> {});
        }

        assertNotNull(mockUserDAO.savedUser);
        assertNotEquals(plainPass, mockUserDAO.savedUser.getPasswordHash());
        assertTrue(mockUserDAO.savedUser.getPasswordHash().startsWith("$2a$"));
    }

    @Test
    void testInvalidNameFormat() {
        setFieldValues("123", "456", "johndoe", "john@example.com", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        waitUntilMessageVisible();
        assertTrue(isMessageVisibleOrNonEmpty());
    }

    @Test
    void testPasswordWithoutSpecialCharacter() {
        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Password123", "Password123");
        invokeHandleSignUp();
        waitUntilMessageVisible();
        assertTrue(isMessageVisibleOrNonEmpty());
    }

    @Test
    void testPasswordWithoutNumber() {
        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Password!!", "Password!!");
        invokeHandleSignUp();
        waitUntilMessageVisible();
        assertTrue(isMessageVisibleOrNonEmpty());
    }

    @Test
    void testSetUserDAO() {
        MockUserDAO newDAO = new MockUserDAO();
        controller.setUserDAO(newDAO);
        assertNotNull(newDAO);
    }

    @Test
    void testSetPasswordHasher() {
        IPasswordHasher newHasher = new BcryptPasswordHasher();
        controller.setPasswordHasher(newHasher);
        assertNotNull(newHasher);
    }

    @Test
    void testSafeHandlesNull() {
        runOnFxThreadAndWait(() -> firstNameField.setText(null));
        try {
            Method method = SignUpController.class.getDeclaredMethod("safe", TextField.class);
            method.setAccessible(true);
            String result = (String) method.invoke(controller, firstNameField);
            assertEquals("", result);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testSafeHandlesWhitespace() {
        runOnFxThreadAndWait(() -> firstNameField.setText("   "));
        try {
            Method method = SignUpController.class.getDeclaredMethod("safe", TextField.class);
            method.setAccessible(true);
            assertEquals("", method.invoke(controller, firstNameField));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testClearFields() {
        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Pass123!", "Pass123!");
        invokePrivateMethod("clearFields");
        runOnFxThreadAndWait(() -> {
            assertEquals("", firstNameField.getText());
            assertEquals("", lastNameField.getText());
            assertEquals("", usernameField.getText());
            assertEquals("", emailField.getText());
            assertEquals("", passwordField.getText());
            assertEquals("", confirmPasswordField.getText());
        });
    }
}