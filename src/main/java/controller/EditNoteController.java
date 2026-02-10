package controller;

import dao.note.JpaNoteDao;
import dao.note.NoteDAO;
import entity.NoteEntity;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditNoteController {

    private NoteDAO noteDao;

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

    private NoteDAO noteDAO = new JpaNoteDao();
    private NoteEntity note;

    public void setNoteDao(NoteDAO noteDao) {
        this.noteDao = noteDao;
    }

    public void setNote(NoteEntity note){
        this.note = note;
        titleBox.setText(note.getTitle());
        contentBox.setText(note.getContent());
        annotationBox.setText(note.getAnnotation());
    }

    @FXML
    void handleUpdate(){
        note.setTitle(titleBox.getText());
        note.setContent(contentBox.getText());
        note.setAnnotation(annotationBox.getText());
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

}
