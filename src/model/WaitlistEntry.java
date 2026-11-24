package model;

import java.time.LocalDateTime;

public class WaitlistEntry {
    private final String entryId;
    private final String studentId;
    private final String stationId;
    private final LocalDateTime requestedStart;
    private final LocalDateTime requestedEnd;
    private final LocalDateTime joinedAt;

    public WaitlistEntry(
            String entryId,
            String studentId,
            String stationId,
            LocalDateTime requestedStart,
            LocalDateTime requestedEnd,
            LocalDateTime joinedAt) {
        if (entryId == null || entryId.isBlank()) {
            throw new IllegalArgumentException("Waitlist entry ID cannot be blank.");
        }
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("Student ID cannot be blank.");
        }
        if (stationId == null || stationId.isBlank()) {
            throw new IllegalArgumentException("Station ID cannot be blank.");
        }
        if (requestedStart == null || requestedEnd == null || !requestedEnd.isAfter(requestedStart)) {
            throw new IllegalArgumentException("Requested time range is invalid.");
        }
        if (joinedAt == null) {
            throw new IllegalArgumentException("Joined timestamp cannot be null.");
        }
        this.entryId = entryId.trim();
        this.studentId = studentId.trim();
        this.stationId = stationId.trim();
        this.requestedStart = requestedStart;
        this.requestedEnd = requestedEnd;
        this.joinedAt = joinedAt;
    }

    public String getEntryId() {
        return entryId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStationId() {
        return stationId;
    }

    public LocalDateTime getRequestedStart() {
        return requestedStart;
    }

    public LocalDateTime getRequestedEnd() {
        return requestedEnd;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    @Override
    public String toString() {
        return String.format("[%s] Student %s queued for %s (%s to %s) @ %s",
                entryId, studentId, stationId, requestedStart, requestedEnd, joinedAt);
    }
}
