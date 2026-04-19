package session;

import entity.entities.NoteEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoteSessionTest {

    @Test
    void setGetClearLastCreatedNote() {
        NoteEntity n = new NoteEntity();
        NoteSession.setLastCreatedNote(n);
        assertSame(n, NoteSession.getLastCreatedNote());
        NoteSession.clear();
        assertNull(NoteSession.getLastCreatedNote());
    }
}

