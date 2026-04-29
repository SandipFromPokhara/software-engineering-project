# Database Localization Implementation

## Implementation Details (Hibernate/JPA)

The database localization strategy is implemented using Java Persistence API (JPA) with Hibernate ORM, 
following the translation table pattern described earlier. The implementation separates language-neutral entities 
from language-specific translation entities and uses object-relational mapping to manage relationships.

## Entity Structure

Each translatable entity maintains a collection of translations using a `Map<String, TranslationEntity>`, where:
- Key = Language code (e.g., EN, FI, NP, MY, SI)
- Value = Corresponding translation entity

Example from `NotebookEntity`:

```java
@OneToMany(mappedBy="notebook", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
@MapKey(name="langCode")
private Map<String, NotebookTranslationEntity> translations = new HashMap<>();
```
This design allows efficient retrieval of translations based on language codes.

---

## Translation Entity Mapping

Each translation entity extends a shared base class and stores language-specific fields.

Example: `NotebookTranslationEntity`

```java
@Entity
@Table(name="notebook_translation")
public class NotebookTranslationEntity extends BaseTranslationEntity {

    @Column(name="title", nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name="notebook_id", nullable = false)
    private NotebookEntity notebook;
}
```

## Base Translation Class

All translation entities inherit from `BaseTranslationEntity`, which defines common fields:

```java
@MappedSuperclass
public class BaseTranslationEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="language_code", nullable = false, length = 6)
    private String langCode;

    @Column(name="createdAt")
    private LocalDateTime createdAt;

    @Column(name="updatedAt")
    private LocalDateTime updatedAt;
}
```

**Key Features:**
- Language code is immutable once set
- Automatic timestamp management using `@PrePersist` and `@PreUpdate`

---

## Translation Management Logic

Translations are managed within the parent entity:

```java
public void addTranslation(NotebookTranslationEntity nt) {
if (nt != null) {
String langCode = nt.getLangCode();
nt.setNotebook(this);

        if (translations.putIfAbsent(langCode, nt) != null) {
            throw new IllegalArgumentException("Translation already exists for language: " + langCode);
        }
    }
}
```

**Features:**
- Prevents duplicate translations per language
- Maintains bidirectional relationship integrity
- Ensures each translation is linked to its parent entity

---


## Translation Creation

```java
@Override
public NotebookTranslationEntity createTranslation(String langCode) {
NotebookTranslationEntity nt = new NotebookTranslationEntity();
nt.setLangCode(langCode);
addTranslation(nt);
return nt;
}

```

---

## Data Integrity and Constraints
- Each translation is linked to exactly one parent entity via `@ManyToOne`
- `orphanRemoval = true` ensures unused translations are deleted
- `CascadeType.ALL` propagates persistence operations
- Language codes are enforced as unique within each entity

---

## Encoding Support

To support multilingual content (including non-Latin scripts), the database uses `UTF-8 (utf8mb4)`
encoding, which provides full Unicode support.

Example from `TagEntity`:

```java
@Column(
name="tag_name",
nullable = false,
unique = true,
columnDefinition = "VARCHAR(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
)
private String tagName;
```

---

## Summary of Implementation

The implementation ensures:

- Clear separation of language-neutral and language-specific data
- Efficient lookup using language-keyed maps
- Strong data integrity through ORM constraints
- Scalability for adding new languages
- Full support for international character sets