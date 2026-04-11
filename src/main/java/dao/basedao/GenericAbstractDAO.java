package dao.basedao;

import datasource.MariaDbJpaConnection;
import jakarta.persistence.EntityManager;

import java.util.function.Function;

public abstract class GenericAbstractDAO<T, ID> {

    protected <R> R executeInTransaction(Function<EntityManager, R> action) {
        EntityManager em = MariaDbJpaConnection.getEntityManager();
        boolean wasAlreadyActive = em.getTransaction().isActive();

        if (!wasAlreadyActive) {
            em.getTransaction().begin();
        }

        try {
            R result = action.apply(em);

            if (!wasAlreadyActive) {
                em.getTransaction().commit();
            }
            return result;

        } catch (Exception e) {
            if (!wasAlreadyActive && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Transaction failed", e);
        }
    }

    protected <R> R execute(Function<EntityManager, R> action) {
        EntityManager em = MariaDbJpaConnection.getEntityManager();
        try {
            return action.apply(em);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
