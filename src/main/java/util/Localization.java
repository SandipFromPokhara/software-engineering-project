package util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import model.LanguageModel;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class Localization {

    private Localization() {
        /* This utility class should not be instantiated */
    }

    private static final String PREF_KEY = "app_language";
    private static final Preferences prefs = Preferences.userNodeForPackage(Localization.class);
    static final Logger logger = Logger.getLogger(Localization.class.getName());

    private static final ObjectProperty<Locale> locale =
            new SimpleObjectProperty<>(getSavedLocale());

    private static Locale getSavedLocale() {
        String tag = prefs.get(PREF_KEY, Locale.ENGLISH.toLanguageTag());
        return Locale.forLanguageTag(tag);
    }

    public static void setLocale(Locale newLocale) {
        ResourceBundle.clearCache();
        locale.set(newLocale);
        prefs.put(PREF_KEY, newLocale.toLanguageTag()); // persist
    }

    public static Locale getLocale() {
        return locale.get();
    }

    public static String get(String key, Object... args) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.MessagesBundle", getLocale());

        if (!bundle.containsKey(key)) {
            logger.log(Level.WARNING, "Missing i18n key: {0}", key);
            return "!" + key + "!";
        }

        String value = bundle.getString(key);
        return MessageFormat.format(value, args);
    }

    // New: expose the locale property for bindings/listeners
    public static ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    public static StringBinding bind(String key) {
        return Bindings.createStringBinding(() -> get(key), locale);
    }

    public static String getCurrentLanguageCode() {
        return LanguageModel.getByLocale(getLocale()).code();
    }
}
