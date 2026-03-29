package controller;

import dao.note.NoteDAO;
import dao.tag.TagDAO;
import entity.NoteEntity;
import entity.TagEntity;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.*;
import util.bulletList.BulletListStrategy;
import util.bulletList.NumberedListStrategy;
import util.bulletList.TextFormattingUtil;

import java.util.HashSet;
import java.util.Set;

public class EditNoteController {

    private NoteDAO noteDao;
    private TagDAO tagDao;
    private NoteEntity note;
    Set<String> selectedTags = new HashSet<>();

    @FXML
    private Label title;

    @FXML
    TextField titleField;

    @FXML
    private Label content;

    @FXML
    TextArea contentBox;

    @FXML
    private Label annotation;

    @FXML
    TextField annotationBox;

    @FXML
    Button updateButton;

    @FXML
    private Button cancelButton;

    @FXML
    Label statusLabel;

    @FXML
    FlowPane tagFlowpane;

    @FXML
    ComboBox<String> tagComboBox;

    @FXML
    private Button addTagBtn;

    @FXML
    private Tooltip tagTooltip;

    @FXML
    private ImageView tagIcon;

    @FXML
    private Label wordCountLabel;

    // Undo/Redo components
    @FXML
    private MenuItem undoMenuItem;
    @FXML
    private MenuItem redoMenuItem;

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
    private Label editTags;
    @FXML
    private Menu editMenu;

    private UndoRedoManager undoRedoManager = new UndoRedoManager();

    public void setNoteDao(NoteDAO noteDao) {
        this.noteDao = noteDao;
    }

    public void setTagDao(TagDAO tagDao) {
        this.tagDao = tagDao;
        loadTags();
    }

    public void initialize() {
        // LOCALIZATION BINDINGS
        title.textProperty().bind(Localization.bind("edit.title_label"));
        editMenu.textProperty().bind(Localization.bind("edit.menu"));
        undoMenuItem.textProperty().bind(Localization.bind("edit.undo"));
        redoMenuItem.textProperty().bind(Localization.bind("edit.redo"));

        content.textProperty().bind(Localization.bind("edit.content_label"));
        annotation.textProperty().bind(Localization.bind("edit.annotations"));
        editTags.textProperty().bind(Localization.bind("edit.tags"));

        tagTooltip.textProperty().bind(Localization.bind("tooltip.tags_info"));
        updateButton.textProperty().bind(Localization.bind("edit.update"));
        cancelButton.textProperty().bind(Localization.bind("edit.cancel"));

        annotationBox.promptTextProperty().bind(Localization.bind("edit.placeholder_annotations"));
        tagComboBox.promptTextProperty().bind(Localization.bind("edit.placeholder_tags"));
        addTagBtn.textProperty().bind(Localization.bind("edit.add_tags"));

        WordCountUtil.bind(contentBox, wordCountLabel);

        tagTooltip.setShowDelay(Duration.millis(100));
        tagComboBox.setEditable(true);

        // Initialize undo/redo manager
        undoRedoManager.initialize(undoMenuItem, redoMenuItem);
        undoRedoManager.registerField("title", titleField);
        undoRedoManager.registerField("content", contentBox);
        undoRedoManager.registerField("annotation", annotationBox);

        // Apply theme once scene is ready
        javafx.application.Platform.runLater(() -> {
            Scene scene = titleField.getScene();
            if (scene != null) {
                ToggleUtil.applyTheme(scene);
                updateTagIcon();
            }
        });
        // Enable list auto-continuation for content box
        TextFormattingUtil.enableListAutoContinuation(contentBox);
    }

    @FXML
    private void handleUndo() {
        undoRedoManager.undo();
    }

    @FXML
    private void handleRedo() {
        undoRedoManager.redo();
    }

    @FXML
    private void handleToolbarClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();

        switch (buttonId) {
            case "bulletListButton":
                TextFormattingUtil.toggleList(contentBox, bulletListButton, new BulletListStrategy());
                break;
            case "numberedListButton":
                TextFormattingUtil.toggleList(contentBox, numberedListButton, new NumberedListStrategy());
                break;
            case "headingUpButton":
                TextFormattingUtil.increaseFontSize(contentBox);
                break;
            case "headingDownButton":
                TextFormattingUtil.decreaseFontSize(contentBox);
                break;
        }
    }

    private void loadTags() {
        tagComboBox.getItems().clear();

        tagComboBox.getItems().addAll(tagDao.findAll()
                .stream()
                .map(TagEntity::getTagName)
                .sorted(String::compareToIgnoreCase).toList()
        );
    }

    public void setNote(NoteEntity note) {
        this.note = note;
        titleField.setText(note.getTitle());
        contentBox.setText(note.getContent());
        annotationBox.setText(note.getAnnotation());

        selectedTags.clear();
        note.getTags().forEach(tag -> selectedTags.add(tag.getTagName()));
        refreshTagFlowPane();
    }

    @FXML
    void handleUpdate() {
        if (noteDao == null || tagDao == null || note == null) {
            showStatus("Internal error. Please reopen edit window", true);
            return;
        }

        note.setTitle(titleField.getText());
        note.setContent(contentBox.getText());
        note.setAnnotation(annotationBox.getText());

        // Clear old tags first
        for (TagEntity tag : new HashSet<>(note.getTags())) {
            note.removeTag(tag);
        }

        // Add current selected tags
        for (String tagName : selectedTags) {
            TagEntity tag = tagDao.findByName(tagName);
            if (tag == null) {
                tag = new TagEntity(tagName);
                tag = tagDao.save(tag);
            }
            note.addTag(tag);
        }

        noteDao.save(note);
        handleCancel();
    }

    @FXML
    private void handleCancel() {
        WindowUtil.closeWindow(updateButton);
    }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setTextFill(isError ? Color.RED : Color.GREEN);
        statusLabel.setVisible(true);
    }

    private void refreshTagFlowPane() {
        TagUtil.refreshFlowPane(selectedTags, tagFlowpane);
    }

    @FXML
    void handleAddTag() {
        String tagName = tagComboBox.getEditor().getText();
        TagUtil.addTagToUI(selectedTags, tagFlowpane, tagComboBox, tagName);
    }

    // Update tag button
    private void updateTagIcon() {
        String path = ToggleUtil.isDarkMode()
                ? "/Images/tag-white.png"
                : "/Images/tag-black.png";

        tagIcon.setImage(new Image(path));
    }
}