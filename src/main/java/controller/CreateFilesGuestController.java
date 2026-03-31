package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import util.Localization;

public class CreateFilesGuestController {
    @FXML
    Label title;
    @FXML
    Label content;
    @FXML
    Label annotation;

    @FXML
    TextField titleBox;

    @FXML
    TextArea contentBox;

    @FXML
    private AnchorPane contentPane;
    @FXML
    TextField annotationBox;

    @FXML
    Button cancelButton;

    @FXML
    void handleCancel(){

        // Clear all input fields
        if (titleBox != null) {
            titleBox.clear();
        }
        if (contentBox != null) {
            contentBox.clear();
        }
        if (annotationBox != null) {
            annotationBox.clear();
        }
    }

    @FXML
    public void initialize() {
        title.textProperty().bind(Localization.bind("note.title_label"));
        titleBox.textProperty().bind(Localization.bind("note.placeholder_title"));
        content.textProperty().bind(Localization.bind("note.content_label"));
        contentBox.textProperty().bind(Localization.bind("note.placeholder_content"));
        annotation.textProperty().bind(Localization.bind("note.annotation_label"));
        annotationBox.textProperty().bind(Localization.bind("note.placeholder_annotation"));
        cancelButton.textProperty().bind(Localization.bind("button.cancel"));
    }
}
