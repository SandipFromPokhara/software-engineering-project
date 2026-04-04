package entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="note_translation", uniqueConstraints = { @UniqueConstraint(columnNames = {"note_id", "language_code"}) })
public class NoteTranslationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="title", nullable = false)
    private String title;

    @Column(name="content", columnDefinition = "TEXT")
    private String content;

    @Column(name="annotation")
    private String annotation;

    @Column(name="createdAt")
    private LocalDateTime createdAt;

    @Column(name="updatedAt")
    private LocalDateTime updatedAt;

    @Column(name="language_code", nullable = false)
    private String langCode;

    @ManyToOne
    @JoinColumn(name = "note_id", nullable = false)
    private NoteEntity note;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public NoteTranslationEntity() {}

    public Long getId() { return id; }

    public String getTitle() { return title; }

    public String getContent() { return content; }

    public String getAnnotation() { return annotation; }

    public String getLangCode() { return langCode; }

    public NoteEntity getNote() { return note; }

    public void setTitle(String newTitle) {
        this.title = newTitle;
    }

    public void setContent(String newContent) {
        this.content = newContent;
    }

    public void setAnnotation(String newAnnotation) { this.annotation = newAnnotation; }

    public void setLangCode(String langCode) { this.langCode = langCode; }

    public void setNote(NoteEntity note) { this.note = note; }

    @Override
    public String toString() {
        return title;
    }
}