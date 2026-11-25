package ui;

import model.GeneralPc;
import model.SpecializedStation;
import model.Workstation;
import model.WorkstationStatus;
import service.SchedulerService;
import storage.AuditLogStore;
import storage.ScheduleRepository;
import storage.StorageException;

import java.nio.file.Path;
import java.util.Scanner;

public class MainApp {
    public static void main(String[] args) {
        Path dataDir = Path.of("data");
        ScheduleRepository repository = new ScheduleRepository(dataDir);
        AuditLogStore auditLogStore = new AuditLogStore(dataDir.resolve("audit.log"));
        SchedulerService schedulerService = new SchedulerService(repository, auditLogStore);

        try {
            schedulerService.loadState();
            if (schedulerService.getWorkstations().isEmpty()) {
                seedDefaultStations(schedulerService);
                schedulerService.saveState();
            }
        } catch (StorageException e) {
            System.out.println("Warning: Could not load/save initial data (" + e.getMessage() + ")");
        }

        try (Scanner scanner = new Scanner(System.in)) {
            ConsoleMenuHelper ui = new ConsoleMenuHelper(scanner);
            boolean running = true;

            while (running) {
                ui.printHeader("AegisLab — Digital Library Computer Scheduler");
                System.out.println("1. List Workstations");
                System.out.println("2. List Bookings");
                System.out.println("3. Check Student Fairness Score (EAS)");
                System.out.println("4. Save & Exit");

                int choice = ui.promptInt("Choose an option", 1, 4);
                switch (choice) {
                    case 1 -> listWorkstations(schedulerService);
                    case 2 -> listBookings(schedulerService);
                    case 3 -> checkFairness(schedulerService, ui);
                    case 4 -> {
                        saveQuietly(schedulerService);
                        running = false;
                    }
                    default -> {
                    }
                }
            }
        }
    }

    private static void seedDefaultStations(SchedulerService service) throws StorageException {
        service.registerWorkstation(
                new GeneralPc("PC-01", "Library Desk 01", "Main Hall", WorkstationStatus.AVAILABLE, "Ubuntu LTS", 16),
                "SYSTEM");
        service.registerWorkstation(
                new SpecializedStation("LAB-01", "CAD & Sim Station 01", "Quiet Wing", WorkstationStatus.AVAILABLE,
                        "Simulation & Java", "Eclipse / JDK", true),
                "SYSTEM");
    }

    private static void listWorkstations(SchedulerService service) {
        System.out.println("\n--- Workstations ---");
        if (service.getWorkstations().isEmpty()) {
            System.out.println("No workstations registered.");
            return;
        }
        for (Workstation station : service.getWorkstations().values()) {
            System.out.println(station);
        }
    }

    private static void listBookings(SchedulerService service) {
        System.out.println("\n--- Bookings ---");
        if (service.getBookings().isEmpty()) {
            System.out.println("No bookings recorded.");
            return;
        }
        service.getBookings().forEach(System.out::println);
    }

    private static void checkFairness(SchedulerService service, ConsoleMenuHelper ui) {
        String studentId = ui.promptLine("Enter Student ID");
        double score = service.getStudentFairnessScore(studentId);
        System.out.printf("Equitable Access Score for %s: %.1f%n", studentId, score);
    }

    private static void saveQuietly(SchedulerService service) {
        try {
            service.saveState();
            System.out.println("State saved.");
        } catch (StorageException e) {
            System.out.println("Failed to save state: " + e.getMessage());
        }
    }
}
