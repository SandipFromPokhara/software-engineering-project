package model;

import java.time.LocalDateTime;

public class NoteBookModel {

    private int id;
    private String title;
    private UserModel user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NoteBookModel(String title, UserModel user) {
        this.title = title;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public int getId() { return id; }

    public String getTitle() { return title; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setTitle(String title) { this.title = title; }

    public UserModel getUser() { return user; }

    public void setUser(UserModel user) { this.user = user; }

    @Override
    public String toString() {
        return "NoteBook {Id: " + id + ", Title: '" + title + "', user: '" + user + "', Timestamp: " + createdAt + "}";
    }
}
