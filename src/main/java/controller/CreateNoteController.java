package controller;

import dao.notebook.JpaNoteBookDao;
import entity.NoteBookEntity;
import entity.UserEntity;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import services.NoteService;
import entity.NoteEntity;
import util.NavigationUtil;
import util.NoteSession;
import util.UserSession;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Create Note - handles save and clear operations
 */
public class CreateNoteController implements Initializable {

    private static final String CREATE_NEW = "Create New Notebook...";

    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private TextArea annotationArea;
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Label statusLabel;
    @FXML private ComboBox<NoteBookEntity> notebookComboBox;

    private NoteService noteService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UserEntity currentUser = UserSession.getUserInstance().getUser();
        List<NoteBookEntity> notebooks = new JpaNoteBookDao().findByUser(currentUser);

        notebooks.sort((n1, n2) -> {
            if (n1.getCreatedAt() == null) return -1;
            if (n2.getCreatedAt() == null) return 1;
            return n1.getCreatedAt().compareTo(n2.getCreatedAt());
        });

        notebookComboBox.getItems().setAll(notebooks);

        NoteBookEntity createNewItem = new NoteBookEntity(CREATE_NEW, currentUser);
        notebookComboBox.getItems().add(createNewItem);

        notebookComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(NoteBookEntity notebook) {
                if (notebook == null) return "";
                return notebook.getTitle() == null ? "" : notebook.getTitle();
            }

            @Override
            public NoteBookEntity fromString(String string) {
                return null;
            }
        });

        // Select first notebook if available
        if (!notebookComboBox.getItems().isEmpty()) {
            notebookComboBox.getSelectionModel().select(0);
        }

        noteService = new NoteService();
        statusLabel.setVisible(false);
        saveButton.setDisable(true);

        // Disable save button if title is empty or ComboBox has no selection
        titleField.textProperty().addListener((obs, old, newVal) ->  updateSaveButton());
        notebookComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> updateSaveButton());
    }

    private void updateSaveButton() {
        NoteBookEntity selected = notebookComboBox.getSelectionModel().getSelectedItem();
        boolean disable = titleField.getText().trim().isEmpty() || selected == null;
        saveButton.setDisable(disable);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        NoteBookEntity selectedNotebook = notebookComboBox.getSelectionModel().getSelectedItem();

        if (selectedNotebook == null) {
            showStatus("Please select a notebook", true);
            return;
        }

        try {
            String title = titleField.getText().trim();
            String content = contentArea.getText() == null ? "" : contentArea.getText();
            String annotation = annotationArea.getText() == null ? "" : annotationArea.getText();

            if (title.isEmpty()) {
                showStatus("Please enter a note title", true);
                return;
            }

            if (CREATE_NEW.equals(selectedNotebook.getTitle())) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("New Notebook");
                dialog.setHeaderText("Create a new notebook");
                dialog.setContentText("Enter notebook name:");
                dialog.initOwner(titleField.getScene().getWindow());

                selectedNotebook = dialog.showAndWait()
                        .map(String::trim)
                        .filter(name -> !name.isEmpty())
                        .map(name -> {
                            NoteBookEntity newNotebook = new NoteBookEntity(name, UserSession.getUserInstance().getUser());
                            newNotebook = new JpaNoteBookDao().save(newNotebook);
                            // Add new notebook to ComboBox before "Create New"
                            notebookComboBox.getItems().removeIf(nb -> CREATE_NEW.equals(nb.getTitle()));
                            notebookComboBox.getItems().add(newNotebook);

                            notebookComboBox.getItems().sort((n1, n2) -> {
                                if (n1.getCreatedAt() == null) return -1;
                                if (n2.getCreatedAt() == null) return 1;
                                return n1.getCreatedAt().compareTo(n2.getCreatedAt());
                            });

                            NoteBookEntity createNewItem = new NoteBookEntity(CREATE_NEW, UserSession.getUserInstance().getUser());
                            notebookComboBox.getItems().add(createNewItem);

                            notebookComboBox.getSelectionModel().select(newNotebook);
                            return newNotebook;
                        }).orElse(null);

                if (selectedNotebook == null) {
                    showStatus("Notebook creation cancelled", true);
                    return;
                }
            }

            NoteEntity createdNote = noteService.createNote(title, content, annotation, selectedNotebook);
            NoteSession.setLastCreatedNote(createdNote);
            showStatus("Note saved successfully!", false);

            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.close();
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
