package services;

import controller.ManageNotebookController;
import entity.entities.NotebookEntity;
import javafx.stage.Stage;
import javafx.stage.Window;
import session.NotebookSession;
import util.AlertUtil;
import util.Localization;
import util.NavigationUtil;

import java.util.function.Consumer;

public class DashboardNavigationService {

    private DashboardNavigationService() {/* This utility class should not be instantiated */}

    public static void openManageNotebooks(Stage owner, NotebookEntity activeNotebook, Consumer<NotebookEntity> onNotebookChanged) {
        NavigationUtil.openWindow(owner, "/FXML/manage_notebooks.fxml", "notebook.manage_label", false, true,
                (ManageNotebookController controller) -> {
                    controller.loadNotebooks();
                    controller.setActiveNotebook(activeNotebook);
                });

        NotebookEntity lastNotebook = NotebookSession.getLastCreatedNotebook();
        if (lastNotebook != null) {
            onNotebookChanged.accept(lastNotebook);
            NotebookSession.clear();
        }
    }

    public static void logout(Window window, Runnable onCleanup) {

        boolean confirmed = AlertUtil.showConfirmation(window, Localization.get("button.logout"), Localization.get("account.logout_warning"));

        if (!confirmed) return;

        // Session cleanup before navigation
        if (onCleanup != null) {
            onCleanup.run();
        }

        Stage stage = (Stage) window;
        NavigationUtil.replaceScene(stage, "/FXML/entry.fxml", "entry.window_title", false);
    }
}
