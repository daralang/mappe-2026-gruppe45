package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;

/**
 * Reusable inline week-range filter rendered as "Uke [from] – [to]".
 *
 * <p>The component owns two spinners, one for the start of the range
 * and one for the (inclusive) end and enforces {@code from <= to} by
 * adjusting each spinner's bounds when the other side changes.</p>
 *
 * Communication with callers happens through two observable
 * {@link IntegerProperty} values. The upper bound is dynamic.
 */
public class WeekRangeFilter extends HBox {

    /** Preferred width for each spinner, in pixels. */
    private static final int SPINNER_WIDTH = 60;

    /** Horizontal spacing between the label, spinners and separator. */
    private static final int SPACING = 8;

    private final IntegerProperty fromWeek = new SimpleIntegerProperty();
    private final IntegerProperty toWeek = new SimpleIntegerProperty();
    private final Spinner<Integer> fromSpinner;
    private final Spinner<Integer> toSpinner;
    private final Label label;
    private final Runnable languageObserver = this::refreshLabel;

    private final int minWeek;

    /**
     * Constructs a new week range filter with the given absolute bounds.
     * The initial selection covers the full range, i.e. {@code from = minWeek}
     * and {@code to = maxWeek}, which represents "all weeks".
     *
     * @param minWeek the smallest selectable week (typically 1)
     * @param maxWeek the largest selectable week (typically the current game week)
     */
    public WeekRangeFilter(int minWeek, int maxWeek) {
        this.minWeek = minWeek;

        setSpacing(SPACING);
        setAlignment(Pos.TOP_LEFT);
        getStyleClass().add("week-range-filter");

        label = new Label(LanguageManager.get("filter.week"));
        label.getStyleClass().add("filter-label");

        fromSpinner = createSpinner(minWeek, maxWeek, minWeek);
        toSpinner = createSpinner(minWeek, maxWeek, maxWeek);

        Label separator = new Label("–");
        separator.getStyleClass().add("filter-separator");

        wireSpinnerListeners();

        // Initialise the exposed properties to match the spinners' starting
        // values so callers can read them immediately after construction.
        fromWeek.set(minWeek);
        toWeek.set(maxWeek);

        getChildren().addAll(label, fromSpinner, separator, toSpinner);

        // Register the language observer only while attached to a scene, so transient
        // hosts (e.g. modals) do not leak an observer on every open.
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                LanguageManager.addObserver(languageObserver);
            } else {
                LanguageManager.removeObserver(languageObserver);
            }
        });
    }

    /**
     * Refreshes the label text from the active language.
     */
    private void refreshLabel() {
        label.setText(LanguageManager.get("filter.week"));
    }

    /**
     * Creates a configured spinner with the given bounds and initial value.
     *
     * @param min     minimum selectable value
     * @param max     maximum selectable value
     * @param initial starting value
     * @return a configured spinner with a fixed preferred width
     */
    private Spinner<Integer> createSpinner(int min, int max, int initial) {
        Spinner<Integer> spinner = new Spinner<>(min, max, initial);
        spinner.getStyleClass().add("week-spinner");
        spinner.setEditable(true);
        spinner.setPrefWidth(SPINNER_WIDTH);
        return spinner;
    }

    /**
     * Wires the two-way constraint that keeps {@code from <= to}.
     *
     * <p>When the user changes one endpoint, the opposite spinner's bound
     * shifts so the new selection cannot create an invalid range. The
     * exposed properties are updated to mirror the spinner values.</p>
     */
    private void wireSpinnerListeners() {
        fromSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            fromWeek.set(newVal);
            ((SpinnerValueFactory.IntegerSpinnerValueFactory)
                    toSpinner.getValueFactory()).setMin(newVal);
        });
        toSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            toWeek.set(newVal);
            ((SpinnerValueFactory.IntegerSpinnerValueFactory)
                    fromSpinner.getValueFactory()).setMax(newVal);
        });
    }

    /**
     * Raises the upper bound of both spinners and advances the to-spinner
     * value to {@code newMax} so that newly added weeks are always visible.
     *
     * <p>Typically called from a parent view's {@code onGameUpdated()}
     * hook whenever the game advances to a new week.</p>
     *
     * @param newMax the new upper bound; ignored if smaller than the
     *               configured minimum
     */
    public void setMaxWeek(int newMax) {
        if (newMax < minWeek) {
            return;
        }

        SpinnerValueFactory.IntegerSpinnerValueFactory toFactory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) toSpinner.getValueFactory();
        SpinnerValueFactory.IntegerSpinnerValueFactory fromFactory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) fromSpinner.getValueFactory();

        fromFactory.setMax(newMax);
        toFactory.setMax(newMax);
        fromFactory.setValue(minWeek);
        toFactory.setValue(newMax);
    }

    /**
     * @return observable property holding the start of the range
     */
    public ReadOnlyIntegerProperty fromWeekProperty() {
        return fromWeek;
    }

    /**
     * @return observable property holding the end of the range (inclusive)
     */
    public ReadOnlyIntegerProperty toWeekProperty() {
        return toWeek;
    }

    /**
     * @return the currently selected start week
     */
    public int getFromWeek() {
        return fromWeek.get();
    }

    /**
     * @return the currently selected end week (inclusive)
     */
    public int getToWeek() {
        return toWeek.get();
    }
}
