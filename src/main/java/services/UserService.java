package services;

import dao.user.JpaUserDao;
import entity.UserEntity;
import util.BcryptPasswordHasher;

public class UserService {
    private JpaUserDao userDao;

    public UserService(JpaUserDao userDao) {
        this.userDao = userDao;
    }

    public boolean login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return false;
        }
        UserEntity user = userDao.findByUsername(username);
        if (user == null) return false;

        String hashedPassword = user.getPasswordHash();
        return BcryptPasswordHasher.verifyPassword(password, hashedPassword);
    }
}
