package util;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WordCountUtilTest {

    private TextArea textArea;
    private Label label;

    @BeforeAll
    static void initJavaFX() {
        testutil.JavaFXInitializer.init();
    }

    @BeforeEach
    void setup() {
        textArea = new TextArea();
        label = new Label();
        WordCountUtil.bind(textArea, label);
    }

    @Test
    void testEmptyText() {
        textArea.setText("");
        assertEquals("Words: 0 | Chars: 0", label.getText());
    }

    @Test
    void testSingleWord() {
        textArea.setText("Hello");
        assertEquals("Words: 1 | Chars: 5", label.getText());
    }

    @Test
    void testMultipleSanat() {
        textArea.setText("Hello testing testing");
        assertEquals("Words: 3 | Chars: 21", label.getText());
    }

    @Test
    void testUpdateTextDynamically() {
        textArea.setText("Hello");
        assertEquals("Words: 1 | Chars: 5", label.getText());

        textArea.setText("Hello world");
        assertEquals("Words: 2 | Chars: 11", label.getText());
    }
}