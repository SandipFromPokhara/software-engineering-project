package services;

import dao.note.JpaNoteDao;
import dao.notebook.JpaNoteBookDao;
import entity.*;
import util.UserSession;

import java.util.List;

/**
 * Service layer for Note operations
 */
public class NoteService {

    private final JpaNoteDao noteDao;
    private final JpaNoteBookDao notebookDao;
    private Long cachedNotebookId = null;

    public NoteService() {
        this.noteDao = new JpaNoteDao();
        this.notebookDao = new JpaNoteBookDao();
    }

    /**
     * Creates a new note with title, content, and annotation
     */
    public NoteEntity createNote(String title, String content, String annotation, NoteBookEntity notebookParameter) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        UserEntity currentUser = UserSession.getUserInstance().getUser();
        if (currentUser == null) {
            throw new IllegalStateException("No user logged in. Please login first.");
        }

        // Get or create notebook for current user
        NoteBookEntity notebookToUse = (notebookParameter != null) ? notebookParameter : getOrCreatePersonalNotebook(currentUser);

        // Create and save note - separate content and annotation
        NoteEntity note = new NoteEntity(title.trim(), content != null ? content : "", annotation != null ? annotation: "");
        note.setNotebook(notebookToUse);

        return noteDao.save(note);
    }

    /**
     * Gets or creates a notebook for the logged-in user
     */
    private NoteBookEntity getOrCreatePersonalNotebook(UserEntity user) {
        // Use cached notebook if available
        if (cachedNotebookId != null) {
            NoteBookEntity cachedNotebook = notebookDao.findById(cachedNotebookId);
            if (cachedNotebook != null) return cachedNotebook;
        }

        // Find existing notebook for this user
        List<NoteBookEntity> notebooks = notebookDao.findByUser(user);
        if (!notebooks.isEmpty()) {
            NoteBookEntity existingNotebook = notebooks.get(0);
            cachedNotebookId = existingNotebook.getId();
            return existingNotebook;
        }

        // Create personal notebook for user (e.g., "John's Notebook")
        String notebookName = user.getFirstName() + "'s Notebook";
        NoteBookEntity unsavedNotebook  = new NoteBookEntity(notebookName, user);
        NoteBookEntity savedNotebook = notebookDao.save(unsavedNotebook);
        cachedNotebookId = savedNotebook.getId();

        return savedNotebook;
    }
}
