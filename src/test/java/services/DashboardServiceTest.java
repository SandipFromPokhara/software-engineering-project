package services;

import dao.note.INoteDAO;
import dao.notebook.INotebookDAO;
import entity.entities.NoteEntity;
import entity.entities.NotebookEntity;
import entity.entities.UserEntity;
import entity.translationentities.NoteTranslationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import session.NotebookSession;
import session.UserSession;
import util.Localization;

class DashboardServiceTest {

    private INoteDAO noteDao;
    private INotebookDAO notebookDao;
    private TranslationService translationService;
    private DashboardService service;

    @BeforeEach
    void setUp() {
        noteDao = mock(INoteDAO.class);
        notebookDao = mock(INotebookDAO.class);
        translationService = mock(TranslationService.class);

        service = new DashboardService(noteDao, notebookDao, translationService);
    }

    @Test
    void loadNotebooksSortsByCreatedAt() {
        NotebookEntity older = mock(NotebookEntity.class);
        NotebookEntity newer = mock(NotebookEntity.class);

        when(older.getCreatedAt()).thenReturn(LocalDateTime.now().minusDays(2));
        when(newer.getCreatedAt()).thenReturn(LocalDateTime.now());

        UserEntity user = mock(UserEntity.class);
        UserSession userSession = mock(UserSession.class);

        try (MockedStatic<UserSession> userMock = mockStatic(UserSession.class)) {
            userMock.when(UserSession::getUserInstance).thenReturn(userSession);
            when(userSession.getUser()).thenReturn(user);

            when(notebookDao.findByUser(user))
                    .thenReturn(new ArrayList<>(List.of(newer, older)));

            List<NotebookEntity> result = service.loadNotebooks();

            assertEquals(older, result.get(0));
            assertEquals(newer, result.get(1));
        }
    }

    @Test
    void getInitialNotebookReturnsLastCreatedIfExists() {
        NotebookEntity last = mock(NotebookEntity.class);

        try (MockedStatic<NotebookSession> sessionMock = mockStatic(NotebookSession.class)) {
            sessionMock.when(NotebookSession::getLastCreatedNotebook)
                    .thenReturn(last);

            NotebookEntity result = service.getInitialNotebook();

            assertEquals(last, result);
            sessionMock.verify(NotebookSession::clear);
        }
    }

    @Test
    void getInitialNotebook_fallsBackToFirstNotebook() {
        NotebookEntity first = mock(NotebookEntity.class);

        try (MockedStatic<NotebookSession> sessionMock = mockStatic(NotebookSession.class)) {
            sessionMock.when(NotebookSession::getLastCreatedNotebook)
                    .thenReturn(null);

            DashboardService spy = spy(service);
            doReturn(List.of(first)).when(spy).loadNotebooks();

            NotebookEntity result = spy.getInitialNotebook();

            assertEquals(first, result);
        }
    }

    @Test
    void getInitialNotebook_returnsNullIfNoNotebooks() {
        try (MockedStatic<NotebookSession> sessionMock = mockStatic(NotebookSession.class)) {
            sessionMock.when(NotebookSession::getLastCreatedNotebook)
                    .thenReturn(null);

            DashboardService spy = spy(service);
            doReturn(List.of()).when(spy).loadNotebooks();

            NotebookEntity result = spy.getInitialNotebook();

            assertNull(result);
        }
    }

    @Test
    void loadNotesReturnsEmptyIfNotebookNull() {
        List<NoteEntity> result = service.loadNotes(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void loadNotesSortsByUpdatedAtDescending() {
        NotebookEntity notebook = mock(NotebookEntity.class);

        NoteEntity older = mock(NoteEntity.class);
        NoteEntity newer = mock(NoteEntity.class);

        when(older.getUpdatedAt()).thenReturn(LocalDateTime.now().minusDays(1));
        when(newer.getUpdatedAt()).thenReturn(LocalDateTime.now());

        when(noteDao.findByNotebookWithTranslations(notebook))
                .thenReturn(new ArrayList<>(List.of(older, newer)));

        List<NoteEntity> result = service.loadNotes(notebook);

        assertEquals(newer, result.get(0));
        assertEquals(older, result.get(1));
    }

    @Test
    void deleteNoteCallsDaoWhenValid() {
        NoteEntity note = mock(NoteEntity.class);
        when(note.getId()).thenReturn(1L);

        service.deleteNote(note);

        verify(noteDao).delete(note);
    }

    @Test
    void deleteNoteThrowsIfNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteNote(null));
    }

    @Test
    void deleteNoteThrowsIfIdNull() {
        NoteEntity note = mock(NoteEntity.class);
        when(note.getId()).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.deleteNote(note));
    }

    @Test
    void getDisplayTranslationUsesUppercaseLanguageCode() {
        NoteEntity note = mock(NoteEntity.class);
        NoteTranslationEntity translation = mock(NoteTranslationEntity.class);

        try (MockedStatic<Localization> locMock = mockStatic(Localization.class)) {
            locMock.when(Localization::getCurrentLanguageCode)
                    .thenReturn("en");

            when(translationService.getTranslation(note, "EN", "EN"))
                    .thenReturn(translation);

            NoteTranslationEntity result = service.getDisplayTranslation(note);

            assertEquals(translation, result);
        }
    }
}