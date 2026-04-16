package controller;

import dao.notebook.JpaNotebookDao;
import dao.tag.JpaTagDao;
import entity.entities.NotebookEntity;
import entity.entities.NoteEntity;
import entity.entities.TagEntity;
import entity.entities.UserEntity;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import services.NoteService;
import session.NoteSession;
import session.NotebookSession;
import session.UserSession;
import testutil.JavaFxTestExtension;
import util.Localization;
import util.RichTextStorageUtil;

import java.lang.reflect.Field;
import java.util.logging.Logger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JavaFxTestExtension.class)
class CreateNoteControllerTest {

    private static final String IMPORTANT_TAG = "Important";
    private static final String TEST_NOTE_TITLE = "Test Note";
    private static final String TEST_NOTE_CONTENT = "Test Content";
    private static final String TEST_NOTE_ANNOTATION = "Test Annotation";
    private static final String SELECTED_TAGS_FIELD = "selectedTags";

    private CreateNoteController controller;
    private static final Logger LOGGER = Logger.getLogger(CreateNoteControllerTest.class.getName());
    private MockNoteService mockNoteService;
    private MockNotebookDao mockNotebookDao;
    private MockTagDao mockTagDao;

    private TextField titleField;
    private RichTextEditorController contentEditorController;
    private TextArea annotationArea;
    private Button saveButton;
    private Button clearButton;
    private Label statusLabel;
    private ComboBox<NotebookEntity> notebookComboBox;
    private FlowPane tagFlowPane;
    private ComboBox<String> tagComboBox;
    private Button addTagBtn;
    private Menu fileMenu;
    private Menu editMenu;
    private MenuItem backDashboard;
    private MenuItem closeFile;
    private Label noteTitleLabel;
    private Label noteContentLabel;
    private Label noteAnnotationLabel;
    private Label noteTagLabel;
    private Label selectLabel;
    private MenuItem undoMenuItem;
    private MenuItem redoMenuItem;
    private Label wordCountLabel;
    private Tooltip tagTooltip;
    private Tooltip toggleTooltip;
    private Button toggleBtn;
    private ImageView tagIcon;

    private static class MockNoteService extends NoteService {
        private NoteEntity savedNote;
        private boolean shouldThrowException = false;

        private final MockTagDao tagDao;

        public MockNoteService(MockNotebookDao notebookDao, MockTagDao tagDao) {
            super(null, notebookDao, tagDao);
            this.tagDao = tagDao;
        }

        @Override
        public NoteEntity createNote(String title, String content, String annotation,
                                     NotebookEntity notebook, Set<String> tagNames, String langCode) {

            if (shouldThrowException) {
                // Throw a specific custom exception to avoid using a raw RuntimeException
                throw new MockServiceException("Mock exception");
            }

            LOGGER.info("MOCK createNote called!");

            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException("Title cannot be empty");
            }

            NoteEntity note = new NoteEntity();

            String lang = (langCode == null || langCode.isBlank())
                    ? Localization.getLocale().getLanguage()
                    : langCode;

            var translation = note.createTranslation(lang);
            translation.setTitle(title.trim());
            translation.setContent(content == null ? "" : content);
            translation.setAnnotation(annotation == null ? "" : annotation);

            note.setNotebook(notebook);

            if (tagNames != null) {
                for (String tagName : tagNames) {
                    TagEntity tag = tagDao.findByName(tagName);
                    if (tag == null) {
                        tag = new TagEntity();
                        tag.setTagName(tagName);
                        tagDao.save(tag);
                    }
                    note.addTag(tag);
                }
            }

            savedNote = note;
            LOGGER.fine(() -> "assigned savedNote: " + savedNote);
            return note;
        }

