package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.function.IntConsumer;

/**
 * Generic pagination control rendered as an {@link HBox} with Prev and Next buttons.
 * Call {@link #update(int, int)} after each data refresh to rebuild the
 * buttons for the current page state. The bar is left empty when there is
 * only one page.
 *
 * <p>Example usage:
 * <pre>{@code
 * Pagination pagination = new Pagination(PAGE_SIZE, page -> {
 *     currentPage = page;
 *     refresh();
 * });
 * // inside refresh():
 * pagination.update(currentPage, filteredItems.size());
 * }</pre>
 */
public class Pagination extends HBox {

    private static final String PREV_KEY = "pagination.prev";
    private static final String NEXT_KEY = "pagination.next";

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
    }

    /**
     * Rebuilds the pagination buttons for the given page state.
     *
     * <p>Renders only a Prev and a Next button. Prev is disabled on the first
     * page; Next is disabled on the last. The bar is cleared and left empty
     * when there is only one page or no items.
     *
     * @param currentPage the zero-based index of the currently visible page
     * @param totalItems  the total number of items across all pages
     */
    public void update(int currentPage, int totalItems) {
        getChildren().clear();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages <= 1) return;

        getChildren().add(buildButton(
                LanguageManager.get(PREV_KEY),
                currentPage - 1,
                currentPage == 0));

        getChildren().add(buildButton(
                LanguageManager.get(NEXT_KEY),
                currentPage + 1,
                currentPage >= totalPages - 1));
    }

    /**
     * Creates a single pagination button.
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
