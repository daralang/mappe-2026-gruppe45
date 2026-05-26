package edu.ntnu.idatt2003.millions.view.ingame.component.card;

import edu.ntnu.idatt2003.millions.controller.trade.TradeController;
import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.view.ingame.component.ChevronButton;
import edu.ntnu.idatt2003.millions.view.ingame.component.table.RowCells;

/**
 * Abstract base for sortable table cards that open a detail view when a row is activated.
 *
 * <p>Extends {@link SortableTableCard} and adds a {@link TradeController} field shared by
 * all concrete subclasses.</p>
 *
 * <p>Concrete subclasses:</p>
 * <ul>
 *   <li>{@code HoldingsCard} opens the share detail modal for a {@code Share}</li>
 *   <li>{@code StocksCard} opens the stock detail modal for a {@code Stock}</li>
 *   <li>{@code WatchlistCard}opens the stock detail modal for a {@code WatchlistItem}</li>
 * </ul>
 *
 * @param <T>      the item type displayed in the table rows
 * @param <Column> the sort-column enum type
 */
public abstract class DetailTableCard<T, Column> extends SortableTableCard<T, Column> {

    /**
     * The controller used to open detail and trade dialogs.
     * Accessible to subclasses for wiring chevron buttons and action callbacks.
     */
    protected final TradeController controller;

    /**
     * Constructs a detail table card.
     *
     * @param gameService   the game service to observe
     * @param controller    the controller used to open detail and trade dialogs
     * @param pageSize      the number of items shown per page
     * @param statusKey     i18n key for the metadata-row status pattern;
     *                      must accept two positional arguments (filtered count, total count)
     * @param emptyStateKey i18n key for the default empty-state message
     */
    protected DetailTableCard(
            GameService gameService,
            TradeController controller,
            int pageSize,
            String statusKey,
            String emptyStateKey) {
        super(gameService, pageSize, statusKey, emptyStateKey);
        this.controller = controller;
    }

    /**
     * Sealed activation hook: always delegates to {@link #openDetail(Object)}.
     * Subclasses must not override this method; override {@link #openDetail(Object)} instead.
     *
     * @param item the item whose row was confirmed by keyboard or mouse
     */
    @Override
    protected final void onRowEnter(T item) {
        openDetail(item);
    }

    /**
     * Opens the detail view for the given item.
     * Called whenever the user activates a row (Enter, Space, or click on a data cell).
     *
     * @param item the item to show details for
     */
    protected abstract void openDetail(T item);

    /**
     * Returns the i18n key for the chevron button tooltip.
     *
     * @return the tooltip i18n key
     */
    protected String chevronTooltipKey() {
        return "tooltip.stocks.chevron";
    }

    /**
     * Builds a {@link ChevronButton} that opens the detail view for the given item.
     *
     * <p>The button delegates to {@link #openDetail(Object)} and uses the tooltip key
     * returned by {@link #chevronTooltipKey()}. Intended to be placed in the
     * {@code DETAILS} column of {@link RowCells}
     * by concrete subclasses.</p>
     *
     * @param item the item to open the detail view for
     * @return a configured {@link ChevronButton}
     */
    protected final ChevronButton buildDetailChevron(T item) {
        return new ChevronButton(() -> openDetail(item), chevronTooltipKey());
    }
}
