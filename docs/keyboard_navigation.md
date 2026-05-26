# Keyboard Navigation

This document describes how keyboard navigation is implemented in the codebase, what shortcuts are available to a player, and how the underlying classes cooperate to make it work.

---

## Shortcut reference

### During gameplay (in-game screen)

| Keys | Action |
|---|---|
| `Cmd/Ctrl + 1` | Switch to the Dashboard view |
| `Cmd/Ctrl + 2` | Switch to the Exchange view |
| `Cmd/Ctrl + 3` | Switch to the Leaderboard view |
| `Cmd/Ctrl + Enter` | Advance one week |
| `Cmd/Ctrl + S` | Save the current game |
| `Cmd/Ctrl + F` | Focus the search bar in the active view |
| `Shift + 1–4` | Switch to tab 1–4 within the current view (e.g. Stocks / Portfolio sub-tabs on Exchange) |
| `Enter` / `Space` | Activate (click) the currently focused button |

### On the start screen

| Keys | Action |
|---|---|
| `Shift + 1` | Switch to the New Game tab |
| `Shift + 2` | Switch to the Load Game tab |

### In tables and search bars

| Keys | Action |
|---|---|
| `↑` / `↓` | Move selection up/down through table rows |
| `Enter` / `Space` (on a row) | Open the detail view or confirm the selected row |
| `↑` at the first row | Releases the key to `PageArrowDispatcher`, which scrolls the page up instead |
| `↓` at the last row | Releases the key to `PageArrowDispatcher`, which scrolls the page down instead |
| `↓` in search field | Moves focus to the first result row (if results exist); otherwise scrolls the page |
| `Enter` in search field | Triggers the search |
| `Escape` in search field | Clears the search field and returns focus to its parent container |

### In dialogs and modals

All dialogs and modals extend the base `Modal` class, which installs a scene-level key handler that applies to every dialog automatically.

| Keys | Action |
|---|---|
| `Escape` | Closes the dialog and discards any unsaved input |
| `Enter` / `Space` | Activates the currently focused button inside the dialog |

Note that `GameOverModal` and `ForcedSaleDialog` intentionally override `close()` as a no-op, so `Escape` has no effect in those dialogs. The player must make an explicit choice before dismissing them.

### General navigation

| Keys | Action |
|---|---|
| `Tab` / `Shift+Tab` | Standard JavaFX focus traversal through interactive controls |
| `↑` / `↓` (no focused control claiming them) | Scrolls the active page by 40 px per step |

---

## Architecture overview

Keyboard handling is split across a small package (`keyboard/`) with three concerns: shortcut dispatch, arrow-key routing, and focus management.

**`KeyboardNavigationService`** is the single entry point. It attaches one `EventFilter` to the app-lifetime `Scene` and dispatches every `KEY_PRESSED` event through two ordered registries:

1. The **universal registry** - shortcuts that fire in any context (e.g. Enter to click the focused button).
2. The **global registry** - application-wide shortcuts that are active during normal gameplay.

Modal dialogs are naturally isolated: each dialog runs in its own `APPLICATION_MODAL` `Stage`, so global shortcuts on the main scene cannot fire while a dialog has focus.

**`ShortcutRegistry`** backs both registries. It maps `KeyCombination` -> action in insertion order; the first match wins. Actions can be unconditional (`Runnable`) or conditional (`BooleanSupplier`), where returning `false` lets the event propagate.

**`PageArrowDispatcher`** is installed in the capture phase on the window root so it sees arrow keys before any focused control. When UP or DOWN is pressed it checks, in order:

1. Whether the focused node (or any ancestor) has registered a `VerticalArrowHandler` under the property key `millions.verticalArrowHandler`. If it has, the handler gets first refusal.
2. Whether the focused node is a `TextArea`, `ComboBox`, or `Spinner`, which keep arrows natively.
3. Otherwise it delegates to the active `PageScroller` (the `KeyboardScrollPane` wrapping the current view).

**`ArrowKeyNavigator`** is a reusable helper that tracks a selected index and moves it in response to UP, DOWN, Enter, and Space events. Table components wire it to their row list.

**`FocusRestorer`** snapshots the focused node before a UI rebuild and restores it (or falls back to a supplied node) afterwards via `Platform.runLater`.

---

## How the layers cooperate

`MainController` calls `keyboardService.bindToNode(view.getRoot())`, which attaches the service to the scene as the root node enters it. All shortcuts are registered once in `registerShortcuts()`.

Tab switching via `Shift+1–4` goes through `TabNavigationRegistry`, which holds at most one active `IntConsumer` at a time. Each view that has tabs registers its navigator as active when it becomes visible and clears it when it is replaced.

`Cmd/Ctrl+F` goes through `SearchFocusRegistry`, which holds the currently active `SearchFocusProvider`. Views that contain a search bar implement this interface; views without one implement it as a no-op.

When the application switches views, `PageFocusProvider.focusPageEntry()` is called on the incoming view to land focus on the most appropriate control, so the player can immediately use the keyboard without clicking first.

---

## Key classes and interfaces

| Class / Interface | Responsibility |
|---|---|
| `KeyboardNavigationService` | Attaches to the scene; owns and dispatches through both registries |
| `ShortcutRegistry` | Maps `KeyCombination` → action; evaluates in insertion order |
| `PageArrowDispatcher` | Routes UP/DOWN in capture phase; delegates to handlers or `PageScroller` |
| `ArrowKeyNavigator` | Tracks selection index; handles UP/DOWN/Enter/Space for linear lists |
| `KeyboardScrollPane` | Implements `PageScroller`; scrolls 40 px per arrow step |
| `VerticalArrowHandler` | Strategy interface a control registers to claim arrow keys |
| `FocusRestorer` | Snapshots and restores focus around UI rebuilds |
| `TabNavigationRegistry` | Tracks the active tab navigator; delegates `Shift+1–4` to it |
| `SearchFocusRegistry` | Tracks the active search provider; delegates `Cmd/Ctrl+F` to it |
| `PageFocusProvider` | Interface for views that expose a keyboard entry point |
| `SearchFocusProvider` | Interface for views that contain a search bar |
