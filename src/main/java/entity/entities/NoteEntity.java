package entity.entities;

import entity.base.BaseEntity;
import entity.translationentities.NoteTranslationEntity;
import jakarta.persistence.*;
import entity.base.ITranslatable;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Entity
@Table(name="notes")
public class NoteEntity extends BaseEntity implements ITranslatable<NoteTranslationEntity> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notebook_id", nullable = false)
    private NotebookEntity notebook;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(
            name = "note_tags",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<TagEntity> tags = new HashSet<>();

    @OneToMany(mappedBy="note", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    @MapKey(name="langCode")
    private Map<String, NoteTranslationEntity> translations = new HashMap<>();

    public NoteEntity() {/* JPA */}

    public NotebookEntity getNotebook() { return notebook; }

    @Override
    public Map<String, NoteTranslationEntity> getTranslations() {
        return translations;
    }

    public void addTag(TagEntity tag) {
        if (tag != null) {
            tags.add(tag);
        }
    }

    public void removeTag(TagEntity tag) {
        if (tag != null) {
            tags.remove(tag);
        }
    }

    public Set<TagEntity> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public void setNotebook(NotebookEntity notebook) { this.notebook = notebook; }

    public void addTranslation(NoteTranslationEntity nt) {
        if (nt != null) {
            String langCode = nt.getLangCode();
            nt.setNote(this);

            if (translations.putIfAbsent(langCode, nt) != null) {
                throw new IllegalArgumentException("Translation already exists for language: " + langCode);
            }

            translations.put(langCode, nt);
        }
    }

    @Override
    public NoteTranslationEntity createTranslation(String langCode) {
        NoteTranslationEntity nt = new NoteTranslationEntity();
        nt.setLangCode(langCode);
        addTranslation(nt);
        return nt;
    }

    public void removeTranslation(String langCode) {
        if (langCode == null || langCode.isBlank()) return;

        NoteTranslationEntity nt = translations.remove(langCode);

        if (nt != null && nt.getNote() == this) {
            nt.setNote(null);
        }
    }

    public String getFormattedCreatedTime() {
        if (getCreatedAt() == null) return "";
        return getCreatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoteEntity)) return false;

        NoteEntity that = (NoteEntity) o;

        if (getId() == null || that.getId() == null) {
            return false;
        }

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}