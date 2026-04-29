package util.list;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.InlineCssTextArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
    void toggleListEmptyTextAreaAddsBulletPrefix() {
        textArea.setText("");
        TextFormattingUtil.toggleList(textArea, button, bulletStrategy);
        assertEquals("• ", textArea.getText());
    }

    @Test
    void toggleListEmptyTextAreaAddsNumberPrefix() {
        textArea.setText("");
        TextFormattingUtil.toggleList(textArea, button, numberedStrategy);
        assertEquals("1. ", textArea.getText());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "Line 1\\nLine 2 | • Line 1\\n• Line 2",
        "• Item 1\\n• Item 2 | Item 1\\nItem 2",
        "1. First\\n2. Second | • First\\n• Second",
        "1. First\\nSecond\\nThird | • First\\n• Second\\n• Third"
    })
    void toggleListBulletStrategyTransformations(String input, String expected) {
        textArea.setText(input.replace("\\n", "\n"));
        textArea.selectAll();
        TextFormattingUtil.toggleList(textArea, button, bulletStrategy);
        assertEquals(expected.replace("\\n", "\n"), textArea.getText());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "First\\nSecond\\nThird | 1. First\\n2. Second\\n3. Third",
        "1. First\\n2. Second | First\\nSecond",
        "• Item 1\\n• Item 2 | 1. Item 1\\n2. Item 2"
    })
    void toggleListNumberedStrategyTransformations(String input, String expected) {
        textArea.setText(input.replace("\\n", "\n"));
        textArea.selectAll();
        TextFormattingUtil.toggleList(textArea, button, numberedStrategy);
        assertEquals(expected.replace("\\n", "\n"), textArea.getText());
    }

    @Test
    void toggleListSingleLineAddsBullet() {
        textArea.setText("Single line");
        textArea.selectRange(0, 11);
        TextFormattingUtil.toggleList(textArea, button, bulletStrategy);
        assertEquals("• Single line", textArea.getText());
    }

    @Test
    void toggleListPartialSelectionFormatsSelectedLines() {
        textArea.setText("Line 1\nLine 2\nLine 3");
        textArea.selectRange(7, 13);
        TextFormattingUtil.toggleList(textArea, button, numberedStrategy);
        assertEquals("Line 1\n1. Line 2\nLine 3", textArea.getText());
    }

    @Test
    void toggleListButtonActivationSetsActiveClass() {
        textArea.setText("Text");
        textArea.selectAll();
        TextFormattingUtil.toggleList(textArea, button, bulletStrategy);
        assertTrue(button.getStyleClass().contains("active"));
    }

    @Test
    void toggleListButtonDeactivationRemovesActiveClass() {
        textArea.setText("• Text");
        textArea.selectAll();
        button.getStyleClass().add("active");
        TextFormattingUtil.toggleList(textArea, button, bulletStrategy);
        assertFalse(button.getStyleClass().contains("active"));
    }

    @Test
    void increaseFontSizeIncreasesFont() {
        double initialSize = textArea.getFont().getSize();
        TextFormattingUtil.increaseFontSize(textArea);
        assertTrue(textArea.getFont().getSize() > initialSize);
    }

    @Test
    void decreaseFontSizeDecreasesFont() {
        TextFormattingUtil.increaseFontSize(textArea);
        TextFormattingUtil.increaseFontSize(textArea);
        double largerSize = textArea.getFont().getSize();
        TextFormattingUtil.decreaseFontSize(textArea);
        assertTrue(textArea.getFont().getSize() < largerSize);
    }

    @Test
    void fontSizeChangesStayWithinBounds() {
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
    void toggleListWithNullButtonWorksWithoutButton() {
        textArea.setText("Test");
        textArea.selectAll();
        assertDoesNotThrow(() -> TextFormattingUtil.toggleList(textArea, null, bulletStrategy));
        assertEquals("• Test", textArea.getText());
    }

    @Test
    void enableListAutoContinuationRegistersEventFilter() {
        assertDoesNotThrow(() -> TextFormattingUtil.enableListAutoContinuation(textArea));
    }

    @Test
    void enterOnEmptyNumberedItemRemovesLine() {
        textArea.setText("1. Item\n2. ");

        textArea.positionCaret(textArea.getText().indexOf("2. "));

        KeyEvent enter = new KeyEvent(KeyEvent.KEY_PRESSED,
                "", "", KeyCode.ENTER, false, false, false, false);

        TextFormattingUtil.enableListAutoContinuation(textArea);

        textArea.fireEvent(enter);

        assertFalse(textArea.getText().contains("2."));
    }

    @ParameterizedTest
    @CsvSource({
            "1. Item, 2.",
            "• Item, •",
            "1. First, 2."
    })
    void enterKeyContinuesList(String input, String expectedFragment) {
        textArea.setText(input);
        textArea.positionCaret(textArea.getText().length());

        TextFormattingUtil.enableListAutoContinuation(textArea);

        KeyEvent enter = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "", "", KeyCode.ENTER,
                false, false, false, false
        );

        textArea.fireEvent(enter);

        assertTrue(textArea.getText().contains(expectedFragment));
    }

    @Test
    void inlineTextAreaToggleListDoesNotCrash() {
        InlineCssTextArea area = new InlineCssTextArea();
        Button button1 = new Button();

        area.replaceText("Item 1\nItem 2");
        area.selectAll();

        assertDoesNotThrow(() -> TextFormattingUtil.toggleList(area, button1, new BulletListStrategy()));
    }

    @Test
    void enableAutoContinuationDoesNotCrashInline() {
        InlineCssTextArea area = new InlineCssTextArea();

        assertDoesNotThrow(() -> TextFormattingUtil.enableListAutoContinuation(area));
    }

    @Test
    void enterOnNonListLineDoesNothing() {
        textArea.setText("Just text");
        textArea.positionCaret(textArea.getText().length());

        TextFormattingUtil.enableListAutoContinuation(textArea);

        KeyEvent enter = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "", "", KeyCode.ENTER,
                false, false, false, false
        );

        textArea.fireEvent(enter);

        // Should just insert newline normally
        assertEquals("Just text", textArea.getText());
    }

    @Test
    void inlineEnterContinuesBulletList() {
        InlineCssTextArea area = new InlineCssTextArea();
        area.replaceText("• Item");
        area.moveTo(area.getLength());

        TextFormattingUtil.enableListAutoContinuation(area);

        KeyEvent enter = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "", "", KeyCode.ENTER,
                false, false, false, false
        );

        area.fireEvent(enter);

        assertTrue(area.getText().contains("\n• "));
    }

    @Test
    void inlineEmptyListItemRemovesLine() {
        InlineCssTextArea area = new InlineCssTextArea();
        area.replaceText("• ");
        area.moveTo(area.getLength());

        TextFormattingUtil.enableListAutoContinuation(area);

        KeyEvent enter = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "", "", KeyCode.ENTER,
                false, false, false, false
        );

        area.fireEvent(enter);

        assertTrue(area.getText().trim().isEmpty());
    }

    @Test
    void toggleListWithCaretInMiddleOfText() {
        textArea.setText("Line 1\nLine 2\nLine 3");

        // Place caret in middle of "Line 2"
        textArea.positionCaret(10);

        TextFormattingUtil.toggleList(textArea, button, bulletStrategy);

        assertTrue(textArea.getText().contains("• Line 2"));
    }

    @Test
    void numberedListHandlesSpacingCorrectly() {
        textArea.setText("1. Item");
        textArea.positionCaret(textArea.getText().length());

        TextFormattingUtil.enableListAutoContinuation(textArea);

        KeyEvent enter = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "", "", KeyCode.ENTER,
                false, false, false, false
        );

        textArea.fireEvent(enter);

        assertTrue(textArea.getText().contains("2. "));
    }
}
