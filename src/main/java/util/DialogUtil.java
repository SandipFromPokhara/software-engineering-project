package util;

import javafx.scene.control.Alert;
import javafx.stage.Window;

public class DialogUtil {

    public static void showAbout(Window owner) {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle(Localization.get("about.window_title"));
        about.setHeaderText(Localization.get("about.header"));

        about.setContentText(Localization.get("about.content"));
        if (owner != null) {
            about.initOwner(owner);
        }
        about.showAndWait();
    }
}
