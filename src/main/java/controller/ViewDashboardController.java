package controller;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import dao.note.JpaNoteDao;
import dao.note.NoteDAO;
import dao.tag.JpaTagDao;
import dao.tag.TagDAO;
import entity.entities.NoteEntity;
import entity.entities.NotebookEntity;
import entity.entities.UserEntity;
import entity.translationentities.NoteTranslationEntity;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.util.StringConverter;
import model.LanguageModel;
import model.LanguageModel.Language;
import services.DashboardService;
import services.NoteService;
import services.TranslationService;
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

    private NotebookEntity activeNotebook;

    // shared instances
    private DashboardService dashboardService = new DashboardService();
    private final NoteService noteService = new NoteService();
    private final TranslationService translationService = new TranslationService();
    private final NoteDAO noteDao = new JpaNoteDao();
    private final TagDAO tagDao = new JpaTagDao();

    @FXML
    private BorderPane rootPane;

    @FXML
    private MenuButton userMenuButton;

    // userMenu button
    @FXML
    private MenuItem manageAccountItem, deleteAccountItem, logoutItem;

    // For File and Help option
    @FXML
    private Menu fileMenu, helpMenu;

    @FXML
    private MenuItem newNoteItem, exportItem, closeItem;

    @FXML
    private MenuItem faqItem, aboutItem;

    @FXML
    private Button openData;

    @FXML
    private Button viewNotesBtn,
            createNoteBtn,
            logoutBtn;

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
    private Label annotation;

    @FXML
    private TextArea noteViewArea,
            annotationViewArea;

    @FXML
    private FlowPane tagFlowpane;

    @FXML
    private Button toggleBtn;

    @FXML
    private Tooltip toggleTooltip, langTooltip, manageTooltip, createTooltip, logoutTooltip;

    @FXML
    private ImageView tagIcon,
            sideBtn1,
            sideBtn2,
            sideBtn3;

    @FXML
    private ComboBox<String> languageCombo;

    @FXML
    private Label languageIconLabel;

    public ViewDashboardController() {}

    @FXML
    public void initialize() {

        // LOCALIZATION BINDINGS
        fileMenu.textProperty().bind(Localization.bind("menu.file"));
        newNoteItem.textProperty().bind(Localization.bind("menu.newNote"));
        exportItem.textProperty().bind(Localization.bind("menu.exportPdf"));
        closeItem.textProperty().bind(Localization.bind("menu.close"));

        toggleTooltip.textProperty().bind(Localization.bind("tooltip.theme_toggle"));
        langTooltip.textProperty().bind(Localization.bind("tooltip.lang_info"));

        helpMenu.textProperty().bind(Localization.bind("menu.help"));
        faqItem.textProperty().bind(Localization.bind("logged.faq"));
        aboutItem.textProperty().bind(Localization.bind("menu.about"));

        manageTooltip.textProperty().bind(Localization.bind("notebook.manage_label"));
        createTooltip.textProperty().bind(Localization.bind("note.create_label"));
        logoutTooltip.textProperty().bind(Localization.bind("button.logout"));

        titleColumn.textProperty().bind(Localization.bind("note.title"));
        dateColumn.textProperty().bind(Localization.bind("note.date"));

        editButton.textProperty().bind(Localization.bind("note.edit"));
        deleteButton.textProperty().bind(Localization.bind("button.delete"));

        annotation.textProperty().bind(Localization.bind("dashboard.annotations"));

        dbStatusLabel.textProperty().bind(Localization.bind("dashboard.dbStatus"));
        openData.textProperty().bind(Localization.bind("dashboard.openData"));

        manageAccountItem.textProperty().bind(Localization.bind("user.manage_account"));
        deleteAccountItem.textProperty().bind(Localization.bind("user.delete_account"));
        logoutItem.textProperty().bind(Localization.bind("button.logout"));

        // Localize Welcome user display
        UserEntity user = UserSession.getUserInstance().getUser();
        if(user != null) {
            String username = user.getFirstName()+ " " +user.getLastName();

            userMenuButton.textProperty().bind(
                    Bindings.createStringBinding(
                            () -> Localization.get("dashboard.welcomeButton", username),
                            Localization.localeProperty()
                    )
            );
        }

        // Localize default note-title label when nothing is selected
        noteTitleLabel.textProperty().bind(Localization.bind("dashboard.selectNote"));

        // --- Language ComboBox setup ---
        setupLanguageCombo();

        toggleTooltip.setShowDelay(Duration.millis(100));
        langTooltip.setShowDelay(Duration.millis(100));
        manageTooltip.setShowDelay(Duration.millis(100));
        createTooltip.setShowDelay(Duration.millis(100));
        logoutTooltip.setShowDelay(Duration.millis(100));

        WordCountUtil.bind(noteViewArea, wordCountLabel);

        rootPane.getStyleClass().add("root");
        setupTheme();

        editButton.setDisable(true);
        deleteButton.setDisable(true);

        setupTableColumns();
        setupEventBusSubscription();

        // Determine initial notebook
        activeNotebook = dashboardService.getInitialNotebook();
        if (activeNotebook != null) loadNotes();
    }

    private void setupLanguageCombo() {
        languageCombo.getItems().addAll(LanguageModel.LANGUAGES.keySet());

        // Closed state: show code, color depends on theme
        languageCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String code, boolean empty) {
                super.updateItem(code, empty);
                if (empty || code == null) {
                    setText("");
                } else {
                    setText(code);
                }
                // Always transparent background, text color matches theme
                String color = ToggleUtil.isDarkMode() ? "white" : "#266973";
                setStyle("-fx-background-color: transparent; " +
                        "-fx-text-fill: " + color + "; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 11px;");
            }
        });

        // Open state: show full name, always teal on white
        languageCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String code, boolean empty) {
                super.updateItem(code, empty);
                if (empty || code == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(LanguageModel.LANGUAGES.get(code).fullName()
                            + "  (" + LanguageModel.LANGUAGES.get(code).nativeName() + ")");
                    // CSS handle this
                    setStyle("");
                }
            }
        });

        languageCombo.setConverter(new StringConverter<>() {
            @Override public String toString(String code) { return code == null ? "" : code; }
            @Override public String fromString(String s) { return s; }
        });

        Language current = LanguageModel.getByLocale(Localization.getLocale());
        languageCombo.setValue(current.code());

        languageCombo.setOnAction(e -> {
            String selected = languageCombo.getValue();
            if (selected != null) {
                Localization.setLocale(LanguageModel.LANGUAGES.get(selected).locale());
                // Refresh button cell color after locale/theme change
                languageCombo.setButtonCell(new ListCell<>() {
                    @Override
                    protected void updateItem(String code, boolean empty) {
                        super.updateItem(code, empty);
                        if (empty || code == null) {
                            setText("");
                        } else {
                            setText(code);
                        }
                        String color = ToggleUtil.isDarkMode() ? "white" : "#266973";
                        setStyle("-fx-background-color: transparent; " +
                                "-fx-text-fill: " + color + "; " +
                                "-fx-font-weight: bold; " +
                                "-fx-font-size: 11px;");
                    }
                });
            }
        });
    }

    // ---------- TABLE & NOTES ----------

    private void setupTableColumns() {
        titleColumn.setCellValueFactory(cellData -> {
            NoteTranslationEntity t = dashboardService.getDisplayTranslation(cellData.getValue());

            return new SimpleStringProperty(
                    t != null ? t.getTitle() : ""
            );
        });
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
        noteTitleLabel.textProperty().unbind(); // important so we can show the concrete title

        NoteTranslationEntity t = dashboardService.getDisplayTranslation(note);

        if (t == null) {
            logger.warning("Missing translation for note: " + note.getId());

            noteTitleLabel.setText("");
            noteViewArea.clear();
            annotationViewArea.clear();

            return;
        }

        noteTitleLabel.setText(t.getTitle());
        noteViewArea.setText(t.getContent());
        annotationViewArea.setText(t.getAnnotation());
        refreshTagView(note);
        editButton.setDisable(false);
        deleteButton.setDisable(false);
    }

    private void clearNoteDisplay() {
        // Return to the localized "select note" message and keep it reactive to language changes
        noteTitleLabel.textProperty().unbind();
        noteTitleLabel.textProperty().bind(Localization.bind("dashboard.selectNote"));
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
        Platform.runLater(() -> {
            ToggleUtil.applyTheme(rootPane.getScene());
            ImageView icon = new ImageView(
                    new Image(ToggleUtil.isDarkMode() ? "/Images/light-theme.png" : "/Images/dark-theme.png")
            );
            icon.setFitWidth(20);
            icon.setFitHeight(20);
            icon.setPreserveRatio(true);
            toggleBtn.setGraphic(icon);
            updateIcons();

            // Update globe label color for current theme
            updateLanguageIconColor();
        });
    }

    private void updateLanguageIconColor() {
        if (languageIconLabel != null) {
            String globeColor = ToggleUtil.isDarkMode() ? "white" : "#266973";
            languageIconLabel.setStyle("-fx-font-size: 12px; -fx-padding: 3 2 3 6; -fx-text-fill: " + globeColor + ";");
        }

        // Also update the combo's displayed text color to match the theme
        if (languageCombo != null && languageCombo.getButtonCell() != null) {
            String textColor = ToggleUtil.isDarkMode() ? "white" : "#266973";
            languageCombo.getButtonCell().setStyle(
                    "-fx-text-fill: " + textColor + "; -fx-font-weight: bold; " +
                            "-fx-font-size: 11px; -fx-background-color: transparent;");
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
        updateLanguageIconColor(); // keep globe label in sync when toggling
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

            NavigationUtil.openWindow(owner, "/FXML/manage_notebooks.fxml",
                    "notebook.manage_label", false, true,
                    (ManageNotebookController controller) -> {
                        controller.loadNotebooks();
                        controller.setActiveNotebook(activeNotebook);
                    });

            NotebookEntity lastNotebook = NotebookSession.getLastCreatedNotebook();
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
                window,
                Localization.get("button.logout"),
                Localization.get("account.logout_warning")
        );

        if (confirmed) {
            Scene scene = rootPane.getScene();
            scene.getStylesheets().removeIf(s -> s.endsWith("theme.css"));
            scene.getRoot().getStyleClass().removeAll("dark", "light");
            ToggleUtil.setDarkMode(false);

            Stage stage = (Stage) window;
            // Use localized window title instead of hard-coded English string
            NavigationUtil.replaceScene(stage, "/FXML/entry.fxml", "entry.window_title", false);
        }
    }

    @FXML
    public void handleDelete() {
        NoteEntity selectedNote = notesTable.getSelectionModel().getSelectedItem();
        if (selectedNote == null) {
            return;
        }

        Window owner = deleteButton.getScene().getWindow();

        String title = Localization.get("delete.window_title");

        String message = Localization.get("delete_note.confirm", title);

        boolean confirmed = AlertUtil.showConfirmation(owner, title, message);

        if (confirmed) {
            try {
                dashboardService.deleteNote(selectedNote);
                notesTable.getItems().remove(selectedNote);

                if (!notesTable.getItems().isEmpty()) {
                    notesTable.getSelectionModel().selectFirst();
                } else {
                    clearNoteDisplay();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to delete note with ID: " + selectedNote.getId(), e);
                AlertUtil.showError(owner, Localization.get("delete.failed"));
            }
        }
    }

    @FXML
    public void handleCreate() {
        Stage owner = (Stage) rootPane.getScene().getWindow();
        NavigationUtil.openWindow(owner, "/FXML/create_note.fxml", "create.window_title", true, true, null);
    }

    @FXML
    public void handleOpenEditWindow() {
        NoteEntity selectedNote = notesTable.getSelectionModel().getSelectedItem();
        if (selectedNote == null) return;

        Stage stage = (Stage) rootPane.getScene().getWindow();
        NavigationUtil.openWindow(stage, "/FXML/edit.fxml", "edit.window.title", true, true,
                (EditNoteController controller) -> {
                    controller.setNoteDao(noteDao);
                    controller.setTagDao(tagDao);
                    controller.setTranslationService(translationService);
                    controller.setNoteService(noteService);
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
        NavigationUtil.openWindow(stage, "/FXML/user_dashboard.fxml", "account.window_title", false, true, null);
    }

    @FXML
    public void handleDeleteAccount() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        NavigationUtil.openWindow(stage, "/FXML/delete_user.fxml", "account.delete_window_title", false, true, null);

        if (UserSession.getUserInstance().getUser() == null) {
            NavigationUtil.replaceScene(stage, "/FXML/entry.fxml", "entry.window_title", false);
        }
    }

    @FXML
    private void handleExport() {
        if (activeNotebook == null) {
            AlertUtil.showWarning(rootPane.getScene().getWindow(), Localization.get("export.no_notebook"));
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                Localization.get("export.dialog.option.selected"),
                Localization.get("export.dialog.option.selected"),
                Localization.get("export.dialog.option.notebook")
        );

        dialog.setTitle(Localization.get("export.dialog.title"));
        dialog.setHeaderText(Localization.get("export.dialog.header"));
        dialog.setContentText(Localization.get("export.dialog.label"));

        ButtonType ok = new ButtonType(Localization.get("button.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType(Localization.get("button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().setAll(ok, cancel);


        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        if (result.get().equals(Localization.get("export.dialog.option.selected"))) {
            exportSelectedNote();
        } else {
            exportEntireNotebook();
        }
    }

    private void exportSelectedNote() {
        NoteEntity selectedNote = notesTable.getSelectionModel().getSelectedItem();
        if (selectedNote == null) {
            AlertUtil.showWarning(rootPane.getScene().getWindow(),Localization.get("export.no_notes"));
            return;
        }
        List<NoteEntity> singleNoteList = List.of(selectedNote);

        NoteTranslationEntity t = dashboardService.getDisplayTranslation(selectedNote);

        exportNotesToPdf(singleNoteList, t.getTitle());
    }

    private void exportEntireNotebook() {
        List<NoteEntity> notes = dashboardService.loadNotes(activeNotebook); // active one
        if (notes.isEmpty()) {
            AlertUtil.showWarning(rootPane.getScene().getWindow(),Localization.get("export.no_notebook"));
            return;
        }
        exportNotesToPdf(notes, activeNotebook.getTitle());
    }

    private void exportNotesToPdf(List<NoteEntity> notes, String fileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Localization.get("menu.exportPdf"));
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
                NoteTranslationEntity t = dashboardService.getDisplayTranslation(note);

                document.add(new Paragraph(t.getTitle())
                        .setBold()
                        .setFontSize(18));
                document.add(new Paragraph(t.getContent()));

                if (t.getAnnotation() != null &&
                        !t.getAnnotation().isEmpty()) {

                    document.add(new Paragraph("\nAnnotation:")
                            .setBold());

                    document.add(new Paragraph(t.getAnnotation()));
                }

                if (i < notes.size() - 1) {
                    document.add(new AreaBreak());
                }
            }

            document.close();
            AlertUtil.showInfo(rootPane.getScene().getWindow(),Localization.get("export.success"));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Export failed", e);
            AlertUtil.showError(rootPane.getScene().getWindow(), Localization.get("export.failed"));
        }
    }

    @FXML
    private void handleOpenFAQ() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        NavigationUtil.<FAQController>openWindow(stage, "/FXML/faq_view.fxml", "faq.window_title", true, true, controller -> controller.initFaq(false));
    }

    @FXML
    private void handleOpenDataFolder() {
        Window owner = rootPane.getScene().getWindow();

        String title = Localization.get("dashboard.folder.title");
        String message = Localization.get("dashboard.folder.message");

        boolean confirmed = AlertUtil.showConfirmation(owner, title, message);

        if (confirmed) {
            try {
                String userHome = System.getProperty("user.home");
                File dataDir = new File(userHome, ".NoteVault/data");
                if (!dataDir.exists()) {
                    boolean created = dataDir.mkdirs();
                    if (!created) {
                        logger.warning("Failed to create data directory: " + dataDir.getAbsolutePath());
                        AlertUtil.showError(owner, Localization.get("dashboard.folder.error_create"));
                        return;
                    }
                }
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(dataDir);
                } else {
                    AlertUtil.showWarning(owner, Localization.get("dashboard.folder.warning"));
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to open folder", e);
                AlertUtil.showError(owner, Localization.get("dashboard.folder.error", e.getMessage()));
            }
        }
    }
}