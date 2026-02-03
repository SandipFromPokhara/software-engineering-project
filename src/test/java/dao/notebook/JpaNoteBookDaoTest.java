package dao.notebook;

import dao.user.JpaUserDao;
import datasource.MariaDbJpaConnection;
import entity.NoteBookEntity;
import entity.UserEntity;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class JpaNoteBookDaoTest {

    private static  JpaNoteBookDao notebookDao;
    private static NoteBookEntity notebook;

    private static JpaUserDao dao;
    private static UserEntity testUser;

    @BeforeAll
    static void setupBeforeClass() throws Exception {
        dao = new JpaUserDao();
        String unique = String.valueOf(System.currentTimeMillis());
        testUser = new UserEntity("Test", "User", "testuser" + unique, "test" + unique + "@example.com");
        dao.save(testUser);
        notebookDao = new JpaNoteBookDao();
    }

    @AfterAll
    static void tearDown() {
        if (testUser != null) {
            dao.delete(testUser);
        }
        MariaDbJpaConnection.shutdown();
    }

    @Test
    void saveNotebookTest() {
        NoteBookEntity notebook = new NoteBookEntity("JUnit 5 test", testUser);
        notebookDao.save(notebook);

        NoteBookEntity retrievedNotebook = notebookDao.findById(notebook.getId());

        assertNotNull(retrievedNotebook);
        assertEquals("JUnit 5 test", retrievedNotebook.getTitle());
    }

    @Test
    void findNotebookByTitleTest() {
        NoteBookEntity notebook = new NoteBookEntity("Find By Title", testUser);
        notebookDao.save(notebook);

        NoteBookEntity retrievedNotebook = notebookDao.findById(notebook.getId());

        assertNotNull(retrievedNotebook);
        assertEquals("Find By Title", retrievedNotebook.getTitle());
    }

    @Test
    void updateNotebookTest() {
        NoteBookEntity notebook = new NoteBookEntity("Test update method", testUser);
        notebookDao.save(notebook);

        NoteBookEntity retrievedNotebook = notebookDao.findById(notebook.getId());
        retrievedNotebook.setTitle("Test update method for NoteBook entity");
        notebookDao.update(retrievedNotebook);

        assertNotNull(retrievedNotebook);
        assertEquals("Test update method for NoteBook entity", retrievedNotebook.getTitle());
    }

    @Test
    void deleteNotebookTesT() {
        NoteBookEntity notebook = new NoteBookEntity("Testing delete method", testUser);
        notebookDao.save(notebook);

        NoteBookEntity managedNotebook = notebookDao.findById(notebook.getId());
        assertNotNull(managedNotebook);

        notebookDao.delete(managedNotebook);

        NoteBookEntity deletedNotebook = notebookDao.findById(notebook.getId());

        assertNull(deletedNotebook);
    }
}