# NoteVault - A digital note management application

**NoteVault** is a JavaFX-based desktop application designed for hierarchical note management.
It enables users to securely create, organize, and manage notes within structured notebooks.

The system is primarily targeted at students and individuals who require an efficient and organized way to manage personal or academic notes. 
The application ensures secure authentication and structured data handling while maintaining a responsive user interface.

The project was developed over 8 sprints (2 weeks each) as part of Software Engineering Project 1 (SEP1) and Software Engineering Project 2 (SEP2). 
It emphasizes the complete software development lifecycle, including DevOps practices, CI/CD pipelines, testing, and deployment.

Main technologies used include JavaFX, MariaDB, Docker, Jenkins, Kubernetes, and Maven.

---

## Product Vision

**Vision Statement**  
To develop a secure, scalable, and user-friendly desktop application that allows efficient hierarchical note management with modern DevOps integration.

**Goals**
- Provide structured note organization using notebooks
- Ensure secure authentication and data protection
- Support multiple languages for broader accessibility
- Implement a complete DevOps pipeline for automation and deployment

**Key Features**
- Hierarchical notebook and note management
- Secure login using BCrypt
- Multi-language UI support (Burmese, English, Farsi, Finnish, Nepali, Sinhala)
- CI pipeline using Jenkins
- Containerized deployment using Docker and Kubernetes

**Definition of Success**
The project is considered successful when all core features are implemented,
tested, documented, and deployed through an automated DevOps pipeline with acceptable code quality metrics.

---

## Project Plan & Sprint Structure

The project follows an Agile Scrum methodology with iterative development and continuous feedback.

- Sprint length: 2 weeks
- Total sprints: 8
- Tools: Git (feature branching), Trello for task management

### Sprint Overview

| Sprint   | Focus                        |
|----------|------------------------------|
| Sprint 1 | Project Planning & Vision    |
| Sprint 2 | Requirements & Database      |
| Sprint 3 | UI & CI                      |
| Sprint 4 | Docker Containerization      |
| Sprint 5 | UI Localization & Kubernetes |
| Sprint 6 | Database Localization        |
| Sprint 7 | Quality Assurance            |
| Sprint 8 | Documentation & Finalization |

---

## Sprint 1 – Project Planning & Vision

Sprint 1 focused on project setup and alignment: the team was formed and roles were formed, collaboration tools (GitHub, Trello, communication channels) and the development environment were discussed and configured, 
an initial product vision and backlog were created, and low-fidelity UI mockups (Figma) and the initial project plan were produced.
The sprint delivered the repository structure, and the first set of prioritized user stories.

🔗 [Sprint 1 Planning](Documents/Sprint_Reports/Sprint1/Sprint_1_Planning_Report.md)  
🔗 [Sprint 1 Review](Documents/Sprint_Reports/Sprint1/Sprint_1_Review_Report.md)

---

## Sprint 2 – Requirements & Database

Sprint 2 established the application's core foundations: the relational database schema was designed and implemented in MariaDB, 
and CRUD operations for users and notes were completed. Initial JavaFX screens (Entry, Login, Registration, Guest and Notes dashboards) were built and basic input validation added; 
unit testing (JUnit) and JaCoCo coverage reporting were integrated to prepare the codebase for CI work.

🔗 [Use Case Diagram](Documents/Diagrams/01-use-case-overview.png)
🔗 [ER Diagram](Documents/Diagrams/02-initial-er-diagram.png)
🔗 [Relational Schema](Documents/Diagrams/03-initial-relational_schema.png)

🔗 [Database architecture](Documents/Database/database-architecture.md)

🔗 [Sprint 2 Planning](Documents/Sprint_Reports/Sprint2/Sprint2_Planning.md)
🔗 [Sprint 2 Review](Documents/Sprint_Reports/Sprint2/Sprint2_Review_Report.md)

---

## Sprint 3 – UI Implementation & CI

Sprint 3 extended features and introduced DevOps automation: advanced note-management features (undo/redo, text editing tools, tagging, PDF export, theme toggle) were implemented, 
and a Jenkins CI pipeline was configured to run builds, tests and JaCoCo coverage reports. A Docker image was also created and tested locally to prepare for later deployment.

🔗 [Sprint 3 Planning](Documents/Sprint_Reports/Sprint3/Sprint_3_Planning_Report.md)
🔗 [Sprint 3 Review](Documents/Sprint_Reports/Sprint3/Sprint_3_Review_Report.md)

---

## Sprint 4 – Docker Containerization

Sprint 4 finalized the prototype and prepared it for deployment: final testing and bug fixes were performed, the application was containerized with a Dockerfile, 
the image was pushed to Docker Hub, and deployment testing verified that frontend and backend integrate correctly in a containerized environment. 
Documentation and the final presentation were also prepared in this sprint.

🔗 [Sprint 4 Planning](Documents/Sprint_Reports/Sprint4/Sprint_4_Planning_Report.md)
🔗 [Sprint 4 Review](Documents/Sprint_Reports/Sprint4/Sprint_4_Review_Report.md)

---

## Sprint 5 – UI Localization & Kubernetes Deployment

Sprint 5 implemented UI localization and runtime language switching: all UI text was externalized into resource bundles, a language selector was added, 
and support for English, Nepali, Burmese, Finnish and Sinhala (including non-Latin scripts) was implemented and tested. 
Docker configuration was updated to include localization resources; the app can switch languages without restarting.

🔗 [UI Localization Details](Documents/Localization/UI-localization.md)

