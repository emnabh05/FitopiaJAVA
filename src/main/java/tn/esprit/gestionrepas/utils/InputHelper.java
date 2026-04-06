package tn.esprit.gestionrepas.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class InputHelper {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Scanner scanner = new Scanner(System.in);

    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Veuillez saisir un entier valide.");
            }
        }
    }

    public Integer readOptionalInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            if (value.isEmpty()) {
                return null;
            }

            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Veuillez saisir un entier valide ou laisser vide.");
            }
        }
    }

    public Double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim().replace(',', '.');

            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                System.out.println("Veuillez saisir un nombre valide.");
            }
        }
    }

    public Double readOptionalDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim().replace(',', '.');

            if (value.isEmpty()) {
                return null;
            }

            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                System.out.println("Veuillez saisir un nombre valide ou laisser vide.");
            }
        }
    }

    public String readRequiredText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }

            System.out.println("Ce champ est obligatoire.");
        }
    }

    public String readOptionalText(String prompt) {
        System.out.print(prompt);
        String value = scanner.nextLine().trim();
        return value.isEmpty() ? null : value;
    }

    public LocalDateTime readDateTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            try {
                return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Format attendu : yyyy-MM-dd HH:mm");
            }
        }
    }
}
