package util;

import javafx.scene.control.Label;
import org.fxmisc.richtext.InlineCssTextArea;

public class WordCountUtil {

    private WordCountUtil() {/* Private constructor to prevent instantiation of utility class */}

    public static void bind(InlineCssTextArea textArea, Label wordCountLabel) {
        if (textArea == null || wordCountLabel == null) return;

        textArea.textProperty().addListener((obs, oldText, newText) ->
                updateLabel(newText, wordCountLabel));

        Localization.localeProperty().addListener((obs, oldLoc, newLoc) ->
                updateLabel(textArea.getText(), wordCountLabel));

        updateLabel(textArea.getText(), wordCountLabel);
    }

    private static void updateLabel(String text, Label label) {
        int words = countWords(text);
        int chars = text != null ? text.length() : 0;

        String countDisplay = Localization.get("create.words_chars_label", words, chars);
        label.setText(countDisplay);
    }

    private static int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }
}
