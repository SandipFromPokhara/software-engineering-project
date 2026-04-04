package services;

import dao.note.JpaNoteDao;
import dao.notebook.JpaNoteBookDao;
import dao.tag.JpaTagDao;
import entity.*;
import session.UserSession;

import java.util.List;
import java.util.Set;

/**
 * Service layer for Note operations
 */
public class NoteService {

    private final JpaNoteDao noteDao;
    private final JpaNoteBookDao notebookDao;
    private final JpaTagDao tagDao;

    public NoteService() {
        this.noteDao = new JpaNoteDao();
        this.notebookDao = new JpaNoteBookDao();
        this.tagDao = new JpaTagDao();
    }

    public NoteService(JpaNoteDao noteDao, JpaNoteBookDao notebookDao, JpaTagDao tagDao) {
        this.noteDao = noteDao;
        this.notebookDao = notebookDao;
        this.tagDao = tagDao;
    }

    /**
     * Creates a new note with title, content, and annotation
     */
    public NoteEntity createNote(String title, String content, String annotation, NotebookEntity notebookParameter, Set<String> tagNames) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        UserEntity currentUser = UserSession.getUserInstance().getUser();
        if (currentUser == null) {
            throw new IllegalStateException("No user logged in. Please login first.");
        }

        // Get or create notebook for current user
        NotebookEntity notebookToUse = (notebookParameter != null) ? notebookParameter : getOrCreatePersonalNotebook(currentUser);

        // Create and save note - separate content and annotation
        NoteEntity note = new NoteEntity(title.trim(), content != null ? content : "", annotation != null ? annotation: "");
        note.setNotebook(notebookToUse);

        // Shift tag handling logic from controller
        if (tagNames != null) {
            for (String tagName : tagNames) {
                String normalized = tagName.trim().toLowerCase();
                TagEntity tag = tagDao.findByName(normalized);

                if (tag == null) {
                    tag = tagDao.save(new TagEntity(normalized));
                }

                note.addTag(tag);
            }
        }

        return noteDao.save(note);
    }

    public NotebookEntity createNotebook(String name) {
        UserEntity user = UserSession.getUserInstance().getUser();

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Notebook name cannot be empty");
        }

        NotebookEntity notebook = new NotebookEntity(name.trim(), user);
        return notebookDao.save(notebook);
    }

    /**
     * Gets or creates a notebook for the logged-in user
     */
    private NotebookEntity getOrCreatePersonalNotebook(UserEntity user) {

        // Find existing notebook for this user
        List<NotebookEntity> notebooks = notebookDao.findByUser(user);

        if (!notebooks.isEmpty()) {
            return notebooks.get(0);
        }

        // Create personal notebook for user (e.g., "John's Notebook")
        String notebookName = user.getFirstName() + "'s Notebook";
        NotebookEntity unsavedNotebook  = new NotebookEntity(notebookName, user);
        return notebookDao.save(unsavedNotebook);
    }

    public NoteEntity save(NoteEntity note) {
        return noteDao.save(note);
    }
}
