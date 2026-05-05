package controller;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import model.LanguageModel;
import util.Localization;
import util.NavigationUtil;
import util.TooltipUtil;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntryController {

    static final Logger logger = Logger.getLogger(EntryController.class.getName());

    @FXML
    private Label hello;
    @FXML
    private Label welcome;
    @FXML
    private Label privacyLabel;
    @FXML
    private Label languageDisplay;

    @FXML
    private Button guestButton;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink faqLink;

    @FXML
    private HBox languageSwitcher;

    @FXML
    public void initialize() {
        hello.textProperty().bind(Localization.bind("entry.hello"));
        welcome.textProperty().bind(Localization.bind("entry.welcome"));
        loginButton.textProperty().bind(Localization.bind("entry.login"));
        registerButton.textProperty().bind(Localization.bind("entry.register"));
        guestButton.textProperty().bind(Localization.bind("entry.guest"));
        faqLink.textProperty().bind(Localization.bind("entry.faq"));
        privacyLabel.textProperty().bind(Localization.bind("entry.privacy"));

        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(Localization.bind("tooltip.lang_info"));

        Tooltip.install(languageSwitcher, tooltip);
        TooltipUtil.setTooltipDelay(tooltip);

        languageDisplay.textProperty().bind(
                Bindings.createStringBinding(
                        () -> LanguageModel
                                .getByLocale(Localization.getLocale())
                                .nativeName(),
                        Localization.localeProperty()
                )
        );

        languageSwitcher.setOnMouseClicked(e -> handleLanguageSelect());
        languageSwitcher.setPickOnBounds(true);

        languageSwitcher.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleLanguageSelect();
            }
        });

        languageSwitcher.setFocusTraversable(true);
    }

    @FXML
    private void onLogin() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        NavigationUtil.replaceScene(stage, "/FXML/login_view.fxml", "login.window_title", false);
    }

    @FXML
    private void onRegister() {
        Stage stage = (Stage) registerButton.getScene().getWindow();
        NavigationUtil.replaceScene(stage, "/FXML/signup.fxml", "register.window_title", false);
    }

    @FXML
    private void onContinueAsGuest() {
        Stage stage = (Stage) guestButton.getScene().getWindow();
        NavigationUtil.replaceScene(stage, "/FXML/guest_dashboard.fxml", "guest.window_title", true);
    }

    @FXML
    private void handleOpenFAQ() {
        NavigationUtil.<FAQController>openWindow(
                null, "/FXML/faq_view.fxml",
                "faq.window_title",
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

            Stage owner = (Stage) languageSwitcher.getScene().getWindow();
            dialog.setOnShown(e -> {
                dialog.setX(owner.getX() + (owner.getWidth()  - dialog.getWidth())  / 2);
                dialog.setY(owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2);
            });

            dialog.showAndWait();

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to select language", e);
        }
    }
}
