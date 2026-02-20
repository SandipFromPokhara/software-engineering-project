package controller;

import static org.junit.jupiter.api.Assertions.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testutil.JavaFXInitializer;


class CreateFilesGuestControllerTest {

    private CreateFilesGuestController controller;

    @BeforeAll
    static void initJfxRuntime() {
        JavaFXInitializer.init();
    }

    @BeforeEach
    void setUp() {
        controller = new CreateFilesGuestController();

        // Manually inject UI components
        controller.titleBox = new TextField();
        controller.contentBox = new TextArea();
        controller.annotationBox = new TextField();
    }

    @Test
    void handleCancel_shouldClearAllFields() {
        // Arrange
        controller.titleBox.setText("Test Title");
        controller.contentBox.setText("Test Content");
        controller.annotationBox.setText("Test Annotation");

        // Act
        controller.handleCancel();

        // Assert
        assertEquals("", controller.titleBox.getText());
        assertEquals("", controller.contentBox.getText());
        assertEquals("", controller.annotationBox.getText());
    }
}
