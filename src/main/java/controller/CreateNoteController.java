package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import services.NoteService;
import entity.NoteEntity;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for Create Note functionality
 * Simple note creation with title, content, annotation and save/clear buttons
 */
public class CreateNoteController implements Initializable {

    // FXML UI Components
    @FXML
    private TextField titleField;

    @FXML
    private TextArea contentArea;

    @FXML
    private TextArea annotationArea;

    @FXML
    private Button saveButton;

    @FXML
    private Button clearButton;

    @FXML
    private Label statusLabel;

    // Service layer
    private NoteService noteService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Create Note Controller initialized");

        // Initialize services
        noteService = new NoteService();

        // Initialize UI
        setupUI();
        addValidationListeners();
    }

    private void setupUI() {
        // Initially hide status label
        statusLabel.setVisible(false);

        // Initially disable save button
        saveButton.setDisable(true);

        System.out.println("UI components initialized");
    }

    private void addValidationListeners() {
        // Enable save button only when title is provided
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean hasTitle = newValue != null && !newValue.trim().isEmpty();
            saveButton.setDisable(!hasTitle);
        });
    }

    @FXML
    private void handleSave(ActionEvent event) {
        try {
            // Get form data
            String title = titleField.getText().trim();
            String content = contentArea.getText();
            String annotation = annotationArea.getText();

            // Validate
            if (title.isEmpty()) {
                showStatus("Please enter a note title", true);
                return;
            }

            // Combine content and annotation for storage
            String fullContent = content;
            if (annotation != null && !annotation.trim().isEmpty()) {
                fullContent = content + "\n\n--- Annotations ---\n" + annotation;
            }

            // Use default notebook ID (1)
            Long defaultNotebookId = 1L;

            // Create note using service
            NoteEntity createdNote = noteService.createNote(title, fullContent, defaultNotebookId);

            // Show success message
            showStatus("Note '" + createdNote.getTitle() + "' saved successfully!", false);

            // Clear form
            clearForm();

            System.out.println("Note created and saved: " + createdNote.getTitle() + " (ID: " + createdNote.getId() + ")");

        } catch (Exception e) {
            showStatus("Error creating note: " + e.getMessage(), true);
            System.err.println("Error creating note: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
        showStatus("", false);
        System.out.println("Form cleared");
    }

    private void clearForm() {
        titleField.clear();
        contentArea.clear();
        annotationArea.clear();
        statusLabel.setVisible(false);
        titleField.requestFocus();
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setTextFill(isError ? Color.RED : Color.GREEN);
        statusLabel.setVisible(!message.isEmpty());
    }
}
