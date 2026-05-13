# Separation of Concerns for this project

## Application layers

**View** (`view/`)
Builds the JavaFX user interface and captures user input. Reads from the model, observes state changes, and delegates user actions to controllers via callbacks. Contains no business logic and never mutates the model directly.

**Controller** (`controller/`)
Translates user input into calls on `GameService` for actions that change game state, and reads from the model directly when only displaying or previewing data. Owns game-policy rules (the regulations the game imposes on top of the domain) and exposes them as preview-validation methods so views can ask before submitting. Contains no business logic itself.

**GameService** (`service/`)
Owns the live game state (`Player`, `Exchange`), coordinates operations that span multiple domain objects, and notifies observers after each change. Provides controllers and views with a stable API for performing actions (buy, sell, advance week, save/load).

**Application read services** (`service/`)
Stateless query services - `PlayerStatsService`, `PortfolioService`, `RealizedReturnsService` - that compute derived values from `Player` and `CurrencyConverter` without holding any state themselves. Views call these directly instead of asking `GameService` to re-expose every query as a facade method.

**Domain model** (`model/`)
Holds all business logic and business rules. The domain objects own their own state and validation. Operations that do not naturally belong to a single domain object are placed in dedicated Domain Services, such as `TransactionPreviewService`, which coordinates calculations across multiple domain objects without mutating anything.

**Factory** (`factory/`)
Centralised creation of `Transaction` objects. Used by `Exchange` and controllers to avoid direct instantiation of concrete subclasses.

**File layer** (`file/`)
Infrastructure for reading and writing game data. Called from `GameService` or controllers; never directly from the view or the domain.

## Read/write access matrix

The following table summarises which layers may read from and write to the domain model:

| Layer | Read from model? | Mutate model? |
|---|---|---|
| **View** | Yes (getters, derived values) | No |
| **Controller** | Yes (getters, Domain Services such as `TransactionPreviewService`) | No (delegates to `GameService`) |
| **GameService** | Yes | Yes (performs mutations and notifies observers) |
| **Application read service** | Yes | No (stateless, read-only operations) |
| **Domain Service** | Yes | No (stateless, read-only operations) |
| **Factory** | No (constructs new objects, does not read state) | No |
| **File layer** | Yes (serialises domain state to disk) | Yes (deserialises saved state into the domain on load) |

The rule is: **read freely, mutate via `GameService`.** Reading derived values from the model is safe at any layer, but every state change passes through `GameService` so observers can be notified consistently.

## Details

### View
- Builds UI components (layouts, controls, styling)
- Presents data from the model (IMPORTANT: can read from the model layer, but never modify it)
- Implements GameObserver and holds fields that should be updated when the game state changes in the onGameUpdated()-override
- Wires user interactions to the controller: the view attaches event handlers to its own UI elements (e.g. `button.setOnAction(...)`), and the handler delegates the actual work to a callback supplied by the controller
- Owns purely visual state (which tab is active, expanded/collapsed sections, sort order in a table)
- Performs presentation-only logic (formatting numbers via shared utilities, choosing a CSS class based on the sign of a value, disabling a button while an input field is empty)
- Performs UI input validation in dialogs: parsing raw text into typed values, presence checks, locale-aware decimal separators, and structural limits on input fields. Does not encode game rules - these are queried from the controller via preview-validation methods (see *Validation layers* below)

### Controller
- Receives user actions from the view through callbacks (e.g. `onBuyClick`, `onConfirm`) and decides which operation to perform
- Owns game-policy rules and exposes them through preview-validation methods (e.g. `validateBuy(quantity, totalInNok) -> Optional<String>`) that views call for live feedback. The same methods are re-checked by the controller before delegating to `GameService`, so the rules have a single source of truth (see *Validation layers* below)
- Reads from the model directly for any non-mutating operation (e.g. `gameService.getPlayer().getMoney()`, `previewService.previewPurchase(...)`)
- Calls `GameService` for any action that changes game state (`gameService.buy(...)`, `gameService.sell(...)`, `gameService.advanceWeek()`)
- Decides which dialog or view to show in response to a user action, and supplies it with the data it needs (e.g. opens a `BuyDialog` for the selected stock and provides a confirmation callback)
- Surfaces errors from the model back to the view (catches domain exceptions and forwards readable messages)
- Supplies the view with action callbacks via constructor parameters or setters

