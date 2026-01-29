package repository;

import model.User;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private List<User> users;
    private User currentUser;

    public UserRepository() {
        users = new ArrayList<>();
    }

    public void addUser(User user) {
        users.add(user);
    }

    private User findUser(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        } return null;
    }

    public boolean authenticateUser(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return false;
        }

        User user = findUser(username);
        if (user == null) {
            return false;
        }

        if (user.getPassword().equals(password)) {
            currentUser = user;
            return true;
        }
        return false;
    }

}
