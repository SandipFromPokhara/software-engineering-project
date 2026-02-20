package controller;

import dao.note.NoteDAO;
import dao.tag.TagDAO;
import entity.NoteEntity;
import entity.TagEntity;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testutil.JavaFXInitializer;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EditNoteControllerTest {

    private EditNoteController controller;
    private NoteDAO mockNoteDao;
    private TagDAO mockTagDao;
    private NoteEntity note;

    @BeforeAll
    static void initJavaFX() {
        JavaFXInitializer.init();
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new EditNoteController();

        mockNoteDao = mock(NoteDAO.class);
        mockTagDao = mock(TagDAO.class);

        note = new NoteEntity();

        // Inject UI components
        controller.titleField = new TextField();
        controller.contentBox = new TextArea();
        controller.annotationBox = new TextField();
        controller.updateButton = new Button();
        controller.tagFlowpane = new FlowPane();
        controller.tagComboBox = new ComboBox<>();
        controller.tagComboBox.setEditable(true);
        controller.statusLabel = new Label();

        // Inject DAOs via reflection
        Field noteDaoField = EditNoteController.class.getDeclaredField("noteDao");
        noteDaoField.setAccessible(true);
        noteDaoField.set(controller, mockNoteDao);

        Field tagDaoField = EditNoteController.class.getDeclaredField("tagDao");
        tagDaoField.setAccessible(true);
        tagDaoField.set(controller, mockTagDao);

        controller.setNote(note);
    }

    // setNote Tests
    @Test
    void setNote_shouldFillTextFields() {
        note.setTitle("Title");
        note.setContent("Content");
        note.setAnnotation("Annotation");

        controller.setNote(note);

        assertEquals("Title", controller.titleField.getText());
        assertEquals("Content", controller.contentBox.getText());
        assertEquals("Annotation", controller.annotationBox.getText());
    }

    @Test
    void setNote_shouldLoadExistingTags() {
        TagEntity tag = new TagEntity("work");
        note.addTag(tag);

        controller.setNote(note);

        assertTrue(controller.selectedTags.contains("work"));
    }


    // handleUpdate Tests
    @Test
    void handleUpdate_shouldUpdateNoteAndSave() {
        controller.titleField.setText("New Title");
        controller.contentBox.setText("New Content");
        controller.annotationBox.setText("New Annotation");

        try {
            controller.handleUpdate();
        } catch (Exception ignored) {}

        assertEquals("New Title", note.getTitle());
        assertEquals("New Content", note.getContent());
        assertEquals("New Annotation", note.getAnnotation());

        verify(mockNoteDao).save(note);
    }

    @Test
    void handleUpdate_shouldNotSave_ifTagDaoIsNull() throws Exception {
        Field tagDaoField = EditNoteController.class.getDeclaredField("tagDao");
        tagDaoField.setAccessible(true);
        tagDaoField.set(controller, null);

        controller.handleUpdate();

        verify(mockNoteDao, never()).save(any());
    }

    @Test
    void handleUpdate_shouldAddExistingTag() {
        TagEntity tag = new TagEntity("work");

        when(mockTagDao.findByName("work")).thenReturn(tag);

        controller.selectedTags.add("work");

        try {
            controller.handleUpdate();
        } catch (Exception ignored) {}

        assertTrue(note.getTags().contains(tag));
    }

    @Test
    void handleUpdate_shouldCreateNewTag_ifNotExists() {
        when(mockTagDao.findByName("newtag")).thenReturn(null);
        when(mockTagDao.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        controller.selectedTags.add("newtag");

        try {
            controller.handleUpdate();
        } catch (Exception ignored) {}

        verify(mockTagDao).save(any(TagEntity.class));
    }


    // handleAddTag Tests
    @Test
    void handleAddTag_shouldAddValidTag() {
        controller.tagComboBox.getEditor().setText("work");

        controller.handleAddTag();

        assertTrue(controller.selectedTags.contains("work"));
    }

    @Test
    void handleAddTag_shouldRejectLongTag() {
        controller.tagComboBox.getEditor().setText("averyveryverylongtagname");
        controller.handleAddTag();
        assertFalse(controller.selectedTags.contains("averyveryverylongtagname"));
    }

    @Test
    void handleAddTag_shouldRejectInvalidCharacters() {
        controller.tagComboBox.getEditor().setText("bad@tag!");
        controller.handleAddTag();
        assertFalse(controller.selectedTags.contains("bad@tag!"));
    }

    @Test
    void handleAddTag_shouldNotAddDuplicateTag() {
        controller.selectedTags.add("work");
        controller.tagComboBox.getEditor().setText("work");
        controller.handleAddTag();
        assertEquals(1, controller.selectedTags.size());
    }
}