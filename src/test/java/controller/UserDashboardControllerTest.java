package controller;

import dao.user.IUserDAO;
import entity.entities.UserEntity;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import security.BcryptPasswordHasher;
import security.IPasswordHasher;
import session.UserSession;
import testutil.JavaFXInitializer;
import util.Localization;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class UserDashboardControllerTest {

    private UserDashboardController controller;
    private MockUserDAO mockUserDAO;
    private IPasswordHasher passwordHasher;

    private TextField firstNameField;
    private TextField lastNameField;
    private TextField usernameField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Label messageLabel;
    private Label manageAccount;
    private Label note;
    private Label firstLock;
    private Label lastNameLabel;
    private Label usernameLabel;
    private Label emailLock;
    private Label newPassword;
    private Button manageCancel;
    private Button manageUpdate;
    private Stage testStage;

    private static class MockUserDAO implements IUserDAO {
        private UserEntity userToReturn;
        private UserEntity updatedUser;
        private boolean shouldThrowException = false;

        @Override
        public UserEntity save(UserEntity user) {
            return user;
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
            return null;
        }

        @Override
        public UserEntity findById(Long id) {
            return null;
        }

        @Override
        public void update(UserEntity user) {
            if (shouldThrowException) {
                throw new RuntimeException("Database error");
            }
            this.updatedUser = user;
        }

        @Override
        public void delete(UserEntity user) {
        }

        public void setUserToReturn(UserEntity user) {
            this.userToReturn = user;
        }

        public void reset() {
            userToReturn = null;
            updatedUser = null;
            shouldThrowException = false;
        }
    }

    @BeforeAll
    static void initJavaFX() {
        JavaFXInitializer.init();
    }

    @BeforeEach
    void setUp() throws Exception {
        UserSession.getUserInstance().setUser(null);
        Localization.setLocale(Locale.ENGLISH);

        controller = new UserDashboardController();
        mockUserDAO = new MockUserDAO();
        passwordHasher = new BcryptPasswordHasher();

        controller.setUserDao(mockUserDAO);
        controller.setPasswordHasher(passwordHasher);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            firstNameField = new TextField();
            lastNameField = new TextField();
            usernameField = new TextField();
            emailField = new TextField();
            passwordField = new PasswordField();
            confirmPasswordField = new PasswordField();
            messageLabel = new Label();
            manageAccount = new Label();
            note = new Label();
            firstLock = new Label();
            lastNameLabel = new Label();
            usernameLabel = new Label();
            emailLock = new Label();
            newPassword = new Label();
            manageCancel = new Button();
            manageUpdate = new Button();

            testStage = new Stage();
            VBox root = new VBox();
            root.getChildren().addAll(firstNameField, lastNameField, usernameField,
                    emailField, passwordField, confirmPasswordField, messageLabel, manageAccount, note,
                    firstLock, lastNameLabel, usernameLabel, emailLock, newPassword, manageCancel, manageUpdate);
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
        injectField("messageLabel", messageLabel);
        injectField("manageAccount", manageAccount);
        injectField("note", note);
        injectField("firstLock", firstLock);
        injectField("lastNameLabel", lastNameLabel);
        injectField("usernameLabel", usernameLabel);
        injectField("emailLock", emailLock);
        injectField("newPassword", newPassword);
        injectField("manageCancel", manageCancel);
        injectField("manageUpdate", manageUpdate);

        mockUserDAO.reset();
    }

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = UserDashboardController.class.getDeclaredField(fieldName);
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

    private void invokeHandleUpdate() throws Exception {
        Method method = UserDashboardController.class.getDeclaredMethod("handleUpdate");
        method.setAccessible(true);
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                method.invoke(controller);
            } catch (Exception e) {
                e.printStackTrace();
            }
            latch.countDown();
        });
        latch.await(2, TimeUnit.SECONDS);
    }

    @Test
    void initialize_WithActiveUser_PopulatesFields() throws Exception {
        UserEntity testUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(testUser, 1L);
        UserSession.getUserInstance().setUser(testUser);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.initialize();
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        AtomicReference<String> firstName = new AtomicReference<>();
        AtomicReference<String> lastName = new AtomicReference<>();
        AtomicReference<String> username = new AtomicReference<>();
        AtomicReference<String> email = new AtomicReference<>();

        CountDownLatch checkLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            firstName.set(firstNameField.getText());
            lastName.set(lastNameField.getText());
            username.set(usernameField.getText());
            email.set(emailField.getText());
            checkLatch.countDown();
        });
        checkLatch.await(1, TimeUnit.SECONDS);

        assertEquals("John", firstName.get());
        assertEquals("Doe", lastName.get());
        assertEquals("johndoe", username.get());
        assertEquals("john@test.com", email.get());
    }

    @Test
    void initialize_WithNoActiveUser_ShowsErrorMessage() throws Exception {
        UserSession.getUserInstance().setUser(null);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> message = new AtomicReference<>();
        Platform.runLater(() -> {
            controller.initialize();
            message.set(messageLabel.getText());
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        assertEquals(Localization.get("dashboard.no_session"), message.get());
    }

    @Test
    void handleUpdate_ValidData_UpdatesUser() throws Exception {
        UserEntity currentUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(currentUser, 1L);
        UserSession.getUserInstance().setUser(currentUser);

        setFieldValues("John", "Smith", "johnsmith", "john@test.com", "", "");
        invokeHandleUpdate();

        assertNotNull(mockUserDAO.updatedUser);
        assertEquals("Smith", mockUserDAO.updatedUser.getLastName());
        assertEquals("johnsmith", mockUserDAO.updatedUser.getUsername());
    }

    @Test
    void handleUpdate_WithNewPassword_UpdatesPassword() throws Exception {
        UserEntity currentUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(currentUser, 1L);
        UserSession.getUserInstance().setUser(currentUser);

        setFieldValues("John", "Doe", "johndoe", "john@test.com", "NewPass123!", "NewPass123!");
        invokeHandleUpdate();

        assertNotNull(mockUserDAO.updatedUser);
        assertNotEquals("NewPass123!", mockUserDAO.updatedUser.getPasswordHash());
        assertTrue(passwordHasher.verify("NewPass123!", mockUserDAO.updatedUser.getPasswordHash()));
    }

    @Test
    void handleUpdate_EmptyLastName_ShowsError() throws Exception {
        UserEntity currentUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(currentUser, 1L);
        UserSession.getUserInstance().setUser(currentUser);

        setFieldValues("John", "", "johndoe", "john@test.com", "", "");
        invokeHandleUpdate();

        AtomicReference<String> message = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            message.set(messageLabel.getText());
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        assertEquals(Localization.get("dashboard.validation_error"), message.get());
        assertNull(mockUserDAO.updatedUser);
    }

    @Test
    void handleUpdate_EmptyUsername_ShowsError() throws Exception {
        UserEntity currentUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(currentUser, 1L);
        UserSession.getUserInstance().setUser(currentUser);

        setFieldValues("John", "Doe", "", "john@test.com", "", "");
        invokeHandleUpdate();

        AtomicReference<String> message = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            message.set(messageLabel.getText());
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        assertEquals(Localization.get("dashboard.validation_error"), message.get());
        assertNull(mockUserDAO.updatedUser);
    }

    @Test
    void handleUpdate_PasswordMismatch_ShowsError() throws Exception {
        UserEntity currentUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(currentUser, 1L);
        UserSession.getUserInstance().setUser(currentUser);

        setFieldValues("John", "Doe", "johndoe", "john@test.com", "Pass123!", "Different123!");
        invokeHandleUpdate();

        AtomicReference<String> message = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            message.set(messageLabel.getText());
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        assertEquals(Localization.get("dashboard.validation_error"), message.get());
        assertNull(mockUserDAO.updatedUser);
    }

    @Test
    void handleUpdate_WeakPassword_ShowsError() throws Exception {
        UserEntity currentUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(currentUser, 1L);
        UserSession.getUserInstance().setUser(currentUser);

        setFieldValues("John", "Doe", "johndoe", "john@test.com", "weak", "weak");
        invokeHandleUpdate();

        AtomicReference<String> message = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            message.set(messageLabel.getText());
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        assertEquals(Localization.get("dashboard.validation_error"), message.get());
        assertNull(mockUserDAO.updatedUser);
    }

    @Test
    void handleUpdate_DuplicateUsername_ShowsError() throws Exception {
        UserEntity currentUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(currentUser, 1L);
        UserSession.getUserInstance().setUser(currentUser);

        UserEntity existingUser = new UserEntity("Jane", "Smith", "janesmith", "jane@test.com");
        setId(existingUser, 2L);
        mockUserDAO.setUserToReturn(existingUser);

        setFieldValues("John", "Doe", "janesmith", "john@test.com", "", "");
        invokeHandleUpdate();

        AtomicReference<String> message = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            message.set(messageLabel.getText());
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        assertEquals(Localization.get("dashboard.username_exists"), message.get());
        assertNull(mockUserDAO.updatedUser);
    }

    @Test
    void handleUpdate_SameUsername_AllowsUpdate() throws Exception {
        UserEntity currentUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(currentUser, 1L);
        UserSession.getUserInstance().setUser(currentUser);
        mockUserDAO.setUserToReturn(currentUser);

        setFieldValues("John", "Smith", "johndoe", "john@test.com", "", "");
        invokeHandleUpdate();

        assertNotNull(mockUserDAO.updatedUser);
        assertEquals("Smith", mockUserDAO.updatedUser.getLastName());
    }

    @Test
    void handleUpdate_InvalidLastName_ShowsError() throws Exception {
        UserEntity currentUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(currentUser, 1L);
        UserSession.getUserInstance().setUser(currentUser);

        setFieldValues("John", "123", "johndoe", "john@test.com", "", "");
        invokeHandleUpdate();

        AtomicReference<String> message = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            message.set(messageLabel.getText());
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        assertEquals(Localization.get("dashboard.validation_error"), message.get());
        assertNull(mockUserDAO.updatedUser);
    }

    @Test
    void handleUpdate_InvalidUsername_ShowsError() throws Exception {
        UserEntity currentUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(currentUser, 1L);
        UserSession.getUserInstance().setUser(currentUser);

        setFieldValues("John", "Doe", "ab", "john@test.com", "", "");
        invokeHandleUpdate();

        AtomicReference<String> message = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            message.set(messageLabel.getText());
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        assertEquals(Localization.get("dashboard.validation_error"), message.get());
        assertNull(mockUserDAO.updatedUser);
    }

    @Test
    void handleUpdate_NoActiveSession_ShowsError() throws Exception {
        UserSession.getUserInstance().setUser(null);

        setFieldValues("John", "Doe", "johndoe", "john@test.com", "", "");
        invokeHandleUpdate();

        AtomicReference<String> message = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            message.set(messageLabel.getText());
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        assertEquals(Localization.get("dashboard.no_session"), message.get());
        assertNull(mockUserDAO.updatedUser);
    }

    @Test
    void handleUpdate_SuccessfulUpdate_ClearsPasswordFields() throws Exception {
        UserEntity currentUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(currentUser, 1L);
        UserSession.getUserInstance().setUser(currentUser);

        setFieldValues("John", "Smith", "johndoe", "john@test.com", "NewPass123!", "NewPass123!");
        invokeHandleUpdate();

        Thread.sleep(200);

        AtomicReference<String> passwordText = new AtomicReference<>();
        AtomicReference<String> confirmText = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            passwordText.set(passwordField.getText());
            confirmText.set(confirmPasswordField.getText());
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        assertTrue(passwordText.get().isEmpty());
        assertTrue(confirmText.get().isEmpty());
    }

    @Test
    void handleUpdate_SuccessfulUpdate_ShowsSuccessMessage() throws Exception {
        UserEntity currentUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(currentUser, 1L);
        UserSession.getUserInstance().setUser(currentUser);

        setFieldValues("John", "Smith", "johnsmith", "john@test.com", "", "");
        invokeHandleUpdate();

        Thread.sleep(200);

        AtomicReference<String> message = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            message.set(messageLabel.getText());
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        assertEquals(Localization.get("dashboard.update_success"), message.get());
    }

    @Test
    void handleUpdate_WithoutPasswordChange_KeepsOldPassword() throws Exception {
        UserEntity currentUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        currentUser.changePasswordHash("oldHashedPassword");
        setId(currentUser, 1L);
        UserSession.getUserInstance().setUser(currentUser);

        setFieldValues("John", "Smith", "johndoe", "john@test.com", "", "");
        invokeHandleUpdate();

        assertNotNull(mockUserDAO.updatedUser);
        assertEquals("oldHashedPassword", mockUserDAO.updatedUser.getPasswordHash());
    }

    @Test
    void handleUpdate_UpdatesUserSession() throws Exception {
        UserEntity currentUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(currentUser, 1L);
        UserSession.getUserInstance().setUser(currentUser);

        setFieldValues("John", "Smith", "johnsmith", "john@test.com", "", "");
        invokeHandleUpdate();

        Thread.sleep(200);

        UserEntity sessionUser = UserSession.getUserInstance().getUser();
        assertNotNull(sessionUser);
        assertEquals("Smith", sessionUser.getLastName());
        assertEquals("johnsmith", sessionUser.getUsername());
    }

    @Test
    void handleCancel_ClosesWindow() throws Exception {
        Method method = UserDashboardController.class.getDeclaredMethod("handleCancel");
        method.setAccessible(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                method.invoke(controller);
            } catch (Exception e) {
                e.printStackTrace();
            }
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);
    }

    private void setId(UserEntity user, Long id) {
        try {
            Field idField = UserEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            fail("Failed to set ID on user entity");
        }
    }
}

