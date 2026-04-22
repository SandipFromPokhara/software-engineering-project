package util;

import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import security.MessageType;
import testutil.JavaFxTestExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JavaFxTestExtension.class)
class ShowMessageUtilTest {

    private Label label;

    @BeforeEach
    void setUp() {
        label = new Label();
    }

    @Test
    void showMessageSetsTextAndVisibility() {
        ShowMessageUtil.showMessage(label, "Hello", MessageType.INFO);

        assertEquals("Hello", label.getText());
        assertTrue(label.isVisible());
        assertTrue(label.isManaged());
    }

    @Test
    void showMessageKeyUsesLocalizedText() {
        ShowMessageUtil.showMessageKey(label, "test.key", MessageType.SUCCESS);

        assertNotNull(label.getText());
        assertTrue(label.isVisible());
    }

    @Test
    void hideMessageClearsAndHides() {
        label.setText("Something");
        label.setVisible(true);
        label.setManaged(true);

        ShowMessageUtil.hideMessage(label);

        assertEquals("", label.getText());
        assertFalse(label.isVisible());
        assertFalse(label.isManaged());
    }

    @Test
    void applyStylesSuccessErrorInfo() {
        ShowMessageUtil.showMessage(label, "msg", MessageType.SUCCESS);
        assertTrue(label.getStyle().contains("2e7d32"));

        ShowMessageUtil.showMessage(label, "msg", MessageType.ERROR);
        assertTrue(label.getStyle().contains("d32f2f"));

        ShowMessageUtil.showMessage(label, "msg", MessageType.INFO);
        assertTrue(label.getStyle().contains("1976d2"));
    }
}