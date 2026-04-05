package entity.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="notebooks")
public class NotebookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="title")
    private String title;

    @Column(name="createdAt")
    private LocalDateTime createdAt;

    @Column(name="updatedAt")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToMany(mappedBy = "notebook", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoteEntity> notes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public NotebookEntity(String title, UserEntity user) {
        this.title = title;
        this.user = user;
    }

    public NotebookEntity() {}

    public Long getId() { return id; }

    public String getTitle() { return title; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public UserEntity getUser() { return user; }

    public List<NoteEntity> getNotes() { return notes; }

    public void setTitle(String newTitle) {
        this.title = newTitle;
    }

    public void setUser(UserEntity user) {
        if(this.user != null) {
            this.user.getNoteBooks().remove(this);
        }
        this.user = user;
        if (user != null) {
            user.getNoteBooks().add(this);
        }
    }

    @Override
    public String toString() {
        return title;
    }
}