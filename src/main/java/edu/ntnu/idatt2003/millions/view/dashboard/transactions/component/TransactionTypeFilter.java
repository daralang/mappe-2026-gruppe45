package edu.ntnu.idatt2003.millions.view.dashboard.transactions.component;

import edu.ntnu.idatt2003.millions.model.transaction.Purchase;
import edu.ntnu.idatt2003.millions.model.transaction.Sale;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

/**
 * Dropdown filter that narrows the transactions table to purchases,
 * sales, or all types.
 *
 * <p>Lives in the transactions package because the choice set is
 * domain-specific (it knows about {@link Purchase} and {@link Sale}).
 * Mirrors {@code WeekRangeFilter} in shape — extends a JavaFX control,
 * exposes an observable property the caller listens to, and looks
 * after its own localization.</p>
 *
 * <p>Callers read the active filter through {@link #selectedTypeProperty()}:
 * the value is the {@link Transaction} subclass to match against, or
 * {@code null} when no filter is active ("all types"). That lets the
 * caller use a single {@code isInstance} check without caring which
 * dropdown option was picked.</p>
 */
public class TransactionTypeFilter extends ComboBox<TransactionTypeFilter.TypeOption> {

    /**
     * Constructs a new type filter with three options — all, buy, sell —
     * and selects "all" by default so the table starts unfiltered.
     */
    public TransactionTypeFilter() {
        setItems(FXCollections.observableArrayList(
                TypeOption.ALL,
                TypeOption.BUY,
                TypeOption.SELL
        ));
        setValue(TypeOption.ALL);
        getStyleClass().add("type-filter");

        setConverter(new StringConverter<>() {
            /**
             * Renders an option as its localized label so the dropdown
             * reflects the active language without rebuilding the items.
             */
            @Override
            public String toString(TypeOption option) {
                return option == null ? "" : LanguageManager.get(option.labelKey);
            }

            /**
             * Not used — the combo box is not editable, so user input
             * never needs to be parsed back into an option.
             */
            @Override
            public TypeOption fromString(String s) {
                return null;
            }
        });

        // Force the button cell to re-render its current label when the
        // language changes. The converter is re-run by briefly clearing
        // and reassigning the value.
        LanguageManager.addObserver(() -> {
            TypeOption current = getValue();
            setItems(FXCollections.observableArrayList(
                    TypeOption.ALL,
                    TypeOption.BUY,
                    TypeOption.SELL
            ));
            setValue(current);
        });
    }

    /**
     * Returns an observable view of the selected transaction subclass.
     * The value is {@code null} when "all types" is selected.
     *
     * @return observable value carrying the active filter class
     */
    public ObservableValue<Class<? extends Transaction>> selectedTypeProperty() {
        return valueProperty().map(option -> option == null ? null : option.type);
    }

    /**
     * Returns the currently selected transaction subclass, or
     * {@code null} when "all types" is selected.
     *
     * @return the active filter class, or {@code null}
     */
    public Class<? extends Transaction> getSelectedType() {
        TypeOption value = getValue();
        return value == null ? null : value.type;
    }

    /**
     * Internal enumeration of the three dropdown options. Each option
     * carries the i18n key for its label and the {@link Transaction}
     * subclass it filters for ({@code null} for "all types").
     */
    enum TypeOption {
        ALL("transactions.type.all", null),
        BUY("transactions.type.buy", Purchase.class),
        SELL("transactions.type.sell", Sale.class);

        private final String labelKey;
        private final Class<? extends Transaction> type;

        TypeOption(String labelKey, Class<? extends Transaction> type) {
            this.labelKey = labelKey;
            this.type = type;
        }
    }
}