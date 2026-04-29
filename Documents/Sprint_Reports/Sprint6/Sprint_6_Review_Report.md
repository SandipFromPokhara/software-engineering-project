# Software Engineering Project 2 - Sprint Review Report

## Sprint 6 Review – NoteVault

**Project:** NoteVault - Digital Note-taking and Annotation Tool  
**Sprint Duration:** 01.04.2026 - 15.04.2026   
**Team:** Dinal Maha Vidanelage, Sandip Ranjit, Swostika Lama, Twe He Gam Aung  
**Scrum Master for Sprint 6:** Sandip Ranjit

---

## Sprint 6 Goal

The goal of Sprint 6 was to extend the multilingual functionality of the NoteVault application into the database layer 
while improving overall system quality, maintainability, and readiness for final evaluation.

This involved implementing database-level localization support, performing a full static code analysis using SonarQube, 
and refactoring the codebase based on identified issues. In addition, the team prepared an acceptance testing plan 
and updated system architecture documentation including ER and UML diagrams.

---

## Completed User Stories / Tasks

**Database Localization Implementation**

- Designed and implemented multilingual support at the database level
- Refactored database schema to support translations for notebooks and notes
- Ensured proper UTF-8 encoding for multilingual data storage
- Validated correct storage and retrieval of localized content
- Updated entity structures to support translation mappings

---

**Statistical Code Review (SonarQube Analysis)**

- Performed static code analysis using SonarQube
- Identified issues related to:
  - Code complexity
  - Duplication
  - Design vulnerabilities
  - Encapsulation issues
- Reviewed metrics such as maintainability and code smells
- Documented findings and prioritized refactoring tasks

---

**Code Clean-Up and Refactoring**

- Refactored complex methods to improve readability and maintainability
- Removed redundant and duplicated logic across controllers and services
- Improved naming conventions and overall code structure
- Applied Java best practices and coding standards
- Ensured all unit tests passed after refactoring

---

**Acceptance Test Planning**

- Defined acceptance criteria for core system functionality
- Designed test cases covering:
  - Functional correctness
  - Usability behavior
  - Data consistency and reliability
- Mapped test cases to system requirements

---

**Architecture Documentation Updates**

- Updated ER diagrams to reflect database localization structure
- Updated UML diagrams to reflect service-layer improvements
- Stored updated diagrams in the /Documents folder
- Improved clarity of system architecture and layer separation

---

## Sprint 6 Demo Summary

During the Sprint 6 review session, the team demonstrated:

- Successful storage and retrieval of multilingual data from the database
- Refactored and cleaner codebase with improved structure
- SonarQube analysis results highlighting improvements and resolved issues
- Updated system architecture diagrams reflecting database localization
- A stable system with no functional regressions after refactoring

---

## What Went Well

- Database localization was successfully implemented and integrated
- Code quality improved significantly after SonarQube-driven refactoring
- Strong alignment between testing results and implemented improvements
- Improved separation of concerns across service and controller layers
- System remained stable despite extensive refactoring

---

## What Could Be Improved

- Initial database localization design required multiple iterations before finalization
- Some SonarQube issues required deeper refactoring than expected
- More automated test coverage could further support regression prevention

---

## Challenges Faced

- Managing database schema changes without breaking existing functionality
- Handling multilingual data consistency across different layers
- Interpreting and prioritizing SonarQube findings effectively
- Ensuring refactoring did not introduce regressions

---

## Daily Scrum (Stand-up Meetings)

- Daily coordination was conducted via team communication channels
- Discussions focused on:
    - Database localization progress
    - Code quality issues identified via SonarQube
    - Refactoring tasks and testing results
    - Documentation and diagram updates
- Identified issues were resolved collaboratively within the team

---

## **Team Contributions**

| Team Member           | Assigned Tasks                                                                                         | Time Spent (hrs) | In-class Tasks |
|-----------------------|--------------------------------------------------------------------------------------------------------|------------------|----------------|
| Sandip Ranjit         | Scrum Master, Sprint planning, DB localization, localization testing, README and documentation updates | 40               | Submitted      |
| Twe He Gam Aung       | Code analysis, Code refactoring after SonarQube Analysis                                               | 38               | Submitted      |
| Dinal Maha Vidanelage | Code analysis, Code refactoring after SonarQube Analysis                                               | 38               | Submitted      |
| Swostika Lama         | Code analysis, Code refactoring after SonarQube Analysis                                               | 38               | Submitted      |

---

## Next Sprint Focus (Sprint 7)

- Choose scrum master for Sprint 6 (Dinal Maha Vidanelage).
- Final system stabilization and cleanup
- Improve UI consistency and usability
- Enhance error handling and user feedback mechanisms
- Final review of localization implementation
- Final testing and preparation for project submission
- Documentation finalization and repository cleanup

---

## Conclusion

Sprint 6 successfully delivered database-level localization and significant improvements in code quality through systematic refactoring and static analysis.

The system is now more stable, maintainable, and aligned with software engineering best practices, providing a strong foundation for final project completion in Sprint 7.