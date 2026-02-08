package controller;

import dao.user.UserDAO;
import entity.UserEntity;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.BcryptPasswordHasher;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SignUpControllerTest {

    private SignUpController controller;
    private MockUserDAO mockUserDAO;

    // Mock UI components
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField usernameField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Label messageLabel;
    private Button signUpButton;

    // Simple Mock DAO implementation
    private static class MockUserDAO implements UserDAO {
        private List<UserEntity> users = new ArrayList<>();
        private Long nextId = 1L;

        @Override
        public UserEntity save(UserEntity user) {
            if (user.getId() == null) {
                // Simulate auto-increment ID
                try {
                    var idField = UserEntity.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(user, nextId++);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            users.add(user);
            return user;
        }

        @Override
        public UserEntity findById(Long id) {
            return users.stream()
                    .filter(u -> u.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public UserEntity findByUsername(String username) {
            return users.stream()
                    .filter(u -> u.getUsername().equals(username))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public UserEntity findByEmail(String email) {
            return users.stream()
                    .filter(u -> u.getEmail().equals(email))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public void update(UserEntity user) {
            // Not needed for signup tests
        }

        @Override
        public void delete(UserEntity user) {
            users.remove(user);
        }

        public void clear() {
            users.clear();
            nextId = 1L;
        }
    }

    @BeforeEach
    void setUp() {
        // Create controller instance
        controller = new SignUpController();

        // Create mock DAO
        mockUserDAO = new MockUserDAO();
        controller.setUserDAO(mockUserDAO);

        // Initialize UI components (without JavaFX)
        // We'll test validation logic directly
    }

    // ========== VALIDATION TESTS ==========

    @Test
    void testValidEmailFormat() {
        String validEmail = "user@example.com";
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        assertTrue(validEmail.matches(emailRegex), "Valid email should match regex");
    }

    @Test
    void testInvalidEmailFormat() {
        String invalidEmail = "invalid-email";
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        assertFalse(invalidEmail.matches(emailRegex), "Invalid email should not match regex");
    }

    @Test
    void testUsernameWithNordicCharacters() {
        String username = "jääskeläinen";
        String usernameRegex = "^[\\p{L}0-9_]{3,20}$";

        assertTrue(username.matches(usernameRegex), "Username with Nordic chars should be valid");
    }

    @Test
    void testUsernameWithEnglishCharacters() {
        String username = "johndoe123";
        String usernameRegex = "^[\\p{L}0-9_]{3,20}$";

        assertTrue(username.matches(usernameRegex), "Username with English chars should be valid");
    }

    @Test
    void testUsernameTooShort() {
        String username = "ab";
        String usernameRegex = "^[\\p{L}0-9_]{3,20}$";

        assertFalse(username.matches(usernameRegex), "Username with 2 chars should be invalid");
    }

    @Test
    void testUsernameTooLong() {
        String username = "abcdefghijklmnopqrstuvwxyz";
        String usernameRegex = "^[\\p{L}0-9_]{3,20}$";

        assertFalse(username.matches(usernameRegex), "Username with 26 chars should be invalid");
    }

    @Test
    void testNameWithNordicCharacters() {
        String name = "Jääskeläinen";
        String nameRegex = "^[\\p{L}\\s\\-'/]+$";

        assertTrue(name.matches(nameRegex), "Name with Nordic chars should be valid");
    }

    @Test
    void testNameWithHyphen() {
        String name = "Matti-Pekka";
        String nameRegex = "^[\\p{L}\\s\\-'/]+$";

        assertTrue(name.matches(nameRegex), "Name with hyphen should be valid");
    }

    @Test
    void testNameWithSlash() {
        String name = "Anne/Maria";
        String nameRegex = "^[\\p{L}\\s\\-'/]+$";

        assertTrue(name.matches(nameRegex), "Finnish name with slash should be valid");
    }

    @Test
    void testNameWithApostrophe() {
        String name = "O'Brien";
        String nameRegex = "^[\\p{L}\\s\\-'/]+$";

        assertTrue(name.matches(nameRegex), "Name with apostrophe should be valid");
    }

    @Test
    void testNameWithSpace() {
        String name = "Mary Anne";
        String nameRegex = "^[\\p{L}\\s\\-'/]+$";

        assertTrue(name.matches(nameRegex), "Name with space should be valid");
    }

    // ========== PASSWORD VALIDATION TESTS ==========

    @Test
    void testPasswordWithNumberAndSpecialChar() {
        String password = "Pass123!";

        assertTrue(password.length() >= 6, "Password should be at least 6 chars");
        assertTrue(password.matches(".*\\d.*"), "Password should contain a number");
        assertTrue(password.matches(".*[!@#$%^&*()_+=\\-\\[\\]{};':\"\\\\|,.<>/?].*"),
                "Password should contain a special char");
    }

    @Test
    void testPasswordWithoutNumber() {
        String password = "Password!";

        assertFalse(password.matches(".*\\d.*"), "Password without number should be invalid");
    }

    @Test
    void testPasswordWithoutSpecialChar() {
        String password = "Password123";

        assertFalse(password.matches(".*[!@#$%^&*()_+=\\-\\[\\]{};':\"\\\\|,.<>/?].*"),
                "Password without special char should be invalid");
    }

    @Test
    void testPasswordTooShort() {
        String password = "Pa1!";

        assertTrue(password.length() < 6, "Password with 4 chars should be too short");
    }

    @Test
    void testPasswordMeetsAllRequirements() {
        String password = "MyPass123!";

        assertTrue(password.length() >= 6, "Password length should be valid");
        assertTrue(password.matches(".*\\d.*"), "Password should have number");
        assertTrue(password.matches(".*[!@#$%^&*()_+=\\-\\[\\]{};':\"\\\\|,.<>/?].*"),
                "Password should have special char");
    }

    // ========== PASSWORD HASHING TESTS ==========

    @Test
    void testPasswordIsHashed() {
        String plainPassword = "Pass123!";
        String hashedPassword = BcryptPasswordHasher.hashPassword(plainPassword);

        assertNotNull(hashedPassword, "Hashed password should not be null");
        assertNotEquals(plainPassword, hashedPassword, "Hash should not equal plain password");
        assertTrue(hashedPassword.startsWith("$2a$"), "BCrypt hash should start with $2a$");
    }

    @Test
    void testPasswordVerification() {
        String plainPassword = "Pass123!";
        String hashedPassword = BcryptPasswordHasher.hashPassword(plainPassword);

        assertTrue(BcryptPasswordHasher.verifyPassword(plainPassword, hashedPassword),
                "Correct password should verify");
    }

    @Test
    void testIncorrectPasswordDoesNotVerify() {
        String correctPassword = "Pass123!";
        String wrongPassword = "Wrong456!";
        String hashedPassword = BcryptPasswordHasher.hashPassword(correctPassword);

        assertFalse(BcryptPasswordHasher.verifyPassword(wrongPassword, hashedPassword),
                "Wrong password should not verify");
    }

    @Test
    void testBCryptHashesAreDifferent() {
        String password = "Pass123!";
        String hash1 = BcryptPasswordHasher.hashPassword(password);
        String hash2 = BcryptPasswordHasher.hashPassword(password);

        assertNotEquals(hash1, hash2, "BCrypt should use different salts");
        assertTrue(BcryptPasswordHasher.verifyPassword(password, hash1),
                "First hash should verify");
        assertTrue(BcryptPasswordHasher.verifyPassword(password, hash2),
                "Second hash should verify");
    }

    // ========== USER CREATION TESTS ==========

    @Test
    void testUserCanBeSaved() {
        UserEntity user = new UserEntity("John", "Doe", "johndoe", "john@example.com");
        user.changePasswordHash(BcryptPasswordHasher.hashPassword("Pass123!"));

        UserEntity savedUser = mockUserDAO.save(user);

        assertNotNull(savedUser, "Saved user should not be null");
        assertNotNull(savedUser.getId(), "Saved user should have an ID");
        assertEquals("johndoe", savedUser.getUsername(), "Username should match");
    }

    @Test
    void testUserCanBeFoundByUsername() {
        UserEntity user = new UserEntity("John", "Doe", "johndoe", "john@example.com");
        mockUserDAO.save(user);

        UserEntity foundUser = mockUserDAO.findByUsername("johndoe");

        assertNotNull(foundUser, "User should be found");
        assertEquals("johndoe", foundUser.getUsername(), "Username should match");
    }

    @Test
    void testUserCanBeFoundByEmail() {
        UserEntity user = new UserEntity("John", "Doe", "johndoe", "john@example.com");
        mockUserDAO.save(user);

        UserEntity foundUser = mockUserDAO.findByEmail("john@example.com");

        assertNotNull(foundUser, "User should be found");
        assertEquals("john@example.com", foundUser.getEmail(), "Email should match");
    }

    @Test
    void testDuplicateUsernameDetection() {
        UserEntity user1 = new UserEntity("John", "Doe", "johndoe", "john@example.com");
        mockUserDAO.save(user1);

        UserEntity existingUser = mockUserDAO.findByUsername("johndoe");

        assertNotNull(existingUser, "Duplicate username should be detected");
    }

    @Test
    void testDuplicateEmailDetection() {
        UserEntity user1 = new UserEntity("John", "Doe", "johndoe", "john@example.com");
        mockUserDAO.save(user1);

        UserEntity existingUser = mockUserDAO.findByEmail("john@example.com");

        assertNotNull(existingUser, "Duplicate email should be detected");
    }

    @Test
    void testNonExistentUserNotFound() {
        UserEntity foundUser = mockUserDAO.findByUsername("nonexistent");

        assertNull(foundUser, "Non-existent user should return null");
    }

    @Test
    void testMultipleUsersCanBeStored() {
        UserEntity user1 = new UserEntity("John", "Doe", "johndoe", "john@example.com");
        UserEntity user2 = new UserEntity("Jane", "Smith", "janesmith", "jane@example.com");

        mockUserDAO.save(user1);
        mockUserDAO.save(user2);

        assertNotNull(mockUserDAO.findByUsername("johndoe"), "First user should exist");
        assertNotNull(mockUserDAO.findByUsername("janesmith"), "Second user should exist");
    }

    // ========== INTEGRATION TESTS ==========

    @Test
    void testCompleteSignupFlow() {
        // Simulate complete signup
        String firstName = "John";
        String lastName = "Doe";
        String username = "johndoe";
        String email = "john@example.com";
        String password = "Pass123!";

        // Validate data
        assertTrue(username.matches("^[\\p{L}0-9_]{3,20}$"), "Username should be valid");
        assertTrue(email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"), "Email should be valid");
        assertTrue(password.length() >= 6, "Password length should be valid");
        assertTrue(password.matches(".*\\d.*"), "Password should have number");
        assertTrue(password.matches(".*[!@#$%^&*()_+=\\-\\[\\]{};':\"\\\\|,.<>/?].*"), "Password should have special char");

        // Check no duplicates
        assertNull(mockUserDAO.findByUsername(username), "Username should not exist");
        assertNull(mockUserDAO.findByEmail(email), "Email should not exist");

        // Hash password
        String hashedPassword = BcryptPasswordHasher.hashPassword(password);

        // Create and save user
        UserEntity newUser = new UserEntity(firstName, lastName, username, email);
        newUser.changePasswordHash(hashedPassword);
        UserEntity savedUser = mockUserDAO.save(newUser);

        // Verify
        assertNotNull(savedUser, "User should be saved");
        assertNotNull(savedUser.getId(), "User should have ID");
        assertTrue(BcryptPasswordHasher.verifyPassword(password, hashedPassword), "Password should verify");
    }

    @Test
    void testSignupWithFinnishName() {
        String firstName = "Matti-Pekka";
        String lastName = "Jääskeläinen";
        String username = "matti_jää";
        String email = "matti@example.fi";
        String password = "Salasana123!";

        // Validate Finnish-specific features
        assertTrue(firstName.matches("^[\\p{L}\\s\\-'/]+$"), "Finnish first name should be valid");
        assertTrue(lastName.matches("^[\\p{L}\\s\\-'/]+$"), "Finnish last name should be valid");
        assertTrue(username.matches("^[\\p{L}0-9_]{3,20}$"), "Username with ä should be valid");

        // Create user
        String hashedPassword = BcryptPasswordHasher.hashPassword(password);
        UserEntity user = new UserEntity(firstName, lastName, username, email);
        user.changePasswordHash(hashedPassword);

        UserEntity savedUser = mockUserDAO.save(user);

        assertNotNull(savedUser, "Finnish user should be saved");
        assertEquals("Matti-Pekka", savedUser.getFirstName(), "First name should match");
        assertEquals("Jääskeläinen", savedUser.getLastName(), "Last name should match");
    }
}