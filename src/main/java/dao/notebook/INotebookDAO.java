package dao.notebook;

import dao.basedao.IGenericDAO;
import entity.entities.NotebookEntity;
import entity.entities.UserEntity;

import java.util.List;

public interface INotebookDAO extends IGenericDAO<NotebookEntity, Long> {
    List<NotebookEntity> findByUser(UserEntity user);
    List<NotebookEntity> findByTitle(String title);
    /**
     * Atomically delete a notebook and all its associated notes in a single transaction.
     * Implementations should ensure the operation is performed in one transaction so either
     * all rows are removed or none are (to avoid partial deletes).
     */
    void deleteWithNotes(NotebookEntity notebook);
}
