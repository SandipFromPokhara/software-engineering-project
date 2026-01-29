package model;

public class User {
    private int id;
    private static int idCounter = 1;
    private String firstName;
    private String lastName;
    private String username;
    private String password;

    public User(String firstName, String lastName, String username, String password) {
        id = idCounter++;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
    }

    public int getId() { return id; }

    public String getFirstName() { return this.firstName; }

    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return this.lastName; }

    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUsername() { return this.username; }

    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return this.password; }

    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "User {ID: " + id + ", Full Name: '" + firstName + " " + lastName + "', Username: '" + username + "'}";
    }
}
