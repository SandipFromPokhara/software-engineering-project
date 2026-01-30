import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load three screens so we can switch between them for quick testing
        Parent entryRoot = FXMLLoader.load(getClass().getResource("/FXML/entry.fxml"));
        Parent loginRoot = FXMLLoader.load(getClass().getResource("/FXML/login.fxml"));
        Parent signupRoot = FXMLLoader.load(getClass().getResource("/FXML/signup.fxml"));

        BorderPane root = new BorderPane();
        root.setCenter(entryRoot);

        Scene scene = new Scene(root, 600, 400);

        // Keyboard shortcuts for quick testing:
        // E -> Entry, L -> Login, S -> Signup
        scene.setOnKeyPressed(evt -> {
            if (evt.getCode() == KeyCode.E) {
                root.setCenter(entryRoot);
                System.out.println("Switched to Entry screen");
            } else if (evt.getCode() == KeyCode.L) {
                root.setCenter(loginRoot);
                System.out.println("Switched to Login screen");
            } else if (evt.getCode() == KeyCode.S) {
                root.setCenter(signupRoot);
                System.out.println("Switched to Signup screen");
            }
        });

        stage.setTitle("Note Vault - UI Flow Test");
        stage.setScene(scene);
        stage.show();

        System.out.println("Press E (entry), L (login), or S (signup) to switch screens");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
