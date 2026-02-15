package model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class TagModel {
    private Long id;
    private String tagName;

    public TagModel(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        TagModel tagModel = (TagModel) o;
        if (this.id == null || tagModel.id == null) return false;
        return Objects.equals(id, tagModel.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Long getId() { return id; }

    public String getTagName() { return tagName; }

    public void setId(Long id) { this.id = id; }

    public void setTagName(String newTagName) { this.tagName = newTagName; }

    public String toString() {
        return tagName;
    }

}
