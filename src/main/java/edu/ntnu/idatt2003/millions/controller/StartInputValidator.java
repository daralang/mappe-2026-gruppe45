package edu.ntnu.idatt2003.millions.controller;

import java.io.File;
import java.math.BigDecimal;
import java.util.Currency;

/**
 * Validates and converts input from the start screen.
 *
 * <p>This utility keeps simple UI-input validation out of {@link StartController}
 * while staying in the controller layer. It does not contain domain business rules.</p>
 *
 * <p>Provides validators for player name, starting capital and file paths,
 * including a CSV-specific check for stock data files.</p>
 */
final class StartInputValidator {

    private StartInputValidator() {
        // Utility class - should not be instantiated
    }

     /**
     * Validates a player name from UI input.
     *
     * @param name the player name entered by the user
     * @return the trimmed player name
     * @throws IllegalArgumentException if the name is null or blank
     */
    static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be blank");
        }
        return name.trim();
    }

    /**
     * Parses starting capital from UI input.
     *
     * @param capital the capital text entered by the user
     * @return the parsed starting capital
     * @throws IllegalArgumentException if the capital is null, blank, not a valid
     *                                  decimal number, or not strictly greater
     *                                  than zero
     */
    static BigDecimal parseCapital(String capital) {
        if (capital == null || capital.isBlank()) {
            throw new IllegalArgumentException("Starting capital cannot be blank");
        }
        BigDecimal parsed = new BigDecimal(capital);
        if (parsed.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Starting capital must be greater than zero");
        }
        return parsed;
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
     * Validates that a currency has been selected.
     *
     * @param currency the currency selected in the UI
     * @return the validated currency
     * @throws IllegalArgumentException if the currency is null
     */
    static Currency requireCurrency(Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException("Currency must be selected");
        }
        return currency;
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
