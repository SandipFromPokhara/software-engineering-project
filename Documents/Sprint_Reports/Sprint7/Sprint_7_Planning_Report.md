# Software Engineering Project 2 - Sprint Planning Report

## Sprint Number: 7

**Project:** NoteVault - Digital Note-taking and Annotation Tool  
**Sprint Duration:** 16.04.2026 - 30.04.2026  
**Team:** Dinal Maha Vidanelage, Sandip Ranjit, Swostika Lama, Twe He Gam Aung  
**Scrum Master for Sprint 7:** Dinal Maha Vidanelage

---

## Sprint Goal

The goal of Sprint 7 is to verify that the NoteVault application is functional, usable, and fully aligned with the initial requirements defined by the Product Owner.

Building on the database localization and code quality improvements from Sprint 6, this sprint shifts focus to comprehensive testing, both functional and non-functional. The team will conduct final unit testing, produce a formal test plan, perform heuristic evaluation, and carry out user acceptance testing (UAT). Static code analysis via SonarQube and Jenkins will be validated and documented, and all identified bugs will be tracked and resolved.

By the end of Sprint 7, the team aims to deliver a thoroughly tested, production-ready application supported by complete testing documentation and an up-to-date GitHub repository and Trello board.

---

## Sprint Objectives

**1. Test Plan Creation**
- Define test objectives and scope
- Specify resources and test environment (OS, JDK, MariaDB version, tools)
- List functional and non-functional test tasks
- Map test cases to Sprint 6 acceptance criteria and user stories

**2. Final Unit Testing**
- Execute all existing unit tests after Sprint 6 code cleanup to ensure no regressions
- Validate functionality of all implemented features
- Fix any failing tests and document results
- Ensure JaCoCo coverage remains at an acceptable level

**3. Bug Tracking Table**
- Create and maintain a structured bug/issue tracking table
- Record bug ID, description, severity, status, and assigned developer
- Update table throughout the sprint as issues are identified and resolved

**4. User Stories Review (Trello)**
- Review and refine all user stories in Trello related to the latest software functionality
- Ensure Trello reflects Sprint 7 progress accurately
- Close completed cards and create new cards for any identified issues

**5. Jenkins CI/CD & SonarQube Report**
- Run Jenkins pipeline to generate the static code analysis report
- Ensure successful project build and Docker deployment via Jenkins
- Validate that all SonarQube grades are A or B
- Include SonarQube screenshots in the final report

**6. Heuristic Evaluation (Non-functional)**
- Evaluate the final NoteVault UI against Nielsen's 10 usability heuristics
- Document violations and recommendations per lecturer instructions
- Produce a formal heuristic evaluation report

**7. User Acceptance Testing – UAT (Non-functional)**
- Conduct UAT based on Sprint 6 acceptance criteria using the provided template
- Involve representative users or team members acting as end users
- Document test results and any deviations from expected behaviour

**8. Document Technical Changes (Non-functional)**
- Record any specification or architecture changes identified from test results
- Update ER diagrams or UML models if changes are required
- Commit updated documentation to GitHub /Documents folder

---

## Sprint Backlog

| Task                                        | Story Points |   
|---------------------------------------------|--------------|
| Test Plan Creation                          | 5            | 
| Final Unit Testing & Bug Fixing             | 5            |
| Bug Tracking Table                          | 5            |
| Jenkins CI/CD Run & SonarQube Report        | 3            | 
| Heuristic Evaluation                        | 5            | 
| User Acceptance Testing (UAT)               | 5            |
| Document Technical Changes                  | 3            | 
| Debug & Manual Testing                      | 3            | 

---

## Expected Deliverables

- Formal test plan document (objective, resources, environment, test tasks)
- Final unit test execution results with no regressions (JaCoCo report)
- Bug tracking table documenting all identified issues and their resolution status
- SonarQube static code analysis report with all grades A or B (via Jenkins)
- Successful Jenkins build with Docker deployment
- Heuristic evaluation report
- User Acceptance Test (UAT) results report
- Technical changes documentation (spec and architecture updates if applicable)
- Updated Trello board reflecting Sprint 7 progress
- Updated GitHub repository with all deliverables committed

---

## Team Capacity and Assumptions

**Capacity:** 4 members × 3–4 hours/day × ~8–10 days = approximately **100–120 hours**

**Assumptions:**
- Unit tests may require additional fixes following Sprint 6 refactoring
- Heuristic evaluation will be conducted as a team to ensure consistency
- UAT will follow the template provided by the lecturer
- SonarQube must report all metrics as grade A or B before the sprint ends
- Jenkins pipeline from Sprint 6 is expected to work without major changes
- All documentation changes must be committed to GitHub and reflected in Trello

---

## Definition of Done

A task is considered complete when:

- Test plan is written and covers all functional and non-functional test tasks
- All unit tests pass successfully with no regressions after refactoring
- Bug tracking table is filled with all identified issues and their resolution
- Jenkins pipeline runs successfully and generates the SonarQube report
- SonarQube grades are A or B for all metrics
- Heuristic evaluation is documented and submitted
- UAT is completed using the provided template and results are documented
- Technical specification and architecture changes are documented
- Trello board is fully updated with Sprint 7 tasks and statuses
- GitHub repository is updated with all deliverables and documentation

---