# Separation of Concerns for this project

## Summary
## Application layers

**View** (`view/`)
Builds the JavaFX user interface and captures user input. Reads from the model, observes state changes, and delegates user actions to controllers via callbacks. Contains no business logic and never mutates the model directly.

**Controller** (`controller/`)
Translates user input into calls on `GameManager` for actions that change game state, and reads from the model directly when only displaying or previewing data. Validates UI input and orchestrates dialogs. Contains no business logic itself.

**GameManager** (`manager/`)
Service Layer. Owns the game state (`Player`, `Exchange`), coordinates operations that span multiple domain objects, and notifies observers after each change. Provides controllers with a stable API for performing actions.

**Domain model** (`model/`)
Holds all business logic and business rules. The domain objects own their own state and validation. Operations that do not naturally belong to a single domain object are placed in dedicated Domain Services, such as `TransactionPreviewService`, which coordinates calculations across multiple domain objects without mutating anything.

**Factory** (`factory/`)
Centralised creation of `Transaction` objects. Used by `Exchange` and controllers to avoid direct instantiation of concrete subclasses.

**File layer** (`file/`)
Infrastructure for reading and writing game data. Called from `GameManager` or controllers; never directly from the view or the domain.

## Read/write access matrix

The following table summarises which layers may read from and write to the domain model:

| Layer | Read from model? | Mutate model? |
|---|---|---|
| **View** | Yes (getters, derived values) | No |
| **Controller** | Yes (getters, Domain Services such as `TransactionPreviewService`) | No (delegates to `GameManager`) |
| **GameManager** | Yes | Yes (performs mutations and notifies observers) |
| **Domain Service** | Yes | No (stateless, read-only operations) |
| **Factory** | No (constructs new objects, does not read state) | No |
| **File layer** | Yes (serialises domain state to disk) | Yes (deserialises saved state into the domain on load) |

The rule is: **read freely, mutate via `GameManager`.** Reading derived values from the model is safe at any layer, but every state change passes through `GameManager` so observers can be notified consistently.

## Details

### View
- Builds UI components (layouts, controls, styling)
- Presents data from the model (IMPORTANT: can read from the model layer, but never modify it)
- Implements GameObserver and holds fields that should be updated when the game state changes in the onGameUpdated()-override
- Wires user interactions to the controller: the view attaches event handlers to its own UI elements (e.g. `button.setOnAction(...)`), and the handler delegates the actual work to a callback supplied by the controller
- Owns purely visual state (which tab is active, expanded/collapsed sections, sort order in a table)
- Performs presentation-only logic (formatting numbers via shared utilities, choosing a CSS class based on the sign of a value, disabling a button while an input field is empty)

### Controller
- Receives user actions from the view through callbacks (e.g. `onBuyClick`, `onConfirm`) and decides which operation to perform
- Validates UI input before delegating (e.g. that a quantity field is filled in and contains a valid number)
- Reads from the model directly for any non-mutating operation (e.g. `gameManager.getPlayer().getMoney()`, `previewService.previewPurchase(...)`)
- Calls `GameManager` for any action that changes game state (`gameManager.buy(...)`, `gameManager.sell(...)`, `gameManager.advanceWeek()`)
- Decides which dialog or view to show in response to a user action, and supplies it with the data it needs (e.g. opens a `BuyDialog` for the selected stock and provides a confirmation callback)
- Surfaces errors from the model back to the view (catches domain exceptions and forwards readable messages)
- Supplies the view with action callbacks via constructor parameters or setters

