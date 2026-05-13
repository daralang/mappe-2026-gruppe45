package edu.ntnu.idatt2003.millions.view.component.card;

import edu.ntnu.idatt2003.millions.service.GameService;

/**
 * Abstract base class for paginated card components.
 *
 * <p>Extends {@link Card} with shared pagination state and behaviour:
 * current page tracking, page navigation, and page clamping after
 * filter changes. Subclasses implement {@link #refresh()} to rebuild
 * their content for the active page.</p>
 *
 * <p>Uses the Template Method pattern: {@link #setPage} and
 * {@link #resetPageAndRefresh} both delegate to {@link #refresh()}
 * after updating state, so subclasses never need to manage that
 * sequencing themselves.</p>
 */
public abstract class PaginatedCard extends Card {

    /** The number of items shown per page. Set once in the subclass constructor. */
    protected final int pageSize;

    /** The zero-based index of the currently visible page. */
    protected int currentPage = 0;

    /**
     * Constructs a paginated card with the given page size.
     *
     * @param gameService the game manager to observe
     * @param pageSize    the number of items shown per page
     */
    protected PaginatedCard(GameService gameService, int pageSize) {
        super(gameService);
        this.pageSize = pageSize;
    }

    /**
     * Navigates to the given page and refreshes the table.
     * Called by {@link edu.ntnu.idatt2003.millions.view.component.Pagination}
     * when the user clicks Prev or Next.
     *
     * @param page the zero-based page index to navigate to
     */
    public void setPage(int page) {
        currentPage = page;
        refresh();
    }

    /**
     * Resets to the first page and refreshes.
     * Call this whenever a filter or sort change invalidates the current page position.
     */
    protected void resetPageAndRefresh() {
        currentPage = 0;
        refresh();
    }

    /**
     * Clamps {@link #currentPage} to the last valid page for the given item count.
     * Call this before rendering to prevent showing an empty page after items are removed.
     *
     * @param itemCount the total number of filtered items
     */
    protected void clampCurrentPage(int itemCount) {
        int lastPage = Math.max(0, (itemCount - 1) / pageSize);
        if (currentPage > lastPage) {
            currentPage = lastPage;
        }
    }

    /**
     * Rebuilds the card's table content for the current page.
     * Implemented by each subclass.
     */
    protected abstract void refresh();
}
