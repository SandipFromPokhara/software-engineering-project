package services;

import dao.user.JpaUserDao;
import entity.entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import security.IPasswordHasher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private JpaUserDao userDao;
    private UserService userService;
    private IPasswordHasher passwordHasher;

    @BeforeEach
    void setUp() {
        userDao = mock(JpaUserDao.class);
        passwordHasher = mock(IPasswordHasher.class);
        userService = new UserService(userDao, passwordHasher);
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
        when(passwordHasher.verify("wrongpassword", "$2a$10$randomHashpassword")).thenReturn(false);

        assertNull(userService.login("user", "wrongpassword"));
    }

    @Test
    void test_login_correctPassword_returnsUser() {
        UserEntity user = new UserEntity();
        user.setUsername("user");
        user.changePasswordHash("$2a$10$correcthashedpassword");
        when(userDao.findByUsername("user")).thenReturn(user);
        when(passwordHasher.verify("correctpassword", "$2a$10$correcthashedpassword")).thenReturn(true);

        UserEntity result = userService.login("user", "correctpassword");
        assertNotNull(result);
        assertEquals("user", result.getUsername());
    }
}
