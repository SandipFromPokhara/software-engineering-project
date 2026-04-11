package controller;

import dao.notebook.JpaNotebookDao;
import dao.tag.JpaTagDao;
import entity.entities.NotebookEntity;
import entity.entities.TagEntity;
import entity.entities.UserEntity;
import entity.translationentities.NotebookTranslationEntity;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.NoteService;
import entity.entities.NoteEntity;
import session.UserSession;
import util.*;
import util.bulletList.TextFormattingUtil;
import util.events.EventBus;
import util.events.NoteCreatedEvent;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.HashSet;

/**
 * Controller for Create Note - handles save and clear operations
 */
public class CreateNoteController implements Initializable {

    private static final String NEW_NOTEBOOK_KEY = "create.new_notebook";

    private NoteService noteService;
    private final Set<String> selectedTags = new HashSet<>();
    private JpaNotebookDao notebookDao;
    private JpaTagDao tagDao;

    @FXML
    private Menu fileMenu;

    @FXML
    private Menu editMenu;

    @FXML
    private MenuItem backDashboard;

    @FXML
    private MenuItem closeFile;

    @FXML
    private Label noteTitleLabel;

    @FXML
    private Label noteContentLabel;

    @FXML
    private Label noteAnnotationLabel;

    @FXML
    private Label noteTagLabel;

    @FXML
    private Label selectLabel;

    @FXML
    private TextField titleField;

    @FXML
    private TextArea annotationArea;

    @FXML
    private Button saveButton;

    @FXML
    private Button clearButton;

    @FXML
    private Label statusLabel;

    @FXML
    private ComboBox<NotebookEntity> notebookComboBox;

    @FXML
    private FlowPane tagFlowpane;

    @FXML
    private ComboBox<String> tagComboBox;

    @FXML
    private Button addTagBtn;

    @FXML
    private Tooltip tagTooltip;

    @FXML
    private Tooltip toggleTooltip;

    @FXML
    private Button toggleBtn;

    @FXML
    private ImageView tagIcon;

    @FXML
    private Label wordCountLabel;

    // Undo/Redo components
    @FXML
    private MenuItem undoMenuItem;

    @FXML
    private MenuItem redoMenuItem;

    private final UndoRedoManager undoRedoManager = new UndoRedoManager();

    // Reusable rich text editor component controller (from fx:include fx:id="contentEditor")
    @FXML
    private RichTextEditorController contentEditorController;

