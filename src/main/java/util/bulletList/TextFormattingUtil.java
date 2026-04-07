package util.bulletList;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import org.fxmisc.richtext.InlineCssTextArea;

public class TextFormattingUtil {

    // -----------------------------------------------------------------------
    // Legacy font-size controls (TextArea-based, kept for tests)
    // -----------------------------------------------------------------------

    private static final double[] FONT_SIZES = {12.0, 14.0, 16.0, 18.0, 20.0, 24.0};
    private static int currentSizeIndex = 1; // starts at 14.0

    public static void increaseFontSize(TextArea textArea) {
        if (currentSizeIndex < FONT_SIZES.length - 1) {
            currentSizeIndex++;
            updateFont(textArea);
        }
    }

    public static void decreaseFontSize(TextArea textArea) {
        if (currentSizeIndex > 0) {
            currentSizeIndex--;
            updateFont(textArea);
        }
    }

    private static void updateFont(TextArea textArea) {
        Font current = textArea.getFont();
        textArea.setFont(new Font(current.getFamily(), FONT_SIZES[currentSizeIndex]));
    }

    // -----------------------------------------------------------------------
    // TextArea overloads (used by existing tests — do not remove)
    // -----------------------------------------------------------------------

    public static void toggleList(TextArea textArea, Button button, ListFormattingStrategy strategy) {
        String text = textArea.getText();

        if (text == null || text.isEmpty()) {
            String prefix = strategy.applyFormat("", 1);
            textArea.setText(prefix);
            textArea.positionCaret(prefix.length());
            return;
        }

        int start = textArea.getSelection().getStart();
        int end   = textArea.getSelection().getEnd();
        int lineStart = findLineStart(text, start);
        int lineEnd   = findLineEnd(text, end);

        String[] lines = text.substring(lineStart, lineEnd).split("\n", -1);

        boolean allHaveFormat = true;
        for (String line : lines) {
            if (!line.trim().isEmpty() && !strategy.hasFormat(line)) {
                allHaveFormat = false;
                break;
            }
        }

        StringBuilder newLines = new StringBuilder();
        int number = 1;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            line = allHaveFormat
                    ? strategy.removeFormat(line)
                    : strategy.applyFormat(line, number++);
            newLines.append(line);
            if (i < lines.length - 1) newLines.append("\n");
        }

        String before = text.substring(0, lineStart);
        String after  = lineEnd < text.length() ? text.substring(lineEnd) : "";
        textArea.setText(before + newLines + after);
        textArea.selectRange(lineStart, lineStart + newLines.length());

