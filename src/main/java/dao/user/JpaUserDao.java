package dao.user;

import dao.basedao.GenericAbstractDAO;
import entity.entities.UserEntity;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class JpaUserDao extends GenericAbstractDAO<UserEntity, Long> implements IUserDAO {

    public JpaUserDao() {}

    @Override
    public UserEntity save(UserEntity user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");

        return executeInTransaction(em-> {
            try {
                if (user.getId() == null) {
                    em.persist(user);
                    return user;
                } else {
                    return em.merge(user);
                }
            } catch (Exception e) {
                if (isUniqueConstraintViolation(e)) {
                    throw new IllegalArgumentException("Username or email already exists");
                }
                throw e;
            }
        });
    }

    @Override
    public UserEntity findById(Long id) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");

        return execute(em-> em.find(UserEntity.class, id));
    }

    @Override
    public UserEntity findByUsername(String username) {
        if (username == null) throw new IllegalArgumentException("Username cannot be null");

        return execute(em-> {
            TypedQuery<UserEntity> query = em.createQuery("SELECT u FROM UserEntity u WHERE u.username = :username", UserEntity.class);
            query.setParameter("username", username);
            List<UserEntity> result = query.getResultList();

            return result.isEmpty() ? null : result.get(0);
        });
    }

    @Override
    public UserEntity findByEmail(String email) {
        if (email == null) throw new IllegalArgumentException("Email cannot be null");

        return execute(em-> {
            TypedQuery<UserEntity> query = em.createQuery("SELECT u FROM UserEntity u WHERE u.email = :email", UserEntity.class);
            query.setParameter("email", email);
            List<UserEntity> result = query.getResultList();

            return result.isEmpty() ? null : result.get(0);
        });
    }

    @Override
    public void update(UserEntity user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");

        executeInTransaction(em-> {
            em.merge(user);
            return null;
        });
    }

    @Override
    public void delete(UserEntity user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");

        executeInTransaction(em-> {
            UserEntity managedUser = em.find(UserEntity.class, user.getId());

            if (managedUser != null) {
                em.remove(managedUser);
            }

            return null;
        });
    }
}
