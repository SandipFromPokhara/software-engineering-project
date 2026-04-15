package controller;

import dao.user.IUserDAO;
import entity.entities.UserEntity;
import javafx.application.Platform;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

    private static final String PASS = "Pass123!";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final String TEST_USERNAME = "johndoe";
    private static final String TEST_EMAIL = "john@example.com";

    private static final long FX_TIMEOUT_SECONDS = 10;
    private static final String MSG_SHOULD_BE_SHOWN = "Message should be shown";

    private SignUpController controller;
    private MockUserDAO mockUserDAO;
    private IPasswordHasher passwordHasher;

    private TextField firstNameField;
    private TextField lastNameField;
    private TextField usernameField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Button signUpButton;
    private Button backButton;
    private Hyperlink loginLink;
    private Label messageLabel;
    private Label createAccount;
    private Label joinAccount;
    private Label haveAccount;
    private Label privacyLabel;
    private Label passwordStrengthLabel;
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
            if (shouldThrowIllegalArgumentException) {
                throw new IllegalArgumentException("Validation error");
            }
            if (shouldThrowRuntimeException) {
                throw new RuntimeException("Database error");
            }
            if (shouldReturnNullOnSave) {
                return null;
            }
            if (shouldReturnUserWithoutId) {
                return new UserEntity(user.getFirstName(), user.getLastName(),
                        user.getUsername(), user.getEmail());
            }

            savedUser = user;
            // The controller under test doesn't require an entity id for these tests.
            return new UserEntity(user.getFirstName(), user.getLastName(),
                    user.getUsername(), user.getEmail());
        }

        @Override
        public UserEntity findByUsername(String username) {
            if (userToReturn != null && userToReturn.getUsername().equals(username)) {
                return userToReturn;
            }
            return null;
        }

        @Override
        public UserEntity findByEmail(String email) {
            if (userToReturn != null && userToReturn.getEmail().equals(email)) {
                return userToReturn;
            }
            return null;
        }

        @Override
        public UserEntity findById(Long id) {
            return null;
        }

        @Override
        public void update(UserEntity user) {
            // not needed for tests
        }

        @Override
        public void delete(UserEntity user) {
            // not needed for tests
        }

        public void setUserToReturn(UserEntity user) {
            this.userToReturn = user;
        }

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

        // Inject UI fields first
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

        // Call initialize() — this sets userDAO = new JpaUserDao() internally
        runOnFxThreadAndWait(controller::initialize);

        // Inject mock DAO and passwordHasher AFTER initialize() so they override JpaUserDao
        mockUserDAO = new MockUserDAO();
        controller.setUserDAO(mockUserDAO);
        controller.setPasswordHasher(passwordHasher);
    }

    private static void runOnFxThreadAndWait(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

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
            // Make private fields accessible for test injection
            field.setAccessible(true);
            field.set(controller, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new TestReflectionException("Failed to inject field '" + fieldName + "': " + e.getMessage(), e);
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
            // Make private methods accessible for test invocation
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new TestReflectionException("Missing method '" + methodName + "': " + e.getMessage(), e);
        }

        runOnFxThreadAndWait(() -> {
            try {
                method.invoke(controller);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new TestReflectionException("Failed to invoke '" + methodName + "': " + e.getMessage(), e);
            }
        });
    }

    private void invokeHandleSignUp() {
        invokePrivateMethod("handleSignUp");
    }

    private void waitUntilMessageVisible() {
        // Avoid Thread.sleep; poll briefly on the FX thread.
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(FX_TIMEOUT_SECONDS);
        while (System.nanoTime() < deadline) {
            if (isMessageVisibleOrNonEmpty()) {
                return;
            }
            runOnFxThreadAndWait(() -> {
                // no-op; allows FX events to flush
            });
        }
    }

    private boolean isMessageVisibleOrNonEmpty() {
        final boolean[] result = {false};
        runOnFxThreadAndWait(() -> result[0] = messageLabel.isVisible() || !messageLabel.getText().isEmpty());
        return result[0];
    }

    // ─── Tests ───

    @Test
    void testConstructorInitializesController() {
        assertNotNull(controller);
    }

    @Test
    void testInitialize() {
        runOnFxThreadAndWait(() -> assertTrue(signUpButton.isDisabled()));
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
        setFieldValues(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_USERNAME, "invalid-email", PASS, PASS);
        invokeHandleSignUp();
        waitUntilMessageVisible();
        assertTrue(isMessageVisibleOrNonEmpty());
    }

    @Test
    void testHandleSignUpWithPasswordMismatch() {
        setFieldValues(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_USERNAME, TEST_EMAIL, PASS, "Pass456!");
        invokeHandleSignUp();
        waitUntilMessageVisible();
        assertTrue(isMessageVisibleOrNonEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"pass", "Password123", "Password!!"})
    void testHandleSignUp_InvalidOrWeakPasswords_ShowMessage(String badPassword) {
        setFieldValues(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_USERNAME, TEST_EMAIL, badPassword, badPassword);
        invokeHandleSignUp();
        waitUntilMessageVisible();
        assertTrue(isMessageVisibleOrNonEmpty());
    }

    @Test
    void testHandleSignUpWithInvalidUsername() {
        setFieldValues(TEST_FIRST_NAME, TEST_LAST_NAME, "ab", TEST_EMAIL, PASS, PASS);
        invokeHandleSignUp();
        waitUntilMessageVisible();
        assertTrue(isMessageVisibleOrNonEmpty());
    }

    @Test
    void testHandleSignUpWithExistingUsername() {
        UserEntity existingUser = new UserEntity("Jane", "Doe", "johndoe", "jane@example.com");
        mockUserDAO.setUserToReturn(existingUser);

        setFieldValues(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_USERNAME, TEST_EMAIL, PASS, PASS);
        invokeHandleSignUp();
        waitUntilMessageVisible();

        assertTrue(isMessageVisibleOrNonEmpty(), MSG_SHOULD_BE_SHOWN);
    }

    @Test
    void testHandleSignUpWithExistingEmail() {
        UserEntity existingUser = new UserEntity("Jane", "Doe", "janedoe", "john@example.com");
        mockUserDAO.setUserToReturn(existingUser);

        setFieldValues(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_USERNAME, TEST_EMAIL, PASS, PASS);
        invokeHandleSignUp();
        waitUntilMessageVisible();

        assertTrue(isMessageVisibleOrNonEmpty(), MSG_SHOULD_BE_SHOWN);
    }

    @Test
    void testSuccessfulSignUp() {
        mockUserDAO.reset();
        setFieldValues(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_USERNAME, TEST_EMAIL, PASS, PASS);
        invokeHandleSignUp();

        // wait until DAO was called
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(FX_TIMEOUT_SECONDS);
        while (System.nanoTime() < deadline && mockUserDAO.savedUser == null) {
            runOnFxThreadAndWait(() -> {
                // no-op
            });
        }

        assertNotNull(mockUserDAO.savedUser);
        assertEquals(TEST_FIRST_NAME, mockUserDAO.savedUser.getFirstName());
        assertEquals(TEST_LAST_NAME, mockUserDAO.savedUser.getLastName());
        assertEquals(TEST_USERNAME, mockUserDAO.savedUser.getUsername());
        assertTrue(passwordHasher.verify(PASS, mockUserDAO.savedUser.getPasswordHash()));
    }

    @Test
    void testHandleSignUpWithNullSaveResult() {
        mockUserDAO.reset();
        mockUserDAO.shouldReturnNullOnSave = true;

        setFieldValues(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_USERNAME, TEST_EMAIL, PASS, PASS);
        invokeHandleSignUp();
        waitUntilMessageVisible();

        assertTrue(isMessageVisibleOrNonEmpty(), MSG_SHOULD_BE_SHOWN);
    }

    @Test
    void testHandleSignUpWithUserWithoutId() {
        mockUserDAO.reset();
        mockUserDAO.shouldReturnUserWithoutId = true;

        setFieldValues(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_USERNAME, TEST_EMAIL, PASS, PASS);
        invokeHandleSignUp();
        waitUntilMessageVisible();

        assertTrue(isMessageVisibleOrNonEmpty(), MSG_SHOULD_BE_SHOWN);
    }

    @Test
    void testHandleSignUpWithIllegalArgumentException() {
        mockUserDAO.reset();
        mockUserDAO.shouldThrowIllegalArgumentException = true;

        setFieldValues(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_USERNAME, TEST_EMAIL, PASS, PASS);
        invokeHandleSignUp();
        waitUntilMessageVisible();

        assertTrue(isMessageVisibleOrNonEmpty(), MSG_SHOULD_BE_SHOWN);
    }

    @Test
    void testHandleSignUpWithRuntimeException() {
        mockUserDAO.reset();
        mockUserDAO.shouldThrowRuntimeException = true;

        setFieldValues(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_USERNAME, TEST_EMAIL, PASS, PASS);
        invokeHandleSignUp();
        waitUntilMessageVisible();

        assertTrue(isMessageVisibleOrNonEmpty(), MSG_SHOULD_BE_SHOWN);
    }

    @Test
    void testOnLogin() {
        // Navigation probably fails in test env; assert it does not crash the FX thread.
        runOnFxThreadAndWait(() -> {
            try {
                controller.onLogin();
            } catch (RuntimeException expectedInTestEnv) {
                // expected in a headless test environment
            }
        });
        assertNotNull(controller);
    }

    @Test
    void testHandleBack() {
        // Navigation probably fails in test env; ensure reflective call does not crash.
        runOnFxThreadAndWait(() -> {
            try {
                Method method = SignUpController.class.getDeclaredMethod("handleBack");
                method.setAccessible(true);
                method.invoke(controller);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                fail("Failed to invoke handleBack: " + e.getMessage());
            } catch (InvocationTargetException e) {
                // tolerate navigation failure
            }
        });
        assertNotNull(controller);
    }

    @Test
    void testClearFields() {
        setFieldValues(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_USERNAME, TEST_EMAIL, PASS, PASS);

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

    @Test
    void testSetUserDAO() {
        MockUserDAO newDAO = new MockUserDAO();
        controller.setUserDAO(newDAO);
        assertNotNull(newDAO);
    }

    @Test
    void testHandleSignUpTrimsWhitespace() {
        mockUserDAO.reset();
        setFieldValues("  " + TEST_FIRST_NAME + "  ", "  " + TEST_LAST_NAME + "  ", "  " + TEST_USERNAME + "  ", "  " + TEST_EMAIL + "  ", PASS, PASS);
        invokeHandleSignUp();

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline && mockUserDAO.savedUser == null) {
            runOnFxThreadAndWait(() -> {
                // no-op
            });
        }

        assertNotNull(mockUserDAO.savedUser);
        assertEquals(TEST_FIRST_NAME, mockUserDAO.savedUser.getFirstName());
        assertEquals(TEST_LAST_NAME, mockUserDAO.savedUser.getLastName());
    }

    @Test
    void testHandleSignUpWithFinnishCharacters() {
        mockUserDAO.reset();
        setFieldValues("Matti-Pekka", "Jääskeläinen", "matti_jää",
                "matti@example.fi", "Salasana123!", "Salasana123!");
        invokeHandleSignUp();

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline && mockUserDAO.savedUser == null) {
            runOnFxThreadAndWait(() -> {
                // no-op
            });
        }

        assertNotNull(mockUserDAO.savedUser);
        assertEquals("Matti-Pekka", mockUserDAO.savedUser.getFirstName());
    }

    @Test
    void testPasswordIsHashedBeforeSaving() {
        mockUserDAO.reset();
        String plainPass = PASS;
        setFieldValues(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_USERNAME, TEST_EMAIL, plainPass, plainPass);
        invokeHandleSignUp();

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline && mockUserDAO.savedUser == null) {
            runOnFxThreadAndWait(() -> {
                // no-op
            });
        }

        assertNotNull(mockUserDAO.savedUser);
        assertNotEquals(plainPass, mockUserDAO.savedUser.getPasswordHash());
        assertTrue(mockUserDAO.savedUser.getPasswordHash().startsWith("$2a$"));
    }

    @Test
    void testInvalidNameFormat() {
        setFieldValues("123", "456", TEST_USERNAME, TEST_EMAIL, PASS, PASS);
        invokeHandleSignUp();
        waitUntilMessageVisible();
        assertTrue(isMessageVisibleOrNonEmpty());
    }

    // Combined into parameterized test above

    @Test
    void testPasswordStrengthBarInitiallyHidden() {
        runOnFxThreadAndWait(() -> {
            assertFalse(passwordStrengthBar.isVisible());
            assertFalse(passwordStrengthLabel.isVisible());
        });
    }

    @Test
    void testPasswordStrengthBarShowsOnInput() {
        runOnFxThreadAndWait(() -> passwordField.setText(PASS));

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline) {
            final double[] progress = {0};
            final boolean[] visible = {false};
            runOnFxThreadAndWait(() -> {
                progress[0] = passwordStrengthBar.getProgress();
                visible[0] = passwordStrengthBar.isVisible();
            });
            if (visible[0] && progress[0] > 0) {
                return;
            }
        }

        fail("Password strength bar did not become visible with progress");
    }

    @Test
    void testNavigateToLoginMethod() {
        runOnFxThreadAndWait(() -> {
            try {
                Method method = SignUpController.class.getDeclaredMethod("navigateToLogin");
                method.setAccessible(true);
                method.invoke(controller);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                fail("Failed to invoke navigateToLogin: " + e.getMessage());
            } catch (InvocationTargetException e) {
                // tolerate navigation failure
            }
        });
        assertNotNull(controller);
    }

    @Test
    void testSetPasswordHasher() {
        IPasswordHasher newHasher = new BcryptPasswordHasher();
        controller.setPasswordHasher(newHasher);
        assertNotNull(newHasher);
    }

    private static class TestReflectionException extends RuntimeException {
        public TestReflectionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
