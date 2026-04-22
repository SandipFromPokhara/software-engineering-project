package dao.notebook;

import dao.basedao.IGenericDAO;
import entity.entities.NotebookEntity;
import entity.entities.UserEntity;

import java.util.List;

public interface INotebookDAO extends IGenericDAO<NotebookEntity, Long> {
    List<NotebookEntity> findByUser(UserEntity user);
    List<NotebookEntity> findByTitle(String title);
}
