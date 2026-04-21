package services;

import entity.entities.NoteEntity;
import entity.entities.NotebookEntity;
import entity.translationentities.NoteTranslationEntity;
import entity.translationentities.NotebookTranslationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PdfExportServiceTest {

    private DashboardService dashboardService;
    private PdfExportService service;

    @BeforeEach
    void setup() {
        dashboardService = mock(DashboardService.class);
        service = spy(new PdfExportService(dashboardService));
    }

    @Test
    void exportSelectedNoteNullNoteReturnsFalse() {
        assertFalse(service.exportSelectedNote(null, null));
    }

    @Test
    void exportSelectedNoteNullTranslationReturnsFalse() {
        NoteEntity note = mock(NoteEntity.class);

        when(dashboardService.getDisplayTranslation(note)).thenReturn(null);

        assertFalse(service.exportSelectedNote(note, null));
    }

    @Test
    void exportSelectedNoteSuccess() {
        NoteEntity note = mock(NoteEntity.class);
        NoteTranslationEntity translation = mock(NoteTranslationEntity.class);

        when(translation.getTitle()).thenReturn("Test");
        when(translation.getContent()).thenReturn("content");

        when(dashboardService.getDisplayTranslation(note)).thenReturn(translation);

        File file = new File("test.pdf");

        doReturn(file).when(service).chooseFile(any(), any());

        boolean result = service.exportSelectedNote(note, null);

        assertTrue(result);
    }

    @Test
    void exportEntireNotebookEmptyNotesReturnsFalse() {
        NotebookEntity notebook = mock(NotebookEntity.class);

        when(dashboardService.loadNotes(notebook)).thenReturn(List.of());

        assertFalse(service.exportEntireNotebook(notebook, null));
    }

    @Test
    void exportEntireNotebookSuccess() {
        NotebookEntity notebook = mock(NotebookEntity.class);
        NoteEntity note = mock(NoteEntity.class);

        NoteTranslationEntity translation = mock(NoteTranslationEntity.class);
        when(translation.getTitle()).thenReturn("Title");
        when(translation.getContent()).thenReturn("Content");

        when(dashboardService.loadNotes(notebook))
                .thenReturn(List.of(note));

        when(dashboardService.getDisplayTranslation(note))
                .thenReturn(translation);

        NotebookTranslationEntity notebookTranslation = mock(NotebookTranslationEntity.class);
        when(notebookTranslation.getTitle()).thenReturn("NotebookName");

        when(notebook.getTranslations())
                .thenReturn(Map.of("en", notebookTranslation));

        File file = new File("test.pdf");
        doReturn(file).when(service).chooseFile(any(), any());

        boolean result = service.exportEntireNotebook(notebook, null);

        assertTrue(result);
    }

    @Test
    void exportNotesToPdfExceptionReturnsFalse() {
        NoteEntity note = mock(NoteEntity.class);
        NoteTranslationEntity translation = mock(NoteTranslationEntity.class);

        when(translation.getTitle()).thenReturn("Title");
        when(translation.getContent()).thenReturn("Content");

        when(dashboardService.getDisplayTranslation(note))
                .thenReturn(translation);

        doReturn(null).when(service).chooseFile(any(), any());

        boolean result = service.exportSelectedNote(note, null);

        assertFalse(result);
    }
}