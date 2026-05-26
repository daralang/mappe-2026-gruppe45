// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.view.start.component;

import edu.ntnu.idatt2003.millions.util.currency.CurrencyManager;
import edu.ntnu.idatt2003.millions.util.currency.SupportedCurrency;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

import java.util.Currency;
import java.util.List;

/**
 * A reusable currency selector component.
 *
 * <p>Displays a {@link ComboBox} populated with currencies from {@link CurrencyManager}.
 * When the user changes the selection, {@link CurrencyManager} is updated
 * automatically so the chosen currency applies across the entire application.
 * Follows the same component pattern as {@link LanguagePicker}.</p>
 */
public class CurrencySelector extends ComboBox<Currency> {

    /**
     * Creates a currency selector populated with all currencies defined in
     * {@link SupportedCurrency}, defaulting to the currency currently active
     * in {@link CurrencyManager}. Pressing Enter while focused opens the popup.
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
        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER && !isShowing()) {
                show();
                event.consume();
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
