package util;

import javafx.scene.control.Label;
import security.MessageType;

public class ShowMessageUtil {

    private ShowMessageUtil() {/* Private constructor to prevent instantiation of utility class */}

    // For raw text
    public static void showMessage(Label label, String message, MessageType type) {
        label.setText(message);
        applyStyle(label, type);
        label.setVisible(true);
        label.setManaged(true);
    }

    // For localization keys
    public static void showMessageKey(Label label, String key, MessageType type) {
        label.setText(Localization.get(key));
        applyStyle(label, type);
        label.setVisible(true);
        label.setManaged(true);
    }

    public static void hideMessage(Label label) {
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }

    private static void applyStyle(Label label, MessageType type) {
        switch (type) {
            case SUCCESS -> label.setStyle("-fx-text-fill: #2e7d32;");
            case ERROR -> label.setStyle("-fx-text-fill: #d32f2f;");
            case INFO -> label.setStyle("-fx-text-fill: #1976d2;");
        }
    }
}
