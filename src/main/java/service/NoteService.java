package service;

import model.Note;
import temporary_note_storage.NoteStorage;

import java.util.List;

public class NoteService {
    private final NoteStorage storage;

    public NoteService(NoteStorage storage) {
        this.storage = storage;
    }

    public Note createNote(String title, String content, int userId) {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Title cannot be empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new ValidationException("Content cannot be empty");
        }
        if (title.trim().length() > 255) {
            throw new ValidationException("Title must be 255 characters or less");
        }

        Note note = new Note(title.trim(), content.trim(), userId);
        storage.createNote(note);
        return note;
    }

    public List<Note> getNotesByUser(int userId) {
        return storage.getNotesByUser(userId);
    }
}
