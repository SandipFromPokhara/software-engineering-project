package dao.note;

import dao.basedao.GenericDAO;
import entity.NotebookEntity;
import entity.NoteEntity;
import entity.NoteTranslationEntity;

import java.util.List;

public interface NoteDAO extends GenericDAO <NoteEntity, Long> {
    List<NoteTranslationEntity> findByTitle(String title);
    List<NoteEntity> findByNotebook(NotebookEntity notebook);
}
