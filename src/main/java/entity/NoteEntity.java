package entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

    @Column(name="annotation")
    private String annotation;

    @Column(name="createdAt")
    private LocalDateTime createdAt;

    @Column(name="updatedAt")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "notebook_id", nullable = false)
    private NoteBookEntity notebook;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(
            name = "note_tags",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )

    private Set<TagEntity> tags = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public NoteEntity(String title, String content, String annotation) {
        this.title = title;
        this.content = content;
        this.annotation = annotation;
    }

    public NoteEntity() {}

    public Long getId() { return id; }

    public String getTitle() { return title; }

    public String getContent() { return content; }

    public String getAnnotation() { return annotation; }

    public NoteBookEntity getNotebook() { return notebook; }

    public LocalDateTime getCreatedTime() { return createdAt; }

    public LocalDateTime getUpdatedTime() { return updatedAt; }

    public void addTag(TagEntity tag) {
        tags.add(tag);
    }

    public void removeTag(TagEntity tag) {
        tags.remove(tag);
    }

    public Set<TagEntity> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public void setTitle(String newTitle) {
        this.title = newTitle;
    }

    public void setContent(String newContent) {
        this.content = newContent;
    }

    public void setAnnotation(String newAnnotation) { this.annotation = newAnnotation; }

    public void setNotebook(NoteBookEntity notebook) { this.notebook = notebook; }

    @Override
    public String toString() {
        return title;
    }

    public String getFormattedCreatedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return createdAt != null ? createdAt.format(formatter) : "";
    }
}
