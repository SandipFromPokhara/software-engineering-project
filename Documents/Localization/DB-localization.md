# Database Localization Strategy

## Overview

To support multilingual functionality in the NoteVault application, database localization is implemented using a 
**translation table pattern**. This approach separates language-independent data from language-specific content, 
allowing the application to dynamically store and retrieve content in multiple languages.

---

## Design Approach

The application uses a **parent–translation table structure**:

- Main entities store **language-neutral data**
- Separate translation tables store **language-specific fields**

### Example Structure

| Entity                      | Purpose                                        |
|-----------------------------|------------------------------------------------|
| `NoteEntity`                | Core note data (relations, metadata)           |
| `NoteTranslationEntity`     | Translated fields (title, content, annotation) |
| `NotebookEntity`            | Notebook structure                             |
| `NotebookTranslationEntity` | Translated notebook titles                     |

---

## Entity Relationship Design

Each translatable entity follows this pattern:

```java
Map<String, TranslationEntity> translations;
```
- Key → language code (EN, FI, NP, etc.)
- Value → translated content

---

## Translation Handling

Translations are retrieved using:

```
translationService.getTranslation(entity, langCode, defaultLang);
```
**Fallback:**

1. Try selected language
2. Fallback to default language (EN)
3. Fallback to any available translation

**Supported Languages currently**
- English (default)
- Nepali
- Burmese
- Finnish
- Sinhala

**Encoding**

Database uses UTF-8 (utf8mb4) to support Non-Latin scripts.

---

See [DB localization implementation details](DB-localization-implementation.md) for detailed implementation details.

See [ER diagram](../Diagrams/08-updated-er-diagram.png) or [Relational Schema](../Diagrams/09-updated-relational-schema.png) for UML diagrams.