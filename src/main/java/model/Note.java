package model;

import java.time.LocalDateTime;

public class Note {
    private int id;
    private static int idCounter = 1;
    private String title;
    private String content;
    private int userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Note(String title, String content, int userId) {
        this.id = idCounter++;
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static int getIdCounter() {
        return idCounter;
    }

    public static void setIdCounter(int idCounter) {
        Note.idCounter = idCounter;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


    /**
     * Updates new content and syncs last created or modified timestamp.
     */
    public void updateContent(String newContent) {
        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
    }

    public int getUserId() { return userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "Note {Id: " + id + ", Title: '" + title + "', UserId: '" + userId + "', Timestamp: " + createdAt + "}";
    }
}

