# 📝 How Create Note Works

## Overview
Create Note saves notes to MariaDB for the **logged-in user**. User must login first to save notes.

---

## Flow
```
UI (FXML) → Controller → Service → DAO → Database
```

---

## Files

| File | Purpose |
|------|---------|
| `create_note.fxml` | UI layout |
| `CreateNoteController.java` | Handles UI events |
| `NoteService.java` | Business logic |
| `JpaNoteDao.java` | Database operations |
| `NoteEntity.java` | Data model |

---

## How It Works

### 1. User Login
- User logs in → `UserService` stores the logged-in user
- `NoteService.setCurrentUser(user)` is called automatically

### 2. User Enters Note Data
- **Title** (required)
- **Content** (optional)
- **Annotation** (optional)

### 3. User Clicks Save

**Controller** gets form data and calls service:
```java
NoteEntity note = noteService.createNote(title, content, annotation);
```

**Service** processes:
1. Validates title (cannot be empty)
2. Checks user is logged in
3. Combines content + annotation
4. Gets/creates user's personal notebook
5. Saves note via DAO

**DAO** saves to database:
```java
em.persist(note);  // INSERT into database
```

---

## Notebook Logic

Each user gets their own notebook:
```java
// Find user's notebook or create new one
NoteBookEntity notebook = notebookDao.findByUserId(currentUser.getId());
if (notebook == null) {
    notebook = new NoteBookEntity(currentUser.getFirstName() + "'s Notebook", currentUser);
}
```

---

## Database Tables

**notes**
| Column | Description |
|--------|-------------|
| id | Auto-generated ID |
| title | Note title |
| content | Content + annotations |
| createdAt | Creation timestamp |
| updatedAt | Update timestamp |
| notebook_id | FK to notebook |

**notebooks**
| Column | Description |
|--------|-------------|
| id | Auto-generated ID |
| title | e.g., "John's Notebook" |
| user_id | FK to user |

---

## Key Points

1. **User must be logged in** to save notes
2. **Title is required** - Save button disabled until entered
3. **Personal notebook** - Created automatically per user (e.g., "John's Notebook")
4. **Annotation** appended to content with "--- Annotations ---" separator
5. **Timestamps** auto-set by JPA

---

## Summary

```
Login → Create Note → Validate → Save to User's Notebook → Database
```
