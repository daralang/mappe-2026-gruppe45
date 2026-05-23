# View and CSS conventions

## Overview

Styling in this project is split across several CSS files loaded by `StylesheetLoader`. `tokens.css` is loaded first and defines design tokens. `style.css` is a manifest that imports all other files in order.

Colors have one source of truth: `tokens.css`. Typography visual definitions have one source of truth: `typography.css`. `StyledText` is not a second source - it is the enforced access point that prevents raw `new Label()` + `getStyleClass().add()` calls in Java code. Component-specific rules live in their own file.

---

## CSS file structure

| File | Responsibility |
|---|---|
| `tokens.css` | Design tokens (colors only). No selectors beyond `.root`. |
| `typography.css` | Shared text roles (font size, weight, fill). No padding or margin. |
| `title.css` | Large heading styles used on the start screen (`heading-1`, `paragraph-1`). |
| `navbar.css` | Navbar layout and text. |
| `tabs.css` | Tab bar and tab button states. |
| `layout.css` | Card, scroll area, content area, advance button. |
| `holdings.css` | Holdings table - headers, cells, action links, chevron. |
| `modal.css` | Modal overlay, header, body, inputs, buttons, summary box, receipts. |
| `utilities.css` | Stateless modifier classes: `.positive`, `.negative`, `.bold`. |

`style.css` imports these files in order and contains no rules of its own.

---

## Design tokens (`tokens.css`)

All colors are defined as JavaFX looked-up colors in `.root`. Use these token names in every other CSS file - never write a raw hex value.

```css
/* Correct */
-fx-text-fill: text-secondary;
-fx-background-color: danger-bg;

/* Wrong */
-fx-text-fill: #888888;
-fx-background-color: #fdecea;
```

### Adding a new token

Add it to `tokens.css` under the relevant group (danger, success, surface, etc.), give it a descriptive name, and use it immediately in whichever rule requires it. Do not add tokens speculatively - only add one when you are also using it.

### Token groups

| Group | Tokens |
|---|---|
| Primary | `primary`, `primary-dark` |
| Danger | `danger`, `danger-dark`, `danger-text`, `danger-bg` |
| Success | `success`, `success-bg`, `success-text`, `success-border` |
| Text | `text-primary`, `text-secondary`, `text-muted` |
| Border | `border-strong`, `border`, `border-light` |
| Surface | `surface`, `surface-alt` |

---

## Typography - `StyledText` and `typography.css`

`typography.css` is the source of truth for what each text role looks like. `StyledText` is the only way to apply a role in Java code - it ensures the correct CSS class is applied and prevents the class name from being scattered as raw strings across the codebase.

```java
// Correct
StyledText label = StyledText.detailLabel("Aksje");
StyledText value = StyledText.widgetValue();

// Wrong
Label label = new Label("Aksje");
label.getStyleClass().add("detail-label");
```

### Available roles

| Factory method | CSS class | Typical use |
|---|---|---|
| `StyledText.pageTitle(...)` | `page-title` | Top-level view title |
| `StyledText.sectionTitle(...)` | `section-title` | Section heading within a view |
| `StyledText.weekLabel(...)` | `week-label` | Week indicator in the week bar |
| `StyledText.widgetLabel(...)` | `widget-label` | Small grey label above a widget value |
| `StyledText.widgetValue(...)` | `widget-value` | Large bold number in a widget card |
| `StyledText.widgetChange(...)` | `widget-change` | Change line below a widget value |
| `StyledText.detailLabel(...)` | `detail-label` | Row label in a modal, dialog, or receipt |
| `StyledText.detailValue(...)` | `detail-value` | Row value in a modal, dialog, or receipt |
| `StyledText.headingOne(...)` | `heading-1` | Large heading on the start screen |
| `StyledText.paragraphOne(...)` | `paragraph-1` | Body text on the start screen |

Each role has a no-arg overload (empty label) and a `(String text)` overload. Use whichever matches the call site.

### Padding rule

Typography classes define **zero padding**. Spacing between text elements is the container's responsibility (set via `setSpacing(...)` on the parent `VBox` or `HBox`, or `setPadding(...)` on the card). This keeps roles reusable without inheriting unwanted space.

### Adding a new text role

