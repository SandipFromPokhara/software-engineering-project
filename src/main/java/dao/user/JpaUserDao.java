package dao;

import datasource.MariaDbJpaConnection;
import entity.UserEntity;
import jakarta.persistence.EntityManager;

import java.util.List;

public class UserDao {

    public void persist(UserEntity user) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();

        try {
            em.getTransaction().begin();
            em.persist(user);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to persist user", e);
        } finally {
            em.close();
        }
    }

    public UserEntity findById(Long id) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();
        } finally {
            em.close();
        }
    }

    public UserEntity findByUsername(String username) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to find user", e);
        } finally {
            em.close();
        }
    }

    public UserEntity findByEmail(String email) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        return id;
    }

    public UserEntity deleteById(Long id) {

    }


    public UserEntity updateById(Long id) {

    }
}