### GameService
- Owns the live game state (`Player`, `Exchange`) and exposes it for reading via `getPlayer()`, `getExchange()`, and `getCurrencyConverter()`
- Performs all state-changing operations on the game (e.g. `buy(...)`, `sell(...)`, `advanceWeek(...)`, `createNewGame(...)`, `loadGame(...)`)
- Coordinates operations that span multiple domain objects, ensuring they happen in the correct order (e.g. a buy involves `Exchange`, the `TransactionFactory`, the `Player`'s portfolio, and the transaction archive)
- Notifies registered observers (`GameObserver`) after each state change, so views can react and refresh
- Provides controllers with a stable, high-level API so that the same operation can be invoked from different parts of the UI without duplicating coordination logic
- Delegates infrastructure work to the file layer for saving and loading game data, without exposing file handling to controllers or views
- Holds no business rules itself; the rules live in the domain model, and `GameService` only orchestrates calls to them

### Application read services
- `PlayerStatsService` - net worth, weekly change, percent change since start, player status
- `PortfolioService` - portfolio market value, share value and return in NOK, total return in NOK and as a percentage
- `RealizedReturnsService` - realized gains, losses, net result, tax, commission, and sale count

All three are stateless: they hold no fields and accept `Player` and `CurrencyConverter` as method parameters on every call. Views instantiate the relevant service as a field and call it from their `refreshDisplay()` / `onGameUpdated()` methods, passing `gameService.getPlayer()` and `gameService.getCurrencyConverter()`. This keeps computed-value logic out of both `GameService` (which would grow unbounded) and the views (which would contain business logic).

### Domain model
- Represents the core concepts of the game as objects (`Player`, `Stock`, `Share`, `Portfolio`, `Exchange`, `Transaction` and its subclasses)
- Holds all business rules and validation (e.g. `Purchase.commit()` rejects a purchase if the player has insufficient funds; `Sale.commit()` rejects a sale if the player does not own the share)
- Performs all calculations on game data, encapsulated in dedicated calculator classes (`PurchaseCalculator`, `SalesCalculator`) that implement a shared `TransactionCalculator` interface
- Exposes derived values as query methods on domain objects (e.g. `Stock.getLatestPriceChange()`, `Share.getReturnPercent()`, `Portfolio.getTotalValue()`), so callers can read computed information without recalculating it themselves
- Provides Domain Services for operations that do not naturally belong to a single object, such as `TransactionPreviewService`, which coordinates a hypothetical purchase or sale across `Stock`, `Share`, `Player`, and the calculators without mutating anything
- Owns the lifecycle of its own data: domain objects validate their own state, reject invalid operations, and only transition between valid states
- Remains independent of the UI, controllers, and infrastructure: the domain has no knowledge of JavaFX, file formats, or how it is being observed

## Validation layers

Validation answers three different questions, and the question being asked determines where the rule lives. Mixing them up tends to produce duplicated rules across dialogs and controllers, or game-specific knowledge leaking into the domain.

| Layer | Question it answers | Lives in | Example |
|---|---|---|---|
| UI input | Is what the user typed parseable and present at all? | Dialog / View | `"abc"` is not a number; quantity field is empty |
| Game policy | Does the game allow this action? | Controller | Minimum 1 NOK transaction value; maximum 4 decimal places on quantity |
| Domain invariant | Will the model still be valid after this? | Domain model | `quantity > 0`; sufficient funds; share is owned by the player |

### UI input validation

UI input validation turns raw text into typed values *before* any business question can be asked. It is concerned with whether the input is parseable and present, not with whether the value would be allowed by the game.

Examples:
- **Presence**: required fields filled in before the submit button becomes enabled
- **Format**: text parses as the expected type (number, date)
- **Locale**: comma vs. dot as decimal separator on Norwegian input
- **Structural limits**: maximum field length, no control characters in a name field

UI input validation lives in the dialog or view that owns the field. It does not ask the controller, because the question "is this string parseable?" has nothing to do with the game. A useful heuristic: if the rule would be identical in a completely different application using the same input widget (e.g. "parse as number" is true of any number field anywhere), it is UI input validation.

### Game policy

Some rules sit between UI input validation and domain invariants: they describe what the game allows but would not corrupt the model if violated. The model would accept `0.00001` shares at any price, and the `Player` constructor would accept any positive starting capital - but the *game* should not.

Examples:
- **Numeric bounds**: minimum transaction value (1 NOK), maximum decimal precision on quantity (4 places)
- **Activity limits**: maximum trades per week, cooldown between week advances
- **Game balance**: starting capital ceiling, maximum portfolio concentration in a single stock

The defining test: would a CLI client or a unit test calling the domain directly produce a valid model if the rule were violated? If yes, the rule is game policy, not a domain invariant.

Game-policy rules live on the controller because:
- They are game-specific, not model-specific. The domain stays usable from contexts that do not share the game's UX assumptions (CLI clients, tests, future variations).
- They need to be queryable from views for live feedback **and** enforced authoritatively before mutation. Putting them on the controller gives both behaviors from one source.

Pattern: `Controller.validateX(...) -> Optional<String>`. An empty `Optional` means the action is allowed; a present value carries the i18n key for the error message to show. Views call the method in their `updateSummary()` / preview hooks to drive button-enabled state and error labels. The controller calls the same method internally in `confirmBuy` / `confirmSell` before delegating to `GameService`. Same rules, one location, two enforcement points.

### Domain invariants

Domain invariants are rules without which the model itself stops making sense. A `Share` with `quantity <= 0` is not a share. A `Player` with a null name is not a player. A `Purchase` that has already been committed cannot be committed again.

The domain enforces its own invariants by throwing exceptions in constructors and mutating methods. Controllers catch these exceptions and surface readable messages back to the view, but never try to prevent the exception by duplicating the rule above the domain - the domain remains the last line of defence, and tests at higher levels rely on it.

## Example: A complete buy operation

To illustrate how the layers cooperate, this section walks through a complete buy operation, from the user clicking a button to the portfolio refreshing.

**1. The user clicks "Buy" on a row in the holdings table.**
`HoldingsCard` (view) has attached an `onAction` handler to the button when the row was built. The handler calls a callback that was passed in by `PortfolioController`:

```java
// In HoldingsCard (view)
buyButton.setOnAction(e -> onBuyClick.accept(stock));
```

**2. The controller decides what to do.**
`PortfolioController.openBuyDialog(stock)` runs. It builds a `BuyDialog`, supplies it with the data it needs (the stock, the current player balance), and registers a confirmation callback for what should happen if the user confirms:

```java
// In PortfolioController
public void openBuyDialog(Stock stock) {
    BuyDialog dialog = new BuyDialog(stock, gameService.getPlayer().getMoney());
    dialog.setOnPreview(quantity -> previewService.previewPurchase(stock, quantity, gameService.getPlayer(), gameService.getCurrencyConverter()));
    dialog.setOnValidate(quantity -> validateBuy(quantity, stock));
    dialog.setOnConfirm(quantity -> confirmBuy(stock, quantity, dialog));
    dialog.show();
}
```

**3. The dialog (view) displays itself and reacts to user input.**
As the user types a quantity, `BuyDialog` parses the text into a `BigDecimal` (UI input validation: empty / unparseable input disables the confirm button without involving the controller). For parseable input, the dialog requests a fresh `TransactionPreview` through the preview callback and updates the gross/commission/total labels. It also asks the controller for a game-policy verdict via `onValidate`, which returns an `Optional<String>` - if present, the dialog shows the error and keeps the confirm button disabled. No business logic runs in the dialog itself; it only displays values returned by the model and decisions returned by the controller.

**4. The user clicks "Confirm".**
The dialog's confirm handler invokes the callback the controller registered. Control returns to the controller's `confirmBuy` method, which re-checks the same game-policy rules before delegating to `GameService`:

```java
// In PortfolioController
private void confirmBuy(Stock stock, BigDecimal quantity, BuyDialog dialog) {
    Optional<String> policyError = validateBuy(quantity, stock);
    if (policyError.isPresent()) {
        dialog.showError(policyError.get());
        return;
    }
    try {
        Transaction t = gameService.buy(stock.getSymbol(), quantity);
        dialog.close();
        showReceipt(t);
    } catch (InsufficientFundsException e) {
        dialog.showError(e.getMessage());
    }
}
```

**5. `GameService` performs the operation and notifies observers.**
`GameService.buy(...)` coordinates the actual purchase: it asks `Exchange` to create the transaction (via `TransactionFactory`), commits it on the player, and finally notifies all registered observers. Domain invariants are enforced here - the `Purchase.commit()` call throws `InsufficientFundsException` if the player does not have enough money, regardless of what any higher layer thought:

```java
// In GameService
public Transaction buy(String symbol, BigDecimal quantity) {
    Transaction t = exchange.buy(symbol, quantity, player);
    notifyObservers();
    return t;
}
```

**6. The views refresh.**
Every view that implements `GameObserver` (the holdings table, the net-worth card, the available-funds card, etc.) receives `onGameUpdated()` and re-reads the values it displays from the model. The user sees the new share appear, the balance drop, and the portfolio value update - all without any view explicitly telling another view to refresh.

This single flow exercises every responsibility in the architecture: the view captures input and reflects state (with its own UI input validation), the controller orchestrates the response and enforces game policy, `GameService` mutates the model and broadcasts the change, the domain model performs the actual work and guards its own invariants, and the file layer is not involved at all (since this operation does not persist to disk).

### Sources
Pragmatic Coding - "An Introduction to Model-View-Controller-Interactor": https://www.pragmaticcoding.ca/javafx/Mvci-Introduction

Pragmatic Coding - "FXML is NOT Model-View-Controller": https://www.pragmaticcoding.ca/javafx/fxml_isnt_mvc