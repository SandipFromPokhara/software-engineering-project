package services;

import dao.user.JpaUserDao;
import entity.UserEntity;
import util.BcryptPasswordHasher;

public class UserService {
    private JpaUserDao userDao;
    private static UserEntity loggedInUser = null;

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
        boolean authenticated = BcryptPasswordHasher.verifyPassword(password, hashedPassword);

        if (authenticated) {
            loggedInUser = user;
            NoteService.setCurrentUser(user); // Set user for NoteService
        }

        return authenticated;
    }

    public static UserEntity getLoggedInUser() {
        return loggedInUser;
    }

    public static void logout() {
        loggedInUser = null;
        NoteService.setCurrentUser(null);
    }
}
