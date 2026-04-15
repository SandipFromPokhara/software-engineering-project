package controller;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import testutil.JavaFXInitializer;
import util.NavigationUtil;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GuestDashboardControllerTest {

    private GuestDashboardController controller;

    /**
     * Initializes JavaFX runtime once before all tests.
     * Without this, JavaFX components (Button, VBox, etc.) will crash.
     */
    @BeforeAll
    static void initJfxRuntime() {
        JavaFXInitializer.init();
    }

    /**
     * Runs before each test.
     * Creates a fresh controller and injects required private fields.
     */
    @BeforeEach
    void setUp() throws Exception {
        controller = new GuestDashboardController();

        // Inject required UI components (since no FXML loader is used)
        injectField("centerPane", new VBox());
        injectField("newFiles", new Button());
    }

    /**
     * Helper method to inject values into private fields using reflection.
     * This avoids changing access modifiers in the controller.
     */
    private void injectField(String name, Object value) throws Exception {
        Field field = GuestDashboardController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(controller, value);
    }

    /**
     * Runs code on the JavaFX Application Thread.
     * Required when creating Stage, Scene, or interacting with UI elements.
     */
    private void runOnFxThread(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown(); // allow test to continue
            }
        });

        latch.await(); // wait until UI thread finishes
    }

    /**
     * Tests handleNewFiles()
     * Verifies that NavigationUtil.setCenter() is called correctly.
     */
    @Test
    void handleNewFilesShouldNotCrash() {
        VBox mockVBox = mock(VBox.class);

        assertDoesNotThrow(() -> {
            try {
                injectField("centerPane", mockVBox);
            } catch (Exception e) {
                fail(e);
            }

            // Mock static NavigationUtil method
            try (MockedStatic<NavigationUtil> navMock = mockStatic(NavigationUtil.class)) {

                navMock.when(() ->
                        NavigationUtil.setCenter(any(), anyString())
                ).thenAnswer(invocation -> null);

                // Call method directly (it is public)
                controller.handleNewFiles();

                // Verify correct FXML path is used
                navMock.verify(() ->
                        NavigationUtil.setCenter(
                                any(),
                                eq("/FXML/create_files_guest.fxml")
                        )
                );
            }
        });
    }

    /**
     * Tests initialize()
     * Ensures binding does not throw errors.
     */
    @Test
    void initializeShouldBindTexts() throws Exception {
        Button loginBtn = new Button();
        Button registerBtn = new Button();

        injectField("login", loginBtn);
        injectField("register", registerBtn);

        // Just verify it doesn't crash
        assertDoesNotThrow(() -> controller.initialize());
    }

    /**
     * Tests private method handleHome()
     * Uses reflection + JavaFX thread.
     */
    @Test
    void handleHomeShouldCallNavigationUtil() throws Exception {
        runOnFxThread(() -> {
            try {
                Button homeBtn = new Button();
                Scene scene = new Scene(new VBox());
                Stage stage = new Stage();

                // Attach scene to stage and button to scene
                stage.setScene(scene);
                scene.setRoot(homeBtn);

                injectField("home", homeBtn);

                try (MockedStatic<NavigationUtil> navMock = mockStatic(NavigationUtil.class)) {

                    navMock.when(() ->
                            NavigationUtil.replaceScene(any(), anyString(), anyString(), anyBoolean())
                    ).thenAnswer(invocation -> null);

                    // Call private method using reflection
                    var method = GuestDashboardController.class.getDeclaredMethod("handleHome");
                    method.setAccessible(true);
                    method.invoke(controller);

                    // Verify navigation call
                    navMock.verify(() ->
                            NavigationUtil.replaceScene(
                                    any(),
                                    eq("/FXML/entry.fxml"),
                                    eq("entry.window_title"),
                                    eq(false)
                            )
                    );
                }

            } catch (Exception e) {
                fail(e);
            }
        });
    }

    /**
     * Tests private method handleLogin()
     */
    @Test
    void handleLoginShouldNavigateToLoginView() throws Exception {
        runOnFxThread(() -> {
            try {
                Button loginBtn = new Button();
                Scene scene = new Scene(new VBox());
                Stage stage = new Stage();

                stage.setScene(scene);
                scene.setRoot(loginBtn);

                injectField("login", loginBtn);

                try (MockedStatic<NavigationUtil> navMock = mockStatic(NavigationUtil.class)) {

                    navMock.when(() ->
                            NavigationUtil.replaceScene(any(), anyString(), anyString(), anyBoolean())
                    ).thenAnswer(invocation -> null);

                    // Call private method
                    var method = GuestDashboardController.class.getDeclaredMethod("handleLogin");
                    method.setAccessible(true);
                    method.invoke(controller);

                    navMock.verify(() ->
                            NavigationUtil.replaceScene(
                                    any(),
                                    eq("/FXML/login_view.fxml"),
                                    eq("login.window_title"),
                                    eq(false)
                            )
                    );
                }

            } catch (Exception e) {
                fail(e);
            }
        });
    }

    /**
     * Tests private method handleSignUp()
     */
    @Test
    void handleSignUpShouldNavigateToSignup() throws Exception {
        runOnFxThread(() -> {
            try {
                Button registerBtn = new Button();
                Scene scene = new Scene(new VBox());
                Stage stage = new Stage();

                stage.setScene(scene);
                scene.setRoot(registerBtn);

                injectField("register", registerBtn);

                try (MockedStatic<NavigationUtil> navMock = mockStatic(NavigationUtil.class)) {

                    navMock.when(() ->
                            NavigationUtil.replaceScene(any(), anyString(), anyString(), anyBoolean())
                    ).thenAnswer(invocation -> null);

                    // Call private method
                    var method = GuestDashboardController.class.getDeclaredMethod("handleSignUp");
                    method.setAccessible(true);
                    method.invoke(controller);

                    navMock.verify(() ->
                            NavigationUtil.replaceScene(
                                    any(),
                                    eq("/FXML/signup.fxml"),
                                    eq("register.window_title"),
                                    eq(false)
                            )
                    );
                }

            } catch (Exception e) {
                fail(e);
            }
        });
    }
}