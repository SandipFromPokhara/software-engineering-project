package services;

import entity.NoteEntity;
import entity.NoteTranslationEntity;

import static model.LanguageModel.DEFAULT_LANGUAGE_CODE;

public class NoteTranslationService {
    public NoteTranslationEntity getOrCreateTranslation(NoteEntity note, String langCode) {
        if (note == null) {
            return null;
        }

        if (langCode == null || langCode.isBlank()) {
            langCode = DEFAULT_LANGUAGE_CODE;
        }

        NoteTranslationEntity translation = note.getTranslations().get(langCode);

        if (translation == null) {
            translation = note.createTranslation(langCode);
        }

        return translation;
    }
}