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
            throw new UnsupportedOperationException("Delete is not implemented in mock");
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
        latch.await(3, TimeUnit.SECONDS);
    }

    private void runOnFxAndWait(Runnable action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        latch.await(2, TimeUnit.SECONDS);
        if (error.get() != null) {
            throw new RuntimeException("FX action failed", error.get());
        }
    }

    private void invokeHandleUpdate() throws Exception {
        Method method = UserDashboardController.class.getDeclaredMethod("handleUpdate");
        method.setAccessible(true);
        runOnFxAndWait(() -> {
            try {
                method.invoke(controller);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String getLabelText(Label label) throws Exception {
        AtomicReference<String> text = new AtomicReference<>();
        runOnFxAndWait(() -> text.set(label.getText()));
        return text.get();
    }


    @Test
    void initialize_WithActiveUser_PopulatesFields() throws Exception {
        UserEntity testUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(testUser, 1L);
        UserSession.getUserInstance().setUser(testUser);

        runOnFxAndWait(() -> controller.initialize());

        AtomicReference<String> firstName = new AtomicReference<>();
        AtomicReference<String> lastName = new AtomicReference<>();
        AtomicReference<String> username = new AtomicReference<>();
        AtomicReference<String> email = new AtomicReference<>();

        runOnFxAndWait(() -> {
            firstName.set(firstNameField.getText());
            lastName.set(lastNameField.getText());
            username.set(usernameField.getText());
            email.set(emailField.getText());
        });

        assertEquals("John", firstName.get());
        assertEquals("Doe", lastName.get());
        assertEquals("johndoe", username.get());
        assertEquals("john@test.com", email.get());
    }

    @Test
    void initialize_WithNoActiveUser_ShowsErrorMessage() throws Exception {
        UserSession.getUserInstance().setUser(null);

        AtomicReference<String> message = new AtomicReference<>();
        runOnFxAndWait(() -> {
            controller.initialize();
            message.set(messageLabel.getText());
        });

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

        // initialize the controller first
        runOnFxAndWait(() -> controller.initialize());

        setFieldValues("John", "", "johndoe", "john@test.com", "", "");
        invokeHandleUpdate();

        assertEquals(Localization.get("dashboard.validation_error"), getLabelText(messageLabel));
        assertNull(mockUserDAO.updatedUser);
    }

    @Test
    void handleUpdate_EmptyUsername_ShowsError() throws Exception {
        UserEntity currentUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(currentUser, 1L);
        UserSession.getUserInstance().setUser(currentUser);

        setFieldValues("John", "Doe", "", "john@test.com", "", "");
        invokeHandleUpdate();

        assertEquals(Localization.get("dashboard.validation_error"), getLabelText(messageLabel));
        assertNull(mockUserDAO.updatedUser);
    }

    @Test
    void handleUpdate_PasswordMismatch_ShowsError() throws Exception {
        UserEntity currentUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(currentUser, 1L);
        UserSession.getUserInstance().setUser(currentUser);

        setFieldValues("John", "Doe", "johndoe", "john@test.com", "Pass123!", "Different123!");
        invokeHandleUpdate();

        assertEquals(Localization.get("dashboard.validation_error"), getLabelText(messageLabel));
        assertNull(mockUserDAO.updatedUser);
    }

    @Test
    void handleUpdate_WeakPassword_ShowsError() throws Exception {
        UserEntity currentUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(currentUser, 1L);
        UserSession.getUserInstance().setUser(currentUser);

        setFieldValues("John", "Doe", "johndoe", "john@test.com", "weak", "weak");
        invokeHandleUpdate();

        assertEquals(Localization.get("dashboard.validation_error"), getLabelText(messageLabel));
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

        assertEquals(Localization.get("dashboard.username_exists"), getLabelText(messageLabel));
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

        assertEquals(Localization.get("dashboard.validation_error"), getLabelText(messageLabel));
        assertNull(mockUserDAO.updatedUser);
    }

    @Test
    void handleUpdate_InvalidUsername_ShowsError() throws Exception {
        UserEntity currentUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(currentUser, 1L);
        UserSession.getUserInstance().setUser(currentUser);

        setFieldValues("John", "Doe", "ab", "john@test.com", "", "");
        invokeHandleUpdate();

        assertEquals(Localization.get("dashboard.validation_error"), getLabelText(messageLabel));
        assertNull(mockUserDAO.updatedUser);
    }

    @Test
    void handleUpdate_NoActiveSession_ShowsError() throws Exception {
        UserSession.getUserInstance().setUser(null);

        setFieldValues("John", "Doe", "johndoe", "john@test.com", "", "");
        invokeHandleUpdate();

        assertEquals(Localization.get("dashboard.no_session"), getLabelText(messageLabel));
        assertNull(mockUserDAO.updatedUser);
    }

    @Test
    void handleUpdate_SuccessfulUpdate_ClearsPasswordFields() throws Exception {
        UserEntity currentUser = new UserEntity("John", "Doe", "johndoe", "john@test.com");
        setId(currentUser, 1L);
        UserSession.getUserInstance().setUser(currentUser);

        setFieldValues("John", "Smith", "johndoe", "john@test.com", "NewPass123!", "NewPass123!");
        invokeHandleUpdate();

        AtomicReference<String> passwordText = new AtomicReference<>();
        AtomicReference<String> confirmText = new AtomicReference<>();
        runOnFxAndWait(() -> {
            passwordText.set(passwordField.getText());
            confirmText.set(confirmPasswordField.getText());
        });

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

        assertEquals(Localization.get("dashboard.update_success"), getLabelText(messageLabel));
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

        UserEntity sessionUser = UserSession.getUserInstance().getUser();
        assertNotNull(sessionUser);
        assertEquals("Smith", sessionUser.getLastName());
        assertEquals("johnsmith", sessionUser.getUsername());
    }

    @Test
    void handleCancel_ClosesWindow() throws Exception {
        Method method = UserDashboardController.class.getDeclaredMethod("handleCancel");
        method.setAccessible(true);

        runOnFxAndWait(() -> testStage.show());

        runOnFxAndWait(() -> {
            try {
                method.invoke(controller);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        AtomicReference<Boolean> isShowing = new AtomicReference<>(true);
        runOnFxAndWait(() -> isShowing.set(testStage.isShowing()));

        assertFalse(isShowing.get(), "Window should be closed after cancel");
    }

    private void setId(Object target, Object value) {
        Class<?> clazz = target.getClass();

        while (clazz != null) {
            try {
                Field idField = clazz.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                throw new RuntimeException("Failed to set ID on user entity", e);
            }
        }
        throw new RuntimeException("ID field not found in class hierarchy");
    }
}
