package session;

import entity.NoteEntity;

public class NoteSession {

    private static NoteEntity lastCreatedNote;

    public static NoteEntity getLastCreatedNote() { return lastCreatedNote; }

    public static void setLastCreatedNote(NoteEntity note) { lastCreatedNote = note; }

    public static void clear() { lastCreatedNote = null; }
}
