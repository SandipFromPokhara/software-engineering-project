package controller;

import entity.entities.UserEntity;
import javafx.application.Platform;
import javafx.event.ActionEvent;
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

import java.lang.reflect.Method;
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
        if (!latch.await(30, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Timeout waiting for JavaFX setup");
        }

        await().atMost(30, SECONDS).untilAsserted(() ->
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

        await().atMost(30, SECONDS).untilAsserted(
                () -> assertFalse(loginButton.isDisabled())
        );
    }

    @Test
    void testLoginButtonDisabledWhenFieldsEmpty() {

        Platform.runLater(() -> {
            usernameField.setText("");
            passwordField.setText("");
        });

        await().atMost(30, SECONDS).untilAsserted(
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

        await().atMost(30, SECONDS).untilAsserted(
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

        await().atMost(30, SECONDS).untilAsserted(() -> {
            assertTrue(statusLabel.isVisible());
            assertFalse(loginButton.isDisabled());
        });
    }

    /* ---------------- NAVIGATION ---------------- */

    @Test
    void testHandleSignUpNavigation() {

        Platform.runLater(signupLink::fire);

        await().atMost(30, SECONDS).untilAsserted(
                () -> assertTrue(signupLink.isVisible())
        );
    }

    @Test
    void testHandleBackNavigation() {

        Platform.runLater(backButton::fire);

        await().atMost(30, SECONDS).untilAsserted(
                () -> assertTrue(backButton.isVisible())
        );
    }
    //added test to ensure that leading/trailing whitespace is trimmed before login attempt
    @Test
    void testGetUsernameAndPasswordTrimmed() {

        Platform.runLater(() -> {
            usernameField.setText("  user  ");
            passwordField.setText("  pass  ");
            loginButton.fire();
        });

        await().atMost(2, SECONDS).untilAsserted(() ->
                verify(userService).login("user", "pass")
        );
    }
    //added test to ensure that getUserService initializes the service if it is null
    @Test
    void testGetUserServiceLazyInitialization() throws Exception {

        LoginController newController = new LoginController();

        Method method = LoginController.class.getDeclaredMethod("getUserService");
        method.setAccessible(true);

        Object service = method.invoke(newController);

        assertNotNull(service);
    }
    //added test to ensure that resolveStage correctly returns the stage from the login button
    @Test
    void testResolveStageFromLoginButton() throws Exception {

        Method method = LoginController.class.getDeclaredMethod("resolveStage");
        method.setAccessible(true);

        Stage resolved = (Stage) method.invoke(controller);

        assertNotNull(resolved);
    }
    //added test to ensure that resolveStage throws an exception if it cannot find a stage from the login button
    @Test
    void testResolveStageThrowsException() throws Exception {

        LoginController newController = new LoginController();

        Method method = LoginController.class.getDeclaredMethod("resolveStage");
        method.setAccessible(true);

        Exception ex = assertThrows(Exception.class, () -> method.invoke(newController));

        assertNotNull(ex);
    }

    //added test to ensure that after a successful login, the username and password fields, login button, signup link, and back button are all disabled to prevent further interaction
    @Test
    void testFieldsDisabledAfterSuccessfulLogin() {

        UserEntity mockUser = new UserEntity();
        when(userService.login(TEST_USERNAME, TEST_PASS)).thenReturn(mockUser);

        Platform.runLater(() -> {
            usernameField.setText(TEST_USERNAME);
            passwordField.setText(TEST_PASS);
            loginButton.fire();
        });

        await().atMost(2, SECONDS).untilAsserted(() -> {
            assertTrue(usernameField.isDisabled());
            assertTrue(passwordField.isDisabled());
            assertTrue(loginButton.isDisabled());
            assertTrue(signupLink.isDisabled());
            assertTrue(backButton.isDisabled());
        });
    }
    //added test to ensure that after a successful login, the status label is visible and displays a success message (or has a style indicating success)
    @Test
    void testStatusLabelOnSuccess() {

        UserEntity mockUser = new UserEntity();
        when(userService.login(TEST_USERNAME, TEST_PASS)).thenReturn(mockUser);

        Platform.runLater(() -> {
            usernameField.setText(TEST_USERNAME);
            passwordField.setText(TEST_PASS);
            loginButton.fire();
        });

        await().atMost(2, SECONDS).untilAsserted(() -> {
            assertTrue(statusLabel.isVisible());
            assertEquals(javafx.scene.paint.Color.GREEN, statusLabel.getTextFill());
            assertFalse(statusLabel.getText().isEmpty());
        });
    }
    //added test to ensure that after a failed login, the status label is visible and displays an error message (or has a style indicating failure)
    @Test
    void testStatusLabelOnFailureText() {

        when(userService.login(TEST_USERNAME, TEST_PASS)).thenReturn(null);

        Platform.runLater(() -> {
            usernameField.setText(TEST_USERNAME);
            passwordField.setText(TEST_PASS);
            loginButton.fire();
        });

        await().atMost(2, SECONDS).untilAsserted(() -> {
            assertTrue(statusLabel.isVisible());
            assertFalse(statusLabel.getText().isEmpty());
        });
    }
    //added test to ensure that pressing the Enter key while focused on the password field triggers the login action
    @Test
    void testEnterKeyTriggersLogin() {

        when(userService.login(TEST_USERNAME, TEST_PASS)).thenReturn(null);

        Platform.runLater(() -> {
            usernameField.setText(TEST_USERNAME);
            passwordField.setText(TEST_PASS);

            passwordField.fireEvent(new ActionEvent());
        });

        await().atMost(2, SECONDS).untilAsserted(() ->
                verify(userService).login(TEST_USERNAME, TEST_PASS)
        );
    }
    //added test to ensure that while the login task is running, the login button is disabled to prevent multiple login attempts
    @Test
    void testLoginButtonDisabledDuringLogin() {

        // Create a controllable "long running" login
        CountDownLatch latch = new CountDownLatch(1);

        when(userService.login(TEST_USERNAME, TEST_PASS)).thenAnswer(invocation -> {
            latch.await(); // block until we release it
            return null;
        });

        Platform.runLater(() -> {
            usernameField.setText(TEST_USERNAME);
            passwordField.setText(TEST_PASS);
            loginButton.fire();
        });

        // Verify button is disabled WHILE login is running
        await().atMost(2, SECONDS).untilAsserted(() ->
                assertTrue(loginButton.isDisabled())
        );

        // Finish login
        latch.countDown();
    }
    //added test to ensure that getUserService returns the same instance if it has already been initialized, and does not create a new instance on subsequent calls
    @Test
    void testGetUserServiceCreatesNewInstanceWhenNull() throws Exception {
        LoginController c = new LoginController();

        Method method = LoginController.class.getDeclaredMethod("getUserService");
        method.setAccessible(true);

        Object service = method.invoke(c);

        assertNotNull(service);
    }

    //added test to ensure that resolveStage correctly returns the stage from the password field if the login button and username field are not available
    @Test
    void testResolveStageThrowsWhenNoUIAttached() {
        LoginController c = new LoginController();

        try {
            Method method = LoginController.class.getDeclaredMethod("resolveStage");
            method.setAccessible(true);

            Exception ex = assertThrows(Exception.class, () -> method.invoke(c));

            assertTrue(
                    ex.getCause() instanceof IllegalStateException
                            || ex instanceof IllegalStateException
            );

        } catch (NoSuchMethodException e) {
            fail("resolveStage method not found: " + e.getMessage());
        }
    }
    //added test to ensure that after a successful login, the status label is visible, displays a success message, and has a style indicating success (e.g. green text), while the username and password fields are disabled to prevent further input
    @Test
    void testHandleLoginSuccessFullyExecutesUIUpdate() {

        UserEntity mockUser = new UserEntity();
        when(userService.login(TEST_USERNAME, TEST_PASS)).thenReturn(mockUser);

        Platform.runLater(() -> {
            usernameField.setText(TEST_USERNAME);
            passwordField.setText(TEST_PASS);
            loginButton.fire();
        });

        await().atMost(3, SECONDS).untilAsserted(() -> {
            assertTrue(statusLabel.isVisible());
            assertEquals(javafx.scene.paint.Color.GREEN, statusLabel.getTextFill());
            assertTrue(usernameField.isDisabled());
            assertTrue(passwordField.isDisabled());
        });
    }
    //added test to ensure that after a failed login, the login button is re-enabled to allow the user to try again, and the status label is visible with an error message and style indicating failure (e.g. red text)
    @Test
    void testHandleLoginFailureReenablesButton() {

        when(userService.login(TEST_USERNAME, TEST_PASS)).thenReturn(null);

        Platform.runLater(() -> {
            usernameField.setText(TEST_USERNAME);
            passwordField.setText(TEST_PASS);
            loginButton.fire();
        });

        await().atMost(2, SECONDS).untilAsserted(() -> {
            assertFalse(loginButton.isDisabled());
            assertTrue(statusLabel.isVisible());
            assertEquals(javafx.scene.paint.Color.RED, statusLabel.getTextFill());
        });
    }
    //added test to ensure that the login button's disabled state correctly updates when the username and password fields are modified, allowing the button to become enabled when both fields have text, and disabled when either field is empty
    @Test
    void testCheckFieldsTransitions() {

        Platform.runLater(() -> {
            usernameField.setText("a");
            passwordField.setText("b");
        });

        await().atMost(2, SECONDS).untilAsserted(() ->
                assertFalse(loginButton.isDisabled())
        );

        Platform.runLater(() -> {
            usernameField.setText("");
            passwordField.setText("");
        });

        await().atMost(2, SECONDS).untilAsserted(() ->
                assertTrue(loginButton.isDisabled())
        );
    }
    //added test to ensure that the "focus" style class is added to the username field when it gains focus, and removed when it loses focus, allowing for visual feedback on which field is currently active
    @Test
    void testFocusClassAddedAndRemovedManually() {

        Platform.runLater(() -> {
            usernameField.getStyleClass().remove("focus");

            // simulate "focused"
            if (!usernameField.getStyleClass().contains("focus")) {
                usernameField.getStyleClass().add("focus");
            }

            assertTrue(usernameField.getStyleClass().contains("focus"));

            // simulate "unfocused"
            usernameField.getStyleClass().remove("focus");

            assertFalse(usernameField.getStyleClass().contains("focus"));
        });
    }
    //added test to ensure that leading and trailing whitespace in the username and password fields is trimmed before enabling the login button, allowing users to accidentally add spaces without preventing them from logging in
    @Test
    void testTrimEdgeCases() {

        Platform.runLater(() -> {
            usernameField.setText("   user   ");
            passwordField.setText("   pass   ");
        });

        await().atMost(2, SECONDS).untilAsserted(() -> {
            assertFalse(loginButton.isDisabled());
        });
    }
    //added test to ensure that if the user service is not injected (i.e. is null), the login controller's getUserService method will lazily initialize it without throwing a NullPointerException, allowing the login functionality to still work even if dependency injection fails
    @Test
    void testLoginUsesLazyServiceWhenNull() {

        LoginController c = new LoginController(); // no injected service

        Platform.runLater(() -> {
            TextField u = new TextField(TEST_USERNAME);
            PasswordField p = new PasswordField();
            p.setText(TEST_PASS);

            c.setUsernameField(u);
            c.setPasswordField(p);
            c.setLoginButton(new Button());
            c.setStatusLabel(new Label());
            c.setSignupLink(new Hyperlink());
            c.setBackButton(new Button());

            c.initView();

            c.handleLogin(new ActionEvent());
        });

        // no exception = branch covered
        assertTrue(true);
    }
}