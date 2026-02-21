package util;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        ToggleUtil.toggleTheme(scene);

        assertTrue(ToggleUtil.isDarkMode());
        assertTrue(scene.getRoot().getStyleClass().contains("dark"));
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
}