package services;

import dao.note.JpaNoteDao;
import dao.notebook.JpaNoteBookDao;
import entity.NoteEntity;
import entity.NoteBookEntity;
import entity.UserEntity;

import java.util.List;
import java.util.ArrayList;

public class NoteService {

    private final JpaNoteDao noteDao;
    private final JpaNoteBookDao notebookDao;

    public NoteService() {
        this.noteDao = new JpaNoteDao();
        this.notebookDao = new JpaNoteBookDao();
    }

    /**
     * Creates a new note in the specified notebook
     *
     * @param title      Note title (required)
     * @param content    Note content (optional)
     * @param notebookId ID of the notebook to contain the note
     * @return Created NoteEntity
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException         if notebook not found or save fails
     */
    public NoteEntity createNote(String title, String content, Long notebookId) {
        // Validate inputs
        validateTitle(title);
        validateNotebookId(notebookId);

        // Normalize content (null -> empty string)
        if (content == null) {
            content = "";
        }

        // Find the notebook, create default if not found
        NoteBookEntity notebook = findOrCreateDefaultNotebook(notebookId);

        // Create and save the note
        NoteEntity note = new NoteEntity(title.trim(), content);
        note.setNotebook(notebook);

        try {
            NoteEntity savedNote = noteDao.save(note);
            System.out.println(" Note saved successfully: " + savedNote.getTitle() + " (ID: " + savedNote.getId() + ")");
            return savedNote;
        } catch (Exception e) {
            System.err.println(" Failed to save note: " + e.getMessage());
            throw new RuntimeException("Failed to create note: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a note by ID
     *
     * @param noteId ID of note to delete
     * @throws RuntimeException if note not found or delete fails
     */
    public void deleteNote(Long noteId) {
        validateNoteId(noteId);

        try {
            NoteEntity note = noteDao.findById(noteId);
            if (note == null) {
                throw new RuntimeException("Note not found with ID: " + noteId);
            }

            noteDao.delete(note);
            System.out.println("Note deleted successfully: " + note.getTitle() + " (ID: " + noteId + ")");

        } catch (Exception e) {
            System.err.println("Failed to delete note: " + e.getMessage());
            throw new RuntimeException("Failed to delete note: " + e.getMessage(), e);
        }
    }

    /**
     * Gets all notes (for displaying saved notes list)
     *
     * @return List of all notes
     */
    public List<NoteEntity> getAllNotes() {
        try {
            // For now, get all notes. In future, filter by user
            List<NoteEntity> notes = noteDao.findByTitle(""); // This gets all notes
            System.out.println("📄 Retrieved " + notes.size() + " notes from database");
            return notes;
        } catch (Exception e) {
            System.err.println(" Error retrieving notes: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Find note by ID
     *
     * @param noteId Note ID
     * @return NoteEntity or null if not found
     */
    public NoteEntity findNoteById(Long noteId) {
        validateNoteId(noteId);
        return noteDao.findById(noteId);
    }

    // Helper method to find or create default notebook
    private NoteBookEntity findOrCreateDefaultNotebook(Long notebookId) {
        NoteBookEntity notebook = notebookDao.findById(notebookId);

        if (notebook == null) {
            // Create a default notebook if none exists
            System.out.println(" Notebook ID " + notebookId + " not found, creating default notebook...");

            // Create a test user first if needed
            UserEntity defaultUser = createOrGetDefaultUser();

            // Create default notebook
            notebook = new NoteBookEntity("My Notes", defaultUser);
            notebook = notebookDao.save(notebook);

            System.out.println(" Default notebook created: " + notebook.getTitle() + " (ID: " + notebook.getId() + ")");
        }

        return notebook;
    }

    private UserEntity createOrGetDefaultUser() {
        // This is a simple implementation for testing
        // In production, this would get the current logged-in user
        try {
            dao.user.JpaUserDao userDao = new dao.user.JpaUserDao();

            // Try to find an existing user
            UserEntity existingUser = userDao.findById(1L);
            if (existingUser != null) {
                return existingUser;
            }

            // Create a default user if none exists
            String timestamp = String.valueOf(System.currentTimeMillis());
            UserEntity defaultUser = new UserEntity(
                    "Default",
                    "User",
                    "defaultuser" + timestamp,
                    "default" + timestamp + "@notevault.com"
            );

            return userDao.save(defaultUser);

        } catch (Exception e) {
            System.err.println("Error creating default user: " + e.getMessage());
            throw new RuntimeException("Failed to create default user: " + e.getMessage(), e);
        }
    }

    private void validateNoteId(Long noteId) {
        if (noteId == null) {
            throw new IllegalArgumentException("Note ID cannot be null");
        }
        if (noteId <= 0) {
            throw new IllegalArgumentException("Note ID must be positive");
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Note title cannot be empty");
        }
    }

    private void validateNotebookId(Long notebookId) {
        if (notebookId == null) {
            throw new IllegalArgumentException("Notebook ID cannot be null");
        }
        if (notebookId <= 0) {
            throw new IllegalArgumentException("Notebook ID must be positive");
        }
    }
}
