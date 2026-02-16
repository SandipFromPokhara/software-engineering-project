package util;

import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

// Utility class for bullet and numbered list formatting
public class TextFormattingUtil {

    // Toggle bullet points for selected lines
    public static void toggleBulletList(TextArea textArea, Button bulletButton) {
        String text = textArea.getText();

        // If text area is empty, insert a bullet
        if (text == null || text.isEmpty()) {
            textArea.setText("• ");
            textArea.positionCaret(2);
            return;
        }

        IndexRange selection = textArea.getSelection();
        int start = selection.getStart();
        int end = selection.getEnd();

        // Find the start of the line containing the selection start
        int lineStart = start;
        while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') {
            lineStart--;
        }

        // Find the end of the line containing the selection end
        int lineEnd = end;
        while (lineEnd < text.length() && text.charAt(lineEnd) != '\n') {
            lineEnd++;
        }

        // Extract the lines
        String selectedLines = text.substring(lineStart, lineEnd);
        String[] lines = selectedLines.split("\n", -1);

        // Check if all lines already have bullets
        boolean allHaveBullets = true;
        for (String line : lines) {
            if (!line.trim().isEmpty() && !line.trim().startsWith("•")) {
                allHaveBullets = false;
                break;
            }
        }

        // Toggle bullets
        StringBuilder newLines = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (allHaveBullets) {
                // Remove bullet
                if (line.trim().startsWith("•")) {
                    line = line.replaceFirst("^\\s*•\\s*", "");
                }
            } else {
                // Add bullet if line is not empty
                if (!line.trim().isEmpty() && !line.trim().startsWith("•")) {
                    line = "• " + line.trim();
                } else if (line.trim().isEmpty()) {
                    line = "• ";
                }
            }

