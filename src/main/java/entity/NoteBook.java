package entity;

import jakarta.persistence.*;

@Entity
@Table(name="notebooks")
public class NoteBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="title")
    private String title;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public NoteBook(String title, User user) {
        this.title = title;
        this.user = user;
    }

    public Notebook() {}

    public Long getId() { return id; }

    public String getTitle() { return title; }

    public User getUser() { return user; }
}
