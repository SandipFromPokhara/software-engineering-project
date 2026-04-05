package entity.entities;

import entity.base.Translatable;
import entity.translationentities.NoteTranslationEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Entity
@Table(name="notes")
public class NoteEntity implements Translatable<NoteTranslationEntity> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="createdAt")
    private LocalDateTime createdAt;

    @Column(name="updatedAt")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notebook_id", nullable = false)
    private NotebookEntity notebook;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(
            name = "note_tags",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<TagEntity> tags = new HashSet<>();

    @OneToMany(mappedBy="note", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    @MapKey(name="langCode")
    private Map<String, NoteTranslationEntity> translations = new HashMap<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public NoteEntity() {}

    public Long getId() { return id; }

    public NotebookEntity getNotebook() { return notebook; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public Map<String, NoteTranslationEntity> getTranslations() {
        return translations;
    }

    public void addTag(TagEntity tag) {
        if (tag != null) {
            tags.add(tag);
        }
    }

    public void removeTag(TagEntity tag) {
        tags.remove(tag);
    }

    public Set<TagEntity> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public void setNotebook(NotebookEntity notebook) { this.notebook = notebook; }

    public void addTranslation(NoteTranslationEntity nt) {
        if (nt != null) {
            String langCode = nt.getLangCode();
            nt.setNote(this);

            if (translations.containsKey(langCode)) {
                throw new IllegalArgumentException("Translation already exists for language: " + langCode);
            }

            translations.put(langCode, nt);
        }
    }

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return createdAt != null ? createdAt.format(formatter) : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoteEntity)) return false;

        NoteEntity that = (NoteEntity) o;

        if (id == null || that.id == null) {
            return false;
        }

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}