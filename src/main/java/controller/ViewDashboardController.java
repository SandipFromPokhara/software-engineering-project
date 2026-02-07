package controller;

import dao.note.JpaNoteDao;
import dao.notebook.JpaNoteBookDao;
import entity.NoteBookEntity;
import entity.NoteEntity;
import entity.UserEntity;
import javafx.application.Platform;
import javafx.concurrent.Task;
import util.NavigationUtil;
import util.UserSession;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class ViewDashboardController {

    private NoteBookEntity activeNotebook;
    private JpaNoteBookDao notebookDao;
    private JpaNoteDao noteDao;

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
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        this.notebookDao = new JpaNoteBookDao();
        this.noteDao = new JpaNoteDao();

        UserEntity user = UserSession.getUserInstance().getUser();
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getUsername());
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
        noteTitleLabel.setText("Select a note to view details");
        noteViewArea.setText("");

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        notesTable.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
            if (newSelection != null) {
                noteTitleLabel.setText(newSelection.getTitle());
                noteViewArea.setText(newSelection.getContent());

                editButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                noteTitleLabel.setText("Select a note to view details");
                noteViewArea.clear();
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
        notesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

            if (notesTable != null) {
                notesTable.getItems().setAll(notes);
                noteTitleLabel.setText("Select a note to view details");
                noteViewArea.clear();
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
        NavigationUtil.navigateTo(event, "/FXML/entry.fxml", "Welcome", false);
    }

    @FXML
    public void handleDelete(ActionEvent event) {
    }

    @FXML
    public void handleCreate(ActionEvent event) {
        NavigationUtil.navigateTo(event, "/FXML/create_note.fxml", "NoteVault - Create Note", true);
    }

    @FXML
    public void handleOpenEditWindow(ActionEvent event) {
        NoteEntity selectedNote = notesTable.getSelectionModel().getSelectedItem();
        if (selectedNote == null) return;

        Stage editStage = new Stage();
        editStage.initModality(Modality.APPLICATION_MODAL);

        NavigationUtil.navigateTo(editStage, "/FXML/edit.fxml", "NoteVault - Edit Note", true);
        editStage.showAndWait();
    }
}
