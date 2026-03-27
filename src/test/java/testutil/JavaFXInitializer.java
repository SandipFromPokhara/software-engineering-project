package testutil;

import javafx.application.Platform;

public final class JavaFXInitializer {

    private static boolean initialized = false;

    private JavaFXInitializer() {}

    public static synchronized void init() {
        if (!initialized) {
            try {
                Platform.startup(() -> {});
            } catch (IllegalStateException ignored) {
                // JavaFX already initialized — ignore
            }
            initialized = true;
        }
    }
}
