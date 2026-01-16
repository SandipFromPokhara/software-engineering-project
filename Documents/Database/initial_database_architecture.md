# Initial Database Architecture – NoteVault

## Database Purpose
The database is designed to support basic note management functionality, including creating, viewing, editing, and deleting notes.

## Database Name
notevault_db

## Tables

### notes

| Field | Type | Description                     |
|------|-----|---------------------------------|
| id | INT (PK) | Unique identifier for each note |
| title | VARCHAR(255) | Title of the note               |
| content | TEXT | Contents of the note            |
| created_at | TIMESTAMP | Note creation time              |
| updated_at | TIMESTAMP | Last modification time          |

## Notes
This schema represents the initial design and will be refined in later sprints as new requirements are identified.

## Future Extensions

In future iterations of the project, the database design may be extended to support:
- User authentication and authorization
- Note sharing and tagging

These features are outside the scope of Software Engineering Project 1 and are not implemented in the current version.
