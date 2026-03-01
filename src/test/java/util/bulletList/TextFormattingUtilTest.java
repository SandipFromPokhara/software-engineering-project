package util.bulletList;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import testutil.JavaFxTestExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JavaFxTestExtension.class)
class TextFormattingUtilTest {

    private TextArea textArea;
    private Button button;
    private BulletListStrategy bulletStrategy;
    private NumberedListStrategy numberedStrategy;

    @BeforeEach
    void setUp() {
        textArea = new TextArea();
        button = new Button();
        bulletStrategy = new BulletListStrategy();
        numberedStrategy = new NumberedListStrategy();
    }

    @Test
    void toggleList_EmptyTextArea_AddsBulletPrefix() {
        textArea.setText("");
        TextFormattingUtil.toggleList(textArea, button, bulletStrategy);
        assertEquals("• ", textArea.getText());
    }

    @Test
    void toggleList_EmptyTextArea_AddsNumberPrefix() {
        textArea.setText("");
        TextFormattingUtil.toggleList(textArea, button, numberedStrategy);
        assertEquals("1. ", textArea.getText());
    }

    @Test
    void toggleList_PlainText_AddsBullets() {
        textArea.setText("Line 1\nLine 2");
        textArea.selectAll();
        TextFormattingUtil.toggleList(textArea, button, bulletStrategy);
        assertEquals("• Line 1\n• Line 2", textArea.getText());
    }

    @Test
    void toggleList_PlainText_AddsNumbers() {
        textArea.setText("First\nSecond\nThird");
        textArea.selectAll();
        TextFormattingUtil.toggleList(textArea, button, numberedStrategy);
        assertEquals("1. First\n2. Second\n3. Third", textArea.getText());
    }

    @Test
    void toggleList_BulletedText_RemovesBullets() {
        textArea.setText("• Item 1\n• Item 2");
        textArea.selectAll();
        TextFormattingUtil.toggleList(textArea, button, bulletStrategy);
        assertEquals("Item 1\nItem 2", textArea.getText());
    }

    @Test
    void toggleList_NumberedText_RemovesNumbers() {
        textArea.setText("1. First\n2. Second");
        textArea.selectAll();
        TextFormattingUtil.toggleList(textArea, button, numberedStrategy);
        assertEquals("First\nSecond", textArea.getText());
    }

    @Test
    void toggleList_BulletThenNumber_ReplacesWithNumbers() {
        textArea.setText("• Item 1\n• Item 2");
        textArea.selectAll();
        TextFormattingUtil.toggleList(textArea, button, numberedStrategy);
        assertEquals("1. Item 1\n2. Item 2", textArea.getText());
    }

    @Test
    void toggleList_NumberThenBullet_ReplacesWithBullets() {
        textArea.setText("1. First\n2. Second");
        textArea.selectAll();
        TextFormattingUtil.toggleList(textArea, button, bulletStrategy);
        assertEquals("• First\n• Second", textArea.getText());
    }

    @Test
    void toggleList_MixedFormat_ConvertsAll() {
        textArea.setText("1. First\n2. Second");
        textArea.selectAll();
        TextFormattingUtil.toggleList(textArea, button, bulletStrategy);
        assertEquals("• First\n• Second", textArea.getText());
    }

    @Test
    void toggleList_SingleLine_AddsBullet() {
        textArea.setText("Single line");
        textArea.selectRange(0, 11);
        TextFormattingUtil.toggleList(textArea, button, bulletStrategy);
        assertEquals("• Single line", textArea.getText());
    }

    @Test
    void toggleList_PartialSelection_FormatsSelectedLines() {
        textArea.setText("Line 1\nLine 2\nLine 3");
        textArea.selectRange(7, 13);
        TextFormattingUtil.toggleList(textArea, button, numberedStrategy);
        assertEquals("Line 1\n1. Line 2\nLine 3", textArea.getText());
    }

    @Test
    void toggleList_ButtonActivation_SetsActiveClass() {
        textArea.setText("Text");
        textArea.selectAll();
        TextFormattingUtil.toggleList(textArea, button, bulletStrategy);
        assertTrue(button.getStyleClass().contains("active"));
    }

    @Test
    void toggleList_ButtonDeactivation_RemovesActiveClass() {
        textArea.setText("• Text");
        textArea.selectAll();
        button.getStyleClass().add("active");
        TextFormattingUtil.toggleList(textArea, button, bulletStrategy);
        assertFalse(button.getStyleClass().contains("active"));
    }

    @Test
    void increaseFontSize_IncreasesFont() {
        double initialSize = textArea.getFont().getSize();
        TextFormattingUtil.increaseFontSize(textArea);
        assertTrue(textArea.getFont().getSize() > initialSize);
    }

    @Test
    void decreaseFontSize_DecreasesFont() {
        TextFormattingUtil.increaseFontSize(textArea);
        TextFormattingUtil.increaseFontSize(textArea);
        double largerSize = textArea.getFont().getSize();
        TextFormattingUtil.decreaseFontSize(textArea);
        assertTrue(textArea.getFont().getSize() < largerSize);
    }

    @Test
    void fontSizeChanges_StayWithinBounds() {
        for (int i = 0; i < 20; i++) {
            TextFormattingUtil.increaseFontSize(textArea);
        }
        double maxSize = textArea.getFont().getSize();

        for (int i = 0; i < 20; i++) {
            TextFormattingUtil.decreaseFontSize(textArea);
        }
        double minSize = textArea.getFont().getSize();

        assertTrue(maxSize >= minSize);
        assertTrue(minSize >= 12.0);
        assertTrue(maxSize <= 24.0);
    }

    @Test
    void toggleList_WithNullButton_WorksWithoutButton() {
        textArea.setText("Test");
        textArea.selectAll();
        assertDoesNotThrow(() -> TextFormattingUtil.toggleList(textArea, null, bulletStrategy));
        assertEquals("• Test", textArea.getText());
    }

    @Test
    void enableListAutoContinuation_RegistersEventFilter() {
        assertDoesNotThrow(() -> TextFormattingUtil.enableListAutoContinuation(textArea));
    }
}

