package entity.entities;

import entity.translationentities.NoteTranslationEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoteEntityTest {

    @Test
    void tagsAndTranslationsAndFormatting() {
        NoteEntity note = new NoteEntity();
        TagEntity tag = new TagEntity();
        tag.setTagName("x");

        note.addTag(tag);
        assertTrue(note.getTags().contains(tag));

        note.removeTag(tag);
        assertFalse(note.getTags().contains(tag));

        // translations
        NoteTranslationEntity nt = new NoteTranslationEntity();
        nt.setLangCode("EN");
        note.addTranslation(nt);
        assertEquals(nt, note.getTranslations().get("EN"));

        // remove translation
        note.removeTranslation("EN");
        assertNull(note.getTranslations().get("EN"));

        // formatted time when createdAt null
        assertEquals("", note.getFormattedCreatedTime());
    }
}

