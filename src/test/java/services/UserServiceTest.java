package services;

import dao.user.JpaUserDao;
import entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import util.BcryptPasswordHasher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private JpaUserDao userDao;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userDao = mock(JpaUserDao.class);
        userService = new UserService(userDao);
    }

    @Test
    void test_login_nullUsername_returnsNull() {
        assertNull(userService.login(null, "password"));
    }

    @Test
    void test_login_blankUsername_returnsNull() {
        assertNull(userService.login("   ", "password"));
    }

    @Test
    void test_login_nullPassword_returnsNull() {
        assertNull(userService.login("username", null));
    }

    @Test
    void test_login_blankPassword_returnsNull() {
        assertNull(userService.login("username", "   "));
    }

    @Test
    void test_login_userNotFound_returnsNull() {
        when(userDao.findByUsername("user")).thenReturn(null);
        assertNull(userService.login("user", "password"));
    }

    @Test
    void test_login_wrongPassword_returnsNull() {
        UserEntity user = new UserEntity();
        user.setUsername("user");
        user.changePasswordHash("$2a$10$randomHashpassword");
        when(userDao.findByUsername("user")).thenReturn(user);

        // Mock static method verifyPassword to return false
        try (MockedStatic<BcryptPasswordHasher> mockedHasher = Mockito.mockStatic(BcryptPasswordHasher.class)) {
            mockedHasher.when(() -> BcryptPasswordHasher.verifyPassword("wrongpassword", "$2a$10$randomHashpassword"))
                    .thenReturn(false);

            assertNull(userService.login("user", "wrongpassword"));
        }
    }

    @Test
    void test_login_correctPassword_returnsUser() {
        UserEntity user = new UserEntity();
        user.setUsername("user");
        user.changePasswordHash("$2a$10$correcthashedpassword"); // example hash
        when(userDao.findByUsername("user")).thenReturn(user);

        // Mock static method verifyPassword to return true
        try (MockedStatic<BcryptPasswordHasher> mockedHasher = Mockito.mockStatic(BcryptPasswordHasher.class)) {
            mockedHasher.when(() -> BcryptPasswordHasher.verifyPassword("correctpassword", "$2a$10$correcthashedpassword"))
                    .thenReturn(true);

            UserEntity result = userService.login("user", "correctpassword");
            assertNotNull(result);
            assertEquals("user", result.getUsername());
        }
    }
}
