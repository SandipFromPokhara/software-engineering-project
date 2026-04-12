package dao.notebook;

import entity.entities.NotebookEntity;
import entity.entities.UserEntity;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import dao.basedao.GenericAbstractDAO;
import org.hibernate.Hibernate;

import java.util.List;

public class JpaNotebookDao extends GenericAbstractDAO<NotebookEntity, Long> implements INotebookDAO {

    public JpaNotebookDao() {/* Empty constructor to prevent instantiation of the class */}

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

        return execute(em -> {
            try {
                return em.createQuery("SELECT n FROM NotebookEntity n LEFT JOIN FETCH n.translations WHERE n.id = :id", NotebookEntity.class)
                        .setParameter("id", id)
                        .getSingleResult();
            } catch (NoResultException e) {
                return null;
            }
        });
    }

    @Override
    public List<NotebookEntity> findByUser(UserEntity user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");

        return execute(em -> {
            em.clear();
            TypedQuery<NotebookEntity> query = em.createQuery(
                    "SELECT DISTINCT n FROM NotebookEntity n LEFT JOIN FETCH n.translations WHERE n.user.id = :userId", NotebookEntity.class);
            query.setParameter("userId", user.getId());

            List<NotebookEntity> results = query.getResultList();

            // MANUALLY TRIGGER INITIALIZATION while the EM is still open
            for (NotebookEntity n : results) {
                if (n.getTranslations() != null) {
                    // Accessing the size forces Hibernate to load the Map from the DB
                    Hibernate.initialize(n.getTranslations().size());
                }
            }

            return results;
        });
    }

    @Override
    public List<NotebookEntity> findByTitle(String title) {
        if (title == null) throw new IllegalArgumentException("Title cannot be null");

        return execute(em -> {
            TypedQuery<NotebookEntity> query = em.createQuery("SELECT DISTINCT n FROM NotebookEntity n LEFT JOIN FETCH n.translations t WHERE t.title = :title", NotebookEntity.class);
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
        if  (noteBook == null || noteBook.getId() == null) throw new IllegalArgumentException("Notebook cannot be null");

        executeInTransaction(em -> {
            NotebookEntity managedNotebook = em.find(NotebookEntity.class, noteBook.getId());

            if (managedNotebook != null) {
                UserEntity user = managedNotebook.getUser();
                if (user != null) {
                    user.removeNotebook(managedNotebook);
                }

                em.remove(managedNotebook);
            }
            return null;
        });
    }
}
