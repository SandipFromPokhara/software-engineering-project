package util;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import testutil.JavaFXInitializer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Test
    void testOpenWindowModalWithOwnerDoesNotCrash() throws Exception {
        runOnFxThreadAndWait(() -> {
            Stage owner = new Stage();

            assertDoesNotThrow(() ->
                    NavigationUtil.openWindow(owner, "/FXML/test_view.fxml", "title", false, true, null)
            );
        });
    }

    @Test
    void testOpenWindowControllerConsumerRuns() throws Exception {
        runOnFxThreadAndWait(() -> {
            AtomicBoolean called = new AtomicBoolean(false);

            NavigationUtil.openWindow(null, "/FXML/test_view.fxml", "title", false, false, c -> called.set(true));

            assertTrue(called.get());
        });
    }

    @Test
    void testSceneAddsRootStyleClass() throws Exception {
        runOnFxThreadAndWait(() -> {
            Stage stage = new Stage();

            NavigationUtil.replaceScene(stage, "/FXML/test_view.fxml", "title", false);

            assertTrue(stage.getScene().getRoot().getStyleClass().contains("root"));
        });
    }

    @Test
    void testSetCenterWrapsNode() throws Exception {
        runOnFxThreadAndWait(() -> {
            VBox box = new VBox();

            NavigationUtil.setCenter(box, "/FXML/test_view.fxml");

            assertEquals(1, box.getChildren().size());

            assertInstanceOf(StackPane.class, box.getChildren().get(0));
        });
    }

    @Test
    void testOpenWindow_withInvalidFxml_doesNotThrow() throws Exception {
        runOnFxThreadAndWait(() ->
            assertDoesNotThrow(() ->
                    NavigationUtil.openWindow(null, "/FXML/definitely_missing.fxml", "title", false, false, null)
            ));
    }

    @Test
    void testResolveFxmlDevPathFallback() {
        assertDoesNotThrow(() ->
            NavigationUtil.openWindow(null, "/FXML/test_view.fxml", "title", false, false, null));
    }
}