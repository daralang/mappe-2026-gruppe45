package edu.ntnu.idatt2003.millions.view.component.card;

import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.keyboard.SearchFocusProvider;
import edu.ntnu.idatt2003.millions.view.component.Pagination;
import edu.ntnu.idatt2003.millions.view.component.SearchBar;
import edu.ntnu.idatt2003.millions.view.component.SearchMetadataRow;
import edu.ntnu.idatt2003.millions.view.component.table.RowCells;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.component.table.SortProvider;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Abstract base for paginated, sortable, searchable table cards.
 *
 * <p>Extends {@link PaginatedCard} and implements {@link #refresh()} as a sealed
 * Template Method. Row rendering is a second Template Method so
 * subclasses describe only what a row contains, not how it is inserted or navigated.</p>
 *
 * @param <T>      the item type displayed in the table rows
 * @param <Column> the sort-column enum type
 */
public abstract class SortableTableCard<T, Column> extends PaginatedCard implements SearchFocusProvider {

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

    /**
     * The sort-logic provider. Must be assigned by the subclass constructor
     * before the first call to {@link #refresh()}.
     */
    protected SortProvider<T, Column> sortProvider;

    /** Reusable metadata row showing search result counts. */
    protected final SearchMetadataRow metadataRow = new SearchMetadataRow();

    /**
     * The search bar built by {@link #buildSearchRow}.
     * Assigned during construction; may be {@code null} in subclasses that
     * override {@link #buildSearchRow} without calling this method.
     * Exposed via {@link #focusSearch()}.
     */
    protected SearchBar searchBar;

    /** The active text-search term. Updated by the search bar callback. */
    protected String currentSearchTerm = "";

    private final String statusKey;
    private final String emptyStateKey;

    /**
     * Constructs a sortable table card.
     *
     * @param gameService   the game service to observe
     * @param pageSize      the number of items shown per page
     * @param statusKey     i18n key for the metadata-row status pattern;
     *                      must accept two positional arguments (filtered count,
     *                      total count)
     * @param emptyStateKey i18n key for the default empty-state message
     * @throws NullPointerException if statusKey or emptyStateKey is null
     */
    protected SortableTableCard(
            GameService gameService, int pageSize,
            String statusKey, String emptyStateKey) {
        super(gameService, pageSize);
        this.statusKey = Objects.requireNonNull(statusKey, "statusKey cannot be null");
        this.emptyStateKey = Objects.requireNonNull(emptyStateKey, "emptyStateKey cannot be null");
    }

    /**
     * Template Method implementing the common refresh algorithm:
     * clear > fetch > search > header > metadata > hook > sort > render.
     */
    @Override
    protected final void refresh() {
        table.clearRows();
        List<T> all = fetchAll();
        List<T> filtered = applySearch(all, currentSearchTerm);

        table.refreshHeader(this::refresh, !filtered.isEmpty());
        metadataRow.update(statusKey, filtered.size(), all.size());

        afterFilter(all, filtered);

        if (table.isSortActive()) {
            sortProvider.applySort(filtered, table.getSortState());
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
     * Renders one page of items as keyboard-navigable rows. For each item it builds
     * the cells, anchors focus, made focus-traversable so read-only
     * tables are keyboard-reachable, and binds ENTER to {@link #onRowEnter(Object)}.
     *
     * @param page the sub-list of items for the current page
     */
    private void renderPage(List<T> page) {
        for (int i = 0; i < page.size(); i++) {
            T item = page.get(i);
            int rowIndex = i + 1;
            RowCells<Column> cells = buildRowCells(item, rowIndex);
            Node anchor = focusAnchorFor(item, cells);
            if (anchor != null) {
                anchor.setFocusTraversable(true);
            }
            table.addSelectableRow(rowIndex, anchor, () -> onRowEnter(item), cells);
        }
    }

    /**
     * Builds the column-keyed cells for a single item's row.
     *
     * <p>Implemented by every subclass. The {@code rowIndex} is the one-based position
     * of this row within the current page (row 0 is the header), supplied for cells whose
     * content depends on position, such as a rank column.</p>
     *
     * @param item     the item to render as a row
     * @param rowIndex the one-based grid row this item occupies
     * @return the column-keyed cells produced via {@link RowCells#builder()}
     */
    protected abstract RowCells<Column> buildRowCells(T item, int rowIndex);

    /**
     * Returns the node that should receive focus when the user navigates to this
     * item's row with the arrow keys.
     *
     * @param item  the item whose row is being built
     * @param cells the cells built for this row by {@link #buildRowCells(Object, int)}
     * @return the focus anchor node for this row
     */
    protected Node focusAnchorFor(T item, RowCells<Column> cells) {
        return cells.firstNode();
    }

    /**
     * Invoked when the user presses ENTER on this item's row.
     *
     * @param item the item whose row was confirmed
     */
    protected void onRowEnter(T item) {}

    /**
     * Returns the localised empty-state message when no items match the current filter.
     *
     * @param term the active search term; may be blank
     * @return the localised empty-state message
     */
    protected String emptyStateMessage(String term) {
        return LanguageManager.get(emptyStateKey);
    }

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
        searchBar = createSearchBar("search.placeholder", "search.button", metadataRow);
        searchBar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchBar, Priority.ALWAYS);
        HBox row = new HBox(8, searchBar, clearSortButton);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Creates a {@link SearchBar} wired to this card's search callback and to the
     * table, so pressing DOWN in the field moves focus into the first row. Use this
     * instead of constructing {@link SearchBar} directly, including when overriding
     * {@link #buildSearchRow}, so the search-to-results behaviour stays consistent.
     *
     * @param placeholderKey the i18n key for the field placeholder
     * @param buttonKey      the i18n key for the search button text
     * @param metadata       optional metadata node shown below the search row
     * @return a configured search bar
     */
    protected final SearchBar createSearchBar(String placeholderKey, String buttonKey, Node metadata) {
        SearchBar bar = new SearchBar(placeholderKey, buttonKey, searchCallback(), metadata);
        bar.setOnArrowDown(table::focusFirstRow);
        return bar;
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

    /**
     * Focuses the search field of this card, if one exists.
     * Subclasses that override {@link #buildSearchRow} must also override
     * this method to forward focus to their own {@link SearchBar} field.
     */
    public void focusSearch() {
        if (searchBar != null) {
            searchBar.focus();
        }
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
