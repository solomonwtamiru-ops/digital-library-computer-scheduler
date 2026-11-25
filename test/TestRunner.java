package test;

import model.Booking;
import model.BookingStatus;
import model.GeneralPc;
import model.SpecializedStation;
import model.WorkstationStatus;
import service.AlternativeSuggestion;
import service.SchedulerService;
import service.SchedulingException;
import storage.AuditLogStore;
import storage.ScheduleRepository;
import storage.StorageException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class TestRunner {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        testBookingCreationAndOverlapConflict();
        testCancellationAndWaitlistPromotion();
        testNoShowAndEquitableAccessScore();
        testAlternativeSuggestions();
        testSaveAndLoadPersistence();

        System.out.printf("%nTest Summary: %d passed, %d failed%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static SchedulerService newTestService(Path tempDir) {
        ScheduleRepository repo = new ScheduleRepository(tempDir);
        AuditLogStore audit = new AuditLogStore(tempDir.resolve("audit.log"));
        return new SchedulerService(repo, audit);
    }

    private static void seedStations(SchedulerService service) throws StorageException {
        service.registerWorkstation(
                new GeneralPc("PC-01", "Desk 01", "Main Hall", WorkstationStatus.AVAILABLE, "Ubuntu LTS", 16),
                "TEST");
        service.registerWorkstation(
                new SpecializedStation("LAB-01", "Sim Station 01", "Quiet Wing", WorkstationStatus.AVAILABLE,
                        "Simulation", "JDK / Eclipse", true),
                "TEST");
    }

    private static void testBookingCreationAndOverlapConflict() throws Exception {
        Path dir = Files.createTempDirectory("aegislab-test-1-");
        SchedulerService service = newTestService(dir);
        seedStations(service);

        LocalDateTime start = LocalDateTime.of(2026, 11, 26, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 26, 11, 30);

        Booking first = service.createBooking("STU-001", "PC-01", start, end);
        assertTrue(first.getStatus() == BookingStatus.CONFIRMED, "Booking should start CONFIRMED");

        boolean threwConflict = false;
        try {
            service.createBooking("STU-002", "PC-01", start.plusMinutes(30), end.plusMinutes(30));
        } catch (SchedulingException expected) {
            threwConflict = true;
        }
        assertTrue(threwConflict, "Overlapping booking on same station should throw SchedulingException");
    }

    private static void testCancellationAndWaitlistPromotion() throws Exception {
        Path dir = Files.createTempDirectory("aegislab-test-2-");
        SchedulerService service = newTestService(dir);
        seedStations(service);

        LocalDateTime start = LocalDateTime.of(2026, 11, 26, 13, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 26, 14, 0);

        Booking b1 = service.createBooking("STU-001", "PC-01", start, end);
        service.joinWaitlist("STU-002", "PC-01", start, end, start.minusMinutes(10));

        service.cancelBooking(b1.getBookingId(), "STU-001");

        boolean promoted = service.getBookings().stream().anyMatch(b ->
                b.getStudentId().equals("STU-002")
                        && b.getStationId().equals("PC-01")
                        && b.getStatus() == BookingStatus.CONFIRMED);
        assertTrue(promoted, "Waitlisted student should be promoted when booking is cancelled");
    }

    private static void testNoShowAndEquitableAccessScore() throws Exception {
        Path dir = Files.createTempDirectory("aegislab-test-3-");
        SchedulerService service = newTestService(dir);
        seedStations(service);

        double freshScore = service.getStudentFairnessScore("STU-NEW");
        assertTrue(freshScore == 100.0, "Fresh student with no usage should have EAS 100.0");

        LocalDateTime start = LocalDateTime.of(2026, 11, 26, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 26, 10, 0);
        Booking b = service.createBooking("STU-NO-SHOW", "PC-01", start, end);
        service.markNoShow(b.getBookingId(), "LIBRARIAN");

        double penalizedScore = service.getStudentFairnessScore("STU-NO-SHOW");
        assertTrue(penalizedScore < freshScore, "No-show should reduce student's Equitable Access Score");
    }

    private static void testAlternativeSuggestions() throws Exception {
        Path dir = Files.createTempDirectory("aegislab-test-4-");
        SchedulerService service = newTestService(dir);
        seedStations(service);

        LocalDateTime start = LocalDateTime.of(2026, 11, 26, 15, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 26, 16, 0);
        service.createBooking("STU-001", "PC-01", start, end);

        List<AlternativeSuggestion> alts = service.suggestAlternatives("STU-002", "PC-01", start, end, true);
        assertTrue(!alts.isEmpty(), "Should return alternative stations or shifted slots when preferred slot is busy");
    }

    private static void testSaveAndLoadPersistence() throws Exception {
        Path dir = Files.createTempDirectory("aegislab-test-5-");
        SchedulerService service = newTestService(dir);
        seedStations(service);

        LocalDateTime start = LocalDateTime.of(2026, 11, 26, 11, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 26, 12, 0);
        service.createBooking("STU-005", "LAB-01", start, end);
        service.saveState();

        SchedulerService reloaded = newTestService(dir);
        reloaded.loadState();

        assertTrue(reloaded.getWorkstations().size() == 2, "Should reload all registered workstations");
        assertTrue(reloaded.getBookings().size() == 1, "Should reload saved bookings");
    }

    private static void assertTrue(boolean condition, String message) {
        if (condition) {
            passed++;
            System.out.println("[PASS] " + message);
        } else {
            failed++;
            System.err.println("[FAIL] " + message);
        }
    }
}