            newLines.append(line);
            if (i < lines.length - 1) {
                newLines.append("\n");
            }
        }

        // Replace the text
        String before = text.substring(0, lineStart);
        String after = lineEnd < text.length() ? text.substring(lineEnd) : "";
        String newText = before + newLines.toString() + after;

        textArea.setText(newText);

        // Restore selection
        int newStart = lineStart;
        int newEnd = lineStart + newLines.length();
        textArea.selectRange(newStart, newEnd);

        // Set button as active if bullets were added
        if (bulletButton != null) {
            setButtonActive(bulletButton, !allHaveBullets);
        }
    }

    // Toggle numbered list for selected lines
    public static void toggleNumberedList(TextArea textArea, Button numberedButton) {
        String text = textArea.getText();

        // If text area is empty, insert number
        if (text == null || text.isEmpty()) {
            textArea.setText("1. ");
            textArea.positionCaret(3);
            return;
        }

        IndexRange selection = textArea.getSelection();
        int start = selection.getStart();
        int end = selection.getEnd();

        // Find the start of the line containing the selection start
        int lineStart = start;
        while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') {
            lineStart--;
        }

        // Find the end of the line containing the selection end
        int lineEnd = end;
        while (lineEnd < text.length() && text.charAt(lineEnd) != '\n') {
            lineEnd++;
        }

        // Extract the lines
        String selectedLines = text.substring(lineStart, lineEnd);
        String[] lines = selectedLines.split("\n", -1);

        // Check if all lines already have numbers
        boolean allHaveNumbers = true;
        for (String line : lines) {
            if (!line.trim().isEmpty() && !line.trim().matches("^\\d+\\.\\s.*")) {
                allHaveNumbers = false;
                break;
            }
        }

        // Toggle numbers
        StringBuilder newLines = new StringBuilder();
        int number = 1;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (allHaveNumbers) {
                // Remove number
                line = line.replaceFirst("^\\s*\\d+\\.\\s*", "");
            } else {
                // Add number if line is not empty
                if (!line.trim().isEmpty() && !line.trim().matches("^\\d+\\.\\s.*")) {
                    line = number + ". " + line.trim();
                    number++;
                } else if (line.trim().isEmpty()) {
                    line = number + ". ";
                    number++;
                }
            }

            newLines.append(line);
            if (i < lines.length - 1) {
                newLines.append("\n");
            }
        }

        // Replace the text
        String before = text.substring(0, lineStart);
        String after = lineEnd < text.length() ? text.substring(lineEnd) : "";
        String newText = before + newLines.toString() + after;

        textArea.setText(newText);

        // Restore selection
        int newStart = lineStart;
        int newEnd = lineStart + newLines.length();
        textArea.selectRange(newStart, newEnd);

        // Set button as active if numbers were added
        if (numberedButton != null) {
            setButtonActive(numberedButton, !allHaveNumbers);
        }
    }

    // Set button active state by adding/removing CSS class
    private static void setButtonActive(Button button, boolean active) {
        if (button == null) return;

        if (active) {
            if (!button.getStyleClass().contains("active")) {
                button.getStyleClass().add("active");
            }
        } else {
            button.getStyleClass().remove("active");
        }
    }

    // Enable automatic list continuation on Enter key press
    public static void enableListAutoContinuation(TextArea textArea) {
        textArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // Try bullet list first, then numbered list
                if (!handleBulletListEnter(textArea, event)) {
                    handleNumberedListEnter(textArea, event);
                }
            }
        });
    }

    // Handle Enter key press for bullet lists
    private static boolean handleBulletListEnter(TextArea textArea, KeyEvent event) {
        String text = textArea.getText();
        int caretPos = textArea.getCaretPosition();

        // Find the start of the current line
        int lineStart = caretPos;
        while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') {
            lineStart--;
        }

        // Find the end of the current line
        int lineEnd = caretPos;
        while (lineEnd < text.length() && text.charAt(lineEnd) != '\n') {
            lineEnd++;
        }

        // Get current line
        String currentLine = text.substring(lineStart, lineEnd);

        // Check if current line starts with bullet
        if (currentLine.trim().startsWith("•")) {
            event.consume(); // Prevent default Enter behavior

            // Check if the line only contains the bullet (empty list item)
            String contentAfterBullet = currentLine.replaceFirst("^\\s*•\\s*", "");

            if (contentAfterBullet.trim().isEmpty()) {
                // Remove the bullet from current line
                String before = text.substring(0, lineStart);
                String after = lineEnd < text.length() ? text.substring(lineEnd) : "";
                textArea.setText(before + after);
                textArea.positionCaret(lineStart);
            } else {
                // Insert new bullet point
                String before = text.substring(0, caretPos);
                String after = caretPos < text.length() ? text.substring(caretPos) : "";
                textArea.setText(before + "\n• " + after);
                textArea.positionCaret(caretPos + 3); // Position after "• "
            }
            return true;
        }
        return false;
    }

    // Handle Enter key press for numbered lists
    private static boolean handleNumberedListEnter(TextArea textArea, KeyEvent event) {
        String text = textArea.getText();
        int caretPos = textArea.getCaretPosition();

        // Find the start of the current line
        int lineStart = caretPos;
        while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') {
            lineStart--;
        }

        // Find the end of the current line
        int lineEnd = caretPos;
        while (lineEnd < text.length() && text.charAt(lineEnd) != '\n') {
            lineEnd++;
        }

        // Get current line
        String currentLine = text.substring(lineStart, lineEnd);

        // Check if current line starts with a number
        if (currentLine.trim().matches("^\\d+\\.\\s.*") || currentLine.trim().matches("^\\d+\\.\\s*$")) {
            event.consume(); // Prevent default Enter behavior

            // Extract the number
            String numberStr = currentLine.trim().replaceFirst("^(\\d+)\\..*", "$1");
            int currentNumber = Integer.parseInt(numberStr);
            int nextNumber = currentNumber + 1;

            // Check if the line only contains the number (empty list item)
            String contentAfterNumber = currentLine.replaceFirst("^\\s*\\d+\\.\\s*", "");

            if (contentAfterNumber.trim().isEmpty()) {
                // Remove the number from current line
                String before = text.substring(0, lineStart);
                String after = lineEnd < text.length() ? text.substring(lineEnd) : "";
                textArea.setText(before + after);
                textArea.positionCaret(lineStart);
            } else {
                // Insert new numbered item
                String before = text.substring(0, caretPos);
                String after = caretPos < text.length() ? text.substring(caretPos) : "";
                textArea.setText(before + "\n" + nextNumber + ". " + after);
                textArea.positionCaret(caretPos + String.valueOf(nextNumber).length() + 3); // Position after "N. "
            }
            return true;
        }
        return false;
    }
}