package controller;

import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testutil.JavaFXInitializer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class GuestDashboardControllerTest {

    private GuestDashboardController controller;

    @BeforeAll
    static void initJfxRuntime() {
        JavaFXInitializer.init();
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new GuestDashboardController();

        // Inject private centerPane
        Field centerPaneField = GuestDashboardController.class.getDeclaredField("centerPane");
        centerPaneField.setAccessible(true);
        centerPaneField.set(controller, new VBox());

        // Inject private newFiles button
        Field newFilesField = GuestDashboardController.class.getDeclaredField("newFiles");
        newFilesField.setAccessible(true);
        newFilesField.set(controller, new Button());
    }

    // Helper to call private methods
    private void invokePrivateMethod(String methodName) throws Exception {
        Method method = GuestDashboardController.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(controller);
    }

    @Test
    void handleNewFiles_shouldNotCrash() throws Exception {
        assertDoesNotThrow(() -> invokePrivateMethod("handleNewFiles"));

        // Ensure centerPane is still valid
        Field centerPaneField = GuestDashboardController.class.getDeclaredField("centerPane");
        centerPaneField.setAccessible(true);
        VBox centerPane = (VBox) centerPaneField.get(controller);
        assertNotNull(centerPane);
    }
}