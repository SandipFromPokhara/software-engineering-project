package view;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class GuestDashboardView extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/GuestDashboard.fxml"));
        Parent root = fxmlLoader.load();

        Scene guestDashboard = new Scene(root);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/home.png"))));
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/files.png"))));
        stage.setScene(guestDashboard);
        stage.show();
    }
}