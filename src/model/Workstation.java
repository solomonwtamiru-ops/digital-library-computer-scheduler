package model;

import java.util.Objects;

public abstract class Workstation {
    private final String stationId;
    private final String label;
    private final String zone;
    private WorkstationStatus status;

    protected Workstation(String stationId, String label, String zone, WorkstationStatus status) {
        if (stationId == null || stationId.isBlank()) {
            throw new IllegalArgumentException("Station ID cannot be blank.");
        }
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Station label cannot be blank.");
        }
        if (zone == null || zone.isBlank()) {
            throw new IllegalArgumentException("Zone cannot be blank.");
        }
        this.stationId = stationId.trim();
        this.label = label.trim();
        this.zone = zone.trim();
        this.status = Objects.requireNonNullElse(status, WorkstationStatus.AVAILABLE);
    }

    public String getStationId() {
        return stationId;
    }

    public String getLabel() {
        return label;
    }

    public String getZone() {
        return zone;
    }

    public WorkstationStatus getStatus() {
        return status;
    }

    public void setStatus(WorkstationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Workstation status cannot be null.");
        }
        this.status = status;
    }

    public boolean isBookable() {
        return status == WorkstationStatus.AVAILABLE;
    }

    public abstract String getStationCategory();

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) - %s [%s]",
                stationId, label, getStationCategory(), zone, status);
    }
}
