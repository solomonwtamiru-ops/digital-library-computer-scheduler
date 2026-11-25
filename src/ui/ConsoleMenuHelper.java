package ui;

import java.util.Scanner;

public class ConsoleMenuHelper {
    private final Scanner scanner;

    public ConsoleMenuHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    public void printHeader(String title) {
        String line = "=".repeat(Math.max(24, title.length() + 4));
        System.out.println("\n" + line);
        System.out.println("  " + title);
        System.out.println(line);
    }

    public String promptLine(String label) {
        System.out.print(label + ": ");
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    }

    public int promptInt(String label, int min, int max) {
        while (true) {
            String raw = promptLine(label);
            try {
                int value = Integer.parseInt(raw);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Retry until valid integer in range
            }
            System.out.println("Please enter a number between " + min + " and " + max + ".");
        }
    }
}
