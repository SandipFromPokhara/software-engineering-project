package repository;

import model.Note;
import java.util.ArrayList;
import java.util.List;

public class NoteRepository {
    private List<Note> notes;

    public NoteRepository() {
        notes = new ArrayList<>();
    }

    // Gam
    public void createNote(Note note) {
        //implement yourselves
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
