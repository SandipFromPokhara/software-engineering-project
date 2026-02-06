package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EntryView extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the entry FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/entry.fxml"));
            Parent root = loader.load();

            // Create the scene
            Scene scene = new Scene(root);

            // Set up the stage
            primaryStage.setTitle("NoteVault - Welcome");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading Entry view: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
