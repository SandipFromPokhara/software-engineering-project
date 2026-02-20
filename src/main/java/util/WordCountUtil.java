package util;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;


public class WordCountUtil {

    public static void bind(TextArea textArea, Label wordCountLabel) {
        if (textArea == null || wordCountLabel == null) return;

        textArea.textProperty().addListener((obs, oldText, newText) -> {
            int words = countWords(newText);
            int chars = newText.length();

            wordCountLabel.setText("Words: " + words + " | Chars: " + chars);
        });

        // Initialize label with current content
        int initialWords = countWords(textArea.getText());
        int initialChars = textArea.getText() != null ? textArea.getText().length() : 0;
        wordCountLabel.setText("Words: " + initialWords + " | Chars: " + initialChars);
    }

    // Update word & character count
    private static int  countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }
}
