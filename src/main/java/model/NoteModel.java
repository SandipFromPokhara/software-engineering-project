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
        id = idCounter++;
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public int getId() { return id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

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

