package util.events;

import entity.NoteEntity;

public class NoteCreatedEvent {
    private final NoteEntity note;

    public NoteCreatedEvent(NoteEntity note) {
        this.note = note;
    }

    public NoteEntity getNote() {
        return note;
    }
}
