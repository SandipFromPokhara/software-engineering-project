package entity.translationentities;

import entity.base.BaseTranslationEntity;
import entity.entities.NoteEntity;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name="note_translation", uniqueConstraints = { @UniqueConstraint(columnNames = {"note_id", "language_code"}) })
public class NoteTranslationEntity extends BaseTranslationEntity {

    @Column(name="title", nullable=false)
    private String title;

    @Column(name="content", columnDefinition="TEXT")
    private String content;

    @Column(name="annotation")
    private String annotation;

    @ManyToOne
    @JoinColumn(name="note_id", nullable=false)
    private NoteEntity note;

    public NoteTranslationEntity() {}

    public String getTitle() { return title; }

    public String getContent() { return content; }

    public String getAnnotation() { return annotation; }

    public NoteEntity getNote() { return note; }

    public void setTitle(String newTitle) {
        this.title = newTitle;
    }

    public void setContent(String newContent) {
        this.content = newContent;
    }

    public void setAnnotation(String newAnnotation) { this.annotation = newAnnotation; }

    public void setNote(NoteEntity note) { this.note = note; }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoteTranslationEntity)) return false;

        // cast to correct type
        NoteTranslationEntity that = (NoteTranslationEntity) o;

        if (getLangCode() == null || that.getLangCode() == null) return false;
        if (getNote() == null || that.getNote() == null) return false;

        return Objects.equals(getLangCode(), that.getLangCode())
                && Objects.equals(note != null ? note.getId() : null,
                that.note != null ? that.note.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLangCode(), note != null ? note.getId() : null);
    }
}
