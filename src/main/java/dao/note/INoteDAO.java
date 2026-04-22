package dao.note;

import dao.basedao.IGenericDAO;
import entity.entities.NotebookEntity;
import entity.entities.NoteEntity;
import entity.translationentities.NoteTranslationEntity;

import java.util.List;

public interface INoteDAO extends IGenericDAO<NoteEntity, Long> {
    List<NoteTranslationEntity> findByTitle(String title);
    List<NoteEntity> findByNotebook(NotebookEntity notebook);
    List<NoteEntity> findByNotebookWithTranslations(NotebookEntity notebook);
}
