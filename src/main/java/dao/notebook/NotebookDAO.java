package dao.notebook;

import dao.basedao.GenericDAO;
import entity.entities.NotebookEntity;
import entity.entities.UserEntity;

import java.util.List;

public interface NotebookDAO extends GenericDAO<NotebookEntity, Long> {
    List<NotebookEntity> findByUser(UserEntity user);
    List<NotebookEntity> findByTitle(String title);
}