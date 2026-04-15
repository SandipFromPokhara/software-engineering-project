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
import security.Validation;
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
                // Surface the underlying exception to make failures actionable
                fail("Invocation of '" + methodName + "' failed: " + e.getTargetException());
            }
        });
    }

    private void invokeHandleSignUp() {
        invokePrivateMethod("handleSignUp");
    }

    private void waitUntilMessageVisible() {
        // Avoid Thread.sleep; poll briefly on the FX thread.
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
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

        // wait until DAO was called
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline && mockUserDAO.savedUser == null) {
            runOnFxThreadAndWait(() -> {
                // no-op
            });
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

    @Test
    void testSetUserDAO() {
        MockUserDAO newDAO = new MockUserDAO();
        controller.setUserDAO(newDAO);
        assertNotNull(newDAO);
    }

    @Test
    void testHandleSignUpTrimsWhitespace() {
        mockUserDAO.reset();
        setFieldValues("  John  ", "  Doe  ", "  johndoe  ", "  john@example.com  ", "Pass123!", "Pass123!");
        invokeHandleSignUp();

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline && mockUserDAO.savedUser == null) {
            runOnFxThreadAndWait(() -> {
                // no-op
            });
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
        String plainPass = "Pass123!";
        setFieldValues("John", "Doe", "johndoe", "john@example.com", plainPass, plainPass);
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
    void testPasswordStrengthBarInitiallyHidden() {
        runOnFxThreadAndWait(() -> {
            assertFalse(passwordStrengthBar.isVisible());
            assertFalse(passwordStrengthLabel.isVisible());
        });
    }

    @Test
    void testPasswordStrengthBarShowsOnInput() {
        runOnFxThreadAndWait(() -> passwordField.setText("Pass123!"));

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
    //added tests for sign up button enabling logic
    @Test
    void testSignUpButtonEnabledWhenValid() {
        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Pass123!", "Pass123!");

        runOnFxThreadAndWait(() -> {
            // Trigger validation listeners
            passwordField.setText("Pass123!");
        });

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline) {
            final boolean[] disabled = {true};
            runOnFxThreadAndWait(() -> disabled[0] = signUpButton.isDisabled());

            if (!disabled[0]) return;
        }

        fail("Sign up button did not become enabled");
    }
    //added test for safe() method to ensure it handles null TextField without throwing
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
    //added test to ensure resetStyles() removes error styles from fields
    @Test
    void testResetStylesRemovesErrorClass() {
        runOnFxThreadAndWait(() -> {
            firstNameField.getStyleClass().add("input-error");
            lastNameField.getStyleClass().add("input-error");
        });

        invokePrivateMethod("resetStyles");

        runOnFxThreadAndWait(() -> {
            assertFalse(firstNameField.getStyleClass().contains("input-error"));
            assertFalse(lastNameField.getStyleClass().contains("input-error"));
        });
    }
    //added test to verify addErrorStyle and removeErrorStyle correctly modify the style class of a Control
    @Test
    void testAddAndRemoveErrorStyle() {
        try {
            Method add = SignUpController.class.getDeclaredMethod("addErrorStyle", Control.class);
            Method remove = SignUpController.class.getDeclaredMethod("removeErrorStyle", Control.class);
            add.setAccessible(true);
            remove.setAccessible(true);

            runOnFxThreadAndWait(() -> {
                try {
                    add.invoke(controller, firstNameField);
                    assertTrue(firstNameField.getStyleClass().contains("input-error"));

                    remove.invoke(controller, firstNameField);
                    assertFalse(firstNameField.getStyleClass().contains("input-error"));
                } catch (Exception e) {
                    fail(e);
                }
            });

        } catch (Exception e) {
            fail(e);
        }
    }
    //added test to verify setStrengthBarVisible correctly shows/hides the password strength bar
    @Test
    void testSetStrengthBarVisible() {
        try {
            Method method = SignUpController.class.getDeclaredMethod("setStrengthBarVisible", boolean.class);
            method.setAccessible(true);

            runOnFxThreadAndWait(() -> {
                try {
                    method.invoke(controller, true);
                    assertTrue(passwordStrengthBar.isVisible());

                    method.invoke(controller, false);
                    assertFalse(passwordStrengthBar.isVisible());
                } catch (Exception e) {
                    fail(e);
                }
            });

        } catch (Exception e) {
            fail(e);
        }
    }
    @Test
    void testPasswordStrengthWeak() {
        runOnFxThreadAndWait(() -> passwordField.setText("abc"));

        waitUntilMessageVisible();

        runOnFxThreadAndWait(() ->
                assertFalse(passwordStrengthLabel.getText().isEmpty())
        );
    }
    //added test to verify that the password strength bar hides after entering a strong password

    @Test
    void testPasswordStrengthStrongHidesLater() {
        runOnFxThreadAndWait(() -> passwordField.setText("Pass123!Strong"));

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (System.nanoTime() < deadline) {
            final boolean[] visible = {true};
            runOnFxThreadAndWait(() -> visible[0] = passwordStrengthBar.isVisible());

            if (!visible[0]) return;
        }

        fail("Strength bar did not hide after strong password");
    }
    //added test to verify that getControlForField returns the correct Control for known field names and null for unknown names
    @Test
    void testGetControlForField() throws Exception {
        Method method = SignUpController.class.getDeclaredMethod("getControlForField", String.class);
        method.setAccessible(true);

        assertEquals(firstNameField, method.invoke(controller, "firstName"));
        assertEquals(passwordField, method.invoke(controller, "password"));
        assertNull(method.invoke(controller, "unknown"));
    }
    //added test to verify that showValidationErrors correctly handles a ValidationResult with no errors without throwing exceptions
    @Test
    void testShowValidationErrorsWithNoErrors() throws Exception {
        Validation.ValidationResult result =
                new Validation.ValidationResult(true, java.util.Map.of());

        Method method = SignUpController.class.getDeclaredMethod("showValidationErrors", Validation.ValidationResult.class);
        method.setAccessible(true);

        runOnFxThreadAndWait(() -> {
            try {
                method.invoke(controller, result);
            } catch (Exception e) {
                fail(e);
            }
        });

        assertTrue(true); // just ensure no crash
    }
}
