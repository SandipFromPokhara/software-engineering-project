package entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="notes")
public class NoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="title")
    private String title;

    @Column(name="content")
    private String content;

    @Column(name="createdAt")
    private LocalDateTime createdAt;

    @Column(name="updatedAt")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "notebook_id", nullable = false)
    private NoteBookEntity notebook;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public NoteEntity(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public NoteEntity() {}

    public Long getId() { return id; }

    public String getTitle() { return title; }

    public String getContent() { return content; }

    public LocalDateTime getCreatedTime() { return createdAt; }

    public LocalDateTime getUpdatedTime() { return updatedAt; }

    public void setTitle(String newTitle) {
        this.title = newTitle;
    }

    public void setContent(String newContent) {
        this.content = newContent;
    }

    public NoteBookEntity getNotebook() {
        return notebook;
    }

    public void setNotebook(NoteBookEntity notebook) {
        this.notebook = notebook;
    }
}
