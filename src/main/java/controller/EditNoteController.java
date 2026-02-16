package controller;

import dao.note.NoteDAO;
import dao.tag.TagDAO;
import entity.NoteEntity;
import entity.TagEntity;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.UndoRedoManager;

import java.util.HashSet;
import java.util.Set;

public class EditNoteController {

    private NoteDAO noteDao;
    private TagDAO tagDao;
    private NoteEntity note;
    private Set<String> selectedTags = new HashSet<>();

    @FXML
    private Label title;
    @FXML
    TextField titleBox;
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
    private Label statusLabel;

    @FXML
    private FlowPane tagFlowpane;

    @FXML
    private ComboBox<String> tagComboBox;

    @FXML
    private Button addTagBtn;

    @FXML
    private Tooltip tagTooltip;

    // Undo/Redo components
    @FXML private MenuItem undoMenuItem;
    @FXML private MenuItem redoMenuItem;

    private UndoRedoManager undoRedoManager = new UndoRedoManager();

    public void setNoteDao(NoteDAO noteDao) {
        this.noteDao = noteDao;
    }

    public void setTagDao(TagDAO tagDao) {
        this.tagDao = tagDao;
        loadTags();
    }

    public void initialize() {
        tagTooltip.setShowDelay(Duration.millis(100));
        tagComboBox.setEditable(true);

        // Initialize undo/redo manager
        undoRedoManager.initialize(undoMenuItem, redoMenuItem);
        undoRedoManager.registerField("title", titleBox);
        undoRedoManager.registerField("content", contentBox);
        undoRedoManager.registerField("annotation", annotationBox);
    }

    @FXML
    private void handleUndo() {
        undoRedoManager.undo();
    }

    @FXML
    private void handleRedo() {
        undoRedoManager.redo();
    }

    private void loadTags() {
        tagComboBox.getItems().clear();

        tagComboBox.getItems().addAll(tagDao.findAll()
                .stream()
                .map(TagEntity::getTagName)
                .sorted(String::compareToIgnoreCase).toList()
        );
    }

    public void setNote(NoteEntity note){
        this.note = note;
        titleBox.setText(note.getTitle());
        contentBox.setText(note.getContent());
        annotationBox.setText(note.getAnnotation());

        selectedTags.clear();
        note.getTags().forEach(tag -> selectedTags.add(tag.getTagName()));
        refreshTagFlowPane();
    }

    @FXML
    void handleUpdate(){
        if (noteDao == null || tagDao == null || note == null) {
            showStatus("Internal error. Please reopen edit window", true);
            return;
        }

        note.setTitle(titleBox.getText());
        note.setContent(contentBox.getText());
        note.setAnnotation(annotationBox.getText());

        // Clear old tags first
        for (TagEntity tag: new HashSet<>(note.getTags())) {
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
        close();
    }

    @FXML
    private void handleCancel(){
        close();
    }

    private void close() {
        Stage stage = (Stage) updateButton.getScene().getWindow();
        stage.close();
    }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setTextFill(isError ? Color.RED : Color.GREEN);
        statusLabel.setVisible(true);
    }

    private void refreshTagFlowPane() {
        tagFlowpane.getChildren().clear();
        selectedTags.stream()
                .sorted(String::compareToIgnoreCase)
                .forEach(tagName -> {
                    HBox tagBox = new HBox();
                    tagBox.setSpacing(5);
                    tagBox.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 4 8 4 8; -fx-background-radius: 10;");

                    Label label = new Label("#" + tagName);

                    Button removeBtn = new Button("x");
                    removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: red;");
                    removeBtn.setOnAction(e -> {
                        selectedTags.remove(tagName);
                        refreshTagFlowPane();
                    });

                    tagBox.getChildren().addAll(label, removeBtn);
                    tagFlowpane.getChildren().add(tagBox);
                });
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
            showStatus("Invalid characters in tag.", true);
            return;
        }

        if (selectedTags.contains(tagName)) {
            return;
        }

        selectedTags.add(tagName);
        if (!tagComboBox.getItems().contains(tagName)) {
            tagComboBox.getItems().add(tagName);
        }
        refreshTagFlowPane();
        tagComboBox.getEditor().clear();
    }
}