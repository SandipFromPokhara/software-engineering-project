package entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name="tags")
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="tag_name", nullable = false, unique = true)
    private String tagName;

    @Column(name="createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToMany(mappedBy = "tags")
    private Set<NoteEntity> notes = new HashSet<>();

    public TagEntity() {}

    public TagEntity(String tagName) {
        this.tagName = tagName;
    }

    public Long getId() { return id; }

    public String getTagName() { return tagName; }

    public void setId(Long id) { this.id = id; }

    public void setTagName(String newTagName) { this.tagName = newTagName; }

    public Set<NoteEntity> getNotes() { return notes; }

    @Override
    public String toString() { return "#" + tagName; }

    @Override
    public boolean equals(Object other) {
        if ( other == null || getClass() != other.getClass()) return false;

        TagEntity tagEntity = (TagEntity) other;
        if ( this.id == null || tagEntity.id == null) return false;
        return (Objects.equals(id, tagEntity.id));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
