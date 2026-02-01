package dao.notebook;

import datasource.MariaDbJpaConnection;
import entity.NoteBookEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class JpaNoteBookDao implements NoteBookDAO{

    @Override
    public void save(NoteBookEntity noteBook) {
        if (noteBook == null) throw new IllegalArgumentException("Notebook cannot be null");
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();
            if (noteBook.getId() == null) {
                em.persist(noteBook);
            } else {
                em.merge(noteBook);
            }
            em.getTransaction().commit();
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
            NoteBookEntity managedNoteBook = em.merge(noteBook);
            em.remove(managedNoteBook);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Failed to delete notebook", e);
        } finally {
            em.close();
        }
    }
}
