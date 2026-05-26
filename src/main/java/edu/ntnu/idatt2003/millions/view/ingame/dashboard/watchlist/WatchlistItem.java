package edu.ntnu.idatt2003.millions.view.ingame.dashboard.watchlist;

import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.watchlist.WatchlistEntry;

import edu.ntnu.idatt2003.millions.view.ingame.dashboard.watchlist.card.WatchlistCard;
import java.util.Objects;

/**
 * View-layer pairing of a {@link Stock} and its corresponding {@link WatchlistEntry}.
 *
 * <p>Used as the item type in {@link WatchlistCard} so that both stock data
 * and watchlist metadata (added week, note) are available during sorting and rendering
 * without extra lookups per row.</p>
 */
public record WatchlistItem(Stock stock, WatchlistEntry entry) {

    public WatchlistItem {
        Objects.requireNonNull(stock, "Stock cannot be null");
        Objects.requireNonNull(entry, "WatchlistEntry cannot be null");
    }
}
