package util;

import javafx.event.ActionEvent;
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

    // Fixed size pages (entry, login, signup)
    private static final String[] FIXED_SIZE_PAGES = {
        "entry.fxml", "login_view.fxml", "signup.fxml"
    };

    public static void navigateTo(ActionEvent event, String fxmlPath, String title) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        navigateTo(stage, fxmlPath, title);
    }

    public static void navigateTo(Stage stage, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(NavigationUtil.class.getResource(fxmlPath));

            stage.setTitle(title);

            // Check if this is a fixed size page
            boolean isFixedSize = isFixedSizePage(fxmlPath);

            if (isFixedSize) {
                // Fixed size for entry, login, signup (600x400)
                stage.setScene(new Scene(root, 600, 400));
                stage.setResizable(false);
                stage.setMinWidth(600);
                stage.setMinHeight(400);
                stage.setMaxWidth(600);
                stage.setMaxHeight(400);
            } else {
                // Expandable pages - use size from FXML (Scene Builder)
                stage.setScene(new Scene(root));
                stage.setResizable(true);
                stage.setMinWidth(600);
                stage.setMinHeight(400);
                stage.setMaxWidth(Double.MAX_VALUE);
                stage.setMaxHeight(Double.MAX_VALUE);
                stage.sizeToScene(); // Use FXML-defined size
            }

            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            logger.error("Failed to navigate to " + fxmlPath);
            e.printStackTrace();
        }
    }

    private static boolean isFixedSizePage(String fxmlPath) {
        for (String fixedPage : FIXED_SIZE_PAGES) {
            if (fxmlPath.contains(fixedPage)) {
                return true;
            }
        }
        return false;
    }
}
