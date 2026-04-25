package util;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlertUtilTest {

    @Test
    void showAlertWithoutOwnerCreatesAlertAndShows() {
        /*Keep the MockedConstruction reference ("mocked") so the try-with-resources
        lifecycle is applied (it activates the constructor interception and closes it
        when the block exits). The variable also allows inspecting created mocks via
        mocked.constructed() when needed.*/
        try (MockedConstruction<Alert> mocked =
                     mockConstruction(Alert.class, (alert, ctx) -> {
                         // Provide a DialogPane and a modifiable button list to avoid NPEs when AlertUtil manipulates the dialog pane/button types.
                          DialogPane pane = mock(DialogPane.class);
                          when(alert.getDialogPane()).thenReturn(pane);
                          @SuppressWarnings("unchecked")
                          ObservableList<ButtonType> btnList = mock(ObservableList.class);
                          when(pane.getButtonTypes()).thenReturn(btnList);
                          when(alert.getButtonTypes()).thenReturn(btnList);
                          when(alert.showAndWait()).thenReturn(Optional.empty());
                     })) {

            Window owner = mock(Window.class);

            AlertUtil.showAlert(owner,
                    Alert.AlertType.INFORMATION,
                    "Title",
                    "Content");

            Alert alert = mocked.constructed().get(0);

            verify(alert).setTitle("Title");
            verify(alert).setContentText("Content");
            verify(alert).setHeaderText(null);
            verify(alert).initOwner(owner);
            verify(alert).showAndWait();
        }
    }

    @Test
    void showAlertWithNullOwnerDoesNotInitOwner() {
        // Keep the MockedConstruction reference ("mocked") so the try-with-resources
        // lifecycle is applied and to allow inspection via mocked.constructed().
        try (MockedConstruction<Alert> mocked =
                     mockConstruction(Alert.class, (alert, ctx) -> {
                          DialogPane pane = mock(DialogPane.class);
                          when(alert.getDialogPane()).thenReturn(pane);
                          @SuppressWarnings("unchecked")
                          ObservableList<ButtonType> btnList = mock(ObservableList.class);
                          when(pane.getButtonTypes()).thenReturn(btnList);
                          when(alert.getButtonTypes()).thenReturn(btnList);
                          when(alert.showAndWait()).thenReturn(Optional.empty());
                      })) {

            AlertUtil.showAlert(null, Alert.AlertType.ERROR, "Error", "Something went wrong");

            Alert alert = mocked.constructed().get(0);

            verify(alert, never()).initOwner(any());
            verify(alert).setTitle("Error");
        }
    }

    @Test
    void showInfoDelegatesToShowAlert() {
        // Keep the MockedConstruction reference ("mocked") so the try-with-resources
        // lifecycle is applied and to allow inspection via mocked.constructed().
        try (MockedConstruction<Alert> mocked =
                     mockConstruction(Alert.class, (alert, ctx) -> {
                          DialogPane pane = mock(DialogPane.class);
                          when(alert.getDialogPane()).thenReturn(pane);
                          @SuppressWarnings("unchecked")
                          ObservableList<ButtonType> btnList = mock(ObservableList.class);
                          when(pane.getButtonTypes()).thenReturn(btnList);
                          when(alert.getButtonTypes()).thenReturn(btnList);
                          when(alert.showAndWait()).thenReturn(Optional.empty());
                     })) {

            AlertUtil.showInfo(null, "Info message");

            Alert alert = mocked.constructed().get(0);

            verify(alert).setContentText("Info message");
        }
    }

    @Test
    void showWarningDelegatesToShowAlert() {
        // Keep the MockedConstruction reference ("mocked") so the try-with-resources
        // lifecycle is applied and to allow inspection via mocked.constructed().
        try (MockedConstruction<Alert> mocked =
                     mockConstruction(Alert.class, (alert, ctx) -> {
                          DialogPane pane = mock(DialogPane.class);
                          when(alert.getDialogPane()).thenReturn(pane);
                          @SuppressWarnings("unchecked")
                          ObservableList<ButtonType> btnList = mock(ObservableList.class);
                          when(pane.getButtonTypes()).thenReturn(btnList);
                          when(alert.getButtonTypes()).thenReturn(btnList);
                          when(alert.showAndWait()).thenReturn(Optional.empty());
                      })) {

            AlertUtil.showWarning(null, "Warning message");

            Alert alert = mocked.constructed().get(0);

            verify(alert).setContentText("Warning message");
        }
    }

    @Test
    void showErrorDelegatesToShowAlert() {
        // Keep the MockedConstruction reference ("mocked") so the try-with-resources
        // lifecycle is applied and to allow inspection via mocked.constructed().
        try (MockedConstruction<Alert> mocked =
                     mockConstruction(Alert.class, (alert, ctx) -> {
                          DialogPane pane = mock(DialogPane.class);
                          when(alert.getDialogPane()).thenReturn(pane);
                          @SuppressWarnings("unchecked")
                          ObservableList<ButtonType> btnList = mock(ObservableList.class);
                          when(pane.getButtonTypes()).thenReturn(btnList);
                          when(alert.getButtonTypes()).thenReturn(btnList);
                          when(alert.showAndWait()).thenReturn(Optional.empty());
                      })) {

            AlertUtil.showError(null, "Error message");

            Alert alert = mocked.constructed().get(0);

            verify(alert).setContentText("Error message");
        }
    }

    @Test
    void showConfirmationReturnsTrueWhenOkSelected() {
        // Keep the MockedConstruction reference ("mocked") so the try-with-resources
        // lifecycle is applied and to allow inspection via mocked.constructed().
        try (MockedConstruction<Alert> mocked =
                     mockConstruction(Alert.class, (alert, ctx) -> {
                          DialogPane pane = mock(DialogPane.class);
                          when(alert.getDialogPane()).thenReturn(pane);

                          @SuppressWarnings("unchecked")
                          ObservableList<ButtonType> list = mock(ObservableList.class);

                          when(pane.getButtonTypes()).thenReturn(list);
                          when(alert.getButtonTypes()).thenReturn(list);

                          // Capture the ok button that AlertUtil places into the button types so we can
                          // return the exact same instance from showAndWait() (comparison is by reference).
                          AtomicReference<ButtonType> captured = new AtomicReference<>();
                          doAnswer(inv -> {
                              Object[] args = inv.getArguments();
                              if (args != null && args.length > 0 && args[0] instanceof ButtonType) {
                                  captured.set((ButtonType) args[0]);
                              }
                              return null;
                          }).when(list).setAll(any(), any());

                          when(alert.showAndWait()).thenAnswer(inv -> Optional.ofNullable(captured.get()));
                      })) {

            boolean result = AlertUtil.showConfirmation(null, "Confirm", "Are you sure?"
            );

            assertTrue(result);
        }
    }

    @Test
    void showConfirmationReturnsFalseWhenNotOk() {
        ButtonType other = mock(ButtonType.class);

        // Keep the MockedConstruction reference ("mocked") so the try-with-resources
        // lifecycle is applied and to allow inspection via mocked.constructed().
        try (MockedConstruction<Alert> mocked =
                     mockConstruction(Alert.class, (alert, ctx) -> {
                          DialogPane pane = mock(DialogPane.class);
                          when(alert.getDialogPane()).thenReturn(pane);

                         @SuppressWarnings("unchecked")
                         ObservableList<ButtonType> list = mock(ObservableList.class);
                         when(pane.getButtonTypes()).thenReturn(list);
                         when(alert.getButtonTypes()).thenReturn(list);
                         when(alert.showAndWait()).thenReturn(Optional.of(other));
                     })) {

            boolean result = AlertUtil.showConfirmation(null, "Confirm", "Are you sure?"
            );

            assertFalse(result);
        }
    }

    @Test
    void addStandardButtonsConfiguresDialog() {
        Dialog<?> dialog = mock(Dialog.class, RETURNS_DEEP_STUBS);

        AlertUtil.addStandardButtons(dialog);

        verify(dialog.getDialogPane().getButtonTypes())
                .setAll(any(), any());
    }

    @Test
    void setOkOnlyUsedInternallyViaShowAlert() {
        try (MockedConstruction<Alert> mocked =
                     mockConstruction(Alert.class, (alert, ctx) -> {
                          DialogPane pane = mock(DialogPane.class);
                          when(alert.getDialogPane()).thenReturn(pane);
                          @SuppressWarnings("unchecked")
                          ObservableList<ButtonType> btnList = mock(ObservableList.class);
                          when(pane.getButtonTypes()).thenReturn(btnList);
                          when(alert.getButtonTypes()).thenReturn(btnList);
                          when(alert.showAndWait()).thenReturn(Optional.empty());
                     })) {

            AlertUtil.showInfo(null, "Test");

            Alert alert = mocked.constructed().get(0);

            // verifies OK-only behavior indirectly
            verify(alert.getButtonTypes()).setAll(any(ButtonType.class));
        }
    }
}