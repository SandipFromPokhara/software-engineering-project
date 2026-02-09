package services;

import dao.note.JpaNoteDao;
import dao.notebook.JpaNoteBookDao;
import entity.NoteBookEntity;
import entity.NoteEntity;
import entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import session.UserSession;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NoteServiceTest {

    private JpaNoteDao noteDao;
    private JpaNoteBookDao notebookDao;
    private NoteService noteService;

    @BeforeEach
    void setUp() {
        noteDao = mock(JpaNoteDao.class);
        notebookDao = mock(JpaNoteBookDao.class);
        noteService = new NoteService(noteDao, notebookDao);
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
                noteService.createNote(null, "content", "annotation", null));
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
                noteService.createNote("   ", "content", "annotation", null));
        }
    }

    @Test
    void createNoteNoUserLoggedInTest() {
        try (MockedStatic<UserSession> mockedSession = Mockito.mockStatic(UserSession.class)) {
            UserSession session = mock(UserSession.class);
            when(session.getUser()).thenReturn(null);
            mockedSession.when(UserSession::getUserInstance).thenReturn(session);

            assertThrows(IllegalStateException.class, () ->
                noteService.createNote("title", "content", "annotation", null));
        }
    }

    @Test
    void createNoteWithNotebookTest() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");
        NoteBookEntity notebook = new NoteBookEntity("Test Notebook", user);
        NoteEntity expectedNote = new NoteEntity("Test Title", "content", "annotation");

        try (MockedStatic<UserSession> mockedSession = Mockito.mockStatic(UserSession.class)) {
            UserSession session = mock(UserSession.class);
            when(session.getUser()).thenReturn(user);
            mockedSession.when(UserSession::getUserInstance).thenReturn(session);
            when(noteDao.save(any(NoteEntity.class))).thenReturn(expectedNote);

            NoteEntity result = noteService.createNote("Test Title", "content", "annotation", notebook);

            assertNotNull(result);
            verify(noteDao, times(1)).save(any(NoteEntity.class));
        }
    }

    @Test
    void createNoteNullNotebookCreatesPersonalNotebookTest() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");
        NoteBookEntity personalNotebook = new NoteBookEntity("Test's Notebook", user);
        NoteEntity expectedNote = new NoteEntity("Test Title", "content", "annotation");

        try (MockedStatic<UserSession> mockedSession = Mockito.mockStatic(UserSession.class)) {
            UserSession session = mock(UserSession.class);
            when(session.getUser()).thenReturn(user);
            mockedSession.when(UserSession::getUserInstance).thenReturn(session);
            when(notebookDao.findByUser(user)).thenReturn(new ArrayList<>());
            when(notebookDao.save(any(NoteBookEntity.class))).thenReturn(personalNotebook);
            when(noteDao.save(any(NoteEntity.class))).thenReturn(expectedNote);

            NoteEntity result = noteService.createNote("Test Title", "content", "annotation", null);

            assertNotNull(result);
            verify(notebookDao, times(1)).findByUser(user);
            verify(notebookDao, times(1)).save(any(NoteBookEntity.class));
            verify(noteDao, times(1)).save(any(NoteEntity.class));
        }
    }

    @Test
    void createNoteNullContentUsesEmptyStringTest() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");
        NoteBookEntity notebook = new NoteBookEntity("Content Notebook", user);
        NoteEntity expectedNote = new NoteEntity("Test Title", "", "annotation");

        try (MockedStatic<UserSession> mockedSession = Mockito.mockStatic(UserSession.class)) {
            UserSession session = mock(UserSession.class);
            when(session.getUser()).thenReturn(user);
            mockedSession.when(UserSession::getUserInstance).thenReturn(session);
            when(noteDao.save(any(NoteEntity.class))).thenReturn(expectedNote);

            NoteEntity result = noteService.createNote("Test Title", null, "annotation", notebook);

            assertNotNull(result);
            verify(noteDao, times(1)).save(any(NoteEntity.class));
        }
    }

    @Test
    void createNoteNullAnnotationUsesEmptyStringTest() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");
        NoteBookEntity notebook = new NoteBookEntity("Annotation Notebook", user);
        NoteEntity expectedNote = new NoteEntity("Test Title", "content", "");

        try (MockedStatic<UserSession> mockedSession = Mockito.mockStatic(UserSession.class)) {
            UserSession session = mock(UserSession.class);
            when(session.getUser()).thenReturn(user);
            mockedSession.when(UserSession::getUserInstance).thenReturn(session);
            when(noteDao.save(any(NoteEntity.class))).thenReturn(expectedNote);

            NoteEntity result = noteService.createNote("Test Title", "content", null, notebook);

            assertNotNull(result);
            verify(noteDao, times(1)).save(any(NoteEntity.class));
        }
    }
}
