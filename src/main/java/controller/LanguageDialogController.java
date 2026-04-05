package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import model.LanguageModel;
import model.LanguageModel.Language;
import util.Localization;
import util.WindowUtil;

public class LanguageDialogController {

    @FXML private VBox dialogPane;
    @FXML private Label titleLabel;
    @FXML private VBox languagePane;
    @FXML private Button confirmButton, cancelButton;

    private ToggleGroup toggleGroup;
    private Language selectedLanguage;

    @FXML
    public void initialize() {

        titleLabel.textProperty().bind(Localization.bind("entry.lang_choose"));
        confirmButton.textProperty().bind(Localization.bind("button.select"));
        cancelButton.textProperty().bind(Localization.bind("button.cancel"));

        // Apply clip once layout is complete
        dialogPane.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.setOnShown(e -> applyClip());
                    }
                });
            }
        });

        toggleGroup = new ToggleGroup();
        Language current = LanguageModel.getByLocale(Localization.getLocale());

        for (Language lang : LanguageModel.LANGUAGES.values()) {
            RadioButton rb = new RadioButton(
                    lang.fullName() + "  (" + lang.nativeName() + ")"
            );
            rb.setToggleGroup(toggleGroup);
            rb.setUserData(lang);
            rb.setPrefWidth(248.0);
            rb.setPrefHeight(34.0);
            rb.setStyle(
                    "-fx-font-size: 12.5px; -fx-text-fill: #2a6b7c; -fx-cursor: hand; " +
                            "-fx-padding: 4 8 4 8; -fx-background-color: white; " +
                            "-fx-background-radius: 6; -fx-border-color: transparent;"
            );

            if (lang.code().equals(current.code())) {
                rb.setSelected(true);
                selectedLanguage = lang;
                rb.setStyle(
                        "-fx-font-size: 12.5px; -fx-text-fill: #266973; -fx-font-weight: bold; " +
                                "-fx-cursor: hand; -fx-padding: 4 8 4 8; " +
                                "-fx-background-color: #d4eef2; -fx-background-radius: 6; " +
                                "-fx-border-color: #266973; -fx-border-radius: 6; -fx-border-width: 1;"
                );
            }

            rb.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    rb.setStyle(
                            "-fx-font-size: 12.5px; -fx-text-fill: #266973; -fx-font-weight: bold; " +
                                    "-fx-cursor: hand; -fx-padding: 4 8 4 8; " +
                                    "-fx-background-color: #d4eef2; -fx-background-radius: 6; " +
                                    "-fx-border-color: #266973; -fx-border-radius: 6; -fx-border-width: 1;"
                    );
                    selectedLanguage = (Language) rb.getUserData();
                } else {
                    rb.setStyle(
                            "-fx-font-size: 12.5px; -fx-text-fill: #2a6b7c; -fx-cursor: hand; " +
                                    "-fx-padding: 4 8 4 8; -fx-background-color: white; " +
                                    "-fx-background-radius: 6; -fx-border-color: transparent;"
                    );
                }
            });

            languagePane.getChildren().add(rb);
        }
    }

    private void applyClip() {
        double w = dialogPane.getWidth();
        double h = dialogPane.getHeight();
        Rectangle clip = new Rectangle(w, h);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        dialogPane.setClip(clip);
    }

    @FXML
    private void handleConfirm() {
        if (selectedLanguage != null) {
            Localization.setLocale(selectedLanguage.locale());
        }

        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancel() {
        WindowUtil.closeWindow(titleLabel);
    }
}