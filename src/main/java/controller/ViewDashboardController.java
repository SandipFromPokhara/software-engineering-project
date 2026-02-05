package controller;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import util.NavigationUtil;


public class ViewDashboardController {

    @FXML
    private TableView<?> notesTable;

    @FXML
    private TextArea noteTextArea;

    @FXML
    private Button logoutButton;

    @FXML
    public void initialize() {
        notesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        NavigationUtil.navigateTo(event, "/FXML/entry.fxml", "Welcome", false);
    }

    public void handleDelete(ActionEvent actionEvent) {
    }

    public void handleOpenEditWindow(ActionEvent actionEvent) {

    }
}
