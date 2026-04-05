package dao.user;

import datasource.MariaDbJpaConnection;
import entity.entities.UserEntity;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class JpaUserDaoTest {

    private static JpaUserDao dao;
    private static UserEntity testUser;

    @BeforeAll
    static void setupBeforeClass() {
    dao = new JpaUserDao();
    }

    @BeforeEach
    void setUp() {
        String unique = String.valueOf(System.currentTimeMillis());
        testUser = new UserEntity("Test", "User", "tester" + unique, "tester" + unique + "@example.com");
        dao.save(testUser);
    }

    @AfterEach
    void tearDown() {
        if (testUser != null) {
            dao.delete(testUser);
        }
        MariaDbJpaConnection.shutdown();
    }

    @Test
    void saveUserTest() {

        UserEntity retrievedUser = dao.findById(testUser.getId());

        assertNotNull(retrievedUser);
        assertEquals("Test", retrievedUser.getFirstName());
        assertEquals("User", retrievedUser.getLastName());
        assertEquals(testUser.getUsername(), retrievedUser.getUsername());
        assertEquals(testUser.getEmail(), retrievedUser.getEmail());
    }

    @Test
    void testFindById() {
        UserEntity retrievedUser = dao.findById(testUser.getId());

        assertNotNull(retrievedUser);
        assertEquals(testUser.getId(), retrievedUser.getId());
    }

    @Test
    void testFindUserByUsername() {
        UserEntity retrievedUser = dao.findByUsername(testUser.getUsername());

        assertNotNull(retrievedUser);
        assertEquals(testUser.getUsername(), retrievedUser.getUsername());

        UserEntity notFound = dao.findByUsername("null_user");
        assertNull(notFound);
    }

    @Test
    void testFindUserByEmail() {
        UserEntity retrievedUser = dao.findByEmail(testUser.getEmail());

        assertNotNull(retrievedUser);
        assertEquals(testUser.getEmail(), retrievedUser.getEmail());

        UserEntity notFound = dao.findByEmail("null@saveduser.com");
        assertNull(notFound);
    }

    @Test
    void testUpdateUserInfo() {
        UserEntity retrievedUser = dao.findById(testUser.getId());
        retrievedUser.setFirstName("Test1");
        dao.update(retrievedUser);
        assertNotNull(retrievedUser);
        assertEquals("Test1", retrievedUser.getFirstName());
    }

    @Test
    void testDeleteUser() {
        UserEntity retrievedUser = dao.findById(testUser.getId());
        dao.delete(retrievedUser);

        UserEntity deletedUser = dao.findById(testUser.getId());

        assertNull(deletedUser);
    }

    @Test
    void testSaveNullUser() {
        assertThrows(IllegalArgumentException.class, () -> dao.save(null));
    }

    @Test
    void testUpdateNullUser() {
        assertThrows(IllegalArgumentException.class, () -> dao.update(null));
    }
    @Test
    void testDeleteNullUser() {
        assertThrows(IllegalArgumentException.class, () -> dao.delete(null));
    }
}