package model;

import java.time.LocalDateTime;

public class AuditEntry {
    private final String eventId;
    private final LocalDateTime timestamp;
    private final String actorId;
    private final String action;
    private final String details;

    public AuditEntry(
            String eventId,
            LocalDateTime timestamp,
            String actorId,
            String action,
            String details) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID cannot be blank.");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null.");
        }
        if (actorId == null || actorId.isBlank()) {
            throw new IllegalArgumentException("Actor ID cannot be blank.");
        }
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action cannot be blank.");
        }
        this.eventId = eventId.trim();
        this.timestamp = timestamp;
        this.actorId = actorId.trim();
        this.action = action.trim();
        this.details = details == null ? "" : details.trim();
    }

    public String getEventId() {
        return eventId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getActorId() {
        return actorId;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Actor: %s | Action: %s | %s",
                eventId, timestamp, actorId, action, details);
    }
}
