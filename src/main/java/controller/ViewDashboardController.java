package controller;

import entity.NoteEntity;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import util.NavigationUtil;

import java.io.IOException;


public class ViewDashboardController {

    @FXML
    private Button viewNotesBtn, createNoteBtn, settingsBtn, logoutBtn;

    @FXML
    private Label viewNotesLabel, createNoteLabel, settingsLabel, logoutLabel;

    @FXML
    private Button deleteButton;

    @FXML
    private Button editButton;

    @FXML
    private TableView<NoteEntity> notesTable;

    @FXML
    private TableColumn<NoteEntity, String> titleColumn;

    @FXML
    private TableColumn<NoteEntity, String> dateColumn;

    @FXML
    private Label noteTitleLabel;

    @FXML
    private TextArea noteViewArea;

    // for creating fake files and to store
    private ObservableList<NoteEntity> notes =
            FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        noteTitleLabel.setText("Select a note to view details");
        noteViewArea.setText("");

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

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

        setupHover(viewNotesBtn, viewNotesLabel);
        setupHover(createNoteBtn, createNoteLabel);
        setupHover(settingsBtn, settingsLabel);
        setupHover(logoutBtn, logoutLabel);
    }

    private void setupHover(Button button, Label label) {
        button.setOnMouseEntered(e -> {
            label.setVisible(true);
            button.setStyle("-fx-background-color: #93ad9b; -fx-cursor: hand;");
        });

        button.setOnMouseExited(e -> {
            label.setVisible(false);
            button.setStyle("-fx-background-color: transparent");
        });
    }

    @FXML
    private void handleOpen(ActionEvent event) {
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        NavigationUtil.navigateTo(event, "/FXML/entry.fxml", "Welcome", false);
    }

    @FXML
    public void handleDelete(ActionEvent event) {
    }

    @FXML
    public void handleCreate(ActionEvent event) {
        NavigationUtil.navigateTo(event, "/FXML/create_note.fxml", "NoteVault - Create Note", true);
    }

    @FXML
    public void handleOpenEditWindow(ActionEvent event) {
        NoteEntity selectedNote = notesTable.getSelectionModel().getSelectedItem();
        if (selectedNote == null) return;

        Stage editStage = new Stage();
        editStage.initModality(Modality.APPLICATION_MODAL);

        NavigationUtil.navigateTo(editStage, "/FXML/edit.fxml", "NoteVault - Edit Note", true);
        editStage.showAndWait();
    }
}
