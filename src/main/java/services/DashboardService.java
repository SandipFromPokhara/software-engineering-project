package services;

import dao.note.*;
import dao.notebook.*;
import entity.translationentities.NoteTranslationEntity;
import entity.entities.NotebookEntity;
import entity.entities.NoteEntity;
import session.NotebookSession;
import session.UserSession;
import util.Localization;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static model.LanguageModel.DEFAULT_LANGUAGE_CODE;

public class DashboardService {

    private final INoteDAO noteDao;
    private final INotebookDAO notebookDao;
    private final TranslationService translationService;

    public DashboardService() {
        this.noteDao = new JpaNoteDao();
        this.notebookDao = new JpaNotebookDao();
        this.translationService = new TranslationService();
    }

    // constructor overriding for unit test
    public DashboardService(INoteDAO noteDao, INotebookDAO notebookDao, TranslationService translationService) {
        this.noteDao = noteDao;
        this.notebookDao = notebookDao;
        this.translationService = translationService;
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

        List<NoteEntity> notes = noteDao.findByNotebookWithTranslations(notebook);
        notes.sort((n1, n2) -> {
            LocalDateTime t1 = Optional.ofNullable(n1.getUpdatedAt()).orElse(LocalDateTime.MIN);
            LocalDateTime t2 = Optional.ofNullable(n2.getUpdatedAt()).orElse(LocalDateTime.MIN);
            return t2.compareTo(t1);        // descending order
        });
        return notes;
    }

    /** Delete a note */
    public void deleteNote(NoteEntity note) {
        if (note == null || note.getId() == null) {
            throw new IllegalArgumentException("Note cannot be null or unsaved");
        }
        noteDao.delete(note);
    }

    public NoteTranslationEntity getDisplayTranslation(NoteEntity note) {
        String langCode = Localization.getCurrentLanguageCode().toUpperCase();
        return translationService.getTranslation(note, langCode, DEFAULT_LANGUAGE_CODE);
    }
}
