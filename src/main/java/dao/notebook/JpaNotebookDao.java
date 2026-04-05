package dao.notebook;

import entity.entities.NotebookEntity;
import entity.entities.UserEntity;
import jakarta.persistence.TypedQuery;
import dao.basedao.GenericAbstractDAO;

import java.util.List;

public class JpaNotebookDao extends GenericAbstractDAO<NotebookEntity, Long> implements NotebookDAO {

    public JpaNotebookDao() {}

    @Override
    public NotebookEntity save(NotebookEntity noteBook) {
        if (noteBook == null) throw new IllegalArgumentException("Notebook cannot be null");

        return executeInTransaction(em -> {
            if (noteBook.getId() == null) {
                em.persist(noteBook);
                return noteBook;
            } else {
                return em.merge(noteBook);
            }
        });
    }

    @Override
    public NotebookEntity findById(Long id) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");

        return execute(em -> em.find(NotebookEntity.class, id));
    }

    @Override
    public List<NotebookEntity> findByUser(UserEntity user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");

        return execute(em -> {
            TypedQuery<NotebookEntity> query = em.createQuery("SELECT n FROM NotebookEntity n WHERE n.user = :user", NotebookEntity.class);
            query.setParameter("user", user);
            return query.getResultList();
        });
    }

    @Override
    public List<NotebookEntity> findByTitle(String title) {
        if (title == null) throw new IllegalArgumentException("Title cannot be null");

        return execute(em -> {
            TypedQuery<NotebookEntity> query = em.createQuery("SELECT n FROM NotebookEntity n WHERE n.title = :title", NotebookEntity.class);
            query.setParameter("title", title);
            return query.getResultList();
        });
    }

    @Override
    public void update(NotebookEntity noteBook) {
        if  (noteBook == null) throw new IllegalArgumentException("Notebook cannot be null");

        executeInTransaction(em -> {
            em.merge(noteBook);
            return null;
        });
    }

    @Override
    public void delete(NotebookEntity noteBook) {
        if  (noteBook == null) throw new IllegalArgumentException("Notebook cannot be null");

        executeInTransaction(em -> {
            NotebookEntity managedNoteBook = em.find(NotebookEntity.class, noteBook.getId());

            if (managedNoteBook != null) {
                managedNoteBook.setUser(null);
                em.remove(managedNoteBook);
            }

            return null;
        });
    }
}