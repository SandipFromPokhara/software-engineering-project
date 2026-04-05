package services;

import entity.entities.NoteEntity;
import entity.translationentities.NoteTranslationEntity;

import static model.LanguageModel.DEFAULT_LANGUAGE_CODE;

public class NoteTranslationService {
    public NoteTranslationEntity getTranslation(NoteEntity note, String langCode) {
        if (note == null) return null;

        var translations = note.getTranslations();
        if (translations == null || translations.isEmpty()) return null;

        if (langCode == null || langCode.isBlank()) {
            langCode = DEFAULT_LANGUAGE_CODE;
        }

        NoteTranslationEntity translation = note.getTranslations().get(langCode);

        if (translation == null) {
            translation = translations.get(DEFAULT_LANGUAGE_CODE);
        }

        if (translation == null && !translations.isEmpty()) {
            translation = translations.values().iterator().next(); // fallback to any available
        }

        return translation;
    }

    public NoteTranslationEntity createTranslation(NoteEntity note, String langCode) {
        if (note == null || langCode == null) return null;

        return note.createTranslation(langCode);
    }
}