# feat: add full keyboard navigation

## Summary

Before this PR, the application could only be used with a mouse. This PR adds
keyboard support throughout the entire app — navigation between screens, opening
and confirming dialogs, searching, and navigating lists and tables can now all be
done from the keyboard alone.

The implementation is built around a new `keyboard/` package that acts as the
single source of truth for all keyboard behaviour. Everything routes through one
central dispatcher, which means shortcuts are consistent, don't conflict with each
other, and are automatically suppressed when a modal dialog is open.

---

## Architecture

All keyboard logic lives in the new `keyboard/` package and is independent of any
specific screen or component. The key pieces are:

- **`KeyboardNavigationService`** — the central dispatcher. One instance per screen
  (start screen and main game each have their own). Receives every key press from the
  scene and decides what to do with it. The owner controller is responsible for calling
  `detach()` before navigating away from its screen; `bindToNode` also handles this
  automatically when the scene graph is torn down.

- **`ShortcutRegistry`** — a map from key combination to action. Supports conditional
  actions that can choose to pass an event through (used for the Enter key, which fires
  buttons but leaves text fields and dropdowns alone).

- **`KeyboardContext`** — an interface for modal isolation. When a dialog opens, it
  pushes itself onto a stack inside `KeyboardNavigationService`. While it is on the
  stack, all global shortcuts are suppressed. When the dialog closes, it pops itself
  and global shortcuts resume. This prevents shortcuts like `Cmd+Enter` (advance week)
  from firing accidentally while a buy dialog is open.

- **`ArrowKeyNavigator`** — reusable up/down or left/right navigation for any list or
  table. Supports both clamping (stops at first/last item) and wraparound.

- **`FocusRestorer`** — saves which element had focus before a UI rebuild, then
  restores it afterwards. Used when dialogs open and close so focus returns to the
  right place.

- **`SearchFocusRegistry`** and **`TabNavigationRegistry`** — thin delegation layers
  that forward `Cmd+F` and `Shift+1–4` to whichever view is currently visible, without
  the shortcut registration needing to know anything about the view hierarchy.

### Dispatch priority

Every key press goes through three layers in order:

1. **Universal shortcuts** — always fire, even inside a modal (e.g. Enter fires the
   focused button)
2. **Active context** — the top modal on the stack gets first refusal
3. **Global shortcuts** — fire only when no modal is blocking them

---

## Shortcuts

### Global — available from anywhere in the main game

| Keys | Action |
|---|---|
| `Cmd/Ctrl + 1` | Go to Dashboard |
| `Cmd/Ctrl + 2` | Go to Exchange |
| `Cmd/Ctrl + 3` | Go to Leaderboard |
| `Cmd/Ctrl + S` | Save game |
| `Cmd/Ctrl + Enter` | Advance week |
| `Cmd/Ctrl + F` | Focus search field in the active view |
| `Shift + 1–4` | Switch between tabs in the active view |

### Universal — always active, even inside dialogs

| Keys | Action |
|---|---|
| `Enter` | Fires the focused button (passes through in text fields and dropdowns) |

### Navigation within views

| Keys | Where | Action |
|---|---|---|
| `← / →` | Dashboard tab bar | Switch between tabs |
| `↑ / ↓` | Holdings table | Move between rows |
| `↑ / ↓` | Available loans list | Move between loans |

### Start screen

| Keys | Action |
|---|---|
| `Tab` | Move between name, capital, file zone, and start button |
| `Enter` in name field | Jump to capital field |
| `Enter` in capital field | Fire the start button |
| `Enter` or `Space` on currency selector | Open the dropdown |
| `Shift + 1–2` | Switch between New Game and Load Game tabs |

### Dialogs

All dialogs push a `KeyboardContext` when opened, which suppresses global shortcuts
for the duration. When the dialog closes, the context is popped and shortcuts resume.

| Keys | Action |
|---|---|
| `Enter` | Confirm |
| `Escape` | Close / cancel |
| Auto-focus | First input field receives focus when any dialog opens |

---

## Bug fixes included in this PR

- **Memory leak** — the scene listener added by `bindToNode` is now correctly removed
  in `detach()`, so nothing holds a reference to the old scene after navigation.
- **Event filter leak** — the key event handler reference is stored and removed
  explicitly, instead of using an anonymous lambda that could not be unregistered.
- **Tab order** — the clear button in the search bar is excluded from tab traversal
  when it is not visible.
- **Tab indicator sync** — the active tab indicator now updates correctly when
  switching tabs via keyboard (previously it only updated on mouse clicks).
- **`CurrencySelector` initial state** — the currency dropdown was enabled at startup,
  letting users select a currency before any stock file had been loaded. It now starts
  disabled and only becomes interactive after a file is selected.

---

## Tests

The entire `keyboard/` package is covered by four test classes. Tests are grouped by
method and cover positive cases, negative cases, and edge cases.

| Test class | Tests | What it covers |
|---|---|---|
| `ShortcutRegistryTest` | 15 | Dispatch order, conditional actions, tab shortcut helpers, clearing |
| `ArrowKeyNavigatorTest` | 46 | Both orientations, clamping, wraparound, empty list, single-item list, index sync |
| `FocusRestorerTest` | 14 | Snapshot and restore lifecycle, stale node detection, fallback, FX thread sync |
| `KeyboardNavigationServiceTest` | 38 | Dispatch priority, push/pop lifecycle, `detach` clearing all state, registry identity |

`KeyboardNavigationServiceTest` uses a package-private `fireKeyEvent` method instead
of reflection to invoke the dispatch logic. This keeps the tests decoupled from
implementation details — renaming or restructuring internal methods will not break the
tests as long as the observable behaviour stays the same.

---

## Notes on scope

- `MainController` and `StartController` each create their own `KeyboardNavigationService`
  instance attached to their own scene. The two instances are never active at the same
  time — when the start screen is showing, the main controller's service is detached,
  and vice versa. Shortcut sets from the two screens therefore cannot conflict.
- The `keyboard/` package has no dependency on the `view/` package. Views implement
  `SearchFocusProvider` (defined in `keyboard/`) and register themselves with
  `SearchFocusRegistry` and `TabNavigationRegistry`, not the other way around.
