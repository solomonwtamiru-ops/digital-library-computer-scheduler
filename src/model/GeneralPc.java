package model;

public class GeneralPc extends Workstation {
    private final String operatingSystem;
    private final int ramGb;

    public GeneralPc(
            String stationId,
            String label,
            String zone,
            WorkstationStatus status,
            String operatingSystem,
            int ramGb) {
        super(stationId, label, zone, status);
        if (operatingSystem == null || operatingSystem.isBlank()) {
            throw new IllegalArgumentException("Operating system cannot be blank.");
        }
        if (ramGb <= 0) {
            throw new IllegalArgumentException("RAM must be greater than 0 GB.");
        }
        this.operatingSystem = operatingSystem.trim();
        this.ramGb = ramGb;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public int getRamGb() {
        return ramGb;
    }

    @Override
    public String getStationCategory() {
        return "General PC (" + operatingSystem + ", " + ramGb + " GB RAM)";
    }
}
