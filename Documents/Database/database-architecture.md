# Initial Database Architecture – NoteVault

## Database Purpose
The database is designed to support basic note management functionality, including creating, viewing, editing, and deleting notes.

## Database Name
notevault_db

## Tables

### users

| Field        | Type            | Nullable | Unique | Description                 |      
|--------------|-----------------|----------|--------|-----------------------------|
| id           | INT (PK)        | NO       | YES    | Unique user ID              |
| firstName    | VARCHAR(255)    | NO       | NO     | User's first name           |
| lastName     | VARCHAR(255)    | NO       | NO     | User's last name            |
| email        | VARCHAR(255)    | NO       | YES    | User's email address        |
| username     | VARCHAR(255)    | NO       | YES    | Login name                  |
| passwordHash | VARCHAR(255)    | NO       | NO     | Hashed password             |
| createdAt    | DATETIME        | NO       | NO     | DEFAULT CURRENT_TIMESTAMP   |
| updatedAt    | DATETIME        | YES      | NO     | ON UPDATE CURRENT_TIMESTAMP |

**Indexes & Keys**

- PRIMARY KEY: `id`

- UNIQUE INDEX: `email`

- UNIQUE INDEX: `username`

---

### notebooks

| Field     | Type         | Nullable | Description                                                                                     |
|-----------|--------------|----------|-------------------------------------------------------------------------------------------------|
| id        | INT (PK)     | NO       | Unique identifier for each Notebook                                                             |
| title     | VARCHAR(255) | NO       | Title of the Notebook                                                                           |
| user_id   | INT (FK)     | NO       | Owner of the notebook; FK references users.id. Deleting the user cascades deletion of notebooks |
| createdAt | DATETIME     | NO       | DEFAULT CURRENT_TIMESTAMP                                                                       |
| updatedAt | DATETIME     | YES      | ON UPDATE CURRENT_TIMESTAMP                                                                     |

**Indexes & Keys**

- PRIMARY KEY: `id`

- INDEX: `user_id`

- FOREIGN KEY: `user_id` -> `users.id` ON DELETE CASCADE

---

### notes

| Field       | Type          | Nullable | Description                                                                                              |
|-------------|---------------|----------|----------------------------------------------------------------------------------------------------------|
| id          | INT (PK)      | NO       | Unique identifier for each note                                                                          |
| title       | VARCHAR(255)  | NO       | Title of the note                                                                                        |
| content     | TEXT (65535)  | NO       | Contents of the note                                                                                     |
| annotation  | VARCHAR (255) | YES      | Annotations for note                                                                                     |
| notebook_id | INT (FK)      | NO       | Notebook containing this note; references notebooks.id. Deleting the notebook cascades deletion of notes |
| createdAt   | DATETIME      | NO       | DEFAULT CURRENT_TIMESTAMP                                                                                |
| updatedAt   | DATETIME      | YES      | ON UPDATE CURRENT_TIMESTAMP                                                                              |

**Indexes & Keys**

- PRIMARY KEY: `id`

- INDEX: `notebook_id`

- FOREIGN KEY: `notebook_id` -> `notebook.id` ON DELETE CASCADE

---

### tags

| Field      | Type        | Nullable | Unique | Description                    |
|------------|-------------|----------|--------|--------------------------------|
| id         | INT (PK)    | NO       | YES    | Unique identifier for each tag |
| tag_name   | VARCHAR(15) | NO       | YES    | Name of the tag                |
| createdAt  | DATETIME    | NO       | NO     | DEFAULT CURRENT_TIMESTAMP      |
| updatedAt  | DATETIME    | YES      | NO     | ON UPDATE CURRENT_TIMESTAMP    |

**Indexes & Keys**

- PRIMARY KEY: `id`

- UNIQUE INDEX: `tag_name`

---

### note_tags

| Field    | Type       | Nullable | Description                                                       |
|----------|------------|----------|-------------------------------------------------------------------|
| note_id  | INT (FK)   | NO       | References notes.id. Identifies the note associated with the tag. |
| tag_id   | INT (FK)   | NO       | References tags.id. Identifies the tag associated with the note.  |

**Indexes & Keys**

- COMPOSITE PRIMARY KEY: `(note_id, tag_id)` → prevents duplicates

- INDEX: `tag_id` → reverse lookup (all notes with a given tag)

- FOREIGN KEY: `note_id` → `notes.id` ON DELETE CASCADE

- FOREIGN KEY: `tag_id` → `tags.id` ON DELETE CASCADE

---

## Entity Relationships
```
User (0..N) ────── (1) Notebook
Notebook (0..N) ── (1) Note
Note (0..N) ────── (0..N) Tag
```

---

## Cascading Rules

- Deleting a User → Deletes associated Notebooks

- Deleting a Notebook → Deletes associated Notes

- Deleting a Note → Deletes entries in note_tags, Tags remain

- Deleting a Tag → Deletes entries in note_tags, Notes remain

---

## Notes
This schema represents the design of NoteVault.

## Future Extensions

In future iterations of the project, the database design may be extended to support:
- Note sharing

These features are outside the scope of Software Engineering Project 1 and are not implemented in the current version.
