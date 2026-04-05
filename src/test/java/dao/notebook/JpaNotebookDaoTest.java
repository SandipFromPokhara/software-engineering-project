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
    static void setupBeforeClass() throws Exception {
        dao = new JpaUserDao();
        String unique = String.valueOf(System.currentTimeMillis());
        testUser = new UserEntity("Test", "User", "testuser" + unique, "test" + unique + "@example.com");
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
        NotebookEntity notebook = new NotebookEntity("JUnit 5 test", testUser);
        notebookDao.save(notebook);

        NotebookEntity retrievedNotebook = notebookDao.findById(notebook.getId());

        assertNotNull(retrievedNotebook);
        assertEquals("JUnit 5 test", retrievedNotebook.getTitle());

        notebookDao.delete(notebook);
    }

    @Test
    void testFindNotebookByTitle() {
        NotebookEntity notebook = new NotebookEntity("Find By Title", testUser);
        notebookDao.save(notebook);

        List<NotebookEntity> retrievedList = notebookDao.findByTitle("Find By Title");

        assertNotNull(retrievedList);
        assertFalse(retrievedList.isEmpty());
        boolean found = false;
        for (NotebookEntity n : retrievedList) {
            if (n.getTitle().equals("Find By Title")) {
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
        NotebookEntity notebook = new NotebookEntity("User's Notebook", testUser);
        notebookDao.save(notebook);

        List<NotebookEntity> retrievedList = notebookDao.findByUser(testUser);
        assertFalse(retrievedList.isEmpty());

        boolean found = false;
        for (NotebookEntity n : retrievedList) {
            if (n.getTitle().equals("User's Notebook")) {
                found = true;
                break;
            }
        }
        assertTrue(found);

        notebookDao.delete(notebook);
    }

    @Test
    void testFindByUserWithNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            notebookDao.findByUser(null);
        });
    }


    @Test
    void testUpdateNotebook() {
        NotebookEntity notebook = new NotebookEntity("Test update method", testUser);
        notebookDao.save(notebook);

        NotebookEntity retrievedNotebook = notebookDao.findById(notebook.getId());
        retrievedNotebook.setTitle("Test update method for NoteBook entity");
        notebookDao.update(retrievedNotebook);

        assertNotNull(retrievedNotebook);
        assertEquals("Test update method for NoteBook entity", retrievedNotebook.getTitle());
    }

    @Test
    void testUpdateNull() {
        assertThrows(IllegalArgumentException.class, () -> notebookDao.update(null));
    }

    @Test
    void testDeleteNotebook() {
        NotebookEntity notebook = new NotebookEntity("Testing delete method", testUser);
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