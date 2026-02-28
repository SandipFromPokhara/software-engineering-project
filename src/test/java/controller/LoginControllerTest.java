package controller;

import entity.UserEntity;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import services.UserService;
import security.PasswordHasher;
import security.BcryptPasswordHasher;
import session.UserSession;
import testutil.JavaFXInitializer;
import util.NavigationUtil;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LoginControllerTest {

    private LoginController controller;
    private UserService mockUserService;
    private PasswordHasher passwordHasher;

    // Mock UI components
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label statusLabel;
    private Hyperlink signupLink;
    private Button backButton;
    private Stage testStage;

    @BeforeAll
    static void initJavaFX() {
        JavaFXInitializer.init();
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new LoginController();
        passwordHasher = new BcryptPasswordHasher();

        // Stub UserService for testing
        mockUserService = new UserService(null, passwordHasher) {
            @Override
            public UserEntity login(String username, String password) {
                if ("validUser".equals(username) && "validPass".equals(password)) {
                    UserEntity user = new UserEntity();
                    user.setUsername(username);
                    return user;
                }
                return null; // invalid credentials
            }
        };
        setField(controller, "userService", mockUserService);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            usernameField = new TextField();
            passwordField = new PasswordField();
            loginButton = new Button();
            statusLabel = new Label();
            signupLink = new Hyperlink();
            backButton = new Button();

            testStage = new Stage();
            VBox root = new VBox();
            root.getChildren().addAll(usernameField, passwordField, loginButton, statusLabel, signupLink, backButton);
            testStage.setScene(new Scene(root, 400, 400));
            latch.countDown();
        });
        latch.await(2, TimeUnit.SECONDS);

        // Inject UI components
        setField(controller, "usernameField", usernameField);
        setField(controller, "passwordField", passwordField);
        setField(controller, "loginButton", loginButton);
        setField(controller, "statusLabel", statusLabel);
        setField(controller, "signupLink", signupLink);
        setField(controller, "backButton", backButton);

        // Initialize controller (private method) safely
        invokePrivateMethod("initialize", new Class<?>[0]);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void setFieldValues(String username, String password) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            usernameField.setText(username);
            passwordField.setText(password);
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);
    }

    private void invokePrivateMethod(String methodName, Class<?>[] paramTypes, Object... params) throws Exception {
        Method method = LoginController.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                method.invoke(controller, params);
            } catch (Exception e) {
                fail("Invocation failed: " + e.getMessage());
            }
            latch.countDown();
        });
        latch.await(2, TimeUnit.SECONDS);
    }

    @Test
    void testLoginButtonDisabledWhenFieldsEmpty() throws Exception {
        setFieldValues("", "");
        Platform.runLater(() -> {});
        assertTrue(loginButton.isDisabled());
    }

    @Test
    void testLoginButtonEnabledWhenFieldsFilled() throws Exception {
        setFieldValues("validUser", "validPass");
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        latch.await(200, TimeUnit.MILLISECONDS);

        assertFalse(loginButton.isDisabled());
    }

    @Test
    void testHandleSignUpNavigation() throws Exception {
        testStage.setTitle("Original Title");
        invokePrivateMethod("handleSignUp", new Class[]{});

        Thread.sleep(200);

        Platform.runLater(() -> {
            // Assert the stage title changed
            assertEquals("NoteVault - Register", testStage.getTitle());

            // Assert a new scene was set
            assertNotNull(testStage.getScene());
            assertFalse(testStage.isResizable());
        });
    }

    @Test
    void testHandleBackNavigation() throws Exception {
        testStage.setTitle("Title");
        invokePrivateMethod("handleBack", new Class[]{});


        Thread.sleep(200);

        Platform.runLater(() -> {
            assertEquals("Welcome to NoteVault", testStage.getTitle());

            assertNotNull(testStage.getScene());
            assertFalse(testStage.isResizable());
        });
    }

    @Test
    void testHandleLoginWithInvalidCredentials() throws Exception {
        // Mock login to return null instantly
        setField(controller, "userService", new UserService(null, passwordHasher) {
            @Override
            public UserEntity login(String username, String password) {
                return null; // invalid credentials
            }
        });

        setFieldValues("wrongUser", "wrongPass");
        invokePrivateMethod("checkFields", new Class<?>[0]);

        CountDownLatch latch = new CountDownLatch(1);

        // Invoke handleLogin
        invokePrivateMethod("handleLogin", new Class[]{ActionEvent.class}, (ActionEvent) null);

        Thread.sleep(200);

        Platform.runLater(() -> {
            try {
                assertTrue(statusLabel.isVisible(), "Status label should be visible");
                assertEquals("Invalid username or password", statusLabel.getText());
                assertFalse(loginButton.isDisabled(), "Login button should be enabled again");
            } finally {
                latch.countDown();
            }
        });

        if (!latch.await(2, TimeUnit.SECONDS)) {
            fail("UI did not update in time");
        }
    }
}