package controller;

import entity.NoteEntity;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditNoteController {
    @FXML
    private Label title;

    @FXML
    private TextField titleBox;

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



    private NoteEntity note;

    public void setNote(NoteEntity note){
        this.note = note;

        titleBox.setText(note.getTitle());
        contentBox.setText(note.getContent());
        annotationBox.setText(note.getAnnotation());

    }

    @FXML
    private void handleUpdate(){
        note.setTitle(titleBox.getText());
        note.setContent(contentBox.getText());
        note.setAnnotation(annotationBox.getText());
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

}
