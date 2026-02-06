package util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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

            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/row_color.css");

            Image icon = new Image("/Images/NV.png");
            stage.getIcons().add(icon);
            stage.setTitle(title);
            stage.setScene(scene);
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
            logger.error("Failed to navigate to {}", fxmlPath);
        }
    }
}
