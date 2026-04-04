package dao.note;

import dao.basedao.GenericDAO;
import entity.NoteBookEntity;
import entity.NoteEntity;
import java.util.List;

public interface NoteDAO extends GenericDAO <NoteEntity, Long> {
    List<NoteEntity> findByTitle(String title);
    List<NoteEntity> findByNotebook(NoteBookEntity notebook);

    NoteEntity findById(Long id, String language);
}
