package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CreateNoteView extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the create note FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/create_note.fxml"));
            Parent root = loader.load();

            // Create the scene - let FXML define the size
            Scene scene = new Scene(root);

            // Set up the stage as expandable
            primaryStage.setTitle("NoteVault - Create Note");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.sizeToScene();
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading Create Note view: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
