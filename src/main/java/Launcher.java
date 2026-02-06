/**
 * Launcher class for JavaFX applications
 * This class solves the "JavaFX runtime components are missing" error
 * when running from IntelliJ IDEA without VM options.
 *
 * HOW TO USE:
 * - Run this class instead of running View classes directly
 * - Change the APP_TO_LAUNCH constant to test different views
 */
public class Launcher {

    // Change this to launch different applications:
    // "entry" - EntryView (main entry point)
    // "login" - LoginView
    // "signup" - SignUpView
    // "create_note" - CreateNoteView
    // "main" - Main application
    private static final String APP_TO_LAUNCH = "entry";

    public static void main(String[] args) {
        // Set database credentials
        System.setProperty("DB_USER", "notevaultUser");
        System.setProperty("DB_PASSWORD", "group1oPassw0rD");

        switch (APP_TO_LAUNCH.toLowerCase()) {
            case "entry":
                view.EntryView.main(args);
                break;
            case "login":
                view.LoginView.main(args);
                break;
            case "signup":
                view.SignUpView.main(args);
                break;
            case "create_note":
                view.CreateNoteView.main(args);
                break;
            default:
                // Default to Main application
                Main.main(args);
        }
    }
}
