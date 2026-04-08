package services;

import dao.note.*;
import dao.notebook.*;
import dao.tag.JpaTagDao;
import dao.tag.ITagDAO;
import entity.entities.*;
import entity.translationentities.NoteTranslationEntity;
import entity.translationentities.NotebookTranslationEntity;
import session.UserSession;
import util.Localization;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Service layer for Note operations
 */
public class NoteService {

    private static final Logger logger = Logger.getLogger(NoteService.class.getName());

    private final INoteDAO noteDao;
    private final INotebookDAO notebookDao;
    private final ITagDAO tagDao;
    private final TranslationService translationService;

    public NoteService() {
        this.noteDao = new JpaNoteDao();
        this.notebookDao = new JpaNotebookDao();
        this.tagDao = new JpaTagDao();
        this.translationService = new TranslationService();
    }

    // constructor overriding for unit test
    public NoteService(INoteDAO noteDao, INotebookDAO notebookDao, ITagDAO tagDao, TranslationService translationService) {
        this.noteDao = noteDao;
        this.notebookDao = notebookDao;
        this.tagDao = tagDao;
        this.translationService = translationService;
    }

    // Backwards-compatible constructor used by some tests (keeps API stable)
    public NoteService(INoteDAO noteDao, INotebookDAO notebookDao, ITagDAO tagDao) {
        this(noteDao, notebookDao, tagDao, new TranslationService());
    }

    /**
     * Creates a new note with title, content, and annotation
     */
    public NoteEntity createNote(String title, String content, String annotation, NotebookEntity notebookParameter, Set<String> tagNames, String langCode) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        UserEntity currentUser = UserSession.getUserInstance().getUser();
        if (currentUser == null) {
            throw new IllegalStateException("No user logged in. Please login first.");
        }

        // Get or create notebook for current user
        NotebookEntity notebookToUse = (notebookParameter != null) ? notebookParameter : getOrCreatePersonalNotebook(currentUser);

        // Create and save note
        NoteEntity note = new NoteEntity();
        note.setNotebook(notebookToUse);

        // create translations
        NoteTranslationEntity translation = new NoteTranslationEntity();
        translation.setLangCode(langCode);
        translation.setNote(note);
        note.addTranslation(translation);

        translation.setTitle(title.trim());
        translation.setContent(content != null ? content : "");
        translation.setAnnotation(annotation != null ? annotation : "");

        // Shift tag handling logic from create controller
        applyTags(note, tagNames);

        return noteDao.save(note);
    }

    public NoteEntity updateNote(NoteEntity note, String langCode, String title, String content,
                                 String annotation, Set<String> tagNames) {
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }

        // Provide a non-null default language to the translation service (use current UI language)
        String defaultLang = Localization.getCurrentLanguageCode();
        logger.fine(() -> "updateNote called with langCode=" + langCode + ", defaultLang=" + defaultLang + ", noteId=" + (note.getId()==null?"<null>":note.getId()));
        NoteTranslationEntity translation = translationService.getTranslation(note, langCode, defaultLang);

        if (translation == null) {
            translation = new NoteTranslationEntity();
            translation.setLangCode(langCode);
            translation.setNote(note);
            note.addTranslation(translation);
        }

        translation.setTitle(title != null ? title.trim() : "");
        translation.setContent(content != null ? content.trim() : "");
        translation.setAnnotation(annotation != null ? annotation.trim() : "");

        // Shift tag handling logic from edit controller and apply current selected tags
        applyTags(note, tagNames);

        return noteDao.save(note);
    }

    public NotebookEntity createNotebook(String title) {
        UserEntity user = UserSession.getUserInstance().getUser();

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Notebook name cannot be empty");
        }

        NotebookEntity notebook = new NotebookEntity();
        notebook.setUser(user);

        NotebookTranslationEntity translation = new NotebookTranslationEntity();
        // Use current UI language code so notebook translations match selected language
        translation.setLangCode(Localization.getCurrentLanguageCode());
        translation.setTitle(title.trim());

        notebook.addTranslation(translation);

        return notebookDao.save(notebook);
    }

    /**
     * Gets or creates a notebook for the logged-in user
     */
    private NotebookEntity getOrCreatePersonalNotebook(UserEntity user) {

        // Find existing notebook for this user
        List<NotebookEntity> notebooks = notebookDao.findByUser(user);

        if (!notebooks.isEmpty()) {
            return notebooks.get(0);
        }

        NotebookEntity notebook = new NotebookEntity();
        notebook.setUser(user);

        // create default translation using current UI language
        NotebookTranslationEntity t = new NotebookTranslationEntity();
        t.setLangCode(Localization.getCurrentLanguageCode());
        t.setTitle(user.getFirstName() + "'s Notebook");

        notebook.addTranslation(t);

        return notebookDao.save(notebook);
    }

    public NoteEntity save(NoteEntity note) {
        return noteDao.save(note);
    }

    private void applyTags(NoteEntity note, Set<String> tagNames) {
        // Clear old tags first
        if (note.getTags() != null) {
            for (TagEntity tag : new HashSet<>(note.getTags())) {
                note.removeTag(tag);
            }
        }

        if (tagNames == null || tagNames.isEmpty()) return;

        for (String tagName : tagNames) {
            if (tagName == null || tagName.isBlank()) continue;

            String normalized = tagName.trim().toLowerCase();

            TagEntity tag = tagDao.findByName(normalized);

            if (tag == null) {
                tag = new TagEntity();

                tag.setTagName(normalized);

                tag = tagDao.save(tag);
            }

            note.addTag(tag);
        }
    }
}