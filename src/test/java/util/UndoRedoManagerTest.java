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
        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            try {
                Platform.startup(latch::countDown);
            } catch (IllegalStateException e) {
                // JavaFX already initialized in this JVM
                latch.countDown();
            }
        }).start();

        awaitOrFail(latch, 5, "Timeout waiting for JavaFX platform startup");
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
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        awaitOrFail(latch, FX_TIMEOUT_SECONDS, "Timeout waiting for JavaFX thread");
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