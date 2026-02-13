# NoteVault

**NoteVault** is a JavaFX desktop application for hierarchical note management, developed as part of the **Software Engineering Project 1** course.

While the application provides secure notebook and note management, the primary focus of this project is the DevOps lifecycle, including CI/CD automation, testing strategy, containerization, and structured team collaboration.

Developed by a team of 4 students.

---

## Project Objectives

This project demonstrates:

- Clean layered architecture

- Secure authentication implementation

- Background-threaded UI responsiveness (JavaFX Tasks)

- CI/CD pipeline automation with Jenkins

- Automated testing & coverage reporting

- Containerization using Docker

- Kubernetes deployment using Minikube

- Collaborative Git workflow using feature branches

---

## Team

Developed by a team of 4 students as part of:

**Software Engineering Project 1**
Focus: DevOps Process & Automation

**Team members**

- Dinal Maha Vidanelage
- Sandip Ranjit
- Swostika Lama
- Twe He Gam Aung

---

## Branch & Development Workflow

> IMPORTANT: Active development code is located in the `feature-dev` branch.
The `main` branch contains the initial project skeleton.

**Clone the full project:**
``` bash
git clone -b feature-dev https://github.com/SandipFromPokhara/software-engineering-project.git
```

---

## System Architecture

The system follows a Layered Architecture to ensure separation of concerns.

1️⃣ Controller Layer

- JavaFX Controllers manage UI logic.

- Database operations run in background threads using JavaFX Tasks.

- Prevents UI blocking and ensures responsiveness.

2️⃣ Service Layer

- Encapsulates business logic (e.g., `UserService.login()`).

- Prevents direct UI-to-DAO coupling.

3️⃣ DAO Layer

- Implements Repository pattern.

- Uses JPA (Hibernate) for persistence.

- Manages MariaDB interaction.

4️⃣ Entity Layer

Defines relational structure for:

  - `User`
  - `Notebook`
  - `Note`

Notebook

Note

5️⃣ Session Management

- Singleton-based session classes (e.g., `UserSession`)

- Maintains application state across controllers.

---

## Security Implementation

Security was treated as a core requirement.

**Password Security**

- BCrypt hashing

- No plain-text passwords stored

**Validation**

- Email format validation

- Password complexity enforcement

- Empty fields validation

- Client-side validation before persistence

**Secure Configuration**

Database credentials are injected via environment variables:
``` env
DB_USER
DB_PASSWORD
```
No sensitive credentials are stored in source control.

---

## Database Architecture

Database: `notevault_db`

**Entity Relationships**
```
User (0..N) ────── (1) Notebook
Notebook (0..N) ── (1) Note
```

**Cascading Rules**

- Deleting a User → Deletes associated Notebooks

- Deleting a Notebook → Deletes associated Notes

---

## DevOps Pipeline

The project emphasizes CI/CD automation and deployment reproducibility.

** CI/CD
```
                                Developer Commit
                                        │
                                        ▼
                                GitHub (feature-dev branch)
                                        │
                                        ▼
                                Jenkins Pipeline Trigger
                                        │
                                        ▼
                                Maven Build
                                  - Compile
                                  - Dependency resolution
                                        │
                                        ▼
                                 JUnit Test Execution
                                        │
                                        ▼
                                 JaCoCo Coverage Report
                                        │
                                        ▼
                                Build Validation
                                  - Fail on test failure
                                        │
                                        ▼
                                Docker Image Build
                                        │
                                        ▼
                                Docker Container Runtime
                                        │
                                        ▼
                                Kubernetes Deployment (Minikube)
```

---

**CI Stage**

- Automated build using Maven

- Unit testing with JUnit

- Coverage reporting via JaCoCo

- Pipeline fails if tests fail

---

**Containerization**

- Application packaged as Docker image

- Environment variables injected securely

- Ensures consistent runtime environment

---

**Kubernetes Deployment**

- Deployment validated using Minikube

- Demonstrates container orchestration readiness

- Ensures reproducible deployment environment

---

**Testing & Quality Assurance**

- Unit testing with JUnit

- Code coverage analysis using JaCoCo

- Automated validation in CI pipeline

- Fail-fast pipeline design

---

## Project Management & Agile Process

The team followed Agile Scrum methodology throughout the development lifecycle.

**Sprint Structure**

- Total Sprints: **4**

- Sprint Duration: 2 weeks

- Sprint Planning conducted at the beginning of each cycle

- Sprint Review & Retrospective at the end of each sprint

**Scrum Master Rotation**

To ensure shared leadership and equal responsibility:

- The Scrum Master role rotates every sprint.

- Each team member serves as Scrum Master for one sprint.

- Responsibilities included:

    - Sprint planning coordination

    - Task assignment oversight

    - Monitoring progress

    - Facilitating retrospective discussions

This approach strengthens accountability and cross-functional understanding.

**Task Management**

Project tasks were managed using:

**Trello**

Used for:

- Backlog management

- Sprint task tracking

- Priority management

- Workflow visualization

Typical workflow:
```
Backlog → To Do → In Progress → Review → Done
```

** Sprint Overview Table

| Sprint | Duration                | Focus Area                                 | Scrum Master          |
|--------|-------------------------|--------------------------------------------|-----------------------|
| 1      | 13.01.2026 - 27.01.2026 | Planning & Project Setup                   | Sandip Ranjit         |
| 2      | 27.01.2026 - 10.02.2026 | CRUD Operations, Authentication & DB Setup | Swostika Lama         |
| 3      | 10.02.2026 - 03.03.2026 | CI/CD integration                          | Dinal Maha Vidanelage |
| 4      | 03.03.2026 - 10.03.2026 | Containerization & Deployment              | Twe He Gam Aung       |

---

## Tech Stack

Frontend:

- JavaFX (FXML + SceneBuilder)

Backend:

- Java 21

- JPA / Hibernate

- MariaDB

Security:

- BCrypt

DevOps:

- Docker

- JaCoCo

- Jenkins

- JUnit

- Kubernetes (Minikube)

- Maven

---

##  Setup & Run
1️⃣ **Database**

Create:

```
notevault_db
```

2️⃣ **Environment Variables**

Set:
```
DB_USER
DB_PASSWORD
```

3️⃣ **Build**
``` bash
mvn clean install
```

4️⃣ **Run**
``` bash
mvn javafx:run
```

---

