package controller;

import dao.note.NoteDAO;
import dao.tag.TagDAO;
import entity.entities.NoteEntity;
import entity.entities.TagEntity;
import entity.translationentities.NoteTranslationEntity;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;
import services.NoteService;
import services.TranslationService;
import util.*;
import util.bulletList.*;

import java.util.HashSet;
import java.util.Set;

import static model.LanguageModel.DEFAULT_LANGUAGE_CODE;

public class EditNoteController {

    private NoteDAO noteDao;
    private TagDAO tagDao;
    private NoteEntity note;
    Set<String> selectedTags = new HashSet<>();
    private TranslationService translationService;
    private NoteService noteService;

    @FXML
    private Label title;

    @FXML
    private TextField titleField;

    @FXML
    private Label content;

    @FXML
    private TextArea contentBox;

    @FXML
    private Label annotation;

    @FXML
    private TextField annotationBox;

    @FXML
    private Button updateButton;

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

    // set translationService
    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    public void initialize() {
        // LOCALIZATION BINDINGS
        title.textProperty().bind(Localization.bind("edit.title_label"));
        editMenu.textProperty().bind(Localization.bind("edit.menu"));
        undoMenuItem.textProperty().bind(Localization.bind("edit.undo"));
        redoMenuItem.textProperty().bind(Localization.bind("edit.redo"));

        content.textProperty().bind(Localization.bind("edit.content_label"));
        contentBox.promptTextProperty().bind(Localization.bind("edit.content_box"));
        annotation.textProperty().bind(Localization.bind("edit.annotations"));
        editTags.textProperty().bind(Localization.bind("edit.tags"));

        tagTooltip.textProperty().bind(Localization.bind("tooltip.tags_info"));
        updateButton.textProperty().bind(Localization.bind("edit.update"));
        cancelButton.textProperty().bind(Localization.bind("button.cancel"));

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
        Platform.runLater(() -> {
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

        String lang = Localization.getCurrentLanguageCode();

        tagComboBox.getItems().addAll(
                tagDao.findAll().stream()
                        .map(TagEntity::getTagName)
                        .filter(java.util.Objects::nonNull)
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .toList()
        );
    }

    public void setNote(NoteEntity note) {
        this.note = note;

        String langCode = Localization.getCurrentLanguageCode();

        NoteTranslationEntity translation =
                translationService.getTranslation(note, langCode, DEFAULT_LANGUAGE_CODE);

        if (translation != null) {
            titleField.setText(translation.getTitle());
            contentBox.setText(translation.getContent());
            annotationBox.setText(translation.getAnnotation());
        } else {
            titleField.clear();
            contentBox.clear();
            annotationBox.clear();
        }

        selectedTags.clear();
        if (note.getTags() != null) {
            note.getTags().forEach(tag -> {
                String tagName = tag.getTagName();

                if (tagName != null) {
                    selectedTags.add(tagName);
                }
            });
        }
        refreshTagFlowPane();
    }

    @FXML
    private void handleCancel() {
        WindowUtil.closeWindow(updateButton);
    }

    @FXML
    public void handleUpdate() {
        String langCode = Localization.getCurrentLanguageCode();

        noteService.updateNote(
                note,
                langCode,
                titleField.getText(),
                contentBox.getText(),
                annotationBox.getText(),
                selectedTags
        );

        handleCancel();
    }

    private void refreshTagFlowPane() {
        TagUtil.refreshFlowPane(selectedTags, tagFlowpane);
    }

    @FXML
   public  void handleAddTag() {
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