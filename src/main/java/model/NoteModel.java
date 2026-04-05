package model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NoteModel {
    private int id;
    private String title;
    private String content;
    private String annotation;
    private Set<TagModel> tags;
    private NotebookModel notebook;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NoteModel(String title, String content, String annotation, NotebookModel notebook) {
        this.title = title;
        this.content = content;
        this.notebook = notebook;
        this.annotation = annotation;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.tags = new HashSet<>();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAnnotation() { return annotation; }

    public void setAnnotation(String annotation) { this.annotation = annotation; }

    public void addTag(TagModel tag) {
        if (tag != null) {
            tags.add(tag);
        }
    }

    public void removeTag(TagModel tag) {
        tags.remove(tag);
    }

    public Set<TagModel> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Updates new content and syncs last created or modified timestamp.
     */
    public void updateContent(String title, String content, String annotation) {
        this.title = title;
        this.content = content;
        this.annotation = annotation;
        this.updatedAt = LocalDateTime.now();
    }

    public NotebookModel getNotebook() {
        return notebook;
    }

    public void setNotebook(NotebookModel notebook) {
        this.notebook = notebook;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt(){ return updatedAt ;}

    @Override
    public String toString() {
        return "Note {Id: " + id + ", Title: '" + title + "', Annotation: '" + annotation + "', Notebook: '" + notebook + "', Timestamp: " + createdAt + "}";
    }
}

