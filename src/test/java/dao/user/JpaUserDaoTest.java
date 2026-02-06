package dao.user;

import datasource.MariaDbJpaConnection;
import entity.UserEntity;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class JpaUserDaoTest {

    private static JpaUserDao dao;
    private static UserEntity testUser;

    @BeforeAll
    static void setupBeforeClass() throws Exception {
    dao = new JpaUserDao();
    }

    @BeforeEach
    void setUp() {
        String unique = String.valueOf(System.currentTimeMillis());
        testUser = new UserEntity("Test", "User", "testuser" + unique, "testuser" + unique + "@example.com");
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
    void findUserByUsernameTest() {
        UserEntity retrievedUser = dao.findById(testUser.getId());

        assertNotNull(retrievedUser);
        assertEquals(testUser.getUsername(), retrievedUser.getUsername());
    }

    @Test
    void findUserByEmailTest() {
        UserEntity retrievedUser = dao.findById(testUser.getId());

        assertNotNull(retrievedUser);
        assertEquals(testUser.getEmail(), retrievedUser.getEmail());
    }

    @Test
    void updateUserInfoTest() {
        UserEntity retrievedUser = dao.findById(testUser.getId());
        retrievedUser.setFirstName("Test1");
        dao.update(retrievedUser);
        assertNotNull(retrievedUser);
        assertEquals("Test1", retrievedUser.getFirstName());
    }

    @Test
    void deleteUserTest() {
        UserEntity retrievedUser = dao.findById(testUser.getId());
        dao.delete(retrievedUser);

        UserEntity deletedUser = dao.findById(testUser.getId());

        assertNull(deletedUser);
    }

}