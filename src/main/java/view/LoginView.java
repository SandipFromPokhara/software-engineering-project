package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginView extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the login FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login_view.fxml"));
            Parent root = loader.load();

            // Create the scene
            Scene scene = new Scene(root, 600, 400);

            // Set up the stage with fixed dimensions
            primaryStage.setTitle("NoteVault - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(400);
            primaryStage.setMaxWidth(600);
            primaryStage.setMaxHeight(400);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading Login view: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
