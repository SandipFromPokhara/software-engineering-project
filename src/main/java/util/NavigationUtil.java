package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
     * @param titleKey    Window title
     * @param resizable   Whether window is resizable
     * @param modal       If true, window is modal
     * @param consumer    Optional callback to configure the controller
     */
    public static <T> void openWindow(Stage owner, String fxmlPath, String titleKey,
                                      boolean resizable, boolean modal,
                                      ControllerConsumer<T> consumer) {
        try {
            FxmlLoadResult<T> result = buildScene(fxmlPath);

            Stage stage = new Stage();
            if (owner != null && modal) {
                stage.initOwner(owner);
                stage.initModality(Modality.WINDOW_MODAL);
            }

            stage.setScene(result.scene);
            stage.getIcons().setAll(new Image("/Images/NV.png"));
            stage.setResizable(resizable);

            stage.titleProperty().unbind();
            stage.titleProperty().bind(Localization.bind(titleKey));

            applyMinSize(stage, fxmlPath);

            stage.centerOnScreen();

            // Configure controller if needed
            if (consumer != null) {
                consumer.prepare(result.controller);
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
     * Replace the scene on an existing stage
     */
    public static void replaceScene(Stage stage, String fxmlPath, String titleKey, boolean resizable) {
        try {
            FxmlLoadResult<?> result = buildScene(fxmlPath);

            stage.setScene(result.scene);

            stage.getIcons().add(new Image("/Images/NV.png"));
            stage.setResizable(resizable);

            stage.titleProperty().unbind();
            stage.titleProperty().bind(Localization.bind(titleKey));

            // Set minimum size for key windows
            applyMinSize(stage, fxmlPath);

            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            LOGGER.error("Failed to load FXML: {}", fxmlPath, e);
        }
    }

    private static class FxmlLoadResult<T> {
        Scene scene;
        T controller;

        FxmlLoadResult(Scene scene, T controller) {
            this.scene = scene;
            this.controller = controller;
        }
    }

    private static <T> FxmlLoadResult<T> buildScene(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        if (!scene.getRoot().getStyleClass().contains("root")) {
            scene.getRoot().getStyleClass().add("root");
        }

        ToggleUtil.applyTheme(scene);
        scene.getStylesheets().add("/css/row_color.css");

        return new FxmlLoadResult<>(scene, loader.getController());
    }

    private static void applyMinSize(Stage stage, String fxmlPath) {
        // Key windows that should allow maximizing
        boolean isKeyWindow = DASHBOARD_FXML.equals(fxmlPath) || CREATE_FXML.equals(fxmlPath) || EDIT_FXML.equals(fxmlPath);

        if (isKeyWindow) {
            stage.setMinWidth(700);
            stage.setMinHeight(550);
        } else {
            stage.setMinWidth(600);
            stage.setMinHeight(400);
        }
    }
    public static void setCenter(VBox centerPane, String fxmlPath) {
        try {
            FxmlLoadResult<?> result = buildScene(fxmlPath);

            Parent view = result.scene.getRoot();

            StackPane wrapper = new StackPane(view);
            VBox.setVgrow(wrapper, Priority.ALWAYS);

            wrapper.prefWidthProperty().bind(centerPane.widthProperty());
            wrapper.prefHeightProperty().bind(centerPane.heightProperty());

            centerPane.getChildren().setAll(wrapper);

        } catch (IOException e) {
            LOGGER.error("Failed to load center FXML: {}", fxmlPath, e);
        }
    }
}
