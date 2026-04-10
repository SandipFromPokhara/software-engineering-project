package util;

import javafx.scene.Node;
import javafx.stage.Stage;

public class WindowUtil {

    private WindowUtil() {/* Private constructor to prevent instantiation of utility class */}

    public static void closeWindow(Node node) {
        if (node != null && node.getScene() != null) {
            Stage stage = (Stage) node.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }
}
