package services;

import entity.base.ITranslatable;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TranslationServiceTest {

    private final TranslationService svc = new TranslationService();

    @Test
    void nullEntityReturnsNull() {
        assertNull(svc.getTranslation(null, "EN", "EN"));
    }

    @Test
    void exactLangFoundAndCaseInsensitive() {
        ITranslatable<String> t = new ITranslatable<>() {
            @Override
            public Map<String, String> getTranslations() {
                Map<String, String> m = new HashMap<>();
                m.put("EN", "Hello");
                m.put("DE", "Hallo");
                return m;
            }

            @Override
            public String createTranslation(String langCode) {
                throw new UnsupportedOperationException();
            }
        };

        String res = svc.getTranslation(t, "en", "DE");
        assertEquals("Hello", res);
    }

    @Test
    void fallbackToDefaultOrAny() {
        // default present
        ITranslatable<String> t1 = new ITranslatable<>() {
            @Override
            public Map<String, String> getTranslations() {
                return Map.of("DE", "Hallo");
            }

            @Override
            public String createTranslation(String langCode) { throw new UnsupportedOperationException(); }
        };

        String res1 = svc.getTranslation(t1, "FR", "DE");
        assertEquals("Hallo", res1);

        // neither requested nor default present -> return any available translation
        ITranslatable<String> t2 = new ITranslatable<>() {
            @Override
            public Map<String, String> getTranslations() {
                return Map.of("ES", "Hola");
            }

            @Override
            public String createTranslation(String langCode) { throw new UnsupportedOperationException(); }
        };

        String res2 = svc.getTranslation(t2, "FR", "DE");
        assertEquals("Hola", res2);
    }
}

