package dao.user;

import dao.baseDAO.GenericDAO;
import entity.UserEntity;

import java.util.List;

public interface UserDAO extends GenericDAO<UserEntity, Long> {
    UserEntity findByUsername(String username);

    UserEntity findByEmail(String email);
}
