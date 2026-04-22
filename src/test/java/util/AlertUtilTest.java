package util;

import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlertUtilTest {

    @Test
    void showAlertWithoutOwnerSetsValuesAndShows() {
        try (MockedConstruction<Alert> mocked = mockConstruction(Alert.class, (alert, context) ->
                             when(alert.showAndWait()).thenReturn(Optional.empty()))) {
            Window owner = mock(Window.class);

            AlertUtil.showAlert(owner, Alert.AlertType.INFORMATION, "Title", "Content");

            Alert alert = mocked.constructed().getFirst();

            verify(alert).setTitle("Title");
            verify(alert).setContentText("Content");
            verify(alert).setHeaderText(null);
            verify(alert).initOwner(owner);
            verify(alert).showAndWait();
        }
    }

    @Test
    void showAlertWithNullOwnerDoesNotInitOwner() {
        try (MockedConstruction<Alert> mocked =
                     mockConstruction(Alert.class, (alert, context) ->
                             when(alert.showAndWait()).thenReturn(Optional.empty()))) {

            AlertUtil.showAlert(null, Alert.AlertType.ERROR, "Error", "Something went wrong");

            Alert alert = mocked.constructed().get(0);

            verify(alert, never()).initOwner(any());
            verify(alert).setTitle("Error");
        }
    }

    @Test
    void showInfoCallsShowAlert() {
        try (MockedConstruction<Alert> mocked = mockConstruction(Alert.class, (alert, context) ->
                         when(alert.showAndWait()).thenReturn(Optional.empty()))) {

            AlertUtil.showInfo(null, "Info message");

            Alert alert = mocked.constructed().get(0);

            verify(alert).setContentText("Info message");
        }
    }

    @Test
    void showWarningCallsShowAlert() {
        try (MockedConstruction<Alert> mocked = mockConstruction(Alert.class, (alert, context) ->
                         when(alert.showAndWait()).thenReturn(Optional.empty()))) {

            AlertUtil.showWarning(null, "Warning message");

            Alert alert = mocked.constructed().get(0);

            verify(alert).setContentText("Warning message");
        }
    }

    @Test
    void showErrorCallsShowAlert() {
        try (MockedConstruction<Alert> mocked = mockConstruction(Alert.class, (alert, context) ->
                         when(alert.showAndWait()).thenReturn(Optional.empty()))) {

            AlertUtil.showError(null, "Error message");

            Alert alert = mocked.constructed().get(0);

            verify(alert).setContentText("Error message");
        }
    }

    @Test
    void showConfirmationReturnsFalseWhenCancelled() {

        ButtonType cancel = mock(ButtonType.class);

        try (MockedConstruction<Alert> mocked = mockConstruction(Alert.class, (alert, context) -> {
            when(alert.getButtonTypes()).thenReturn(FXCollections.observableArrayList());

            when(alert.showAndWait()).thenReturn(Optional.of(cancel));})) {
            boolean result = AlertUtil.showConfirmation(null, "Confirm", "Are you sure?"
            );

            assertFalse(result);
        }
    }

    @Test
    void addStandardButtonsSetsOkAndCancel() {

        Dialog<ButtonType> dialog = mock(Dialog.class, RETURNS_DEEP_STUBS);

        AlertUtil.addStandardButtons(dialog);

        verify(dialog.getDialogPane().getButtonTypes(), atLeastOnce()).setAll(any(), any());
    }
}