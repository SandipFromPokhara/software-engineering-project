package model;

import java.time.LocalDateTime;

public class NotebookModel {

    private int id;
    private String title;
    private UserModel user;
    private String langCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NotebookModel(String title, UserModel user, String langCode) {
        this.title = title;
        this.user = user;
        this.langCode = langCode;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public String geLangCode() { return this.langCode; }

    public void setLangCode(String langCode) { this.langCode = langCode; }

    @Override
    public String toString() {
        return "NoteBook {Id: " + id + ", Title: '" + title + "', user: '" + user + "', Timestamp: " + createdAt + "}";
    }
}
