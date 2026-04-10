package view;

import javafx.application.Application;
import javafx.stage.Stage;
import util.NavigationUtil;

public class StartView extends Application {

    @Override
    public void start(Stage stage) {
        NavigationUtil.replaceScene(stage, "/FXML/entry.fxml", "entry.window_title", false);
    }
}
