# Table architecture: things we should clean up

**Labels:** `refactor`, `code-quality`, `i18n`

Went through all the table-related code and found a few things worth fixing before submission.

## 1. Inconsistent label for the change column

The i18n keys `changeKr` and `changeNok` refer to the same thing but are named differently. In English, the stocks table uses `+/- NOK` while the watchlist uses `+/- kr`. We should pick one and use it everywhere.

## 2. Duplicated logic in `StocksSort` and `WatchlistSort`

`priceInNok`, `changeInNok` and `highLowRange` are basically copy-pasted between the two classes. On top of that, `WatchlistSort.highLowRange` has an `isEmpty()` check that `StocksSort` is missing — so `StocksSort` will throw a `NoSuchElementException` if a stock has no price history. We should extract the shared logic and make sure the fix is applied in both places.

## 3. `TableColumnDef` stores the resolved label instead of the i18n key

Right now every `getColumnDefs()` calls `LanguageManager.get(...)` for every single column on every header refresh. It would be cleaner to store the i18n key in the record and resolve it there, so the `LanguageManager` dependency lives in one place and the column definitions can be created once.

## 4. `ActiveLoansCard` doesn't use `SortableTableCard`

Every other table card extends `SortableTableCard`, but `ActiveLoansCard` builds its table manually with a raw `GridPane`. It doesn't support sorting, search, or automatic label updates on language change. We should either migrate it or leave a comment explaining why we chose not to.

## 5. Empty string literals in `HoldingsSort`

The spacer columns are defined with `""` which isn't very readable. A `TableColumnDef.spacer(...)` factory method would make the intent clearer.

## 6. Missing Javadoc

The `of(label, percentWidth, alignment, leftInset)` overload in `TableColumnDef` has no Javadoc. `LeaderboardSort` is also missing Javadoc on `getColumnDefs()` and `buildComparator()` — all the other sort classes have it.

## 7. `RANK` comparator sorts on the wrong field

In `LeaderboardSort` the `RANK` case sorts by `returnPercent` instead of rank position. If that's intentional we should add a comment, otherwise it's confusing.
