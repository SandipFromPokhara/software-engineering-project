package controller;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import dao.note.JpaNoteDao;
import dao.note.NoteDAO;
import dao.notebook.JpaNoteBookDao;
import dao.notebook.NoteBookDAO;
import dao.tag.JpaTagDao;
import dao.tag.TagDAO;
import entity.*;
import javafx.concurrent.Task;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import session.NotebookSession;
import util.DialogUtil;
import util.NavigationUtil;
import session.NoteSession;
import session.UserSession;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewDashboardController {

    private static final Logger logger = Logger.getLogger(ViewDashboardController.class.getName());
    private NoteBookEntity activeNotebook;
    private NoteBookDAO notebookDao;
    private NoteDAO noteDao;
    private TagDAO tagDao;

    @FXML
    private BorderPane rootPane;

    @FXML
    private MenuButton userMenuButton;

    @FXML
    private Button viewNotesBtn,
            createNoteBtn,
            logoutBtn;

    @FXML
    private Label viewNotesLabel,
            createNoteLabel,
            logoutLabel;

    @FXML
    private Button deleteButton,
            editButton;

    @FXML
    private TableView<NoteEntity> notesTable;

    @FXML
    private TableColumn<NoteEntity, String> titleColumn;

    @FXML
    private TableColumn<NoteEntity, String> dateColumn;

    @FXML
    private Label noteTitleLabel;

    @FXML
    private TextArea noteViewArea,
            annotationViewArea;

    @FXML
    private FlowPane tagFlowpane;

    public ViewDashboardController() {}

    @FXML
    public void initialize() {
        this.notebookDao = new JpaNoteBookDao();
        this.noteDao = new JpaNoteDao();
        this.tagDao = new JpaTagDao();

        UserEntity user = UserSession.getUserInstance().getUser();
        if (user != null) {
            userMenuButton.setText("Welcome, " + user.getFirstName() + " " + user.getLastName());
        }

        loadNotebooks();

        editButton.setDisable(true);
        deleteButton.setDisable(true);
        noteTitleLabel.setText("Select a note to view");
        noteViewArea.clear();
        annotationViewArea.clear();

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedCreatedTime"));

        notesTable.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
            if (newSelection != null) {
                noteTitleLabel.setText(newSelection.getTitle());
                noteViewArea.setText(newSelection.getContent());
                annotationViewArea.setText(newSelection.getAnnotation());

                refreshTagView(newSelection);

                editButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                noteTitleLabel.setText("Select a note to view details");
                noteViewArea.clear();
                annotationViewArea.clear();
                tagFlowpane.getChildren().clear();
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
        notesTable.setColumnResizePolicy(table -> true);

        setupHover(viewNotesBtn, viewNotesLabel);
        setupHover(createNoteBtn, createNoteLabel);
        setupHover(logoutBtn, logoutLabel);
    }

    private void loadNotebooks() {

        UserEntity user = UserSession.getUserInstance().getUser();

        Task<List<NoteBookEntity>> loadNotebooksTask = new Task<>() {
            @Override
            protected List<NoteBookEntity> call() {
                return notebookDao.findByUser(user);
            }
        };

        loadNotebooksTask.setOnSucceeded(e -> {
            List<NoteBookEntity> notebooks = loadNotebooksTask.getValue();

            if (notebooks.isEmpty()) {
                Stage owner = (Stage) rootPane.getScene().getWindow();
                NavigationUtil.openWindow(owner, "/FXML/create_note.fxml", "NoteVault - Create Note", true, true, null);

                loadNotes();
                return;
            }
            NoteBookEntity lastCreated = NotebookSession.getLastCreatedNotebook();

            if (lastCreated != null) {
                for (NoteBookEntity nb : notebooks) {
                    if (nb.getId().equals(lastCreated.getId())) {
                        activeNotebook = nb;
                        break;
                    }
                }
                NotebookSession.clear();
            }

            if (activeNotebook == null && !notebooks.isEmpty()) {
                activeNotebook = notebooks.get(0);
            }

            loadNotes();
        });
        new Thread(loadNotebooksTask).start();
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

    private void refreshTagView(NoteEntity note) {
        tagFlowpane.getChildren().clear();

        note.getTags().stream()
                    .sorted((t1, t2) -> t1.getTagName().compareToIgnoreCase(t2.getTagName()))
                    .forEach(tag -> {
                        Label tagLabel = new Label("#" + tag.getTagName());
                        tagLabel.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 4 8; -fx-background-radius: 10;");
                        tagFlowpane.getChildren().add(tagLabel);
                    });
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
        dialog.initOwner(userMenuButton.getScene().getWindow());

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
        Window window = rootPane.getScene().getWindow();

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Logout");
        confirmDialog.setHeaderText("Are your sure you want to logout?");
        confirmDialog.setContentText("Any unsaved changes may be lost!");
        confirmDialog.initOwner(window);

        ButtonType logout = new ButtonType("Logout");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        confirmDialog.getButtonTypes().setAll(logout, cancel);

        Optional<ButtonType> result = confirmDialog.showAndWait();

        if (result.isPresent() && result.get() == logout) {
            Stage stage = (Stage) window;
            NavigationUtil.replaceScene(stage, "/FXML/entry.fxml", "Welcome To NoteVault", false);
        }
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
                    notesTable.getSelectionModel().selectFirst();
                } else {
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
    public void handleCreate() {
        Stage owner = (Stage) createNoteBtn.getScene().getWindow();
        NavigationUtil.openWindow(owner, "/FXML/create_note.fxml", "NoteVault - Create Note", true, true, null);
        loadNotes();
    }

    @FXML
    public void handleOpenEditWindow() {
        NoteEntity selectedNote = notesTable.getSelectionModel().getSelectedItem();
        if (selectedNote == null) return;

        Stage stage = (Stage) editButton.getScene().getWindow();
        NavigationUtil.openWindow(stage, "/FXML/edit.fxml", "NoteVault - Edit Note", true, true,
                (EditNoteController controller) -> {
                    controller.setNoteDao(noteDao);
                    controller.setTagDao(tagDao);
                    controller.setNote(selectedNote);
                }
        );

        // Refresh table after edit
        loadNotes();
    }

    @FXML
    private void handleAbout() {
        DialogUtil.showAbout(userMenuButton.getScene().getWindow());
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) userMenuButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleManageAccount() {
        // Stage stage = (Stage) userMenuButton.getScene().getWindow();
        // NavigationUtil.openWindow(stage, "/FXML/user_dashboard.fxml", "NoteVault - User Dashboard", true, true, null);
    }

    @FXML
    private void handleExport() {
        if (activeNotebook == null) {
            showWarning("No notebook selected.");
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                "Selected Note",
                "Selected Note",
                "Entire Notebook"
        );
        dialog.setTitle("Export Options");
        dialog.setHeaderText("Choose what to export:");
        dialog.setContentText("Export:");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) return;

        if (result.get().equals("Selected Note")) {
            exportSelectedNote();
        } else {
            exportEntireNotebook();
        }
    }

    private void exportSelectedNote() {
        NoteEntity selectedNote = notesTable.getSelectionModel().getSelectedItem();
        if (selectedNote == null) {
            showWarning("Please select a note to export.");
            return;
        }
        List<NoteEntity> singleNoteList = List.of(selectedNote);
        exportNotesToPdf(singleNoteList, selectedNote.getTitle());
    }

    private void exportEntireNotebook() {
        List<NoteEntity> notes = noteDao.findByNotebook(activeNotebook); // active one not
        if (notes.isEmpty()) {
            showWarning("Notebook is empty.");
            return;
        }
        exportNotesToPdf(notes, activeNotebook.getTitle());
    }

    private void exportNotesToPdf(List<NoteEntity> notes, String fileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export as PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        fileChooser.setInitialFileName(fileName + ".pdf");

        File file = fileChooser.showSaveDialog(
                userMenuButton.getScene().getWindow());

        if (file == null) return;

        try {
            PdfWriter writer = new PdfWriter(file.getAbsolutePath());
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            for (int i = 0; i < notes.size(); i++) {
                NoteEntity note = notes.get(i);
                document.add(new Paragraph(note.getTitle())
                        .setBold()
                        .setFontSize(18));
                document.add(new Paragraph(note.getContent()));

                if (note.getAnnotation() != null &&
                        !note.getAnnotation().isEmpty()) {

                    document.add(new Paragraph("\nAnnotation:")
                            .setBold());

                    document.add(new Paragraph(note.getAnnotation()));
                }

                if (i < notes.size() - 1) {
                    document.add(new AreaBreak());
                }
            }

            document.close();
            showInfo("Export successful!");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Export failed", e);
            showError("Export failed.");
        }
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(userMenuButton.getScene().getWindow());
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(userMenuButton.getScene().getWindow());
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(userMenuButton.getScene().getWindow());
        alert.showAndWait();
    }
}
