package services;

import entity.base.ITranslatable;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TranslationServiceTest {

    private final TranslationService service = new TranslationService();

    @Test
    void returnsNullWhenEntityIsNull() {
        assertNull(service.getTranslation(null, "en", "en"));
    }

    @Test
    void returnsExactLanguageMatch() {
        ITranslatable<String> entity = mock(ITranslatable.class);

        Map<String, String> map = new HashMap<>();
        map.put("EN", "Hello");
        map.put("FI", "Moi");

        when(entity.getTranslations()).thenReturn(map);

        String result = service.getTranslation(entity, "en", "fi");

        assertEquals("Hello", result);
    }

    @Test
    void fallsBackToDefaultLanguage() {
        ITranslatable<String> entity = mock(ITranslatable.class);

        Map<String, String> map = new HashMap<>();
        map.put("FI", "Moi");

        when(entity.getTranslations()).thenReturn(map);

        String result = service.getTranslation(entity, "en", "fi");

        assertEquals("Moi", result);
    }

    @Test
    void usesAnyTranslationAsFinalFallback() {
        ITranslatable<String> entity = mock(ITranslatable.class);

        Map<String, String> map = new HashMap<>();
        map.put("DE", "Hallo");

        when(entity.getTranslations()).thenReturn(map);

        String result = service.getTranslation(entity, "en", "fi");

        assertEquals("Hallo", result);
    }

    @Test
    void handlesBlankLangCodeUsesDefault() {
        ITranslatable<String> entity = mock(ITranslatable.class);

        Map<String, String> map = new HashMap<>();
        map.put("FI", "Moi");

        when(entity.getTranslations()).thenReturn(map);

        String result = service.getTranslation(entity, "", "fi");

        assertEquals("Moi", result);
    }
}