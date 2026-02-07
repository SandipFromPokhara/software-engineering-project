package services;

import dao.note.JpaNoteDao;
import dao.notebook.JpaNoteBookDao;
import entity.NoteEntity;
import entity.NoteBookEntity;
import entity.UserEntity;

/**
 * Service layer for Note operations
 */
public class NoteService {

    private final JpaNoteDao noteDao;
    private final JpaNoteBookDao notebookDao;
    private Long cachedNotebookId = null;
    private static UserEntity currentUser = null;

    public NoteService() {
        this.noteDao = new JpaNoteDao();
        this.notebookDao = new JpaNoteBookDao();
    }

    public NoteService(UserEntity user) {
        this.noteDao = new JpaNoteDao();
        this.notebookDao = new JpaNoteBookDao();
        currentUser = user;
        this.cachedNotebookId = null;
    }

    /**
     * Sets the current logged-in user
     */
    public static void setCurrentUser(UserEntity user) {
        currentUser = user;
    }

    /**
     * Gets the current logged-in user
     */
    public static UserEntity getCurrentUser() {
        return currentUser;
    }

    /**
     * Creates a new note with title, content, and annotation
     */
    public NoteEntity createNote(String title, String content, String annotation) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        if (currentUser == null) {
            throw new IllegalStateException("No user logged in. Please login first.");
        }

        // Get or create notebook for current user
        NoteBookEntity notebook = getNotebookForUser();

        // Create and save note - separate content and annotation
        String noteContent = (content != null ? content : "");
        String noteAnnotation = (annotation != null ? annotation : "");
        NoteEntity note = new NoteEntity(title.trim(), noteContent, noteAnnotation);
        note.setNotebook(notebook);

        return noteDao.save(note);
    }

    /**
     * Gets or creates a notebook for the logged-in user
     */
    private NoteBookEntity getNotebookForUser() {
        // Use cached notebook if available
        if (cachedNotebookId != null) {
            NoteBookEntity notebook = notebookDao.findById(cachedNotebookId);
            if (notebook != null) return notebook;
        }

        // Find existing notebook for this user (efficient query)
        NoteBookEntity notebook = notebookDao.findByUserId(currentUser.getId());
        if (notebook != null) {
            cachedNotebookId = notebook.getId();
            return notebook;
        }

        // Create personal notebook for user (e.g., "John's Notebook")
        String notebookName = currentUser.getFirstName() + "'s Notebook";
        notebook = new NoteBookEntity(notebookName, currentUser);
        notebook = notebookDao.save(notebook);
        cachedNotebookId = notebook.getId();

        return notebook;
    }
}
