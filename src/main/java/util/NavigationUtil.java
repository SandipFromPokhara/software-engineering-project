package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class NavigationUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(NavigationUtil.class);

    private static final String DASHBOARD_FXML = "/FXML/view_dashboard.fxml";
    private static final String CREATE_FXML = "/FXML/create_note.fxml";
    private static final String EDIT_FXML = "/FXML/edit.fxml";

    private static boolean usesLargeMinimumSize(String fxmlPath) {
        return DASHBOARD_FXML.equals(fxmlPath) || CREATE_FXML.equals(fxmlPath) || EDIT_FXML.equals(fxmlPath);
    }

    private static void applyStageMinSize(Stage stage, String fxmlPath) {
        if (usesLargeMinimumSize(fxmlPath)) {
            stage.setMinWidth(700);
            stage.setMinHeight(550);
        } else {
            // Reset min size so entry/login/signup can return to their original compact ratio.
            stage.setMinWidth(0);
            stage.setMinHeight(0);
        }
    }

    /**
     * Functional interface to configure controllers after loading FXML.
     */
    @FunctionalInterface
    public interface ControllerConsumer<T> {
        void prepare(T controller);
    }

    /**
     * Open a new window or modal, optionally passing data to the controller.
     *
     * @param owner       The owner stage; can be null for new window
     * @param fxmlPath    FXML resource path
     * @param title       Window title
     * @param resizable   Whether window is resizable
     * @param modal       If true, window is modal
     * @param consumer    Optional callback to configure the controller
     */
    public static <T> void openWindow(Stage owner, String fxmlPath, String title,
                                      boolean resizable, boolean modal,
                                      ControllerConsumer<T> consumer) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            if (owner != null && modal) {
                stage.initOwner(owner);
                stage.initModality(Modality.WINDOW_MODAL);
            }

            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/row_color.css");
            stage.setScene(scene);
            stage.setTitle(title);
            stage.getIcons().add(new Image("/Images/NV.png"));
            stage.setResizable(resizable);
            stage.centerOnScreen();

            applyStageMinSize(stage, fxmlPath);

            // Configure controller if needed
            if (consumer != null) {
                consumer.prepare(loader.getController());
            }

            if (modal) {
                stage.showAndWait();
            } else {
                stage.show();
            }

        } catch (IOException e) {
            LOGGER.error("Failed to load FXML: {}", fxmlPath, e);
        }
    }

    /**
     * Replace the scene on an existing stage (like SignUp navigation).
     */
    public static void replaceScene(Stage stage, String fxmlPath, String title, boolean resizable) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/row_color.css");

            stage.setScene(scene);
            stage.setTitle(title);
            stage.getIcons().add(new Image("/Images/NV.png"));
            stage.setResizable(resizable);

            applyStageMinSize(stage, fxmlPath);

            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            LOGGER.error("Failed to load FXML: {}", fxmlPath, e);
        }
    }
}
