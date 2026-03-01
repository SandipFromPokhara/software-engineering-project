package controller;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import dao.note.JpaNoteDao;
import dao.note.NoteDAO;
import dao.notebook.JpaNoteBookDao;
import dao.tag.JpaTagDao;
import dao.tag.TagDAO;
import entity.*;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import session.NotebookSession;
import util.*;
import session.NoteSession;
import session.UserSession;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewDashboardController {

    static final Logger logger = Logger.getLogger(ViewDashboardController.class.getName());
    private NoteBookEntity activeNotebook;
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
    private Label noteTitleLabel,
            wordCountLabel,
            dbStatusLabel;

    @FXML
    private TextArea noteViewArea,
            annotationViewArea;

    @FXML
    private FlowPane tagFlowpane;

    @FXML
    private Button toggleBtn;

    @FXML
    private Tooltip toggleTooltip;

    @FXML
    private ImageView tagIcon,
            sideBtn1,
            sideBtn2,
            sideBtn3;

    public ViewDashboardController() {
    }

    @FXML
    public void initialize() {
        toggleTooltip.setShowDelay(Duration.millis(100));

        this.noteDao = new JpaNoteDao();
        this.tagDao = new JpaTagDao();

        WordCountUtil.bind(noteViewArea, wordCountLabel);

        UserEntity user = UserSession.getUserInstance().getUser();
        if (user != null) {
            userMenuButton.setText("Welcome, " + user.getFirstName() + " " + user.getLastName());
        }

        rootPane.getStyleClass().add("root");

        // Apply initial theme once scene is available
        javafx.application.Platform.runLater(() -> {
            ToggleUtil.applyTheme(rootPane.getScene());

            // Set toggle button icon correctly on load
            ImageView icon = new ImageView(
                    new Image(ToggleUtil.isDarkMode() ? "/Images/light-theme.png" : "/Images/dark-theme.png")
            );
            icon.setFitWidth(20);
            icon.setFitHeight(20);
            icon.setPreserveRatio(true);
            toggleBtn.setGraphic(icon);
        });

        editButton.setDisable(true);
        deleteButton.setDisable(true);
        noteTitleLabel.setText("Select a note to view");
        noteViewArea.clear();
        annotationViewArea.clear();

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedCreatedTime"));
        notesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        List<NoteBookEntity> notebooks = new JpaNoteBookDao().findByUser(user);

        notebooks.sort((n1, n2) -> {
            if (n1.getCreatedAt() == null) return -1;
            if (n2.getCreatedAt() == null) return 1;
            return n1.getCreatedAt().compareTo(n2.getCreatedAt());
        });

        NoteBookEntity lastCreatedNotebook = NotebookSession.getLastCreatedNotebook();
        NoteBookEntity selectedNotebook;
        if (lastCreatedNotebook != null && notebooks.contains(lastCreatedNotebook)) {
            selectedNotebook = lastCreatedNotebook;
        } else if (!notebooks.isEmpty()) {
            selectedNotebook = notebooks.get(0);
        } else {
            selectedNotebook = null;
        }

        activeNotebook = selectedNotebook;
        if (activeNotebook != null) {
            loadNotes();
        }

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

        setupHover(viewNotesBtn, viewNotesLabel);
        setupHover(createNoteBtn, createNoteLabel);
        setupHover(logoutBtn, logoutLabel);

        dbStatusLabel.setText("● Local Storage Active (MariaDB)");
        dbStatusLabel.setStyle("-fx-text-fill: green;");
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

            notes.sort((n1, n2) -> {
                if (n1.getUpdatedTime() == null) return 1;
                if (n2.getUpdatedTime() == null) return -1;
                return n2.getUpdatedTime().compareTo(n1.getUpdatedTime());
            });

            notesTable.getItems().setAll(notes);

            // Select last created note if exists
            NoteEntity lastCreated = NoteSession.getLastCreatedNote();
            if (lastCreated != null) {
                notesTable.getItems().stream()
                        .filter(n -> n.getId().equals(lastCreated.getId()))
                        .findFirst()
                        .ifPresent(n -> notesTable.getSelectionModel().select(n));
                NoteSession.clear();
            } else if (!notes.isEmpty()) {
                notesTable.getSelectionModel().selectFirst();
            }
            editButton.setDisable(false);
            deleteButton.setDisable(false);
        });

        new Thread(loadNotesTask).start();
    }

    private void refreshTagView(NoteEntity note) {
        tagFlowpane.getChildren().clear();

        note.getTags().stream()
                .sorted((t1, t2) -> t1.getTagName().compareToIgnoreCase(t2.getTagName()))
                .forEach(tag -> {
                    Label tagLabel = new Label("#" + tag.getTagName());
                    tagLabel.getStyleClass().addAll("note-tag", "tag-box");
                    tagFlowpane.getChildren().add(tagLabel);
                });
    }

    @FXML
    private void handleOpenManageNotebook() {
        try {
            Stage owner = (Stage) rootPane.getScene().getWindow();

            NavigationUtil.openWindow(owner, "/FXML/manage_notebooks.fxml", "Manage Notebooks", false, true,
                    (ManageNotebookController controller) -> {
                        controller.loadNotebooks();
                        controller.setActiveNotebook(activeNotebook);
                    });

            NoteBookEntity lastNotebook = NotebookSession.getLastCreatedNotebook();
            if (lastNotebook != null) {
                activeNotebook = lastNotebook;
                NotebookSession.clear();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to open Manage Notebooks window", e);
        }

        loadNotes();
    }

    private void setupHover(Button button, Label label) {
        button.setOnMouseEntered(e ->
                label.setVisible(true));

        button.setOnMouseExited(e ->
                label.setVisible(false));
    }

    @FXML
    private void handleLogout() {
        Window window = rootPane.getScene().getWindow();

        boolean confirmed = AlertUtil.showConfirmation(
                window, "Logout", "Are you sure you want to logout? Any unsaved changes may be lost!"
        );

        if (confirmed) {
            Scene scene = rootPane.getScene();
            scene.getStylesheets().removeIf(s -> s.endsWith("theme.css"));
            scene.getRoot().getStyleClass().removeAll("dark", "light");
            ToggleUtil.setDarkMode(false);

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
        boolean confirmed = AlertUtil.showConfirmation(
                deleteButton.getScene().getWindow(),
                "Delete Note",
                "Delete \"" + selectedNote.getTitle() + "\"?\nThis action cannot be undone. Are you sure?");

        if (confirmed) {
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
                logger.log(Level.SEVERE, "Failed to delete note with ID: " + selectedNote.getId(), e);
                AlertUtil.showError(deleteButton.getScene().getWindow(), "Failed to delete note: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleCreate() {
        Stage owner = (Stage) rootPane.getScene().getWindow();
        NavigationUtil.openWindow(owner, "/FXML/create_note.fxml", "NoteVault - Create Note", true, true, null);
        loadNotes();
    }

    @FXML
    public void handleOpenEditWindow() {
        NoteEntity selectedNote = notesTable.getSelectionModel().getSelectedItem();
        if (selectedNote == null) return;

        Stage stage = (Stage) rootPane.getScene().getWindow();
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
        DialogUtil.showAbout(rootPane.getScene().getWindow());
    }

    @FXML
    public void handleClose() {
        WindowUtil.closeWindow(rootPane);
    }

    @FXML
    public void handleManageAccount() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        NavigationUtil.openWindow(stage, "/FXML/user_dashboard.fxml", "NoteVault - Manage Account", false, true, null);

        UserEntity user = UserSession.getUserInstance().getUser();
        if (user != null) {
            userMenuButton.setText("Welcome, " + user.getFirstName() + " " + user.getLastName());
        }
    }

    @FXML
    public void handleDeleteAccount() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        NavigationUtil.openWindow(stage, "/FXML/delete_user.fxml", "NoteVault - Delete Account", false, true, null);

        if (UserSession.getUserInstance().getUser() == null) {
            NavigationUtil.replaceScene(stage, "/FXML/entry.fxml", "Welcome To NoteVault", false);
        }
    }

    @FXML
    private void handleExport() {
        if (activeNotebook == null) {
            AlertUtil.showWarning(rootPane.getScene().getWindow(), "No notebook selected.");
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
            AlertUtil.showWarning(rootPane.getScene().getWindow(), "No notes available in this notebook.");
            return;
        }
        List<NoteEntity> singleNoteList = List.of(selectedNote);
        exportNotesToPdf(singleNoteList, selectedNote.getTitle());
    }

    private void exportEntireNotebook() {
        List<NoteEntity> notes = noteDao.findByNotebook(activeNotebook); // active one not
        if (notes.isEmpty()) {
            AlertUtil.showWarning(rootPane.getScene().getWindow(), "No notebook selected.");
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
            AlertUtil.showInfo(rootPane.getScene().getWindow(), "Export successful!");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Export failed", e);
            AlertUtil.showError(rootPane.getScene().getWindow(), "Export failed.");
        }
    }

    @FXML
    private void handleThemeToggle() {
        Scene scene = rootPane.getScene();
        ToggleUtil.toggleTheme(scene);

        ImageView icon = new ImageView(new Image(ToggleUtil.isDarkMode() ? "/Images/light-theme.png" : "/Images/dark-theme.png"));

        icon.setFitWidth(20);
        icon.setFitHeight(20);
        icon.setPreserveRatio(true);

        toggleBtn.setGraphic(icon);
        updateIcons();
    }

    // Update side-panel buttons
    private void updateIcons() {
        boolean dark = ToggleUtil.isDarkMode();

        tagIcon.setImage(new Image(dark ? "/Images/tag-white.png" : "/Images/tag-black.png"));
        sideBtn1.setImage(new Image(dark ? "/Images/open-folder-dark.png" : "/Images/open-folder.png"));
        sideBtn2.setImage(new Image(dark ? "/Images/create-file-dark.png" : "/Images/create-file.png"));
        sideBtn3.setImage(new Image(dark ? "/Images/logout-dark.png" : "/Images/logout.png"));
    }

    @FXML
    private void handleOpenFAQ() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        NavigationUtil.<FAQController>openWindow(stage, "/FXML/faq_view.fxml", "Frequently Asked Questions", true, true, controller -> controller.initFaq(false));
    }

    @FXML
    private void handleOpenDataFolder() {
        Window owner = rootPane.getScene().getWindow();
        String title = "Access Local Storage";
        String message = """
                You are about to open your NoteVault database folder.
                
                Please do not move, rename, or delete these files,
                as this will result in data loss. Continue?""";

        boolean confirmed = AlertUtil.showConfirmation(owner, title, message);

        if (confirmed) {
            try {
                String userHome = System.getProperty("user.home");
                File dataDir = new File(userHome, ".NoteVault/data");
                if (!dataDir.exists()) {
                    dataDir.mkdirs();
                }
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(dataDir);
                } else {
                    AlertUtil.showWarning(owner, "Your system does not support opening file folders automatically.");
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to open folder", e);
                AlertUtil.showError(owner, "Failed to open folder: " + e.getMessage());
            }
        }
    }
}