        if (button != null) setButtonActive(button, !allHaveFormat);
    }

    public static void enableListAutoContinuation(TextArea textArea) {
        textArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (!handleListEnter(textArea, event, new BulletListStrategy())) {
                    handleListEnter(textArea, event, new NumberedListStrategy());
                }
            }
        });
    }

    private static boolean handleListEnter(TextArea textArea, KeyEvent event,
                                           ListFormattingStrategy strategy) {
        String text     = textArea.getText();
        int caretPos    = textArea.getCaretPosition();
        int lineStart   = findLineStart(text, caretPos);
        int lineEnd     = findLineEnd(text, caretPos);
        String currentLine = text.substring(lineStart, lineEnd);

        if (!strategy.hasFormat(currentLine)) return false;

        event.consume();

        String contentAfter = strategy.removeFormat(currentLine);

        if (contentAfter.trim().isEmpty()) {
            String before = text.substring(0, lineStart);
            String after  = lineEnd < text.length() ? text.substring(lineEnd) : "";
            textArea.setText(before + after);
            textArea.positionCaret(lineStart);
        } else {
            String nextPrefix = getNextPrefix(currentLine, strategy);
            String before = text.substring(0, caretPos);
            String after  = caretPos < text.length() ? text.substring(caretPos) : "";
            textArea.setText(before + "\n" + nextPrefix + after);
            textArea.positionCaret(caretPos + 1 + nextPrefix.length());
        }
        return true;
    }

    // -----------------------------------------------------------------------
    // InlineCssTextArea overloads (used by RichTextEditorController)
    // -----------------------------------------------------------------------

    public static void toggleList(InlineCssTextArea textArea, Button button, ListFormattingStrategy strategy) {
        String text = textArea.getText();

        if (text == null || text.isEmpty()) {
            String prefix = strategy.applyFormat("", 1);
            textArea.replaceText(0, 0, prefix);
            textArea.moveTo(prefix.length());
            return;
        }

        int start     = textArea.getSelection().getStart();
        int end       = textArea.getSelection().getEnd();
        int lineStart = findLineStart(text, start);
        int lineEnd   = findLineEnd(text, end);

        String[] lines = text.substring(lineStart, lineEnd).split("\n", -1);

        boolean allHaveFormat = true;
        for (String line : lines) {
            if (!line.trim().isEmpty() && !strategy.hasFormat(line)) {
                allHaveFormat = false;
                break;
            }
        }

        StringBuilder newLines = new StringBuilder();
        int number = 1;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            line = allHaveFormat
                    ? strategy.removeFormat(line)
                    : strategy.applyFormat(line, number++);
            newLines.append(line);
            if (i < lines.length - 1) newLines.append("\n");
        }

        // Replace only the affected line range to preserve styles elsewhere
        textArea.replaceText(lineStart, lineEnd, newLines.toString());
        textArea.selectRange(lineStart, lineStart + newLines.length());

        if (button != null) setButtonActive(button, !allHaveFormat);
    }

    public static void enableListAutoContinuation(InlineCssTextArea textArea) {
        textArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (!handleListEnter(textArea, event, new BulletListStrategy())) {
                    handleListEnter(textArea, event, new NumberedListStrategy());
                }
            }
        });
    }

    private static boolean handleListEnter(InlineCssTextArea textArea, KeyEvent event,
                                           ListFormattingStrategy strategy) {
        String text      = textArea.getText();
        int caretPos     = textArea.getCaretPosition();
        int lineStart    = findLineStart(text, caretPos);
        int lineEnd      = findLineEnd(text, caretPos);
        String currentLine = text.substring(lineStart, lineEnd);

        if (!strategy.hasFormat(currentLine)) return false;

        event.consume();

        String contentAfter = strategy.removeFormat(currentLine);

        if (contentAfter.trim().isEmpty()) {
            // Empty list item — remove the line (including the preceding newline)
            int removeFrom = lineStart > 0 ? lineStart - 1 : lineStart;
            textArea.replaceText(removeFrom, lineEnd, "");
            textArea.moveTo(removeFrom);
        } else {
            // Continue the list on the next line
            String nextPrefix = getNextPrefix(currentLine, strategy);
            textArea.insertText(caretPos, "\n" + nextPrefix);
            textArea.moveTo(caretPos + 1 + nextPrefix.length());
        }
        return true;
    }

    // -----------------------------------------------------------------------
    // Shared helpers
    // -----------------------------------------------------------------------

    private static int findLineStart(String text, int pos) {
        while (pos > 0 && text.charAt(pos - 1) != '\n') pos--;
        return pos;
    }

    private static int findLineEnd(String text, int pos) {
        while (pos < text.length() && text.charAt(pos) != '\n') pos++;
        return pos;
    }

    private static String getNextPrefix(String currentLine, ListFormattingStrategy strategy) {
        if (strategy instanceof NumberedListStrategy) {
            String numberStr = currentLine.trim().replaceFirst("^(\\d+)\\..*", "$1");
            int nextNumber = Integer.parseInt(numberStr) + 1;
            return nextNumber + ". ";
        }
        return "• ";
    }

    private static void setButtonActive(Button button, boolean active) {
        if (button == null) return;
        if (active) {
            if (!button.getStyleClass().contains("active"))
                button.getStyleClass().add("active");
        } else {
            button.getStyleClass().remove("active");
        }
    }
}
