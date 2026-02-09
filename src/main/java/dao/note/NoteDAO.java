package dao.note;

import dao.baseDAO.GenericDAO;
import entity.NoteBookEntity;
import entity.NoteEntity;
import java.util.List;

public interface NoteDAO extends GenericDAO <NoteEntity, Long> {
    List<NoteEntity> findByTitle(String title);
    List<NoteEntity> findByNotebook(NoteBookEntity notebook);
}
