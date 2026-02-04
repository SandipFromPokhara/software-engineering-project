package dao.notebook;

import dao.baseDAO.GenericDAO;
import entity.NoteBookEntity;
import java.util.List;

public interface NoteBookDAO extends GenericDAO<NoteBookEntity, Long> {
    List<NoteBookEntity> findByTitle(String title);
}
