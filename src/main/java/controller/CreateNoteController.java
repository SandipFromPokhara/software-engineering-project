package controller;

import dao.notebook.JpaNoteBookDao;
import dao.tag.JpaTagDao;
import entity.NoteBookEntity;
import entity.TagEntity;
import entity.UserEntity;
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
import entity.NoteEntity;
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

    private static final String CREATE_NEW = "Create New Notebook...";
    private NoteService noteService;
    private Set<String> selectedTags = new HashSet<>();
    private JpaNoteBookDao notebookDao;
    private JpaTagDao tagDao;

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
    private ComboBox<NoteBookEntity> notebookComboBox;
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

        // Tooltip delay
        tagTooltip.setShowDelay(Duration.millis(100));
        toggleTooltip.setShowDelay(Duration.millis(100));

        // Load current user and notebooks
        UserEntity currentUser = UserSession.getUserInstance().getUser();
        List<NoteBookEntity> notebooks = (notebookDao != null ? notebookDao : new JpaNoteBookDao()).findByUser(currentUser);

        notebooks.sort((n1, n2) -> {
            if (n1.getCreatedAt() == null) return -1;
            if (n2.getCreatedAt() == null) return 1;
            return n1.getCreatedAt().compareTo(n2.getCreatedAt());
        });

        notebookComboBox.getItems().setAll(notebooks);

        NoteBookEntity createNewItem = new NoteBookEntity(CREATE_NEW, currentUser);
        notebookComboBox.getItems().add(createNewItem);

        notebookComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(NoteBookEntity notebook) {
                if (notebook == null) {
                    return "";
                }
                return notebook.getTitle() == null ? "" : notebook.getTitle();
            }

            @Override
            public NoteBookEntity fromString(String string) {
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
        NoteBookEntity selected = notebookComboBox.getSelectionModel().getSelectedItem();
        boolean disable = titleField.getText() == null || titleField.getText().isBlank() || selected == null;
        saveButton.setDisable(disable);
    }

    @FXML
    private void handleSave() {
        try {
            String title = titleField.getText().trim();
            String content = contentArea.getText() == null ? "" : contentArea.getText();
            String annotation = annotationArea.getText() == null ? "" : annotationArea.getText();

            NoteBookEntity selectedNotebook = notebookComboBox.getSelectionModel().getSelectedItem();

            if (selectedNotebook == null) {
                showStatus("Please select a notebook", true);
                return;
            }

            if (title.isEmpty()) {
                showStatus("Please enter a note title", true);
                return;
            }

            // Handle "Create New Notebook"
            if (CREATE_NEW.equals(selectedNotebook.getTitle())) {
                String name = promptForNotebookName();
                if (name == null) {
                    showStatus("Notebook creation cancelled", true);
                    return;
                }

                selectedNotebook = noteService.createNotebook(name);
                addNotebookToComboBox(selectedNotebook);
            }

            NoteEntity createdNote = noteService.createNote(title, content, annotation, selectedNotebook, selectedTags);

            EventBus.publish(new NoteCreatedEvent(createdNote));

            showStatus("Note saved successfully!", false);

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

    public void setNotebookDao(JpaNoteBookDao notebookDao) {
        this.notebookDao = notebookDao;
    }

    public void setTagDao(JpaTagDao tagDao) {
        this.tagDao = tagDao;
    }

    private String promptForNotebookName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Notebook");
        dialog.setHeaderText("Create a new notebook");
        dialog.setContentText("Enter notebook name:");
        dialog.initOwner(titleField.getScene().getWindow());

        return dialog.showAndWait()
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .orElse(null);
    }

    private void addNotebookToComboBox(NoteBookEntity newNotebook) {
        notebookComboBox.getItems().removeIf(nb -> CREATE_NEW.equals(nb.getTitle()));
        notebookComboBox.getItems().add(newNotebook);

        notebookComboBox.getItems().sort((n1, n2) ->
                n1.getCreatedAt().compareTo(n2.getCreatedAt())
        );

        notebookComboBox.getItems().add(new NoteBookEntity(CREATE_NEW, newNotebook.getUser()));

        notebookComboBox.getSelectionModel().select(newNotebook);
    }
}