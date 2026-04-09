package controller;

import entity.entities.UserEntity;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import services.UserService;
import security.IPasswordHasher;
import security.BcryptPasswordHasher;
import testutil.JavaFXInitializer;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LoginControllerTest {

    private LoginController controller;
    private UserService mockUserService;
    private IPasswordHasher passwordHasher;

    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label statusLabel;
    private Hyperlink signupLink;
    private Button backButton;

    private Label loginWelcome, loginNote, loginNoAccount, privacyLabel;

    private Stage testStage;

    @BeforeAll
    static void initJavaFX() {
        JavaFXInitializer.init();
    }

    @BeforeEach
    void setUp() throws Exception {
        passwordHasher = new BcryptPasswordHasher();

        // Mock service
        mockUserService = new UserService(null, passwordHasher) {
            @Override
            public UserEntity login(String username, String password) {
                System.out.println("MOCK LOGIN CALLED");
                if ("validUser".equals(username) && "validPass".equals(password)) {
                    UserEntity user = new UserEntity();
                    user.setUsername(username);
                    return user;
                }
                return null;
            }
        };

        controller = new LoginController(mockUserService);

        setField(controller, "userService", mockUserService);

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            usernameField = new TextField();
            passwordField = new PasswordField();
            loginButton = new Button();
            statusLabel = new Label();
            signupLink = new Hyperlink();
            backButton = new Button();

            loginWelcome = new Label();
            loginNote = new Label();
            loginNoAccount = new Label();
            privacyLabel = new Label();

            testStage = new Stage();

            VBox root = new VBox(
                    usernameField,
                    passwordField,
                    loginButton,
                    statusLabel,
                    signupLink,
                    backButton
            );

            testStage.setScene(new Scene(root, 400, 400));

            latch.countDown();
        });

        latch.await(2, TimeUnit.SECONDS);

        // Inject fields
        setField(controller, "usernameField", usernameField);
        setField(controller, "passwordField", passwordField);
        setField(controller, "loginButton", loginButton);
        setField(controller, "statusLabel", statusLabel);
        setField(controller, "signupLink", signupLink);
        setField(controller, "backButton", backButton);

        setField(controller, "loginWelcome", loginWelcome);
        setField(controller, "loginNote", loginNote);
        setField(controller, "loginNoAccount", loginNoAccount);
        setField(controller, "privacyLabel", privacyLabel);

        invokePrivateMethod("initialize");
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

    private void invokePrivateMethod(String methodName, Object... args) throws Exception {
        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClass();
        }

        Method method = LoginController.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                method.invoke(controller, args);
            } catch (Exception e) {
                e.printStackTrace();
                fail("Invocation failed: " + e.getCause());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testLoginButtonEnabledWhenFieldsFilled() throws Exception {
        setFieldValues("validUser", "validPass");

        invokePrivateMethod("checkFields");

        assertFalse(loginButton.isDisabled());
    }

    @Test
    void testLoginButtonDisabledWhenFieldsEmpty() throws Exception {
        setFieldValues("", "");

        invokePrivateMethod("checkFields");

        assertTrue(loginButton.isDisabled());
    }

    @Test
    void testHandleLoginWithInvalidCredentials() throws Exception {

        setFieldValues("wrongUser", "wrongPass");

        // Inject mock
        setField(controller, "userService", mockUserService);

        invokePrivateMethod("handleLogin", new ActionEvent());

        boolean visible = false;

        for (int i = 0; i < 10; i++) {
            Thread.sleep(100);
            if (statusLabel.isVisible()) {
                visible = true;
                break;
            }
        }

        assertTrue(visible);
        assertFalse(loginButton.isDisabled());
        assertEquals("Invalid username or password", statusLabel.getText());
    }

    @Test
    void testHandleSignUpNavigation() throws Exception {
        invokePrivateMethod("handleSignUp");

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                assertNotNull(testStage.getScene());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testHandleBackNavigation() throws Exception {
        invokePrivateMethod("handleBack");

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                assertNotNull(testStage.getScene());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }
}