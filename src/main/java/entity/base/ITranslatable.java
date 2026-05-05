package entity.base;

import java.util.Map;

public interface ITranslatable<T> {
    Map<String, T> getTranslations();
    T createTranslation(String langCode);
}
