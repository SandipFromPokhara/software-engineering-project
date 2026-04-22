# Localization & Internationalization in NoteVault

## Overview

NoteVault implements localization to support multiple languages and improve accessibility for a diverse user base.

The application is designed using **internationalization (i18n)** principles, where all user-facing text is externalized from the core code and managed through resource files.

---

## Supported Languages

NoteVault currently supports the following languages:

- Burmese (my) 🇲🇲
- English (en) 🇬🇧
- Finnish (fi) 🇫🇮
- Nepali (np) 🇳🇵
- Sinhala (si) 🇱🇰

> Note: Nepali, Sinhala, and Burmese are non-Latin languages.

---

## Localization Approach

Localization is implemented using **Java ResourceBundle**, which allows the application to load language-specific resources at runtime.

### Key Concepts

- All UI text is stored in `.properties` files
- The application selects the appropriate language file based on the user’s selection
- A fallback mechanism ensures English (`en_UK`) is used if a translation is missing
- UI and logic are separated to improve maintainability

---

## Resource File Structure

MessagesBundle_en.properties
MessagesBundle_fi.properties
MessagesBundle_np.properties
MessagesBundle_si.properties
MessagesBundle_my.properties

```
Each file contans key-value pairs for UI text.
Example:
entry.window_title = NoteVault - Digital Note-taking App
entry.hello = Hello!
entry.welcome = Welcome to NoteVault
```
---

## User-Centered Language Selection

NoteVault provides **two separate language selection points**:

### 1. Guest Users
- Language selector is available in the top-right corner of entry window with a Globe icon
- Allows users to select their preferred language before login

### 2. Authenticated Users
- Language selector is available in the user dashboard
- Enables personalized language preference

This approach follows **user-centered design principles**.

---

## Language Persistence

- The selected language is stored after user selection
- The application remembers the last selected language
- On restart, the previously selected language is automatically applied
- Default language is **English (`en_UK`)**

---

## Runtime Behavior

1. User selects a language from the dropdown
2. The application updates the locale
3. UI is refreshed with the selected language
4. Preference is saved for future sessions

---

## Implementation Details

- Java `Locale` is used to define the selected language
- `ResourceBundle` loads the appropriate `.properties` file
- All UI components retrieve text dynamically using keys
- JavaFX UI elements update dynamically upon language change

---

## Current Limitations

- RTL (Right-to-Left) and LTR layout adjustments are not implemented
- Localization currently focuses only on UI text
- Database localization is not implemented yet

---

## Future Improvements

- Addition of more languages (e.g., Farsi, Russian)
- Implementation of database-level localization
- RTL/LTR layout support
- Improved testing for localization features
- Integration with advanced quality tools (e.g., SonarQube)

---

## References

- Java ResourceBundle API
- Java Locale API
- JavaFX UI framework

---

## Related

- See main project [README](../../README.md) for setup and overview