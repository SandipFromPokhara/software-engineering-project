package dao.notebook;

import dao.basedao.GenericDAO;
import entity.NotebookEntity;
import entity.UserEntity;

import java.util.List;

public interface NoteBookDAO extends GenericDAO<NotebookEntity, Long> {
    List<NotebookEntity> findByUser(UserEntity user);
    List<NotebookEntity> findByTitle(String title);
}
