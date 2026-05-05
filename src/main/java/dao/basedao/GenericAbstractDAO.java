package dao.basedao;

import datasource.MariaDbJpaConnection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import java.util.function.Function;

/**
 * @param <E> Entity type
 * @param <K> ID (Key) type
 */
public abstract class GenericAbstractDAO<E, K> {

    public abstract E findById(K id);
    public abstract E save(E entity);
    public abstract void update(E entity);
    public abstract void delete(E entity);

    protected <R> R executeInTransaction(Function<EntityManager, R> action) {
        EntityManager em = MariaDbJpaConnection.getEntityManager();
        var transaction = em.getTransaction();
        boolean wasAlreadyActive = em.getTransaction().isActive();

        if (!wasAlreadyActive) {
            transaction.begin();
        }

        try {
            R result = action.apply(em);

            if (!wasAlreadyActive) {
                transaction.commit();
            }
            return result;

        } catch (RuntimeException e) {
            if (!wasAlreadyActive && transaction.isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } catch (Exception e) { // Catching any potential checked exceptions
            if (!wasAlreadyActive && transaction.isActive()) {
                transaction.rollback();
            }
            throw new PersistenceException("Unexpected checked exception during transaction", e);
        }
    }

    protected <R> R execute(Function<EntityManager, R> action) {
        EntityManager em = MariaDbJpaConnection.getEntityManager();
        try {
            return action.apply(em);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenceException("Data access operation failed", e);
        }
    }

    // Detect unique constraint violation
    protected boolean isUniqueConstraintViolation(Exception e) {
        Throwable cause = e;

        while (cause != null) {
            String message = cause.getMessage();
            if (message != null && message.toLowerCase().contains("unique")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
