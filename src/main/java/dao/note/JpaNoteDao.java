package dao.note;

import dao.basedao.GenericAbstractDAO;
import entity.NotebookEntity;
import entity.NoteEntity;
import entity.NoteTranslationEntity;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class JpaNoteDao extends GenericAbstractDAO<NoteEntity, Long> implements NoteDAO{

    public JpaNoteDao() {}

    @Override
    public NoteEntity save(NoteEntity note) {
        if (note == null) throw new IllegalArgumentException("Note cannot be null");

        return executeInTransaction(em -> {
            if (note.getId() == null) {
                em.persist(note);
                return note;
            } else {
                return em.merge(note);
            }
        });
    }

    @Override
    public NoteEntity findById(Long id) {
        return executeInTransaction(em -> em.find(NoteEntity.class, id));
    }

    @Override
    public List<NoteTranslationEntity> findByTitle(String title) {
        if (title == null) throw new IllegalArgumentException("Title cannot be null");

        return executeInTransaction(em -> {
            TypedQuery<NoteTranslationEntity> query = em.createQuery("SELECT t FROM NoteTranslationEntity t WHERE LOWER (t.title) = LOWER(:title)", NoteTranslationEntity.class);
            query.setParameter("title", title);

            return query.getResultList();
        });
    }

    @Override
    public List<NoteEntity> findByNotebook(NotebookEntity notebook) {
        if (notebook == null) throw new IllegalArgumentException("Notebook cannot be null");

        return executeInTransaction(em -> {
            TypedQuery<NoteEntity> query = em.createQuery("Select n from NoteEntity n where n.notebook = :notebook", NoteEntity.class);
            query.setParameter("notebook", notebook);

            return query.getResultList();
        });
    }

    @Override
    public void delete(NoteEntity note) {
        if (note == null || note.getId() == null)
            throw new IllegalArgumentException("Note or ID is null");

        executeInTransaction(em -> {
            NoteEntity managed = em.find(NoteEntity.class, note.getId());
            if (managed != null) {
                em.remove(managed);
            }
            return null;
        });
    }

    @Override
    public void update(NoteEntity note) {
        if (note == null) throw new IllegalArgumentException("Note cannot be null");

        executeInTransaction(em -> {
            em.merge(note);
            return null;
        });
    }
}