# feat: full keyboard navigation

## Overview

This PR introduces a complete keyboard navigation system across the entire application.
Before this PR, no views, dialogs, or screens had keyboard support. After this PR, the
game can be played almost entirely from the keyboard.

---

## New package: `keyboard/`

All infrastructure is collected in a dedicated package that is independent of JavaFX
scenes and views.

| Class | Responsibility |
|---|---|
| `KeyboardNavigationService` | Central dispatcher. Installs a single `EventFilter` on the scene and routes events in priority order: universal → active context → global |
| `ShortcutRegistry` | `LinkedHashMap`-based mapping from `KeyCombination` to action. Supports conditional actions (`BooleanSupplier`) that can let the event pass through |
| `KeyboardContext` | Interface for modal isolation. Modals push themselves onto the context stack and pop on close, suppressing global shortcuts while the modal is open |
| `ArrowKeyNavigator` | Generic arrow-key navigation for lists and tables. Supports `VERTICAL` and `HORIZONTAL` orientation, clamping, and wraparound |
| `FocusRestorer` | Snapshot/restore of JavaFX focus across UI rebuilds. `wrap(scene, rebuild, fallback)` takes a snapshot, runs the rebuild, then restores focus |
| `SearchFocusRegistry` | Delegates `Cmd/Ctrl+F` to the search field belonging to the currently visible view |
| `TabNavigationRegistry` | Delegates `Shift+1–4` to the tab navigator of the currently active view |
| `SearchFocusProvider` | Interface implemented by views that contain a search field |

---

## Registered shortcuts

### Universal — active even when a modal is open

| Shortcut | Action |
|---|---|
| `Enter` | Fires the focused button (passes through in text fields and ComboBox) |

### Global — suppressed when a modal context is active

| Shortcut | Action |
|---|---|
| `Cmd/Ctrl+1` | Navigate to Dashboard |
| `Cmd/Ctrl+2` | Navigate to Exchange |
| `Cmd/Ctrl+3` | Navigate to Leaderboard |
| `Cmd/Ctrl+S` | Save game |
| `Cmd/Ctrl+Enter` | Advance week |
| `Cmd/Ctrl+F` | Focus search field in the active view |
| `Shift+1–4` | Navigate between tabs in the active view |

### Per-context — only active in the relevant view or dialog

| Shortcut | Context | Action |
|---|---|---|
| `←` / `→` | `ViewHeader` | Switch between Dashboard tabs |
| `↑` / `↓` | `HoldingsCard` | Navigate rows in the holdings table |
| `↑` / `↓` | `AvailableLoansCard` | Navigate available loans |
| `Shift+1–2` | Start screen | Switch between New Game / Load Game tabs |

### Start screen

| Action | Behaviour |
|---|---|
| `Tab` | Navigates between name, capital, file zone, and start button |
| `Enter` in name field | Jumps to capital field |
| `Enter` in capital field | Fires the start button |
| `Enter` / `Space` on currency selector | Opens the dropdown |
| `Enter` / `Space` on file drop zone | Opens the file chooser |

### Dialogs

| Dialog | Behaviour |
|---|---|
| Buy / Sell | `Enter` confirms, `Escape` closes, arrow keys in fields work normally |
| Loan application | `Enter` in amount field confirms |
| All modals | Push a `KeyboardContext` onto the stack — global shortcuts are suppressed while the modal is open |
| All dialogs | Auto-focus on the first input field when opened |

---

## Bug fixes and refactoring

- **Memory leak** — `bindToNode` now removes the `sceneProperty` listener in `detach()` so nothing holds a reference to the scene after navigation
- **Event filter leak** — the `EventHandler` reference is stored and removed explicitly rather than using a lambda directly on `removeEventFilter`
- **Tab order** — the clear button in `SearchBar` is excluded from tab traversal when hidden
- **Tab indicator** — now synchronised correctly on keyboard navigation (previously only updated on mouse click)
- **`detach()` on screen transition** — `KeyboardNavigationService` is reset before navigating to the start screen to prevent double registration
- **`SearchFocusProvider` layer inversion** — `keyboard.SearchFocusRegistry` previously imported from the `view` package. `SearchFocusProvider` has been moved to `keyboard/`, and the old `view` variant is kept as a `@Deprecated` wrapper for backward compatibility
- **`CurrencySelector` initial state** — the selector was enabled at startup, allowing currency selection before any file was loaded; changed to `setDisable(true)` by default, enabled only after `onFileSelected()` is called

---

## Test coverage — `keyboard/` package

| Test class | Tests | Covers |
|---|---|---|
| `ShortcutRegistryTest` | 15 | Dispatch order, conditional actions, `registerTabShortcuts`, `clear()` |
| `ArrowKeyNavigatorTest` | 46 | Vertical/horizontal orientation, clamping, wraparound, empty list, single-item list, `syncIndex`, `select` |
| `FocusRestorerTest` | 14 | `snapshot`/`restore`/`wrap` lifecycle, stale-node detection, fallback invocation, FX-thread synchronisation via `CountDownLatch` |
| `KeyboardNavigationServiceTest` | 38 | Dispatch order, push/pop lifecycle callbacks, `detach` clearing all state, registry identity, reflection-based access to private `onKeyPressed` |

All tests follow the AAA pattern and cover positive cases, negative cases, and edge cases.

---

## Files changed

47 commits, 70+ files changed — see the commit history for the full list.
