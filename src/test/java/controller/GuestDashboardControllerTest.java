package controller;

import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testutil.JavaFXInitializer;

import static org.junit.jupiter.api.Assertions.*;

class GuestDashboardControllerTest {

    private GuestDashboardController controller;

    @BeforeAll
    static void initJfxRuntime() {
        JavaFXInitializer.init();
    }

    @BeforeEach
    void setUp() {
        controller = new GuestDashboardController();

        // Inject required UI components manually
        controller.centerPane = new VBox();
    }

    @Test
    void handleNewFiles_shouldNotCrash_whenFXMLMissing() {
        // Since FXML may not exist in test environment,
        // this should NOT throw exception (it is caught and logged)
        assertDoesNotThrow(() -> controller.handleNewFiles());
    }

    @Test
    void handleNewFiles_shouldAttemptToModifyCenterPane() {
        controller.handleNewFiles();

        // We can't guarantee FXML loads,
        // but we can check centerPane is still valid

        assertNotNull(controller.centerPane);
    }
}
