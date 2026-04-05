package util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class Localization {

    private static final String PREF_KEY = "app_language";
    private static final Preferences prefs = Preferences.userNodeForPackage(Localization.class);
    private static final Logger logger = Logger.getLogger(Localization.class.getName());
    private static final String BUNDLE_PATH = "i18n.MessagesBundle";

    private static final ObjectProperty<Locale> locale =
            new SimpleObjectProperty<>(getSavedLocale());

    // static initialization
    private static volatile ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH, getSavedLocale());

    // cache bundle per locale
    static {
        locale.addListener((obs, oldLocale, newLocale) -> {
            ResourceBundle.clearCache();
            bundle = ResourceBundle.getBundle(BUNDLE_PATH, newLocale);
            prefs.put(PREF_KEY, newLocale.toLanguageTag());    // persist
        });
    }

    private static Locale getSavedLocale() {
        String tag = prefs.get(PREF_KEY, Locale.ENGLISH.toLanguageTag());
        return Locale.forLanguageTag(tag);
    }

    public static void setLocale(Locale newLocale) {
        locale.set(newLocale);
    }

    public static Locale getLocale() {
        return locale.get();
    }

    public static String get(String key, Object... args) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        try {
            String value = bundle.getString(key);
            return MessageFormat.format(value, args);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Missing i18n key: {0}", key);
            return "!" + key + "!";
        }
    }

    // New: expose the locale property for bindings/listeners
    public static ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    public static StringBinding bind(String key) {
        return Bindings.createStringBinding(() -> get(key), locale);
    }

    public static String getCurrentLanguageCode() {
        return getLocale().getLanguage();
    }
}