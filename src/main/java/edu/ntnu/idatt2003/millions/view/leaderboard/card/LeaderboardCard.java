package edu.ntnu.idatt2003.millions.view.leaderboard.card;

import edu.ntnu.idatt2003.millions.model.leaderboard.LeaderboardEntry;
import edu.ntnu.idatt2003.millions.model.leaderboard.Outcome;
import edu.ntnu.idatt2003.millions.model.player.PlayerStatusLevel;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.LeaderboardService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.Pagination;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.card.SortableTableCard;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.leaderboard.LeaderboardSort;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;

/**
 * Sortable, searchable, paginated table card showing the leaderboard.
 *
 * <p>Owns a {@link LeaderboardService} instance — the leaderboard is a read-only
 * auxiliary feature, not live game state, so no getter on {@code GameService} is needed.</p>
 */
public class LeaderboardCard extends SortableTableCard<LeaderboardEntry, LeaderboardSort.SortColumn> {

    private static final int PAGE_SIZE = Pagination.DEFAULT_PAGE_SIZE;

    private final LeaderboardService leaderboardService = new LeaderboardService();
    private final LeaderboardSort sort = new LeaderboardSort();
    private final StyledText title;

    /**
     * Constructs a new LeaderboardCard.
     *
     * @param gameService the game service used for observer registration
     */
    public LeaderboardCard(GameService gameService) {
        super(gameService, PAGE_SIZE, "leaderboard.status", "leaderboard.empty");
        this.sortProvider = sort;
        this.table = new SortColumnTable<>(sort::getColumnDefs);
        this.pagination = new Pagination(PAGE_SIZE, this::setPage);

        Button clearSortButton = table.createClearSortButton(
                () -> LanguageManager.get("exchange.stocks.sort.clear"), this::refresh);

        title = StyledText.sectionTitle(LanguageManager.get("leaderboard.title"));
        setSpacing(16);
        getChildren().addAll(title, buildSearchRow(clearSortButton), table.asNode(), pagination);
        refresh();
    }

    @Override
    protected List<LeaderboardEntry> fetchAll() {
        return new ArrayList<>(leaderboardService.getAllEntries());
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
        title.setText(LanguageManager.get("leaderboard.title"));
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
        String key = outcome == Outcome.ACTIVE
                ? "leaderboard.outcome.active"
                : "leaderboard.outcome.gameover";
        Label badge = new Label(LanguageManager.get(key).toUpperCase());
        badge.getStyleClass().addAll("transaction-type-badge",
                outcome == Outcome.ACTIVE ? "badge-success" : "badge-danger");
        return badge;
    }
}
