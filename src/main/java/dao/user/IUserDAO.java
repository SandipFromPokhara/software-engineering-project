package dao.user;

import dao.basedao.IGenericDAO;
import entity.entities.UserEntity;

public interface IUserDAO extends IGenericDAO<UserEntity, Long> {
    UserEntity findByUsername(String username);
    UserEntity findByEmail(String email);
}
