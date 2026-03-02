package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class CreateFilesGuestController {
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

}
