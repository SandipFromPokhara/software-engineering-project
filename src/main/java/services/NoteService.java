package services;

import dao.note.JpaNoteDao;
import dao.note.NoteDAO;
import dao.notebook.JpaNotebookDao;
import dao.notebook.NotebookDAO;
import dao.tag.JpaTagDao;
import dao.tag.TagDAO;
import entity.entities.NoteEntity;
import entity.entities.NotebookEntity;
import entity.entities.TagEntity;
import entity.entities.UserEntity;
import entity.translationentities.NoteTranslationEntity;
import session.UserSession;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static model.LanguageModel.DEFAULT_LANGUAGE_CODE;

/**
 * Service layer for Note operations
 */
public class NoteService {

    private final NoteDAO noteDao;
    private final NotebookDAO notebookDao;
    private final TagDAO tagDao;
    private final TranslationService translationService;

    public NoteService() {
        this.noteDao = new JpaNoteDao();
        this.notebookDao = new JpaNotebookDao();
        this.tagDao = new JpaTagDao();
        this.translationService = new TranslationService();
    }

    // constructor overriding for unit test
    public NoteService(NoteDAO noteDao, NotebookDAO notebookDao, TagDAO tagDao, TranslationService translationService) {
        this.noteDao = noteDao;
        this.notebookDao = notebookDao;
        this.tagDao = tagDao;
        this.translationService = translationService;
    }

    /**
     * Creates a new note with title, content, and annotation
     */
    public NoteEntity createNote(String title, String content, String annotation, NotebookEntity notebookParameter, Set<String> tagNames) {

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

        // create translation
        NoteTranslationEntity translation = translationService.createTranslation(note, DEFAULT_LANGUAGE_CODE);

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

        NoteTranslationEntity translation = translationService.getTranslation(note, langCode, DEFAULT_LANGUAGE_CODE);

        if (translation == null) {
            translation = translationService.getTranslation(note, langCode, DEFAULT_LANGUAGE_CODE);
        }

        if (translation == null) {
            throw new IllegalStateException("No translation available");
        }

        translation.setTitle(title != null ? title.trim() : "");
        translation.setContent(content != null ? content.trim() : "");
        translation.setAnnotation(annotation != null ? annotation.trim() : "");

        // Shift tag handling logic from edit controller and apply current selected tags
        applyTags(note, tagNames);

        return noteDao.save(note);
    }

    public NotebookEntity createNotebook(String name) {
        UserEntity user = UserSession.getUserInstance().getUser();

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Notebook name cannot be empty");
        }

        NotebookEntity notebook = new NotebookEntity(name.trim(), user);
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

        // Create personal notebook for user (e.g., "John's Notebook")
        String notebookName = user.getFirstName() + "'s Notebook";
        NotebookEntity unsavedNotebook  = new NotebookEntity(notebookName, user);
        return notebookDao.save(unsavedNotebook);
    }

    public NoteEntity save(NoteEntity note) {
        return noteDao.save(note);
    }

    private void applyTags(NoteEntity note, Set<String> tagNames) {
        // Clear old tags first
        for (TagEntity tag : new HashSet<>(note.getTags())) {
            note.removeTag(tag);
        }

        if (tagNames == null) return;

        for (String tagName : tagNames) {
            String normalized = tagName.trim().toLowerCase();

            TagEntity tag = tagDao.findByName(normalized);
            if (tag == null) {
                tag = tagDao.save(new TagEntity(normalized));
            }

            note.addTag(tag);
        }
    }
}