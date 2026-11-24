package storage;

import model.AuditEntry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogStore {
    private static final String DELIMITER = "\t";
    private final Path logPath;

    public AuditLogStore(Path logPath) {
        if (logPath == null) {
            throw new IllegalArgumentException("Log path cannot be null.");
        }
        this.logPath = logPath;
    }

    public void append(AuditEntry entry) throws StorageException {
        if (entry == null) {
            throw new IllegalArgumentException("Audit entry cannot be null.");
        }
        try {
            if (logPath.getParent() != null) {
                Files.createDirectories(logPath.getParent());
            }
            String line = String.join(DELIMITER,
                    sanitize(entry.getEventId()),
                    entry.getTimestamp().toString(),
                    sanitize(entry.getActorId()),
                    sanitize(entry.getAction()),
                    sanitize(entry.getDetails()));

            try (BufferedWriter writer = Files.newBufferedWriter(
                    logPath,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new StorageException("Failed to append audit log entry to " + logPath, e);
        }
    }

    public List<AuditEntry> readAll() throws StorageException {
        List<AuditEntry> entries = new ArrayList<>();
        if (!Files.exists(logPath)) {
            return entries;
        }
        try (BufferedReader reader = Files.newBufferedReader(logPath, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(DELIMITER, -1);
                if (parts.length < 5) {
                    throw new StorageException("Malformed audit log line " + lineNumber + " in " + logPath);
                }
                entries.add(new AuditEntry(
                        parts[0],
                        LocalDateTime.parse(parts[1]),
                        parts[2],
                        parts[3],
                        parts[4]));
            }
            return entries;
        } catch (IOException | RuntimeException e) {
            throw new StorageException("Failed to read audit log from " + logPath, e);
        }
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\t", " ").replace("\r", " ").replace("\n", " ").trim();
    }
}
