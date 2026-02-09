package session;

import entity.UserEntity;

public class UserSession {
    private static UserSession userInstance;
    private UserEntity loggedInUser;

    private UserSession() {}

    public static UserSession getUserInstance() {
        if (userInstance == null) {
            userInstance = new UserSession();
        }
        return userInstance;
    }

    public void setUser(UserEntity user) { this.loggedInUser = user; }

    public UserEntity getUser() { return loggedInUser; }

    public void cleanUserSession() {
        loggedInUser = null;
    }
}
