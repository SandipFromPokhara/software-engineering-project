package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import util.Localization;
import util.NavigationUtil;

import java.util.Locale;

public class EntryController {
    @FXML
    private Label hello;

    @FXML
    private Label welcome;
    @FXML
    private Button guestButton;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink faqLink;

    @FXML
    private ComboBox<Locale> languageDropdown; //added

    @FXML
    private void onLogin() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        NavigationUtil.replaceScene(
                stage,
                "/FXML/login_view.fxml",
                Localization.get("login.window_title"),
                false);
    }

    @FXML
    private void onRegister() {
        Stage stage = (Stage) registerButton.getScene().getWindow();
        NavigationUtil.replaceScene(
                stage,
                "/FXML/signup.fxml",
                Localization.get("register.window_title"),false);
    }

    @FXML
    private void onContinueAsGuest() {
        Stage stage = (Stage) guestButton.getScene().getWindow();
        NavigationUtil.replaceScene(
                stage,
                "/FXML/guestDashboard.fxml",
                Localization.get("guest.window_title"), true);
    }

    @FXML
    private void handleOpenFAQ() {
        NavigationUtil.<FAQController>openWindow(
                null,
                "/FXML/faq_view.fxml",
                Localization.get("faq.window_title"),
                true, true,
                controller -> {
                    controller.initFaq(true);
                });
    }

    // Added new initialize method for localization entry page
    @FXML
    public void initialize() {
        hello.textProperty().bind(Localization.bind("entry.hello"));
        welcome.textProperty().bind(Localization.bind("entry.welcome"));
        loginButton.textProperty().bind(Localization.bind("entry.login"));
        registerButton.textProperty().bind(Localization.bind("entry.register"));
        guestButton.textProperty().bind(Localization.bind("entry.guest"));
        faqLink.textProperty().bind(Localization.bind("entry.faq"));

        languageDropdown.getItems().addAll(
                Locale.ENGLISH,
                new Locale("fi"),
                new Locale("np"),
                new Locale("si"),
                new Locale("my")
        );

        languageDropdown.setValue(Locale.ENGLISH);

        languageDropdown.setOnAction(e ->
                Localization.setLocale(languageDropdown.getValue())
        );
    }
}