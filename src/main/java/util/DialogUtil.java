package util;

import javafx.scene.control.Alert;
import javafx.stage.Window;

public class DialogUtil {

    public static void showAbout(Window owner) {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("About");
        about.setHeaderText("NoteVault - Notebook Manager\n" + "Version 0.1");

        about.setContentText(
                "A simple notebook and note management application.\n\n" +
                        "Developed by:\n" +
                        "  Dinal Maha Vidanelage\n" +
                        "  Sandip Ranjit\n" +
                        "  Swostika Lama\n" +
                        "  Twe He Gam\n\n" +
                        "Software Engineering DevOps Project 2026\n" +
                        "Metropolia University of Applied Sciences\n\n" +
                        "Technologies:\n" +
                        "  Docker, Java, JavaFX, JPA (Hibernate), JUni5\n" +
                        "  Jenkins, Kubernetes, MariaDB"
        );
        if (owner != null) {
            about.initOwner(owner);
        }
        about.showAndWait();
    }
}
