package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.scene.layout.HBox;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * Reusable metadata row shown below a search bar.
 *
 * <p>Displays a left-aligned result count label that is hidden when the
 * filtered count is zero. Cards call {@link #update(String, int, int)}
 * after each filter operation to keep the label in sync.</p>
 */
public class SearchMetadataRow extends HBox {

    private final StyledText statusLabel = StyledText.widgetLabel();

    /**
     * Creates an empty metadata row. The status label is hidden until
     * the first call to {@link #update(String, int, int)}.
     */
    public SearchMetadataRow() {
        setMaxWidth(Double.MAX_VALUE);
        getChildren().add(statusLabel);
        setStatusVisible(false);
    }

    /**
     * Updates the result count label from the given i18n pattern.
     *
     * <p>The label is always visible after the first call, including when the
     * table is empty (e.g. "Showing 0 of 0"). This keeps the row height stable
     * so the layout does not shift when switching between tabs or filters.</p>
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
        setStatusVisible(true);
    }

    private void setStatusVisible(boolean visible) {
        statusLabel.setVisible(visible);
        statusLabel.setManaged(visible);
    }
}
