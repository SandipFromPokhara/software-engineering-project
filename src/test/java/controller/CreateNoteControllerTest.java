package controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateNoteControllerTest {

    private CreateNoteController controller;

    @BeforeEach
    void setUp() {
        controller = new CreateNoteController();
    }

    @Test
    void createNoteControllerInstantiationTest() {
        assertNotNull(controller);
    }

    @Test
    void createNoteControllerMultipleInstancesTest() {
        CreateNoteController controller1 = new CreateNoteController();
        CreateNoteController controller2 = new CreateNoteController();

        assertNotNull(controller1);
        assertNotNull(controller2);
        assertNotSame(controller1, controller2);
    }
}
