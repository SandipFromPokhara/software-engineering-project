package view;

import javafx.application.Application;
import javafx.stage.Stage;
import util.NavigationUtil;

public class EntryView extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        NavigationUtil.navigateTo(stage, "/FXML/entry.fxml", "NoteVault - Welcome");
    }
}
