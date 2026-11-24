package model;

import java.util.Objects;

public class SpecializedStation extends Workstation {
    private final String workloadFocus;
    private final String primaryTool;
    private final boolean upsBacked;

    public SpecializedStation(
            String stationId,
            String label,
            String zone,
            WorkstationStatus status,
            String workloadFocus,
            String primaryTool,
            boolean upsBacked) {
        super(stationId, label, zone, status);
        if (workloadFocus == null || workloadFocus.isBlank()) {
            throw new IllegalArgumentException("Workload focus cannot be blank.");
        }
        if (primaryTool == null || primaryTool.isBlank()) {
            throw new IllegalArgumentException("Primary tool cannot be blank.");
        }
        this.workloadFocus = workloadFocus.trim();
        this.primaryTool = primaryTool.trim();
        this.upsBacked = upsBacked;
    }

    public String getWorkloadFocus() {
        return workloadFocus;
    }

    public String getPrimaryTool() {
        return primaryTool;
    }

    public boolean isUpsBacked() {
        return upsBacked;
    }

    @Override
    public String getStationCategory() {
        String powerTag = upsBacked ? "UPS-Backed" : "Grid";
        return String.format("Specialized: %s (%s, %s)", workloadFocus, primaryTool, powerTag);
    }

    @Override
    public String toString() {
        return Objects.toString(super.toString()) + " | Tool: " + primaryTool;
    }
}
