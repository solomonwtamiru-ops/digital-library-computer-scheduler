package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Booking {
    private final String bookingId;
    private final String studentId;
    private final String stationId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private BookingStatus status;

    public Booking(
            String bookingId,
            String studentId,
            String stationId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            BookingStatus status) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("Booking ID cannot be blank.");
        }
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("Student ID cannot be blank.");
        }
        if (stationId == null || stationId.isBlank()) {
            throw new IllegalArgumentException("Station ID cannot be blank.");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time cannot be null.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        this.bookingId = bookingId.trim();
        this.studentId = studentId.trim();
        this.stationId = stationId.trim();
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = Objects.requireNonNullElse(status, BookingStatus.CONFIRMED);
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStationId() {
        return stationId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Booking status cannot be null.");
        }
        this.status = status;
    }

    public long getDurationMinutes() {
        return Duration.between(startTime, endTime).toMinutes();
    }

    public boolean overlapsWith(LocalDateTime otherStart, LocalDateTime otherEnd) {
        if (otherStart == null || otherEnd == null || !otherEnd.isAfter(otherStart)) {
            throw new IllegalArgumentException("Invalid comparison time range.");
        }
        return startTime.isBefore(otherEnd) && endTime.isAfter(otherStart);
    }

    @Override
    public String toString() {
        return String.format("[%s] Student %s -> %s (%s to %s) [%s]",
                bookingId, studentId, stationId, startTime, endTime, status);
    }
}
