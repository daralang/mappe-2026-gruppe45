package edu.ntnu.idatt2003.millions.view.ingame.component;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;

import java.text.MessageFormat;
import java.util.function.IntConsumer;

/**
 * Generic pagination control rendered as an {@link HBox}.
 *
 * <p>Displays a muted status label on the left (e.g. {@code Side 2 av 5}) and
 * two icon-only chevron buttons on the right. The previous button is disabled on
 * the first page; the next button is disabled on the last.
 *
 * <p>The control is cleared and left empty when there is only one page or no items.
 *
 * <p>Call {@link #update(int, int)} after each data refresh to rebuild the control
 * for the current page state. The public API is unchanged from the previous version.
 *
 * <p>Example usage:
 * <pre>{@code
 * Pagination pagination = new Pagination(PAGE_SIZE, page -> table.setPage(page));
 * // inside onRefreshed callback:
 * pagination.update(table.getCurrentPage(), table.getFilteredCount());
 * }</pre>
 */
public class Pagination extends HBox {

    /** Default number of table rows shown before pagination is needed. */
    public static final int DEFAULT_PAGE_SIZE = 8;

    private static final String PAGES_KEY = "pagination.pages";

    private final int pageSize;
    private final IntConsumer onPageChange;

    /**
     * Constructs a new Pagination control.
     *
     * @param pageSize     the number of items shown per page
     * @param onPageChange callback invoked with the target page index when a
     *                     navigation button is clicked
     */
    public Pagination(int pageSize, IntConsumer onPageChange) {
        super(8);
        this.pageSize = pageSize;
        this.onPageChange = onPageChange;
        setAlignment(Pos.CENTER_LEFT);
    }

    /**
     * Rebuilds the pagination control for the given page state.
     *
     * <p>Shows a muted status label on the left and icon-only prev/next buttons on the
     * right. Prev is disabled on the first page; next is disabled on the last.
     * The control is cleared and left empty when there is only one page or no items.
     *
     * @param currentPage the zero-based index of the currently visible page
     * @param totalItems  the total number of items across all pages
     */
    public void update(int currentPage, int totalItems) {
        getChildren().clear();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages <= 1) return;

        Label statusLabel = new Label(MessageFormat.format(
                LanguageManager.get(PAGES_KEY),
                currentPage + 1,
                totalPages));
        statusLabel.getStyleClass().add("pagination-label");

        Button prevButton = buildButton("fth-chevron-left", currentPage - 1, currentPage == 0);
        Button nextButton = buildButton("fth-chevron-right", currentPage + 1, currentPage >= totalPages - 1);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(4, prevButton, nextButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(statusLabel, spacer, actions);
    }

    /**
     * Creates a single icon-only pagination navigation button.
     *
     * @param iconLiteral the ikonli icon literal for the button graphic
     * @param targetPage  the page index to navigate to on click
     * @param disabled    whether the button should be disabled
     * @return a configured {@link Button}
     */
    private Button buildButton(String iconLiteral, int targetPage, boolean disabled) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.getStyleClass().add("pagination-icon");
        Button button = new Button();
        button.setGraphic(icon);
        button.getStyleClass().add("pagination-button");
        button.setDisable(disabled);
        button.setOnAction(e -> onPageChange.accept(targetPage));
        return button;
    }
}
