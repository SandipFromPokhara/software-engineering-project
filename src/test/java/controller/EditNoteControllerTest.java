package controller;


import dao.note.NoteDAO;
import entity.NoteEntity;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class EditNoteControllerTest {

    private static boolean javafxStarted = false;

    private EditNoteController controller;
    private NoteDAO mockDao;
    private NoteEntity note;

    @BeforeAll
    static void initJavaFX() {
        if (!javafxStarted) {
            Platform.startup(() -> {});
            javafxStarted = true;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new EditNoteController();
        mockDao = Mockito.mock(NoteDAO.class);
        note = new NoteEntity();

        // Inject JavaFX controls
        controller.titleBox = new TextField();
        controller.contentBox = new TextArea();
        controller.annotationBox = new TextField();
        controller.updateButton = new Button();

        // Inject mock DAO using reflection
        Field daoField = EditNoteController.class.getDeclaredField("noteDao");
        daoField.setAccessible(true);
        daoField.set(controller, mockDao);

        controller.setNote(note);
    }

    @Test
    void setNote_shouldFillTextFields() {
        note.setTitle("Title");
        note.setContent("Content");
        note.setAnnotation("Annotation");

        controller.setNote(note);

        assertEquals("Title", controller.titleBox.getText());
        assertEquals("Content", controller.contentBox.getText());
        assertEquals("Annotation", controller.annotationBox.getText());
    }

    @Test
    void handleUpdate_shouldUpdateNoteAndSave() {
        controller.titleBox.setText("New Title");
        controller.contentBox.setText("New Content");
        controller.annotationBox.setText("New Annotation");

        // Prevent close() crash by skipping scene access
        // (no Stage exists in unit tests)
        try {
            controller.handleUpdate();
        } catch (NullPointerException ignored) {
            // expected due to Stage being null
        }

        assertEquals("New Title", note.getTitle());
        assertEquals("New Content", note.getContent());
        assertEquals("New Annotation", note.getAnnotation());

        verify(mockDao).save(note);
    }
}
