package session;

import entity.NoteBookEntity;

public class NotebookSession {

    private static NoteBookEntity lastCreatedNotebook;

    public static void setLastCreatedNotebook(NoteBookEntity notebook) {
        lastCreatedNotebook = notebook;
    }

    public static NoteBookEntity getLastCreatedNotebook() {
        return lastCreatedNotebook;
    }

    public static void clear() {
        lastCreatedNotebook = null;
    }
}
