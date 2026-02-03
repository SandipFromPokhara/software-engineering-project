package dao.user;

import datasource.MariaDbJpaConnection;
import entity.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class JpaUserDao implements UserDAO{

    @Override
    public UserEntity save(UserEntity user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");

        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();
            UserEntity managedUser;

            if (user.getId() == null) {
                em.persist(user);
                managedUser = user;
            } else {
                managedUser = em.merge(user);
            }
            em.getTransaction().commit();
            return managedUser;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to persist user", e);
        } finally {
            em.close();
        }
    }

    @Override
    public UserEntity findById(Long id) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            return em.find(UserEntity.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public UserEntity findByUsername(String username) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            TypedQuery<UserEntity> query = em.createQuery("Select u from UserEntity u where u.username = :username", UserEntity.class);
            query.setParameter("username", username);
            List<UserEntity> result = query.getResultList();

            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public UserEntity findByEmail(String email) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            TypedQuery<UserEntity> query = em.createQuery("Select e from UserEntity e where e.email = :email", UserEntity.class);
            query.setParameter("email", email);
            List<UserEntity> result = query.getResultList();

            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public void update(UserEntity user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");

        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Failed to update user", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(UserEntity user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");

        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();
            UserEntity managedUser = em.find(UserEntity.class, user.getId());

            if (managedUser != null) {
                em.remove(managedUser);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Failed to delete user" + user, e);
        } finally {
            em.close();
        }
    }
}
