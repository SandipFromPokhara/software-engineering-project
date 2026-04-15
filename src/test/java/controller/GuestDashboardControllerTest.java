package controller;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
        // Make private field accessible for test injection
        centerPaneField.setAccessible(true);
        centerPaneField.set(controller, new VBox());

        // Inject private newFiles button
        Field newFilesField = GuestDashboardController.class.getDeclaredField("newFiles");
        // Make private field accessible for test injection
        newFilesField.setAccessible(true);
        newFilesField.set(controller, new Button());
    }

    private void setField(Object target, Object value) {
        try {
            Field field = target.getClass().getDeclaredField("centerPane");
            // Make private field accessible for test injection
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new TestReflectionException("Failed to set field 'centerPane' via reflection", e);
        }
    }

    private static class TestReflectionException extends RuntimeException {
        public TestReflectionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @Test
    void handleNewFilesShouldNotCrash() {
        VBox mockVBox = mock(VBox.class);
        Scene mockScene = mock(Scene.class);
        Stage mockStage = mock(Stage.class);

        // Inject mocks
        setField(controller, mockVBox);

        when(mockVBox.getScene()).thenReturn(mockScene);
        when(mockScene.getWindow()).thenReturn(mockStage);

        // Mock NavigationUtil to avoid real FXML loading
        try (MockedStatic<NavigationUtil> navMock = mockStatic(NavigationUtil.class)) {

            navMock.when(() ->
                    NavigationUtil.openWindow(
                            any(),
                            anyString(),
                            anyString(),
                            anyBoolean(),
                            anyBoolean(),
                            any()
                    )
            ).thenAnswer(invocation -> null);

            assertDoesNotThrow(() -> controller.handleNewFiles());
        }
    }
}
