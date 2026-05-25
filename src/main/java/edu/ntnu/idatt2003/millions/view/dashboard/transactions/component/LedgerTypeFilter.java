package edu.ntnu.idatt2003.millions.view.dashboard.transactions.component;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

import java.util.List;

/**
 * Generic dropdown filter that narrows a ledger table to a specific entry type.
 *
 * <p>Constructed with a list of {@link TypeOption} values, where the first option's
 * value is {@code null} and represents "no filter applied" (show all). Selecting
 * any other option exposes its typed value via {@link #selectedValueProperty()} and
 * {@link #getSelectedValue()}, letting callers filter without casting.</p>
 *
 * <p>Two concrete uses:</p>
 * <ul>
 *   <li>Stock trade filter — {@code T = Class<? extends Transaction>} (Purchase / Sale)</li>
 *   <li>Loan ledger filter — {@code T = LoanLedgerEntryType} (DISBURSEMENT / INTEREST / REPAYMENT)</li>
 * </ul>
 *
 * <p>Mirrors {@link edu.ntnu.idatt2003.millions.view.component.WeekRangeFilter} in shape: extends a JavaFX control, exposes an
 * observable property, and handles its own localization via {@link LanguageManager}.</p>
 *
 * @param <T> the type of value each option represents
 */
public class LedgerTypeFilter<T> extends ComboBox<LedgerTypeFilter.TypeOption<T>> {

    /**
     * A single option in the filter dropdown.
     *
     * @param labelKey the i18n key for the display label
     * @param value    the filter value, or {@code null} for "all types"
     * @param <T>      the type of the filter value
     */
    public record TypeOption<T>(String labelKey, T value) {}

    private final List<TypeOption<T>> options;

    /**
     * Constructs a filter with the given options. The first option is selected by
     * default and conventionally has a {@code null} value to represent "all types".
     *
     * @param options the ordered list of filter options; must not be empty
     */
    public LedgerTypeFilter(List<TypeOption<T>> options) {
        this.options = List.copyOf(options);

        setItems(FXCollections.observableArrayList(this.options));
        setValue(this.options.getFirst());
        getStyleClass().add("type-filter");

        setConverter(new StringConverter<TypeOption<T>>() {
            @Override
            public String toString(TypeOption<T> option) {
                return option == null ? "" : LanguageManager.get(option.labelKey());
            }
            @Override
            public TypeOption<T> fromString(String s) {
                return null;
            }
        });

        // Force button cell to re-render localized label when language changes.
        LanguageManager.addObserver(() -> {
            TypeOption<T> current = getValue();
            setItems(FXCollections.observableArrayList(LedgerTypeFilter.this.options));
            setValue(current);
        });
    }

    /**
     * Returns an observable holding the currently selected filter value.
     * The value is {@code null} when "all types" is selected.
     *
     * @return observable of the active filter value
     */
    public ObservableValue<T> selectedValueProperty() {
        return valueProperty().map(option -> option == null ? null : option.value());
    }

    /**
     * Returns the currently selected filter value, or {@code null} when
     * "all types" is selected.
     *
     * @return the active filter value, or {@code null}
     */
    public T getSelectedValue() {
        TypeOption<T> option = getValue();
        return option == null ? null : option.value();
    }
}
