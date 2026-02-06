package dao.note;

import datasource.MariaDbJpaConnection;
import entity.NoteEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class JpaNoteDao implements NoteDAO{

    @Override
    public NoteEntity save(NoteEntity note) {
        if (note == null) throw new IllegalArgumentException("Note cannot be null");

        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();
            NoteEntity managedNote;

            if (note.getId() == null) {
                em.persist(note);
                managedNote = note;
            } else {
                managedNote = em.merge(note);
            }
            em.getTransaction().commit();
            return managedNote;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Failed to save note.", e);
        } finally {
            em.close();
        }
    }

    @Override
    public NoteEntity findById(Long id) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            return em.find(NoteEntity.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<NoteEntity> findByTitle(String title) {
        if (title == null) throw new IllegalArgumentException("Title cannot be null");

        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            TypedQuery<NoteEntity> query = em.createQuery("Select n from NoteEntity n where n.title = :title", NoteEntity.class);
            query.setParameter("title", title);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(NoteEntity note) {
        if (note == null) throw new IllegalArgumentException("Note cannot be null");

        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();
            NoteEntity managedNote = em.merge(note);
            em.remove(managedNote);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Failed to delete note", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void update(NoteEntity note) {
        if (note == null) throw new IllegalArgumentException("Note cannot be null");

        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(note);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Failed to update note", e);
        } finally {
            em.close();
        }
    }
}
