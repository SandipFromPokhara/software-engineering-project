package services;

import dao.note.NoteDAO;
import dao.notebook.NoteBookDAO;
import dao.tag.TagDAO;
import entity.NotebookEntity;
import entity.NoteEntity;
import session.NotebookSession;
import session.UserSession;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DashboardService {

    private final NoteDAO noteDao;
    private final NoteBookDAO notebookDao;
    private final TagDAO tagDao;

    public DashboardService(NoteDAO noteDao, NoteBookDAO notebookDao, TagDAO tagDao) {
        this.noteDao = noteDao;
        this.notebookDao = notebookDao;
        this.tagDao = tagDao;
    }

    public DashboardService() {
        this.noteDao = new dao.note.JpaNoteDao();
        this.notebookDao = new dao.notebook.JpaNoteBookDao();
        this.tagDao = new dao.tag.JpaTagDao();
    }

    /** Load all notebooks for current user, sorted by creation time */
    public List<NotebookEntity> loadNotebooks() {
        var user = UserSession.getUserInstance().getUser();
        List<NotebookEntity> notebooks = notebookDao.findByUser(user);
        notebooks.sort(Comparator.comparing(nb -> Optional.ofNullable(nb.getCreatedAt()).orElse(LocalDateTime.MIN)));
        return notebooks;
    }

    /** Determine initial notebook to select */
    public NotebookEntity getInitialNotebook() {
        NotebookEntity lastCreated = NotebookSession.getLastCreatedNotebook();
        if (lastCreated != null) {
            NotebookSession.clear();
            return lastCreated;
        }
        List<NotebookEntity> notebooks = loadNotebooks();
        return notebooks.isEmpty() ? null : notebooks.get(0);
    }

    /** Load all notes for a notebook, sorted by updated time descending */
    public List<NoteEntity> loadNotes(NotebookEntity notebook) {
        if (notebook == null) return List.of();

        List<NoteEntity> notes = noteDao.findByNotebook(notebook);
        notes.sort((n1, n2) -> {
            LocalDateTime t1 = Optional.ofNullable(n1.getUpdatedAt()).orElse(LocalDateTime.MIN);
            LocalDateTime t2 = Optional.ofNullable(n2.getUpdatedAt()).orElse(LocalDateTime.MIN);
            return t2.compareTo(t1);        // descending order
        });
        return notes;
    }

    /** Delete a note */
    public void deleteNote(NoteEntity note) {
        noteDao.delete(note);
    }
}