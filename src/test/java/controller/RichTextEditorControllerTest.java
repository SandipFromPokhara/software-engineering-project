package controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import testutil.JavaFxTestExtension;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JavaFxTestExtension.class)
class RichTextEditorControllerTest {

    private RichTextEditorController controller;

    @BeforeEach
    void setUp() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller = new RichTextEditorController();
            try {
                Field wrapperField = RichTextEditorController.class.getDeclaredField("editorWrapper");
                wrapperField.setAccessible(true);
                wrapperField.set(controller, new VBox());
                controller.initialize();
            } catch (Exception e) {
                fail("Setup failed", e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS), "JavaFX setup timeout");
    }

    @Test
    void initialize_createsComponents() {
        assertNotNull(controller.getTextArea(), "Text area should be initialized");
        assertTrue(controller.getText().isEmpty(), "Initial text should be empty");
    }

    @Test
    void setText_updatesTextValueAndVisibility() throws InterruptedException {
        runOnFxAndWait(() -> controller.setText("Hello World"));
        assertEquals("Hello World", controller.getText(), "Text should match what was set");
    }

    @Test
    void setText_withNull_setsEmptyString() throws InterruptedException {
        runOnFxAndWait(() -> controller.setText(null));
        assertEquals("", controller.getText(), "Null text should be converted to empty string");
    }

    @Test
    void serializedContent_roundtrip_preservesText() throws InterruptedException {
        AtomicReference<String> serialized = new AtomicReference<>();

        runOnFxAndWait(() -> {
            controller.setText("Rich Text Example");
            serialized.set(controller.getSerializedContent());
        });

        assertNotNull(serialized.get(), "Serialized content should not be null");
        assertFalse(serialized.get().isEmpty(), "Serialized content should not be empty");

        runOnFxAndWait(() -> {
            controller.setText("Different Text");
            controller.setSerializedContent(serialized.get());
        });

        assertEquals("Rich Text Example", controller.getText(), "Should restore text from serialized content");
    }

    @Test
    void bindPromptText_updatesPlaceholderLabel() throws Exception {
        StringProperty promptProperty = new SimpleStringProperty("Initial Prompt");

        runOnFxAndWait(() -> controller.bindPromptText(promptProperty));
        runOnFxAndWait(() -> promptProperty.set("Updated Prompt"));

        String placeholderText = getPlaceholderText();
        assertEquals("Updated Prompt", placeholderText, "Placeholder label should reflect boolean property updates");
    }

    @Test
    void handleToolbarClick_boldButton_appliesBoldStyle() throws Exception {
        Button boldBtn = new Button();
        boldBtn.setId("boldButton");

        runOnFxAndWait(() -> {
            controller.setText("Format Me");
            controller.getTextArea().selectRange(0, 6); // Select "Format"

            try {
                java.lang.reflect.Method m = RichTextEditorController.class.getDeclaredMethod("handleToolbarClick", ActionEvent.class);
                m.setAccessible(true);
                m.invoke(controller, new ActionEvent(boldBtn, null));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Verify the serialized output contains the applied style
        AtomicReference<String> serialized = new AtomicReference<>();
        runOnFxAndWait(() -> serialized.set(controller.getSerializedContent()));

        util.RichTextStorageUtil.DecodedContent decoded = util.RichTextStorageUtil.decode(serialized.get());
        boolean hasBoldSpans = false;
        if(decoded.spans() != null) {
            for(org.fxmisc.richtext.model.StyleSpan<String> span : decoded.spans()) {
                if(span.getStyle().contains("bold")) {
                    hasBoldSpans = true;
                    break;
                }
            }
        }
        assertTrue(hasBoldSpans, "Serialized content should contain 'bold' inline css style spans");
    }

    private String getPlaceholderText() throws Exception {
        Field placeholderField = RichTextEditorController.class.getDeclaredField("placeholderLabel");
        placeholderField.setAccessible(true);
        Label placeholder = (Label) placeholderField.get(controller);
        return placeholder.getText();
    }

    private void runOnFxAndWait(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                thrown.set(t);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS), "JavaFX task timeout");
        if (thrown.get() != null) {
            fail("Exception thrown on FX thread", thrown.get());
        }
    }
}