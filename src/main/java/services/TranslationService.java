package services;

import entity.base.ITranslatable;
import util.Localization;
import java.util.logging.Logger;

public class TranslationService {
    private static final Logger logger = Logger.getLogger(TranslationService.class.getName());

    public <T> T getTranslation(ITranslatable<T> entity, String langCode, String defaultLang) {
        if (entity == null) return null;

        // If defaultLang wasn't provided, use current UI language as a sensible fallback
        if (defaultLang == null || defaultLang.isBlank()) {
            String fallback = Localization.getCurrentLanguageCode();
            logger.fine(() -> "TranslationService: defaultLang was null/blank, falling back to " + fallback);
            defaultLang = fallback;
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
}
