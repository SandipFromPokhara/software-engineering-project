package dao.user;

import dao.baseDAO.GenericDAO;
import entity.UserEntity;

public interface UserDAO extends GenericDAO<UserEntity, Long> {
    UserEntity findByUsername(String username);

    UserEntity findByEmail(String email);
}