### GameManager
- Owns the live game state (`Player`, `Exchange`) and exposes it for reading
- Performs all state-changing operations on the game (e.g. `buy(...)`, `sell(...)`, `advanceWeek(...)`, `startNewGame(...)`, `loadGame(...)`)
- Coordinates operations that span multiple domain objects, ensuring they happen in the correct order (e.g. a buy involves `Exchange`, the `TransactionFactory`, the `Player`'s portfolio, and the transaction archive)
- Notifies registered observers (`GameObserver`) after each state change, so views can react and refresh
- Provides controllers with a stable, high-level API so that the same operation can be invoked from different parts of the UI without duplicating coordination logic
- Delegates infrastructure work to the file layer for saving and loading game data, without exposing file handling to controllers or views
- Holds no business rules itself; the rules live in the domain model, and `GameManager` only orchestrates calls to them

### Domain model
- Represents the core concepts of the game as objects (`Player`, `Stock`, `Share`, `Portfolio`, `Exchange`, `Transaction` and its subclasses)
- Holds all business rules and validation (e.g. `Purchase.commit()` rejects a purchase if the player has insufficient funds; `Sale.commit()` rejects a sale if the player does not own the share)
- Performs all calculations on game data, encapsulated in dedicated calculator classes (`PurchaseCalculator`, `SalesCalculator`) that implement a shared `TransactionCalculator` interface
- Exposes derived values as query methods on domain objects (e.g. `Stock.getLatestPriceChange()`, `Share.getReturnPercent()`, `Portfolio.getTotalValue()`), so callers can read computed information without recalculating it themselves
- Provides Domain Services for operations that do not naturally belong to a single object, such as `TransactionPreviewService`, which coordinates a hypothetical purchase or sale across `Stock`, `Share`, `Player`, and the calculators without mutating anything
- Owns the lifecycle of its own data: domain objects validate their own state, reject invalid operations, and only transition between valid states
- Remains independent of the UI, controllers, and infrastructure: the domain has no knowledge of JavaFX, file formats, or how it is being observed

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
    BuyDialog dialog = new BuyDialog(stock, gameManager.getPlayer().getMoney());
    dialog.setOnPreview(quantity -> previewService.previewPurchase(stock, quantity, gameManager.getPlayer()));
    dialog.setOnConfirm(quantity -> confirmBuy(stock, quantity, dialog));
    dialog.show();
}
```

**3. The dialog (view) displays itself and reacts to user input.**
As the user types a quantity, `BuyDialog` requests a fresh `TransactionPreview` through the preview callback and updates the gross/commission/total labels. No business logic runs in the dialog itself - it only shows the values returned by the model.

**4. The user clicks "Confirm".**
The dialog's confirm handler invokes the callback the controller registered. Control returns to the controller's `confirmBuy` method:

```java
// In PortfolioController
private void confirmBuy(Stock stock, BigDecimal quantity, BuyDialog dialog) {
    try {
        Transaction t = gameManager.buy(stock.getSymbol(), quantity);
        dialog.close();
        showReceipt(t);
    } catch (InsufficientFundsException e) {
        dialog.showError(e.getMessage());
    }
}
```

**5. The manager performs the operation and notifies observers.**
`GameManager.buy(...)` coordinates the actual purchase: it asks `Exchange` to create the transaction (via `TransactionFactory`), commits it on the player, and finally notifies all registered observers:

```java
// In GameManager
public Transaction buy(String symbol, BigDecimal quantity) {
    Transaction t = exchange.buy(symbol, quantity, player);
    notifyObservers();
    return t;
}
```

**6. The views refresh.**
Every view that implements `GameObserver` (the holdings table, the net-worth card, the available-funds card, etc.) receives `onGameUpdated()` and re-reads the values it displays from the model. The user sees the new share appear, the balance drop, and the portfolio value update - all without any view explicitly telling another view to refresh.

This single flow exercises every responsibility in the architecture: the view captures input and reflects state, the controller orchestrates the response, the manager mutates the model and broadcasts the change, the domain model performs the actual work, and the file layer is not involved at all (since this operation doesn't persist to disk).

### Sources
Pragmatic Coding - "An Introduction to Model-View-Controller-Interactor": https://www.pragmaticcoding.ca/javafx/Mvci-Introduction

Pragmatic Coding - "FXML is NOT Model-View-Controller": https://www.pragmaticcoding.ca/javafx/fxml_isnt_mvc