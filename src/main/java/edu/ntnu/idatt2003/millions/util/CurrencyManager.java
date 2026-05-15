package edu.ntnu.idatt2003.millions.util;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.WeakChangeListener;

/**
 * Manages the active currency for the loaded stock data.
 *
 * <p>Supported currencies are defined in {@link SupportedCurrency}.
 * The active currency is set when a stock file is selected and indicates
 * which currency the stock prices are denominated in.
 *
 * <p>Note: this is the source currency of the stock data, not the display
 * currency used by {@link CurrencyFormatter}. Display currency is currently
 * fixed in the formatter and tracked separately.
 *
 * <p>Currency changes are observable via {@link #currencyProperty()}.
 * Listeners registered on the property are notified automatically by JavaFX
 * when {@link #setCurrency(Currency)} is called. Use
 * {@link javafx.beans.value.WeakChangeListener} to avoid memory leaks in views.
 */
public class CurrencyManager {

    private static final ReadOnlyObjectWrapper<Currency> current =
            new ReadOnlyObjectWrapper<>(Currency.getInstance("USD"));

    private CurrencyManager() {}

    /**
     * Returns a read-only property for the active currency.
     *
     * <p>Register a {@link WeakChangeListener} on this property
     * to observe currency changes without preventing garbage
     * collection of the observer.</p>
     *
     * @return the read-only currency property
     */
    public static ReadOnlyObjectProperty<Currency> currencyProperty() {
        return current.getReadOnlyProperty();
    }

    /**
     * Returns the list of currencies available for selection,
     * derived from {@link SupportedCurrency}.
     *
     * @return an unmodifiable list of supported currencies
     */
    public static List<Currency> getSupportedCurrencies() {
        return Arrays.stream(SupportedCurrency.values())
                .map(SupportedCurrency::getCurrency)
                .toList();
    }

    /**
     * Sets the active currency for the session.
     * All listeners registered on {@link #currencyProperty()} are notified automatically.
     *
     * @param currency the currency to use
     * @throws NullPointerException if currency is null
     */
    public static void setCurrency(Currency currency) {
        Objects.requireNonNull(currency, "Currency cannot be null");
        current.set(currency);
    }

    /**
     * Returns the currently active currency.
     *
     * @return the active currency
     */
    public static Currency get() {
        return current.get();
    }
}
