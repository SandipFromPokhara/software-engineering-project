package dao;

import datasource.MariaDbJpaConnection;
import entity.UserEntity;
import jakarta.persistence.EntityManager;

public class UserDao {

    public void persist(UserEntity user) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();

    }

    public UserEntity findByUsername(UserEntity id) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        return id;
    }

    public UserEntity findByEmail(UserEntity id) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        return id;
    }
}
