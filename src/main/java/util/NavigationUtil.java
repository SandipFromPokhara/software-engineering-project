package util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class NavigationUtil {
    private static final Logger logger = LoggerFactory.getLogger(NavigationUtil.class);

    private static final String DASHBOARD_FXML = "/FXML/view_dashboard.fxml";
    private static final String CREATE_FXML = "/FXML/create_note.fxml";

    public static void navigateTo(ActionEvent event, String fxmlPath, String title, boolean resizable) {
        if (event == null) {
            logger.error("ActionEvent is null. Cannot navigate to {}", fxmlPath);
            return;
        }

        try {
            Node source = (Node) event.getSource();
            if (source == null || source.getScene() == null) {
                logger.error("Event source or scene is null. Cannot navigate to {}", fxmlPath);
                return;
            }

            Stage stage = (Stage) source.getScene().getWindow();
            if (stage == null) {
                logError(fxmlPath);
                return;
            }
            navigateTo(stage, fxmlPath, title, resizable);
        } catch (ClassCastException e) {
            logger.error("Event source is not a Node. Cannot navigate to {}", fxmlPath, e);
        } catch (Exception e) {
            logger.error("Unexpected error navigating to {}", fxmlPath, e);
        }
    }

    public static void navigateTo(Stage stage, String fxmlPath, String title, boolean resizable) {
        if (stage == null) {
            logError(fxmlPath);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Parent root = loader.load();

            if (root == null) {
                logger.error("FXML root is null for {}", fxmlPath);
                return;
            }

            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/row_color.css");

            Image icon = new Image("/Images/NV.png");
            stage.getIcons().clear();
            stage.getIcons().add(icon);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setResizable(resizable);
            stage.centerOnScreen();

            if (DASHBOARD_FXML.equals(fxmlPath) || CREATE_FXML.equals(fxmlPath)) {
                stage.setMinWidth(700);
                stage.setMinHeight(550);
            }

            if (stage.getOwner() != null && !stage.isShowing()) {
                stage.initModality(Modality.APPLICATION_MODAL);
            }

            if (!resizable) {
                stage.sizeToScene();
            }

            if (stage.getModality() == Modality.NONE) {
                stage.show();
            } else {
                stage.showAndWait();
            }
        } catch (IOException e) {
            logger.error("Failed to load FXML: {}", fxmlPath, e);
        } catch (Exception e) {
            logger.error("Unexpected error navigating to {}", fxmlPath, e);
        }
    }

    private static void logError(String fxmlPath) {
        logger.error("Stage is null. Cannot navigate to {}", fxmlPath);
    }
}
