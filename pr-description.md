# feat: add stocks tab to exchange view

## Summary

Implements the full **Stocks** tab inside the Exchange view, giving the player a sortable, searchable and paginated table of all stocks listed on the exchange, together with portfolio summary cards and trade actions.

---

## What changed

### New views and components

- **`StocksView`** — top-level view for the Stocks tab. Assembles four summary cards, a search bar with a clear-sort button, a status label, a `Pagination` bar and the `StocksListCard` table.
- **`StocksListCard`** — sortable, filterable and paginated table. Manages data loading, filtering, sort state and page state. Delegates row rendering to `StocksRowRenderer` and notifies the parent via `setOnRefreshed(Runnable)`.
- **`StocksRowRenderer`** — stateless renderer responsible for building a single table row (ticker with owner badge, company, price in original currency and NOK, weekly change, 4-week high/low, sparkline, buy/sell buttons).
- **`StocksSort`** — manages sort state (column + direction) and delegates header button creation to `TableCells.sortHeader()`.
- **`StocksInvestedCard`** — summary card showing total invested amount with a dynamic subtitle showing the number of open positions. Delegates position count to `PortfolioService`.
- **`StocksUnrealizedReturnCard`** — summary card showing total unrealized return in NOK with total return percentage and an arrow/sign prefix. Subtitle reads "Since purchase".

### New shared components

- **`SearchBar`** (`view/component/`) — reusable search bar with a placeholder key, a search button key and an `onSearch` callback. Search is triggered on Enter or button click.
- **`Pagination`** (`view/component/`) — generic pagination control with a centred page indicator (`1 / 20 pages`) and right-aligned Prev/Next buttons. Owned by the parent view; the table exposes `setPage(int)` and `getCurrentPage()` so the parent drives navigation.
- **`SparklineChart`** (`view/component/`) — lightweight JavaFX `Canvas`-based sparkline rendering recent price history. Fixed to correctly render a single-point chart instead of throwing when only one price entry exists.
- **`PortfolioValueCard`**, **`AvailableFundsCard`**, **`SimpleWidgetCard`**, **`WidgetCard`** and **`Card`** moved to `view/component/card/` so they can be shared across the Dashboard, Exchange and any future views.
- **`SimpleWidgetCard`** extracted to eliminate boilerplate in single-value widget cards.

### Utility additions

- **`TableCells`** — added `sortHeader(String, Runnable)` factory for styled, hoverable sort header buttons and `configureColumns(GridPane, double[], HPos[])` for consistent column setup.
- **`SortState`** — reusable value class encapsulating active sort column and direction. Extracted from `StocksSort` so any table can use it without reimplementing sort tracking.
- **`ChangeFormatter`** — added `formatPlain(BigDecimal)` for unstyled number output.
- **`PortfolioService`** — added `getInvestedAmount`, `getPositionCount`, `getTotalReturnInNok`, `getTotalReturnPercent`, `getWeeklyReturnInNok` and `getWeeklyReturnPercent`.

### i18n

- Added all `exchange.stocks.*` keys to both `messages_no.properties` and `messages_en.properties`.
- Added generic `pagination.prev`, `pagination.next` and `pagination.pages` keys.
- Fixed corrupted ISO-8859-1 Norwegian characters (`ø`, `å`) across `messages_no.properties`.

### CSS

- `searchbar.css` — styles for `.search-bar`, `.search-button` and `.search-clear-button`.
- `holdings.css` — added `.Button.holdings-header` (transparent sort button) with primary hover colour and `.owner-cell` badge style.
- `buttons.css` / `label.css` — pagination button and label styles.

---

## Architecture notes

The implementation follows the project's layered architecture:

| Concern | Where it lives |
|---|---|
| Game state updates | `StocksListCard.onGameUpdated()` via `Card` base class |
| Business logic | `PortfolioService` (stateless read service) |
| Mutation | `GameService` only — no direct model mutation in view |
| Row presentation | `StocksRowRenderer` (package-private, stateless) |
| Sort state | `StocksSort` + `SortState` utility |
| Pagination | `Pagination` component owned by `StocksView` |

`StocksView` does **not** implement `GameObserver` directly — all game-driven updates flow through `StocksListCard`, which fires `onRefreshed` after every refresh so `StocksView` can update its status label and pagination in one place.

---

## Testing

- `GameManagerTest` extended to cover state changes observable from the Stocks tab.

---

## Further work

The following improvements and known limitations are deferred to follow-up tasks.

### UI / UX

- **Icons for search and sort** — the search button and sort header buttons currently use text labels only. Replace or supplement with SVG/font icons (e.g. a magnifying glass for search, up/down arrows for sort direction) to align with common exchange UI conventions and improve scannability.
- **Singular vs plural for position count** — `StocksInvestedCard` subtitle reads `{0} posisjoner` regardless of count. When there is exactly 1 position it should read `1 posisjon`. A helper method on `LanguageManager` (or a dedicated pluralisation utility) would make this reusable across the application.
### Data and domain

- **Week 1 price change** — on the first game week there is no prior price to compare against, so `getLatestPriceChange()` and `getWeeklyChangePercent()` return a hardcoded ±10 % synthetic change. Consider either pre-seeding one week of historical data when a new game is created so the first displayed change reflects a real calculation, or explicitly showing `—` in the UI when no prior week exists rather than a synthetic value.
- **`formatHighLow` edge case** — `StocksRowRenderer.formatHighLow()` calls `orElseThrow()` on an empty stream. If a stock has fewer than one historical price entry the method will throw. Add a guard that falls back to `—` when insufficient data is available.
- **`StocksRowRenderer` stale converter** — the `CurrencyConverter` is captured once in the constructor. If the converter is replaced (e.g. after loading a save file with a different base currency), the renderer will use stale rates until the next restart. Consider reading the converter from `GameService` on each `buildRow` call instead.

### Architecture

- **Multi-column sort** — `SortState` supports a single active column. A follow-up could extend it to support a primary + secondary sort column, which is common in stock screeners.
- **Debounced live search** — the current `SearchBar` requires an explicit Enter/button press. Live filtering as the user types (with a short debounce) would feel more responsive.
- **Pagination keyboard navigation** — `Pagination` prev/next buttons are not reachable via keyboard Tab/arrow keys. Adding `setFocusTraversable(true)` and key bindings would improve accessibility.

---

## Checklist

- [x] No business logic in view or controller
- [x] All new classes have full JavaDoc
- [x] i18n keys added for Norwegian and English
- [x] `PortfolioService` methods are stateless
- [x] `StocksRowRenderer` is stateless and reusable across refreshes
- [x] `Pagination` and `SearchBar` are generic and not coupled to the Stocks tab
