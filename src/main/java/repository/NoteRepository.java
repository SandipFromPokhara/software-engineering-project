package repository;

import model.NoteModel;

import java.util.ArrayList;
import java.util.List;

public class NoteRepository {
    private List<NoteModel> notes;

    public NoteRepository() {
        notes = new ArrayList<>();
    }

    // Gam
    public void createNote(NoteModel note) {
        //implement yourselves
    }

    // Swostika
    public void updateNote(NoteModel note) {
        //implement yourselves
    }

    // Dinal
    public void deleteNote(NoteModel note) {
        //implement yourselves
    }

    public List<NoteModel> getNotesByUser(int userId) {
        List<NoteModel> userNotes = new ArrayList<>();
        for (NoteModel note : notes) {
            if (note.getUserId() == userId) {
                userNotes.add(note);
            }
        }
        return userNotes;
    }
}
