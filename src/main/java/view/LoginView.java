package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class LoginView extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/login_view.fxml"));
        Parent root = fxmlLoader.load();

        Scene loginScene = new Scene(root);
        stage.setMinWidth(440);
        stage.setMinHeight(350);

        stage.setMaxWidth(1600);
        stage.setMaxHeight(1000);

        stage.getIcons().add(new Image("/Images/Logo_white.png"));
        stage.setTitle("NoteVault");
        stage.setScene(loginScene);
        stage.show();
    }
}
