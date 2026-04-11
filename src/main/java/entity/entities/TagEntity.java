package entity.entities;

import entity.base.BaseEntity;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name="tags")
public class TagEntity extends BaseEntity {

    @Column(name="tag_name", nullable = false, unique = true, columnDefinition = "VARCHAR(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String tagName;

    @ManyToMany(mappedBy = "tags")
    private Set<NoteEntity> notes = new HashSet<>();

    public TagEntity() {/* JPA */}

    public String getTagName() { return tagName; }

    public void setTagName(String newTagName) { this.tagName = newTagName; }

    public Set<NoteEntity> getNotes() { return notes; }

    @Override
    public String toString() { return "#" + tagName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TagEntity)) return false;

        TagEntity that = (TagEntity) o;

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
