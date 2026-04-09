package util;

import javafx.scene.Scene;

import java.util.Objects;

public class ToggleUtil {
    private static boolean darkMode = false;

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

    public static void toggleTheme(Scene scene) {
        darkMode = !darkMode;
        applyTheme(scene);
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean mode) {
        darkMode = mode;
    }
}