🔗 [Sprint 5 Planning](Documents/Sprint_Reports/Sprint5/Sprint_5_Planning_Report.md)
🔗 [Sprint 5 Review](Documents/Sprint_Reports/Sprint5/Sprint_5_Review_Report.md)

---

## Sprint 6 – Database Localization

Sprint 6 moved localization into the data layer and improved code quality: the database schema and JPA entities were updated to store multilingual content with UTF-8 encoding, 
and SonarQube-driven static analysis guided focused refactoring to reduce complexity and duplication. 
The team also produced an acceptance test plan and updated ER/UML diagrams to reflect localization changes.

🔗 [Updated ER Diagram](Documents/Diagrams/08-updated-er-diagram.png)
🔗 [Updated Relational Schema](Documents/Diagrams/09-updated-relational-schema.png)

🔗 [Database Localization Strategy](Documents/Localization/DB-localization.md)
🔗 [Database Localization Implementation](Documents/Localization/DB-localization-implementation.md)

🔗 [Sprint 6 Planning](Documents/Sprint_Reports/Sprint6/Sprint_6_Planning_Report.md)
🔗 [Sprint 6 Review](Documents/Sprint_Reports/Sprint6/Sprint_6_Review_Report.md)

---

## Sprint 7 – Quality Assurance

Sprint 7 concentrated on validation and acceptance: the team prepared a formal test plan, executed final unit tests, ran SonarQube analysis via Jenkins, 
and carried out heuristic evaluation and user acceptance testing (UAT). A bug-tracking table was maintained and all findings were addressed to ensure the application met acceptance criteria for submission.

🔗 [Quality Assurance Summary](Documents/Reports/Quality_Assurance_Summary.pdf)

🔗 [Sprint 7 Planning](Documents/Sprint_Reports/Sprint7/Sprint_7_Planning_Report.md)
🔗 [Sprint 7 Review](Documents/Sprint_Reports/Sprint7/Sprint_7_Review_Report.md)

---

## Sprint 8 – Documentation & Finalization

Sprint 8 (finalization) focused on finishing documentation, polishing the UI and codebase, preparing final deliverables and the project presentation.
The team consolidated technical reports, user guides, architecture diagrams and test artifacts so the project is ready for submission and demonstration.

---

## How to Run the Project

### Prerequisites
- Java 21
- Git
- Maven
- Docker or Docker Compose
- MariaDB (if not using Docker)

### Steps

1. Clone the repository
```bash
git clone https://github.com/SandipFromPokhara/software-engineering-project.git
cd software-engineering-project
```

2. Run the application using Docker:
   ```bash
   docker-compose up --build

---

## Testing Instructions

- Run unit tests:
  ```bash
  mvn test

- View test coverage:
  - Located at: /target/site/jacoco/index.html
- Static code analysis:
  - Available via SonarQube dashboard
  
---

## Repository Structure
    /Documents → Project documentation (design docs, sprint reports, user guides and other project artifacts)
    /src       → Application source code (Java sources, FXML, resources) and tests

**Repository `src/` layout**

- `src/main/java` contains the production Java code organized by feature and layer. 
- The `controller` package holds JavaFX controllers that wire FXML views to application logic and handle UI events. 
- The `services` package implements core business logic and coordinates operations between the data layer and controllers. 
- Persistence is split across `dao` (Data Access Objects) for CRUD operations and `entity` which defines JPA entity classes mapping to database tables.
- `datasource` contains database connection and initialization code. 
- The `model` package contains DTOs and domain models used to transfer data between layers, while `session` provides session or context-handling utilities for the currently authenticated user. 
- `security` includes authentication and authorization helpers (e.g., BCrypt usage) and related security utilities.
- `util` is for general-purpose helper classes shared across the codebase.
- `view` contains class that extends Application and overrides `void start(Stage stage)`method. 
- The `org/example` package is used for Main class that launches the application UI.

- `src/main/resources` stores runtime resources required by the application. 
- The `FXML` folder contains JavaFX FXML layout files, `css` holds stylesheets, 
`Images` stores image assets, and `i18n` contains resource bundles and localization files for supported languages. 
- `logback.xml` configures logging and `META-INF` contains metadata such as persistence configuration.

- `src/test/java` mirrors the main package layout for unit and integration tests. It includes test classes for controllers, DAOs, services and helpers.

---

## Technologies Used

- **Frontend:** `JavaFX (FXML + SceneBuilder)`
- **Backend:** `Java 21`, `JPA/Hibernate`
- **Database:** `MariaDB`
- **DevOps:** `Docker`, `JaCoCo`, `JUnit`, `Jenkins`, `Kubernetes (Minikube)`, `Maven`
- **Quality Assurance:** `SonarQube`, `SpotBugs`, `PMD`
- **Project Management:** `Git (feature branches)`, `Trello`

---

## DevOps Pipeline

> High-level overview of CI/CD, testing, and containerization stages.

![NoteVault DevOps Pipeline](Documents/assets/notevault-pipeline.gif)

---

## Authors

### Team Members

- Dinal Maha Vidanelage
- Sandip Ranjit
- Swostika Lama
- Twe He Gam Aung

Course: Software Engineering Project 1 & 2

Duration: 13.01.2026 - 06.05.2026 (8 Sprints)

---

## 🔗 Additional Resources

- **GitHub repository:**
    
    https://github.com/SandipFromPokhara/software-engineering-project.git

- **Trello workspace:**

    https://trello.com/w/sep1_team9/home

- **Detailed DevOps process, project management:** 

    [Related Files](Documents/Related_Files)

- **Additional UML diagrams:**

    [Diagrams](Documents/Diagrams)