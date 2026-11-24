package storage;

import model.Booking;
import model.BookingStatus;
import model.GeneralPc;
import model.SpecializedStation;
import model.WaitlistEntry;
import model.Workstation;
import model.WorkstationStatus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ScheduleRepository {
    private static final String DELIMITER = "\t";
    private final Path workstationsFile;
    private final Path bookingsFile;
    private final Path waitlistFile;

    public ScheduleRepository(Path dataDir) {
        if (dataDir == null) {
            throw new IllegalArgumentException("Data directory cannot be null.");
        }
        this.workstationsFile = dataDir.resolve("workstations.tsv");
        this.bookingsFile = dataDir.resolve("bookings.tsv");
        this.waitlistFile = dataDir.resolve("waitlist.tsv");
    }

    public void saveWorkstations(Map<String, Workstation> workstations) throws StorageException {
        writeLines(workstationsFile, serializeWorkstations(workstations));
    }

    public Map<String, Workstation> loadWorkstations() throws StorageException {
        Map<String, Workstation> map = new LinkedHashMap<>();
        if (!Files.exists(workstationsFile)) {
            return map;
        }
        for (String line : readLines(workstationsFile)) {
            if (line.isBlank()) {
                continue;
            }
            String[] p = line.split(DELIMITER, -1);
            if (p.length < 7) {
                throw new StorageException("Malformed workstation line: " + line);
            }
            String type = p[0];
            String id = p[1];
            String label = p[2];
            String zone = p[3];
            WorkstationStatus status = WorkstationStatus.valueOf(p[4]);

            Workstation station;
            if ("GENERAL".equals(type)) {
                if (p.length < 7) {
                    throw new StorageException("Malformed GeneralPc line: " + line);
                }
                station = new GeneralPc(id, label, zone, status, p[5], Integer.parseInt(p[6]));
            } else if ("SPECIALIZED".equals(type)) {
                if (p.length < 8) {
                    throw new StorageException("Malformed SpecializedStation line: " + line);
                }
                station = new SpecializedStation(
                        id, label, zone, status, p[5], p[6], Boolean.parseBoolean(p[7]));
            } else {
                throw new StorageException("Unknown workstation type: " + type);
            }
            map.put(station.getStationId(), station);
        }
        return map;
    }

    public void saveBookings(List<Booking> bookings) throws StorageException {
        List<String> lines = new ArrayList<>();
        if (bookings != null) {
            for (Booking b : bookings) {
                lines.add(String.join(DELIMITER,
                        clean(b.getBookingId()),
                        clean(b.getStudentId()),
                        clean(b.getStationId()),
                        b.getStartTime().toString(),
                        b.getEndTime().toString(),
                        b.getStatus().name()));
            }
        }
        writeLines(bookingsFile, lines);
    }

    public List<Booking> loadBookings() throws StorageException {
        List<Booking> bookings = new ArrayList<>();
        if (!Files.exists(bookingsFile)) {
            return bookings;
        }
        for (String line : readLines(bookingsFile)) {
            if (line.isBlank()) {
                continue;
            }
            String[] p = line.split(DELIMITER, -1);
            if (p.length < 6) {
                throw new StorageException("Malformed booking line: " + line);
            }
            bookings.add(new Booking(
                    p[0],
                    p[1],
                    p[2],
                    LocalDateTime.parse(p[3]),
                    LocalDateTime.parse(p[4]),
                    BookingStatus.valueOf(p[5])));
        }
        return bookings;
    }

    public void saveWaitlist(List<WaitlistEntry> entries) throws StorageException {
        List<String> lines = new ArrayList<>();
        if (entries != null) {
            for (WaitlistEntry e : entries) {
                lines.add(String.join(DELIMITER,
                        clean(e.getEntryId()),
                        clean(e.getStudentId()),
                        clean(e.getStationId()),
                        e.getRequestedStart().toString(),
                        e.getRequestedEnd().toString(),
                        e.getJoinedAt().toString()));
            }
        }
        writeLines(waitlistFile, lines);
    }

    public List<WaitlistEntry> loadWaitlist() throws StorageException {
        List<WaitlistEntry> entries = new ArrayList<>();
        if (!Files.exists(waitlistFile)) {
            return entries;
        }
        for (String line : readLines(waitlistFile)) {
            if (line.isBlank()) {
                continue;
            }
            String[] p = line.split(DELIMITER, -1);
            if (p.length < 6) {
                throw new StorageException("Malformed waitlist line: " + line);
            }
            entries.add(new WaitlistEntry(
                    p[0],
                    p[1],
                    p[2],
                    LocalDateTime.parse(p[3]),
                    LocalDateTime.parse(p[4]),
                    LocalDateTime.parse(p[5])));
        }
        return entries;
    }

    private List<String> serializeWorkstations(Map<String, Workstation> workstations) {
        List<String> lines = new ArrayList<>();
        if (workstations == null) {
            return lines;
        }
        for (Workstation w : workstations.values()) {
            if (w instanceof GeneralPc pc) {
                lines.add(String.join(DELIMITER,
                        "GENERAL",
                        clean(pc.getStationId()),
                        clean(pc.getLabel()),
                        clean(pc.getZone()),
                        pc.getStatus().name(),
                        clean(pc.getOperatingSystem()),
                        Integer.toString(pc.getRamGb())));
            } else if (w instanceof SpecializedStation spec) {
                lines.add(String.join(DELIMITER,
                        "SPECIALIZED",
                        clean(spec.getStationId()),
                        clean(spec.getLabel()),
                        clean(spec.getZone()),
                        spec.getStatus().name(),
                        clean(spec.getWorkloadFocus()),
                        clean(spec.getPrimaryTool()),
                        Boolean.toString(spec.isUpsBacked())));
            }
        }
        return lines;
    }

    private List<String> readLines(Path path) throws StorageException {
        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException e) {
            throw new StorageException("Failed to read file " + path, e);
        }
    }

    private void writeLines(Path path, List<String> lines) throws StorageException {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(
                    path,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed to write file " + path, e);
        }
    }

    private String clean(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\t", " ").replace("\r", " ").replace("\n", " ").trim();
    }
}
