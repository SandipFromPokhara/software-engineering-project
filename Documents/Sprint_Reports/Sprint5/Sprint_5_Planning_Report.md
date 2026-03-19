# Software Engineering Project 2 - Sprint Planning Report

## Sprint Number: 5

**Project:** NoteVault - Digital Note-taking and Annotation Tool  
**Sprint Duration:** 18.03.2026 - 01.04.2026   
**Team:** Dinal Maha Vidanelage, Sandip Ranjit, Swostika Lama, Twe He Gam Aung  
**Scrum Master for Sprint 5:** Twe He Gam Aung

---

## Sprint Goal

The goal of Sprint 5 is to prepare the NoteVault application for multilingual support by implementing user interface
localization and enabling the system to support multiple languages dynamically.

During this sprint, the team will refactor the existing user interface to remove hardcoded text and externalize all UI
elements into localization resource files. The team will then implement a language selection feature and add support for
four languages: Nepali, Burmese, Finnish, and Sinhala.

In addition, the team will ensure that the application can dynamically switch languages without restarting, and that all
UI components display correctly across different languages. The deployment configuration will also be updated to support
localization, and the localized application will be tested in the Docker environment.

By the end of Sprint 5, the team aims to deliver a fully localized prototype with multilingual support, updated
documentation, and a stable deployment-ready application.

---

## Sprint Objectives

- Refactor UI to remove hardcoded text and prepare for localization
- Create localization resource structure and translation plan
- Implement language selector in the user interface
- Add support for Nepali, Burmese, Finnish and Sinhala languages
- Ensure UI dynamically updates when language is changed
- Verify correct rendering of text, layout, and fonts for all languages
- Update Docker deployment to include localization resources
- Perform localization testing and fix UI issues
- Update GitHub documentation and README with localization instructions
- Maintain Trello board and sprint progress tracking

---

## Sprint Backlog

The following Product Backlog items were selected for Sprint 5:

| Task                                            | Story Points |
|-------------------------------------------------|--------------|
| Refactor UI for localization                    | 5            |
| Localization planning and translation resources | 5            |
| Implement language selector                     | 3            |
| Base language setup (English)                   | 3            |
| Nepali language implementation                  | 5            |
| Burmese language implementation                 | 5            |
| Finnish language implementation                 | 5            |
| Sinhala language implementation                 | 5            |
| Dynamic language switching                      | 3            |
| Deployment update for localization              | 5            |
| Localization testing and UI fixes               | 3            |
| Documentation and GitHub update                 | 3            |

---

## Expected Deliverables

- Localized NoteVault application supporting Nepali, Burmese, Finnish and Sinhala
- Functional language selector integrated into the UI
- Fully externalized UI text using localization resources
- Support for non-Latin languages
- Dynamic language switching without application restart
- Updated Docker image supporting multilingual application
- Updated GitHub repository with localization documentation
- Updated Trello board reflecting Sprint 5 progress

---

## Team Capacity and Assumptions

**Capacity:** 4 members × 3–4 hours/day × ~8–10 days = about **100–120 hours**

**Assumptions:**  
The team assumes that refactoring the existing UI for localization will require significant effort. Additional time may
be needed to resolve UI layout issues caused by different language lengths and character sets. Team members will
collaborate closely to ensure translation consistency and proper integration.

---

## Definition of Done

A task is considered complete when:

- All UI text is externalized and no hardcoded strings remain
- Language selector is implemented and functional
- Application supports Nepali, Burmese, Finnish and Sinhala languages
- UI updates dynamically when language is changed
- All screens display correctly without layout issues
- Docker deployment supports localization and runs successfully
- Localization testing is completed with no critical issues
- Documentation is updated with clear localization instructions
- GitHub repository contains updated code and resources
- Trello board reflects task completion and sprint progress