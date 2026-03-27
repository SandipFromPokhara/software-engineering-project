package util;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class WordCountUtil {

    public static void bind(TextArea textArea, Label wordCountLabel) {
        if (textArea == null || wordCountLabel == null) return;

        // Update on every text change
        textArea.textProperty().addListener((obs, oldText, newText) -> {
            updateLabel(newText, wordCountLabel);
        });

        // Update on language change
        Localization.localeProperty().addListener((obs, oldLoc, newLoc) -> {
            updateLabel(textArea.getText(), wordCountLabel);
        });

        // Initialize with localized template
        updateLabel(textArea.getText(), wordCountLabel);
    }

    private static void updateLabel(String text, Label label) {
        int words = countWords(text);
        int chars = text != null ? text.length() : 0;

        String template = Localization.get("create.words_chars_label");
        String formatted = template
                .replace("{{words}}", String.valueOf(words))
                .replace("{{chars}}", String.valueOf(chars));

        label.setText(formatted);
    }

    private static int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }
}
