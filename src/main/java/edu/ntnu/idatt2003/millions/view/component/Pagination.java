package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.text.MessageFormat;
import java.util.function.IntConsumer;

/**
 * Generic pagination control rendered as an {@link HBox}.
 *
 * <p>Displays a centred page indicator label (e.g. {@code 1 / 20 sider})
 * flanked by grow spacers, followed by Prev and Next buttons right-aligned.
 * Layout: {@code spacer | pageLabel | spacer | [< Prev] [Next >]}
 *
 * <p>Call {@link #update(int, int)} after each data refresh to rebuild the
 * control for the current page state. The control is cleared when there is
 * only one page or no items.
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
    public static final int DEFAULT_PAGE_SIZE = 15;

    private static final String PREV_KEY  = "pagination.prev";
    private static final String NEXT_KEY  = "pagination.next";
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
     * <p>Shows a centred page indicator label and Prev / Next buttons.
     * Prev is disabled on the first page; Next is disabled on the last.
     * The control is cleared and left empty when there is only one page
     * or no items.
     *
     * @param currentPage the zero-based index of the currently visible page
     * @param totalItems  the total number of items across all pages
     */
    public void update(int currentPage, int totalItems) {
        getChildren().clear();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages <= 1) return;

        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);

        Label pageLabel = new Label(MessageFormat.format(
                LanguageManager.get(PAGES_KEY),
                currentPage + 1,
                totalPages));
        pageLabel.getStyleClass().add("pagination-label");

        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        Button prevButton = buildButton(
                LanguageManager.get(PREV_KEY),
                currentPage - 1,
                currentPage == 0);

        Button nextButton = buildButton(
                LanguageManager.get(NEXT_KEY),
                currentPage + 1,
                currentPage >= totalPages - 1);

        getChildren().addAll(leftSpacer, pageLabel, rightSpacer, prevButton, nextButton);
    }

    /**
     * Creates a single pagination navigation button.
     *
     * <p>Clicking the button invokes {@link #onPageChange} with the given
     * target page index.
     *
     * @param text       the button label
     * @param targetPage the page index to navigate to on click
     * @param disabled   whether the button should be disabled
     * @return a configured {@link Button}
     */
    private Button buildButton(String text, int targetPage, boolean disabled) {
        Button button = new Button(text);
        button.getStyleClass().add("pagination-button");
        button.setDisable(disabled);
        button.setOnAction(e -> onPageChange.accept(targetPage));
        return button;
    }
}
