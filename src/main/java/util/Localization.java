package util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class Localization {

    private static final String PREF_KEY = "app_language";
    private static final Preferences prefs = Preferences.userNodeForPackage(Localization.class);

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

    // New: expose the locale property for bindings/listeners
    public static ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    public static String get(String key) {
        return ResourceBundle
                .getBundle("MessagesBundle", getLocale())
                .getString(key);
    }

    public static StringBinding bind(String key) {
        return Bindings.createStringBinding(() -> get(key), locale);
    }
}