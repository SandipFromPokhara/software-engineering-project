# Software Engineering Project 1 - Sprint 2 Review Report

## Sprint 2 Review – NoteVault

- **Project**: NoteVault = Digital Notetaking and Annotation Tool.
- **Sprint Duration**: 27.01.2026 - 10.02.2026.
- **Team Members**: Sandip Ranjit, Gam Twe He, Dinal Maha Vidanelage and Swostika Lama.
- **Scrum Master**: Swostika Lama.

---

### Sprint 2 Goal

- Build the core foundation of the NoteVault application.
- Implement the database with CRUD operations.
- Begin developing the initial user interface based on the Figma design.
- Integrate unit testing and code coverage tools (JaCoCo, JUnit, Maven)
- Prepare the project for CI/CD activities in Sprint 3.

---

### Completed User Stories / Tasks

**Database**
- Designed and finalized the relational database schema.
- Created MariaDB tables for users and notes.
- Implemented CRUD operations for notes and user accounts.
- Verified CRUD functionality through manual and automated tests. 

**User Interface**
- Set up JavaFX project structure.
- Implemented Entry page, Login and Registration screens.
- Implemented Guest dashboard (Create method only) and Notes Dashboard (for logged in user with all CRUD functionalities) with basic navigation.
- Added simple input validation and UI interactions.

**Testing & Tools**
- Configured JUnit for unit testing.
- Wrote unit tests for CRUD logic and authentication.
- Integrated JaCoCo and generated HTML coverage reports.

**Project Management**
- Updated GitHub repository with commits and documentation.
- Maintained Trello board with task progress.

---

### Sprint 2 Demo Summary

During the Sprint 2 review, the team demonstrated:
- Working database with CRUD operations.
- Initial JavaFX UI screens (Entry page, Guest Dashboard, Login, Registration, Notes Dashboard).
- Navigation between screens and basic note creation.
- Running unit tests and viewing the JaCoCo coverage report.
- Updates and reviews included progress on sprint 2 goal, UI, database implementation, Unit testing backlog creation in Trello, and  documentation in GitHub.
  This demonstration showed the first functional version of the NoteVault application.

---

### What Went Well

- Database integration and CRUD logic worked smoothly.
- UI development progressed smoothly
- The current version doesn’t fully match the Figma design. We have also made some changes in Ui.
- Team collaboration improved, and tasks were completed on time.
- Unit testing setup worked well, and JaCoCo were successfully integrated.

---

### What Could Be Improved

- Increase the number of test cases to ensure better coverage.
- Need to allocate time earlier for CI/CD setup in Sprint 3.

---

### Daily Scrum (Stand-up Meetings)

- Our team had discussions on WhatsApp and Google Meet.
- Updates included progress on product vision, product plan documentation, Figma designs, backlog creation in Trello, and draft documentation in GitHub.

---

### Team contributions

| Team Members          | Tasks                                                                                                                      |
|-----------------------|----------------------------------------------------------------------------------------------------------------------------|
| Swostika Lama         | Scrum master, edit method implementation (full stack), Guest dashboard and create functionality for guest users. Unit test |
| Gam Twe He            | Implement Entry page and Create Note (full-stack), unit test.                                                              |
| Dinal Maha Vidanelage | Implement Signup page (full-stack), delete function and unit test.                                                         |
| Sandip Ranjit         | Dao, Read method implementation (full-stack), login, unit test.                                                            |

---

### Next Sprint Focus (Sprint 3)
- Choose scrum master for sprint 3 (Dinal Maha Vidanelage)
- Reviewed Sprint 2
- Planned product backlog for sprint 3
- Implement CI/CD pipeline using Jenkins.
- Automate unit testing and code coverage reporting.
- Conduct functional testing of all core features. (add some features like undo redo export as pdf)
- Fix bugs identified during testing.
- Prepare Docker image for deployment

---






