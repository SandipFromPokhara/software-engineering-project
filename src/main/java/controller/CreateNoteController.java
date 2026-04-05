package controller;

import dao.notebook.JpaNotebookDao;
import dao.tag.JpaTagDao;
import entity.entities.NotebookEntity;
import entity.entities.TagEntity;
import entity.entities.UserEntity;
import javafx.event.ActionEvent;
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
import util.bulletList.BulletListStrategy;
import util.bulletList.NumberedListStrategy;
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

    private NoteService noteService;
    private Set<String> selectedTags = new HashSet<>();
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
    private TextArea contentArea;
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

    // Toolbar buttons
    @FXML
    private Button bulletListButton;
    @FXML
    private Button numberedListButton;
    @FXML
    private Button headingUpButton;
    @FXML
    private Button headingDownButton;
    @FXML
    private Tooltip tagTooltip, toggleTooltip;
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

    private UndoRedoManager undoRedoManager = new UndoRedoManager();

    public CreateNoteController() {
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

        WordCountUtil.bind(contentArea, wordCountLabel);

        noteAnnotationLabel.textProperty().bind(Localization.bind("create.note_annotations"));
        noteTagLabel.textProperty().bind(Localization.bind("create.tags"));

        saveButton.textProperty().bind(Localization.bind("create.save"));
        clearButton.textProperty().bind(Localization.bind("create.clear"));

        titleField.promptTextProperty().bind(Localization.bind("create.placeholder_title"));
        contentArea.promptTextProperty().bind(Localization.bind("create.placeholder_content"));
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

        NotebookEntity createNewItem = new NotebookEntity(Localization.get("create.new_notebook"), currentUser);
        notebookComboBox.getItems().add(createNewItem);

        notebookComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(NotebookEntity notebook) {
                if (notebook == null) {
                    return "";
                }
                return notebook.getTitle() == null ? "" : notebook.getTitle();
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
                .sorted(String::compareToIgnoreCase)
                .toList();

        tagComboBox.getItems().setAll(tagNames);

        // Initialize undo/redo manager
        undoRedoManager.initialize(undoMenuItem, redoMenuItem);
        undoRedoManager.registerField("title", titleField);
        undoRedoManager.registerField("content", contentArea);
        undoRedoManager.registerField("annotation", annotationArea);

        // Apply theme once scene is ready
        javafx.application.Platform.runLater(() -> {
            Scene scene = titleField.getScene();
            ToggleUtil.applyTheme(scene);
            updateToggleIcon();
            updateTagIcon();
            WordCountUtil.bind(contentArea, wordCountLabel);
        });
        // Enable list auto-continuation for content area
        TextFormattingUtil.enableListAutoContinuation(contentArea);
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
            String content = contentArea.getText() == null ? "" : contentArea.getText();
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
            if (Localization.get("create.new_notebook").equals(selectedNotebook.getTitle())) {
                String name = promptForNotebookName();
                if (name == null) {
                    showStatus(Localization.get("create.cancelled"), true);
                    return;
                }

                selectedNotebook = noteService.createNotebook(name);
                addNotebookToComboBox(selectedNotebook);
            }

            NoteEntity createdNote = noteService.createNote(title, content, annotation, selectedNotebook, selectedTags);

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
        clearForm();
        statusLabel.setVisible(false);
    }

    @FXML
    private void handleToolbarClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();

        switch (buttonId) {
            case "bulletListButton":
                TextFormattingUtil.toggleList(contentArea, bulletListButton, new BulletListStrategy());
                break;
            case "numberedListButton":
                TextFormattingUtil.toggleList(contentArea, numberedListButton, new NumberedListStrategy());
                break;
            case "headingUpButton":
                TextFormattingUtil.increaseFontSize(contentArea);
                break;
            case "headingDownButton":
                TextFormattingUtil.decreaseFontSize(contentArea);
                break;
        }
    }

    private void clearForm() {
        titleField.clear();
        contentArea.clear();
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
        dialog.setTitle(Localization.get("create.new_notebook"));
        dialog.setHeaderText(Localization.get("create.new_notebook"));
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
        notebookComboBox.getItems().removeIf(nb -> Localization.get("create.new_notebook").equals(nb.getTitle()));
        notebookComboBox.getItems().add(newNotebook);

        notebookComboBox.getItems().sort((n1, n2) -> {
            if (n1.getCreatedAt() == null) return -1;
            if (n2.getCreatedAt() == null) return 1;
            return n1.getCreatedAt().compareTo(n2.getCreatedAt());
        });

        notebookComboBox.getItems().add(new NotebookEntity(Localization.get("create.new_notebook"), newNotebook.getUser()));

        notebookComboBox.getSelectionModel().select(newNotebook);
    }
}