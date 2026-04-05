package dao.note;

import dao.basedao.GenericDAO;
import entity.entities.NotebookEntity;
import entity.entities.NoteEntity;
import entity.translationentities.NoteTranslationEntity;

import java.util.List;

public interface NoteDAO extends GenericDAO <NoteEntity, Long> {
    List<NoteTranslationEntity> findByTitle(String title);
    List<NoteEntity> findByNotebook(NotebookEntity notebook);
    List<NoteEntity> findByNotebookWithTranslations(NotebookEntity notebook);
}
