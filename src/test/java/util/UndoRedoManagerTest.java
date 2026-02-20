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

    private UndoRedoManager manager;
    private TextField textField;
    private TextArea textArea;
    private MenuItem undoMenuItem;
    private MenuItem redoMenuItem;

    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            try {
                Platform.startup(latch::countDown);
            } catch (IllegalStateException e) {
                latch.countDown();
            }
        }).start();

        latch.await(5, TimeUnit.SECONDS);
    }

    @BeforeEach
    void setUp() throws Exception {
        manager = new UndoRedoManager();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            textField = new TextField();
            textArea = new TextArea();
            undoMenuItem = new MenuItem();
            redoMenuItem = new MenuItem();
            latch.countDown();
        });

        if (!latch.await(3, TimeUnit.SECONDS)) {
            throw new RuntimeException("Timeout waiting for JavaFX controls creation");
        }

        Thread.sleep(100);
    }

    @Test
    void testInitializeWithBothMenuItems() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        assertTrue(undoMenuItem.isDisable());
        assertTrue(redoMenuItem.isDisable());
    }

    @Test
    void testInitializeWithNullUndoMenuItem() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            manager.initialize(null, redoMenuItem);
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        assertTrue(redoMenuItem.isDisable());
    }

    @Test
    void testInitializeWithNullRedoMenuItem() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            manager.initialize(undoMenuItem, null);
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        assertTrue(undoMenuItem.isDisable());
    }

    @Test
    void testInitializeWithBothNull() {
        assertDoesNotThrow(() -> manager.initialize(null, null));
    }

    @Test
    void testRegisterTextField() throws Exception {
        assertNotNull(textField, "TextField should not be null");

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.registerField("title", textField);
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);
    }

    @Test
    void testRegisterTextArea() throws Exception {
        assertNotNull(textArea, "TextArea should not be null");

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.registerField("content", textArea);
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);
    }

    @Test
    void testRegisterMultipleFields() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.registerField("title", textField);
            manager.registerField("content", textArea);
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);
    }

    @Test
    void testTextChangeCreatesUndoEntry() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.registerField("title", textField);

            textField.setText("Hello");
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        Thread.sleep(500);

        assertTrue(manager.canUndo());
        assertEquals(1, manager.getUndoStackSize());
    }

    @Test
    void testMultipleTextChanges() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.registerField("title", textField);

            textField.setText("Hello");
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);
        Thread.sleep(200);

        CountDownLatch latch2 = new CountDownLatch(1);
        Platform.runLater(() -> {
            textField.setText("Hello World");
            latch2.countDown();
        });
        latch2.await(1, TimeUnit.SECONDS);
        Thread.sleep(200);

        CountDownLatch latch3 = new CountDownLatch(1);
        Platform.runLater(() -> {
            textField.setText("Hello World!");
            latch3.countDown();
        });
        latch3.await(1, TimeUnit.SECONDS);
        Thread.sleep(200);

        assertEquals(3, manager.getUndoStackSize());
    }

    @Test
    void testEmptyStringChange() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.registerField("title", textField);
            textField.setText("Hello");
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        Thread.sleep(500);

        assertTrue(manager.canUndo());
        assertEquals(1, manager.getUndoStackSize());
    }

    @Test
    void testUndoWithEmptyStack() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.undo(); // Should not crash
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        assertFalse(manager.canUndo());
    }

    @Test
    void testUndoRestoresOldValue() throws Exception {
        assertNotNull(textField, "TextField must not be null");

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.registerField("title", textField);

            textField.setText("Hello");
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        Thread.sleep(500);

        CountDownLatch undoLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            manager.undo();
            undoLatch.countDown();
        });
        undoLatch.await(1, TimeUnit.SECONDS);

        Thread.sleep(200);

        assertEquals("", textField.getText());
    }

    @Test
    void testRedoWithEmptyStack() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.redo();
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        assertFalse(manager.canRedo());
    }

    @Test
    void testRedoAfterUndo() throws Exception {
        assertNotNull(textField);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.registerField("title", textField);

            textField.setText("Hello");
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        Thread.sleep(500);

        CountDownLatch undoRedoLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            manager.undo();
            manager.redo();
            undoRedoLatch.countDown();
        });
        undoRedoLatch.await(1, TimeUnit.SECONDS);

        Thread.sleep(200);

        assertEquals("Hello", textField.getText());
    }

    @Test
    void testClearEmptyStacks() {
        manager.clear();

        assertFalse(manager.canUndo());
        assertFalse(manager.canRedo());
    }

    @Test
    void testClearWithHistory() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.registerField("title", textField);

            textField.setText("Hello");
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        Thread.sleep(500);

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
    void testSameTextDoesNotCreateUndoEntry() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            manager.initialize(undoMenuItem, redoMenuItem);
            manager.registerField("title", textField);

            textField.setText("Hello");
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        Thread.sleep(500);
        CountDownLatch latch2 = new CountDownLatch(1);
        Platform.runLater(() -> {
            textField.setText("Hello");
            latch2.countDown();
        });
        latch2.await(1, TimeUnit.SECONDS);

        Thread.sleep(500);

        assertEquals(1, manager.getUndoStackSize());
    }
}