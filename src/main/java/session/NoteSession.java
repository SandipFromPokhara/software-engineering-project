package session;

import entity.entities.NoteEntity;

public class NoteSession {

    private NoteSession() {/* Private constructor to prevent instantiation */}

    private static NoteEntity lastCreatedNote;

    public static NoteEntity getLastCreatedNote() { return lastCreatedNote; }

    public static void setLastCreatedNote(NoteEntity note) { lastCreatedNote = note; }

    public static void clear() { lastCreatedNote = null; }
}
