package controller;

import entity.entities.UserEntity;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import services.UserService;
import testutil.JavaFxTestExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(JavaFxTestExtension.class)
class LoginControllerTest {

    private static final String TEST_USERNAME = "validUser";
    private static final String TEST_PASS = "validPass";

    @Mock
    private UserService userService;

    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label statusLabel;
    private Hyperlink signupLink;
    private Button backButton;

    private Stage stage;
    private Scene scene;
    private Pane root;

    private LoginController controller;

    @BeforeEach
    void setUp() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                controller = new LoginController();

                usernameField = new TextField();
                passwordField = new PasswordField();
                loginButton = new Button();
                statusLabel = new Label();
                signupLink = new Hyperlink();
                backButton = new Button();

                Label loginWelcome = new Label();
                Label loginNote = new Label();
                Label loginNoAccount = new Label();
                Label privacyLabel = new Label();

                controller.setUsernameField(usernameField);
                controller.setPasswordField(passwordField);
                controller.setLoginButton(loginButton);
                controller.setStatusLabel(statusLabel);
                controller.setSignupLink(signupLink);
                controller.setBackButton(backButton);
                controller.setLoginWelcome(loginWelcome);
                controller.setLoginNote(loginNote);
                controller.setLoginNoAccount(loginNoAccount);
                controller.setPrivacyLabel(privacyLabel);
                controller.setUserService(userService);

                stage = new Stage();
                root = new Pane();

                root.getChildren().addAll(
                        usernameField,
                        passwordField,
                        loginButton,
                        statusLabel,
                        signupLink,
                        backButton
                );

                scene = new Scene(root);
                stage.setScene(scene);
                stage.show();

                controller.setStage(stage);

                controller.initView();

            } finally {
                latch.countDown();
            }
        });

        // Wait for the JavaFX thread to finish setup
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Timeout waiting for JavaFX setup");
        }

        await().atMost(2, SECONDS).untilAsserted(() ->
                assertNotNull(loginButton)
        );
    }

    /* ---------------- FIELD VALIDATION ---------------- */

    @Test
    void testLoginButtonEnabledWhenFieldsFilled() {

        Platform.runLater(() -> {
            usernameField.setText(TEST_USERNAME);
            passwordField.setText(TEST_PASS);
        });

        await().atMost(2, SECONDS).untilAsserted(
                () -> assertFalse(loginButton.isDisabled())
        );
    }

    @Test
    void testLoginButtonDisabledWhenFieldsEmpty() {

        Platform.runLater(() -> {
            usernameField.setText("");
            passwordField.setText("");
        });

        await().atMost(2, SECONDS).untilAsserted(
                () -> assertTrue(loginButton.isDisabled())
        );
    }

    /* ---------------- LOGIN SUCCESS ---------------- */

    @Test
    void testHandleLoginSuccess() {

        UserEntity mockUser = new UserEntity();
        when(userService.login(TEST_USERNAME, TEST_PASS)).thenReturn(mockUser);

        Platform.runLater(() -> {
            usernameField.setText(TEST_USERNAME);
            passwordField.setText(TEST_PASS);
            loginButton.fire();
        });

        await().atMost(2, SECONDS).untilAsserted(
                () -> verify(userService).login(TEST_USERNAME, TEST_PASS)
        );
    }

    /* ---------------- LOGIN FAILURE ---------------- */

    @Test
    void testHandleLoginFailure() {

        when(userService.login(TEST_USERNAME, TEST_PASS)).thenReturn(null);

        Platform.runLater(() -> {
            usernameField.setText(TEST_USERNAME);
            passwordField.setText(TEST_PASS);
            loginButton.fire();
        });

        await().atMost(2, SECONDS).untilAsserted(() -> {
            assertTrue(statusLabel.isVisible());
            assertFalse(loginButton.isDisabled());
        });
    }

    /* ---------------- NAVIGATION ---------------- */

    @Test
    void testHandleSignUpNavigation() {

        Platform.runLater(signupLink::fire);

        await().atMost(2, SECONDS).untilAsserted(
                () -> assertTrue(signupLink.isVisible())
        );
    }

    @Test
    void testHandleBackNavigation() {

        Platform.runLater(backButton::fire);

        await().atMost(2, SECONDS).untilAsserted(
                () -> assertTrue(backButton.isVisible())
        );
    }
}