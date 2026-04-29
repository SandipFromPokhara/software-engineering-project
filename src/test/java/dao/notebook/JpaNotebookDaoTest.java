package dao.notebook;

import dao.note.JpaNoteDao;
import dao.user.JpaUserDao;
import datasource.MariaDbJpaConnection;
import entity.entities.NoteEntity;
import entity.entities.NotebookEntity;
import entity.entities.UserEntity;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JpaNotebookDaoTest {

    private static JpaNotebookDao notebookDao;

    private static JpaUserDao dao;
    private static UserEntity testUser;

    @BeforeAll
    static void setupBeforeClass() {
        dao = new JpaUserDao();
        String unique = String.valueOf(System.currentTimeMillis());
        testUser = new UserEntity("Test", "User", "tester" + unique, "test" + unique + "@example.com");
        dao.save(testUser);
        notebookDao = new JpaNotebookDao();
    }

    @AfterAll
    static void tearDown() {
        if (testUser != null) {
            dao.delete(testUser);
        }
        MariaDbJpaConnection.shutdown();
    }

    @Test
    void testSaveNotebook() {
        NotebookEntity notebook = new NotebookEntity(testUser);

        var translation = notebook.createTranslation("EN");
        translation.setTitle("JUnit 5 test");
        notebookDao.save(notebook);

        NotebookEntity retrievedNotebook = notebookDao.findById(notebook.getId());
        var getTranslate = retrievedNotebook.getTranslations().get("EN");

        assertNotNull(getTranslate);
        assertEquals("JUnit 5 test", getTranslate.getTitle());

        notebookDao.delete(notebook);
    }

    @Test
    void testFindNotebookByTitle() {
        NotebookEntity notebook = new NotebookEntity(testUser);

        var translation = notebook.createTranslation("EN");
        String findTitle = "Find By Title";
        translation.setTitle(findTitle);
        notebookDao.save(notebook);

        List<NotebookEntity> retrievedList = notebookDao.findByTitle(findTitle);

        assertNotNull(retrievedList);
        assertFalse(retrievedList.isEmpty());
        boolean found = false;
        for (NotebookEntity book : retrievedList) {
            var getTranslate = book.getTranslations().get("EN");
            if (getTranslate != null && getTranslate.getTitle().equals(findTitle)) {
                found = true;
                break;
            }
        }
        assertTrue(found);

        notebookDao.delete(notebook);
    }

    @Test
    void testFindByTitleWithNull() {
        assertThrows(IllegalArgumentException.class, () -> notebookDao.findByTitle(null));
    }

    @Test
    void testFindByUser() {
        NotebookEntity notebook = new NotebookEntity(testUser);

        var translation = notebook.createTranslation("EN");
        translation.setTitle("User's Notebook");
        notebookDao.save(notebook);

        List<NotebookEntity> retrievedList = notebookDao.findByUser(testUser);
        assertFalse(retrievedList.isEmpty());

        boolean found = false;
        for (NotebookEntity book : retrievedList) {
            var getTranslate = book.getTranslations().get("EN");
            if (getTranslate != null && getTranslate.getTitle().equals("User's Notebook")) {
                found = true;
                break;
            }
        }
        assertTrue(found);

        notebookDao.delete(notebook);
    }

    @Test
    void testFindByUserWithNull() {
        assertThrows(IllegalArgumentException.class, () -> notebookDao.findByUser(null));
    }

    @Test
    void testFindByIdReturnsNullWhenMissing() {
        NotebookEntity result = notebookDao.findById(999999L);
        assertNull(result);
    }

    @Test
    void testUpdateNotebook() {
        NotebookEntity notebook = new NotebookEntity(testUser);

        var translation = notebook.createTranslation("EN");
        translation.setTitle("Test update method");
        notebookDao.save(notebook);

        NotebookEntity retrievedNotebook = notebookDao.findById(notebook.getId());
        var getTranslate = retrievedNotebook.getTranslations().get("EN");

        getTranslate.setTitle("Test update method for NoteBook entity");
        notebookDao.update(retrievedNotebook);

        var updated = notebookDao.findById(retrievedNotebook.getId());
        var updatedT = updated.getTranslations().get("EN");

        assertNotNull(retrievedNotebook);
        assertEquals("Test update method for NoteBook entity", updatedT.getTitle());

        notebookDao.delete(updated);
    }

    @Test
    void testUpdateNull() {
        assertThrows(IllegalArgumentException.class, () -> notebookDao.update(null));
    }

    @Test
    void testDeleteNotebook() {
        NotebookEntity notebook = new NotebookEntity(testUser);

        var translation = notebook.createTranslation("EN");
        translation.setTitle("Testing delete method");
        notebookDao.save(notebook);

        NotebookEntity managedNotebook = notebookDao.findById(notebook.getId());
        assertNotNull(managedNotebook);

        notebookDao.delete(managedNotebook);

        NotebookEntity deletedNotebook = notebookDao.findById(notebook.getId());

        assertNull(deletedNotebook);
    }

    @Test
    void testDeleteNull() {
        assertThrows(IllegalArgumentException.class, () -> notebookDao.delete(null));
    }

    @Test
    void deleteWithNotesRemovesNotesAndNotebookAtomically() {
        JpaUserDao userDao = new JpaUserDao();
        JpaNoteDao noteDao = new JpaNoteDao();

        // Create and persist a user with a unique username/email to avoid collisions with other tests
        String unique = String.valueOf(System.currentTimeMillis());
        UserEntity user = new UserEntity("Test", "User", "it_user_" + unique, "it_" + unique + "@example.com");
        user = userDao.save(user);
        MariaDbJpaConnection.closeEntityManager();

        // Create and persist a notebook for the user
        NotebookEntity notebook = new NotebookEntity(user);
        notebook = notebookDao.save(notebook);
        MariaDbJpaConnection.closeEntityManager();

        // Create and persist a couple of notes attached to the notebook
        NoteEntity n1 = new NoteEntity();
        n1.setNotebook(notebook);
        noteDao.save(n1);

        NoteEntity n2 = new NoteEntity();
        n2.setNotebook(notebook);
        noteDao.save(n2);

        MariaDbJpaConnection.closeEntityManager();

        List<NoteEntity> before = noteDao.findByNotebook(notebook);
        assertEquals(2, before.size(), "There should be 2 notes before deletion");

        // Perform atomic delete
        notebookDao.deleteWithNotes(notebook);
        MariaDbJpaConnection.closeEntityManager();

        // Notebook should be gone
        NotebookEntity maybe = notebookDao.findById(notebook.getId());
        assertNull(maybe, "Notebook should be removed by deleteWithNotes");

        // Notes should also be gone
        List<NoteEntity> after = noteDao.findByNotebook(notebook);
        assertTrue(after.isEmpty(), "All notes belonging to the notebook should be deleted");
        
        // Clean up the user we created for this test
        MariaDbJpaConnection.closeEntityManager();
        userDao.delete(user);
    }
}