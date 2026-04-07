package entity.entities;

import entity.base.BaseEntity;
import entity.base.Translatable;
import entity.translationentities.NotebookTranslationEntity;
import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name="notebooks")
public class NotebookEntity extends BaseEntity implements Translatable<NotebookTranslationEntity> {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToMany(mappedBy = "notebook", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoteEntity> notes = new ArrayList<>();

    @OneToMany(mappedBy="notebook", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @MapKey(name="langCode")
    private Map<String, NotebookTranslationEntity> translations = new HashMap<>();

    public NotebookEntity(UserEntity user) {
        this.user = user;
    }

    public NotebookEntity() {}

    public UserEntity getUser() { return user; }

    public List<NoteEntity> getNotes() { return notes; }

    @Override
    public Map<String, NotebookTranslationEntity> getTranslations() {
        return translations;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public void addTranslation(NotebookTranslationEntity nt) {
        if (nt != null) {
            String langCode = nt.getLangCode();
            nt.setNotebook(this);

            if (translations.containsKey(langCode)) {
                throw new IllegalArgumentException("Translation already exists for language: " + langCode);
            }

            translations.put(langCode, nt);
        }
    }

    @Override
    public NotebookTranslationEntity createTranslation(String langCode) {
        NotebookTranslationEntity nt = new NotebookTranslationEntity();
        nt.setLangCode(langCode);
        addTranslation(nt);
        return nt;
    }

    public void removeTranslation(String langCode) {
        if (langCode == null || langCode.isBlank()) return;

        NotebookTranslationEntity nt = translations.remove(langCode);

        if (nt != null && nt.getNotebook() == this) {
            nt.setNotebook(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotebookEntity)) return false;

        NotebookEntity that = (NotebookEntity) o;

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