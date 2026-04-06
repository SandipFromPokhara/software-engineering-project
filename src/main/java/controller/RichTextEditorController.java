package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.IndexRange;
import util.bulletList.BulletListStrategy;
import util.bulletList.NumberedListStrategy;
import util.bulletList.TextFormattingUtil;

public class RichTextEditorController {

    @FXML
    private TextArea contentArea;  // later: InlineCssTextArea

    // toolbar buttons if you ever need direct access
    @FXML private Button boldButton;
    @FXML private Button italicButton;
    @FXML private Button underlineButton;
    @FXML private Button bulletListButton;
    @FXML private Button numberedListButton;
    @FXML private Button headingUpButton;
    @FXML private Button headingDownButton;

    // ---------- Public API for parent controllers ----------

    public String getText() {
        return contentArea.getText();
    }

    public void setText(String text) {
        contentArea.setText(text);
    }

    public TextArea getNode() {
        return contentArea;
    }

    // New helper to expose the underlying TextArea for bindings/utilities
    public TextArea getTextArea() {
        return contentArea;
    }

    // ---------- Toolbar handler ----------

    @FXML
    private void handleToolbarClick(ActionEvent event) {
        if (!(event.getSource() instanceof Button button)) {
            return;
        }
        String id = button.getId();
        switch (id) {
            case "boldButton" -> applyBold();
            case "italicButton" -> applyItalic();
            case "underlineButton" -> applyUnderline();
            case "bulletListButton" -> toggleBullet();
            case "numberedListButton" -> toggleNumbered();
            case "headingUpButton" -> headingUp();
            case "headingDownButton" -> headingDown();
        }
    }

    // ---------- Formatting helpers (for now, simple text-based) ----------

    private void applyBold() {
        // First simple version: wrap selection in ** **
        IndexRange sel = contentArea.getSelection();
        if (sel.getLength() == 0) return;

        String text = contentArea.getText();
        String selected = text.substring(sel.getStart(), sel.getEnd());
        String replaced = "**" + selected + "**";
        contentArea.replaceText(sel.getStart(), sel.getEnd(), replaced);
    }

    private void applyItalic() {
        // Wrap selection with * *
        IndexRange sel = contentArea.getSelection();
        if (sel.getLength() == 0) return;
        String text = contentArea.getText();
        String selected = text.substring(sel.getStart(), sel.getEnd());
        String replaced = "*" + selected + "*";
        contentArea.replaceText(sel.getStart(), sel.getEnd(), replaced);
    }

    private void applyUnderline() {
        // Maybe just wrap with __selection__
        IndexRange sel = contentArea.getSelection();
        if (sel.getLength() == 0) return;
        String text = contentArea.getText();
        String selected = text.substring(sel.getStart(), sel.getEnd());
        String replaced = "__" + selected + "__";
        contentArea.replaceText(sel.getStart(), sel.getEnd(), replaced);
    }

    private void toggleBullet() {
        TextFormattingUtil.toggleList(contentArea, bulletListButton, new BulletListStrategy());
    }

    private void toggleNumbered() {
        TextFormattingUtil.toggleList(contentArea, numberedListButton, new NumberedListStrategy());
    }

    private void headingUp() {
        TextFormattingUtil.increaseFontSize(contentArea);
    }

    private void headingDown() {
        TextFormattingUtil.decreaseFontSize(contentArea);
    }
}
