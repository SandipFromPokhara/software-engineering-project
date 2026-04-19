package entity.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaseTranslationEntityTest {

    @Test
    void setLangCodeUppercasesAndImmutable() {
        BaseTranslationEntity b = new BaseTranslationEntity();
        b.setLangCode("en");
        assertEquals("EN", b.getLangCode());

        // second set should throw
        assertThrows(IllegalStateException.class, () -> b.setLangCode("de"));
    }
}

