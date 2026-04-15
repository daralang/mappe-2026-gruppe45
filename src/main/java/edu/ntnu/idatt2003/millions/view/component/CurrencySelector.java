package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.CurrencyManager;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * A reusable currency selector component.
 *
 * <p>Displays a {@link ComboBox} populated with currencies from {@link CurrencyManager}.
 * When the user changes the selection, {@link CurrencyManager} is updated
 * automatically so the chosen currency applies across the entire application.
 *
 * <p>Follows the same component pattern as {@link LanguagePicker}.
 *
 * <p>Example usage:
 * <pre>
 *   CurrencySelector selector = new CurrencySelector();
 *   selector.setDisable(true); // disable until a file is chosen
 * </pre>
 */
public class CurrencySelector extends ComboBox<Currency> {

    /**
     * Creates a currency selector populated with all currencies
     * defined in {@link edu.ntnu.idatt2003.millions.util.SupportedCurrency}.
     * Defaults to the currency currently active in {@link CurrencyManager}.
     */
    public CurrencySelector() {
        List<Currency> currencies = CurrencyManager.getSupportedCurrencies();

        setItems(FXCollections.observableArrayList(currencies));
        setConverter(buildConverter());
        setValue(CurrencyManager.get());
        setOnAction(e -> {
            if (getValue() != null) {
                CurrencyManager.setCurrency(getValue());
            }
        });
    }

    /**
     * Builds a {@link StringConverter} that displays the ISO 4217 currency code.
     *
     * @return a converter between {@link Currency} and its currency code string
     */
    private StringConverter<Currency> buildConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Currency currency) {
                return currency == null ? "" : currency.getCurrencyCode();
            }

            @Override
            public Currency fromString(String code) {
                return Currency.getInstance(code);
            }
        };
    }
}