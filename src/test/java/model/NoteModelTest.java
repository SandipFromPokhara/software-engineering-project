package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoteModelTest {

    @Test
    void updateNote() {
        NoteModel note = new NoteModel("Title", "content", "annotation", null);
        note.updateNote("New title", "New content", "New annotation");

        assertEquals("New title", note.getTitle());
        assertEquals("New content", note.getContent());
        assertEquals("New annotation", note.getAnnotation());
        assertNotNull(note.getUpdatedAt());
    }

}