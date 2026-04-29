package services;

import javafx.scene.control.ComboBox;
import model.LanguageModel;
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
        ComboBox<LanguageModel.Language> comboBox = new ComboBox<>();

        assertDoesNotThrow(() -> LanguageComboService.setup(comboBox));

        // real keys from LanguageModel
        assertTrue(comboBox.getItems().stream().anyMatch(l -> l.code().equals("EN")));
        assertTrue(comboBox.getItems().stream().anyMatch(l -> l.code().equals("FI")));
        assertTrue(comboBox.getItems().stream().anyMatch(l -> l.code().equals("NP")));

        // default value should be one of the valid keys
        assertNotNull(comboBox.getValue());
        assertTrue(comboBox.getItems().contains(comboBox.getValue()));
    }
}