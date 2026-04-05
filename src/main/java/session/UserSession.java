package session;

import entity.entities.UserEntity;

public class UserSession {
    private static final UserSession INSTANCE = new UserSession();
    private UserEntity loggedInUser;

    private UserSession() {}

    public static UserSession getUserInstance() {
        return INSTANCE;
    }

    public void setUser(UserEntity user) { this.loggedInUser = user; }

    public UserEntity getUser() { return loggedInUser; }
}