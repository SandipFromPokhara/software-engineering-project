package controller;

import dao.notebook.JpaNoteBookDao;
import entity.NoteBookEntity;
import entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.NoteService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateNoteControllerTest {

    private CreateNoteController controller;
    private NoteService noteService;
    private JpaNoteBookDao notebookDao;

    @BeforeEach
    void setUp() {
        noteService = mock(NoteService.class);
        notebookDao = mock(JpaNoteBookDao.class);
        controller = new CreateNoteController(noteService, notebookDao);
    }

    @Test
    void controllerInstantiationWithDefaultConstructorTest() {
        CreateNoteController defaultController = new CreateNoteController();
        assertNotNull(defaultController);
    }

    @Test
    void controllerInstantiationWithDependenciesTest() {
        assertNotNull(controller);
    }

    @Test
    void multipleInstancesAreIndependentTest() {
        CreateNoteController controller1 = new CreateNoteController(noteService, notebookDao);
        CreateNoteController controller2 = new CreateNoteController(noteService, notebookDao);

        assertNotNull(controller1);
        assertNotNull(controller2);
        assertNotSame(controller1, controller2);
    }

    @Test
    void validateTitleValidTitleTest() {
        assertTrue(controller.validateTitle("Valid Title"));
    }

    @Test
    void validateTitleWithSpacesTest() {
        assertTrue(controller.validateTitle("  Title with spaces  "));
    }

    @Test
    void validateTitleNullTest() {
        assertFalse(controller.validateTitle(null));
    }

    @Test
    void validateTitleEmptyTest() {
        assertFalse(controller.validateTitle(""));
    }

    @Test
    void validateTitleBlankTest() {
        assertFalse(controller.validateTitle("   "));
    }

    @Test
    void isCreateNewNotebookTrueTest() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");
        NoteBookEntity notebook = new NoteBookEntity("Create New Notebook...", user);

        assertTrue(controller.isCreateNewNotebook(notebook));
    }

    @Test
    void isCreateNewNotebookFalseTest() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");
        NoteBookEntity notebook = new NoteBookEntity("Regular Notebook", user);

        assertFalse(controller.isCreateNewNotebook(notebook));
    }

    @Test
    void isCreateNewNotebookNullTest() {
        assertFalse(controller.isCreateNewNotebook(null));
    }

    @Test
    void getCreateNewConstantTest() {
        assertEquals("Create New Notebook...", controller.getCreateNewConstant());
    }

    @Test
    void createNewConstantIsConsistentTest() {
        String constant1 = controller.getCreateNewConstant();
        String constant2 = controller.getCreateNewConstant();

        assertSame(constant1, constant2);
    }

    @Test
    void validateTitleWorksWithDifferentInputsTest() {
        assertTrue(controller.validateTitle("A"));
        assertTrue(controller.validateTitle("Test Note"));
        assertTrue(controller.validateTitle("123"));
        assertTrue(controller.validateTitle("Special@#$Characters"));

        assertFalse(controller.validateTitle(""));
        assertFalse(controller.validateTitle(" "));
        assertFalse(controller.validateTitle(null));
    }

    @Test
    void isCreateNewNotebookWorksWithDifferentNotebooksTest() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");

        assertTrue(controller.isCreateNewNotebook(new NoteBookEntity("Create New Notebook...", user)));
        assertFalse(controller.isCreateNewNotebook(new NoteBookEntity("My Notebook", user)));
        assertFalse(controller.isCreateNewNotebook(new NoteBookEntity("Work Notes", user)));
        assertFalse(controller.isCreateNewNotebook(null));
    }
}
