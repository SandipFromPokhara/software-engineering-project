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

    private CreateNoteController controller;
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
                throw new RuntimeException("Mock exception");
            }

            System.out.println("MOCK createNote called!");

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
            System.out.println("assigned savedNote: " + savedNote);
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
                idField.setAccessible(true);
                idField.set(tag, id);
            } catch (Exception e) {
                fail("Failed to set TagEntity id via reflection", e);
            }
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

    private void invokeInitialize() throws Exception {
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
                System.out.println("Type of noteService in controller: " + (ns == null ? "null" : ns.getClass().getName()));
                System.out.println("Is it identical to test's mockNoteService? " + (ns == mockNoteService));

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
    void initialize_WithExistingNotebooks_PopulatesComboBox() throws Exception {
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
    void initialize_WithExistingTags_PopulatesTagComboBox() throws Exception {
        TagEntity tag1 = new TagEntity();
        tag1.setTagName("Important");

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
    void initialize_DisablesSaveButtonInitially() throws Exception {
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
    void saveButton_EnabledWhenTitleAndNotebookSelected() throws Exception {
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
    void handleSave_WithValidData_SavesNote() throws Exception {
        NotebookEntity notebook = new NotebookEntity(UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText("Test Note");
            contentEditorController.setText("Test Content");
            annotationArea.setText("Test Annotation");
            notebookComboBox.getSelectionModel().select(0);
        });

        invokeSave();

        assertNotNull(mockNoteService.savedNote);

        var translation = mockNoteService.savedNote.getTranslations().get(Localization.getCurrentLanguageCode());
        assertNotNull(translation);
        assertEquals("Test Note", translation.getTitle());
        assertEquals("Test Content", RichTextStorageUtil.decode(translation.getContent()).text());
        assertEquals("Test Annotation", translation.getAnnotation());
    }

    @Test
    void handleSave_EmptyTitle_ShowsError() throws Exception {
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
    void handleSave_NoNotebookSelected_ShowsError() throws Exception {
        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText("Test Note");
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
    void handleSave_WithTags_AssociatesTagsWithNote() throws Exception {
        NotebookEntity notebook = new NotebookEntity(UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        TagEntity tag1 = new TagEntity();
        tag1.setTagName("Important");
        mockTagDao.addTag(tag1);

        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText("Test Note");
            notebookComboBox.getSelectionModel().select(0);
        });

        Field selectedTagsField = CreateNoteController.class.getDeclaredField("selectedTags");
        selectedTagsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<String> selectedTags = (java.util.Set<String>) selectedTagsField.get(controller);
        selectedTags.add("Important");

        invokeSave();

        assertNotNull(mockNoteService.savedNote);
        assertTrue(mockNoteService.savedNote.getTags().stream()
                .anyMatch(tag -> tag.getTagName().equals("Important")));
    }

    @Test
    void handleSave_WithNewTag_CreatesAndAssociatesTag() throws Exception {
        NotebookEntity notebook = new NotebookEntity(UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText("Test Note");
            notebookComboBox.getSelectionModel().select(0);
        });

        Field selectedTagsField = CreateNoteController.class.getDeclaredField("selectedTags");
        selectedTagsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<String> selectedTags = (java.util.Set<String>) selectedTagsField.get(controller);
        selectedTags.add("NewTag");

        invokeSave();

        assertNotNull(mockTagDao.savedTag);
        assertEquals("NewTag", mockTagDao.savedTag.getTagName());
    }

    @Test
    void handleSave_SetsNoteSession() throws Exception {
        NotebookEntity notebook = new NotebookEntity(UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText("Test Note");
            notebookComboBox.getSelectionModel().select(0);
        });

        invokeSave();

        assertNotNull(NoteSession.getLastCreatedNote());

        var translation = mockNoteService.savedNote.getTranslations().get(Localization.getCurrentLanguageCode());
        assertNotNull(translation);
        assertEquals("Test Note", translation.getTitle());
    }

    @Test
    void handleClear_ClearsAllFields() throws Exception {
        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText("Test Title");
            contentEditorController.setText("Test Content");
            annotationArea.setText("Test Annotation");
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
    void handleClear_ClearsSelectedTags() throws Exception {
        invokeInitialize();

        Field selectedTagsField = CreateNoteController.class.getDeclaredField("selectedTags");
        selectedTagsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<String> selectedTags = (java.util.Set<String>) selectedTagsField.get(controller);
        selectedTags.add("Tag1");
        selectedTags.add("Tag2");

        invokeClear();

        assertTrue(selectedTags.isEmpty());
    }

    @Test
    void handleSave_WithNullContent_SavesWithEmptyContent() throws Exception {
        NotebookEntity notebook = new NotebookEntity(UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText("Test Note");
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
    void handleSave_TrimsTitle() throws Exception {
        NotebookEntity notebook = new NotebookEntity(UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText("  Test Note  ");
            notebookComboBox.getSelectionModel().select(0);
        });

        invokeSave();

        assertNotNull(mockNoteService.savedNote);

        var translation = mockNoteService.savedNote.getTranslations().get(Localization.getCurrentLanguageCode());
        assertNotNull(translation);
        assertEquals("Test Note", translation.getTitle());
    }

    @Test
    void handleSave_ShowsSuccessMessage() throws Exception {
        NotebookEntity notebook = new NotebookEntity(UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        runOnFxAndWait(() -> {
            titleField.setText("Test Note");
            notebookComboBox.getSelectionModel().select(0);
        });

        invokeSave();

        AtomicReference<String> message = new AtomicReference<>();
        runOnFxAndWait(() -> message.set(statusLabel.getText()));

        assertTrue(message.get().contains(Localization.get("create.success")));
    }
}
