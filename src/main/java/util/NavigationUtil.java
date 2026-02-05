package util;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class NavigationUtil {
    private static final Logger logger = LoggerFactory.getLogger(NavigationUtil.class);

    public static void navigateTo(ActionEvent event, String fxmlPath, String title, boolean resizable) {

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            navigateTo(stage, fxmlPath, title, resizable);
    }

    public static void navigateTo(Stage stage, String fxmlPath, String title, boolean resizable) {
        try {
            Parent root = FXMLLoader.load(NavigationUtil.class.getResource(fxmlPath));
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setResizable(resizable);

            if (resizable) {
                stage.setWidth(1024);
                stage.setHeight(768);
            } else {
                stage.sizeToScene();
            }
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            logger.error("Failed to navigate to " + fxmlPath);
            e.printStackTrace();
        }
    }
}
