# Sprint 7 (Final Iteration – Testing & Refactoring)

Sprint 7 focused on system stabilization, quality improvements, and refactoring based on testing feedback and static analysis (SonarQube).

**Key Improvements**

- Refactored entity design to improve encapsulation and prevent direct modification of internal collections (notes and tags)
- Improved separation of concerns across controller, service, and utility layers
- Enhanced UI consistency across dashboard and authentication screens
- Improved localization coverage by adding missing translation keys identified during testing
- Strengthened error handling and logging across service layer
- Fixed dashboard behavior related to notebook selection and note display updates

---

**Testing & Quality Assurance**
- Expanded unit and integration testing to cover additional edge cases discovered during debugging
- Applied SonarQube analysis to identify design-level issues and improve code quality
- Validated system behavior after refactoring to ensure stability of core CRUD operations