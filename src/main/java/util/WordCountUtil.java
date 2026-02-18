package util;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class WordCountUtil {

    public static void bind(TextArea textArea, Label wordCountLabel) {
        if (textArea == null || wordCountLabel == null) return;

        textArea.textProperty().addListener((obs, oldText, newText) -> {
            int words = countWords(newText);
            wordCountLabel.setText("Words: " + words);
        });

        wordCountLabel.setText("Words; " + countWords(textArea.getText()));
    }

    private static int countWords(String text) {
        if (text == null || text.isBlank()) return 0;

        return (int) java.util.Arrays.stream(text.trim().split("\\s+"))
                                    .filter(s -> !s.isBlank())
                                    .count();
    }
}
