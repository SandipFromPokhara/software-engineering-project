package services;

import dao.note.JpaNoteDao;
import dao.notebook.JpaNotebookDao;
import dao.tag.JpaTagDao;
import entity.entities.NotebookEntity;
import entity.entities.NoteEntity;
import entity.entities.UserEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import session.UserSession;

import java.util.ArrayList;
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
}
