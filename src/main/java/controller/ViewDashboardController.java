package controller;

import dao.note.*;
import dao.tag.*;
import entity.entities.*;
import entity.translationentities.NoteTranslationEntity;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Window;
import javafx.util.Duration;
import services.*;
import util.*;
import session.UserSession;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import util.events.EventBus;
import util.events.NoteCreatedEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewDashboardController {

    static final Logger logger = Logger.getLogger(ViewDashboardController.class.getName());
    private static final String LOGOUT_KEY = "button.logout";
    private static final String EXPORT_SELECTED = "export.dialog.option.selected";

    private NotebookEntity activeNotebook;

    // shared instances
    private final DashboardService dashboardService = new DashboardService();
    private final PdfExportService exportService = new PdfExportService(dashboardService);
    private final NoteService noteService = new NoteService();
    private final TranslationService translationService = new TranslationService();
    private final INoteDAO noteDao = new JpaNoteDao();
    private final ITagDAO tagDao = new JpaTagDao();

    @FXML private BorderPane rootPane;

    @FXML private MenuButton userMenuButton;

    @FXML private MenuItem manageAccountItem;

    @FXML private MenuItem deleteAccountItem;

    @FXML private MenuItem logoutItem;

    @FXML private Menu fileMenu;

    @FXML private Menu helpMenu;

    @FXML private MenuItem newNoteItem;

    @FXML private MenuItem exportItem;

    @FXML private MenuItem closeItem;

    @FXML private MenuItem faqItem;

    @FXML private MenuItem aboutItem;

    @FXML private Button openData;

    @FXML private Button deleteButton;

    @FXML private Button editButton;

    @FXML private TableView<NoteEntity> notesTable;

    @FXML private TableColumn<NoteEntity, String> titleColumn;

    @FXML private TableColumn<NoteEntity, String> dateColumn;

    @FXML private Label noteTitleLabel;

    @FXML private Label wordCountLabel;

    @FXML private Label dbStatusLabel;

    @FXML private Label annotation;

    @FXML private TextArea noteViewArea;

    @FXML private TextArea annotationViewArea;

    @FXML private FlowPane tagFlowpane;

    @FXML private Button toggleBtn;

    @FXML private AnchorPane editorPane;

    @FXML private Tooltip toggleTooltip;

    @FXML private Tooltip langTooltip;

    @FXML private Tooltip manageTooltip;

    @FXML private Tooltip createTooltip;

    @FXML private Tooltip logoutTooltip;

    @FXML private ImageView tagIcon;

    @FXML private ImageView sideBtn1;

    @FXML private ImageView sideBtn2;

    @FXML private ImageView sideBtn3;

    @FXML private ComboBox<String> languageCombo;

    @FXML private Label languageIconLabel;

    @FXML private SplitPane mainSplit;

    @FXML
    public void initialize() {
        initLocalization();
        initUserMenu();
        setupLanguageCombo();
        initTooltips();
        initTheme();
        initTable();
        setupEventBusSubscription();
        initSplitPane();
        loadInitialData();
    }

    private void initLocalization() {
        // LOCALIZATION BINDINGS
        fileMenu.textProperty().bind(Localization.bind("menu.file"));
        newNoteItem.textProperty().bind(Localization.bind("menu.newNote"));
        exportItem.textProperty().bind(Localization.bind("menu.exportPdf"));
        closeItem.textProperty().bind(Localization.bind("menu.close"));

        helpMenu.textProperty().bind(Localization.bind("menu.help"));
        faqItem.textProperty().bind(Localization.bind("logged.faq"));
        aboutItem.textProperty().bind(Localization.bind("menu.about"));

        manageAccountItem.textProperty().bind(Localization.bind("user.manage_account"));
        deleteAccountItem.textProperty().bind(Localization.bind("user.delete_account"));
        logoutItem.textProperty().bind(Localization.bind(LOGOUT_KEY));

        titleColumn.textProperty().bind(Localization.bind("note.title"));
        dateColumn.textProperty().bind(Localization.bind("note.date"));

        editButton.textProperty().bind(Localization.bind("note.edit"));
        deleteButton.textProperty().bind(Localization.bind("button.delete"));

        annotation.textProperty().bind(Localization.bind("dashboard.annotations"));
        dbStatusLabel.textProperty().bind(Localization.bind("dashboard.dbStatus"));
        openData.textProperty().bind(Localization.bind("dashboard.openData"));

        noteTitleLabel.textProperty().bind(Localization.bind("dashboard.selectNote"));
    }

    private void initUserMenu() {
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
    }

    private void initTooltips() {
        toggleTooltip.textProperty().bind(Localization.bind("tooltip.theme_toggle"));
        langTooltip.textProperty().bind(Localization.bind("tooltip.lang_info"));
        manageTooltip.textProperty().bind(Localization.bind("notebook.manage_label"));
        createTooltip.textProperty().bind(Localization.bind("note.create_label"));
        logoutTooltip.textProperty().bind(Localization.bind(LOGOUT_KEY));

        toggleTooltip.setShowDelay(Duration.millis(100));
        langTooltip.setShowDelay(Duration.millis(100));
        manageTooltip.setShowDelay(Duration.millis(100));
        createTooltip.setShowDelay(Duration.millis(100));
        logoutTooltip.setShowDelay(Duration.millis(100));
    }

    private void initTheme() {
        rootPane.getStyleClass().add("root");
        setupTheme();
    }

    private void initTable() {
        editButton.setDisable(true);
        deleteButton.setDisable(true);

        setupTableColumns();
        WordCountUtil.bind(noteViewArea, wordCountLabel);
    }

    private void initSplitPane() {
        // Enforce SplitPane divider limits so the notes table and editor keep their minimum widths
        Platform.runLater(() -> {
            try {
                if (mainSplit == null) return;

                // Helper to clamp divider based on min widths
                Runnable clampDivider = () -> {
                    double total = mainSplit.getWidth();
                    if (total <= 0) return;
                    double leftMin = notesTable.getMinWidth();
                    double rightMin = editorPane.getMinWidth();

                    double minPosition = Math.clamp(leftMin / total, 0.0, 0.99);
                    double maxPosition = Math.clamp(1.0 - (rightMin / total), 0.01, 1.0);

                    var divider = mainSplit.getDividers().get(0);
                    double position = divider.getPosition();

                    double newPosition = Math.clamp(position, minPosition, maxPosition);
                    if (Double.compare(newPosition, position) != 0) {
                        divider.setPosition(newPosition);
                    }
                };

                // Clamp immediately
                clampDivider.run();

                // When user drags divider, ensure it stays within the min/max
                mainSplit.getDividers().get(0).positionProperty().addListener((obs, oldV, newV) -> clampDivider.run());

                // When the split pane width changes (window resize), re-clamp
                mainSplit.widthProperty().addListener((obs, oldV, newV) -> clampDivider.run());
            } catch (Exception ex) {
                logger.log(Level.FINE, "Failed to initialize split pane clamps", ex);
            }
        });
    }

    private void loadInitialData() {
        // Determine initial notebook
        activeNotebook = dashboardService.getInitialNotebook();
        if (activeNotebook != null) loadNotes();
    }

    private void setupLanguageCombo() {
        LanguageComboService.setup(languageCombo);
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

        NoteTranslationEntity translation = dashboardService.getDisplayTranslation(note);

        if (translation == null) {
            logger.warning("Missing translation for note: " + note.getId());

            noteTitleLabel.setText("");
            noteViewArea.clear();
            annotationViewArea.clear();

            return;
        }

        noteTitleLabel.setText(translation.getTitle());
        noteViewArea.setText(translation.getContent());
        annotationViewArea.setText(translation.getAnnotation());
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
                .map(TagEntity::getTagName)
                .filter(Objects::nonNull)
                .sorted(String::compareToIgnoreCase)
                .forEach(tagName -> {
                    Label tagLabel = new Label("#" + tagName);
                    tagLabel.getStyleClass().addAll("note-tag", "tag-box");
                    tagFlowpane.getChildren().add(tagLabel);
                });
    }

    // ---------- EVENT BUS ----------

    private void setupEventBusSubscription() {
        EventBus.subscribe(event -> {
            if (event instanceof NoteCreatedEvent(NoteEntity note)) {
                loadNotes();
                javafx.application.Platform.runLater(() ->
                        notesTable.getItems().stream()
                                .filter(n -> n.getId().equals(note.getId()))
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
            ImageView icon = new ImageView(new Image(ToggleUtil.isDarkMode() ? "/Images/light-theme.png" : "/Images/dark-theme.png"));
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
            languageIconLabel.getStyleClass().add("language-icon");
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

            DashboardNavigationService.openManageNotebooks( owner, activeNotebook, notebook -> activeNotebook = notebook);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to open Manage Notebooks window", e);
        }

        loadNotes();
    }

    @FXML
    private void handleLogout() {
        Window window = rootPane.getScene().getWindow();

        DashboardNavigationService.logout(window, () -> {
            Scene scene = rootPane.getScene();

            scene.getStylesheets().removeIf(s -> s.endsWith("theme.css"));
            scene.getRoot().getStyleClass().removeAll("dark", "light");
            ToggleUtil.setDarkMode(false);
        });
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
                logger.log(Level.SEVERE, e, () -> "Failed to delete note with ID: " + selectedNote.getId());
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
                Localization.get(EXPORT_SELECTED),
                Localization.get(EXPORT_SELECTED),
                Localization.get("export.dialog.option.notebook")
        );

        dialog.setTitle(Localization.get("export.dialog.title"));
        dialog.setHeaderText(Localization.get("export.dialog.header"));
        dialog.setContentText(Localization.get("export.dialog.label"));

        AlertUtil.addStandardButtons(dialog);

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        boolean success;

        if (result.get().equals(Localization.get(EXPORT_SELECTED))) {
            NoteEntity selected = notesTable.getSelectionModel().getSelectedItem();

            if (selected == null) {
                AlertUtil.showWarning(rootPane.getScene().getWindow(), Localization.get("export.no_notes"));
                return;
            }
            success = exportService.exportSelectedNote(selected, rootPane.getScene().getWindow());
        } else {
            success = exportService.exportEntireNotebook(activeNotebook, rootPane.getScene().getWindow());
        }

        if (success) {
            AlertUtil.showInfo(rootPane.getScene().getWindow(), Localization.get("export.success"));
        } else {
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
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(dataDir);
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
