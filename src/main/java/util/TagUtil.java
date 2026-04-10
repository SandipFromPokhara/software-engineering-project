package util;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

import java.util.Set;

public class TagUtil {

    public static void addTagToUI(Set<String> selectedTags,
                                  FlowPane flowPane,
                                  ComboBox<String> comboBox,
                                  String tagName) {

        if (tagName == null || tagName.isBlank()) return;

        tagName = tagName.trim();

        int maxLength = 15;
        if (tagName.length() > maxLength) return;
        if (!tagName.matches("[a-zA-ZäöåÄÖÅ0-9_-]+")) return;

        // Only add if not already present
        if (!selectedTags.add(tagName)) return;

        if (!comboBox.getItems().contains(tagName)) {
            comboBox.getItems().add(tagName);
        }

        flowPane.getChildren().add(
                createTagBox(tagName, selectedTags, flowPane)
        );

        comboBox.getEditor().clear();
    }


    public static void refreshFlowPane(Set<String> selectedTags, FlowPane flowPane) {
        flowPane.getChildren().clear();

        selectedTags.stream()
                .sorted(String::compareToIgnoreCase)
                .forEach(tagName -> flowPane.getChildren().add(createTagBox(tagName, selectedTags, flowPane))
                );
    }

    private static HBox createTagBox(String tagName, Set<String> selectedTags, FlowPane flowPane) {
        HBox tagBox = new HBox();
        tagBox.setSpacing(6);
        tagBox.getStyleClass().addAll("note-tag", "tag-box");

        Label label = new Label("#" + tagName);
        label.getStyleClass().add("tag-label");

        Button removeBtn = new Button("x");
        removeBtn.getStyleClass().add("tag-remove-btn");

        removeBtn.setOnAction(e -> {
            selectedTags.remove(tagName);
            flowPane.getChildren().remove(tagBox);
        });

        tagBox.getChildren().addAll(label, removeBtn);
        return tagBox;
    }
}
