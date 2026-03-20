package controller;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import dao.note.JpaNoteDao;
import dao.tag.JpaTagDao;
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
import services.DashboardService;
import session.NotebookSession;
import util.*;
import session.UserSession;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import util.events.EventBus;
import util.events.NoteCreatedEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewDashboardController {

    static final Logger logger = Logger.getLogger(ViewDashboardController.class.getName());

    private NoteBookEntity activeNotebook;
    private DashboardService dashboardService = new DashboardService();

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
        WordCountUtil.bind(noteViewArea, wordCountLabel);

        UserEntity user = UserSession.getUserInstance().getUser();
        if (user != null) {
            userMenuButton.setText("Welcome, " + user.getFirstName() + " " + user.getLastName());
        }

        rootPane.getStyleClass().add("root");
        setupTheme();

        editButton.setDisable(true);
        deleteButton.setDisable(true);
        noteTitleLabel.setText("Select a note to view");
        noteViewArea.clear();
        annotationViewArea.clear();

        setupTableColumns();
        setupHoverEffects();
        setupEventBusSubscription();

        // Determine initial notebook
        activeNotebook = dashboardService.getInitialNotebook();
        if (activeNotebook != null) loadNotes();

        dbStatusLabel.setText("● Local Storage Active (MariaDB)");
        dbStatusLabel.setStyle("-fx-text-fill: green;");
    }

    // ---------- TABLE & NOTES ----------

    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedCreatedTime"));
        notesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        notesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayNote(newSelection);
            } else clearNoteDisplay();
        });
    }

    private void loadNotes() {
        if (activeNotebook == null) return;

        Task<List<NoteEntity>> loadNotesTask = new Task<>() {
            @Override
            protected List<NoteEntity> call() {
                return dashboardService.loadNotes(activeNotebook);
            }
        };

        loadNotesTask.setOnSucceeded(e -> {
            List<NoteEntity> notes = loadNotesTask.getValue();
            notesTable.getItems().setAll(notes);

            // Auto-select latest updated note
            if (!notes.isEmpty()) notesTable.getSelectionModel().select(0);
        });

        new Thread(loadNotesTask).start();
    }

    private void displayNote(NoteEntity note) {
        noteTitleLabel.setText(note.getTitle());
        noteViewArea.setText(note.getContent());
        annotationViewArea.setText(note.getAnnotation());
        refreshTagView(note);
        editButton.setDisable(false);
        deleteButton.setDisable(false);
    }

    private void clearNoteDisplay() {
        noteTitleLabel.setText("Select a note to view details");
        noteViewArea.clear();
        annotationViewArea.clear();
        tagFlowpane.getChildren().clear();
        editButton.setDisable(true);
        deleteButton.setDisable(true);
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

    // ---------- EVENT BUS ----------

    private void setupEventBusSubscription() {
        EventBus.subscribe(event -> {
            if (event instanceof NoteCreatedEvent e) {
                loadNotes();
                javafx.application.Platform.runLater(() ->
                        notesTable.getItems().stream()
                                .filter(n -> n.getId().equals(e.getNote().getId()))
                                .findFirst()
                                .ifPresent(n -> notesTable.getSelectionModel().select(n))
                );
            }
        });
    }

    // ---------- UI & THEME ----------

    private void setupTheme() {
        javafx.application.Platform.runLater(() -> {
            ToggleUtil.applyTheme(rootPane.getScene());
            ImageView icon = new ImageView(
                    new Image(ToggleUtil.isDarkMode() ? "/Images/light-theme.png" : "/Images/dark-theme.png")
            );
            icon.setFitWidth(20);
            icon.setFitHeight(20);
            icon.setPreserveRatio(true);
            toggleBtn.setGraphic(icon);
            updateIcons();
        });
    }

    private void setupHover(Button button, Label label) {
        button.setOnMouseEntered(e -> label.setVisible(true));

        button.setOnMouseExited(e -> label.setVisible(false));
    }

    private void setupHoverEffects() {
        setupHover(viewNotesBtn, viewNotesLabel);
        setupHover(createNoteBtn, createNoteLabel);
        setupHover(logoutBtn, logoutLabel);
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

    // ---------- BUTTON ACTIONS ----------

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
                dashboardService.deleteNote(selectedNote);

                // Remove from TableView
                notesTable.getItems().remove(selectedNote);

                // Select first note if any
                if (!notesTable.getItems().isEmpty()) {
                    notesTable.getSelectionModel().selectFirst();
                } else {
                    clearNoteDisplay();
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
    }

    @FXML
    public void handleOpenEditWindow() {
        NoteEntity selectedNote = notesTable.getSelectionModel().getSelectedItem();
        if (selectedNote == null) return;

        Stage stage = (Stage) rootPane.getScene().getWindow();
        NavigationUtil.openWindow(stage, "/FXML/edit.fxml", "NoteVault - Edit Note", true, true,
                (EditNoteController controller) -> {
                    controller.setNoteDao(new JpaNoteDao());
                    controller.setTagDao(new JpaTagDao());
                    controller.setNote(selectedNote);
                }
        );

        loadNotes();    // Refresh table after edit
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
        List<NoteEntity> notes = dashboardService.loadNotes(activeNotebook); // active one not
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