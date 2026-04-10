package controller;

import dao.notebook.JpaNotebookDao;
import dao.notebook.INotebookDAO;
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
    private static final String NOTEBOOKS_RENAME_KEY = "notebooks.rename";

    private INotebookDAO notebookDao;
    private NotebookEntity activeNotebook;

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
        deleteBtn.textProperty().bind(Localization.bind("button.delete"));
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
        return new ListCell<>() {
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
                    updateCellContent(item, converter);
                }
            }

            private void updateCellContent(NotebookEntity item, StringConverter<NotebookEntity> converter) {
                String title = converter.toString(item);
                logger.fine(() -> "Updating notebook list cell: id=" + (item.getId() == null ? "<null>" : item.getId()) + ", title='" + title + "'");
                label.setText(title);
                setText(title);
                setGraphic(null);

                // Inline fallback for text color
                String colorStyle = util.ToggleUtil.isDarkMode() ? "-fx-text-fill: #e6e6e6;" : "-fx-text-fill: #000000;";
                setStyle(colorStyle);
            }
        };
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

                    // Ensure the list is not null before sorting or setting items
                    notebooks.sort((n1, n2) -> {
                        String t1 = getNotebookTitle(n1);
                        String t2 = getNotebookTitle(n2);
                        return String.CASE_INSENSITIVE_ORDER.compare(t1, t2);
                    });
                    notebookListView.getItems().setAll(notebooks);
                } else {
                    notebookListView.getItems().clear();
                }

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
