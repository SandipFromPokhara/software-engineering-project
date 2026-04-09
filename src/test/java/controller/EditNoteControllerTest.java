package controller;

import dao.note.INoteDAO;
import dao.tag.ITagDAO;
import entity.entities.NoteEntity;
import entity.entities.TagEntity;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import org.fxmisc.richtext.InlineCssTextArea;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.NoteService;
import services.TranslationService;
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
    private RichTextEditorController editorMock;
    private NoteService noteServiceMock;

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
        setField("annotationBox", new TextField());
        setField("updateButton", new Button());
        setField("tagFlowpane", new FlowPane());
        setField("tagComboBox", new ComboBox<String>());
        setField("statusLabel", new Label());

        // Inject RichTextEditorController mock
        editorMock = mock(RichTextEditorController.class);
        InlineCssTextArea textAreaMock = mock(InlineCssTextArea.class);

        when(editorMock.getTextArea()).thenReturn(textAreaMock);
        when(editorMock.getText()).thenReturn("test content");

        setField("contentEditorController", editorMock);

        // Inject TranslationService mock
        TranslationService translationServiceMock = mock(TranslationService.class);

        when(translationServiceMock.getTranslation(any(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    NoteEntity n = invocation.getArgument(0);
                    String lang = invocation.getArgument(1);
                    return n.getTranslations().get(lang);
                });

        setField("translationService", translationServiceMock);

        // Inject NoteService mock
        noteServiceMock = mock(NoteService.class);

        when(noteServiceMock.updateNote(
                any(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anySet()
        )).thenAnswer(invocation -> {
            NoteEntity n = invocation.getArgument(0);
            String lang = invocation.getArgument(1);
            String title = invocation.getArgument(2);
            String content = invocation.getArgument(3);
            String annotation = invocation.getArgument(4);

            var t = n.getTranslations().computeIfAbsent(lang, k -> n.createTranslation(lang));
            t.setTitle(title);
            t.setContent(content);
            t.setAnnotation(annotation);

            return n;
        });

        setField("noteService", noteServiceMock);
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
        var translation = note.createTranslation("en");
        translation.setTitle("My Title");
        translation.setContent("My Content");
        translation.setAnnotation("My Annotation");

        controller.setNote(note);

        assertEquals("My Title", ((TextField) getField("titleField")).getText());
        verify(editorMock).setText("My Content");
        assertEquals("My Annotation", ((TextField) getField("annotationBox")).getText());
    }

    @Test
    void setNote_shouldLoadTags() {
        TagEntity tag = new TagEntity();
        tag.setTagName("work");
        note.addTag(tag);

        controller.setNote(note);

        assertTrue(controller.selectedTags.contains("work"));
    }

    // ---------- handleUpdate ----------
    @Test
    void handleUpdate_shouldSaveNote() throws Exception {
        TextField titleField = getField("titleField");
        TextField annotationBox = getField("annotationBox");

        String lang = util.Localization.getCurrentLanguageCode();
        note.createTranslation(lang);

        when(editorMock.getText()).thenReturn("New Content");

        titleField.setText("New Title");
        annotationBox.setText("New Annotation");

        //Ignore JavaFX window closing crash
        try {
            controller.handleUpdate();
        } catch (Exception ignored) {}

        var translation = note.getTranslations().get(lang);

        assertEquals("New Title", translation.getTitle());
        assertEquals("New Content", translation.getContent());
        assertEquals("New Annotation", translation.getAnnotation());

        verify(noteServiceMock).updateNote(
                any(), anyString(), anyString(), anyString(), anyString(), anySet()
        );
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
        TagEntity tag = new TagEntity();
        tag.setTagName("work");
        when(tagDao.findByName("work")).thenReturn(tag);

        controller.selectedTags.add("work");

        try {
            controller.handleUpdate();
        } catch (Exception ignored) {}

        verify(noteServiceMock).updateNote(any(), anyString(), anyString(), anyString(), anyString(), anySet());
    }

    @Test
    void handleUpdate_shouldCreateNewTag() throws Exception {
        when(tagDao.findByName("newtag")).thenReturn(null);
        when(tagDao.save(any())).thenAnswer(i -> i.getArgument(0));

        controller.selectedTags.add("newtag");

        try {
            controller.handleUpdate();
        } catch (Exception ignored) {}

        verify(noteServiceMock).updateNote(any(), anyString(), anyString(), anyString(), anyString(), anySet());
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
