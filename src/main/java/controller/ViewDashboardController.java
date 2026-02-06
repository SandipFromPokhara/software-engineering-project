package controller;

import entity.NoteEntity;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import util.NavigationUtil;


public class ViewDashboardController {

    @FXML
    private Button deleteButton;

    @FXML
    private Button editButton;

    @FXML
    private TableView<NoteEntity> notesTable;

    @FXML
    private Label noteTitleLabel;

    @FXML
    private TextArea noteViewArea;

    @FXML
    private Button logoutButton;

    @FXML
    public void initialize() {
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        noteTitleLabel.setText("Select a note to view details");
        noteViewArea.setText("");

        notesTable.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
            if (newSelection != null) {
                noteTitleLabel.setText(newSelection.getTitle());
                noteViewArea.setText(newSelection.getContent());

                editButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                noteTitleLabel.setText("Select a note to view details");
                noteViewArea.clear();
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
        notesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        NavigationUtil.navigateTo(event, "/FXML/entry.fxml", "Welcome", false);
    }

    @FXML
    public void handleDelete(ActionEvent event) {
    }

    @FXML
    public void handleOpenEditWindow(ActionEvent event) {
        Stage editStage = new Stage();
        editStage.initModality(Modality.APPLICATION_MODAL);

        NavigationUtil.navigateTo(event, "/FXML/EditPage.fxml", "Edit", true);
    }
}
