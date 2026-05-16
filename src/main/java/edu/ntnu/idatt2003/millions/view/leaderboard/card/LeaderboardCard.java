package edu.ntnu.idatt2003.millions.view.leaderboard.card;

import edu.ntnu.idatt2003.millions.model.leaderboard.LeaderboardEntry;
import edu.ntnu.idatt2003.millions.model.leaderboard.Outcome;
import edu.ntnu.idatt2003.millions.model.player.PlayerStatusLevel;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.LeaderboardService;
import edu.ntnu.idatt2003.millions.service.toast.ToastService;
import edu.ntnu.idatt2003.millions.view.component.toast.ToastType;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.Pagination;
import edu.ntnu.idatt2003.millions.view.component.SearchBar;
import edu.ntnu.idatt2003.millions.view.component.card.SortableTableCard;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.leaderboard.LeaderboardSort;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Sortable, searchable, paginated table card showing the leaderboard.
 *
 * <p>Owns a {@link LeaderboardService} instance — the leaderboard is a read-only
 * auxiliary feature, not live game state, so no getter on {@code GameService} is needed.</p>
 */
public class LeaderboardCard extends SortableTableCard<LeaderboardEntry, LeaderboardSort.SortColumn> {

    private static final int PAGE_SIZE = Pagination.DEFAULT_PAGE_SIZE;

    private final GameService gameService;
    private final LeaderboardService leaderboardService = new LeaderboardService();
    private final LeaderboardSort sort = new LeaderboardSort();
    private final ToastService toastService;
    private Button pushScoreBtn;

    /**
     * Constructs a new LeaderboardCard.
     *
     * @param gameService the game service used for observer registration and score updates
     */
    public LeaderboardCard(GameService gameService, ToastService toastService) {
        super(gameService, PAGE_SIZE, "leaderboard.status", "leaderboard.empty");
        this.gameService = gameService;
        this.toastService = toastService;
        getStyleClass().add("leaderboard-card");
        this.sortProvider = sort;
        this.table = new SortColumnTable<>(sort::getColumnDefs);
        this.pagination = new Pagination(PAGE_SIZE, this::setPage);

        Button clearSortButton = table.createClearSortButton(
                () -> LanguageManager.get("leaderboard.sort.clear"), this::refresh);

        pushScoreBtn = new Button(LanguageManager.get("leaderboard.pushScore"));
        pushScoreBtn.getStyleClass().add("leaderboard-push-score-btn");
        pushScoreBtn.setOnAction(e -> handlePushScore());

        setSpacing(16);
        getChildren().addAll(buildSearchRow(clearSortButton), table.asNode(), pagination);
        refresh();
    }

    private void handlePushScore() {
        try {
            gameService.recordLeaderboardEntry();
            refresh();
            toastService.show(LanguageManager.get("toast.scoreUpdated"), ToastType.SUCCESS);
        } catch (Exception e) {
            toastService.show(LanguageManager.get("toast.scoreUpdateFailed"), ToastType.ERROR);
        }
    }

    @Override
    protected HBox buildSearchRow(Button clearSortButton) {
        SearchBar searchBar = new SearchBar(
                "leaderboard.search.placeholder", "search.button", searchCallback(), metadataRow);
        HBox row = new HBox(36, searchBar, pushScoreBtn, clearSortButton);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    @Override
    protected List<LeaderboardEntry> fetchAll() {
        pushScoreBtn.setDisable(gameService.getPlayer() == null);
        return new ArrayList<>(leaderboardService.getAllEntries());
    }

    /**
     * Applies a default sort by {@link LeaderboardEntry} descending
     * when no explicit column sort is active, so rank 1 always shows the best performer.
     *
     * @param all      the full entry list before text-search filtering
     * @param filtered the text-filtered entry list
     */
    @Override
    protected void afterFilter(List<LeaderboardEntry> all, List<LeaderboardEntry> filtered) {
        if (!table.isSortActive()) {
            filtered.sort(Comparator.comparing(LeaderboardEntry::returnPercent)
                    .thenComparing(LeaderboardEntry::finalNetWorth)
                    .reversed());
        }
    }

    @Override
    protected List<LeaderboardEntry> applySearch(List<LeaderboardEntry> all, String term) {
        if (term.isBlank()) return new ArrayList<>(all);
        String lower = term.toLowerCase();
        return new ArrayList<>(all.stream()
                .filter(e -> e.playerName().toLowerCase().contains(lower))
                .toList());
    }

    @Override
    protected void renderPage(List<LeaderboardEntry> page) {
        int baseRank = currentPage * pageSize;
        int row = 1;
        for (LeaderboardEntry entry : page) {
            addDataRow(row, baseRank + row, entry);
            row++;
        }
    }

    @Override
    protected void onLanguageChanged() {
        pushScoreBtn.setText(LanguageManager.get("leaderboard.pushScore"));
        super.onLanguageChanged();
    }

    private void addDataRow(int tableRow, int rank, LeaderboardEntry entry) {
        table.addRow(tableRow,
                TableCells.data(String.valueOf(rank)),
                TableCells.data(entry.playerName()),
                ChangeFormatter.styledPercent(entry.returnPercent(), "holdings-cell"),
                TableCells.data(CurrencyFormatter.format(entry.finalNetWorth())),
                TableCells.data(String.valueOf(entry.weeksPlayed())),
                statusBadge(entry.status()),
                outcomeBadge(entry.outcome())
        );
    }

    private Label statusBadge(PlayerStatusLevel status) {
        String text = LanguageManager.get("status." + status.name().toLowerCase()).toUpperCase();
        Label badge = new Label(text);
        badge.getStyleClass().addAll("transaction-type-badge",
                "badge-status-" + status.name().toLowerCase());
        return badge;
    }

    private Label outcomeBadge(Outcome outcome) {
        String key = switch (outcome) {
            case ACTIVE     -> "leaderboard.outcome.active";
            case RETIRED    -> "leaderboard.outcome.retired";
            case BANKRUPTCY -> "leaderboard.outcome.bankruptcy";
        };
        String cssClass = switch (outcome) {
            case ACTIVE     -> "badge-success";
            case RETIRED    -> "badge-info";
            case BANKRUPTCY -> "badge-danger";
        };
        Label badge = new Label(LanguageManager.get(key).toUpperCase());
        badge.getStyleClass().addAll("transaction-type-badge", cssClass);
        return badge;
    }
}
