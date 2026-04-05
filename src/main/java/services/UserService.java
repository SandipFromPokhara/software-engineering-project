package services;

import dao.user.JpaUserDao;
import entity.entities.UserEntity;
import security.PasswordHasher;

public class UserService {
    private final JpaUserDao userDao;
    private final PasswordHasher passwordHasher;

    public UserService(JpaUserDao userDao, PasswordHasher passwordHasher) {
        this.userDao = userDao;
        this.passwordHasher = passwordHasher;
    }

    public UserEntity login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return null;
        }
        UserEntity user = userDao.findByUsername(username);
        if (user == null) return null;

        String hashedPassword = user.getPasswordHash();
        if (passwordHasher.verify(password, hashedPassword)) {
            return user;
        } else {
            return null;
        }
    }
}