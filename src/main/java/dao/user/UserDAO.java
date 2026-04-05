package dao.user;

import dao.basedao.GenericDAO;
import entity.entities.UserEntity;

public interface UserDAO extends GenericDAO<UserEntity, Long> {
    UserEntity findByUsername(String username);
    UserEntity findByEmail(String email);
}