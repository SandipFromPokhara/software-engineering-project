package util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class Localization {

    private static final ObjectProperty<Locale> locale =
            new SimpleObjectProperty<>(Locale.ENGLISH);

    public static void setLocale(Locale newLocale) {
        locale.set(newLocale);
    }

    public static Locale getLocale() {
        return locale.get();
    }

    public static String get(String key, Object... args) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.MessagesBundle", getLocale());

        if (!bundle.containsKey(key)) {
            return "!" + key + "!";
        }

        String value = bundle.getString(key);
        return MessageFormat.format(value, args);
    }

    public static StringBinding bind(String key) {
        return Bindings.createStringBinding(() ->
                get(key), locale);
    }
}