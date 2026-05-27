# Millions

A stock-market simulation game built with JavaFX. Start with a sum of cash, trade
real S&P 500 companies week by week, take out loans, build a portfolio, and try to
grow your net worth before retiring onto the leaderboard.

## Release v3.0 - Final Prototype

This release represents the final prototype delivered at the end of the project,
implementing the core user stories from the vision document.

### Game flow

- Start a new game with a player name, starting capital, and an optional currency choice
- Upload your own stock data as a `.csv` file, or play with the bundled S&P 500 dataset
- Resume a previous run by loading a saved game file (`.json`)
- Advance the game one week at a time - stock prices move every week
- Save your game at any point and pick it up later
- Retire to lock in your result, or play until bankruptcy ends the game

### Trading

- Browse the exchange: an overview tab and a searchable list of stocks
- Open a stock to see its price history and details before trading
- Buy and sell shares with a live preview of gross amount, commission, and total
- Track every trade in the transaction history
- Keep an eye on stocks of interest with a personal watchlist

### Portfolio and dashboard

- Personal dashboard with portfolio value, holdings, and per-share return
- Total return shown in NOK and as a percentage
- Realized returns broken down into gains, losses, tax, and commission
- Net-worth history chart that updates as the weeks advance

### Loans

- Apply for loans up to a capacity tied to your net worth (max 50%)
- Weekly interest accrues on outstanding debt
- Repay loans early to save on interest
- Loan ledger separating disbursements, interest, and repayments
- A forced sale is triggered if you cannot cover your obligations when a loan matures

### Leaderboard, notifications and quality-of-life

- Persistent leaderboard ranking players by return percent and final net worth
- In-app notifications for due loans, high debt ratios, low balance, large price moves, and status changes
- Toast messages for quick feedback on actions
- Player status tiers (Novice, Investor, Speculator, ...) that change as you grow
- Full keyboard navigation
- Bilingual interface (English / Norwegian) switchable at runtime
- Multi-currency support with conversion to and from NOK

Note: All market data is simulated for the purposes of the game - no real trades are
made and no real money is involved.

## Prerequisites

Make sure the following are installed before running the application:

| Tool       | Version | Download                       |
|------------|---------|--------------------------------|
| Java (JDK) | 25+     | https://adoptium.net           |
| Maven      | 3.9+    | https://maven.apache.org       |

The project targets Java 25 and JavaFX 25. JavaFX and all other dependencies are
resolved automatically by Maven on the first build.

## Getting Started

### 1. Download and unzip the project

Download the zip file from the release page, unzip it, and open a terminal inside the
project folder:

```bash
cd mappe-2026-gruppe45
```

### 2. Run the application

```bash
mvn javafx:run
```

Maven will download dependencies on the first run - this may take a minute. The
application window opens when ready.

From the start screen you can either begin a **New game** (enter a name and starting
capital, optionally upload your own `.csv` stock data) or **Resume game** by loading a
previously saved `.json` file.

## Saving and loading

The game state is serialized to JSON using Gson.

- **Save game** from the in-game menu writes the current player and exchange state to a `.json` file you choose.
- **Resume game** on the start screen loads that file back into a playable session.
- The leaderboard is stored separately in `leaderboard.json` at the project root and is updated when a game is saved or a player retires.

Save files are version-specific. A file from an older version may fail to load and
will report a readable error rather than corrupting your session.

## Running the Tests

Run the unit test suite:

```bash
mvn test
```

Test coverage is measured with JaCoCo (measurement only, no enforced thresholds). After
running the tests, the HTML coverage report is generated at
`target/site/jacoco/index.html`.

Tests follow the AAA pattern (Arrange, Act, Assert) and aim to cover positive cases,
negative cases, and edge cases. See `docs/test_conventions.md` for the testing
conventions used in this project.

## Known Limitations

The system is a prototype and has the following limitations:

- All stock prices and weekly price movements are simulated - no real market data feed is used and no real transactions are made.
- The game is single-player and runs entirely on the local machine; the leaderboard is a local file, not a shared online ranking.
- Bundled stock data is the S&P 500 dataset shipped in `src/main/resources/data/sp500.csv`; custom datasets must follow the same `Ticker,Name,Price` CSV format.
- All amounts are settled in NOK; other currencies are supported through conversion only.
- Save files are tied to the application version and may not load across versions.
- The interface is designed for a desktop window and may render inconsistently across screens with very different resolutions (a known JavaFX limitation on HiDPI / Retina displays).

## Project Structure

