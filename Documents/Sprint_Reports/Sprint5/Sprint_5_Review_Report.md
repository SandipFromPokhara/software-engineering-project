# Software Engineering Project 2 - Sprint 5 Review Report

## Sprint 5 Review – NoteVault

- **Project**: NoteVault – Digital Note-taking and Annotation Tool
- **Sprint Duration**: 18.03.2026 – 01.04.2026
- **Team Members**: Sandip Ranjit, Twe He Gam Aung, Dinal Maha Vidanelage, Swostika Lama
- **Scrum Master**: Twe He Gam Aung

---

## **Sprint 5 Goal**

The goal of Sprint 5 was to implement comprehensive **UI localization** in the NoteVault application and prepare the system for scalable multilingual support.

This involved refactoring the existing UI to remove hardcoded text, externalizing all user-facing content, implementing a dynamic language selection mechanism, and ensuring seamless runtime language switching across the application.

---

## **Completed User Stories / Tasks**

### **UI Localization Preparation**
- Refactored the entire user interface to eliminate hardcoded text.
- Externalized all UI strings into structured localization resource files.
- Identified and documented all translatable UI components (labels, buttons, messages, menus).
- Designed a scalable localization architecture to support future language extensions.

---

### **Multilingual Implementation**
- Successfully implemented support for the following languages:
  - English (Base Language)
  - Nepali
  - Burmese
  - Finnish
  - Sinhala
- Enabled full support for **non-Latin character sets**.
- Ensured correct encoding, font rendering, and display consistency across all languages.

---

### **Language Selector & Dynamic Switching**
- Developed and integrated a language selector within the user interface.
- Implemented **dynamic runtime language switching** without requiring application restart.
- Ensured all UI components update instantly, including:
  - Labels and buttons
  - Menus and navigation elements
  - System messages and notifications

---

### **UI Adaptation & Localization Testing**
- Resolved UI layout issues caused by varying text lengths across languages.
- Ensured responsive and consistent UI behavior for all supported languages.
- Conducted extensive localization testing in both local and Docker environments.
- Verified that no UI components break or misalign during language switching.

---

### **DevOps & Deployment**
- Updated Docker configuration to include localization resources.
- Built and tested a Docker image supporting multilingual functionality.
- Verified successful deployment and execution in a containerized environment.
- Ensured consistency between development and deployment environments.

---

### **Documentation & Repository Updates**
- Updated GitHub repository with:
  - Localization resource files
  - Refactored UI codebase
  - Updated README with language usage instructions
- Maintained Trello board to reflect sprint progress and task completion.
- Ensured all deliverables align with sprint requirements.

---

## **Sprint 5 Demo Summary**

During the Sprint 5 review session, the team demonstrated:

- A fully localized NoteVault application supporting multiple languages.
- Seamless real-time language switching functionality.
- Correct rendering of non-Latin languages (Burmese, Nepali, Sinhala).
- Clean separation of UI content using resource bundles.
- Dockerized application running successfully with localization support.
- A stable, production-ready system.

This sprint represents the **completion of all core application features** and establishes a strong foundation for future enhancements.

---

## **What Went Well**

- All planned localization features were successfully implemented within the sprint timeline.
- Dynamic language switching was implemented smoothly without major issues.
- Strong team collaboration in handling translation, UI adaptation, and testing.
- Docker deployment worked reliably with localization support.
- Code quality improved significantly after removing hardcoded strings.
- The system is now scalable for adding new languages in the future.

---

## **What Could Be Improved**

- Localization testing could be expanded to cover more edge cases and UI scenarios.
- UI layout adjustments required more time than initially estimated.
- Translation consistency could be improved using standardized tools or guidelines.

---

## **Challenges Faced**

- Managing UI layout variations caused by different language lengths.
- Translating technical terms accurately while preserving context.
- Supporting non-Latin languages and ensuring correct font rendering.
- Maintaining consistency across multiple localization files.

---

## **Daily Scrum (Stand-up Meetings)**

- Daily communication was conducted via WhatsApp and periodic online meetings.
- Discussions focused on:
  - Localization progress
  - UI adjustments and bug fixes
  - Translation updates
  - Docker testing and deployment
- Identified blockers were resolved collaboratively within the team.

---

## **Team Contributions**

| Team Member            | Assigned Tasks                                                                                                                                                                                         | Time Spent (hrs) | In-class Tasks |
|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------|----------------|
| Twe He Gam Aung       | Scrum Master, sprint planning, UI refactoring for localization, Burmese language implementation, localization testing, deployment updates, README and documentation updates                         | 42               | Submitted      |
| Sandip Ranjit         | Localization integration, pull request management, Nepali language implementation, deployment updates, localization testing                                                                          |                  | Submitted      |
| Dinal Maha Vidanelage | Language selector UI implementation, Sinhala language implementation, localization testing                                                                                                           |                  | Submitted      |
| Swostika Lama         | Localization resource preparation and translation planning, English/base localization setup, Finnish language implementation, localization testing                                                   |                  | Submitted      |

---

### **Next Sprint Focus (Sprint 6)**

- Choose scrum master for Sprint 6 (Sandip Ranjit).
- Review Sprint 5 accomplishments and lessons learned.
- Plan product backlog for Sprint 6.
- Implement database-driven localization for multilingual support.
- Design and update database schema for storing localized content.
- Migrate localization from resource bundles to database.
- Test database localization functionality and ensure system stability.
- Prepare final demonstration and documentation for database localization.

---

## **Conclusion**

Sprint 5 successfully implemented UI localization and dynamic language switching in the NoteVault application.

The system is now prepared for the next phase, focusing on database-driven localization and further scalability improvements.