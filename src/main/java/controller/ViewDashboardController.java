package controller;

import dao.note.JpaNoteDao;
import dao.notebook.JpaNoteBookDao;
import entity.*;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import util.DialogUtil;
import util.NavigationUtil;
import util.NoteSession;
import util.UserSession;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ViewDashboardController {

    private NoteBookEntity activeNotebook;
    private JpaNoteBookDao notebookDao;
    private JpaNoteDao noteDao;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button viewNotesBtn, createNoteBtn, settingsBtn, logoutBtn;

    @FXML
    private Label viewNotesLabel, createNoteLabel, settingsLabel, logoutLabel;

    @FXML
    private Button deleteButton;

    @FXML
    private Button editButton;

    @FXML
    private TableView<NoteEntity> notesTable;

    @FXML
    private TableColumn<NoteEntity, String> titleColumn;

    @FXML
    private TableColumn<NoteEntity, String> dateColumn;

    @FXML
    private Label noteTitleLabel;

    @FXML
    private TextArea noteViewArea;

    @FXML
    private TextArea annotationViewArea;

    @FXML
    public void initialize() {
        this.notebookDao = new JpaNoteBookDao();
        this.noteDao = new JpaNoteDao();

        UserEntity user = UserSession.getUserInstance().getUser();
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getFirstName());
        }

        Task<List<NoteBookEntity>> loadNotebooksTask = new Task<>() {
            @Override
            protected List<NoteBookEntity> call() {
                return notebookDao.findByUser(user);
            }
        };

        loadNotebooksTask.setOnSucceeded(e -> {
            List<NoteBookEntity> notebooks = loadNotebooksTask.getValue();
            if (welcomeLabel != null && welcomeLabel.getScene() != null && welcomeLabel.getScene().getWindow() != null) {
                Stage stage = (Stage) welcomeLabel.getScene().getWindow();
                if (notebooks.isEmpty()) {
                    NavigationUtil.navigateTo(stage, "/FXML/create_note.fxml", "NoteVault - Create Note", true);
                } else {
                    activeNotebook = notebooks.get(0);
                    loadNotes();
                }
            }
        });

        new Thread(loadNotebooksTask).start();

        editButton.setDisable(true);
        deleteButton.setDisable(true);
        noteTitleLabel.setText("Select a note to view");
        noteViewArea.setText("");

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedCreatedTime"));

        notesTable.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
            if (newSelection != null) {
                noteTitleLabel.setText(newSelection.getTitle());
                noteViewArea.setText(newSelection.getContent());
                annotationViewArea.setText(newSelection.getAnnotation());

                editButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                noteTitleLabel.setText("Select a note to view details");
                noteViewArea.clear();
                annotationViewArea.clear();
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
        notesTable.setColumnResizePolicy(table -> true);

        setupHover(viewNotesBtn, viewNotesLabel);
        setupHover(createNoteBtn, createNoteLabel);
        setupHover(settingsBtn, settingsLabel);
        setupHover(logoutBtn, logoutLabel);
    }

    private void loadNotes() {
        if (activeNotebook == null) return;

        Task<List<NoteEntity>> loadNotesTask = new Task<>() {
            @Override
            protected List<NoteEntity> call() {
                return noteDao.findByNotebook(activeNotebook);
            }
        };

        loadNotesTask.setOnSucceeded(e -> {
            List<NoteEntity> notes = loadNotesTask.getValue();

            notesTable.getItems().setAll(notes);

            // notesTable.setItems(FXCollections.observableArrayList(notes));

            if (!notes.isEmpty()) {
                notesTable.getSelectionModel().selectFirst();
            }

            NoteEntity lastCreated = NoteSession.getLastCreatedNote();

            if (lastCreated != null) {
                for (NoteEntity n : notes) {
                    if (n.getId().equals(lastCreated.getId())) {
                        notesTable.getSelectionModel().select(n);
                        break;
                    }
                }
                NoteSession.clear();
            }

            if (notesTable.getSelectionModel().getSelectedItem() == null) {
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        new Thread(loadNotesTask).start();
    }

    @FXML
    private void handleOpen() {
        UserEntity currentUser = UserSession.getUserInstance().getUser();
        List<NoteBookEntity> notebooksList = notebookDao.findByUser(currentUser);
        if (notebooksList.isEmpty()) return;

        ChoiceDialog<NoteBookEntity> dialog = new ChoiceDialog<>(activeNotebook, notebooksList);
        dialog.setTitle("Open Notebook");
        dialog.setHeaderText("Select a notebook to open");
        dialog.setContentText("Available notebooks:");
        dialog.initOwner(welcomeLabel.getScene().getWindow());

        Optional<NoteBookEntity> result = dialog.showAndWait();
        if (result.isPresent()) {
            activeNotebook = result.get();
            loadNotes();
        }
    }

    private void setupHover(Button button, Label label) {
        button.setOnMouseEntered(e -> {
            label.setVisible(true);
            button.setStyle("-fx-background-color: #93ad9b; -fx-cursor: hand;");
        });

        button.setOnMouseExited(e -> {
            label.setVisible(false);
            button.setStyle("-fx-background-color: transparent");
        });
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Stage currentStage = (Stage) logoutBtn.getScene().getWindow();

        Stage entryStage = new Stage();
        NavigationUtil.navigateTo(entryStage, "/FXML/entry.fxml", "Welcome To NoteVault", false);

        currentStage.close();
    }

    @FXML
    public void handleDelete() {
        // Get the selected note
        NoteEntity selectedNote = notesTable.getSelectionModel().getSelectedItem();

        if (selectedNote == null) {
            return;
        }

        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Note");
        confirmDialog.setHeaderText("Delete \"" + selectedNote.getTitle() + "\"?");
        confirmDialog.setContentText("This action cannot be undone. Are you sure you want to delete this note?");
        confirmDialog.initOwner(deleteButton.getScene().getWindow());

        Optional<ButtonType> result = confirmDialog.showAndWait();

        // If user confirms deletion
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete from database
                noteDao.delete(selectedNote);

                // Remove from TableView
                notesTable.getItems().remove(selectedNote);

                // Select first note if any
                if (!notesTable.getItems().isEmpty()) {
                    notesTable.getSelectionModel().selectFirst()
;                } else {
                    noteTitleLabel.setText("Select a note to view");
                    noteViewArea.clear();
                    annotationViewArea.clear();
                    editButton.setDisable(true);
                    deleteButton.setDisable(true);
                }
            } catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Failed to delete note");
                errorAlert.setContentText("An error occurred: " + e.getMessage());
                errorAlert.initOwner(deleteButton.getScene().getWindow());
                errorAlert.showAndWait();

                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleCreate(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/create_note.fxml"));
            Parent root = loader.load();

            Stage createStage = new Stage();
            createStage.initModality(Modality.APPLICATION_MODAL);
            createStage.setTitle("NoteVault - Create Note");
            createStage.setScene(new Scene(root));
            createStage.showAndWait();
            loadNotes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleOpenEditWindow() {
        NoteEntity selectedNote = notesTable.getSelectionModel().getSelectedItem();
        if (selectedNote == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/FXML/edit.fxml")
            );

            Parent root = loader.load();

            // Get controller and pass note
            EditNoteController controller = loader.getController();
            controller.setNote(selectedNote);

            Stage editStage = new Stage();
            editStage.initModality(Modality.APPLICATION_MODAL);
            editStage.setTitle("NoteVault - Edit Note");
            editStage.setScene(new Scene(root));
            editStage.showAndWait();

            // Refresh table after edit
            loadNotes();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAbout() {
        DialogUtil.showAbout(welcomeLabel.getScene().getWindow());
    }

    @FXML
    public void handleClose(ActionEvent actionEvent) {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        stage.close();
    }
}