    public CreateNoteController() {
        // Required by FXMLLoader; dependencies are injected/set later.
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // LOCALIZATION BINDINGS
        fileMenu.textProperty().bind(Localization.bind("file.menu"));
        editMenu.textProperty().bind(Localization.bind("edit.menu"));
        closeFile.textProperty().bind(Localization.bind("file.menu-item1"));
        backDashboard.textProperty().bind(Localization.bind("file.menu-item2"));
        undoMenuItem.textProperty().bind(Localization.bind("edit.undo"));
        redoMenuItem.textProperty().bind(Localization.bind("edit.redo"));

        toggleTooltip.textProperty().bind(Localization.bind("tooltip.theme_toggle"));
        tagTooltip.textProperty().bind(Localization.bind("tooltip.tags_info"));
        noteTitleLabel.textProperty().bind(Localization.bind("create.title_label"));
        noteContentLabel.textProperty().bind(Localization.bind("create.note_content"));
        selectLabel.textProperty().bind(Localization.bind("notebook.select_label"));

        WordCountUtil.bind(contentEditorController.getTextArea(), wordCountLabel);

        noteAnnotationLabel.textProperty().bind(Localization.bind("create.note_annotations"));
        noteTagLabel.textProperty().bind(Localization.bind("create.tags"));

        saveButton.textProperty().bind(Localization.bind("create.save"));
        clearButton.textProperty().bind(Localization.bind("create.clear"));

        titleField.promptTextProperty().bind(Localization.bind("create.placeholder_title"));
        contentEditorController.bindPromptText(Localization.bind("create.placeholder_content"));
        annotationArea.promptTextProperty().bind(Localization.bind("create.placeholder_annotations"));

        tagComboBox.promptTextProperty().bind(Localization.bind("create.placeholder_tags"));
        addTagBtn.textProperty().bind(Localization.bind("create.add_tags"));

        // Tooltip delay
        tagTooltip.setShowDelay(Duration.millis(100));
        toggleTooltip.setShowDelay(Duration.millis(100));

        // Load current user and notebooks
        UserEntity currentUser = UserSession.getUserInstance().getUser();
        List<NotebookEntity> notebooks = (notebookDao != null ? notebookDao : new JpaNotebookDao()).findByUser(currentUser);

        notebooks.sort((n1, n2) -> {
            if (n1.getCreatedAt() == null) return -1;
            if (n2.getCreatedAt() == null) return 1;
            return n1.getCreatedAt().compareTo(n2.getCreatedAt());
        });

        notebookComboBox.getItems().setAll(notebooks);

        NotebookEntity createNewItem = new NotebookEntity();
        createNewItem.setUser(currentUser);

        NotebookTranslationEntity t = new NotebookTranslationEntity();
        t.setLangCode(Localization.getCurrentLanguageCode());
        t.setTitle(Localization.get(NEW_NOTEBOOK_KEY));
        t.setNotebook(createNewItem);

        createNewItem.addTranslation(t);

        notebookComboBox.getItems().add(createNewItem);

        notebookComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(NotebookEntity notebook) {
                if (notebook == null) {
                    return "";
                }
                // Use robust lookup that handles case-variants and fallbacks
                String title = getNotebookTitle(notebook);
                return title == null ? "" : title;
            }

            @Override
            public NotebookEntity fromString(String string) {
                return null;
            }
        });

        // Select first notebook if available
        if (!notebookComboBox.getItems().isEmpty()) {
            notebookComboBox.getSelectionModel().select(0);
        }

        if (noteService == null) {
            noteService = new NoteService();
        }
        statusLabel.setVisible(false);
        saveButton.setDisable(true);

        // Disable save button if title is empty or ComboBox has no selection
        titleField.textProperty().addListener((obs, old, newVal) -> updateSaveButton());
        notebookComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> updateSaveButton());

        // Load existing tags
        List<TagEntity> allTags = (tagDao != null ? tagDao : new JpaTagDao()).findAll();
        List<String> tagNames = allTags.stream()
                .map(TagEntity::getTagName)
                .filter(name -> name != null && !name.isBlank())
                .sorted(String::compareToIgnoreCase)
                .toList();

        tagComboBox.getItems().setAll(tagNames);

        // Initialize undo/redo manager
        undoRedoManager.initialize(undoMenuItem, redoMenuItem);
        undoRedoManager.registerField("title", titleField);
        undoRedoManager.registerField("content", contentEditorController.getTextArea());
        undoRedoManager.registerField("annotation", annotationArea);

        // Apply theme once scene is ready
        javafx.application.Platform.runLater(() -> {
            Scene scene = titleField.getScene();
            ToggleUtil.applyTheme(scene);
            updateToggleIcon();
            updateTagIcon();
        });
        // Enable list auto-continuation for content area
        TextFormattingUtil.enableListAutoContinuation(contentEditorController.getTextArea());
    }

    @FXML
    private void handleUndo() {
        undoRedoManager.undo();
    }

    @FXML
    private void handleRedo() {
        undoRedoManager.redo();
    }

    private void updateSaveButton() {
        NotebookEntity selected = notebookComboBox.getSelectionModel().getSelectedItem();
        boolean disable = titleField.getText() == null || titleField.getText().isBlank() || selected == null;
        saveButton.setDisable(disable);
    }

    @FXML
    private void handleSave() {
        try {
            String title = titleField.getText().trim();
            String content = contentEditorController != null ? contentEditorController.getSerializedContent() : "";
            String annotation = annotationArea.getText() == null ? "" : annotationArea.getText();

            NotebookEntity selectedNotebook = notebookComboBox.getSelectionModel().getSelectedItem();

            if (selectedNotebook == null) {
                showStatus(Localization.get("create.error_no_notebook"), true);
                return;
            }

            if (title.isEmpty()) {
                showStatus(Localization.get("create.error_no_title"), true);
                return;
            }

            // Handle "Create New Notebook"
            String notebookTitle = getNotebookTitle(selectedNotebook);

            if (Localization.get(NEW_NOTEBOOK_KEY).equals(notebookTitle)) {

                String name = promptForNotebookName();
                if (name == null) {
                    showStatus(Localization.get("create.cancelled"), true);
                    return;
                }

                selectedNotebook = noteService.createNotebook(name);
                addNotebookToComboBox(selectedNotebook);
            }

            String lang = Localization.getCurrentLanguageCode();
            NoteEntity createdNote = noteService.createNote(title, content, annotation, selectedNotebook, selectedTags, lang);

            EventBus.publish(new NoteCreatedEvent(createdNote));

            showStatus(Localization.get("create.success"), false);

            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleClear() {
        // Clear the reusable editor content as well
        if (contentEditorController != null) {
            contentEditorController.setText("");
        }

        clearForm();
        statusLabel.setVisible(false);
    }

    private void clearForm() {
        titleField.clear();
        annotationArea.clear();
        selectedTags.clear();
        refreshTagFlowPane();
        titleField.requestFocus();

        // Clear undo/redo history
        undoRedoManager.clear();
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setTextFill(isError ? Color.RED : Color.GREEN);
        statusLabel.setVisible(true);
    }

    @FXML
    private void handleBackToHome() {
        WindowUtil.closeWindow(titleField);
    }

    @FXML
    private void handleClose() {
        WindowUtil.closeWindow(titleField);
    }

    @FXML
    private void handleAddTag() {
        String tagName = tagComboBox.getEditor().getText();
        TagUtil.addTagToUI(selectedTags, tagFlowpane, tagComboBox, tagName);
    }

    private void refreshTagFlowPane() {
        TagUtil.refreshFlowPane(selectedTags, tagFlowpane);
    }

    // Update toggle button icon
    private void updateToggleIcon() {
        ImageView icon = new ImageView(
                new Image(ToggleUtil.isDarkMode() ? "/Images/light-theme.png" : "/Images/dark-theme.png")
        );
        icon.setFitWidth(20);
        icon.setFitHeight(20);
        icon.setPreserveRatio(true);
        toggleBtn.setGraphic(icon);
    }

    @FXML
    private void handleThemeToggle() {
        Scene scene = saveButton.getScene();
        ToggleUtil.toggleTheme(scene);

        ImageView icon = new ImageView(new Image(ToggleUtil.isDarkMode() ? "/Images/light-theme.png" : "/Images/dark-theme.png"));

        icon.setFitWidth(20);
        icon.setFitHeight(20);
        icon.setPreserveRatio(true);

        toggleBtn.setGraphic(icon);
        updateTagIcon();
    }

    // Update tag button
    private void updateTagIcon() {
        String path = ToggleUtil.isDarkMode()
                ? "/Images/tag-white.png"
                : "/Images/tag-black.png";

        tagIcon.setImage(new Image(path));
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    public void setNotebookDao(JpaNotebookDao notebookDao) {
        this.notebookDao = notebookDao;
    }

    public void setTagDao(JpaTagDao tagDao) {
        this.tagDao = tagDao;
    }

    private String promptForNotebookName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(Localization.get(NEW_NOTEBOOK_KEY));
        dialog.setHeaderText(Localization.get(NEW_NOTEBOOK_KEY));
        dialog.setContentText(Localization.get("create.placeholder_title"));

        if (titleField.getScene() != null) {
            dialog.initOwner(titleField.getScene().getWindow());
        }

        return dialog.showAndWait()
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .orElse(null);
    }

    private void addNotebookToComboBox(NotebookEntity newNotebook) {
        // Remove the "Create new notebook" placeholder(s) by comparing the displayed title using getNotebookTitle
        notebookComboBox.getItems().removeIf(nb -> Localization.get(NEW_NOTEBOOK_KEY).equals(getNotebookTitle(nb)));
        notebookComboBox.getItems().add(newNotebook);

        notebookComboBox.getItems().sort((n1, n2) -> {
            if (n1.getCreatedAt() == null) return -1;
            if (n2.getCreatedAt() == null) return 1;
            return n1.getCreatedAt().compareTo(n2.getCreatedAt());
        });

        NotebookEntity placeholder = new NotebookEntity();
        placeholder.setUser(newNotebook.getUser());

        NotebookTranslationEntity t = new NotebookTranslationEntity();
        t.setLangCode(Localization.getCurrentLanguageCode());
        t.setTitle(Localization.get(NEW_NOTEBOOK_KEY));
        t.setNotebook(placeholder);

        placeholder.addTranslation(t);

        notebookComboBox.getItems().add(placeholder);

        notebookComboBox.getSelectionModel().select(newNotebook);
    }

    // Robust notebook title lookup: case-insensitive keys and fallbacks (mirrors ManageNotebookController logic)
    private String getNotebookTitle(NotebookEntity nb) {
        if (nb == null || nb.getTranslations() == null || nb.getTranslations().isEmpty()) {
            return "";
        }

        String currentCode = Localization.getCurrentLanguageCode();
        String title = findTranslationTitle(nb, currentCode);
        if (!title.isBlank()) {
            return title;
        }

        title = findTranslationTitle(nb, "en");
        if (!title.isBlank()) {
            return title;
        }

        return findAnyTranslationTitle(nb);
    }

    private String findTranslationTitle(NotebookEntity nb, String langCode) {
        if (langCode == null || langCode.isBlank()) {
            return "";
        }

        NotebookTranslationEntity translation = findTranslation(nb, langCode);
        if (translation == null) {
            return "";
        }

        String title = translation.getTitle();
        return title == null || title.isBlank() ? "" : title;
    }

    private NotebookTranslationEntity findTranslation(NotebookEntity nb, String langCode) {
        String code = langCode.trim();

        NotebookTranslationEntity translation = nb.getTranslations().get(code);
        if (translation != null) {
            return translation;
        }

        translation = nb.getTranslations().get(code.toLowerCase());
        if (translation != null) {
            return translation;
        }

        translation = nb.getTranslations().get(code.toUpperCase());
        if (translation != null) {
            return translation;
        }

        for (String key : nb.getTranslations().keySet()) {
            if (key != null && key.equalsIgnoreCase(code)) {
                return nb.getTranslations().get(key);
            }
        }

        return null;
    }

    private String findAnyTranslationTitle(NotebookEntity nb) {
        for (NotebookTranslationEntity t : nb.getTranslations().values()) {
            if (t != null && t.getTitle() != null && !t.getTitle().isBlank()) {
                return t.getTitle();
            }
        }

        return "";
    }
}
