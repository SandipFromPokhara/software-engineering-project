package controller;

import dao.notebook.JpaNoteBookDao;
import dao.notebook.NoteBookDAO;
import entity.NoteBookEntity;
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
import util.WindowUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ManageNotebookController {
    private static final Logger logger = Logger.getLogger(ManageNotebookController.class.getName());

    private NoteBookDAO notebookDao;
    private NoteBookEntity activeNotebook;

    @FXML
    private ListView<NoteBookEntity> notebookListView;

    @FXML
    private Button openBtn, renameBtn, deleteBtn, closeBtn;

    public void setActiveNotebook(NoteBookEntity notebook) {
        this.activeNotebook = notebook;
    }

    @FXML
    private void initialize() {
        notebookDao = new JpaNoteBookDao();

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
            public String toString(NoteBookEntity nb) {
                return nb.getTitle();
            }

            @Override
            public NoteBookEntity fromString(String string) {
                return notebookListView.getSelectionModel().getSelectedItem();
            }
        }));
    }

    public void loadNotebooks() {
        try {
            UserEntity user = UserSession.getUserInstance().getUser();

            Task<List<NoteBookEntity>> loadNotebooksTask = new Task<>() {
                @Override
                protected List<NoteBookEntity> call() {
                    return notebookDao.findByUser(user);
                }
            };
            loadNotebooksTask.setOnSucceeded(e -> {
                List<NoteBookEntity> notebooks = loadNotebooksTask.getValue();
                notebooks.sort(Comparator.comparing(NoteBookEntity::getTitle, String.CASE_INSENSITIVE_ORDER));
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
        NoteBookEntity selected = notebookListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        NotebookSession.setLastCreatedNotebook(selected);

        Stage stage = (Stage) notebookListView.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleRename() {
        NoteBookEntity selected = notebookListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TextInputDialog dialog = new TextInputDialog(selected.getTitle());
        dialog.setTitle("Rename Notebook");
        dialog.setHeaderText("Rename Notebook");
        dialog.setContentText("New name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (newName.isBlank()) return;

            try {
                selected.setTitle(newName.trim());
                notebookDao.save(selected);
                notebookListView.getItems().sort(Comparator.comparing(NoteBookEntity::getTitle, String.CASE_INSENSITIVE_ORDER));
                notebookListView.refresh();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to rename notebook: " + selected.getTitle(), e);
                AlertUtil.showError(notebookListView.getScene().getWindow(), "Rename failed.\nCould not rename notebook.");
            }
        });
    }

    @FXML
    private void handleDelete() {
        NoteBookEntity selected = notebookListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        boolean confirmed = AlertUtil.showConfirmation(
                notebookListView.getScene().getWindow(),
                "Delete Notebook",
                "Delete notebook \"" + selected.getTitle() + "\"?\nAll notes inside will also be deleted. This cannot be undone.");

        if (confirmed) {
            try {
                notebookDao.delete(selected);
                notebookListView.getItems().remove(selected);

                // If deleted notebook was active, clear session
                NoteBookEntity active = NotebookSession.getLastCreatedNotebook();
                if (active != null && active.getId().equals(selected.getId())) {
                    NotebookSession.clear();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to delete notebook", e);
                AlertUtil.showError(notebookListView.getScene().getWindow(), "Could not delete notebook.");
            }
        }
    }

    @FXML
    private void handleClose() {
        WindowUtil.closeWindow(notebookListView);
    }
}