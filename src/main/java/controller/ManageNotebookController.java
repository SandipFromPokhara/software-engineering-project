package controller;

import dao.notebook.JpaNotebookDao;
import dao.notebook.NotebookDAO;
import entity.entities.NotebookEntity;
import entity.entities.UserEntity;
import entity.translationentities.NotebookTranslationEntity;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
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

    private NotebookDAO notebookDao;
    private NotebookEntity activeNotebook;

    @FXML
    private Label manageTitle;

    @FXML
    private ListView<NotebookEntity> notebookListView;

    @FXML
    private Button openBtn, renameBtn, deleteBtn, closeBtn;

    public void setActiveNotebook(NotebookEntity notebook) {
        this.activeNotebook = notebook;
    }

    @FXML
    private void initialize() {
        notebookDao = new JpaNotebookDao();

        // LOCALIZATION BINDINGS
        manageTitle.textProperty().bind(Localization.bind("notebooks.title"));
        openBtn.textProperty().bind(Localization.bind("notebooks.open"));
        renameBtn.textProperty().bind(Localization.bind("notebooks.rename"));
        deleteBtn.textProperty().bind(Localization.bind("button.delete"));
        closeBtn.textProperty().bind(Localization.bind("notebooks.close"));

        // Disable buttons initially
        renameBtn.setDisable(true);
        deleteBtn.setDisable(true);

        loadNotebooks();

        if (activeNotebook != null) {
            notebookListView.getSelectionModel().select(activeNotebook);
        }

        notebookListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isSelected = newVal != null;
            openBtn.setDisable(!isSelected);
            renameBtn.setDisable(!isSelected);
            deleteBtn.setDisable(!isSelected);
        });

        // Use a StringConverter for converting NotebookEntity -> title string
        StringConverter<NotebookEntity> converter = new StringConverter<>() {
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

        // Create a cell factory that uses a wrapping Label so long titles are visible
        notebookListView.setCellFactory(lv -> new ListCell<NotebookEntity>() {
            private final Label label = new Label();
            {
                label.setWrapText(true);
                // Bind the label max width to the list view width minus padding so it can wrap correctly
                label.maxWidthProperty().bind(lv.widthProperty().subtract(35));
                // Show full title on hover in a tooltip (helps when titles are long)
                Tooltip tooltip = new Tooltip();
                tooltip.textProperty().bind(label.textProperty());
                label.setTooltip(tooltip);
                // Ensure label picks up a theme-aware color via CSS
                label.getStyleClass().add("list-cell-text");
            }

            @Override
            protected void updateItem(NotebookEntity item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle(null);
                } else {
                    String title = converter.toString(item);
                    logger.fine(() -> "Updating notebook list cell: id=" + (item.getId() == null ? "<null>" : item.getId()) + ", title='" + title + "'");
                    // Set both the text and graphic as a fallback so the title is visible even if CSS/graphic rendering fails
                    label.setText(title);
                    setText(title);
                    setGraphic(null);
                    // Inline fallback for text color if stylesheets didn't apply yet
                    boolean dark = util.ToggleUtil.isDarkMode();
                    if (dark) {
                        setStyle("-fx-text-fill: #e6e6e6;");
                    } else {
                        setStyle("-fx-text-fill: #000000;");
                    }
                }
            }
        });
    }

    public void loadNotebooks() {
        try {
            UserEntity user = UserSession.getUserInstance().getUser();

            Task<List<NotebookEntity>> loadNotebooksTask = new Task<>() {
                @Override
                protected List<NotebookEntity> call() {
                    return notebookDao.findByUser(user);
                }
            };

            loadNotebooksTask.setOnSucceeded(e -> {
                List<NotebookEntity> notebooks = loadNotebooksTask.getValue();

                if (notebooks != null) {
                    String currentCode = Localization.getCurrentLanguageCode();
                    for (NotebookEntity n : notebooks) {
                        String title = getNotebookTitle(n);
                        logger.fine(() -> "Notebook id=" + (n.getId() == null ? "<null>" : n.getId()) + ", title='" + title + "', lang=" + currentCode);
                    }
                }
                notebooks.sort((n1, n2) -> {
                    String t1 = getNotebookTitle(n1);
                    String t2 = getNotebookTitle(n2);
                    return String.CASE_INSENSITIVE_ORDER.compare(t1, t2);
                });
                notebookListView.getItems().setAll(notebooks);

                // Select active notebook if available
                if (activeNotebook != null) {
                    notebookListView.getSelectionModel().select(activeNotebook);
                }
            });

            loadNotebooksTask.setOnFailed(e ->
                    logger.log(Level.SEVERE, "Failed to load notebooks", loadNotebooksTask.getException())
            );

            new Thread(loadNotebooksTask).start();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load notebooks", e);
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
        dialog.setTitle(Localization.get("notebooks.rename"));
        dialog.setHeaderText(Localization.get("notebooks.rename"));
        dialog.setContentText(Localization.get("notebooks.new_name"));

        // Localize OK and Cancel buttons
        ButtonType okButton = new ButtonType(Localization.get("button.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType(Localization.get("button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().setAll(okButton, cancelButton);

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
                logger.log(Level.SEVERE, "Failed to rename notebook: " + getNotebookTitle(selected), e);
                AlertUtil.showError(
                        notebookListView.getScene().getWindow(),
                        Localization.get("notebooks.rename_failed")
                );
            }
        });
    }

    @FXML
    private void handleDelete() {
        NotebookEntity selected = notebookListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        boolean confirmed = AlertUtil.showConfirmation(
                notebookListView.getScene().getWindow(),
                Localization.get("button.delete"),
                Localization.get("notebooks.delete_confirm", getNotebookTitle(selected))
        );

        if (confirmed) {
            try {
                notebookDao.delete(selected);
                notebookListView.getItems().remove(selected);

                // If deleted notebook was active, clear session
                NotebookEntity active = NotebookSession.getLastCreatedNotebook();
                if (active != null && active.getId().equals(selected.getId())) {
                    NotebookSession.clear();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to delete notebook", e);
                AlertUtil.showError(notebookListView.getScene().getWindow(), Localization.get("notebook.deleteAlert"));
            }
        }
    }

    @FXML
    private void handleClose() {
        WindowUtil.closeWindow(notebookListView);
    }

    private String getNotebookTitle(NotebookEntity nb) {
        if (nb == null) return "";

        String currentCode = Localization.getCurrentLanguageCode();

        // 1) Preferred: exact match for current language code
        // Try case-sensitive key first
        NotebookTranslationEntity translation = nb.getTranslations().get(currentCode);
        if (translation == null) {
            // Try lowercase/uppercase variants
            translation = nb.getTranslations().get(currentCode == null ? null : currentCode.toLowerCase());
        }
        if (translation == null) {
            translation = nb.getTranslations().get(currentCode == null ? null : currentCode.toUpperCase());
        }
        if (translation == null && currentCode != null) {
            // Try to find a key that equalsIgnoreCase(currentCode)
            for (String key : nb.getTranslations().keySet()) {
                if (key != null && key.equalsIgnoreCase(currentCode)) {
                    translation = nb.getTranslations().get(key);
                    break;
                }
            }
        }
        if (translation != null && translation.getTitle() != null && !translation.getTitle().isBlank()) {
            return translation.getTitle();
        }

        // 2) Common fallback: English (if available)
        // Try english keys in a case-insensitive way
        translation = nb.getTranslations().get("en");
        if (translation == null) translation = nb.getTranslations().get("EN");
        if (translation == null) {
            for (String key : nb.getTranslations().keySet()) {
                if (key != null && key.equalsIgnoreCase("en")) {
                    translation = nb.getTranslations().get(key);
                    break;
                }
            }
        }
        if (translation != null && translation.getTitle() != null && !translation.getTitle().isBlank()) {
            logger.fine(() -> "Falling back to 'en' translation for notebook id=" + nb.getId());
            return translation.getTitle();
        }

        // 3) Any other available translation (first non-empty)
        for (NotebookTranslationEntity t : nb.getTranslations().values()) {
            if (t != null && t.getTitle() != null && !t.getTitle().isBlank()) {
                logger.fine(() -> "Falling back to available translation (lang=" + t.getLangCode() + ") for notebook id=" + nb.getId());
                return t.getTitle();
            }
        }

        // 4) Nothing found
        logger.fine(() -> "No translation found for notebook id=" + nb.getId() + " (requested lang=" + currentCode + ") available langs=" + nb.getTranslations().keySet());
        return "";
    }
}