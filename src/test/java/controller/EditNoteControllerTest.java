package controller;

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
import util.UndoRedoManager;


import java.lang.reflect.Field;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EditNoteControllerTest {

    private EditNoteController controller;
    private NoteEntity note;
    private RichTextEditorController editorMock;
    private NoteService noteServiceMock;

    // Initialize JavaFX
    @BeforeAll
    static void initJavaFX() {
        JavaFXInitializer.init();
    }

    @BeforeEach
    void setUp() {
        controller = new EditNoteController();
        ITagDAO tagDao = mock(ITagDAO.class);
        note = new NoteEntity();

        // Inject dependencies
        setField("tagDao", tagDao);

        // Inject UI components
        setField("titleField", new TextField());
        setField("annotationBox", new TextField());
        setField("updateButton", new Button());
        setField("tagFlowpane", new FlowPane());
        setField("tagComboBox", new ComboBox<String>());
        setField("statusLabel", new Label());

        // Mock editor
        editorMock = mock(RichTextEditorController.class);
        InlineCssTextArea textAreaMock = mock(InlineCssTextArea.class);

        when(editorMock.getTextArea()).thenReturn(textAreaMock);
        when(editorMock.getText()).thenReturn("test content");

        setField("contentEditorController", editorMock);

        // Mock TranslationService
        TranslationService translationServiceMock = mock(TranslationService.class);
        setField("translationService", translationServiceMock);

        when(translationServiceMock.getTranslation(any(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    NoteEntity n = invocation.getArgument(0);
                    String lang = invocation.getArgument(1);
                    return n.getTranslations().get(lang);
                });

        setField("translationService", translationServiceMock);

        // Mock NoteService
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

            var translations = n.getTranslations();

            var t = translations.get(lang);
            if (t == null) {
                t = n.createTranslation(lang);
                translations.put(lang, t);
            }

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

    // ---------- Reflection helpers ----------
    private void setField(String name, Object value) {
        try {
            Field field = EditNoteController.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getField(String name) {
        try {
            Field field = EditNoteController.class.getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------- setNote ----------
    @Test
    void setNote_shouldPopulateFields() {
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
    void handleUpdate_shouldSaveNote() {
        TextField titleField = getField("titleField");
        TextField annotationBox = getField("annotationBox");

        String lang = util.Localization.getCurrentLanguageCode();
        note.createTranslation(lang);

        when(editorMock.getSerializedContent()).thenReturn("New Content");

        titleField.setText("New Title");
        annotationBox.setText("New Annotation");

        assertDoesNotThrow(() -> controller.handleUpdate());

        var translation = note.getTranslations().get(lang);

        assertEquals("New Title", translation.getTitle());
        assertEquals("New Content", translation.getContent());
        assertEquals("New Annotation", translation.getAnnotation());

        verify(noteServiceMock).updateNote(
                any(), anyString(), anyString(), anyString(), anyString(), anySet()
        );
    }


    @Test
    void handleUpdate_shouldCallService() {
        assertDoesNotThrow(() -> controller.handleUpdate());

        verify(noteServiceMock).updateNote(
                any(), anyString(), anyString(), anyString(), anyString(), anySet()
        );
    }

    @Test
    void handleUpdate_shouldPassTagsToService() {
        controller.selectedTags.add("work");

        assertDoesNotThrow(() -> controller.handleUpdate());

        verify(noteServiceMock).updateNote(
                any(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                argThat(set -> set.contains("work"))
        );
    }

    // ---------- handleAddTag ----------
    @Test
    void handleAddTag_shouldAddTag() {
        ComboBox<String> combo = getField("tagComboBox");
        combo.getEditor().setText("work");

        controller.handleAddTag();

        assertTrue(controller.selectedTags.contains("work"));
    }

    @Test
    void handleAddTag_shouldNotDuplicate() {
        controller.selectedTags.add("work");

        ComboBox<String> combo = getField("tagComboBox");
        combo.getEditor().setText("work");

        controller.handleAddTag();

        assertEquals(1, controller.selectedTags.size());
    }
    @Test
    void setNote_shouldClearFields_whenTranslationMissing() {
        reset(editorMock);

        controller.setNote(note);

        assertEquals("", ((TextField) getField("titleField")).getText());
        assertEquals("", ((TextField) getField("annotationBox")).getText());

        verify(editorMock, atLeastOnce()).setText("");
    }
    @Test
    void handleUpdate_shouldHandleNullValues() {
        TextField titleField = getField("titleField");
        TextField annotationBox = getField("annotationBox");

        when(editorMock.getSerializedContent()).thenReturn(null);

        titleField.setText("Title");
        annotationBox.setText(null);

        assertDoesNotThrow(() -> controller.handleUpdate());

        verify(noteServiceMock).updateNote(
                any(),
                anyString(),
                eq("Title"),
                eq(""),   // safe(null)
                eq(""),   // safe(null)
                anySet()
        );
    }
    private void invokePrivate(String methodName) {
        try {
            var method = EditNoteController.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void handleCancel_shouldNotThrow() {
        assertDoesNotThrow(() -> invokePrivate("handleCancel"));
    }
    @Test
    void loadTags_shouldPopulateComboBox() {
        ITagDAO tagDao = mock(ITagDAO.class);

        TagEntity t1 = new TagEntity();
        t1.setTagName("work");

        TagEntity t2 = new TagEntity();
        t2.setTagName("home");

        when(tagDao.findAll()).thenReturn(java.util.List.of(t1, t2));

        setField("tagDao", tagDao);

        controller.setTagDao(tagDao);

        ComboBox<String> combo = getField("tagComboBox");

        assertTrue(combo.getItems().contains("work"));
        assertTrue(combo.getItems().contains("home"));
    }
    @Test
    void handleAddTag_shouldIgnoreEmptyTag() {
        ComboBox<String> combo = getField("tagComboBox");
        combo.getEditor().setText("");

        controller.handleAddTag();

        assertTrue(controller.selectedTags.isEmpty());
    }
    @Test
    void setNote_shouldHandleNullTags() {
        assertDoesNotThrow(() -> controller.setNote(note));
        assertTrue(controller.selectedTags.isEmpty());
    }
    @Test
    void handleUpdate_shouldHandleEmptyStrings() {
        TextField titleField = getField("titleField");

        when(editorMock.getSerializedContent()).thenReturn("");

        titleField.setText("");

        assertDoesNotThrow(() -> controller.handleUpdate());
    }
    @Test
    void handleUndo_shouldCallManager() {
        UndoRedoManager spyManager = spy(new UndoRedoManager());
        setField("undoRedoManager", spyManager);

        invokePrivate("handleUndo");

        verify(spyManager).undo();
    }

    @Test
    void handleRedo_shouldCallManager() {
        UndoRedoManager spyManager = spy(new UndoRedoManager());
        setField("undoRedoManager", spyManager);

        invokePrivate("handleRedo");

        verify(spyManager).redo();
    }

}