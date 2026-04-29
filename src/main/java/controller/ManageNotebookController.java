package controller;

import dao.notebook.JpaNotebookDao;
import dao.notebook.INotebookDAO;
import datasource.MariaDbJpaConnection;
import entity.entities.NotebookEntity;
import entity.entities.UserEntity;
import entity.translationentities.NotebookTranslationEntity;
import javafx.concurrent.Task;
import dao.note.JpaNoteDao;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.beans.binding.Bindings;
import session.NotebookSession;
import session.UserSession;
import javafx.fxml.FXML;
import util.AlertUtil;
import util.Localization;
import util.WindowUtil;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ManageNotebookController {
    private static final Logger logger = Logger.getLogger(ManageNotebookController.class.getName());
    private static final String NOTEBOOKS_RENAME_KEY = "notebooks.rename";
    private static final String DELETE_BUTTON = "button.delete";
    private static final String NULL = "<null>";
    private static final String SELECTED_NOTEBOOK_STYLE = "selected-notebook";

    private INotebookDAO notebookDao;
    private NotebookEntity activeNotebook;
    // Cache of notebook -> note count for display in the list
    private final java.util.Map<Long, Integer> notebookNoteCounts = new java.util.HashMap<>();

    @FXML
    private Label manageTitle;

    @FXML
    private ListView<NotebookEntity> notebookListView;

    @FXML
    private Button openBtn;

    @FXML
    private Button renameBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private Button closeBtn;

    public void setActiveNotebook(NotebookEntity notebook) {
        this.activeNotebook = notebook;
    }

    @FXML
    private void initialize() {
        notebookDao = new JpaNotebookDao();

        bindUIProperties();
        setupListeners();

        loadNotebooks();

        if (activeNotebook != null) {
            notebookListView.getSelectionModel().select(activeNotebook);
        }

        StringConverter<NotebookEntity> converter = createNotebookConverter();
        notebookListView.setCellFactory(lv -> createNotebookListCell(lv, converter));
    }

    // UI binding method
    private void bindUIProperties() {
        manageTitle.textProperty().bind(Localization.bind("notebooks.title"));
        openBtn.textProperty().bind(Localization.bind("notebooks.open"));
        renameBtn.textProperty().bind(Localization.bind(NOTEBOOKS_RENAME_KEY));
        deleteBtn.textProperty().bind(Localization.bind(DELETE_BUTTON));
        closeBtn.textProperty().bind(Localization.bind("notebooks.close"));

        // Disable buttons initially
        renameBtn.setDisable(true);
        deleteBtn.setDisable(true);
    }

    private void setupListeners() {
        notebookListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isSelected = newVal != null;
            openBtn.setDisable(!isSelected);
            renameBtn.setDisable(!isSelected);
            deleteBtn.setDisable(!isSelected);
        });
    }

    // Use a StringConverter for converting NotebookEntity -> title string
    private StringConverter<NotebookEntity> createNotebookConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(NotebookEntity nb) {
                if (nb == null) return "";
                String title = getNotebookTitle(nb);
                return (title == null || title.isBlank()) ? "Untitled (ID: " + nb.getId() + ")" : title;
            }

            @Override
            public NotebookEntity fromString(String string) {
                return notebookListView.getSelectionModel().getSelectedItem();
            }
        };
    }

    private ListCell<NotebookEntity> createNotebookListCell(ListView<NotebookEntity> lv, StringConverter<NotebookEntity> converter) {
        return new NotebookListCell(lv, converter);
    }

    /**
     * Extracted ListCell implementation to reduce cognitive complexity of the factory method.
     */
    private class NotebookListCell extends ListCell<NotebookEntity> {
        private final Label titleLabel = new Label();
        private final Label countLabel = new Label();
        private final HBox container = new javafx.scene.layout.HBox(8);
        private final StringConverter<NotebookEntity> converter;

        NotebookListCell(ListView<NotebookEntity> lv, StringConverter<NotebookEntity> converter) {
            this.converter = converter;
            titleLabel.setWrapText(true);
            titleLabel.getStyleClass().addAll("list-cell-text", "list-cell-title");
            // Title label should take available width, count label stays compact
            titleLabel.maxWidthProperty().bind(lv.widthProperty().subtract(90));

            countLabel.getStyleClass().addAll("list-cell-text", "list-cell-count");

            // Tooltip shows full title (and optionally count)
            Tooltip tooltip = new Tooltip();
            tooltip.textProperty().bind(titleLabel.textProperty());
            util.TooltipUtil.setTooltipDelay(tooltip);
            titleLabel.setTooltip(tooltip);

            Tooltip countTooltip = new Tooltip();
            util.TooltipUtil.setTooltipDelay(countTooltip);
            // Localized tooltip for notes count; updates when locale or count changes
            countTooltip.textProperty().bind(Bindings.createStringBinding(() -> {
                String txt = countLabel.getText();
                if (txt == null || txt.isBlank()) return "";
                return util.Localization.get("notebooks.count", txt);
            }, util.Localization.localeProperty(), countLabel.textProperty()));
            countLabel.setTooltip(countTooltip);

            container.getChildren().addAll(titleLabel, countLabel);

            selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                // primitive boolean expression to satisfy static analysis
                boolean nowSelected = Boolean.TRUE.equals(isNowSelected);
                if (nowSelected) {
                    if (!getStyleClass().contains(SELECTED_NOTEBOOK_STYLE)) {
                        getStyleClass().add(SELECTED_NOTEBOOK_STYLE);
                    }
                } else {
                    getStyleClass().remove(SELECTED_NOTEBOOK_STYLE);
                }
            });
        }

        @Override
        protected void updateItem(NotebookEntity item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                clearCell();
            } else {
                renderCell(item);
            }
        }

        private void renderCell(NotebookEntity item) {
            String title = converter.toString(item);
            Integer count = notebookNoteCounts.get(item.getId());

            titleLabel.setText(title == null ? "" : title);

            applyCount(count);
            applySelectionStyle(isSelected());

            // Use container as graphic (avoid setText to allow richer layout)
            setText(null);
            setGraphic(container);
        }

        private void clearCell() {
            setGraphic(null);
            setText(null);
            titleLabel.setText("");
            countLabel.setText("");
            countLabel.setVisible(false);
            // remove selection style from the cell itself
            getStyleClass().remove(SELECTED_NOTEBOOK_STYLE);
            // also defensively remove from container in case previous code added it there
            container.getStyleClass().remove(SELECTED_NOTEBOOK_STYLE);
        }

        private void applyCount(Integer count) {
            if (count != null) {
                countLabel.setText(String.format("%d", count));
                countLabel.setVisible(true);
            } else {
                countLabel.setText("");
                countLabel.setVisible(false);
            }
        }

        private void applySelectionStyle(boolean selected) {
            if (selected) {
                if (!getStyleClass().contains(SELECTED_NOTEBOOK_STYLE)) {
                    getStyleClass().add(SELECTED_NOTEBOOK_STYLE);
                }
            } else {
                getStyleClass().remove(SELECTED_NOTEBOOK_STYLE);
            }
        }
    }

    public void loadNotebooks() {
        try {
            UserEntity user = UserSession.getUserInstance().getUser();

            Task<List<NotebookEntity>> loadNotebooksTask = new Task<>() {
                @Override
                protected List<NotebookEntity> call() {
                    try {
                        return notebookDao.findByUser(user);
                    } finally {
                        MariaDbJpaConnection.closeEntityManager();
                    }
                }
            };

            loadNotebooksTask.setOnSucceeded(e -> processLoadedNotebooks(loadNotebooksTask.getValue()));

            loadNotebooksTask.setOnFailed(e -> logger.log(Level.SEVERE, "Failed to load notebooks", loadNotebooksTask.getException()));

            new Thread(loadNotebooksTask).start();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load notebooks", e);
        }
    }

    // Process results from the background task. Extracted to reduce complexity in loadNotebooks().
    private void processLoadedNotebooks(List<NotebookEntity> notebooks) {
        if (notebooks == null) {
            notebookListView.getItems().clear();
            return;
        }

        String currentCode = Localization.getCurrentLanguageCode();
        for (NotebookEntity n : notebooks) {
            String title = getNotebookTitle(n);
            logger.fine(() -> "Notebook id=" + (n.getId() == null ? NULL : n.getId()) + ", title='" + title + "', lang=" + currentCode);
        }

        // Sort notebooks by title (localized)
        notebooks.sort((n1, n2) -> {
            String t1 = getNotebookTitle(n1);
            String t2 = getNotebookTitle(n2);
            return String.CASE_INSENSITIVE_ORDER.compare(t1, t2);
        });

        // Populate note counts (may hit DB) — acceptable for small number of notebooks
        computeNoteCounts(notebooks);

        notebookListView.getItems().setAll(notebooks);
        notebookListView.refresh();

        // Select active notebook if available
        if (activeNotebook != null) {
            notebookListView.getSelectionModel().select(activeNotebook);
        }
    }

    private void computeNoteCounts(List<NotebookEntity> notebooks) {
        try {
            JpaNoteDao noteDao = new JpaNoteDao();
            notebookNoteCounts.clear();
            for (NotebookEntity n : notebooks) {
                if (n == null || n.getId() == null) {
                    // skip null entries defensively
                    continue;
                }

                int count = safeCountForNotebook(noteDao, n);
                notebookNoteCounts.put(n.getId(), count);
            }
        } catch (Exception ex) {
            logger.log(Level.FINE, "Failed to compute notebook note counts", ex);
        }
    }

    private int safeCountForNotebook(JpaNoteDao noteDao, NotebookEntity notebook) {
        if (notebook == null || notebook.getId() == null) return 0;

        try {
            var notes = noteDao.findByNotebook(notebook);
            return notes == null ? 0 : notes.size();
        } catch (Exception ex) {
            String idStr = notebook.getId() == null ? NULL : String.valueOf(notebook.getId());
            logger.log(Level.FINE, () -> "Failed to load notes for notebook id=" + idStr + ex);
            return 0;
        }
    }

    @FXML
    private void handleOpen() {
        NotebookEntity selected = notebookListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        NotebookSession.setLastCreatedNotebook(selected);

        Stage stage = (Stage) notebookListView.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleRename() {
        NotebookEntity selected = notebookListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TextInputDialog dialog = new TextInputDialog(getNotebookTitle(selected));
        dialog.setTitle(Localization.get(NOTEBOOKS_RENAME_KEY));
        dialog.setHeaderText(Localization.get(NOTEBOOKS_RENAME_KEY));
        dialog.setContentText(Localization.get("notebooks.new_name"));

        // Handle creation and localization of OK and Cancel buttons
        AlertUtil.addStandardButtons(dialog);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (newName.isBlank()) return;

            try {
                NotebookTranslationEntity translation =
                        selected.getTranslations().get(Localization.getCurrentLanguageCode());

                if (translation == null) {
                    translation = new NotebookTranslationEntity();
                    translation.setLangCode(Localization.getCurrentLanguageCode());
                    selected.addTranslation(translation);
                }

                translation.setTitle(newName.trim());

                notebookDao.save(selected);
                notebookListView.getItems().sort((n1, n2) -> {
                    String t1 = getNotebookTitle(n1);
                    String t2 = getNotebookTitle(n2);
                    return String.CASE_INSENSITIVE_ORDER.compare(t1, t2);
                });

                notebookListView.refresh();
            } catch (Exception e) {
                logger.log(Level.SEVERE, e, () -> "Failed to rename notebook: " + getNotebookTitle(selected));
                AlertUtil.showError(notebookListView.getScene().getWindow(), Localization.get("notebooks.rename_failed"));
            }
        });
    }

    @FXML
    private void handleDelete() {
        NotebookEntity selected = notebookListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // initial confirmation
        boolean confirmed = AlertUtil.showConfirmation(
                notebookListView.getScene().getWindow(),
                Localization.get(DELETE_BUTTON),
                Localization.get("notebooks.delete_confirm", getNotebookTitle(selected))
        );

        if (!confirmed) return;

        try {
            int noteCount = getNoteCount(selected);

            if (noteCount > 0) {
                boolean deleteAll = confirmDeleteAllDialog(selected, noteCount);
                if (!deleteAll) return;

                // user confirmed: perform an atomic delete of notes + notebook in one transaction
                notebookDao.deleteWithNotes(selected);

                // Update UI and session state after successful deletion
                onNotebookRemoved(selected);
            } else {
                // no notes, just delete the notebook
                safeDeleteNotebook(selected);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to delete notebook", e);
            AlertUtil.showError(notebookListView.getScene().getWindow(), Localization.get("notebook.deleteAlert"));
        }
    }

    // --- extracted helpers to reduce cognitive complexity ---
    private int getNoteCount(NotebookEntity notebook) {
        JpaNoteDao noteDao = new JpaNoteDao();
        try {
            java.util.List<entity.entities.NoteEntity> notes = noteDao.findByNotebook(notebook);
            return notes == null ? 0 : notes.size();
        } catch (Exception e) {
            String idStr = notebook == null || notebook.getId() == null ? NULL : String.valueOf(notebook.getId());
            logger.log(Level.FINE, () -> "Failed to get note count for notebook id=" + idStr + e);
            return 0;
        }
    }

    private boolean confirmDeleteAllDialog(NotebookEntity notebook, int noteCount) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(Localization.get(DELETE_BUTTON));
        confirm.setHeaderText(null);
        confirm.setContentText(Localization.get("notebooks.delete_with_notes_confirm", getNotebookTitle(notebook), String.valueOf(noteCount)));
        if (notebookListView.getScene() != null && notebookListView.getScene().getWindow() != null) {
            confirm.initOwner(notebookListView.getScene().getWindow());
        }

        ButtonType deleteAll = new ButtonType(Localization.get(DELETE_BUTTON), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType(Localization.get("button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(deleteAll, cancel);

        var res = confirm.showAndWait();
        return res.isPresent() && res.get() == deleteAll;
    }
    
    private void safeDeleteNotebook(NotebookEntity notebook) {
        notebookDao.delete(notebook);
        onNotebookRemoved(notebook);
    }

    /**
     * Common UI/session cleanup after a notebook has been removed from the database.
     */
    private void onNotebookRemoved(NotebookEntity notebook) {
        if (notebook == null) return;

        notebookListView.getItems().remove(notebook);

        NotebookEntity active = NotebookSession.getLastCreatedNotebook();
        if (active != null && active.getId() != null && active.getId().equals(notebook.getId())) {
            NotebookSession.clear();
        }

        notebookNoteCounts.remove(notebook.getId());
    }

    @FXML
    private void handleClose() {
        WindowUtil.closeWindow(notebookListView);
    }

    private String getNotebookTitle(NotebookEntity nb) {
        if (nb == null) return "";

        String currentCode = Localization.getCurrentLanguageCode();

        // 1) Try exact/case-insensitive match for current language
        String title = findTitleByLanguage(nb, currentCode);
        if (title != null) return title;

        // 2) Fallback to English
        title = findTitleByLanguage(nb, "en");
        if (title != null) {
            logger.fine(() -> "Falling back to en translation for notebook id=" + nb.getId());
            return title;
        }

        // 3) Any other available translation
        return nb.getTranslations().values().stream()
                .map(NotebookTranslationEntity::getTitle)
                .filter(t -> t != null && !t.isBlank())
                .findFirst()
                .orElseGet(() -> {
                    logger.fine(() -> "No translation found for notebook id=" + nb.getId());
                    return "";
                });
    }

    // Helper method to find a title by language key (case-insensitive)
    private String findTitleByLanguage(NotebookEntity nb, String langCode) {
        if (langCode == null) return null;

        return nb.getTranslations().entrySet().stream()
                .filter(entry -> langCode.equalsIgnoreCase(entry.getKey()))
                .map(entry -> entry.getValue().getTitle())
                .filter(t -> t != null && !t.isBlank())
                .findFirst()
                .orElse(null);
    }
}
