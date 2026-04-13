package services;

import dao.note.JpaNoteDao;
import dao.notebook.JpaNotebookDao;
import dao.tag.JpaTagDao;
import entity.entities.NotebookEntity;
import entity.entities.NoteEntity;
import entity.entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import session.UserSession;

import java.util.ArrayList;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NoteServiceTest {

    private final String content = "content";
    private final String annotation = "annotation";
    private final String notNull = "NotNull";
    private final String testTitle = "Test Title";

    private JpaNoteDao noteDao;
    private JpaNotebookDao notebookDao;
    private NoteService noteService;

    @BeforeEach
    void setUp() {
        noteDao = mock(JpaNoteDao.class);
        notebookDao = mock(JpaNotebookDao.class);
        JpaTagDao tagDao = mock(JpaTagDao.class);
        noteService = new NoteService(noteDao, notebookDao, tagDao);
    }

    @Test
    void createNoteNullTitleTest() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");

        try (MockedStatic<UserSession> mockedSession = Mockito.mockStatic(UserSession.class)) {
            UserSession session = mock(UserSession.class);
            when(session.getUser()).thenReturn(user);
            mockedSession.when(UserSession::getUserInstance).thenReturn(session);

            assertThrows(IllegalArgumentException.class, () ->
                    noteService.createNote(null, content, annotation, null, null, "en"));
        }
    }

    @Test
    void createNoteBlankTitleTest() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");

        try (MockedStatic<UserSession> mockedSession = Mockito.mockStatic(UserSession.class)) {
            UserSession session = mock(UserSession.class);
            when(session.getUser()).thenReturn(user);
            mockedSession.when(UserSession::getUserInstance).thenReturn(session);

            assertThrows(IllegalArgumentException.class, () ->
                    noteService.createNote("   ", content, annotation, null, null, "en"));
        }
    }

    @Test
    void createNoteNoUserLoggedInTest() {
        try (MockedStatic<UserSession> mockedSession = Mockito.mockStatic(UserSession.class)) {
            UserSession session = mock(UserSession.class);
            when(session.getUser()).thenReturn(null);
            mockedSession.when(UserSession::getUserInstance).thenReturn(session);

            assertThrows(IllegalStateException.class, () ->
                    noteService.createNote("title", content, annotation, null, null, "en"));
        }
    }

    @Test
    void createNoteWithNotebookTest() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");
        NotebookEntity notebook = new NotebookEntity(user);

        var translation = notebook.createTranslation("en");
        translation.setTitle("Test Notebook");
        Set<String> tags = Set.of(notNull);
        NoteEntity expectedNote = new NoteEntity();

        try (MockedStatic<UserSession> mockedSession = Mockito.mockStatic(UserSession.class)) {
            UserSession session = mock(UserSession.class);
            when(session.getUser()).thenReturn(user);
            mockedSession.when(UserSession::getUserInstance).thenReturn(session);
            when(noteDao.save(any(NoteEntity.class))).thenReturn(expectedNote);

            NoteEntity result = noteService.createNote(testTitle, content, annotation, notebook, tags, "en");

            assertNotNull(result);
            verify(noteDao, times(1)).save(any(NoteEntity.class));
        }
    }

    @Test
    void createNoteNullNotebookCreatesPersonalNotebookTest() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");
        NotebookEntity personalNotebook = new NotebookEntity(user);

        var translation = personalNotebook.createTranslation("en");
        translation.setTitle("Test's Notebook");
        NoteEntity expectedNote = new NoteEntity();

        try (MockedStatic<UserSession> mockedSession = Mockito.mockStatic(UserSession.class)) {
            UserSession session = mock(UserSession.class);
            when(session.getUser()).thenReturn(user);
            mockedSession.when(UserSession::getUserInstance).thenReturn(session);
            when(notebookDao.findByUser(user)).thenReturn(new ArrayList<>());
            when(notebookDao.save(any(NotebookEntity.class))).thenReturn(personalNotebook);
            when(noteDao.save(any(NoteEntity.class))).thenReturn(expectedNote);

            NoteEntity result = noteService.createNote(testTitle, content, annotation, null, null, "en");

            assertNotNull(result);
            verify(notebookDao, times(1)).findByUser(user);
            verify(notebookDao, times(1)).save(any(NotebookEntity.class));
            verify(noteDao, times(1)).save(any(NoteEntity.class));
        }
    }

    @Test
    void createNoteNullContentUsesEmptyStringTest() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");
        NotebookEntity notebook = new NotebookEntity(user);

        var translation = notebook.createTranslation("en");
        translation.setTitle("Content Notebook");
        Set<String> tags = Set.of(notNull);
        NoteEntity expectedNote = new NoteEntity();

        try (MockedStatic<UserSession> mockedSession = Mockito.mockStatic(UserSession.class)) {
            UserSession session = mock(UserSession.class);
            when(session.getUser()).thenReturn(user);
            mockedSession.when(UserSession::getUserInstance).thenReturn(session);
            when(noteDao.save(any(NoteEntity.class))).thenReturn(expectedNote);

            NoteEntity result = noteService.createNote(testTitle, null, annotation, notebook, tags, "en");

            assertNotNull(result);
            verify(noteDao, times(1)).save(any(NoteEntity.class));
        }
    }

    @Test
    void createNoteNullAnnotationUsesEmptyStringTest() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");
        NotebookEntity notebook = new NotebookEntity(user);

        var translation = notebook.createTranslation("en");
        translation.setTitle("Annotation Notebook");
        Set<String> tags = Set.of(notNull);
        NoteEntity expectedNote = new NoteEntity();

        try (MockedStatic<UserSession> mockedSession = Mockito.mockStatic(UserSession.class)) {
            UserSession session = mock(UserSession.class);
            when(session.getUser()).thenReturn(user);
            mockedSession.when(UserSession::getUserInstance).thenReturn(session);
            when(noteDao.save(any(NoteEntity.class))).thenReturn(expectedNote);

            NoteEntity result = noteService.createNote(testTitle, content, null, notebook, tags, "en");

            assertNotNull(result);
            verify(noteDao, times(1)).save(any(NoteEntity.class));
        }
    }
}
