package temporary_note_storage;

import model.Note;
import java.util.ArrayList;
import java.util.List;

public class NoteStorage {
    private List<Note> notes;

    public NoteStorage() {
        notes = new ArrayList<>();
    }

    // Gam
    public synchronized void createNote(Note note) {
        if (note == null) throw new IllegalArgumentException("note cannot be null");
        notes.add(note);
    }

    // Swostika
    public void updateNote(Note note) {
        //implement yourselves
    }

    // Dinal
    public void deleteNote(Note note) {
        //implement yourselves
    }

    public List<Note> getNotesByUser(int userId) {
        List<Note> userNotes = new ArrayList<>();
        for (Note note : notes) {
            if (note.getUserId() == userId) {
                userNotes.add(note);
            }
        }
        return userNotes;
    }
}
