package dao.notebook;

import datasource.MariaDbJpaConnection;
import entity.NoteBookEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class JpaNoteBookDao implements NoteBookDAO{

    @Override
    public NoteBookEntity save(NoteBookEntity noteBook) {
        if (noteBook == null) throw new IllegalArgumentException("Notebook cannot be null");
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();
            NoteBookEntity managedNoteBook;
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
    public NoteBookEntity findById(Long id) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            return em.find(NoteBookEntity.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<NoteBookEntity> findByTitle(String title) {
        if (title == null) throw new IllegalArgumentException("Title cannot be null");

        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            TypedQuery<NoteBookEntity> query = em.createQuery("Select t from NoteBookEntity t where t.title = :title", NoteBookEntity.class);
            query.setParameter("title", title);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void update(NoteBookEntity noteBook) {
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
    public void delete(NoteBookEntity noteBook) {
        if  (noteBook == null) throw new IllegalArgumentException("Notebook cannot be null");
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();
            NoteBookEntity managedNoteBook = em.find(NoteBookEntity.class, noteBook.getId());

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


    /**
     * Finds notebook by user ID (efficient query)
     */
    public NoteBookEntity findByUserId(Long userId) {
        if (userId == null) return null;
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            TypedQuery<NoteBookEntity> query = em.createQuery(
                    "SELECT n FROM NoteBookEntity n WHERE n.user.id = :userId", NoteBookEntity.class);
            query.setParameter("userId", userId);
            List<NoteBookEntity> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }
}
