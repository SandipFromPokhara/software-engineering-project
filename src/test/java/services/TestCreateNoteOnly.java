package services;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Standalone test application for simplified Create Note feature
 * Only includes: Title, Content, Annotation fields and Save/Clear buttons
 */
public class TestCreateNoteOnly extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Set database credentials
            System.setProperty("DB_USER", "notevaultUser");
            System.setProperty("DB_PASSWORD", "group1oPassw0rD");

            // Load your simplified Create Note FXML
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/create_note.fxml"));

            // Create scene
            Scene scene = new Scene(root, 800, 600);

            // Setup stage
            primaryStage.setTitle("NoteVault - Create Note (Simplified)");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(500);
            primaryStage.show();

            System.out.println("✅ Simplified Create Note UI loaded successfully!");
            System.out.println("🎯 Available features:");
            System.out.println("  📝 Note Title - Required field");
            System.out.println("  📄 Note Content - Main writing area");
            System.out.println("  📋 Annotation Area - Additional notes");
            System.out.println("  💾 Save Note - Saves to database");
            System.out.println("  🗑️  Clear - Clears all fields");

        } catch (Exception e) {
            System.err.println("❌ Error loading Create Note UI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
