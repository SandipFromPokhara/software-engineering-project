package dao.note;

import dao.notebook.JpaNotebookDao;
import dao.user.JpaUserDao;
import datasource.MariaDbJpaConnection;
import entity.entities.NotebookEntity;
import entity.entities.NoteEntity;
import entity.entities.UserEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JpaNoteDaoTest {

    private static JpaNoteDao noteDao;
    private static JpaNotebookDao notebookDao;
    private static JpaUserDao userDao;

    private static UserEntity testUser;
    private static NotebookEntity testNotebook;

    @BeforeAll
    static void setup() {
        userDao = new JpaUserDao();
        notebookDao = new JpaNotebookDao();
        noteDao = new JpaNoteDao();

        String unique = String.valueOf(System.currentTimeMillis());

        testUser = new UserEntity("Test","User","user"+unique,"mail"+unique+"@test.com");
        userDao.save(testUser);

        testNotebook = new NotebookEntity(testUser);
        notebookDao.save(testNotebook);
    }

    @AfterAll
    static void cleanup() {
        List<NoteEntity> notes = noteDao.findByNotebookWithTranslations(testNotebook);
        for (NoteEntity note : notes) {
            note.getTranslations().clear();
            noteDao.delete(note);
        }

        notebookDao.delete(testNotebook);
        userDao.delete(testUser);
        MariaDbJpaConnection.shutdown();
    }

    @Test
    void testSaveAndFindNote() {
        NoteEntity note = new NoteEntity();

        var cTranslation = note.createTranslation("EN");
        cTranslation.setTitle("Title");
        cTranslation.setContent("Content");
        cTranslation.setAnnotation("Ok");

        note.setNotebook(testNotebook);
        testNotebook.getNotes().add(note);

        noteDao.save(note);

        List<NoteEntity> notes = noteDao.findByNotebookWithTranslations(testNotebook);

        // Find specific note in the list
        NoteEntity found = notes.stream()
                                .filter(n -> n.getId().equals(note.getId()))
                                .findFirst()
                                .orElse(null);

        assertNotNull(found, "Note should be found in the database");

        var translation = found.getTranslations().get("EN");

        assertNotNull(translation, "Translation 'EN' should be present");
        assertEquals("Title", translation.getTitle());
    }

    @Test
    void testUpdateNote() {
        NoteEntity note = new NoteEntity();

        var createTranslation = note.createTranslation("EN");
        createTranslation.setTitle("Old Title");
        createTranslation.setContent("Old Content");
        createTranslation.setAnnotation("Old Annotation");

        note.setNotebook(testNotebook);
        testNotebook.getNotes().add(note);
        noteDao.save(note);

        createTranslation.setTitle("Updated Title");
        createTranslation.setContent("Updated Content");
        createTranslation.setAnnotation("Updated Annotation");

        noteDao.update(note);

        List<NoteEntity> fetchedNotes = noteDao.findByNotebookWithTranslations(testNotebook);
        NoteEntity updated = fetchedNotes.stream()
                                        .filter(n -> n.getId().equals(note.getId()))
                                        .findFirst()
                                        .orElse(null);

        assertNotNull(updated, "Updated note should exist");
        var translation = updated.getTranslations().get("EN");

        assertNotNull(translation, "Translation should not be null");
        assertEquals("Updated Title", translation.getTitle());
        assertEquals("Updated Content", translation.getContent());
        assertEquals("Updated Annotation", translation.getAnnotation());
    }

    @Test
    void deleteNote() {
        NoteEntity note = new NoteEntity();

        var translation = note.createTranslation("EN");
        translation.setTitle("Test Delete");
        translation.setContent("This will test delete functionality");
        translation.setAnnotation("OK");

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
