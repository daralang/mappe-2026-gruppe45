package edu.ntnu.idatt2003.millions.view.component.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.component.Pagination;
import edu.ntnu.idatt2003.millions.view.component.SearchBar;
import edu.ntnu.idatt2003.millions.view.component.SearchMetadataRow;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.function.Consumer;

/**
 * Abstract base for paginated, sortable, searchable table cards.
 *
 * <p>Extends {@link PaginatedCard} with shared fields and implements
 * {@link #refresh()} as a sealed Template Method. Subclasses provide
 * the data-specific steps via abstract methods, may override the optional
 * {@link #afterFilter} hook, and may override {@link #buildSearchRow} to
 * add extra filter controls.</p>
 *
 * <p>{@link #table} and {@link #pagination} are non-final protected fields
 * that subclasses must assign in their constructors after the {@code super()}
 * call, since both depend on sort objects that cannot be created before it.</p>
 *
 * @param <T>      the item type displayed in the table rows
 * @param <Column> the sort-column enum type
 */
public abstract class SortableTableCard<T, Column> extends PaginatedCard {

    /**
     * The sortable table. Must be assigned by the subclass constructor
     * before the first call to {@link #refresh()}.
     */
    protected SortColumnTable<Column> table;

    /**
     * The pagination component. Must be assigned by the subclass constructor
     * before the first call to {@link #refresh()}.
     */
    protected Pagination pagination;

    /** Reusable metadata row showing search result counts. */
    protected final SearchMetadataRow metadataRow = new SearchMetadataRow();

    /** The active text-search term. Updated by the search bar callback. */
    protected String currentSearchTerm = "";

    /**
     * Constructs a sortable table card.
     *
     * @param gameService the game service to observe
     * @param pageSize    the number of items shown per page
     */
    protected SortableTableCard(GameService gameService, int pageSize) {
        super(gameService, pageSize);
    }

    /**
     * Template Method implementing the common refresh algorithm:
     * clear → fetch → search → header → metadata → hook → sort → render.
     */
    @Override
    protected final void refresh() {
        table.clearRows();
        List<T> all = fetchAll();
        List<T> filtered = applySearch(all, currentSearchTerm);

        table.refreshHeader(this::refresh, !filtered.isEmpty());
        metadataRow.update(statusKey(), filtered.size(), all.size());

        afterFilter(all, filtered);

        if (table.isSortActive()) {
            applySort(filtered);
        }

        if (filtered.isEmpty()) {
            table.renderEmptyState(emptyStateMessage(currentSearchTerm));
            pagination.update(0, 0);
            return;
        }

        clampCurrentPage(filtered.size());
        pagination.update(currentPage, filtered.size());

        int from = currentPage * pageSize;
        int to = Math.min(from + pageSize, filtered.size());
        renderPage(filtered.subList(from, to));
    }

    /**
     * Returns all items to display before text-search filtering.
     *
     * @return mutable list of all items in their default order
     */
    protected abstract List<T> fetchAll();

    /**
     * Filters {@code all} to items matching the search term.
     * A blank term must return all items.
     *
     * @param all  the full item list from {@link #fetchAll()}
     * @param term the active search term; may be blank
     * @return a new mutable list of matching items
     */
    protected abstract List<T> applySearch(List<T> all, String term);

    /**
     * Sorts {@code items} in place. Only called when a sort is active.
     *
     * @param items the filtered item list to sort in place
     */
    protected abstract void applySort(List<T> items);

    /**
     * Renders one page of items into the table.
     *
     * @param page the sub-list of items for the current page
     */
    protected abstract void renderPage(List<T> page);

    /**
     * Returns the i18n key for the metadata-row status pattern.
     * The pattern must accept two positional arguments: filtered count and total count.
     *
     * @return the i18n key
     */
    protected abstract String statusKey();

    /**
     * Returns the empty-state message when no items match the current filter.
     *
     * @param term the active search term; may be blank
     * @return the localised empty-state message
     */
    protected abstract String emptyStateMessage(String term);

    /**
     * Optional hook called after filtering but before sorting and rendering.
     * Default implementation is a no-op.
     *
     * @param all      the full item list before text-search filtering
     * @param filtered the text-filtered item list
     */
    protected void afterFilter(List<T> all, List<T> filtered) {}

    /**
     * Builds the default search row: an expanding {@link SearchBar} and the
     * clear-sort button on the right. Override to add extra filter controls.
     *
     * @param clearSortButton the button from
     *        {@link SortColumnTable#createClearSortButton}
     * @return the configured search row
     */
    protected HBox buildSearchRow(Button clearSortButton) {
        SearchBar searchBar = new SearchBar(
                "search.placeholder",
                "search.button",
                searchCallback(),
                metadataRow);
        searchBar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchBar, Priority.ALWAYS);
        HBox row = new HBox(8, searchBar, clearSortButton);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Returns a callback that updates {@link #currentSearchTerm} and resets
     * to the first page. Use this when overriding {@link #buildSearchRow}.
     *
     * @return the search callback
     */
    protected final Consumer<String> searchCallback() {
        return term -> {
            currentSearchTerm = term == null ? "" : term;
            resetPageAndRefresh();
        };
    }

    /** Rebuilds the card when the game state changes. */
    @Override
    public void onGameUpdated() {
        refresh();
    }

    /**
     * Rebuilds the card when the application language changes.
     * Override to also update title labels that hold i18n text.
     */
    @Override
    protected void onLanguageChanged() {
        refresh();
    }
}
