## DevOps Pipeline

The project implements a fully automated CI/CD workflow using Jenkins, Maven, Docker, and Kubernetes.

![NoteVault DevOps Pipeline](Documents/assets/notevault-pipeline.gif)

**Pipeline Stages:**
- Feature branch commit (`feature-dev`)
- Jenkins automated build trigger
- Maven compilation & dependency resolution
- JUnit test execution
- JaCoCo coverage reporting
- Docker image build
- Kubernetes deployment (Minikube)
- MariaDB runtime connection

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

**Testing & Quality Assurance**

- Unit testing with JUnit

- Code coverage analysis using JaCoCo

- Automated validation in CI pipeline

- Fail-fast pipeline design