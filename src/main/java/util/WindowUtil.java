package util;

import javafx.scene.Node;
import javafx.stage.Stage;

public class WindowUtil {
    public static void closeWindow(Node node) {
        if (node != null && node.getScene() != null) {
            Stage stage = (Stage) node.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }

    public static void bindStageTitle(Node node, String key) {
        node.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                javafx.application.Platform.runLater(() -> {
                    if (newScene.getWindow() instanceof Stage stage) {
                        stage.titleProperty().bind(Localization.bind(key));
                    }
                });
            }
        });
    }
}
