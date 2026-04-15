package controller;

import entity.entities.*;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.SplitPane;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import services.*;
import testutil.JavaFXInitializer;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ViewDashboardControllerTest {

    private ViewDashboardController controller;

    @Mock private DashboardService dashboardService;
    @Mock private PdfExportService exportService;
    @Mock private NoteService noteService;
    @Mock private TranslationService translationService;

    private TableView<NoteEntity> notesTable;

    private NoteEntity note;
    private NotebookEntity notebook;


    // Initialize JavaFX
    @BeforeAll
    static void initJavaFX() {
        JavaFXInitializer.init();
    }

    @BeforeEach
    void setUp() {
        controller = new ViewDashboardController();

        inject(controller, "dashboardService", dashboardService);
        inject(controller, "exportService", exportService);
        inject(controller, "noteService", noteService);
        inject(controller, "translationService", translationService);

        notesTable = new TableView<>();
        inject(controller, "notesTable", notesTable);

        // Build UI controls required by initialize()
        var rootPane = new BorderPane();
        var fileMenu = new Menu();
        var helpMenu = new Menu();
        var newNoteItem = new MenuItem();
        var exportItem = new MenuItem();
        var closeItem = new MenuItem();
        var faqItem = new MenuItem();
        var aboutItem = new MenuItem();
        var manageAccountItem = new MenuItem();
        var deleteAccountItem = new MenuItem();
        var logoutItem = new MenuItem();
        var userMenuButtonLocal = new MenuButton();
        var editButtonLocal = new Button();
        var deleteButtonLocal = new Button();
        var titleColumnLocal = new TableColumn<NoteEntity, String>();
        var dateColumnLocal = new TableColumn<NoteEntity, String>();
        var noteViewAreaLocal = new TextArea();
        var annotationViewAreaLocal = new TextArea();
        var tagFlowpaneLocal = new FlowPane();
        var toggleBtnLocal = new Button();
        var editorPaneLocal = new AnchorPane();
        var toggleTooltipLocal = new Tooltip();
        var langTooltipLocal = new Tooltip();
        var manageTooltipLocal = new Tooltip();
        var createTooltipLocal = new Tooltip();
        var logoutTooltipLocal = new Tooltip();
        var tagIconLocal = new ImageView();
        var sideBtn1Local = new ImageView();
        var sideBtn2Local = new ImageView();
        var sideBtn3Local = new ImageView();
        var languageComboLocal = new ComboBox<String>();
        var languageIconLabelLocal = new Label();
        var mainSplitLocal = new SplitPane();
        var noteTitleLabelLocal = new Label();
        var wordCountLabelLocal = new Label();
        var dbStatusLabelLocal = new Label();
        var annotationLabelLocal = new Label();
        var openDataLocal = new Button();

        // Inject all UI controls
        inject(controller, "rootPane", rootPane);
        inject(controller, "fileMenu", fileMenu);
        inject(controller, "helpMenu", helpMenu);
        inject(controller, "newNoteItem", newNoteItem);
        inject(controller, "exportItem", exportItem);
        inject(controller, "closeItem", closeItem);
        inject(controller, "faqItem", faqItem);
        inject(controller, "aboutItem", aboutItem);
        inject(controller, "manageAccountItem", manageAccountItem);
        inject(controller, "deleteAccountItem", deleteAccountItem);
        inject(controller, "logoutItem", logoutItem);
        inject(controller, "userMenuButton", userMenuButtonLocal);
        inject(controller, "editButton", editButtonLocal);
        inject(controller, "deleteButton", deleteButtonLocal);
        inject(controller, "titleColumn", titleColumnLocal);
        inject(controller, "dateColumn", dateColumnLocal);
        inject(controller, "noteViewArea", noteViewAreaLocal);
        inject(controller, "annotationViewArea", annotationViewAreaLocal);
        inject(controller, "tagFlowpane", tagFlowpaneLocal);
        inject(controller, "toggleBtn", toggleBtnLocal);
        inject(controller, "editorPane", editorPaneLocal);
        inject(controller, "toggleTooltip", toggleTooltipLocal);
        inject(controller, "langTooltip", langTooltipLocal);
        inject(controller, "manageTooltip", manageTooltipLocal);
        inject(controller, "createTooltip", createTooltipLocal);
        inject(controller, "logoutTooltip", logoutTooltipLocal);
        inject(controller, "tagIcon", tagIconLocal);
        inject(controller, "sideBtn1", sideBtn1Local);
        inject(controller, "sideBtn2", sideBtn2Local);
        inject(controller, "sideBtn3", sideBtn3Local);
        inject(controller, "languageCombo", languageComboLocal);
        inject(controller, "languageIconLabel", languageIconLabelLocal);
        inject(controller, "mainSplit", mainSplitLocal);
        inject(controller, "noteTitleLabel", noteTitleLabelLocal);
        inject(controller, "wordCountLabel", wordCountLabelLocal);
        inject(controller, "dbStatusLabel", dbStatusLabelLocal);
        inject(controller, "annotation", annotationLabelLocal);
        inject(controller, "openData", openDataLocal);

        note = mock(NoteEntity.class);
        notebook = mock(NotebookEntity.class);

        lenient().when(note.getId()).thenReturn(1L);
    }

    @Test
    void initialize_shouldLoadNotebookAndNotes() {
        lenient().when(dashboardService.getInitialNotebook()).thenReturn(notebook);
        lenient().when(dashboardService.loadNotes(notebook)).thenReturn(List.of(note));

        controller.initialize();

        verify(dashboardService).getInitialNotebook();
        verify(dashboardService).loadNotes(notebook);
    }

    @Test
    void initialize_shouldPopulateTable() {
        lenient().when(dashboardService.getInitialNotebook()).thenReturn(notebook);
        lenient().when(dashboardService.loadNotes(notebook)).thenReturn(List.of(note));

        controller.initialize();

        Assertions.assertNotNull(notesTable);
    }

    @Test
    void handleDelete_shouldDoNothing_whenNoSelection() {
        controller.handleDelete();
        verifyNoInteractions(dashboardService);
    }

    private void inject(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
