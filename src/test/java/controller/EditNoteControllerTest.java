package controller;

import dao.note.INoteDAO;
import dao.tag.ITagDAO;
import entity.entities.NoteEntity;
import entity.entities.TagEntity;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testutil.JavaFXInitializer;

import java.lang.reflect.Field;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EditNoteControllerTest {

    private EditNoteController controller;
    private INoteDAO noteDao;
    private ITagDAO tagDao;
    private NoteEntity note;

    // Initialize JavaFX
    @BeforeAll
    static void initJavaFX() {
        JavaFXInitializer.init();
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new EditNoteController();

        noteDao = mock(INoteDAO.class);
        tagDao = mock(ITagDAO.class);
        note = new NoteEntity();

        // Inject DAOs
        setField("noteDao", noteDao);
        setField("tagDao", tagDao);

        // Inject UI components
        setField("titleField", new TextField());
        setField("contentBox", new TextArea());
        setField("annotationBox", new TextField());
        setField("updateButton", new Button());
        setField("tagFlowpane", new FlowPane());
        setField("tagComboBox", new ComboBox<String>());
        setField("statusLabel", new Label());

        ComboBox<String> combo = getField("tagComboBox");
        combo.setEditable(true);

        controller.selectedTags = new HashSet<>();

        controller.setNote(note);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = EditNoteController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(controller, value);
    }

    @SuppressWarnings("unchecked")
    private <T> T getField(String name) throws Exception {
        Field field = EditNoteController.class.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(controller);
    }

    // ---------- setNote ----------
    @Test
    void setNote_shouldPopulateFields() throws Exception {
        note.setTitle("My Title");
        note.setContent("My Content");
        note.setAnnotation("My Annotation");

        controller.setNote(note);

        assertEquals("My Title", ((TextField) getField("titleField")).getText());
        assertEquals("My Content", ((TextArea) getField("contentBox")).getText());
        assertEquals("My Annotation", ((TextField) getField("annotationBox")).getText());
    }

    @Test
    void setNote_shouldLoadTags() {
        TagEntity tag = new TagEntity("work");
        note.addTag(tag);

        controller.setNote(note);

        assertTrue(controller.selectedTags.contains("work"));
    }

    // ---------- handleUpdate ----------
    @Test
    void handleUpdate_shouldSaveNote() throws Exception {
        TextField titleField = getField("titleField");
        TextArea contentBox = getField("contentBox");
        TextField annotationBox = getField("annotationBox");

        titleField.setText("New Title");
        contentBox.setText("New Content");
        annotationBox.setText("New Annotation");

        //Ignore JavaFX window closing crash
        try {
            controller.handleUpdate();
        } catch (Exception ignored) {}

        assertEquals("New Title", note.getTitle());
        assertEquals("New Content", note.getContent());
        assertEquals("New Annotation", note.getAnnotation());

        verify(noteDao).save(note);
    }

    @Test
    void handleUpdate_shouldNotSave_whenDaoMissing() throws Exception {
        setField("tagDao", null);

        try {
            controller.handleUpdate();
        } catch (Exception ignored) {}

        verify(noteDao, never()).save(any());
    }

    @Test
    void handleUpdate_shouldAddExistingTag() throws Exception {
        TagEntity tag = new TagEntity("work");
        when(tagDao.findByName("work")).thenReturn(tag);

        controller.selectedTags.add("work");

        try {
            controller.handleUpdate();
        } catch (Exception ignored) {}

        assertTrue(note.getTags().contains(tag));
    }

    @Test
    void handleUpdate_shouldCreateNewTag() throws Exception {
        when(tagDao.findByName("newtag")).thenReturn(null);
        when(tagDao.save(any())).thenAnswer(i -> i.getArgument(0));

        controller.selectedTags.add("newtag");

        try {
            controller.handleUpdate();
        } catch (Exception ignored) {}

        verify(tagDao).save(any(TagEntity.class));
    }

    // ---------- handleAddTag ----------
    @Test
    void handleAddTag_shouldAddTag() throws Exception {
        ComboBox<String> combo = getField("tagComboBox");
        combo.getEditor().setText("work");

        controller.handleAddTag();

        assertTrue(controller.selectedTags.contains("work"));
    }

    @Test
    void handleAddTag_shouldNotDuplicate() throws Exception {
        controller.selectedTags.add("work");

        ComboBox<String> combo = getField("tagComboBox");
        combo.getEditor().setText("work");

        controller.handleAddTag();

        assertEquals(1, controller.selectedTags.size());
    }
}