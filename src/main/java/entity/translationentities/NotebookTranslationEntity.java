package entity.translationentities;

import entity.base.BaseTranslationEntity;
import entity.entities.NotebookEntity;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name="notebook_translation")
public class NotebookTranslationEntity extends BaseTranslationEntity {

    @Column(name="title", nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name="notebook_id", nullable = false)
    private NotebookEntity notebook;

    public NotebookTranslationEntity() {/* JPA only*/}

    public String getTitle() { return title; }

    public NotebookEntity getNotebook() { return notebook; }

    public void setTitle(String title) { this.title = title; }

    public void setNotebook(NotebookEntity notebook) { this.notebook = notebook; }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotebookTranslationEntity))return false;

        // cast to correct type
        NotebookTranslationEntity that = (NotebookTranslationEntity) o;

        if (getLangCode() == null || that.getLangCode() == null) return false;
        if (getNotebook() == null || that.getNotebook() == null) return false;

        return Objects.equals(getLangCode(), that.getLangCode())
                && Objects.equals(notebook != null ? notebook.getId() : null,
                that.notebook != null ? that.notebook.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLangCode(), notebook != null ? notebook.getId() : null);
    }
}
