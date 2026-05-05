package util.events;

import entity.entities.NoteEntity;

public record NoteCreatedEvent(NoteEntity note) {}
