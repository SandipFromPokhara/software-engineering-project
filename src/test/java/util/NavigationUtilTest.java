package util;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import testutil.JavaFXInitializer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class NavigationUtilTest {

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
    void testReplaceSceneWithNullStageThrowsException() throws InterruptedException {
        runOnFxThreadAndWait(() ->
                assertThrows(IllegalArgumentException.class, () ->
                        NavigationUtil.replaceScene(null, "/FXML/login_view.fxml", "login.window_title", false)
                )
        );
    }

    @Test
    void testOpenWindowWithInvalidFxmlDoesNotThrow() throws InterruptedException {
        runOnFxThreadAndWait(() ->
                assertDoesNotThrow(() ->
                        NavigationUtil.openWindow(null, "/FXML/nonexistent.fxml", "title", false, false, null)
                )
        );
    }

    @Test
    void testSetCenterWithInvalidFxmlDoesNotThrow() throws InterruptedException {
        runOnFxThreadAndWait(() -> {
            javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox();
            assertDoesNotThrow(() ->
                    NavigationUtil.setCenter(vbox, "/FXML/nonexistent.fxml")
            );
        });
    }

    @Test
    void testReplaceSceneWithInvalidFxmlDoesNotThrow() throws InterruptedException {
        runOnFxThreadAndWait(() -> {
            Stage stage = new Stage();
            assertDoesNotThrow(() ->
                    NavigationUtil.replaceScene(stage, "/FXML/nonexistent.fxml", "title", false)
            );
        });
    }
}