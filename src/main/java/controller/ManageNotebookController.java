package controller;

import dao.notebook.JpaNoteBookDao;
import dao.notebook.NoteBookDAO;
import entity.NotebookEntity;
import entity.UserEntity;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import session.NotebookSession;
import session.UserSession;
import javafx.fxml.FXML;
import util.AlertUtil;
import util.Localization;
import util.WindowUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ManageNotebookController {
    private static final Logger logger = Logger.getLogger(ManageNotebookController.class.getName());

    private NoteBookDAO notebookDao;
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
        notebookDao = new JpaNoteBookDao();

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

        notebookListView.setCellFactory(TextFieldListCell.forListView(new StringConverter<>() {
            @Override
            public String toString(NotebookEntity nb) {
                return nb.getTitle();
            }

            @Override
            public NotebookEntity fromString(String string) {
                return notebookListView.getSelectionModel().getSelectedItem();
            }
        }));


        notebookListView.setCellFactory(TextFieldListCell.forListView(new StringConverter<>() {
            @Override
            public String toString(NotebookEntity nb) {
                return nb.getTitle();
            }

            @Override
            public NotebookEntity fromString(String string) {
                return notebookListView.getSelectionModel().getSelectedItem();
            }
        }));
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
                notebooks.sort(Comparator.comparing(NotebookEntity::getTitle, String.CASE_INSENSITIVE_ORDER));
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

        TextInputDialog dialog = new TextInputDialog(selected.getTitle());
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
                selected.setTitle(newName.trim());
                notebookDao.save(selected);
                notebookListView.getItems().sort(Comparator.comparing(NotebookEntity::getTitle, String.CASE_INSENSITIVE_ORDER));
                notebookListView.refresh();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to rename notebook: " + selected.getTitle(), e);
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
                Localization.get("notebooks.delete_confirm", selected.getTitle())
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
}