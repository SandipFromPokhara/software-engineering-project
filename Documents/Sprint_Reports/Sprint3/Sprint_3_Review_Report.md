# Software Engineering Project 1 - Sprint 3 Review Report

## Sprint 3 Review – NoteVault

- **Project**: NoteVault = Digital Note-taking and Annotation Tool.
- **Sprint Duration**: 10.02.2026 - 03.03.2026.
- **Team Members**: Sandip Ranjit, Twe He Gam Aung, Dinal Maha Vidanelage and Swostika Lama.
- **Scrum Master**: Dinal Maha Vidanelage.

---

### Sprint 3 Goal

- Extend the functional prototype with advanced note management features.
- Integrate Jenkins CI/CD pipeline for automated build and testing.
- Enhance automated testing with comprehensive unit tests and code coverage analysis.
- Create and test Docker image locally using Docker Desktop.
- Prepare the application for functional review demonstration.
- Fix bugs and optimize application performance.

---

### Completed User Stories / Tasks

**Extended Features**
- Implemented Text Modification features for note text manipulation.
- Developed Undo/Redo operations for note editing.
- Created PDF export feature for saving notes in PDF format.
- Built Tag/Label system for organizing notes by categories.
- Implemented Switch/Toggle Theme feature for selecting dark or light mode.

**CI/CD Integration**
- Installed and configured Jenkins with required plugins.
- Created Jenkins pipeline with automated build stages.
- Successfully integrated code checkout, Maven build, JUnit tests, and JaCoCo report generation stages.

**Testing & Code Coverage**
- Extended unit test suite to cover all new features.
- Wrote tests for edge cases and error scenarios.
- Integrated JaCoCo for comprehensive code coverage analysis.
- Achieved 53% code coverage across the application.
- All unit tests passing successfully in both local environment and Jenkins pipeline.

**Docker Containerization**
- Created Dockerfile with appropriate base image for JavaFX application.
- Configured application properties for containerized environment.
- Successfully built Docker image locally.
- Tested Docker image on Docker Desktop with database connectivity.

**Bug Fixing & Optimization**
- Resolved bugs identified during Sprint 2 testing.
- Optimized database queries for improved performance.
- Refactored code to follow clean code principles.
- Conducted regression testing to ensure no new bugs introduced.

**Project Management**
- Updated GitHub repository with commits, Jenkinsfile, and Dockerfile.
- Maintained Trello board with task progress and completion status.
- Updated README file.

---

### Sprint 3 Demo Summary

During the Sprint 3 review, the team demonstrated:
- Extended features: Text Modification, Undo/Redo, PDF Export, Tag/Label, and Toggle Theme functionality.
- Working Jenkins CI/CD pipeline with automated build, test, and coverage report stages.
- JaCoCo code coverage report showing 53% coverage.
- Docker image(backend + frontend) running successfully on Docker Desktop.
- Improved application performance and bug fixes.
- End-to-end functionality of the NoteVault application.

This demonstration showed significant progress in both feature implementation and DevOps practices.

---

### What Went Well

- All planned extended features were successfully implemented.
- Jenkins CI/CD pipeline integration worked smoothly after initial setup.
- Team collaboration improved with daily stand-ups and effective task distribution.
- Docker image creation and local testing completed without major issues.
- Code coverage increased significantly with comprehensive unit tests.
- Bug fixing and performance optimization improved application stability.

---

### What Could Be Improved

- Jenkins setup took longer than expected due to configuration challenges.
- Need to increase code coverage further to reach 80% threshold in some modules.
- Some features required multiple iterations to match requirements.
- More time should be allocated for end-to-end testing in future sprints.

---

### Challenges Faced

- Jenkins plugin configuration and pipeline debugging required additional learning time.
- JavaFX TextArea limitations for rich text formatting in PDF export.
- Docker networking configuration for database connectivity.
- Balancing feature implementation with comprehensive testing.

---

### Daily Scrum (Stand-up Meetings)

- Our team had daily discussions on WhatsApp and Google Meet.
- Updates included progress on extended features, Jenkins pipeline configuration, JaCoCo integration, Docker image creation, unit testing, and bug fixes.
- Blockers and challenges were discussed and resolved collaboratively.

---

### Team Contributions

| Team Members          | Tasks                                                                                                                      | 
|-----------------------|----------------------------------------------------------------------------------------------------------------------------|
| Dinal Maha Vidanelage | Scrum master, implement undo/redo feature and font size, create unit test, check Jenkins pipeline and create docker image. |
| Sandip Ranjit         | Fix bugs, create Docker image, implement Notebook Dashboard, tag/label and toggle theme features, check Jenkins pipeline.  |
| Swostika Lama         | implement new feature (export) on the app, write unit testing, jenkins pipeline integration, create docker image.          |
| Twe He Gam Aung       | Implement User Dashboard, Design UI for text editing feature, implement bullet list and number list, and testing.          |

---

### Next Sprint Focus (Sprint 4)

- Choose scrum master for Sprint 4 (Twe He Gam Aung).
- Review Sprint 3 accomplishments and lessons learned.
- Plan product backlog for Sprint 4.
- Finalize all application features and conduct thorough testing.
- Push Docker image to Docker Hub for public access.
- Test deployed image in suitable environment (Docker Play).
- Prepare final presentation and demonstration.

---