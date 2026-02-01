package repository;

import model.UserModel;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private List<UserModel> users;
    private UserModel currentUser;

    public UserRepository() {
        users = new ArrayList<>();
    }

    public void addUser(UserModel user) {
        users.add(user);
    }

    private UserModel findUser(String username) {
        for (UserModel user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        } return null;
    }

    public boolean authenticateUser(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return false;
        }

        UserModel user = findUser(username);
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
