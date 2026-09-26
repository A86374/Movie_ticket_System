package com.mts.apps.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads console input. Every method reads a whole line, so there is never a
 * leftover newline like the one nextInt() leaves behind.
 */
public final class InputUtil {

    private static final Logger logger = LoggerFactory.getLogger(InputUtil.class);

    private InputUtil() {
    }

    // never logs what was typed, passwords come through here
    public static String readText(Scanner scanner, String prompt) {
        System.out.print("  " + prompt);
        return scanner.nextLine().trim();
    }

    public static int readInt(Scanner scanner, String prompt) {
        while (true) {
            String line = readText(scanner, prompt);
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                logger.warn("Invalid whole number entered: '{}'", line, e);
                System.out.println("  Please enter a whole number");
            }
        }
    }

    public static BigDecimal readDecimal(Scanner scanner, String prompt) {
        while (true) {
            String line = readText(scanner, prompt);
            try {
                return new BigDecimal(line);
            } catch (NumberFormatException e) {
                logger.warn("Invalid decimal entered: '{}'", line, e);
                System.out.println("  Please enter a number, for example 250 or 250.50");
            }
        }
    }

    public static LocalDate readDate(Scanner scanner, String prompt) {
        while (true) {
            String line = readText(scanner, prompt);
            try {
                return LocalDate.parse(line);
            } catch (DateTimeParseException e) {
                logger.warn("Invalid date entered: '{}'", line, e);
                System.out.println("  Please enter the date as yyyy-mm-dd, for example 2026-09-25");
            }
        }
    }

    public static boolean readYes(Scanner scanner, String prompt) {
        String answer = readText(scanner, prompt);
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }
}