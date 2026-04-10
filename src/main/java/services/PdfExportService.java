package services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import entity.entities.NoteEntity;
import entity.entities.NotebookEntity;
import entity.translationentities.NoteTranslationEntity;
import entity.translationentities.NotebookTranslationEntity;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import util.Localization;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PdfExportService {
    static final Logger logger = Logger.getLogger(PdfExportService.class.getName());
    private final DashboardService dashboardService;

    public PdfExportService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    public boolean exportSelectedNote(NoteEntity note, Window owner) {
        if (note == null) return false;

        NoteTranslationEntity t = dashboardService.getDisplayTranslation(note);
        if (t == null) return false;

        return exportNotesToPdf(List.of(note), t.getTitle(), owner);
    }

    public boolean exportEntireNotebook(NotebookEntity notebook, Window owner) {
        List<NoteEntity> notes = dashboardService.loadNotes(notebook);
        if (notes.isEmpty()) return false;

        String name = notebook.getTranslations().values().stream()
                .findFirst()
                .map(NotebookTranslationEntity::getTitle)
                .orElse("Notebook");

        return exportNotesToPdf(notes, name, owner);
    }

    private boolean exportNotesToPdf(List<NoteEntity> notes, String fileName, Window owner) {
        File file = chooseFile(fileName, owner) ;
        if (file == null) return false;

        try (
                PdfWriter writer = new PdfWriter(file.getAbsolutePath());
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf)
        ) {
            for (int i = 0; i < notes.size(); i++) {
                NoteEntity note = notes.get(i);
                NoteTranslationEntity translation = dashboardService.getDisplayTranslation(note);

                document.add(new Paragraph(translation.getTitle()).setBold().setFontSize(18));
                document.add(new Paragraph(translation.getContent()));

                if (translation.getAnnotation() != null && !translation.getAnnotation().isEmpty()) {
                    document.add(new Paragraph("\nAnnotation:").setBold());
                    document.add(new Paragraph(translation.getAnnotation()));
                }

                if (i < notes.size() - 1) {
                    document.add(new AreaBreak());
                }
            }

            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Export failed", e);
            return false;
        }
    }

    private File chooseFile(String fileName, Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Localization.get("menu.exportPdf"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName(fileName + ".pdf");

        return fileChooser.showSaveDialog(owner);
    }
}
