package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import util.Localization;
import util.NavigationUtil;

import java.io.IOException;

public class EntryController {

    @FXML private Label hello;
    @FXML private Label welcome;
    @FXML private Button guestButton;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Hyperlink faqLink;
    @FXML private Label privacyLabel;
    @FXML private Button languageButton; // world icon button

    @FXML
    private void onLogin() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        NavigationUtil.replaceScene(stage, "/FXML/login_view.fxml",
                Localization.get("login.window_title"), false);
    }

    @FXML
    private void onRegister() {
        Stage stage = (Stage) registerButton.getScene().getWindow();
        NavigationUtil.replaceScene(stage, "/FXML/signup.fxml",
                Localization.get("register.window_title"), false);
    }

    @FXML
    private void onContinueAsGuest() {
        Stage stage = (Stage) guestButton.getScene().getWindow();
        NavigationUtil.replaceScene(stage, "/FXML/guestDashboard.fxml",
                Localization.get("guest.window_title"), true);
    }

    @FXML
    private void handleOpenFAQ() {
        NavigationUtil.<FAQController>openWindow(
                null, "/FXML/faq_view.fxml",
                Localization.get("faq.window_title"),
                true, true,
                controller -> controller.initFaq(true));
    }

    @FXML
    private void handleLanguageSelect() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/FXML/language_dialog.fxml"));
            Parent root = loader.load();

            // Add drop shadow via wrapper in code instead of FXML
            javafx.scene.layout.StackPane wrapper = new javafx.scene.layout.StackPane(root);
            wrapper.setStyle("-fx-background-color: transparent; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 12, 0, 0, 4);");
            wrapper.setPadding(new javafx.geometry.Insets(8));

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(wrapper);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            dialog.setScene(scene);
            dialog.setResizable(false);

            Stage owner = (Stage) languageButton.getScene().getWindow();
            dialog.setOnShown(e -> {
                dialog.setX(owner.getX() + (owner.getWidth()  - dialog.getWidth())  / 2);
                dialog.setY(owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2);
            });

            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        hello.textProperty().bind(Localization.bind("entry.hello"));
        welcome.textProperty().bind(Localization.bind("entry.welcome"));
        loginButton.textProperty().bind(Localization.bind("entry.login"));
        registerButton.textProperty().bind(Localization.bind("entry.register"));
        guestButton.textProperty().bind(Localization.bind("entry.guest"));
        faqLink.textProperty().bind(Localization.bind("entry.faq"));
        privacyLabel.textProperty().bind(Localization.bind("entry.privacy"));
    }
}