package entity.base;

import java.util.Map;

public interface Translatable<T> {
    Map<String, T> getTranslations();
    T createTranslation(String langCode);
}