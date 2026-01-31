# Initial Database Architecture – NoteVault

## Database Purpose
The database is designed to support basic note management functionality, including creating, viewing, editing, and deleting notes.

## Database Name
notevault_db

## Tables

### users

| Field         | Type         | Description            |      
|---------------|--------------|------------------------|
| id            | INT (PK)     | Unique user ID         |
| firstName     | VARCHAR(255) | User's first name      |
| lastName      | VARCHAR(255) | User's last name       |
| email         | VARCHAR(255) | User's email address   |
| username      | VARCHAR(255) | Login name             |
| password_hash | VARCHAR(255) | Hashed password        |
| created_at    | TIMESTAMP    | User registration time |

### users

| Field     | Type         | Description                         |
|-----------|--------------|-------------------------------------|
| id        | INT (PK)     | Unique identifier for each Notebook |
| title     | VARCHAR(255) | Title of the Notebook               |
| user_id   | INT (FK)     | Relation to user                    |

### notes

| Field      | Type               | Description                     |
|------------|--------------------|---------------------------------|
| id         | INT (PK)           | Unique identifier for each note |
| title      | VARCHAR(255)       | Title of the note               |
| content    | TEXT               | Contents of the note            |
| user_id    | INT (FK, nullable) | Registered user; NULL -> guest  |
| created_at | TIMESTAMP          | Note creation time              |
| updated_at | TIMESTAMP          | Last modification time          |

## Notes
This schema represents the initial design and will be refined in later sprints as new requirements are identified.

## Future Extensions

In future iterations of the project, the database design may be extended to support:
- Note sharing and tagging

These features are outside the scope of Software Engineering Project 1 and are not implemented in the current version.
