package entity.entities;

import entity.base.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="users")
public class UserEntity extends BaseEntity {

    @Column(name="firstName", nullable = false)
    private String firstName;

    @Column(name="lastName", nullable = false)
    private String lastName;

    @Column(name="username", nullable = false, unique = true)
    private String username;

    @Column(name="email", nullable = false, unique = true)
    private String email;

    @Column(name="passwordHash")
    private String passwordHash;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotebookEntity> notebooks = new ArrayList<>();

    public UserEntity(String firstName, String lastName, String username, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
    }

    public UserEntity() {}

    public String getFirstName() { return firstName; }

    public String getLastName() { return lastName; }

    public String getUsername() { return username; }

    public String getEmail() { return email; }

    public String getPasswordHash() { return passwordHash; }

    public void setFirstName(String newFirstName) {
        this.firstName = newFirstName;
    }

    public void setLastName(String newLastName) {
        this.lastName = newLastName;
    }

    public void setEmail(String newEmail) {
        this.email = newEmail;
    }

    public void setUsername(String newUsername) {
        this.username = newUsername;
    }

    public void changePasswordHash(String newHashedPassword) {
        if (newHashedPassword != null && !newHashedPassword.isEmpty()) {
            this.passwordHash = newHashedPassword;
        }
    }

    public List<NotebookEntity> getNotebooks() { return notebooks; }

    public void removeNotebook(NotebookEntity notebook) {
        notebooks.remove(notebook);
        notebook.setUser(null);
    }

    public void addNotebook(NotebookEntity notebook) {
        notebooks.add(notebook);
        notebook.setUser(this);
    }

    @Override
    public String toString() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity)) return false;
        UserEntity that = (UserEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}