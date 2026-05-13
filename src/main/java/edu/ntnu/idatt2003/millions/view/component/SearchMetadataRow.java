package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * Reusable metadata row for search sections.
 *
 * <p>Displays a left-aligned result count label and a right-aligned clear-sort
 * {@link Button}. Cards update the label with {@link #update(String, int, int)}
 * after filtering, while {@code SortColumnTable} owns the clear-sort button state.</p>
 */
public class SearchMetadataRow extends HBox {

    private final StyledText statusLabel = StyledText.widgetLabel();

    /**
     * Creates a metadata row with a right-aligned clear-sort button.
     *
     * @param clearSortButton the button used to clear the active table sort
     * @throws NullPointerException if clearSortButton is null
     */
    public SearchMetadataRow(Button clearSortButton) {
        Objects.requireNonNull(clearSortButton, "Clear sort button cannot be null");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        setAlignment(Pos.CENTER_LEFT);
        setMaxWidth(Double.MAX_VALUE);
        getChildren().addAll(statusLabel, spacer, clearSortButton);
        setStatusVisible(false);
    }

    /**
     * Updates the result count label from the given i18n pattern.
     *
     * <p>The label is hidden when the filtered count is zero, leaving only the
     * clear-sort button when sorting is active.</p>
     *
     * @param patternKey    the i18n key with two positional arguments
     * @param filteredCount the number of rows currently shown after filtering
     * @param totalCount    the number of rows before search filtering
     */
    public void update(String patternKey, int filteredCount, int totalCount) {
        Objects.requireNonNull(patternKey, "Pattern key cannot be null");
        statusLabel.setText(MessageFormat.format(
                LanguageManager.get(patternKey),
                filteredCount,
                totalCount));
        setStatusVisible(filteredCount > 0);
    }

    private void setStatusVisible(boolean visible) {
        statusLabel.setVisible(visible);
        statusLabel.setManaged(visible);
    }
}
