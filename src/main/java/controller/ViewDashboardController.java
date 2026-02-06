package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import util.NavigationUtil;

public class ViewDashboardController {
    @FXML
    private void handleLogout(ActionEvent event) {
        NavigationUtil.navigateTo(event, "/FXML/entry.fxml", "Welcome");
    }
}
