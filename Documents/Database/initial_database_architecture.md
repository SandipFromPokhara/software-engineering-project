# Initial Database Architecture – NoteVault

## Database Purpose
The database is designed to support basic note management functionality, including creating, viewing, editing, and deleting notes.

## Database Name
notevault_db

## Tables

### users

| Field        | Type              | Nullable | Unique | Description            |      
|--------------|-------------------|----------|--------|------------------------|
| id           | INT (PK)          | NO       | YES    | Unique user ID         |
| firstName    | VARCHAR(255)      | NO       | NO     | User's first name      |
| lastName     | VARCHAR(255)      | NO       | NO     | User's last name       |
| email        | VARCHAR(255)      | NO       | YES    | User's email address   |
| username     | VARCHAR(255)      | NO       | YES    | Login name             |
| passwordHash | VARCHAR(255)      | NO       | NO     | Hashed password        |
| createdAt    | CURRENT_TIMESTAMP | NO       | NO     | User registration time |
| updatedAt    | CURRENT_TIMESTAMP | YES      | NO     | Last modification time |

### notebooks

| Field     | Type              | Nullable | Description                                                                                     |
|-----------|-------------------|----------|-------------------------------------------------------------------------------------------------|
| id        | INT (PK)          | NO       | Unique identifier for each Notebook                                                             |
| title     | VARCHAR(255)      | NO       | Title of the Notebook                                                                           |
| user_id   | INT (FK)          | NO       | Owner of the notebook; FK references users.id. Deleting the user cascades deletion of notebooks |
| createdAt | CURRENT_TIMESTAMP | NO       | Note creation time                                                                              |
| updatedAt | CURRENT_TIMESTAMP | YES      | Last modification time                                                                          |

### notes

| Field       | Type              | Nullable | Description                                                                                              |
|-------------|-------------------|----------|----------------------------------------------------------------------------------------------------------|
| id          | INT (PK)          | NO       | Unique identifier for each note                                                                          |
| title       | VARCHAR(255)      | NO       | Title of the note                                                                                        |
| content     | TEXT              | NO       | Contents of the note                                                                                     |
| notebook_id | INT (FK)          | NO       | Notebook containing this note; references notebooks.id. Deleting the notebook cascades deletion of notes |
| createdAt   | CURRENT_TIMESTAMP | NO       | Note creation time                                                                                       |
| updatedAt   | CURRENT_TIMESTAMP | YES      | Last modification time                                                                                   |

## Notes
This schema represents the initial design and will be refined in later sprints as new requirements are identified.

## Future Extensions

In future iterations of the project, the database design may be extended to support:
- Note sharing and tagging

These features are outside the scope of Software Engineering Project 1 and are not implemented in the current version.
