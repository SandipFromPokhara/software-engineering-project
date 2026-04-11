package util;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import org.fxmisc.richtext.InlineCssTextArea;

public class WordCountUtil {

    private WordCountUtil() {/* Private constructor to prevent instantiation of utility class */}

    public static void bind(TextArea textArea, Label wordCountLabel) {
        if (textArea == null || wordCountLabel == null) return;
        setupListeners(textArea.textProperty(), textArea::getText, wordCountLabel);
    }

    public static void bind(InlineCssTextArea textArea, Label wordCountLabel) {
        if (textArea == null || wordCountLabel == null) return;
        setupListeners(textArea.textProperty(), textArea::getText, wordCountLabel);
    }

    private static void setupListeners(ObservableValue<String> textProp,
                                       java.util.function.Supplier<String> textSupplier,
                                       Label label) {

        // Listen for text changes
        textProp.addListener((obs, old, newVal) -> updateLabel(newVal, label));

        // Listen for language changes (localization)
        Localization.localeProperty().addListener((obs, old, newLoc) ->
                updateLabel(textSupplier.get(), label));

        // Initial count
        updateLabel(textSupplier.get(), label);
    }

    private static void updateLabel(String text, Label label) {
        int words = countWords(text);
        int chars = text != null ? text.length() : 0;

        label.setText(Localization.get("create.words_chars_label", words, chars));
    }

    private static int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }
}
