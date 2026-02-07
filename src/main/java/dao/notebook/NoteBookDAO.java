package dao.notebook;

import dao.baseDAO.GenericDAO;
import entity.NoteBookEntity;
import entity.UserEntity;

import java.util.List;

public interface NoteBookDAO extends GenericDAO<NoteBookEntity, Long> {
    List<NoteBookEntity> findByUser(UserEntity user);
    List<NoteBookEntity> findByTitle(String title);
}
