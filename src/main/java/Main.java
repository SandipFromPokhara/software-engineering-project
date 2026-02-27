import javafx.application.Application;
import view.StartView;

public class Main {

    public static void main(String[] args) {
        // Detect headless: only true if explicitly set
        boolean headless = Boolean.getBoolean("java.awt.headless");

        if (!headless) {
            Application.launch(StartView.class, args);
        } else {
            System.out.println("Running in headless mode (Docker)");
            // You can run backend tasks here if needed
        }
    }
}