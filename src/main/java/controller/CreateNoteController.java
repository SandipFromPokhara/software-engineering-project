package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import services.NoteService;
import entity.NoteEntity;
import util.NavigationUtil;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for Create Note - handles save and clear operations
 */
public class CreateNoteController implements Initializable {

    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private TextArea annotationArea;
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Label statusLabel;

    private NoteService noteService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        noteService = new NoteService();
        statusLabel.setVisible(false);
        saveButton.setDisable(true);

        // Enable save button only when title is provided
        titleField.textProperty().addListener((obs, old, newVal) ->
            saveButton.setDisable(newVal == null || newVal.trim().isEmpty()));
    }

    @FXML
    private void handleSave(ActionEvent event) {
        try {
            String title = titleField.getText().trim();
            String content = contentArea.getText();
            String annotation = annotationArea.getText();

            if (title.isEmpty()) {
                showStatus("Please enter a note title", true);
                return;
            }

            // Create note with content and annotation
            NoteEntity createdNote = noteService.createNote(title, content, annotation);

            showStatus("Note saved successfully!", false);
            clearForm();

        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
        statusLabel.setVisible(false);
    }

    private void clearForm() {
        titleField.clear();
        contentArea.clear();
        annotationArea.clear();
        titleField.requestFocus();
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setTextFill(isError ? Color.RED : Color.GREEN);
        statusLabel.setVisible(true);
    }

    @FXML
    private void handleBackToHome(ActionEvent event) {
        Stage stage = (Stage) titleField.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/FXML/view_dashboard.fxml", "NoteVault - Dashboard", true);
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}
