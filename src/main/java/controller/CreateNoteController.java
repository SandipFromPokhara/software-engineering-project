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
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.NoteService;
import entity.NoteEntity;
import session.NotebookSession;
import session.NoteSession;
import session.UserSession;
import util.WordCountUtil;
import util.ToggleUtil;
import util.UndoRedoManager;
import util.TextFormattingUtil;

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

    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private TextArea annotationArea;
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Label statusLabel;
    @FXML private ComboBox<NoteBookEntity> notebookComboBox;
    @FXML private FlowPane tagFlowpane;
    @FXML private ComboBox<String> tagComboBox;
    @FXML private Button addTagBtn;

    // Toolbar buttons
    @FXML private Button bulletListButton;
    @FXML private Button numberedListButton;
    @FXML private Button headingUpButton;
    @FXML private Button headingDownButton;
    @FXML private Tooltip tagTooltip, toggleTooltip;
    @FXML private Button toggleBtn;
    @FXML private ImageView tagIcon;
    @FXML private Label wordCountLabel;

    // Undo/Redo components
    @FXML private MenuItem undoMenuItem;
    @FXML private MenuItem redoMenuItem;

    private UndoRedoManager undoRedoManager = new UndoRedoManager();

    public CreateNoteController() {}

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Tooltip delay
        tagTooltip.setShowDelay(Duration.millis(100));
        toggleTooltip.setShowDelay(Duration.millis(100));

        // Load current user and notebooks
        UserEntity currentUser = UserSession.getUserInstance().getUser();
        List<NoteBookEntity> notebooks = new JpaNoteBookDao().findByUser(currentUser);

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
                if (notebook == null) { return ""; }
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

        noteService = new NoteService();
        statusLabel.setVisible(false);
        saveButton.setDisable(true);

        // Disable save button if title is empty or ComboBox has no selection
        titleField.textProperty().addListener((obs, old, newVal) ->  updateSaveButton());
        notebookComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> updateSaveButton());

        // Load existing tags from database
        List<TagEntity> allTags = new JpaTagDao().findAll();
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
        NoteBookEntity selectedNotebook = notebookComboBox.getSelectionModel().getSelectedItem();

        if (selectedNotebook == null) {
            showStatus("Please select a notebook", true);
            return;
        }

        try {
            String title = titleField.getText().trim();
            String content = contentArea.getText() == null ? "" : contentArea.getText();
            String annotation = annotationArea.getText() == null ? "" : annotationArea.getText();

            if (title.isEmpty()) {
                showStatus("Please enter a note title", true);
                return;
            }

            if (CREATE_NEW.equals(selectedNotebook.getTitle())) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("New Notebook");
                dialog.setHeaderText("Create a new notebook");
                dialog.setContentText("Enter notebook name:");
                dialog.initOwner(titleField.getScene().getWindow());

                selectedNotebook = dialog.showAndWait()
                        .map(String::trim)
                        .filter(name -> !name.isEmpty())
                        .map(name -> {
                            NoteBookEntity newNotebook = new NoteBookEntity(name, UserSession.getUserInstance().getUser());
                            newNotebook = new JpaNoteBookDao().save(newNotebook);
                            NotebookSession.setLastCreatedNotebook(newNotebook);
                            // Add new notebook to ComboBox before "Create New"
                            notebookComboBox.getItems().removeIf(nb -> CREATE_NEW.equals(nb.getTitle()));
                            notebookComboBox.getItems().add(newNotebook);

                            notebookComboBox.getItems().sort((n1, n2) -> {
                                if (n1.getCreatedAt() == null) return -1;
                                if (n2.getCreatedAt() == null) return 1;
                                return n1.getCreatedAt().compareTo(n2.getCreatedAt());
                            });

                            NoteBookEntity createNewItem = new NoteBookEntity(CREATE_NEW, UserSession.getUserInstance().getUser());
                            notebookComboBox.getItems().add(createNewItem);

                            notebookComboBox.getSelectionModel().select(newNotebook);
                            return newNotebook;
                        }).orElse(null);

                if (selectedNotebook == null) {
                    showStatus("Notebook creation cancelled", true);
                    return;
                }
            }

            NoteEntity note = new NoteEntity(title.trim(), content, annotation);
            note.setNotebook(selectedNotebook);

            JpaTagDao tagDao = new JpaTagDao();

            for (String tagName : selectedTags) {
                TagEntity tag = tagDao.findByName(tagName);

                if (tag == null) {
                    tag = new TagEntity(tagName);
                    tag = tagDao.save(tag);
                }
                note.addTag(tag);
            }

            NoteEntity createdNote = noteService.save(note);
            NoteSession.setLastCreatedNote(createdNote);
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

        System.out.println("Button clicked: " + buttonId);
        switch (buttonId) {
            case "bulletListButton":
                TextFormattingUtil.toggleBulletList(contentArea, bulletListButton);
                break;
            case "numberedListButton":
                TextFormattingUtil.toggleNumberedList(contentArea, numberedListButton);
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
        tagFlowpane.getChildren().clear();
        titleField.requestFocus();

        // Clear undo/redo history
        undoRedoManager.clear();
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setTextFill(isError ? Color.RED : Color.GREEN);
        statusLabel.setVisible(true);
    }

    private void closeCurrentWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleBackToHome() {
        closeCurrentWindow();
    }

    @FXML
    private void handleClose() {
        closeCurrentWindow();
    }

    @FXML
    private void handleAddTag() {
        String tagName = tagComboBox.getEditor().getText();

        if (tagName == null || tagName.isBlank()) {
            return;
        }

        tagName = tagName.trim();

        int maxLength = 15;
        if (tagName.length() > maxLength) {
            showStatus("Tag too long! Max " + maxLength + " characters allowed", true);
            return;
        }

        if (!tagName.matches("[a-zA-ZäöåÄÖÅ0-9_-]+")) {
            showStatus("Invalid characters in tag", true);
            return;
        }

        if (selectedTags.contains(tagName)) {
            return;
        }

        selectedTags.add(tagName);
        if (!tagComboBox.getItems().contains(tagName)) {
            tagComboBox.getItems().add(tagName);
        }
        addTagToFlowPane(tagName);

        tagComboBox.getEditor().clear();
    }

    private void addTagToFlowPane(String tagName) {
        HBox tagBox = new HBox();
        tagBox.setSpacing(5);
        tagBox.getStyleClass().addAll("note-tag", "tag-box");

        Label label = new Label("#" + tagName);
        label.getStyleClass().add("tag-label");

        Button removeBtn = new Button("x");
        removeBtn.getStyleClass().add("tag-remove-btn");
        removeBtn.setOnAction(e -> {
            selectedTags.remove(tagName);
            tagFlowpane.getChildren().remove(tagBox);
        });

        tagBox.getChildren().addAll(label, removeBtn);
        tagFlowpane.getChildren().add(tagBox);
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
}