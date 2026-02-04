package model;

import java.time.LocalDateTime;

public class NoteModel {
    private int id;
    private String title;
    private String content;
    private NoteBookModel notebook;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NoteModel(String title, String content, NoteBookModel notebook) {
        this.title = title;
        this.content = content;
        this.notebook = notebook;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public int getId() { return id; }

    public String getTitle() { return title; }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

    /**
     * Updates new content and syncs last created or modified timestamp.
     */
    public void updateContent(String newContent) {
        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
    }

    public NoteBookModel getNotebook() { return notebook; }

    public void setNotebook(NoteBookModel notebook) { this.notebook = notebook; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "Note {Id: " + id + ", Title: '" + title + "', Notebook: '" + notebook + "', Timestamp: " + createdAt + "}";
    }
}

