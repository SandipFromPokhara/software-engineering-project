package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.NoteModel;

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



    private NoteModel note;

    public void setNote(NoteModel note){
        this.note = note;

        titleBox.setText(note.getTitle());
        contentBox.setText(note.getContent());
        annotationBox.setText(note.getAnnotation());

    }

    @FXML
    private void handleUpdate(){
        if (note==null){
            return;
        }
        note.updateNote(
                titleBox.getText(),
                contentBox.getText(),
                annotationBox.getText()
        );

        System.out.println(note); //just for test

        //close window
        Stage stage = (Stage) updateButton.getScene().getWindow();
        stage.close();

    }

    @FXML
    private void handleCancel(){
        //close window
        System.out.println(" edit is cancelled");
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();

    }
}