1. Add the CSS class to `typography.css` with font properties only (no padding).
2. Add two factory methods to `StyledText` - one no-arg, one with a `String text` argument.
3. Use the factory method everywhere the role appears; do not mix direct `Label` construction with `getStyleClass()` for the same role.

---

## Table cells — `TableCells` and `holdings.css`

`holdings.css` is the source of truth for what header and data cells look like. `TableCells` is the only way to create those cells in Java code - it ensures the correct CSS class is applied and prevents raw `new Label()` + `getStyleClass().add()` calls from scattering class names across the codebase.

```java
// Correct
Label cell   = TableCells.data(stock.getSymbol());
Label header = TableCells.header("Company");

// Wrong
Label cell = new Label(stock.getSymbol());
cell.getStyleClass().add("holdings-cell");
```

### Cell factories

| Factory method | CSS class(es) | Typical use |
|---|---|---|
| `TableCells.header(text)` | `holdings-header` | Non-interactive column header label |
| `TableCells.sortHeader(text, active, ascending, onClick)` | `holdings-header` | Sortable column header button |
| `TableCells.data(text)` | `holdings-cell` | Plain data cell |
| `TableCells.boldData(text)` | `holdings-cell`, `bold` | Bold data cell for total rows |
| `TableCells.empty(text)` | `holdings-empty` | Centered empty-state message |

Specialised cells that carry more than a typography class belong elsewhere: use `ChangeFormatter.styledAmount` / `styledPercent` for sign-colored values, component CSS for badges, and manual `Button` construction for action links. Do not add styling shortcuts for one-off cases to `TableCells`.

### Grid helpers

Three methods write directly to a `GridPane` and remove boilerplate that every table card would otherwise duplicate:

- `configureColumns(grid, widths, alignments)` — applies percentage widths and horizontal alignments from parallel arrays. Call once during construction; does not clear existing constraints.
- `addHeaderRow(grid, headers)` — writes one `header(text)` cell per entry at row 0.
- `renderEmptyState(grid, message, columnCount)` — adds the `empty(text)` label at row 1, centered and spanning all columns.

### Adding a new cell factory

1. Add the CSS class to `holdings.css`.
2. Add a static factory method to `TableCells` that applies it.
3. Use the factory method everywhere that cell type appears — do not mix direct `Label` construction for the same role.

---

## Component-specific CSS

Rules that only apply to one component belong in that component's CSS file (e.g. `holdings.css`, `modal.css`). Use a consistent prefix so the scope is immediately clear:

- `.holdings-*` - holdings table only
- `.modal-*` - modal infrastructure, dialogs, receipts
- `.navbar-*` - navbar only

Do not use a shared typography class when the component genuinely needs different sizing. `modal-section-value` (16px bold) exists because modal section values are intentionally larger than `detail-value` (13px) - that distinction is kept in `modal.css`.

---

## Modifier classes (`utilities.css`)

`.positive`, `.negative`, and `.bold` are stateless modifier classes that can be combined with any text node. Apply them alongside a `StyledText` role - they do not replace it.

```java
StyledText value = StyledText.detailValue("+ 1 234,00 NOK");
value.getStyleClass().add("positive");
```

Never encode positive/negative coloring directly in a typography class or in a component-specific rule. Always apply it through these shared modifiers so the color comes from the `success` and `danger` tokens in `tokens.css`.

---

## `WidgetCard` base class

All read-only dashboard cards extend `WidgetCard`, which provides:
- A `titleLabel` (`StyledText.widgetLabel()`) wired to the i18n key
- `onLanguageChanged()` - refreshes the title and calls `refreshDisplay()`
- `onGameUpdated()` - calls `refreshDisplay()`

Subclasses implement only `refreshDisplay()`. Do not override `onLanguageChanged()` or `onGameUpdated()` unless the card has additional update logic (e.g. `NetWorthCard` also appends a chart data point in `onGameUpdated()`).

---

## What not to do

- Do not write raw hex colors in any file other than `tokens.css`.
- Do not put padding or margin in `typography.css` classes.
- Do not create a `Label` and manually add a typography class name - use `StyledText`.
- Do not define new colors in component CSS files - add a token to `tokens.css` first.
- Do not add tokens to `tokens.css` without using them immediately.
- Do not duplicate CSS rules across files - if two components need the same style, extract a shared class.
