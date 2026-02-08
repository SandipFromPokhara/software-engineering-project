package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class CreateFilesGuestController {
    @FXML
    private TextField titleBox;

    @FXML
    private TextArea contentBox;

    @FXML
    private AnchorPane contentPane;

    @FXML
    private TextField annotationBox;

    @FXML
    private Button cancelButton;

    @FXML
    private void handleCancel(){

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
