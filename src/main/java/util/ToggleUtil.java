package util;

import javafx.scene.Scene;
import javafx.stage.Window;
import util.events.EventBus;
import util.events.ThemeChangedEvent;

import java.util.Objects;

public class ToggleUtil {

    private static boolean darkMode = false;

    private ToggleUtil() {/* Private constructor to prevent instantiation of utility class */}

    public static void applyTheme(Scene scene) {

        if (!scene.getRoot().getStyleClass().contains("root")) {
            return;
        }

        // Remove both theme
        scene.getRoot().getStyleClass().removeAll("light", "dark");
        // Add current theme
        scene.getRoot().getStyleClass().add(darkMode ? "dark" : "light");

        if (scene.getStylesheets().stream().noneMatch(s -> s.endsWith("theme.css"))) {
            scene.getStylesheets().add(
                    Objects.requireNonNull(ToggleUtil.class.getResource("/css/theme.css")).toExternalForm()
            );
        }
    }

    // Apply theme to the given scene and all other open windows' scenes
    private static void applyThemeToAll(Scene scene) {
        // Apply to the provided scene first
        if (scene != null) applyTheme(scene);

        try {
            for (Window w : Window.getWindows()) {
                Scene s = w.getScene();
                if (s != null && s != scene) applyTheme(s);
            }
        } catch (Exception e) {
            // Best-effort: ignore any errors while iterating windows/scenes
        }
    }

    public static void toggleTheme(Scene scene) {
        darkMode = !darkMode;
        applyThemeToAll(scene);

        // Notify subscribers about the theme change so UI components can update icons etc.
        try {
            EventBus.publish(new ThemeChangedEvent(darkMode));
        } catch (Exception e) {
            // Best-effort: ignore publish failures
        }
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean mode) {
        darkMode = mode;
        applyThemeToAll(null);

        // Notify listeners that theme was changed programmatically
        try {
            EventBus.publish(new ThemeChangedEvent(darkMode));
        } catch (Exception e) {
            // Best-effort: ignore publish failures
        }
    }
}
