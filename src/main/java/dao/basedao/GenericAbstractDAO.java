package dao.basedao;

import datasource.MariaDbJpaConnection;
import jakarta.persistence.EntityManager;

import java.util.function.Function;

public abstract class GenericAbstractDAO<T, ID> {

    protected <R> R executeInTransaction(Function<EntityManager, R> action) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();

            R result = action.apply(em);

            em.getTransaction().commit();
            return result;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Transaction failed", e);
        } finally {
            em.close();
        }
    }
}