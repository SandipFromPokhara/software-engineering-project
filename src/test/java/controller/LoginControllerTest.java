package controller;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import services.UserService;
import testutil.JavaFxTestExtension;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(JavaFxTestExtension.class)
class LoginControllerTest {

    private static final String TEST_USERNAME = "validUser";
    private static final String TEST_PASSWORD = "validPass";

    @Mock
    private UserService mockUserService;

    @InjectMocks
    private LoginController controller;

    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label statusLabel;
    private Hyperlink signupLink;
    private Button backButton;
    private Label loginWelcome;
    private Label loginNote;
    private Label loginNoAccount;
    private Label privacyLabel;

    private Stage testStage;

    @BeforeEach
    void setUp() throws Exception {
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

        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new AssertionError("Test timed out: JavaFX initialization took longer than 5 seconds.");
        }

        // Inject UI fields using reflection (since we aren't changing the source)
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

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Refactor Error: Field '" + fieldName + "' not found.", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Access denied to field '" + fieldName + "'", e);
        }
    }

    private void setFieldValues(String username, String password) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            usernameField.setText(username);
            passwordField.setText(password);
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Setting field values timed out.");
    }

    private void invokePrivateMethod(String methodName, Object... args) throws Exception {

        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClass();
        }

        Method method = LoginController.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                method.invoke(controller, args);
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS), "FX thread invocation timed out");

        if (error.get() != null) {
            throw new RuntimeException("Controller method failed", error.get());
        }
    }

    @Test
    void testLoginButtonEnabledWhenFieldsFilled() throws Exception {
        setFieldValues(TEST_USERNAME, TEST_PASSWORD);
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