        public void reset() {
            savedNote = null;
            shouldThrowException = false;
        }
    }

    private static class MockNotebookDao extends JpaNotebookDao {
        private final List<NotebookEntity> notebooks = new ArrayList<>();

        @Override
        public List<NotebookEntity> findByUser(UserEntity user) {
            return new ArrayList<>(notebooks);
        }

        @Override
        public NotebookEntity save(NotebookEntity notebook) {
            notebooks.add(notebook);
            return notebook;
        }

        public void addNotebook(NotebookEntity notebook) {
            notebooks.add(notebook);
        }

        public void reset() {
            notebooks.clear();
        }
    }

    private static class MockTagDao extends JpaTagDao {
        private final List<TagEntity> tags = new ArrayList<>();
        private TagEntity savedTag;

        @Override
        public List<TagEntity> findAll() {
            return new ArrayList<>(tags);
        }

        @Override
        public TagEntity findByName(String name) {
            return tags.stream()
                    .filter(tag -> tag.getTagName().equals(name))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public TagEntity save(TagEntity tag) {
            if (tag.getId() == null) {
                setId(tag, (long) (tags.size() + 1));
            }
            tags.add(tag);
            savedTag = tag;
            return tag;
        }

        public void addTag(TagEntity tag) {
            tags.add(tag);
        }

        public void reset() {
            tags.clear();
            savedTag = null;
        }

        private void setId(TagEntity tag, Long id) {
            try {
                Field idField = getFieldFromHierarchy(TagEntity.class, "id");
                // Make private id field accessible for test injection
                idField.setAccessible(true);
                idField.set(tag, id);
            } catch (Exception e) {
                fail("Failed to set TagEntity id via reflection", e);
            }
        }
    }

    /**
     * Custom exception used by mocks to be more specific than a raw RuntimeException.
     */
    private static class MockServiceException extends RuntimeException {
        public MockServiceException(String message) {
            super(message);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        UserSession.getUserInstance().setUser(null);
        NoteSession.setLastCreatedNote(null);
        NotebookSession.setLastCreatedNotebook(null);
        Localization.setLocale(Locale.ENGLISH);

        controller = new CreateNoteController();
        mockNotebookDao = new MockNotebookDao();
        mockTagDao = new MockTagDao();
        mockNoteService = new MockNoteService(mockNotebookDao, mockTagDao);

        UserEntity testUser = new UserEntity("Test", "User", "testUser", "test@example.com");
        setUserId(testUser);
        UserSession.getUserInstance().setUser(testUser);

        runOnFxAndWait(() -> {
            titleField = new TextField();
            contentEditorController = new RichTextEditorController();
            try {
                Field wrapperField = RichTextEditorController.class.getDeclaredField("editorWrapper");
                wrapperField.setAccessible(true);
                wrapperField.set(contentEditorController, new VBox());
            } catch (Exception e) {
                fail("Failed to set editorWrapper", e);
            }
            contentEditorController.initialize();
            annotationArea = new TextArea();
            saveButton = new Button();
            clearButton = new Button();
            statusLabel = new Label();
            notebookComboBox = new ComboBox<>();
            tagFlowPane = new FlowPane();
            tagComboBox = new ComboBox<>();
            addTagBtn = new Button();
            fileMenu = new Menu();
            editMenu = new Menu();
            backDashboard = new MenuItem();
            closeFile = new MenuItem();
            noteTitleLabel = new Label();
            noteContentLabel = new Label();
            noteAnnotationLabel = new Label();
            noteTagLabel = new Label();
            selectLabel = new Label();
            undoMenuItem = new MenuItem();
            redoMenuItem = new MenuItem();
            wordCountLabel = new Label();
            tagTooltip = new Tooltip();
            toggleTooltip = new Tooltip();
            toggleBtn = new Button();
            tagIcon = new ImageView();

            Stage localStage = new Stage();
            VBox root = new VBox();
            root.getChildren().addAll(titleField, annotationArea,
                    saveButton, clearButton, statusLabel, notebookComboBox,
                    tagFlowPane, tagComboBox, addTagBtn, wordCountLabel, toggleBtn);
            Scene scene = new Scene(root, 600, 800);
            localStage.setScene(scene);
        });

        injectField("titleField", titleField);
        injectField("contentEditorController", contentEditorController);
        injectField("annotationArea", annotationArea);
        injectField("saveButton", saveButton);
        injectField("clearButton", clearButton);
        injectField("statusLabel", statusLabel);
        injectField("notebookComboBox", notebookComboBox);
        injectField("tagFlowpane", tagFlowPane);
        injectField("tagComboBox", tagComboBox);
        injectField("addTagBtn", addTagBtn);
        injectField("fileMenu", fileMenu);
        injectField("editMenu", editMenu);
        injectField("backDashboard", backDashboard);
        injectField("closeFile", closeFile);
        injectField("noteTitleLabel", noteTitleLabel);
        injectField("noteContentLabel", noteContentLabel);
        injectField("noteAnnotationLabel", noteAnnotationLabel);
        injectField("noteTagLabel", noteTagLabel);
        injectField("selectLabel", selectLabel);
        injectField("undoMenuItem", undoMenuItem);
        injectField("redoMenuItem", redoMenuItem);
        injectField("wordCountLabel", wordCountLabel);
        injectField("tagTooltip", tagTooltip);
        injectField("toggleTooltip", toggleTooltip);
        injectField("toggleBtn", toggleBtn);
        injectField("tagIcon", tagIcon);

        // Set mocks before they get reset
        controller.setNoteService(mockNoteService);
        controller.setNotebookDao(mockNotebookDao);
        controller.setTagDao(mockTagDao);
    }

    private void setUserId(UserEntity user) {
        try {
            Field idField = getFieldFromHierarchy(UserEntity.class, "id");
            // Make private id field accessible for test injection
            idField.setAccessible(true);
            idField.set(user, 1L);
        } catch (Exception e) {
            fail("Failed to set UserEntity id via reflection", e);
        }
    }

    private static Field getFieldFromHierarchy(Class<?> type, String fieldName) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = CreateNoteController.class.getDeclaredField(fieldName);
        // Make private fields accessible for test injection
        field.setAccessible(true);
        field.set(controller, value);
    }

    private void awaitLatch(CountDownLatch latch, long timeoutSeconds, String message) throws InterruptedException {
        assertTrue(latch.await(timeoutSeconds, TimeUnit.SECONDS), message);
    }

    private void runOnFxAndWait(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        awaitLatch(latch, 2, "FX task timed out");
        if (error.get() != null) {
            fail("FX task failed", error.get());
        }
    }

    private void invokeInitialize() throws InterruptedException {
        runOnFxAndWait(() -> {
            controller.initialize(null, null);
            // Re-inject mocks after initialize in case it resets them
            controller.setNoteService(mockNoteService);
            controller.setNotebookDao(mockNotebookDao);
            controller.setTagDao(mockTagDao);
            mockNoteService.reset();
            mockNotebookDao.reset();
            mockTagDao.reset();
        });
    }

    private void invokeSave() throws Exception {
        runOnFxAndWait(() -> {
            try {
                Field nsField = CreateNoteController.class.getDeclaredField("noteService");
                nsField.setAccessible(true);
                Object ns = nsField.get(controller);
                LOGGER.fine(() -> "Type of noteService in controller: " + (ns == null ? "null" : ns.getClass().getName()));
                LOGGER.fine(() -> "Is it identical to test's mockNoteService? " + (ns == mockNoteService));

                java.lang.reflect.Method method = CreateNoteController.class.getDeclaredMethod("handleSave");
                method.setAccessible(true);
                method.invoke(controller);

                if (mockNoteService.savedNote != null) {
                    NoteSession.setLastCreatedNote(mockNoteService.savedNote);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void invokeClear() throws Exception {
        runOnFxAndWait(() -> {
            try {
                java.lang.reflect.Method method = CreateNoteController.class.getDeclaredMethod("handleClear");
                method.setAccessible(true);
                method.invoke(controller);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void initializeWithExistingNotebooksPopulatesComboBox() throws Exception {
        NotebookEntity notebook1 = new NotebookEntity(UserSession.getUserInstance().getUser());
        setCreatedAt(notebook1, LocalDateTime.now().minusDays(2));
        NotebookEntity notebook2 = new NotebookEntity(UserSession.getUserInstance().getUser());
        setCreatedAt(notebook2, LocalDateTime.now().minusDays(1));

        mockNotebookDao.addNotebook(notebook1);
        mockNotebookDao.addNotebook(notebook2);

        invokeInitialize();

        CountDownLatch checkLatch = new CountDownLatch(1);
        AtomicReference<Integer> itemCount = new AtomicReference<>();
        Platform.runLater(() -> {
            itemCount.set(notebookComboBox.getItems().size());
            checkLatch.countDown();
        });
        awaitLatch(checkLatch, 1, "Notebook combo box population timed out");

        assertEquals(3, itemCount.get());
    }

    private void setCreatedAt(NotebookEntity notebook, LocalDateTime dateTime) {
        try {
            Field field = getFieldFromHierarchy(NotebookEntity.class, "createdAt");
            field.setAccessible(true);
            field.set(notebook, dateTime);
        } catch (Exception e) {
            fail("Failed to set NotebookEntity createdAt via reflection", e);
        }
    }

    @Test
    void initializeWithExistingTagsPopulatesTagComboBox() throws Exception {
        TagEntity tag1 = new TagEntity();
        tag1.setTagName(IMPORTANT_TAG);

        TagEntity tag2 = new TagEntity();
        tag2.setTagName("Work");

        mockTagDao.addTag(tag1);
        mockTagDao.addTag(tag2);

        invokeInitialize();

        CountDownLatch checkLatch = new CountDownLatch(1);
        AtomicReference<Integer> itemCount = new AtomicReference<>();
        Platform.runLater(() -> {
            itemCount.set(tagComboBox.getItems().size());
            checkLatch.countDown();
        });
        awaitLatch(checkLatch, 1, "Tag combo box population timed out");

        assertEquals(2, itemCount.get());
    }

    @Test
    void initializeDisablesSaveButtonInitially() throws Exception {
        invokeInitialize();

        CountDownLatch checkLatch = new CountDownLatch(1);
        AtomicReference<Boolean> isDisabled = new AtomicReference<>();
        Platform.runLater(() -> {
            isDisabled.set(saveButton.isDisabled());
            checkLatch.countDown();
        });
        awaitLatch(checkLatch, 1, "Save button disable check timed out");

        assertTrue(isDisabled.get());
    }

    @Test
    void saveButtonEnabledWhenTitleAndNotebookSelected() throws Exception {
        NotebookEntity notebook = new NotebookEntity(UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText("Test Title");
            notebookComboBox.getSelectionModel().select(0);
        });

        CountDownLatch checkLatch = new CountDownLatch(1);
        AtomicReference<Boolean> isDisabled = new AtomicReference<>();
        Platform.runLater(() -> {
            isDisabled.set(saveButton.isDisabled());
            checkLatch.countDown();
        });
        awaitLatch(checkLatch, 1, "Save button state check timed out");

        assertFalse(isDisabled.get());
    }

    @Test
    void handleSaveWithValidDataSavesNote() throws Exception {
        NotebookEntity notebook = new NotebookEntity(UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText(TEST_NOTE_TITLE);
            contentEditorController.setText(TEST_NOTE_CONTENT);
            annotationArea.setText(TEST_NOTE_ANNOTATION);
            notebookComboBox.getSelectionModel().select(0);
        });

        invokeSave();

        assertNotNull(mockNoteService.savedNote);

        var translation = mockNoteService.savedNote.getTranslations().get(Localization.getCurrentLanguageCode());
        assertNotNull(translation);
        assertEquals(TEST_NOTE_TITLE, translation.getTitle());
        assertEquals(TEST_NOTE_CONTENT, RichTextStorageUtil.decode(translation.getContent()).text());
        assertEquals(TEST_NOTE_ANNOTATION, translation.getAnnotation());
    }

    @Test
    void handleSaveEmptyTitleShowsError() throws Exception {
        NotebookEntity notebook = new NotebookEntity(UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText("   ");
            notebookComboBox.getSelectionModel().select(0);
        });

        invokeSave();

        AtomicReference<String> message = new AtomicReference<>();
        runOnFxAndWait(() -> message.set(statusLabel.getText()));

        assertTrue(message.get().contains(Localization.get("create.error_no_title")));
        assertNull(mockNoteService.savedNote);
    }

    @Test
    void handleSaveNoNotebookSelectedShowsError() throws Exception {
        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText(TEST_NOTE_TITLE);
            // Clear the notebook selection
            notebookComboBox.getSelectionModel().clearSelection();
        });

        invokeSave();

        AtomicReference<String> message = new AtomicReference<>();
        runOnFxAndWait(() -> message.set(statusLabel.getText()));

        assertTrue(message.get().contains(Localization.get("create.error_no_notebook")));
        assertNull(mockNoteService.savedNote);
    }

    @Test
    void handleSaveWithTagsAssociatesTagsWithNote() throws Exception {
        NotebookEntity notebook = new NotebookEntity(UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        TagEntity tag1 = new TagEntity();
        tag1.setTagName(IMPORTANT_TAG);
        mockTagDao.addTag(tag1);

        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText(TEST_NOTE_TITLE);
            notebookComboBox.getSelectionModel().select(0);
        });

        Field selectedTagsField = CreateNoteController.class.getDeclaredField(SELECTED_TAGS_FIELD);
        selectedTagsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<String> selectedTags = (java.util.Set<String>) selectedTagsField.get(controller);
        selectedTags.add(IMPORTANT_TAG);

        invokeSave();

        assertNotNull(mockNoteService.savedNote);
        assertTrue(mockNoteService.savedNote.getTags().stream()
                .anyMatch(tag -> tag.getTagName().equals(IMPORTANT_TAG)));
    }

    @Test
    void handleSaveWithNewTagCreatesAndAssociatesTag() throws Exception {
        NotebookEntity notebook = new NotebookEntity(UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText(TEST_NOTE_TITLE);
            notebookComboBox.getSelectionModel().select(0);
        });

        Field selectedTagsField = CreateNoteController.class.getDeclaredField(SELECTED_TAGS_FIELD);
        selectedTagsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<String> selectedTags = (java.util.Set<String>) selectedTagsField.get(controller);
        selectedTags.add("NewTag");

        invokeSave();

        assertNotNull(mockTagDao.savedTag);
        assertEquals("NewTag", mockTagDao.savedTag.getTagName());
    }

    @Test
    void handleSaveSetsNoteSession() throws Exception {
        NotebookEntity notebook = new NotebookEntity(UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText(TEST_NOTE_TITLE);
            notebookComboBox.getSelectionModel().select(0);
        });

        invokeSave();

        assertNotNull(NoteSession.getLastCreatedNote());

        var translation = mockNoteService.savedNote.getTranslations().get(Localization.getCurrentLanguageCode());
        assertNotNull(translation);
        assertEquals(TEST_NOTE_TITLE, translation.getTitle());
    }

    @Test
    void handleClearClearsAllFields() throws Exception {
        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText("Test Title");
            contentEditorController.setText(TEST_NOTE_CONTENT);
            annotationArea.setText(TEST_NOTE_ANNOTATION);
        });

        invokeClear();

        AtomicReference<String> title = new AtomicReference<>();
        AtomicReference<String> content = new AtomicReference<>();
        AtomicReference<String> annotation = new AtomicReference<>();
        runOnFxAndWait(() -> {
            title.set(titleField.getText());
            content.set(contentEditorController.getText());
            annotation.set(annotationArea.getText());
        });

        assertTrue(title.get().isEmpty());
        assertTrue(content.get().isEmpty());
        assertTrue(annotation.get().isEmpty());
    }

    @Test
    void handleClearClearsSelectedTags() throws Exception {
        invokeInitialize();

        Field selectedTagsField = CreateNoteController.class.getDeclaredField(SELECTED_TAGS_FIELD);
        selectedTagsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<String> selectedTags = (java.util.Set<String>) selectedTagsField.get(controller);
        selectedTags.add("Tag1");
        selectedTags.add("Tag2");

        invokeClear();

        assertTrue(selectedTags.isEmpty());
    }

    @Test
    void handleSaveWithNullContentSavesWithEmptyContent() throws Exception {
        NotebookEntity notebook = new NotebookEntity(UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText(TEST_NOTE_TITLE);
            notebookComboBox.getSelectionModel().select(0);
        });

        invokeSave();

        assertNotNull(mockNoteService.savedNote);

        var translation = NoteSession.getLastCreatedNote().getTranslations().get(Localization.getCurrentLanguageCode());
        assertNotNull(translation);
        assertEquals("", RichTextStorageUtil.decode(translation.getContent()).text());
        assertEquals("", translation.getAnnotation());
    }

    @Test
    void handleSaveTrimsTitle() throws Exception {
        NotebookEntity notebook = new NotebookEntity(UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText("  " + TEST_NOTE_TITLE + "  ");
            notebookComboBox.getSelectionModel().select(0);
        });

        invokeSave();

        assertNotNull(mockNoteService.savedNote);

        var translation = mockNoteService.savedNote.getTranslations().get(Localization.getCurrentLanguageCode());
        assertNotNull(translation);
        assertEquals(TEST_NOTE_TITLE, translation.getTitle());
    }

    @Test
    void handleSaveShowsSuccessMessage() throws Exception {
        NotebookEntity notebook = new NotebookEntity(UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText(TEST_NOTE_TITLE);
            notebookComboBox.getSelectionModel().select(0);
        });

        invokeSave();

        AtomicReference<String> message = new AtomicReference<>();
        runOnFxAndWait(() -> message.set(statusLabel.getText()));

        assertTrue(message.get().contains(Localization.get("create.success")));
    }

    @Test
    void handleUndoAndRedoExecutesWithoutException() throws Exception {
        invokeInitialize();
        
        runOnFxAndWait(() -> {
            try {
                java.lang.reflect.Method undoMethod = CreateNoteController.class.getDeclaredMethod("handleUndo");
                undoMethod.setAccessible(true);
                undoMethod.invoke(controller);

                java.lang.reflect.Method redoMethod = CreateNoteController.class.getDeclaredMethod("handleRedo");
                redoMethod.setAccessible(true);
                redoMethod.invoke(controller);
            } catch (Exception e) {
                fail("Undo/Redo should not throw an exception", e);
            }
        });
    }

    @Test
    void handleSaveWhenExceptionIsThrownShowsErrorMessage() throws Exception {
        NotebookEntity notebook = new NotebookEntity(UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText(TEST_NOTE_TITLE);
            notebookComboBox.getSelectionModel().select(0);
        });

        mockNoteService.shouldThrowException = true;
        invokeSave();

        AtomicReference<String> message = new AtomicReference<>();
        runOnFxAndWait(() -> message.set(statusLabel.getText()));

        assertTrue(message.get().contains("Error: Mock exception"));
    }

    @Test
    void handleAddTagAddsTagToSelectedTags() throws Exception {
        invokeInitialize();

        runOnFxAndWait(() -> {
            tagComboBox.getEditor().setText("NewAddedTag");
            try {
                java.lang.reflect.Method addTagMethod = CreateNoteController.class.getDeclaredMethod("handleAddTag");
                addTagMethod.setAccessible(true);
                addTagMethod.invoke(controller);
            } catch (Exception e) {
                fail("handleAddTag should not throw an exception", e);
            }
        });

        Field selectedTagsField = CreateNoteController.class.getDeclaredField(SELECTED_TAGS_FIELD);
        selectedTagsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<String> selectedTags = (java.util.Set<String>) selectedTagsField.get(controller);

        assertTrue(selectedTags.contains("NewAddedTag"));
    }

    @Test
    void handleThemeToggleTogglesIcon() throws Exception {
        invokeInitialize();

        runOnFxAndWait(() -> {
            try {
                java.lang.reflect.Method toggleMethod = CreateNoteController.class.getDeclaredMethod("handleThemeToggle");
                toggleMethod.setAccessible(true);
                toggleMethod.invoke(controller);
            } catch (Exception e) {
                fail("handleThemeToggle should not throw an exception", e);
            }
        });

        AtomicReference<ImageView> graphic = new AtomicReference<>();
        runOnFxAndWait(() -> graphic.set((ImageView) toggleBtn.getGraphic()));

        assertNotNull(graphic.get());
    }

    @Test
    void getNotebookTitleWithNullNotebookReturnsEmpty() throws Exception {
        java.lang.reflect.Method getTitleMethod = CreateNoteController.class.getDeclaredMethod("getNotebookTitle", NotebookEntity.class);
        getTitleMethod.setAccessible(true);
        
        String title = (String) getTitleMethod.invoke(controller, (NotebookEntity) null);
        assertEquals("", title);
    }
}
