package dao;

import model.User;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MockUserDAOImpl.
 * Tests all CRUD operations and validation logic.
 *
 * @author NoteVault Team
 * @version 1.0
 */
class MockUserDAOImplTest {

    private MockUserDAOImpl mockDAO;

    @BeforeEach
    void setUp() {
        mockDAO = new MockUserDAOImpl();
    }

    @AfterEach
    void tearDown() {
        mockDAO.clearAll();
    }

    @Test
    void testCreateUser_Success() {
        User user = new User("John", "Doe", "johndoe", "john@example.com", "password123");
        boolean result = mockDAO.createUser(user);

        assertTrue(result);
        assertEquals(1, mockDAO.getAllUsers().size());
    }

    @Test
    void testUsernameExists_True() {
        User user = new User("John", "Doe", "johndoe", "john@example.com", "password123");
        mockDAO.createUser(user);

        assertTrue(mockDAO.usernameExists("johndoe"));
    }

    @Test
    void testUsernameExists_False() {
        assertFalse(mockDAO.usernameExists("nonexistent"));
    }

    @Test
    void testEmailExists_True() {
        User user = new User("John", "Doe", "johndoe", "john@example.com", "password123");
        mockDAO.createUser(user);

        assertTrue(mockDAO.emailExists("john@example.com"));
    }

    @Test
    void testEmailExists_False() {
        assertFalse(mockDAO.emailExists("nonexistent@example.com"));
    }

    @Test
    void testGetUserByUsername_Found() {
        User user = new User("John", "Doe", "johndoe", "john@example.com", "password123");
        mockDAO.createUser(user);

        User found = mockDAO.getUserByUsername("johndoe");
        assertNotNull(found);
        assertEquals("John", found.getFirstName());
        assertEquals("johndoe", found.getUsername());
    }

    @Test
    void testGetUserByUsername_NotFound() {
        User found = mockDAO.getUserByUsername("notfound");
        assertNull(found);
    }

    @Test
    void testGetUserByEmail_Found() {
        User user = new User("Jane", "Smith", "janesmith", "jane@example.com", "password456");
        mockDAO.createUser(user);

        User found = mockDAO.getUserByEmail("jane@example.com");
        assertNotNull(found);
        assertEquals("Jane", found.getFirstName());
        assertEquals("jane@example.com", found.getEmail());
    }

    @Test
    void testGetUserByEmail_NotFound() {
        User found = mockDAO.getUserByEmail("notfound@example.com");
        assertNull(found);
    }

    @Test
    void testMultipleUsers() {
        User user1 = new User("John", "Doe", "john123", "john@example.com", "pass1");
        User user2 = new User("Jane", "Smith", "jane456", "jane@example.com", "pass2");

        mockDAO.createUser(user1);
        mockDAO.createUser(user2);

        assertEquals(2, mockDAO.getAllUsers().size());
        assertTrue(mockDAO.usernameExists("john123"));
        assertTrue(mockDAO.usernameExists("jane456"));
        assertTrue(mockDAO.emailExists("john@example.com"));
        assertTrue(mockDAO.emailExists("jane@example.com"));
    }

    @Test
    void testClearAll() {
        User user1 = new User("John", "Doe", "john123", "john@example.com", "pass1");
        User user2 = new User("Jane", "Smith", "jane456", "jane@example.com", "pass2");

        mockDAO.createUser(user1);
        mockDAO.createUser(user2);
        assertEquals(2, mockDAO.getAllUsers().size());

        mockDAO.clearAll();
        assertEquals(0, mockDAO.getAllUsers().size());
        assertFalse(mockDAO.usernameExists("john123"));
    }
}
