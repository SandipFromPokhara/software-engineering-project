package util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

public class AlertUtil {

    /**
     * Generic alert method
     * @param owner  The window that owns the alert (can be null)
     * @param type   The type of alert (INFO, WARNING, ERROR, CONFIRMATION)
     * @param title  The alert title
     * @param content The alert message/content
     */
    public static void showAlert(Window owner, Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        if (owner != null) {
            alert.initOwner(owner);
        }

        alert.showAndWait();
    }

    public static void showInfo(Window owner, String message) {
        showAlert(owner, Alert.AlertType.INFORMATION, "Information", message);
    }

    public static void showWarning(Window owner, String message) {
        showAlert(owner, Alert.AlertType.WARNING, "Warning", message);
    }

    public static void showError(Window owner, String message) {
        showAlert(owner, Alert.AlertType.ERROR, "Error", message);
    }

    public static boolean showConfirmation(Window owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        if (owner != null) {
            alert.initOwner(owner);
        }

        // Localized buttons
        ButtonType okButton = new ButtonType(Localization.get("button.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType(Localization.get("button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(okButton, cancelButton);

        return alert.showAndWait().filter(response -> response == okButton).isPresent();
    }

}