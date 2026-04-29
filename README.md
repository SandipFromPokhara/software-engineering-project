# NoteVault

**NoteVault** is a JavaFX desktop application for hierarchical note management, 
developed across **Software Engineering Project 1 (SEP1)** and continued in **Software Engineering Project 2 (SEP2)**.

While the application delivers secure and structured note management, 
the core focus of the project is the full software engineering lifecycle, 
emphasizing DevOps practices, quality assurance, and collaborative development.

The application supports multi-language localization using Java ResourceBundles, with user-selected language preferences.

---

## Project Evolution

**SEP1 Focus**

- DevOps lifecycle implementation
- CI/CD pipeline
- Image containerization
- Core application development

**SEP2 Focus (Ongoing)**

- Localization support (multi-language UI: Burmese, English, Finnish, Nepali, Sinhala)
- Advanced quality assurance (functional & non-functional testing)
- Improved documentation and maintainability
- Extended DevOps practices and test coverage
- Continued agile development using sprint cycles

---

## Features

- Hierarchical notebook & note management
- Secure authentication using BCrypt
- Responsive GUI using JavaFX background threads
- Automated CI/CD pipeline with Jenkins
- Containerized environment using Docker
- Localization support (introduced in SEP2)
- Kubernetes-based deployment (Minikube)
- Quality assurance

---

## Technologies Used

- **Frontend:** `JavaFX (FXML + SceneBuilder)`
- **Backend:** `Java 21`, `JPA/Hibernate`
- **Database:** `MariaDB`
- **DevOps:** `Docker`, `JaCoCo`, `JUnit`, `Jenkins`, `Kubernetes (Minikube)`, `Maven`
- **Quality Assurance:** `SonarQube`, `SpotBugs`, `PMD`
- **Project Management:** `Git (feature branches)`, `Trello`

---

## Database Architecture

NoteVault uses a MariaDB database (`notevault_db`) to manage users, notebooks, notes, and tags.
The database is automatically initialized when the application runs; no manual setup is needed.

**Note on Database Credentials & Docker**
> The application uses environment variables for database credentials (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`).  
> For a ready-to-run setup, you can use Docker Compose — see [Related Files](Documents/Related_Files/setup-instructions.md) for detailed instructions.

- For full table details, visit [Database Documentation](Documents/Database/database-architecture.md)

- For initial and updated modelling diagrams, visit [UML Diagrams](Documents/Diagrams)

---

## DevOps Pipeline

> High-level overview of CI/CD, testing, and containerization stages.

![NoteVault DevOps Pipeline](Documents/assets/notevault-pipeline.gif)

---

## Team

Developed by a team of 4 students as part of:

**Software Engineering Project 1 & 2**

**Team members**

- Dinal Maha Vidanelage
- Sandip Ranjit
- Swostika Lama
- Twe He Gam Aung

---

## See more

- **GitHub repository:** [software-engineering-project](https://github.com/SandipFromPokhara/software-engineering-project.git)

- **Trello workspace:** [SEP1_Team9](https://trello.com/w/sep1_team9/home)

- **Detailed DevOps process, project management:** [Related Files](Documents/Related_Files)

- **UI and DB Localization Documentations:** [Localization Details](Documents/Localization)

- **Sprint reports and reviews:** [Sprint Documentations](Documents/Sprint_Reports)

- **Sprint 7 summary:** [Sprint 7 Summary](Documents/Related_Files/sprint7-summary.md)