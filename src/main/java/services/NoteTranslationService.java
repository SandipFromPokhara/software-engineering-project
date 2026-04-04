package services;

import entity.NoteEntity;
import entity.NoteTranslationEntity;

import static model.LanguageModel.DEFAULT_LANGUAGE_CODE;

public class NoteTranslationService {
    public NoteTranslationEntity getTranslation(NoteEntity note, String langCode) {
        if (note == null) return null;

        if (langCode == null || langCode.isBlank()) {
            langCode = DEFAULT_LANGUAGE_CODE;
        }

        NoteTranslationEntity translation = note.getTranslations().get(langCode);

        if (translation == null) {
            translation = note.getTranslations().get(DEFAULT_LANGUAGE_CODE);
        }

        return translation;
    }

    public NoteTranslationEntity createTranslation(NoteEntity note, String langCode) {
        if (note == null || langCode == null) return null;

        return note.createTranslation(langCode);
    }
}