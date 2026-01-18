## Project Overview
NoteVault is a Java-based digital note-taking and annotation desktop application developed as a part of *Software Engineering Project 1* course.

The project focuses on applying DevOps practices, Agile development, automated testing, and CI/CD pipelines.

## Technology Stack
The selected technology stack fully satisfies the course requirements and supports all the planned functionalities of the application.

### Frontend
- JavaFX
- SceneBuilder

**Rationale:**

JavaFX provides a structured and maintainable framework for building desktop user interfaces using Java.
SceneBuilder allows visual UI design, and separation of UI and application logic.
This approach supports clean code practices and improves maintainability.

### Backend
- Java

**Rationale:**

Java is the primary programming language required by the course. 
It offers strong object-oriented principles, extensive library support, and seamless integration with testing, build automation, and CI/CD tools used throughout the project.

### Database
- MariaDB

**Rationale:**

MariaDB is a relational database system that supports structured data storage and persistence. 
It integrates well with Java applications and allows reliable storage, retrieval, and modification of data. 
Its use ensures that all data-related functionalities are fully supported without limitations.

### Additional Tools and Frameworks

- IntelliJ IDEA - Integrated Development Environment

- Maven – Dependency management and build automation

- JUnit – Unit testing framework

- JaCoCo – Code coverage analysis

- Jenkins – Continuous integration and automated testing

- Docker – Application packaging and environment consistency

- Kubernetes (Minikube) - local container orchestration and experimentation

- GitHub – Version control and collaboration

- Trello – Agile project management and sprint tracking

- Figma – UI/UX prototyping

**Rationale:**

These tools collectively support automation, testing, quality assurance, and DevOps workflows, which are the core focus of the course.

## DevOps and Automation

The project follows DevOps principles by integrating automated build, testing, and packaging processes.
Jenkins pipelines are used to automate compilation, unit testing, and code coverage analysis.
Docker is used to package the application into a consistent runtime environment, ensuring reliable deployment across different systems.
Kubernetes is used in local development environment to demonstrate basic orchestration concepts and is not intended for production-scale deployment in this project.

