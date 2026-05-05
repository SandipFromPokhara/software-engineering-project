package services;

import entity.entities.NotebookEntity;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.function.Consumer;

import static org.mockito.Mockito.*;

import session.NotebookSession;
import util.AlertUtil;
import util.NavigationUtil;

class DashboardNavigationServiceTest {

    @Test
    void testOpenManageNotebooks_withLastNotebook() {
        Stage stage = mock(Stage.class);
        NotebookEntity activeNotebook = mock(NotebookEntity.class);
        NotebookEntity lastNotebook = mock(NotebookEntity.class);

        Consumer<NotebookEntity> consumer = mock(Consumer.class);

        try (MockedStatic<NavigationUtil> navMock = mockStatic(NavigationUtil.class);
             MockedStatic<NotebookSession> sessionMock = mockStatic(NotebookSession.class)) {

            // Mock session behavior
            sessionMock.when(NotebookSession::getLastCreatedNotebook).thenReturn(lastNotebook);

            DashboardNavigationService.openManageNotebooks(stage, activeNotebook, consumer);

            // Verify window opened
            navMock.verify(() -> NavigationUtil.openWindow(
                    eq(stage),
                    eq("/FXML/manage_notebooks.fxml"),
                    eq("notebook.manage_label"),
                    eq(false),
                    eq(true),
                    any()
            ));

            // Verify callback triggered
            verify(consumer).accept(lastNotebook);

            // Verify session cleared
            sessionMock.verify(NotebookSession::clear);
        }
    }

    @Test
    void testOpenManageNotebooks_withoutLastNotebook() {
        Stage stage = mock(Stage.class);
        NotebookEntity activeNotebook = mock(NotebookEntity.class);
        Consumer<NotebookEntity> consumer = mock(Consumer.class);

        try (MockedStatic<NavigationUtil> navMock = mockStatic(NavigationUtil.class);
             MockedStatic<NotebookSession> sessionMock = mockStatic(NotebookSession.class)) {

            sessionMock.when(NotebookSession::getLastCreatedNotebook).thenReturn(null);

            DashboardNavigationService.openManageNotebooks(stage, activeNotebook, consumer);

            // Verify no callback triggered
            verify(consumer, never()).accept(any());

            // Verify clear not called
            sessionMock.verify(NotebookSession::clear, never());

            navMock.verify(() -> NavigationUtil.openWindow(
                    eq(stage),
                    eq("/FXML/manage_notebooks.fxml"),
                    eq("notebook.manage_label"),
                    eq(false),
                    eq(true),
                    any()
            ));
        }
    }

    @Test
    void testLogout_confirmed_withCleanup() {
        Window window = mock(Stage.class); // Stage extends Window
        Runnable cleanup = mock(Runnable.class);

        try (MockedStatic<AlertUtil> alertMock = mockStatic(AlertUtil.class);
             MockedStatic<NavigationUtil> navMock = mockStatic(NavigationUtil.class)) {

            alertMock.when(() -> AlertUtil.showConfirmation(any(), any(), any()))
                    .thenReturn(true);

            DashboardNavigationService.logout(window, cleanup);

            // Cleanup should run
            verify(cleanup).run();

            // Navigation should happen
            navMock.verify(() -> NavigationUtil.replaceScene(
                    any(Stage.class),
                    eq("/FXML/entry.fxml"),
                    eq("entry.window_title"),
                    eq(false)
            ));
        }
    }

    @Test
    void testLogout_notConfirmed() {
        Window window = mock(Window.class);
        Runnable cleanup = mock(Runnable.class);

        try (MockedStatic<AlertUtil> alertMock = mockStatic(AlertUtil.class);
             MockedStatic<NavigationUtil> navMock = mockStatic(NavigationUtil.class)) {

            alertMock.when(() -> AlertUtil.showConfirmation(any(), any(), any()))
                    .thenReturn(false);

            DashboardNavigationService.logout(window, cleanup);

            // Cleanup should NOT run
            verify(cleanup, never()).run();

            // Navigation should NOT happen
            navMock.verifyNoInteractions();
        }
    }

    @Test
    void testLogout_confirmed_withoutCleanup() {
        Window window = mock(Stage.class);

        try (MockedStatic<AlertUtil> alertMock = mockStatic(AlertUtil.class);
             MockedStatic<NavigationUtil> navMock = mockStatic(NavigationUtil.class)) {

            alertMock.when(() -> AlertUtil.showConfirmation(any(), any(), any()))
                    .thenReturn(true);

            DashboardNavigationService.logout(window, null);

            // Navigation should still happen
            navMock.verify(() -> NavigationUtil.replaceScene(
                    any(Stage.class),
                    eq("/FXML/entry.fxml"),
                    eq("entry.window_title"),
                    eq(false)
            ));
        }
    }
}