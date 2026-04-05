package services;

import entity.base.Translatable;

public class TranslationService {
    public <T> T getTranslation(Translatable<T> entity, String langCode, String defaultLang) {
        if (entity == null) return null;

        if (defaultLang == null || defaultLang.isBlank()) {
            throw new IllegalArgumentException("Default language must not be null or blank");
        }

        if (langCode == null || langCode.isBlank()) {
            langCode = defaultLang;
        }

        var translations = entity.getTranslations();
        if (translations == null || translations.isEmpty()) {
            return null;
        }

        T translation = translations.get(langCode.toUpperCase());

        if (translation == null) {
            translation = translations.get(defaultLang);
        }

        // final fallback → ANY available translation
        if (translation == null) {
            return translations.values().stream().findFirst().orElse(null);
        }

        return translation;
    }

    public <T> T createTranslation(Translatable<T> entity, String langCode) {
        if (entity == null) return null;

        if (langCode == null || langCode.isBlank()) {
            throw new IllegalArgumentException("Language code cannot be null or blank");
        }

        return entity.createTranslation(langCode);
    }
}