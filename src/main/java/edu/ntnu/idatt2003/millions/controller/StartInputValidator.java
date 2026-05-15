package edu.ntnu.idatt2003.millions.controller;

import java.io.File;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * Validates and converts input from the start screen.
 *
 * <p>This utility keeps simple UI-input validation out of {@link StartController}
 * while staying in the controller layer. It does not contain domain business rules.</p>
 *
 * <p>Provides format validators for player name and starting capital that return
 * i18n error keys, and path validators for stock and save files.</p>
 */
final class StartInputValidator {

    private StartInputValidator() {
        // Utility class - should not be instantiated
    }

    /**
     * Validates a file path from UI input and converts it to a {@link File}.
     *
     * @param filePath the file path text entered or selected by the user
     * @param message  the exception message used when the path is missing
     * @return a file representing the validated path
     * @throws IllegalArgumentException if the path is null or blank
     */
    static File requireFilePath(String filePath, String message) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return new File(filePath);
    }

    /**
     * Validates the format of a player name string without checking presence.
     * Returns an i18n error key if the value is present but invalid, or empty if valid or blank.
     *
     * <p>A non-blank name must be at least 3 characters long and contain at least
     * one letter (including Norwegian characters æøåÆØÅ).</p>
     *
     * @param name the player name entered by the user
     * @return an {@link Optional} containing an i18n error key, or empty if the input is valid or blank
     */
    static Optional<String> validateNameFormat(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        if (name.length() < 3) {
            return Optional.of("error.name.too.short");
        }
        if (!name.matches(".*[a-zA-ZæøåÆØÅ].*")) {
            return Optional.of("error.name.invalid");
        }
        return Optional.empty();
    }

    /**
     * Validates the format of a capital string without checking presence.
     * Returns an i18n error key if the value is present but invalid, or empty if valid or blank.
     *
     * @param capital the capital text entered by the user
     * @return an {@link Optional} containing an i18n error key, or empty if the input is valid or blank
     */
    static Optional<String> validateCapitalFormat(String capital) {
        if (capital == null || capital.isBlank()) {
            return Optional.empty();
        }
        try {
            BigDecimal parsed = new BigDecimal(capital);
            if (parsed.compareTo(BigDecimal.ZERO) <= 0) {
                return Optional.of("error.capital.zero");
            }
        } catch (NumberFormatException e) {
            return Optional.of("error.capital.invalid");
        }
        return Optional.empty();
    }

    /**
     * Validates a custom stock file path and requires it to point to a CSV file.
     *
     * @param filePath the stock file path selected by the user
     * @return a CSV file representing the validated path
     * @throws IllegalArgumentException if the path is missing or does not end with {@code .csv}
     */
    static File requireCsvFilePath(String filePath) {
        File file = requireFilePath(filePath, "Stock file must be selected");
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Stock file must be a CSV file");
        }
        return file;
    }
}
