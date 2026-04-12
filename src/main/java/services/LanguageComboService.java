package services;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;
import model.LanguageModel;
import model.LanguageModel.Language;
import util.Localization;

public class LanguageComboService {

    private LanguageComboService() {/* This utility class should not be instantiated */}

    public static void setup(ComboBox<String> comboBox) {

        comboBox.getItems().addAll(LanguageModel.LANGUAGES.keySet());

        // Closed state
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String code, boolean empty) {
                super.updateItem(code, empty);
                setText(empty || code == null ? "" : code);
                getStyleClass().add("language-combo-button");
            }
        });

        // Open dropdown
        comboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String code, boolean empty) {
                super.updateItem(code, empty);

                if (empty || code == null) {
                    setText("");
                    setStyle("");
                } else {
                    var lang = LanguageModel.LANGUAGES.get(code);
                    setText(lang.fullName() + " (" + lang.nativeName() + ")");
                }

                getStyleClass().add("language-combo-item");
            }
        });

        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(String code) {
                return code == null ? "" : code;
            }

            @Override
            public String fromString(String s) {
                return s;
            }
        });

        Language current = LanguageModel.getByLocale(Localization.getLocale());
        comboBox.setValue(current.code());

        comboBox.setOnAction(e -> {
            String selected = comboBox.getValue();
            if (selected != null) {
                Localization.setLocale(
                        LanguageModel.LANGUAGES.get(selected).locale()
                );
            }
        });
    }
}
