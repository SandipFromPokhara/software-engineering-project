package util;

import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import org.fxmisc.richtext.InlineCssTextArea;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

public class UndoRedoManager {

    private final Stack<TextCommand> undoStack = new Stack<>();
    private final Stack<TextCommand> redoStack = new Stack<>();
    private boolean isUndoRedoAction = false;

    private MenuItem undoMenuItem;
    private MenuItem redoMenuItem;

    private final Map<String, TextInputControl> fieldMap = new HashMap<>();
    private final Map<String, Consumer<String>> richFieldSetters = new HashMap<>();

    // Initialize the undo/redo manager with menu items
    public void initialize(MenuItem undoMenuItem, MenuItem redoMenuItem) {
        this.undoMenuItem = undoMenuItem;
        this.redoMenuItem = redoMenuItem;

        if (undoMenuItem != null) {
            undoMenuItem.setDisable(true);
        }
        if (redoMenuItem != null) {
            redoMenuItem.setDisable(true);
        }
    }

    // Register a text field or text area for undo/redo tracking
    public void registerField(String fieldName, TextInputControl field) {
        fieldMap.put(fieldName, field);

        // Add listener to track changes
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUndoRedoAction && oldVal != null && !oldVal.equals(newVal)) {
                recordChange(fieldName, oldVal, newVal);
            }
        });
    }

    // Overload for InlineCssTextArea (which is not a TextInputControl)
    public void registerField(String fieldName, InlineCssTextArea field) {
        richFieldSetters.put(fieldName, text -> {
            String safeText = text != null ? text : "";
            field.replaceText(0, field.getLength(), safeText);
            if (!safeText.isEmpty()) {
                field.setStyle(0, safeText.length(), "");
            }
        });

        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUndoRedoAction && oldVal != null && !oldVal.equals(newVal)) {
                recordChange(fieldName, oldVal, newVal);
            }
        });
    }

    // Record a text change for undo/redo
    private void recordChange(String fieldName, String oldValue, String newValue) {
        if (oldValue == null) oldValue = "";
        if (newValue == null) newValue = "";

        if (!oldValue.equals(newValue)) {
            TextCommand command = new TextCommand(fieldName, oldValue, newValue);
            undoStack.push(command);
            redoStack.clear(); // Clear redo stack on new action

            updateMenuItems();
        }
    }

    // Undo the last action
    public void undo() {
        if (undoStack.isEmpty()) {
            return;
        }

        isUndoRedoAction = true;

        TextCommand command = undoStack.pop();
        redoStack.push(command);

        // Restore old value
        TextInputControl field = fieldMap.get(command.getFieldName());
        if (field != null) {
            field.setText(command.getOldValue());
        } else {
            Consumer<String> setter = richFieldSetters.get(command.getFieldName());
            if (setter != null) setter.accept(command.getOldValue());
        }

        updateMenuItems();
        isUndoRedoAction = false;
    }

    // Redo the last undone action
    public void redo() {
        if (redoStack.isEmpty()) {
            return;
        }

        isUndoRedoAction = true;

        TextCommand command = redoStack.pop();
        undoStack.push(command);

        // Restore new value
        TextInputControl field = fieldMap.get(command.getFieldName());
        if (field != null) {
            field.setText(command.getNewValue());
        } else {
            Consumer<String> setter = richFieldSetters.get(command.getFieldName());
            if (setter != null) setter.accept(command.getNewValue());
        }

        updateMenuItems();
        isUndoRedoAction = false;
    }

    // Clear all undo/redo history
    public void clear() {
        undoStack.clear();
        redoStack.clear();
        updateMenuItems();
    }

    // Update the enabled/disabled state of menu items
    private void updateMenuItems() {
        if (undoMenuItem != null) {
            undoMenuItem.setDisable(undoStack.isEmpty());
        }
        if (redoMenuItem != null) {
            redoMenuItem.setDisable(redoStack.isEmpty());
        }
    }

    // Check if there are actions that can be undone
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    // Check if there are actions that can be redone
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    // Get the number of actions in the undo stack
    public int getUndoStackSize() {
        return undoStack.size();
    }

    // Get the number of actions in the redo stack
    public int getRedoStackSize() {
        return redoStack.size();
    }
}
