package controller;

import dao.user.JpaUserDao;
import entity.entities.UserEntity;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import security.IPasswordHasher;
import session.UserSession;
import testutil.JavaFXInitializer;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteUserControllerTest {

    private DeleteUserController controller;

    @Mock private JpaUserDao userDao;
    @Mock private IPasswordHasher passwordHasher;
    @Mock private UserEntity user;

    private PasswordField passwordField;
    private Label messageLabel;
    private Button cancelButton;
    private Button deleteButton;

    // Initialize JavaFX
    @BeforeAll
    static void initJavaFX() {
        JavaFXInitializer.init();
    }

    @BeforeEach
    void setUp() {
        controller = new DeleteUserController();

        passwordField = new PasswordField();
        messageLabel = new Label();
        cancelButton = new Button();
        deleteButton = new Button();

        inject("passwordField", passwordField);
        inject("messageLabel", messageLabel);
        inject("cancelButton", cancelButton);
        inject("deleteButton", deleteButton);

        inject("userDao", userDao);
        inject("passwordHasher", passwordHasher);

        controller.initialize();
    }

    // ---------------- TESTS ----------------

    @Test
    void handleDelete_shouldFail_whenNoUserSession() {
        UserSession.getUserInstance().setUser(null);

        passwordField.setText("pass");

        invoke("handleDelete");

        Assertions.assertNotNull(messageLabel.getText());
    }

    @Test
    void handleDelete_shouldFail_whenPasswordEmpty() {
        UserSession.getUserInstance().setUser(user);
        passwordField.setText("");

        invoke("handleDelete");

        Assertions.assertNotNull(messageLabel.getText());
    }

    @Test
    void handleDelete_shouldFail_whenPasswordIncorrect() {
        when(user.getPasswordHash()).thenReturn("hash");
        when(passwordHasher.verify("wrong", "hash")).thenReturn(false);

        UserSession.getUserInstance().setUser(user);
        passwordField.setText("wrong");

        invoke("handleDelete");

        Assertions.assertNotNull(messageLabel.getText());
        verify(userDao, never()).delete(any());
    }

    @Test
    void handleDelete_shouldDeleteUser_whenPasswordCorrect() {
        when(user.getPasswordHash()).thenReturn("hash");
        when(passwordHasher.verify("correct", "hash")).thenReturn(true);

        UserSession.getUserInstance().setUser(user);
        passwordField.setText("correct");

        invoke("handleDelete");

        verify(userDao).delete(user);
        Assertions.assertNull(UserSession.getUserInstance().getUser());
    }

    @Test
    void handleCancel_shouldNotCrash() {
        Assertions.assertDoesNotThrow(() -> invoke("handleCancel"));
    }

    // ---------------- HELPERS ----------------

    private void inject(String fieldName, Object value) {
        try {
            var field = DeleteUserController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invoke(String methodName) {
        try {
            var method = DeleteUserController.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}