```
mappe-2026-gruppe45/
├── docs/                                   # Project documentation (Markdown)
│   ├── SoC.md                              # Separation of Concerns / architecture
│   ├── keyboard_navigation.md              # Keyboard navigation design
│   ├── leaderboard.md                      # Leaderboard design
│   ├── test_conventions.md                 # Testing conventions (AAA)
│   └── view_css_conventions.md             # View and CSS conventions
├── src/
│   ├── main/
│   │   ├── java/edu/ntnu/idatt2003/millions/
│   │   │   ├── App.java                    # Application entry point
│   │   │   ├── controller/                 # Translate user actions into operations
│   │   │   │   ├── game/                    # Start and main game controllers
│   │   │   │   ├── loan/                    # Loan controllers
│   │   │   │   └── trade/                   # Buy/sell controllers
│   │   │   ├── service/                    # Service layer (game state + read services)
│   │   │   │   ├── game/                    # GameService - owns live game state
│   │   │   │   ├── leaderboard/             # Leaderboard service
│   │   │   │   ├── loan/                    # Loan service
│   │   │   │   ├── notification/            # Notification service
│   │   │   │   ├── player/                  # Player stats / read services
│   │   │   │   ├── stock/                   # Stock services
│   │   │   │   ├── toast/                   # Toast messaging
│   │   │   │   └── transaction/             # Transaction preview service
│   │   │   ├── model/                      # Domain model and business rules
│   │   │   │   ├── currency/                # Currency and conversion
│   │   │   │   ├── exchange/                # Exchange
│   │   │   │   ├── leaderboard/             # Leaderboard entries
│   │   │   │   ├── loan/                    # Loans
│   │   │   │   ├── notification/            # Notifications
│   │   │   │   ├── player/                  # Player and portfolio
│   │   │   │   ├── stock/                   # Stock and share
│   │   │   │   ├── transaction/             # Transaction and subclasses
│   │   │   │   └── watchlist/               # Watchlist
│   │   │   ├── factory/                    # Centralised Transaction creation
│   │   │   ├── file/                       # File layer (JSON serialization)
│   │   │   │   ├── game/                    # Save/load game state
│   │   │   │   ├── leaderboard/             # Leaderboard persistence
│   │   │   │   └── stock/                   # Stock data loading (CSV)
│   │   │   ├── observer/                   # GameObserver mechanism
│   │   │   ├── keyboard/                   # Keyboard navigation
│   │   │   │   ├── navigation/
│   │   │   │   └── registry/
│   │   │   ├── util/                       # Utilities
│   │   │   │   ├── currency/
│   │   │   │   ├── format/                  # Currency and text formatting
│   │   │   │   └── language/                # Language manager (i18n)
│   │   │   └── view/                       # JavaFX UI layer
│   │   │       ├── titlebar/                # Custom window title bar
│   │   │       ├── start/                   # Start screen (new / resume game)
│   │   │       ├── component/               # Shared UI components
│   │   │       └── ingame/                  # In-game screens
│   │   │           ├── exchange/            # Exchange overview and stock views
│   │   │           ├── dashboard/           # Portfolio, transactions, watchlist, loans
│   │   │           ├── leaderboard/         # Leaderboard view
│   │   │           ├── game/                # In-game shell
│   │   │           └── component/           # Cards, charts, modals, tables, toasts
│   │   └── resources/
│   │       ├── css/                         # Stylesheets (tokens, typography, components)
│   │       ├── data/sp500.csv               # Bundled S&P 500 stock data
│   │       ├── flags/                       # Language flags (en, no)
│   │       └── i18n/                         # Message bundles (en, no)
│   └── test/
│       └── java/                            # Unit tests
├── leaderboard.json                         # Persisted leaderboard
└── pom.xml                                  # Maven build configuration
```

## Tech Stack

| Technology         | Version | Purpose                          |
|--------------------|---------|----------------------------------|
| Java               | 25      | Application runtime              |
| JavaFX             | 25.0.1  | UI framework                     |
| Maven              | 3.9+    | Build and dependency management  |
| Gson               | 2.11.0  | JSON serialization (save/load)   |
| Ikonli (Feather)   | 12.4.0  | Icon pack for JavaFX             |
| JUnit Jupiter      | 6.0.1   | Unit testing framework           |
| JaCoCo             | 0.8.14  | Test coverage measurement        |

## Architecture

The project follows a layered architecture with a strict separation of concerns:
the **view** builds the UI and reads from the model but never mutates it, **controllers**
translate user actions and own game-policy rules, **`GameService`** owns the live game
state and performs all mutations (notifying observers afterwards), the **domain model**
holds the business rules and guards its own invariants, and the **file layer** handles
JSON persistence. The guiding rule is *read freely, mutate via `GameService`*.

See `docs/SoC.md` for the full architecture description and read/write matrix.

## Documentation

- **Architecture** - `docs/SoC.md`
- **View and CSS conventions** - `docs/view_css_conventions.md`
- **Keyboard navigation** - `docs/keyboard_navigation.md`
- **Leaderboard** - `docs/leaderboard.md`
- **Test conventions** - `docs/test_conventions.md`
- **JavaDoc** - run `mvn javadoc:javadoc` and open `target/site/apidocs/index.html`

## Collaborators

| Name                    | GitHub    |
|-------------------------|-----------|
| Alva Kjærstad Leiner    | @alvakleiner   |
| Dara Champathong Langved | @daralang  |
