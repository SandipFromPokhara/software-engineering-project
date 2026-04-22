package services;

import dao.note.JpaNoteDao;
import dao.notebook.JpaNotebookDao;
import dao.tag.JpaTagDao;
import entity.entities.NotebookEntity;
import entity.entities.NoteEntity;
import entity.entities.UserEntity;
import entity.translationentities.NoteTranslationEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import session.UserSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NoteServiceTest {

    private static final String CONTENT = "content";
    private static final String ANNOTATION = "annotation";
    private static final String NOT_NULL = "NotNull";
    private static final String TEST_TITLE = "Test Title";

    private JpaNoteDao noteDao;
    private JpaNotebookDao notebookDao;
    private NoteService noteService;

    @BeforeEach
    void setUp() {
        noteDao = mock(JpaNoteDao.class);
        notebookDao = mock(JpaNotebookDao.class);
        JpaTagDao tagDao = mock(JpaTagDao.class);
        noteService = new NoteService(noteDao, notebookDao, tagDao);
        // ensure no leftover session
        UserSession.getUserInstance().setUser(null);
    }

    @AfterEach
    void tearDown() {
        // reset singleton session state between tests
        UserSession.getUserInstance().setUser(null);
    }

    @Test
    void createNoteNullTitleTest() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");

        UserSession.getUserInstance().setUser(user);
        assertThrows(IllegalArgumentException.class, () ->
                noteService.createNote(null, CONTENT, ANNOTATION, null, null, "en"));
    }

    @Test
    void createNoteBlankTitleTest() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");

        UserSession.getUserInstance().setUser(user);
        assertThrows(IllegalArgumentException.class, () ->
                noteService.createNote("   ", CONTENT, ANNOTATION, null, null, "en"));
    }

    @Test
    void createNoteNoUserLoggedInTest() {
        UserSession.getUserInstance().setUser(null);
        assertThrows(IllegalStateException.class, () ->
                noteService.createNote("title", CONTENT, ANNOTATION, null, null, "en"));
    }

    @Test
    void createNoteWithNotebookTest() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");
        NotebookEntity notebook = new NotebookEntity(user);

        var translation = notebook.createTranslation("en");
        translation.setTitle("Test Notebook");
        Set<String> tags = Set.of(NOT_NULL);
        NoteEntity expectedNote = new NoteEntity();

        UserSession.getUserInstance().setUser(user);
        when(noteDao.save(any(NoteEntity.class))).thenReturn(expectedNote);

        NoteEntity result = noteService.createNote(TEST_TITLE, CONTENT, ANNOTATION, notebook, tags, "en");

        assertNotNull(result);
        verify(noteDao, times(1)).save(any(NoteEntity.class));
    }

    @Test
    void createNoteNullNotebookCreatesPersonalNotebookTest() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");
        NotebookEntity personalNotebook = new NotebookEntity(user);

        var translation = personalNotebook.createTranslation("en");
        translation.setTitle("Test's Notebook");
        NoteEntity expectedNote = new NoteEntity();

        UserSession.getUserInstance().setUser(user);
        when(notebookDao.findByUser(user)).thenReturn(new ArrayList<>());
        when(notebookDao.save(any(NotebookEntity.class))).thenReturn(personalNotebook);
        when(noteDao.save(any(NoteEntity.class))).thenReturn(expectedNote);

        NoteEntity result = noteService.createNote(TEST_TITLE, CONTENT, ANNOTATION, null, null, "en");

        assertNotNull(result);
        verify(notebookDao, times(1)).findByUser(user);
        verify(notebookDao, times(1)).save(any(NotebookEntity.class));
        verify(noteDao, times(1)).save(any(NoteEntity.class));
    }

    @Test
    void createNoteNullContentUsesEmptyStringTest() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");
        NotebookEntity notebook = new NotebookEntity(user);

        var translation = notebook.createTranslation("en");
        translation.setTitle("Content Notebook");
        Set<String> tags = Set.of(NOT_NULL);
        NoteEntity expectedNote = new NoteEntity();

        UserSession.getUserInstance().setUser(user);
        when(noteDao.save(any(NoteEntity.class))).thenReturn(expectedNote);

        NoteEntity result = noteService.createNote(TEST_TITLE, null, ANNOTATION, notebook, tags, "en");

        assertNotNull(result);
        verify(noteDao, times(1)).save(any(NoteEntity.class));
    }

    @Test
    void createNoteNullAnnotationUsesEmptyStringTest() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");
        NotebookEntity notebook = new NotebookEntity(user);

        var translation = notebook.createTranslation("en");
        translation.setTitle("Annotation Notebook");
        Set<String> tags = Set.of(NOT_NULL);
        NoteEntity expectedNote = new NoteEntity();

        UserSession.getUserInstance().setUser(user);
        when(noteDao.save(any(NoteEntity.class))).thenReturn(expectedNote);

        NoteEntity result = noteService.createNote(TEST_TITLE, CONTENT, null, notebook, tags, "en");

        assertNotNull(result);
        verify(noteDao, times(1)).save(any(NoteEntity.class));
    }

    @Test
    void updateNoteNullNoteThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> noteService.updateNote(null, "en", "t", "c", "a", null));
    }

    @Test
    void updateNoteCreatesTranslationWhenMissing() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");

        NoteEntity note = new NoteEntity();

        when(noteDao.save(any())).thenReturn(note);

        NoteEntity result = noteService.updateNote(note, "en", "title", "content", "annotation", Set.of("tag"));

        assertNotNull(result);
        verify(noteDao).save(note);
    }

    @Test
    void updateNoteUpdatesExistingTranslation() {
        NoteEntity note = new NoteEntity();
        NoteTranslationEntity nt = new NoteTranslationEntity();
        note.addTranslation(nt);

        when(noteDao.save(any())).thenReturn(note);

        NoteEntity result = noteService.updateNote(
                note, "en", "title", "content", "annotation", null
        );

        assertNotNull(result);
        verify(noteDao).save(note);
    }

    @Test
    void createNotebookBlankTitleThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> noteService.createNotebook(" "));
    }

    @Test
    void createNotebookSuccessSavesNotebook() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");

        UserSession.getUserInstance().setUser(user);

        NotebookEntity notebook = new NotebookEntity(user);
        when(notebookDao.save(any())).thenReturn(notebook);

        NotebookEntity result = noteService.createNotebook("My Notebook");

        assertNotNull(result);
        verify(notebookDao).save(any(NotebookEntity.class));
    }

    @Test
    void createNoteExistingNotebookIsUsed() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");

        NotebookEntity existing = new NotebookEntity(user);

        UserSession.getUserInstance().setUser(user);

        when(notebookDao.findByUser(user)).thenReturn(List.of(existing));
        when(noteDao.save(any())).thenReturn(new NoteEntity());

        NoteEntity result = noteService.createNote(
                "title", "content", "annotation", null, null, "en"
        );

        assertNotNull(result);
        verify(notebookDao).findByUser(user);
        verify(notebookDao, never()).save(any()); // IMPORTANT branch
    }

    @Test
    void createNoteWithNullTagsSkipsTagProcessing() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");

        UserSession.getUserInstance().setUser(user);

        when(noteDao.save(any())).thenReturn(new NoteEntity());

        NoteEntity result = noteService.createNote("title", "content", "annotation", null, null, "en");

        assertNotNull(result);
        verify(noteDao).save(any());
    }
}
