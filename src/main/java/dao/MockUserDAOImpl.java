package dao;

import model.User;
import java.util.ArrayList;
import java.util.List;

//Mock implementation of UserDAO for development/testing without database
public class MockUserDAOImpl implements UserDAO {

    // Temporary storage in memory
    private List<User> users = new ArrayList<>();

    //Creates a new user in memory
    @Override
    public boolean createUser(User user) {
        users.add(user);
        System.out.println("✓ Mock DAO: User created - " + user.toString());
        return true;
    }

    //Retrieves a user by username from memory
    @Override
    public User getUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    //Retrieves a user by email from memory
    @Override
    public User getUserByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    //Checks if a username exists in memory
    @Override
    public boolean usernameExists(String username) {
        return getUserByUsername(username) != null;
    }

    //Checks if an email exists in memory
    @Override
    public boolean emailExists(String email) {
        return getUserByEmail(email) != null;
    }

    //Helper method to get all users
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    //Helper method to clear all users
    public void clearAll() {
        users.clear();
        System.out.println("✓ Mock DAO: All users cleared");
    }
}
