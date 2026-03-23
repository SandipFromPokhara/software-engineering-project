package model;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class LanguageModel {

    public record Language(String code, String fullName, String nativeName, Locale locale) {}

    public static final Map<String, Language> LANGUAGES = new LinkedHashMap<>();

    static {
        LANGUAGES.put("EN", new Language("EN", "English",   "English",    Locale.ENGLISH));
        LANGUAGES.put("FI", new Language("FI", "Finnish",   "Suomi",      new Locale("fi")));
        LANGUAGES.put("NP", new Language("NP", "Nepali",    "नेपाली",      new Locale("ne")));
        LANGUAGES.put("MY", new Language("MY", "Burmese",   "မြန်မာဘာသာ", new Locale("my")));
        LANGUAGES.put("SI", new Language("SI", "Sinhalese", "සිංහල",      new Locale("si")));
    }

    public static Language getByLocale(Locale locale) {
        return LANGUAGES.values().stream()
                .filter(l -> l.locale().getLanguage().equals(locale.getLanguage()))
                .findFirst()
                .orElse(LANGUAGES.get("EN"));
    }
}
