package dao.notebook;

import dao.user.JpaUserDao;
import datasource.MariaDbJpaConnection;
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
}
