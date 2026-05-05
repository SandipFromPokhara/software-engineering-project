package services;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import model.LanguageModel;
import model.LanguageModel.Language;
import util.Localization;

public class LanguageComboService {

    private LanguageComboService() {/* This utility class should not be instantiated */}

    public static void setup(ComboBox<Language> comboBox) {

        comboBox.getItems().addAll(LanguageModel.LANGUAGES.values());

        // Closed state
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Language lang, boolean empty) {
                super.updateItem(lang, empty);
                setText(empty || lang == null ? "" : lang.nativeName());
            }
        });

        // Open dropdown
        comboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Language lang, boolean empty) {
                super.updateItem(lang, empty);

                if (empty || lang == null) {
                    setText("");
                } else {
                    setText(lang.fullName() + " (" + lang.nativeName() + ")");
                }
            }
        });

        Language current = LanguageModel.getByLocale(Localization.getLocale());
        comboBox.setValue(current);

        comboBox.setOnAction(e -> {
            Language selected = comboBox.getValue();
            if (selected != null) {
                Localization.setLocale(selected.locale());
            }
        });
    }
}
