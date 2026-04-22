package session;

import entity.entities.NotebookEntity;

public class NotebookSession {

    private NotebookSession() { /* Private constructor to prevent instantiation */ }

    private static NotebookEntity lastCreatedNotebook;

    public static void setLastCreatedNotebook(NotebookEntity notebook) {
        lastCreatedNotebook = notebook;
    }

    public static NotebookEntity getLastCreatedNotebook() {
        return lastCreatedNotebook;
    }

    public static void clear() {
        lastCreatedNotebook = null;
    }
}
