# Software Engineering Project 2 - Sprint Planning Report

## Sprint Number: 6

**Project:** NoteVault - Digital Note-taking and Annotation Tool  
**Sprint Duration:** 01.04.2026 - 15.04.2026   
**Team:** Dinal Maha Vidanelage, Sandip Ranjit, Swostika Lama, Twe He Gam Aung  
**Scrum Master for Sprint 6:** Sandip Ranjit

---

## Sprint Goal

The goal of Sprint 6 is to extend the multilingual capabilities of the NoteVault application into the database layer while improving overall code quality, 
maintainability, and readiness for final project acceptance.

During this sprint, the team will implement database-level localization to ensure that multilingual data is properly stored,
retrieved, and displayed. In parallel, the team will conduct a comprehensive statistical code review using static analysis tools 
to identify code quality issues.

Based on the findings, the codebase will be refactored and cleaned to align with best practices and improve readability and maintainability. 
Additionally, the team will design a formal acceptance test plan and update system architecture documentation, including ER diagrams and UML models.

By the end of Sprint 6, the team aims to deliver a fully localized database structure, a clean and standardized codebase,
detailed code review documentation, and a complete acceptance testing plan, ensuring the application is ready for final evaluation.

---

## Sprint Objectives
**1. Database Localization**
- Design and implement multilingual support at the database level
- Refactor database schema to support translations (ERD updates)
- Ensure UTF-8 encoding and proper locale handling
- Validate multilingual data storage and retrieval

**2. Statistical Code Review**
- Perform static code analysis using tools such as:
  - SonarQube
  - SonarScanner
  - Checkstyle / PMD / FindBugs
- Analyze:
  - Cyclomatic complexity
  - Code duplication
  - Method size and readability
  - Identify bugs, inefficiencies, and violations
  
**3. Code Clean-Up and Refactoring**
- Refactor complex and large methods
- Remove redundant and duplicate code
- Improve naming conventions and formatting
- Apply Java coding standards
- Add comments and documentation
- Ensure all unit tests pass after changes

**4. Acceptance Test Planning**
- Define clear acceptance criteria
- Design test cases for:
  - Functional testing
  - Usability testing
  - Performance and reliability
- Map test cases to requirements

**5. Architecture Design Documentation**
- Update ER diagrams to reflect localization
- Create/update UML diagrams
- Store all diagrams in /Documents folder

---

## Sprint Backlog
| Task	                                  | Story Points |
|----------------------------------------|--------------|
| Database Localization                  | 5            |
| Statistical code review                | 5            |
| Code clean-up and refactoring          | 5            |
| Acceptance test planning               | 5            |
| Architecture documentation (ERD & UML) | 5            |
| Implement Rich Text Formatting         | 3            |
| Debug & manual testing                 | 3            |

---

## Expected Deliverables
- Localized database supporting multilingual data
- Updated ER diagram reflecting translation structure
- Statistical Code Review Report including:
  - Metrics (complexity, duplication, etc.)
  - Identified issues and recommendations
  - Screenshots and charts
- Clean and refactored codebase in GitHub
- Successful unit test execution (no regressions)
- Acceptance Test Plan document
- UML diagrams and system design files in /Documents
- Updated GitHub repository with documentation

---

## Team Capacity and Assumptions

**Capacity:** 4 members × 3–4 hours/day × ~8–10 days = about **100–120 hours**

**Assumptions:**
- Database localization may require schema restructuring and migration handling
- Static analysis tools may reveal significant refactoring needs
- Refactoring must not break existing functionality
- Acceptance testing is design-focused (not execution)
- Team collaboration is required to maintain consistency across code improvements

---

## Definition of Done

A task is considered complete when:

- Database supports multilingual data with proper schema design
- All data is stored and retrieved correctly using localization logic
- Static code analysis is completed and documented
- Codebase is refactored, cleaned, and follows coding standards
- No critical issues remain after refactoring
- All unit tests pass successfully
- Acceptance test plan is fully documented
- ER diagrams and UML designs are updated and stored in /Documents 
- GitHub repository is updated with all deliverables
- Sprint progress is tracked and updated in Trello

---