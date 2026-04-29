package util;

import javafx.application.Platform;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import testutil.JavaFXInitializer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TooltipUtilTest {

    @BeforeAll
    static void initJavaFX() {
        JavaFXInitializer.init();
    }

    private static void runOnFxThreadAndWait(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try { action.run(); } finally { latch.countDown(); }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timed out waiting for JavaFX thread");
    }

    @Test
    void testCreateTooltipSetsTextAndDelay() throws InterruptedException {
        runOnFxThreadAndWait(() -> {
            Tooltip t = TooltipUtil.createTooltip("Hello");
            assertEquals("Hello", t.getText());
            assertEquals(Duration.millis(100.0), t.getShowDelay());
        });
    }

    @Test
    void testCreateLocalizedTooltipSetsDelay() throws InterruptedException {
        runOnFxThreadAndWait(() -> {
            Tooltip t = TooltipUtil.createLocalizedTooltip("button.ok");
            assertNotNull(t.getText());
            assertEquals(Duration.millis(100.0), t.getShowDelay());
        });
    }

    @Test
    void testSetTooltipDelayWithNull() throws InterruptedException {
        runOnFxThreadAndWait(() -> assertDoesNotThrow(() -> TooltipUtil.setTooltipDelay(null)));
    }

    @Test
    void testSetTooltipDelaySetsCorrectDelay() throws InterruptedException {
        runOnFxThreadAndWait(() -> {
            Tooltip t = new Tooltip("test");
            TooltipUtil.setTooltipDelay(t);
            assertEquals(Duration.millis(100.0), t.getShowDelay());
        });
    }
}