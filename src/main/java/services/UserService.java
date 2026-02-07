package services;

import dao.user.JpaUserDao;
import entity.UserEntity;
import util.BcryptPasswordHasher;

public class UserService {
    private JpaUserDao userDao;

    public UserService(JpaUserDao userDao) {
        this.userDao = userDao;
    }

    public UserEntity login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return null;
        }
        UserEntity user = userDao.findByUsername(username);
        if (user == null) return null;

        String hashedPassword = user.getPasswordHash();
        if (BcryptPasswordHasher.verifyPassword(password, hashedPassword)) {
            return user;
        } else {
            return null;
        }
    }
}
