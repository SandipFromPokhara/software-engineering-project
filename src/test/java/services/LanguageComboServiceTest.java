package services;

import javafx.scene.control.ComboBox;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.BeforeAll;

class LanguageComboServiceTest {

    @BeforeAll
    static void initJavaFX() {
        new JFXPanel(); // initializes JavaFX toolkit
    }

    @Test
    void setupInitializesComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();

        assertDoesNotThrow(() -> LanguageComboService.setup(comboBox));

        // real keys from LanguageModel
        assertTrue(comboBox.getItems().contains("EN"));
        assertTrue(comboBox.getItems().contains("FI"));
        assertTrue(comboBox.getItems().contains("NP"));

        // default value should be one of the valid keys
        assertNotNull(comboBox.getValue());
        assertTrue(comboBox.getItems().contains(comboBox.getValue()));
    }
}