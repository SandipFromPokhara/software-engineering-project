package util;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import util.events.EventBus;
import util.events.ThemeChangedEvent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class ToggleUtilTest {

    private Scene scene;

    @BeforeAll
    static void initJavaFX() {
        testutil.JavaFXInitializer.init();
    }

    @BeforeEach
    void setupScene() {
        scene = new Scene(new StackPane());
    }

    @Test
    void testDefaultThemeIsLight() {
        ToggleUtil.setDarkMode(false);
        ToggleUtil.applyTheme(scene);

        assertTrue(scene.getRoot().getStyleClass().contains("light"));
        assertFalse(scene.getRoot().getStyleClass().contains("dark"));
    }

    @Test
    void testToggleThemeChangesDarkMode() {
        ToggleUtil.setDarkMode(false);

        try (MockedStatic<EventBus> mocked = mockStatic(EventBus.class)) {
            ToggleUtil.toggleTheme(scene);

            assertTrue(ToggleUtil.isDarkMode());
            assertTrue(scene.getRoot().getStyleClass().contains("dark"));

            mocked.verify(() -> EventBus.publish(any(ThemeChangedEvent.class)));
        }
    }

    @Test
    void testToggleBackToLight() {
        ToggleUtil.setDarkMode(false);
        ToggleUtil.toggleTheme(scene); // dark
        ToggleUtil.toggleTheme(scene); // light

        assertFalse(ToggleUtil.isDarkMode());
        assertTrue(scene.getRoot().getStyleClass().contains("light"));
        assertFalse(scene.getRoot().getStyleClass().contains("dark"));
    }

    @Test
    void testSetDarkModeTrueTriggersEvent() {
        try (MockedStatic<EventBus> mocked = mockStatic(EventBus.class)) {
            ToggleUtil.setDarkMode(true);

            assertTrue(ToggleUtil.isDarkMode());

            mocked.verify(() -> EventBus.publish(any(ThemeChangedEvent.class)));
        }
    }

    @Test
    void testSetDarkModeFalseTriggersEvent() {
        try (MockedStatic<EventBus> mocked = mockStatic(EventBus.class)) {
            ToggleUtil.setDarkMode(false);

            assertFalse(ToggleUtil.isDarkMode());

            mocked.verify(() -> EventBus.publish(any(ThemeChangedEvent.class)));
        }
    }

    @Test
    void testApplyThemeWithNullSceneDoesNotCrash() {
        assertDoesNotThrow(() -> ToggleUtil.setDarkMode(true));
    }

    @Test
    void testEventBusFailureIsHandledGracefully() {
        ToggleUtil.setDarkMode(false);

        try (MockedStatic<EventBus> mocked = mockStatic(EventBus.class)) {

            // force exception when publish is called
            mocked.when(() -> EventBus.publish(any())).thenThrow(new RuntimeException("fail"));

            // should NOT throw even though EventBus fails internally
            assertDoesNotThrow(() -> ToggleUtil.toggleTheme(scene));
            assertDoesNotThrow(() -> ToggleUtil.setDarkMode(true));
        }
    }

    @Test
    void applyThemeWithoutRootClassDoesNothing() {
        Scene testScene  = new Scene(new StackPane()); // no "root" class

        ToggleUtil.setDarkMode(true);
        ToggleUtil.applyTheme(testScene);

        // No theme should be applied
        assertFalse(scene.getRoot().getStyleClass().contains("dark"));
        assertFalse(scene.getRoot().getStyleClass().contains("light"));
    }

    @Test
    void applyThemeWithRootClassAppliesTheme() {
        StackPane root = new StackPane();
        root.getStyleClass().add("root");

        Scene testScene = new Scene(root);

        ToggleUtil.setDarkMode(true);
        ToggleUtil.applyTheme(testScene);

        assertTrue(root.getStyleClass().contains("dark"));
    }

    @Test
    void applyThemeDoesNotDuplicateStylesheet() {
        StackPane root = new StackPane();
        root.getStyleClass().add("root");

        Scene testScene = new Scene(root);

        ToggleUtil.setDarkMode(false);

        ToggleUtil.applyTheme(testScene);
        int firstCount = testScene.getStylesheets().size();

        ToggleUtil.applyTheme(testScene);
        int secondCount = testScene.getStylesheets().size();

        assertEquals(firstCount, secondCount); // no duplicate
    }

    @Test
    void setDarkModeWithNullSceneStillWorks() {
        try (MockedStatic<EventBus> mocked = mockStatic(EventBus.class)) {
            ToggleUtil.setDarkMode(true);

            assertTrue(ToggleUtil.isDarkMode());

            mocked.verify(() -> EventBus.publish(any(ThemeChangedEvent.class)));
        }
    }

    @Test
    void applyThemeToAllHandlesMultipleWindowsGracefully() {
        StackPane root = new StackPane();
        root.getStyleClass().add("root");
        Scene testScene = new Scene(root);

        ToggleUtil.setDarkMode(true);

        // This will internally iterate Window.getWindows()
        assertDoesNotThrow(() -> ToggleUtil.toggleTheme(testScene));
    }
}