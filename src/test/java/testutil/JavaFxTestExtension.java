package testutil;

import javafx.application.Platform;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class JavaFxTestExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    private static boolean started = false;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (started) {
            return;
        }

        synchronized (JavaFxTestExtension.class) {
            if (started) {
                return;
            }

            try {
                CountDownLatch latch = new CountDownLatch(1);
                Platform.startup(latch::countDown);

                if (!latch.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("JavaFX Platform failed to start");
                }
            } catch (IllegalStateException ignored) {
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for JavaFX Platform to start", e);
            }
            started = true;
        }
    }

    @Override
    public void close() {
        Platform.exit();
    }
}
