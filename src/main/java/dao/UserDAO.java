package dao;

import model.User;

public interface UserDAO {

    //Creates a new user in the data store
    boolean createUser(User user);

    //Retrieves a user by username
    User getUserByUsername(String username);

    //Retrieves a user by email
    User getUserByEmail(String email);

    //Checks if a username already exists
    boolean usernameExists(String username);

    //Checks if an email already exists
    boolean emailExists(String email);
}
