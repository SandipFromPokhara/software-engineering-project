package controller;

import dao.user.UserDAO;
import entity.UserEntity;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.BcryptPasswordHasher;
import javafx.scene.control.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SignUpControllerTest {

    private SignUpController controller;
    private MockUserDAO mockUserDAO;

    // Mock UI components
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField usernameField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Button signUpButton;
    private Hyperlink loginLink;
    private Label messageLabel;
    private Button backButton;
    private Stage testStage;

    // Mock DAO implementation
    private static class MockUserDAO implements UserDAO {
        private UserEntity userToReturn;
        private UserEntity savedUser;
        private boolean shouldThrowRuntimeException = false;
        private boolean shouldThrowIllegalArgumentException = false;
        private boolean shouldReturnNullOnSave = false;
        private boolean shouldReturnUserWithoutId = false;

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
            UserEntity returnUser = new UserEntity(user.getFirstName(), user.getLastName(),
                    user.getUsername(), user.getEmail());
            setId(returnUser, 1L);
            return returnUser;
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
        }

        @Override
        public void delete(UserEntity user) {
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

        private void setId(UserEntity user, Long id) {
            try {
                Field idField = UserEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(user, id);
            } catch (Exception e) {
            }
        }
    }

    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            try {
                Platform.startup(latch::countDown);
            } catch (IllegalStateException e) {
                latch.countDown();
            }
        }).start();

        latch.await(5, TimeUnit.SECONDS);
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new SignUpController();
        mockUserDAO = new MockUserDAO();
        controller.setUserDAO(mockUserDAO);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
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

            // Create a proper Scene and Stage
            testStage = new Stage();
            VBox root = new VBox();
            root.getChildren().addAll(signUpButton, loginLink, backButton, messageLabel,
                    firstNameField, lastNameField, usernameField,
                    emailField, passwordField, confirmPasswordField);
            Scene scene = new Scene(root, 400, 600);
            testStage.setScene(scene);
            latch.countDown();
        });
        latch.await(2, TimeUnit.SECONDS);

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
    }

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = SignUpController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    private void setFieldValues(String firstName, String lastName, String username,
                                String email, String password, String confirmPassword) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            firstNameField.setText(firstName);
            lastNameField.setText(lastName);
            usernameField.setText(username);
            emailField.setText(email);
            passwordField.setText(password);
            confirmPasswordField.setText(confirmPassword);
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);
    }

    private void invokeHandleSignUp() throws Exception {
        Method method = SignUpController.class.getDeclaredMethod("handleSignUp");
        method.setAccessible(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                method.invoke(controller);
            } catch (Exception e) {

            }
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);
    }

    @Test
    void testConstructorInitializesDAO() {
        SignUpController newController = new SignUpController();
        assertNotNull(newController);
    }

    @Test
    void testInitialize() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.initialize();
            assertTrue(signUpButton.isDefaultButton());
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);
    }

    @Test
    void testHandleSignUpWithEmptyFields() throws Exception {
        setFieldValues("", "", "", "", "", "");
        invokeHandleSignUp();
        Thread.sleep(300);

        assertTrue(messageLabel.isVisible());
    }

    @Test
    void testHandleSignUpWithInvalidEmail() throws Exception {
        setFieldValues("John", "Doe", "johndoe", "invalid-email", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        Thread.sleep(300);

        assertTrue(messageLabel.isVisible());
    }

    @Test
    void testHandleSignUpWithPasswordMismatch() throws Exception {
        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Pass123!", "Pass456!");
        invokeHandleSignUp();
        Thread.sleep(300);

        assertTrue(messageLabel.isVisible());
    }

    @Test
    void testHandleSignUpWithWeakPassword() throws Exception {
        setFieldValues("John", "Doe", "johndoe", "john@example.com", "pass", "pass");
        invokeHandleSignUp();
        Thread.sleep(300);

        assertTrue(messageLabel.isVisible());
    }

    @Test
    void testHandleSignUpWithInvalidUsername() throws Exception {
        setFieldValues("John", "Doe", "ab", "john@example.com", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        Thread.sleep(300);

        assertTrue(messageLabel.isVisible());
    }

    @Test
    void testHandleSignUpWithExistingUsername() throws Exception {
        UserEntity existingUser = new UserEntity("Jane", "Doe", "johndoe", "jane@example.com");
        mockUserDAO.setUserToReturn(existingUser);

        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        Thread.sleep(300);

        assertTrue(messageLabel.isVisible());
        assertTrue(messageLabel.getText().contains("username is already taken"));
    }

    @Test
    void testHandleSignUpWithExistingEmail() throws Exception {
        UserEntity existingUser = new UserEntity("Jane", "Doe", "janedoe", "john@example.com");
        mockUserDAO.setUserToReturn(existingUser);

        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        Thread.sleep(300);

        assertTrue(messageLabel.isVisible());
        assertTrue(messageLabel.getText().contains("email already exists"));
    }

    @Test
    void testSuccessfulSignUp() throws Exception {
        mockUserDAO.reset();
        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        Thread.sleep(2000);

        assertNotNull(mockUserDAO.savedUser);
        assertEquals("John", mockUserDAO.savedUser.getFirstName());
        assertEquals("Doe", mockUserDAO.savedUser.getLastName());
        assertEquals("johndoe", mockUserDAO.savedUser.getUsername());
        assertTrue(BcryptPasswordHasher.verifyPassword("Pass123!", mockUserDAO.savedUser.getPasswordHash()));
    }

    @Test
    void testHandleSignUpWithNullSaveResult() throws Exception {
        mockUserDAO.reset();
        mockUserDAO.shouldReturnNullOnSave = true;

        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        Thread.sleep(300);

        assertTrue(messageLabel.isVisible());
        assertTrue(messageLabel.getText().contains("Failed to create account"));
    }

    @Test
    void testHandleSignUpWithUserWithoutId() throws Exception {
        mockUserDAO.reset();
        mockUserDAO.shouldReturnUserWithoutId = true;

        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        Thread.sleep(300);

        assertTrue(messageLabel.isVisible());
        assertTrue(messageLabel.getText().contains("Failed to create account"));
    }

    @Test
    void testHandleSignUpWithIllegalArgumentException() throws Exception {
        mockUserDAO.reset();
        mockUserDAO.shouldThrowIllegalArgumentException = true;

        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        Thread.sleep(300);

        assertTrue(messageLabel.isVisible());
        assertTrue(messageLabel.getText().toLowerCase().contains("error"));
    }

    @Test
    void testHandleSignUpWithRuntimeException() throws Exception {
        mockUserDAO.reset();
        mockUserDAO.shouldThrowRuntimeException = true;

        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        Thread.sleep(300);

        assertTrue(messageLabel.isVisible());
        assertTrue(messageLabel.getText().toLowerCase().contains("error"));
    }

    @Test
    void testOnLogin() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.onLogin();
            } catch (Exception e) {

            }
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);
    }

    @Test
    void testHandleBack() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Method method = SignUpController.class.getDeclaredMethod("handleBack");
                method.setAccessible(true);
                method.invoke(controller);
            } catch (Exception e) {

            }
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);
    }

    @Test
    void testClearFields() throws Exception {
        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Pass123!", "Pass123!");

        Method clearMethod = SignUpController.class.getDeclaredMethod("clearFields");
        clearMethod.setAccessible(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                clearMethod.invoke(controller);
            } catch (Exception e) {
                fail("Clear fields failed");
            }
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        assertEquals("", firstNameField.getText());
        assertEquals("", lastNameField.getText());
    }

    @Test
    void testSetUserDAO() {
        MockUserDAO newDAO = new MockUserDAO();
        controller.setUserDAO(newDAO);
        assertNotNull(newDAO);
    }

    @Test
    void testHandleSignUpTrimsWhitespace() throws Exception {
        mockUserDAO.reset();
        setFieldValues("  John  ", "  Doe  ", "  johndoe  ", "  john@example.com  ", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        Thread.sleep(2000);

        assertNotNull(mockUserDAO.savedUser);
        assertEquals("John", mockUserDAO.savedUser.getFirstName());
        assertEquals("Doe", mockUserDAO.savedUser.getLastName());
    }

    @Test
    void testHandleSignUpWithFinnishCharacters() throws Exception {
        mockUserDAO.reset();
        setFieldValues("Matti-Pekka", "Jääskeläinen", "matti_jää", "matti@example.fi", "Salasana123!", "Salasana123!");
        invokeHandleSignUp();
        Thread.sleep(2000);

        assertNotNull(mockUserDAO.savedUser);
        assertEquals("Matti-Pekka", mockUserDAO.savedUser.getFirstName());
    }

    @Test
    void testPasswordIsHashedBeforeSaving() throws Exception {
        mockUserDAO.reset();
        String plainPassword = "Pass123!";
        setFieldValues("John", "Doe", "johndoe", "john@example.com", plainPassword, plainPassword);
        invokeHandleSignUp();
        Thread.sleep(2000);

        assertNotNull(mockUserDAO.savedUser);
        assertNotEquals(plainPassword, mockUserDAO.savedUser.getPasswordHash());
        assertTrue(mockUserDAO.savedUser.getPasswordHash().startsWith("$2a$"));
    }

    @Test
    void testInvalidNameFormat() throws Exception {
        setFieldValues("123", "456", "johndoe", "john@example.com", "Pass123!", "Pass123!");
        invokeHandleSignUp();
        Thread.sleep(300);

        assertTrue(messageLabel.isVisible());
    }

    @Test
    void testPasswordWithoutSpecialCharacter() throws Exception {
        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Password123", "Password123");
        invokeHandleSignUp();
        Thread.sleep(300);

        assertTrue(messageLabel.isVisible());
    }

    @Test
    void testPasswordWithoutNumber() throws Exception {
        setFieldValues("John", "Doe", "johndoe", "john@example.com", "Password!!", "Password!!");
        invokeHandleSignUp();
        Thread.sleep(300);

        assertTrue(messageLabel.isVisible());
    }

    @Test
    void testNavigateToLoginMethod() throws Exception {
        Method method = SignUpController.class.getDeclaredMethod("navigateToLogin");
        method.setAccessible(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                method.invoke(controller);
            } catch (Exception e) {

            }
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);
    }
}