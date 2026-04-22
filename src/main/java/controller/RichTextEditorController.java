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
import javafx.collections.ListChangeListener;
import javafx.application.Platform;
import javafx.scene.input.KeyEvent;
import util.RichTextStorageUtil;
import util.ToggleUtil;
import util.list.BulletListStrategy;
import util.list.NumberedListStrategy;
import util.list.TextFormattingUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RichTextEditorController {

    @FXML
    private VBox editorWrapper;

    @FXML
    private Button boldButton;
    @FXML
    private Button italicButton;
    @FXML
    private Button underlineButton;
    @FXML
    private Button bulletListButton;
    @FXML
    private Button numberedListButton;
    @FXML
    private Button headingUpButton;
    @FXML
    private Button headingDownButton;

    private InlineCssTextArea contentArea;
    private Label placeholderLabel;
    private VirtualizedScrollPane<InlineCssTextArea> editorScrollPane;
    private boolean defaultStyleApplied = false;

    private static final double MIN_FONT_SIZE = 8.0;
    private static final double MAX_FONT_SIZE = 48.0;
    private static final double FONT_STEP = 2.0;
    private static final double DEFAULT_FONT_SIZE = 14.0;
    private static final String FONT_WEIGHT_PROPERTY = "-fx-font-weight";

    @FXML
    public void initialize() {
        initAll();
    }

    private void initAll() {
        // Delegate initialization to smaller helpers to reduce cognitive complexity
        setupEditorNodes();
        scheduleInitialThemeApply();
        addThemeSceneListener();
        addKeyFallback();
    }

    // --- extracted helpers to keep initialize() simple ---
    private void setupEditorNodes() {
        contentArea = new InlineCssTextArea();
        contentArea.setWrapText(true);
        contentArea.getStyleClass().add("rich-text-area");

        placeholderLabel = new Label("Write your contents...");
        placeholderLabel.getStyleClass().add("editor-placeholder");
        placeholderLabel.setMouseTransparent(true);
        placeholderLabel.setWrapText(true);
        placeholderLabel.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(placeholderLabel, Pos.TOP_LEFT);
        StackPane.setMargin(placeholderLabel, new Insets(8, 8, 8, 10));

        contentArea.textProperty().addListener((obs, old, text) ->
                placeholderLabel.setVisible(text == null || text.isEmpty()));

        editorScrollPane = new VirtualizedScrollPane<>(contentArea);
        // Use CSS class to let theme.css control scroll pane background
        editorScrollPane.getStyleClass().add("editor-scroll-pane");

        StackPane stack = new StackPane(editorScrollPane, placeholderLabel);
        VBox.setVgrow(stack, Priority.ALWAYS);
        editorWrapper.getChildren().add(stack);
    }

    private void scheduleInitialThemeApply() {
        // Use runLater so the style is applied after the scene/window is fully initialized.
        Platform.runLater(this::applyThemeToEditor);
    }

    private void addThemeSceneListener() {
        // Listen for theme class changes on the root so the editor updates when the app toggles theme
        editorWrapper.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && newScene.getRoot() != null) {
                // Re-apply theme
                applyThemeToEditor();
                newScene.getRoot().getStyleClass().addListener((ListChangeListener<String>) change -> {
                    while (change.next()) {
                        if (change.wasAdded() || change.wasRemoved()) {
                            // Re-apply theme styles for the editor components
                            applyThemeToEditor();
                        }
                    }
                });
            }
        });
    }

    private void addKeyFallback() {
        // Fallback: when the user types in a newly created editor, ensure the default style is applied
        contentArea.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            if (!defaultStyleApplied) {
                defaultStyleApplied = true;
                Platform.runLater(this::applyThemeToEditor);
            }
        });
    }

    private void applyThemeToEditor() {
        // Update style spans where explicit -fx-fill was previously set: switch black<->light when theme changes
        updateSpansForTheme();
        // Ensure the editor/control backgrounds match other text areas in the app.
        if (ToggleUtil.isDarkMode()) {
            // Dark background similar to other dark mode text areas and light text
            contentArea.setStyle("-fx-control-inner-background: #4a4a4a; -fx-background-color: #3c3c3c; -fx-fill: #e6e6e6; -fx-text-fill: #e6e6e6;");
            if (editorScrollPane != null) editorScrollPane.setStyle("-fx-background-color: #3c3c3c;");
            // Ensure insertion/text default style is set so newly typed text uses correct color
            Platform.runLater(() -> {
                try {
                    contentArea.setStyle(0, Math.max(0, contentArea.getLength()), "-fx-fill: #e6e6e6; -fx-text-fill: #e6e6e6;");
                } catch (Exception ignored) { /* Ignore: if contentArea isn't fully initialized yet, we can't set style. It will be set on next key typed or theme change. */
                }
            });
        } else {
            contentArea.setStyle("-fx-control-inner-background: #ffffff; -fx-background-color: #ffffff; -fx-fill: #000000; -fx-text-fill: #000000;");
            if (editorScrollPane != null) editorScrollPane.setStyle("-fx-background-color: #ffffff;");
            Platform.runLater(() -> {
                try {
                    contentArea.setStyle(0, Math.max(0, contentArea.getLength()), "-fx-fill: #000000; -fx-text-fill: #000000;");
                } catch (Exception ignored) { /* Ignore: if contentArea isn't fully initialized yet, we can't set style. It will be set on next key typed or theme change. */
                }
            });
        }
    }

    private void updateSpansForTheme() {
        int len = contentArea.getLength();
        if (len == 0) return;
        StyleSpans<String> spans = contentArea.getStyleSpans(0, len);
        StyleSpans<String> newSpans = spans.mapStyles(style -> {
            String s = style == null ? "" : style;
            if (ToggleUtil.isDarkMode()) {
                // Replace explicit black fills with light fill
                s = s.replaceAll("(?i)-fx-fill:\\s*(?:#000000|#000|black)\\s*;?", "-fx-fill: #e6e6e6;");
                s = s.replaceAll("(?i)-fx-text-fill:\\s*(?:#000000|#000|black)\\s*;?", "-fx-text-fill: #e6e6e6;");
            } else {
                // Replace explicit light fills with black in light mode
                s = s.replaceAll("(?i)-fx-fill:\\s*#e6e6e6\\s*;?", "-fx-fill: #000000;");
                s = s.replaceAll("(?i)-fx-text-fill:\\s*#e6e6e6\\s*;?", "-fx-text-fill: #000000;");
            }
            return s.trim();
        });
        contentArea.setStyleSpans(0, newSpans);
    }

    public String getText() {
        return contentArea.getText();
    }

    public String getSerializedContent() {
        return RichTextStorageUtil.serialize(
                contentArea.getText(),
                contentArea.getStyleSpans(0, contentArea.getLength())
        );
    }

    public void setText(String text) {
        String safeText = text != null ? text : "";
        contentArea.replaceText(0, contentArea.getLength(), safeText);
        placeholderLabel.setVisible(safeText.isEmpty());
        // Re-apply theme in case text insertion added default spans or changed rendering
        Platform.runLater(this::applyThemeToEditor);
    }

    public void setSerializedContent(String stored) {
        RichTextStorageUtil.DecodedContent decoded = RichTextStorageUtil.decode(stored);
        String text = decoded.text() == null ? "" : decoded.text();

        contentArea.replaceText(0, contentArea.getLength(), text);
        if (decoded.spans() != null) {
            contentArea.setStyleSpans(0, decoded.spans());
        }

        placeholderLabel.setVisible(text.isEmpty());
        // Ensure any explicit fills in spans are adapted to the current theme
        Platform.runLater(this::applyThemeToEditor);
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
        java.util.Optional.ofNullable(binding.getValue()).ifPresent(placeholderLabel::setText);
        binding.addListener((obs, old, newText) -> {
            if (newText != null) placeholderLabel.setText(newText);
        });
    }

    // ---------- Toolbar handler ----------

    @FXML
    private void handleToolbarClick(ActionEvent event) {
        if (!(event.getSource() instanceof Button button)) return;
        switch (button.getId()) {
            case "boldButton" -> applyBold();
            case "italicButton" -> applyItalic();
            case "underlineButton" -> applyUnderline();
            case "bulletListButton" -> toggleBullet();
            case "numberedListButton" -> toggleNumbered();
            case "headingUpButton" -> headingUp();
            case "headingDownButton" -> headingDown();
            default -> {
                // No-op: unrecognized toolbar button.
            }
        }
    }

    // ---------- Formatting actions ----------

    private void applyBold() {
        toggleStyle(FONT_WEIGHT_PROPERTY, "bold");
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
     * Otherwise, it is applied to the whole selection.
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
            double next = Math.clamp(current + delta, MIN_FONT_SIZE, MAX_FONT_SIZE);
            String updated = setProperty(style, "-fx-font-size", next + "px");
            // Explicitly pin weight to normal so the font renderer does not pick a heavier
            // optical weight for larger sizes (unless the user has explicitly applied bold).
            if (hasProperty(updated, FONT_WEIGHT_PROPERTY, "bold")) {
                updated = setProperty(updated, FONT_WEIGHT_PROPERTY, "normal");
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
