package util;

import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class UndoRedoManagerTest {

    private static final long FX_TIMEOUT_SECONDS = 3;

    private UndoRedoManager manager;
    private TextField textField;
    private TextArea textArea;
    private MenuItem undoMenuItem;
    private MenuItem redoMenuItem;

    @BeforeAll
    static void initJavaFX() {
        /*
         * Ensure JavaFX platform is started before tests run.
         *
         * Why this is necessary / why the tests were failing previously:
         * - The previous implementation attempted to start JavaFX using a
         *   background thread and a CountDownLatch. In some test execution
         *   orders that produced a race, the latch.wait() would time out
         *   because the FX thread did not process the startup callback in
         *   time. That produced the intermittent failure seen as
         *   "Timeout waiting for JavaFX thread" in the test output.
         *
         * - Also, Platform.startup may only be invoked once per JVM; calling
         *   it from a background thread combined with other tests that also
         *   manipulate the JavaFX lifecycle could leave the platform in an
         *   unexpected state and make Platform.runLater scheduling unreliable.
         *
         * To avoid these race conditions we call Platform.startup
         * synchronously here and catch IllegalStateException which indicates
         * the platform is already initialized.
         */
        try {
            Platform.startup(() -> {
                // no-op; platform initialized
            });
        } catch (IllegalStateException e) {
            // JavaFX already initialized in this JVM - nothing to do
        }
    }

    @BeforeEach
    void setUp() {
        manager = new UndoRedoManager();

        runOnFxThreadAndWait(() -> {
            textField = new TextField();
            textArea = new TextArea();
            undoMenuItem = new MenuItem();
            redoMenuItem = new MenuItem();
        });
    }

    private static void awaitOrFail(CountDownLatch latch, long timeoutSeconds, String message) {
        try {
            boolean completed = latch.await(timeoutSeconds, TimeUnit.SECONDS);
            assertTrue(completed, message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted while waiting: " + message);
        }
    }

    private static void runOnFxThreadAndWait(Runnable action) {
        CountDownLatch latch = new CountDownLatch(1);
        /*
         * Run the given action on the JavaFX Application Thread and wait for
         * completion. This helper is defensive because tests running in the
         * same JVM can interfere with JavaFX lifecycle (platform not started,
         * or runLater queue not being processed due to ordering). The original
         * implementation assumed Platform.runLater would always execute
         * quickly; when it didn't the tests hit a timeout. To make tests
         * robust we:
         *  - schedule with Platform.runLater and wait for the latch
         *  - if we time out, attempt to synchronously (re)start the FX
         *    platform (Platform.startup) and retry once
         *  - if Platform.runLater throws IllegalStateException we start the
         *    platform synchronously and run the action directly
         */
        try {
            Platform.runLater(() -> {
                try {
                    action.run();
                } finally {
                    latch.countDown();
                }
            });

            // Wait for the action to be run on the FX thread. If the platform
            // is not running or the runLater queue isn't being processed we
            // will time out — in that case attempt to start the platform and
            // retry once.
            boolean completed = latch.await(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!completed) {
                // Try to (re)start JavaFX platform synchronously and retry
                try {
                    Platform.startup(() -> {
                        // no-op
                    });
                } catch (IllegalStateException ignored) {
                    // already started
                }

                CountDownLatch retryLatch = new CountDownLatch(1);
                Platform.runLater(() -> {
                    try {
                        action.run();
                    } finally {
                        retryLatch.countDown();
                    }
                });
                awaitOrFail(retryLatch, FX_TIMEOUT_SECONDS, "Timeout waiting for JavaFX thread");
            }
        } catch (IllegalStateException e) {
            // Platform not initialized; start it synchronously and run the action
            try {
                Platform.startup(() -> {
                    try {
                        action.run();
                    } finally {
                        latch.countDown();
                    }
                });
            } catch (IllegalStateException ignored) {
                // If startup throws, try scheduling the action one more time
                Platform.runLater(() -> {
                    try {
                        action.run();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            awaitOrFail(latch, FX_TIMEOUT_SECONDS, "Timeout waiting for JavaFX thread");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted while waiting for JavaFX thread");
        }
    }

    private void waitUntilCanUndo(int expectedUndoSize) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline) {
            if (manager.canUndo() && manager.getUndoStackSize() >= expectedUndoSize) {
                return;
            }
            runOnFxThreadAndWait(() -> {
                // flush FX events
            });
        }
        fail("Undo stack did not reach expected size: " + expectedUndoSize);
    }

    private void setupTitleField() {
        runOnFxThreadAndWait(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.registerField("title", textField);
        });
    }

    @Test
    void testInitializeWithBothMenuItems() {
        runOnFxThreadAndWait(() -> manager.initialize(undoMenuItem, redoMenuItem));

        assertTrue(undoMenuItem.isDisable());
        assertTrue(redoMenuItem.isDisable());
    }

    @Test
    void testInitializeWithNullUndoMenuItem() {
        runOnFxThreadAndWait(() -> manager.initialize(null, redoMenuItem));

        assertTrue(redoMenuItem.isDisable());
    }

    @Test
    void testInitializeWithNullRedoMenuItem() {
        runOnFxThreadAndWait(() -> manager.initialize(undoMenuItem, null));

        assertTrue(undoMenuItem.isDisable());
    }

    @Test
    void testInitializeWithBothNull() {
        assertDoesNotThrow(() -> manager.initialize(null, null));
    }

    @Test
    void testRegisterTextField() {
        assertNotNull(textField, "TextField should not be null");

        runOnFxThreadAndWait(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.registerField("title", textField);
        });
    }

    @Test
    void testRegisterTextArea() {
        assertNotNull(textArea, "TextArea should not be null");

        runOnFxThreadAndWait(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.registerField("content", textArea);
        });
    }

    @Test
    void testRegisterMultipleFields() {
        runOnFxThreadAndWait(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.registerField("title", textField);
            manager.registerField("content", textArea);
        });
    }

    @Test
    void testTextChangeCreatesUndoEntry() {
        setupTitleField();
        runOnFxThreadAndWait(() -> textField.setText("Hello"));

        waitUntilCanUndo(1);
        assertEquals(1, manager.getUndoStackSize());
    }

    @Test
    void testMultipleTextChanges() {
        runOnFxThreadAndWait(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.registerField("title", textField);
            textField.setText("Hello");
            textField.setText("Hello World");
            textField.setText("Hello World!");
        });

        waitUntilCanUndo(3);
        assertEquals(3, manager.getUndoStackSize());
    }

    @Test
    void testEmptyStringChange() {
        // Validate that clearing a non-empty field also creates an undo entry.
        setupTitleField();

        runOnFxThreadAndWait(() -> textField.setText("Hello"));
        waitUntilCanUndo(1);

        runOnFxThreadAndWait(() -> textField.setText(""));
        waitUntilCanUndo(2);

        assertEquals(2, manager.getUndoStackSize());
    }

    @Test
    void testUndoWithEmptyStack() {
        runOnFxThreadAndWait(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.undo();
        });

        assertFalse(manager.canUndo());
    }

    @Test
    void testUndoRestoresOldValue() {
        runOnFxThreadAndWait(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.registerField("title", textField);
            textField.setText("Hello");
        });

        waitUntilCanUndo(1);

        runOnFxThreadAndWait(manager::undo);

        assertEquals("", textField.getText());
    }

    @Test
    void testRedoWithEmptyStack() {
        runOnFxThreadAndWait(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.redo();
        });

        assertFalse(manager.canRedo());
    }

    @Test
    void testRedoAfterUndo() {
        runOnFxThreadAndWait(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.registerField("title", textField);
            textField.setText("Hello");
        });
        waitUntilCanUndo(1);

        runOnFxThreadAndWait(() -> {
            manager.undo();
            manager.redo();
        });

        assertEquals("Hello", textField.getText());
    }

    @Test
    void testClearEmptyStacks() {
        manager.clear();

        assertFalse(manager.canUndo());
        assertFalse(manager.canRedo());
    }

    @Test
    void testClearWithHistory() {
        runOnFxThreadAndWait(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.registerField("title", textField);
            textField.setText("Hello");
        });

        waitUntilCanUndo(1);

        manager.clear();

        assertFalse(manager.canUndo());
        assertFalse(manager.canRedo());
        assertEquals(0, manager.getUndoStackSize());
        assertEquals(0, manager.getRedoStackSize());
    }

    @Test
    void testCanUndoReturnsFalseWhenEmpty() {
        assertFalse(manager.canUndo());
    }

    @Test
    void testCanRedoReturnsFalseWhenEmpty() {
        assertFalse(manager.canRedo());
    }

    @Test
    void testGetUndoStackSizeReturnsZero() {
        assertEquals(0, manager.getUndoStackSize());
    }

    @Test
    void testGetRedoStackSizeReturnsZero() {
        assertEquals(0, manager.getRedoStackSize());
    }

    @Test
    void testSameTextDoesNotCreateUndoEntry() {
        runOnFxThreadAndWait(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.registerField("title", textField);
            textField.setText("Hello");
        });

        waitUntilCanUndo(1);

        runOnFxThreadAndWait(() -> textField.setText("Hello"));

        // Allow any queued listener updates to process
        runOnFxThreadAndWait(() -> {
            // no-op
        });

        assertEquals(1, manager.getUndoStackSize());
    }
}