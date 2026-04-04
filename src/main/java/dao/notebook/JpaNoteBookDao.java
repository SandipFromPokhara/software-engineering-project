package dao.notebook;

import datasource.MariaDbJpaConnection;
import entity.NotebookEntity;
import entity.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class JpaNoteBookDao implements NoteBookDAO{

    public JpaNoteBookDao() {}

    @Override
    public NotebookEntity save(NotebookEntity noteBook) {
        if (noteBook == null) throw new IllegalArgumentException("Notebook cannot be null");
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();
            NotebookEntity managedNoteBook;
            if (noteBook.getId() == null) {
                em.persist(noteBook);
                managedNoteBook = noteBook;
            } else {
                managedNoteBook = em.merge(noteBook);
            }
            em.getTransaction().commit();
            return managedNoteBook;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Failed to save notebook", e);
        } finally {
            em.close();
        }
    }

    @Override
    public NotebookEntity findById(Long id) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            return em.find(NotebookEntity.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<NotebookEntity> findByUser(UserEntity user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");

        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            TypedQuery<NotebookEntity> query = em.createQuery("Select n from NotebookEntity n where n.user = :user", NotebookEntity.class);
            query.setParameter("user", user);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<NotebookEntity> findByTitle(String title) {
        if (title == null) throw new IllegalArgumentException("Title cannot be null");

        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            TypedQuery<NotebookEntity> query = em.createQuery("Select n from NotebookEntity n where n.title = :title", NotebookEntity.class);
            query.setParameter("title", title);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void update(NotebookEntity noteBook) {
        if  (noteBook == null) throw new IllegalArgumentException("Notebook cannot be null");
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(noteBook);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Failed to update notebook", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(NotebookEntity noteBook) {
        if  (noteBook == null) throw new IllegalArgumentException("Notebook cannot be null");
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();
            NotebookEntity managedNoteBook = em.find(NotebookEntity.class, noteBook.getId());

            if (managedNoteBook != null) {
                managedNoteBook.setUser(null);
                em.remove(managedNoteBook);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Failed to delete notebook", e);
        } finally {
            em.close();
        }
    }
}
