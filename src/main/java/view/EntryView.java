package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;



public class EntryView extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/entry.fxml"));
        Parent root = fxmlLoader.load();

        Scene entry = new Scene(root);
        stage.setScene(entry);
        stage.show();
    }
}
