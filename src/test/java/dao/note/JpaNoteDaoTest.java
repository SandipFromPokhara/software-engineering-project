package dao.note;

import dao.notebook.JpaNoteBookDao;
import dao.user.JpaUserDao;
import datasource.MariaDbJpaConnection;
import entity.NotebookEntity;
import entity.NoteEntity;
import entity.UserEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JpaNoteDaoTest {

    private static JpaNoteDao noteDao;
    private static JpaNoteBookDao notebookDao;
    private static JpaUserDao userDao;

    private static UserEntity testUser;
    private static NotebookEntity testNotebook;

    @BeforeAll
    static void setup() {
        userDao = new JpaUserDao();
        notebookDao = new JpaNoteBookDao();
        noteDao = new JpaNoteDao();

        String unique = String.valueOf(System.currentTimeMillis());

        testUser = new UserEntity("Test","User","user"+unique,"mail"+unique+"@test.com");
        userDao.save(testUser);

        testNotebook = new NotebookEntity("TestNotebook", testUser);
        notebookDao.save(testNotebook);
    }

    @AfterAll
    static void cleanup() {
        testNotebook.getNotes().forEach(noteDao::delete);
        notebookDao.delete(testNotebook);
        userDao.delete(testUser);
        MariaDbJpaConnection.shutdown();
    }

    @Test
    void testSaveAndFindNote() {
        NoteEntity note = new NoteEntity("Title", "Content", "Ok");
        note.setNotebook(testNotebook);
        noteDao.save(note);

        NoteEntity found = noteDao.findById(note.getId());

        assertNotNull(found);
        assertEquals("Title", found.getTitle());
        assertEquals("Content", found.getContent());
        assertEquals("Ok", found.getAnnotation());
    }

    @Test
    void testUpdateNote() {
        NoteEntity note = new NoteEntity("Old Title", "Old Content", "Old Annotation");
        note.setNotebook(testNotebook);
        noteDao.save(note);

        note.setTitle("Updated Title");
        note.setContent("Updated Content");
        note.setAnnotation("Updated Annotation");
        noteDao.update(note);

        NoteEntity updated = noteDao.findById(note.getId());
        assertNotNull(updated);
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Updated Content", updated.getContent());
        assertEquals("Updated Annotation", updated.getAnnotation());
    }

    @Test
    void deleteNote() {
        NoteEntity note = new NoteEntity("Test Delete", "This will test delete functionality", "OK");
        note.setNotebook(testNotebook);
        noteDao.save(note);

        Long noteId = note.getId();
        assertNotNull(noteDao.findById(noteId));

        noteDao.delete(note);
        assertNull(noteDao.findById(noteId));
    }

    @Test
    void testSaveNullNoteThrows() {
        assertThrows(IllegalArgumentException.class, () -> noteDao.save(null));
    }

    @Test
    void testUpdateNullNoteThrows() {
        assertThrows(IllegalArgumentException.class, () -> noteDao.update(null));
    }

    @Test
    void testDeleteNullNoteThrows() {
        assertThrows(IllegalArgumentException.class, () -> noteDao.delete(null));
    }

    @Test
    void testFindByTitleNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> noteDao.findByTitle(null));
    }

    @Test
    void testFindByNotebookNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> noteDao.findByNotebook(null));
    }
}