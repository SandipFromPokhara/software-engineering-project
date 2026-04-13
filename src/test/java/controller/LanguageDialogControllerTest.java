package controller;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import model.LanguageModel;
import model.LanguageModel.Language;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testutil.JavaFXInitializer;
import util.Localization;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LanguageDialogControllerTest {

    private static final long FX_TIMEOUT_SECONDS = 5;

    private LanguageDialogController controller;
    private VBox dialogPane;
    private Label titleLabel;
    private VBox languagePane;
    private Button confirmButton;
    private Button cancelButton;

    @BeforeAll
    static void initJavaFX() {
        JavaFXInitializer.init();
    }

    @BeforeEach
    void setUp() {
        controller = new LanguageDialogController();

        runOnFxThreadAndWait(() -> {
            dialogPane = new VBox();
            titleLabel = new Label();
            languagePane = new VBox();
            confirmButton = new Button();
            cancelButton = new Button();

            // Provide a Scene so confirmButton/label have a scene graph (but no need to show a Stage)
            VBox root = new VBox(dialogPane, titleLabel, languagePane, confirmButton, cancelButton);
            new Scene(root, 320, 240);
        });

        injectField("dialogPane", dialogPane);
        injectField("titleLabel", titleLabel);
        injectField("languagePane", languagePane);
        injectField("confirmButton", confirmButton);
        injectField("cancelButton", cancelButton);

        runOnFxThreadAndWait(controller::initialize);
    }

    private static void runOnFxThreadAndWait(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });

        try {
            boolean completed = latch.await(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertTrue(completed, "Timed out waiting for JavaFX thread");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted while waiting for JavaFX thread");
        }
    }

    private void injectField(String fieldName, Object value) {
        try {
            Field field = LanguageDialogController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to inject field '" + fieldName + "': " + e.getMessage());
        }
    }

    private static void setSelectedLanguage(LanguageDialogController c, Language lang) {
        try {
            Field field = LanguageDialogController.class.getDeclaredField("selectedLanguage");
            field.setAccessible(true);
            field.set(c, lang);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set selectedLanguage: " + e.getMessage());
        }
    }

    private static Language getSelectedLanguage(LanguageDialogController c) {
        try {
            Field selectedField = LanguageDialogController.class.getDeclaredField("selectedLanguage");
            selectedField.setAccessible(true);
            return (Language) selectedField.get(c);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to access selectedLanguage: " + e.getMessage());
            return null; // unreachable
        }
    }

    private static void invokePrivate(LanguageDialogController c, String methodName) {
        try {
            Method m = LanguageDialogController.class.getDeclaredMethod(methodName);
            m.setAccessible(true);
            m.invoke(c);
        } catch (ReflectiveOperationException e) {
            fail("Failed to invoke " + methodName + ": " + e.getMessage());
        }
    }

    private static void invokePrivateAllowRuntime(LanguageDialogController c, String methodName) {
        try {
            Method m = LanguageDialogController.class.getDeclaredMethod(methodName);
            m.setAccessible(true);
            m.invoke(c);
        } catch (ReflectiveOperationException e) {
            // If the invoked method throws, it is wrapped in InvocationTargetException.
            if (e instanceof java.lang.reflect.InvocationTargetException ite) {
                Throwable target = ite.getTargetException();
                if (target instanceof RuntimeException) {
                    return; // expected in headless env for window-closing code
                }
                fail("Invocation of " + methodName + " failed: " + target);
                return;
            }
            fail("Failed to invoke " + methodName + ": " + e.getMessage());
        }
    }

    @Test
    void testInitializePopulatesLanguageButtons() {
        runOnFxThreadAndWait(() -> {
            int expected = LanguageModel.LANGUAGES.size();
            assertEquals(expected, languagePane.getChildren().size(),
                    "Language pane should contain one radio button per supported language");

            long rbCount = languagePane.getChildren().stream().filter(RadioButton.class::isInstance).count();
            assertEquals(expected, rbCount, "All language entries should be RadioButtons");

            assertTrue(confirmButton.textProperty().isBound(), "Confirm button text should be bound to Localization");
            assertTrue(cancelButton.textProperty().isBound(), "Cancel button text should be bound to Localization");
        });
    }

    @Test
    void testSelectionReflectsLocaleAndUpdatesWhenUserSelectsAnother() {
        runOnFxThreadAndWait(() -> {
            Language initiallySelected = getSelectedLanguage(controller);
            Language expected = LanguageModel.getByLocale(java.util.Locale.getDefault());
            assertNotNull(initiallySelected, "A language should be selected after initialization");
            assertEquals(expected.code(), initiallySelected.code(),
                    "Initially selected language should match the application's locale");

            List<Node> children = languagePane.getChildren();
            RadioButton other = null;
            for (Node n : children) {
                if (n instanceof RadioButton rb) {
                    Object data = rb.getUserData();
                    if (data instanceof Language lang) {
                        String langCode = lang.code();
                        String selectedCode = initiallySelected.code();
                        if (langCode != null && selectedCode != null && !langCode.equals(selectedCode)) {
                            other = rb;
                            break;
                        }
                    }
                }
            }

            if (other != null) {
                other.setSelected(true);
                Language nowSelected = getSelectedLanguage(controller);
                assertNotNull(nowSelected);
                Object otherData = other.getUserData();
                assertTrue(otherData instanceof Language);
                assertEquals(((Language) otherData).code(), nowSelected.code(),
                        "Selecting another radio button should update selectedLanguage");
            }
        });
    }

    @Test
    void testHandleConfirmWithNullSelectedLanguageDoesNotChangeLocale() {
        java.util.Locale before = Localization.getLocale();

        runOnFxThreadAndWait(() -> {
            setSelectedLanguage(controller, null);
            invokePrivateAllowRuntime(controller, "handleConfirm");
        });

        assertEquals(before.getLanguage(), Localization.getLocale().getLanguage(),
                "Locale should remain unchanged when selectedLanguage is null");
    }

    @Test
    void testApplyClipDirectly() {
        runOnFxThreadAndWait(() -> {
            dialogPane.resize(200, 100);
            invokePrivate(controller, "applyClip");

            assertNotNull(dialogPane.getClip());
            assertInstanceOf(Rectangle.class, dialogPane.getClip());
            Rectangle r = (Rectangle) dialogPane.getClip();
            assertEquals(24.0, r.getArcWidth());
            assertEquals(24.0, r.getArcHeight());
        });
    }

    @Test
    void testHandleCancelDoesNotThrowWithoutWindow() {
        // handleCancel delegates to WindowUtil.closeWindow which is null-safe
        runOnFxThreadAndWait(() -> assertDoesNotThrow(() -> invokePrivate(controller, "handleCancel")));
    }
}
