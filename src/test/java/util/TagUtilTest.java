package util;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.FlowPane;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import testutil.JavaFxTestExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JavaFxTestExtension.class)
class TagUtilTest {

    private Set<String> selectedTags;
    private FlowPane flowPane;
    private ComboBox<String> comboBox;

    @BeforeEach
    void setUp() {
        selectedTags = new HashSet<>();
        flowPane = new FlowPane();
        comboBox = new ComboBox<>(FXCollections.observableArrayList());
    }

    @Test
    void addValidTagAddsToAllStructures() {
        TagUtil.addTagToUI(selectedTags, flowPane, comboBox, "java");

        assertTrue(selectedTags.contains("java"));
        assertTrue(comboBox.getItems().contains("java"));
        assertEquals(1, flowPane.getChildren().size());
    }

    @Test
    void addDuplicateTagDoesNothing() {
        selectedTags.add("java");

        TagUtil.addTagToUI(selectedTags, flowPane, comboBox, "java");

        assertEquals(1, selectedTags.size());
        assertEquals(0, flowPane.getChildren().size());
    }

    @Test
    void addBlankTagIgnored() {
        TagUtil.addTagToUI(selectedTags, flowPane, comboBox, "   ");

        assertTrue(selectedTags.isEmpty());
        assertTrue(flowPane.getChildren().isEmpty());
    }

    @Test
    void invalidTagRejected() {
        TagUtil.addTagToUI(selectedTags, flowPane, comboBox, "invalid tag!");

        assertTrue(selectedTags.isEmpty());
        assertTrue(flowPane.getChildren().isEmpty());
    }

    @Test
    void refreshFlowPaneSortsAndRebuilds() {
        selectedTags.add("banana");
        selectedTags.add("apple");

        TagUtil.refreshFlowPane(selectedTags, flowPane);

        assertEquals(2, flowPane.getChildren().size());
    }
}