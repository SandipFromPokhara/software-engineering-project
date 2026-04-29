package util;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

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
        if (stage == null) {
            throw new IllegalArgumentException("stage must not be null. Provide a valid Stage or use openWindow(...) to open a new window.");
        }
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
        URL fxmlUrl = resolveFxmlUrl(fxmlPath);
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        root.nodeOrientationProperty().bind(
                Bindings.when(Localization.isRTLProperty())
                        .then(NodeOrientation.RIGHT_TO_LEFT)
                        .otherwise(NodeOrientation.LEFT_TO_RIGHT)
        );

        Scene scene = new Scene(root);

        if (!scene.getRoot().getStyleClass().contains("root")) {
            scene.getRoot().getStyleClass().add("root");
        }

        scene.getStylesheets().add(css("/css/theme.css"));
        scene.getStylesheets().add(css("/css/row_color.css"));

        ToggleUtil.applyTheme(scene);

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

    private static String css(String path) {
        return Objects.requireNonNull(
                NavigationUtil.class.getResource(path),
                "Missing CSS file: " + path
        ).toExternalForm();
    }

    /*
     * Resolve the FXML URL using the following order:
     * 1. Classpath resource (NavigationUtil.class.getResource or classloader)
     * 2. Developer workspace file (src/main/resources/...) if it exists and is preferred
     * The dev file is preferred when the classpath resource is missing, when the
     * classpath resource is packaged in a JAR, or when the workspace file is newer.
     */
    private static URL resolveFxmlUrl(String fxmlPath) throws IOException {
        String normalized = fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath;

        // 1) Try classpath
        URL classpathUrl = NavigationUtil.class.getResource(fxmlPath);
        if (classpathUrl == null) {
            classpathUrl = NavigationUtil.class.getClassLoader().getResource(normalized);
        }

        // 2) Prepare dev workspace path
        Path devPath = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", normalized.replace('/', java.io.File.separatorChar));

        // If dev file doesn't exist, return whatever classpath gave us (may be null)
        if (!Files.exists(devPath)) {
            if (classpathUrl != null) return classpathUrl;
            throw new IOException("FXML resource not found on classpath: " + fxmlPath + " - ensure it exists under src/main/resources and is packaged.");
        }

        // If classpath is missing, prefer dev file
        if (classpathUrl == null) {
            URL devUrl = devPath.toUri().toURL();
            LOGGER.warn("Loading FXML from filesystem during development: {}", devPath);
            return devUrl;
        }

        // If classpath resource is not a plain file URL (e.g. inside a JAR), prefer dev file
        String proto = classpathUrl.getProtocol();
        if (!"file".equalsIgnoreCase(proto)) {
            LOGGER.warn("Loading FXML from filesystem during development (classpath is jar): {}", devPath);
            return devPath.toUri().toURL();
        }

        // Both are files on disk: prefer the newer (dev file wins if newer)
        try {
            Path classpathPath = Paths.get(classpathUrl.toURI());
            if (!Files.exists(classpathPath)) {
                LOGGER.warn("Loading FXML from filesystem during development (classpath missing file): {}", devPath);
                return devPath.toUri().toURL();
            }

            long devMillis = Files.getLastModifiedTime(devPath).toMillis();
            long cpMillis = Files.getLastModifiedTime(classpathPath).toMillis();
            if (devMillis > cpMillis) {
                URL devUrl = devPath.toUri().toURL();
                LOGGER.warn("Loading FXML from filesystem during development (dev is newer): {}", devPath);
                return devUrl;
            }
        } catch (Exception e) {
            // If anything goes wrong comparing timestamps, fall back to dev to make iteration easier
            LOGGER.debug("Error comparing dev/classpath FXML timestamps, preferring dev: {}", e.toString());
            return devPath.toUri().toURL();
        }

        // Default to classpath resource
        return classpathUrl;
    }
}
