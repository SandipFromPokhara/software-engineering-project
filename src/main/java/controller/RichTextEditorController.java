package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import util.bulletList.BulletListStrategy;
import util.bulletList.NumberedListStrategy;
import util.bulletList.TextFormattingUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RichTextEditorController {

    @FXML private VBox editorWrapper;

    @FXML private Button boldButton;
    @FXML private Button italicButton;
    @FXML private Button underlineButton;
    @FXML private Button bulletListButton;
    @FXML private Button numberedListButton;
    @FXML private Button headingUpButton;
    @FXML private Button headingDownButton;

    private InlineCssTextArea contentArea;
    private Label placeholderLabel;

    private static final double MIN_FONT_SIZE = 8.0;
    private static final double MAX_FONT_SIZE = 48.0;
    private static final double FONT_STEP = 2.0;
    private static final double DEFAULT_FONT_SIZE = 14.0;

    @FXML
    public void initialize() {
        contentArea = new InlineCssTextArea();
        contentArea.setWrapText(true);
        contentArea.getStyleClass().addAll("editor-textarea", "text-area");
        contentArea.setStyle("-fx-font-family: Verdana; -fx-font-size: 14px;");

        placeholderLabel = new Label("Write your contents...");
        placeholderLabel.getStyleClass().add("editor-placeholder");
        placeholderLabel.setMouseTransparent(true);
        placeholderLabel.setWrapText(true);
        placeholderLabel.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(placeholderLabel, Pos.TOP_LEFT);
        StackPane.setMargin(placeholderLabel, new Insets(8, 8, 8, 10));

        contentArea.textProperty().addListener((obs, old, text) ->
                placeholderLabel.setVisible(text == null || text.isEmpty()));

        VirtualizedScrollPane<InlineCssTextArea> scrollPane = new VirtualizedScrollPane<>(contentArea);

        StackPane stack = new StackPane(scrollPane, placeholderLabel);
        VBox.setVgrow(stack, Priority.ALWAYS);
        editorWrapper.getChildren().add(stack);
    }



    public String getText() {
        return contentArea.getText();
    }

    public void setText(String text) {
        String safeText = text != null ? text : "";
        contentArea.replaceText(0, contentArea.getLength(), safeText);
        if (!safeText.isEmpty()) {
            contentArea.setStyle(0, safeText.length(), "");
        }
        placeholderLabel.setVisible(safeText.isEmpty());
    }

    public InlineCssTextArea getTextArea() {
        return contentArea;
    }

    /**
     * Binds the placeholder text to an observable value.
     * Called by parent controllers instead of promptTextProperty() (which TextArea had but InlineCssTextArea does not).
     */
    public void bindPromptText(javafx.beans.value.ObservableValue<String> binding) {
        if (placeholderLabel == null || binding == null) return;
        String initial = binding.getValue();
        if (initial != null) placeholderLabel.setText(initial);
        binding.addListener((obs, old, newText) -> {
            if (newText != null) placeholderLabel.setText(newText);
        });
    }

    // ---------- Toolbar handler ----------

    @FXML
    private void handleToolbarClick(ActionEvent event) {
        if (!(event.getSource() instanceof Button button)) return;
        switch (button.getId()) {
            case "boldButton"         -> applyBold();
            case "italicButton"       -> applyItalic();
            case "underlineButton"    -> applyUnderline();
            case "bulletListButton"   -> toggleBullet();
            case "numberedListButton" -> toggleNumbered();
            case "headingUpButton"    -> headingUp();
            case "headingDownButton"  -> headingDown();
        }
    }

    // ---------- Formatting actions ----------

    private void applyBold() {
        toggleStyle("-fx-font-weight", "bold");
    }

    private void applyItalic() {
        toggleStyle("-fx-font-style", "italic");
    }

    private void applyUnderline() {
        toggleStyle("-fx-underline", "true");
    }

    private void toggleBullet() {
        TextFormattingUtil.toggleList(contentArea, bulletListButton, new BulletListStrategy());
    }

    private void toggleNumbered() {
        TextFormattingUtil.toggleList(contentArea, numberedListButton, new NumberedListStrategy());
    }

    private void headingUp() {
        changeFontSize(FONT_STEP);
    }

    private void headingDown() {
        changeFontSize(-FONT_STEP);
    }
    /**
     * Toggles a CSS property on the current selection.
     * If every character in the selection already has property=value, the property is removed.
     * Otherwise it is applied to the whole selection.
     */
    private void toggleStyle(String property, String value) {
        IndexRange sel = contentArea.getSelection();
        if (sel.getLength() == 0) return;

        boolean allHave = isPropertySetOnAll(sel.getStart(), sel.getEnd(), property, value);

        StyleSpans<String> spans = contentArea.getStyleSpans(sel.getStart(), sel.getEnd());
        StyleSpans<String> newSpans = spans.mapStyles(style -> allHave
                ? removeProperty(style, property)
                : setProperty(style, property, value));
        contentArea.setStyleSpans(sel.getStart(), newSpans);
    }

    /**
     * Increases or decreases font size by |delta| px on the current selection only.
     * Each character's current size is read individually and clamped to [MIN, MAX].
     */
    private void changeFontSize(double delta) {
        IndexRange sel = contentArea.getSelection();
        if (sel.getLength() == 0) return;

        StyleSpans<String> spans = contentArea.getStyleSpans(sel.getStart(), sel.getEnd());
        StyleSpans<String> newSpans = spans.mapStyles(style -> {
            double current = parseFontSize(style);
            double next = Math.max(MIN_FONT_SIZE, Math.min(MAX_FONT_SIZE, current + delta));
            String updated = setProperty(style, "-fx-font-size", next + "px");
            // Explicitly pin weight to normal so the font renderer does not pick a heavier
            // optical weight for larger sizes (unless the user has explicitly applied bold).
            if (!hasProperty(updated, "-fx-font-weight", "bold")) {
                updated = setProperty(updated, "-fx-font-weight", "normal");
            }
            return updated;
        });
        contentArea.setStyleSpans(sel.getStart(), newSpans);
    }

    // ---------- CSS string helpers ----------
    private boolean isPropertySetOnAll(int from, int to, String property, String value) {
        for (int i = from; i < to; i++) {
            if (!hasProperty(contentArea.getStyleAtPosition(i), property, value)) return false;
        }
        return true;
    }

    private boolean hasProperty(String style, String property, String value) {
        if (style == null || style.isEmpty()) return false;
        return Pattern.compile(
                Pattern.quote(property) + "\\s*:\\s*" + Pattern.quote(value) + "(?:\\s|;|$)",
                Pattern.CASE_INSENSITIVE
        ).matcher(style).find();
    }

    private String setProperty(String style, String property, String value) {
        String cleaned = removeProperty(style, property);
        String decl = property + ": " + value + ";";
        return cleaned.isBlank() ? decl : cleaned.trim() + " " + decl;
    }

    private String removeProperty(String style, String property) {
        if (style == null) return "";
        return style.replaceAll("(?i)" + Pattern.quote(property) + "\\s*:[^;]*;?\\s*", "").trim();
    }

    private double parseFontSize(String style) {
        if (style == null || style.isEmpty()) return DEFAULT_FONT_SIZE;
        Matcher m = Pattern.compile("(?i)-fx-font-size:\\s*(\\d+(?:\\.\\d+)?)(?:px)?").matcher(style);
        return m.find() ? Double.parseDouble(m.group(1)) : DEFAULT_FONT_SIZE;
    }
}
