package controller;

import dao.notebook.JpaNoteBookDao;
import dao.tag.JpaTagDao;
import entity.NoteBookEntity;
import entity.NoteEntity;
import entity.TagEntity;
import entity.UserEntity;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import services.NoteService;
import session.NoteSession;
import session.NotebookSession;
import session.UserSession;
import testutil.JavaFXInitializer;
import testutil.JavaFxTestExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private TextArea contentArea;
    private TextArea annotationArea;
    private Button saveButton;
    private Button clearButton;
    private Label statusLabel;
    private ComboBox<NoteBookEntity> notebookComboBox;
    private FlowPane tagFlowpane;
    private ComboBox<String> tagComboBox;
    private Button addTagBtn;
    private MenuItem undoMenuItem;
    private MenuItem redoMenuItem;
    private Label wordCountLabel;
    private Tooltip tagTooltip;
    private Tooltip toggleTooltip;
    private Button toggleBtn;
    private Button bulletListButton;
    private Button numberedListButton;
    private Button headingUpButton;
    private Button headingDownButton;
    private ImageView tagIcon;

    private Stage testStage;

    private static class MockNoteService extends NoteService {
        private NoteEntity savedNote;
        private boolean shouldThrowException = false;

        public MockNoteService() {
            super(null, null);
        }

        @Override
        public NoteEntity save(NoteEntity note) {
            if (shouldThrowException) {
                throw new RuntimeException("Database error");
            }
            this.savedNote = note;
            if (note.getId() == null) {
                setId(note, 1L);
            }
            // Don't call super.save() to avoid database access
            return note;
        }

        public void reset() {
            savedNote = null;
            shouldThrowException = false;
        }

        private void setId(NoteEntity note, Long id) {
            try {
                Field idField = NoteEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(note, id);
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private static class MockNotebookDao extends JpaNoteBookDao {
        private List<NoteBookEntity> notebooks = new ArrayList<>();
        private NoteBookEntity savedNotebook;

        @Override
        public List<NoteBookEntity> findByUser(UserEntity user) {
            return new ArrayList<>(notebooks);
        }

        @Override
        public NoteBookEntity save(NoteBookEntity notebook) {
            if (notebook.getId() == null) {
                setId(notebook, (long) (notebooks.size() + 1));
            }
            notebooks.add(notebook);
            savedNotebook = notebook;
            return notebook;
        }

        public void addNotebook(NoteBookEntity notebook) {
            notebooks.add(notebook);
        }

        public void reset() {
            notebooks.clear();
            savedNotebook = null;
        }

        private void setId(NoteBookEntity notebook, Long id) {
            try {
                Field idField = NoteBookEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(notebook, id);
            } catch (Exception e) {
            }
        }
    }

    private static class MockTagDao extends JpaTagDao {
        private List<TagEntity> tags = new ArrayList<>();
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
                Field idField = TagEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(tag, id);
            } catch (Exception e) {
            }
        }
    }

    @BeforeAll
    static void initJavaFX() {
        JavaFXInitializer.init();
    }

    @BeforeEach
    void setUp() throws Exception {
        UserSession.getUserInstance().setUser(null);
        NoteSession.setLastCreatedNote(null);
        NotebookSession.setLastCreatedNotebook(null);

        controller = new CreateNoteController();
        mockNoteService = new MockNoteService();
        mockNotebookDao = new MockNotebookDao();
        mockTagDao = new MockTagDao();

        UserEntity testUser = new UserEntity("Test", "User", "testuser", "test@example.com");
        setId(testUser, 1L);
        UserSession.getUserInstance().setUser(testUser);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            titleField = new TextField();
            contentArea = new TextArea();
            annotationArea = new TextArea();
            saveButton = new Button();
            clearButton = new Button();
            statusLabel = new Label();
            notebookComboBox = new ComboBox<>();
            tagFlowpane = new FlowPane();
            tagComboBox = new ComboBox<>();
            addTagBtn = new Button();
            undoMenuItem = new MenuItem();
            redoMenuItem = new MenuItem();
            wordCountLabel = new Label();
            tagTooltip = new Tooltip();
            toggleTooltip = new Tooltip();
            toggleBtn = new Button();
            bulletListButton = new Button();
            numberedListButton = new Button();
            headingUpButton = new Button();
            headingDownButton = new Button();
            tagIcon = new ImageView();

            testStage = new Stage();
            VBox root = new VBox();
            root.getChildren().addAll(titleField, contentArea, annotationArea,
                    saveButton, clearButton, statusLabel, notebookComboBox,
                    tagFlowpane, tagComboBox, addTagBtn, wordCountLabel, toggleBtn);
            Scene scene = new Scene(root, 600, 800);
            testStage.setScene(scene);
            latch.countDown();
        });
        latch.await(2, TimeUnit.SECONDS);

        injectField("titleField", titleField);
        injectField("contentArea", contentArea);
        injectField("annotationArea", annotationArea);
        injectField("saveButton", saveButton);
        injectField("clearButton", clearButton);
        injectField("statusLabel", statusLabel);
        injectField("notebookComboBox", notebookComboBox);
        injectField("tagFlowpane", tagFlowpane);
        injectField("tagComboBox", tagComboBox);
        injectField("addTagBtn", addTagBtn);
        injectField("undoMenuItem", undoMenuItem);
        injectField("redoMenuItem", redoMenuItem);
        injectField("wordCountLabel", wordCountLabel);
        injectField("tagTooltip", tagTooltip);
        injectField("toggleTooltip", toggleTooltip);
        injectField("toggleBtn", toggleBtn);
        injectField("bulletListButton", bulletListButton);
        injectField("numberedListButton", numberedListButton);
        injectField("headingUpButton", headingUpButton);
        injectField("headingDownButton", headingDownButton);
        injectField("tagIcon", tagIcon);

        // Set mocks before they get reset
        controller.setNoteService(mockNoteService);
        controller.setNotebookDao(mockNotebookDao);
        controller.setTagDao(mockTagDao);
    }

    private void setId(UserEntity user, Long id) {
        try {
            Field idField = UserEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
        }
    }

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = CreateNoteController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    private void invokeInitialize() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.initialize(null, null);
                // Re-inject mocks after initialize in case it resets them
                controller.setNoteService(mockNoteService);
                controller.setNotebookDao(mockNotebookDao);
                controller.setTagDao(mockTagDao);
                mockNoteService.reset();
                mockNotebookDao.reset();
                mockTagDao.reset();
            } catch (Exception e) {
                e.printStackTrace();
            }
            latch.countDown();
        });
        latch.await(2, TimeUnit.SECONDS);
    }

    private void invokeSave() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                java.lang.reflect.Method method = CreateNoteController.class.getDeclaredMethod("handleSave");
                method.setAccessible(true);
                method.invoke(controller);
            } catch (Exception e) {
                e.printStackTrace();
            }
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);
    }

    private void invokeClear() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                java.lang.reflect.Method method = CreateNoteController.class.getDeclaredMethod("handleClear");
                method.setAccessible(true);
                method.invoke(controller);
            } catch (Exception e) {
            }
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);
    }

    @Test
    void initialize_WithExistingNotebooks_PopulatesComboBox() throws Exception {
        NoteBookEntity notebook1 = new NoteBookEntity("Notebook 1", UserSession.getUserInstance().getUser());
        setCreatedAt(notebook1, LocalDateTime.now().minusDays(2));
        NoteBookEntity notebook2 = new NoteBookEntity("Notebook 2", UserSession.getUserInstance().getUser());
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
        checkLatch.await(1, TimeUnit.SECONDS);

        assertEquals(3, itemCount.get());
    }

    private void setCreatedAt(NoteBookEntity notebook, LocalDateTime dateTime) {
        try {
            Field field = NoteBookEntity.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(notebook, dateTime);
        } catch (Exception e) {
        }
    }

    @Test
    void initialize_WithExistingTags_PopulatesTagComboBox() throws Exception {
        TagEntity tag1 = new TagEntity("Important");
        TagEntity tag2 = new TagEntity("Work");
        mockTagDao.addTag(tag1);
        mockTagDao.addTag(tag2);

        invokeInitialize();

        CountDownLatch checkLatch = new CountDownLatch(1);
        AtomicReference<Integer> itemCount = new AtomicReference<>();
        Platform.runLater(() -> {
            itemCount.set(tagComboBox.getItems().size());
            checkLatch.countDown();
        });
        checkLatch.await(1, TimeUnit.SECONDS);

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
        checkLatch.await(1, TimeUnit.SECONDS);

        assertTrue(isDisabled.get());
    }

    @Test
    void saveButton_EnabledWhenTitleAndNotebookSelected() throws Exception {
        NoteBookEntity notebook = new NoteBookEntity("Test Notebook", UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            titleField.setText("Test Title");
            notebookComboBox.getSelectionModel().select(0);
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);

        Thread.sleep(100);

        CountDownLatch checkLatch = new CountDownLatch(1);
        AtomicReference<Boolean> isDisabled = new AtomicReference<>();
        Platform.runLater(() -> {
            isDisabled.set(saveButton.isDisabled());
            checkLatch.countDown();
        });
        checkLatch.await(1, TimeUnit.SECONDS);

        assertFalse(isDisabled.get());
    }

    @Test
    void handleSave_WithValidData_SavesNote() throws Exception {
        NoteBookEntity notebook = new NoteBookEntity("Test Notebook", UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        CountDownLatch setupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            titleField.setText("Test Note");
            contentArea.setText("Test Content");
            annotationArea.setText("Test Annotation");
            notebookComboBox.getSelectionModel().select(0);
            setupLatch.countDown();
        });
        setupLatch.await(1, TimeUnit.SECONDS);

        invokeSave();

        assertNotNull(mockNoteService.savedNote);
        assertEquals("Test Note", mockNoteService.savedNote.getTitle());
        assertEquals("Test Content", mockNoteService.savedNote.getContent());
        assertEquals("Test Annotation", mockNoteService.savedNote.getAnnotation());
    }

    @Test
    void handleSave_EmptyTitle_ShowsError() throws Exception {
        NoteBookEntity notebook = new NoteBookEntity("Test Notebook", UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        CountDownLatch setupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            titleField.setText("   ");
            notebookComboBox.getSelectionModel().select(0);
            setupLatch.countDown();
        });
        setupLatch.await(1, TimeUnit.SECONDS);

        invokeSave();

        AtomicReference<String> message = new AtomicReference<>();
        CountDownLatch checkLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            message.set(statusLabel.getText());
            checkLatch.countDown();
        });
        checkLatch.await(1, TimeUnit.SECONDS);

        assertTrue(message.get().contains("title"));
        assertNull(mockNoteService.savedNote);
    }

    @Test
    void handleSave_NoNotebookSelected_ShowsError() throws Exception {
        invokeInitialize();

        CountDownLatch setupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            titleField.setText("Test Note");
            // Clear the notebook selection
            notebookComboBox.getSelectionModel().clearSelection();
            setupLatch.countDown();
        });
        setupLatch.await(1, TimeUnit.SECONDS);

        invokeSave();

        AtomicReference<String> message = new AtomicReference<>();
        CountDownLatch checkLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            message.set(statusLabel.getText());
            checkLatch.countDown();
        });
        checkLatch.await(1, TimeUnit.SECONDS);

        assertTrue(message.get().contains("notebook"));
        assertNull(mockNoteService.savedNote);
    }

    @Test
    void handleSave_WithTags_AssociatesTagsWithNote() throws Exception {
        NoteBookEntity notebook = new NoteBookEntity("Test Notebook", UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        TagEntity tag1 = new TagEntity("Important");
        mockTagDao.addTag(tag1);

        invokeInitialize();

        CountDownLatch setupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            titleField.setText("Test Note");
            notebookComboBox.getSelectionModel().select(0);
            setupLatch.countDown();
        });
        setupLatch.await(1, TimeUnit.SECONDS);

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
        NoteBookEntity notebook = new NoteBookEntity("Test Notebook", UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        CountDownLatch setupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            titleField.setText("Test Note");
            notebookComboBox.getSelectionModel().select(0);
            setupLatch.countDown();
        });
        setupLatch.await(1, TimeUnit.SECONDS);

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
        NoteBookEntity notebook = new NoteBookEntity("Test Notebook", UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        CountDownLatch setupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            titleField.setText("Test Note");
            notebookComboBox.getSelectionModel().select(0);
            setupLatch.countDown();
        });
        setupLatch.await(1, TimeUnit.SECONDS);

        invokeSave();

        assertNotNull(NoteSession.getLastCreatedNote());
        assertEquals("Test Note", NoteSession.getLastCreatedNote().getTitle());
    }

    @Test
    void handleClear_ClearsAllFields() throws Exception {
        invokeInitialize();

        CountDownLatch setupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            titleField.setText("Test Title");
            contentArea.setText("Test Content");
            annotationArea.setText("Test Annotation");
            setupLatch.countDown();
        });
        setupLatch.await(1, TimeUnit.SECONDS);

        invokeClear();

        CountDownLatch checkLatch = new CountDownLatch(1);
        AtomicReference<String> title = new AtomicReference<>();
        AtomicReference<String> content = new AtomicReference<>();
        AtomicReference<String> annotation = new AtomicReference<>();
        Platform.runLater(() -> {
            title.set(titleField.getText());
            content.set(contentArea.getText());
            annotation.set(annotationArea.getText());
            checkLatch.countDown();
        });
        checkLatch.await(1, TimeUnit.SECONDS);

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
        NoteBookEntity notebook = new NoteBookEntity("Test Notebook", UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        CountDownLatch setupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            titleField.setText("Test Note");
            notebookComboBox.getSelectionModel().select(0);
            setupLatch.countDown();
        });
        setupLatch.await(1, TimeUnit.SECONDS);

        invokeSave();

        assertNotNull(mockNoteService.savedNote);
        assertEquals("", mockNoteService.savedNote.getContent());
        assertEquals("", mockNoteService.savedNote.getAnnotation());
    }

    @Test
    void handleSave_TrimsTitle() throws Exception {
        NoteBookEntity notebook = new NoteBookEntity("Test Notebook", UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        CountDownLatch setupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            titleField.setText("  Test Note  ");
            notebookComboBox.getSelectionModel().select(0);
            setupLatch.countDown();
        });
        setupLatch.await(1, TimeUnit.SECONDS);

        invokeSave();

        assertNotNull(mockNoteService.savedNote);
        assertEquals("Test Note", mockNoteService.savedNote.getTitle());
    }

    @Test
    void handleSave_ShowsSuccessMessage() throws Exception {
        NoteBookEntity notebook = new NoteBookEntity("Test Notebook", UserSession.getUserInstance().getUser());
        mockNotebookDao.addNotebook(notebook);

        invokeInitialize();

        CountDownLatch setupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            titleField.setText("Test Note");
            notebookComboBox.getSelectionModel().select(0);
            setupLatch.countDown();
        });
        setupLatch.await(1, TimeUnit.SECONDS);

        invokeSave();

        Thread.sleep(100);

        AtomicReference<String> message = new AtomicReference<>();
        CountDownLatch checkLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            message.set(statusLabel.getText());
            checkLatch.countDown();
        });
        checkLatch.await(1, TimeUnit.SECONDS);

        assertTrue(message.get().contains("success"));
    }
}

