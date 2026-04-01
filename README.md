# NoteVault

**NoteVault** is a JavaFX desktop application for hierarchical note management.
It was developed as part of the **Software Engineering Project 1** course, demonstrating modern DevOps practices alongside Java application development.

While the application provides secure notebook and note management, the primary focus of this project is the DevOps lifecycle, including CI/CD automation, testing strategy, containerization, and structured team collaboration.

---

## Summary

- Hierarchical notebook & note management
- Secure authentication with BCrypt
- Responsive GUI using JavaFX background threads
- CI/CD pipeline with Jenkins
- Containerization with Docker

---

## Localization (language switch)

- Supported languages: English (default), Finnish, Nepali, Burmese, Sinhalese. Strings live in `src/main/resources/i18n/MessagesBundle*.properties`.
- How to switch at runtime:
  1. Launch the app (e.g., `mvn clean javafx:run` or run the packaged JAR).
  2. In the dashboard toolbar use the language dropdown (shows codes like EN/FI/NP/MY/SI) or open the language dialog ("Change language" action).
  3. Pick a language and confirm — UI text updates immediately and the choice is saved in user preferences for the next run.
- To add a new language: create `MessagesBundle_<lang>.properties`, fill all keys (use `MessagesBundle_en.properties` as the baseline), and register it in `LanguageModel` with a locale code.
- Essential localization resources: translation files in `src/main/resources/i18n/`, locale helper `util.Localization`, language registry `model.LanguageModel`, and the selection UI in `controller.ViewDashboardController` / `controller.LanguageDialogController`. Human translators or a lightweight localization tool (e.g., POEditor/Weblate) can manage the property files.

---

## Technologies Used

- **Frontend:** `JavaFX (FXML + SceneBuilder)`
- **Backend:** `Java 21`, `JPA/Hibernate`
- **Database:** `MariaDB`
- **DevOps:** `Docker`, `JaCoCo`, `JUnit`, `Jenkins`, `Maven`
- **Project Management:** `Git (feature branches)`, `Trello`

---

## Database Architecture

NoteVault uses a MariaDB database (`notevault_db`) to manage users, notebooks, notes, and tags.
The database is automatically initialized when the application runs; no manual setup is needed.

**Entity Relationships**
```
User (0..N) ────── (1) Notebook
Notebook (0..N) ── (1) Note
Note (0..N) ────── (0..N) Tag
```

**Cascading Rules**

- Deleting a User → Deletes associated Notebooks

- Deleting a Notebook → Deletes associated Notes

- Deleting a Note → Deletes entries in note_tags, Tags remain

- Deleting a Tag → Deletes entries in note_tags, Notes remain

**Note on Database Credentials & Docker**
> The application uses environment variables for database credentials (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`).  
> For a ready-to-run setup, you can use Docker Compose — see [Related Files](Documents/Related_Files/setup-instructions.md) for detailed instructions.

For full table details and diagrams, see [Database Documentation](Documents/Database/database-architecture.md) and [ER & Relational Diagrams](Documents/Diagrams/).

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

## DevOps Pipeline

> High-level overview of CI/CD, testing, and containerization stages.

![NoteVault DevOps Pipeline](Documents/assets/notevault-pipeline.gif)

---

## Useful Links

- **GitHub repository:** [software-engineering-project](https://github.com/SandipFromPokhara/software-engineering-project.git)

- **Trello workspace:** [SEP1_Team9](https://trello.com/w/sep1_team9/home)

- **Detailed DevOps process, project management:** [Related Files](Documents/Related_Files/)

- **Sprint reports and reviews:** [Sprint Documentations](Documents/Sprint_Reports/)