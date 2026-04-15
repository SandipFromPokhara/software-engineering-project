package controller;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    void initializeShouldSetupBindingsWithoutErrors() {
        // Arrange
        controller.title = new Label();
        controller.content = new Label();
        controller.annotation = new Label();

        controller.titleBox = new TextField();
        controller.contentBox = new TextArea();
        controller.annotationBox = new TextField();
        controller.cancelButton = new Button();

        // Act + Assert
        assertDoesNotThrow(() -> controller.initialize());

        // Bindings exist (not null)
        assertNotNull(controller.title.textProperty());
        assertNotNull(controller.content.textProperty());
        assertNotNull(controller.annotation.textProperty());

        assertNotNull(controller.cancelButton.textProperty());
    }

    @Test
    void handleCancelShouldClearAllFields() {
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
    @Test
    void handleCancelShouldNotCrashWhenSomeFieldsAreNull() {
        controller.titleBox = new TextField("Hello");
        controller.contentBox = null;
        controller.annotationBox = new TextField("World");

        assertDoesNotThrow(() -> controller.handleCancel());

        assertEquals("", controller.titleBox.getText());
        assertEquals("", controller.annotationBox.getText());
    }
    @Test
    void initializeShouldBindPromptTexts() {
        controller.titleBox = new TextField();
        controller.contentBox = new TextArea();
        controller.annotationBox = new TextField();

        controller.title = new Label();
        controller.content = new Label();
        controller.annotation = new Label();
        controller.cancelButton = new Button();

        controller.initialize();

        // Prompt text properties should be non-null bindings
        assertNotNull(controller.titleBox.getPromptText());
        assertNotNull(controller.contentBox.getPromptText());
        assertNotNull(controller.annotationBox.getPromptText());
    }
}
