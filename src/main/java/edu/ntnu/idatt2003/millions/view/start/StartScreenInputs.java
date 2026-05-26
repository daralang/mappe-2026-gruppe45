package edu.ntnu.idatt2003.millions.view.start;

import java.util.Currency;

/**
 * Read/write seam for the data the start screen exposes to its controller.
 *
 * <p>Implemented by {@link StartView} and lets {@code StartController} read
 * user input and update path fields without depending on the concrete
 * JavaFX view. Test doubles can implement this interface directly so the
 * controller's start-flow logic can be unit tested without initialising
 * the JavaFX toolkit.</p>
 */
public interface StartScreenInputs {

    /**
     * Returns the trimmed player name entered by the user.
     *
     * @return trimmed player name, never {@code null}
     */
    String getName();

    /**
     * Returns the trimmed starting capital entered by the user.
     *
     * @return trimmed starting capital, never {@code null}
     */
    String getCapital();

    /**
     * Returns the path to the stock data file selected by the user.
     *
     * @return stock file path, or empty string when none is selected
     */
    String getStockFilePath();

    /**
     * Returns the path to the save file selected by the user.
     *
     * @return save file path, or empty string when none is selected
     */
    String getSaveFilePath();

    /**
     * Returns the currency currently selected for the uploaded stock file.
     *
     * @return the selected currency, or {@code null} if none is selected
     */
    Currency getSelectedCurrency();

    /**
     * Updates the displayed stock file path.
     *
     * @param path the absolute file path; {@code null} or blank resets the field
     */
    void setStockFilePath(String path);

    /**
     * Updates the displayed save file path.
     *
     * @param path the absolute file path; {@code null} clears the field
     */
    void setSaveFilePath(String path);
}
