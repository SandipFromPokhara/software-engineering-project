import controller.EditNoteController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import model.NoteModel;

public class TestEditNote extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/FXML/edit.fxml")
        );
        Parent root = loader.load();

        // 🔹 Create a TEST note
        NoteModel testNote = new NoteModel(
                "Test title",
                "Test content",
                "Test annotation",
                null   // notebook not needed for this test
        );

        // 🔹 Inject note into controller
        EditNoteController controller = loader.getController();
        controller.setNote(testNote);

        stage.setTitle("Edit Note Test");
        stage.setScene(new Scene(root));
        stage.show();
    }

}
