package edu.ntnu.idatt2003.millions.view.ingame.exchange.stock.dialog;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.stock.StockHistoryService;
import edu.ntnu.idatt2003.millions.service.stock.StockHistoryService.WeeklyPriceChange;
import edu.ntnu.idatt2003.millions.util.format.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.language.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.ingame.component.search.WeekRangeFilter;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.MessageFormat;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * A self-contained weekly price-change card for a single {@link Stock}.
 *
 * <p>When the stock's currency is NOK the NOK-change column is omitted from the
 * table, because it would be identical to the native-change column and therefore
 * redundant. For all other currencies both columns are shown side by side.</p>
 */
public class WeeklyChangeCard extends VBox {

    /**
     * Maximum number of data rows visible in the scroll pane before the
     * vertical scrollbar appears.
     */
    private static final int MAX_VISIBLE_ROWS = 4;

    /** Vertical gap between rows in the weekly-change {@link GridPane}. */
    private static final int TABLE_VGAP = 6;

    private static final Currency NOK = Currency.getInstance("NOK");

    /**
     * Constructs a {@code WeeklyChangeCard} for the given stock and
     * immediately wires the {@link WeekRangeFilter} to the scrollable table.
     *
     * @param stock       the stock whose weekly price changes are displayed;
     *                    must not be {@code null}
     * @param converter   the currency converter used to express changes in NOK;
     *                    must not be {@code null}
     * @param currentWeek the current game week; used to bound the upper end of
     *                    the {@link WeekRangeFilter}
     * @throws NullPointerException if {@code stock} or {@code converter} is {@code null}
     */
    public WeeklyChangeCard(Stock stock, CurrencyConverter converter, int currentWeek) {
        Objects.requireNonNull(stock, "stock must not be null");
        Objects.requireNonNull(converter, "converter must not be null");

        StockHistoryService historyService = new StockHistoryService();
        int week = Math.max(1, currentWeek);
        WeekRangeFilter filter = new WeekRangeFilter(1, week);

        int[] lastRowCount = {0};
        VBox tableContainer = new VBox();

        ScrollPane scroll = new ScrollPane(tableContainer);
        scroll.getStyleClass().add("content-scroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        tableContainer.heightProperty().addListener((obs, oldH, newH) -> {
            int n = lastRowCount[0];
            if (n > MAX_VISIBLE_ROWS + 1) {
                scroll.setMaxHeight(newH.doubleValue() * (MAX_VISIBLE_ROWS + 1.0) / n);
            } else {
                scroll.setMaxHeight(Double.MAX_VALUE);
            }
        });

        Runnable rebuild = () -> {
            List<WeeklyPriceChange> rows = historyService.getWeeklyChanges(
                    stock, converter, filter.getFromWeek(), filter.getToWeek());
            lastRowCount[0] = rows.isEmpty() ? 0 : rows.size() + 1;
            tableContainer.getChildren().setAll(buildTable(stock, rows));
        };

        filter.fromWeekProperty().addListener((obs, oldVal, newVal) -> rebuild.run());
        filter.toWeekProperty().addListener((obs, oldVal, newVal) -> rebuild.run());
        rebuild.run();

        getStyleClass().addAll("stock-detail-panel", "stock-detail-panel--scroll");
        setSpacing(8);
        getChildren().addAll(buildHeader(filter), scroll);
    }

    /**
     * Builds the section header.
     *
     * @param filter the week-range filter to place on the right
     * @return the header row
     */
    private HBox buildHeader(WeekRangeFilter filter) {
        StyledText title = StyledText.sectionTitle(
                LanguageManager.get("stockDetail.weeklyTitle"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(8, title, spacer, filter);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    /**
     * Builds the weekly-change table from the given rows, or an info label when
     * the list is empty (e.g. only week 1 selected with no transitions).
     * Rows are displayed newest first.
     *
     * <p>When the stock's currency is NOK the NOK-change column is omitted because
     * it would duplicate the native-change column. Foreign-currency stocks show both
     * columns so the player can compare the change in the stock's own currency and
     * in NOK side by side.</p>
     *
     * @param stock the stock whose currency code labels the native-change column
     * @param rows  the pre-fetched {@link WeeklyPriceChange} rows, newest first
     * @return the populated {@link GridPane}, or an info label when {@code rows} is empty
     */
    private Region buildTable(Stock stock, List<WeeklyPriceChange> rows) {
        if (rows.isEmpty()) {
            return StyledText.detailLabel(LanguageManager.get("stockDetail.weeklyEmpty"));
        }

        boolean isNok = stock.getCurrency().equals(NOK);
        String code = stock.getCurrency().getCurrencyCode();

        GridPane table = new GridPane();
        table.getStyleClass().add("stock-detail-weekly");
        table.setHgap(12);
        table.setVgap(TABLE_VGAP);

        HPos[] alignments = isNok
                ? new HPos[]{HPos.LEFT, HPos.RIGHT, HPos.RIGHT}
                : new HPos[]{HPos.LEFT, HPos.RIGHT, HPos.RIGHT, HPos.RIGHT};
        for (HPos alignment : alignments) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setHalignment(alignment);
            table.getColumnConstraints().add(col);
        }

        if (isNok) {
            table.addRow(0,
                    StyledText.detailLabel(LanguageManager.get("col.week")),
                    StyledText.detailLabel(LanguageManager.get("stockDetail.changeNok")),
                    StyledText.detailLabel(LanguageManager.get("stockDetail.changePct")));
        } else {
            table.addRow(0,
                    StyledText.detailLabel(LanguageManager.get("col.week")),
                    StyledText.detailLabel(MessageFormat.format(
                            LanguageManager.get("stockDetail.changeNative"), code)),
                    StyledText.detailLabel(LanguageManager.get("stockDetail.changeNok")),
                    StyledText.detailLabel(LanguageManager.get("stockDetail.changePct")));
        }

        int rowIndex = 1;
        for (WeeklyPriceChange row : rows) {
            if (isNok) {
                table.addRow(rowIndex++,
                        StyledText.detailValue(String.valueOf(row.week())),
                        ChangeFormatter.styledAmount(row.nokChange(), "detail-value"),
                        ChangeFormatter.styledPercent(row.percentChange(), "detail-value"));
            } else {
                table.addRow(rowIndex++,
                        StyledText.detailValue(String.valueOf(row.week())),
                        ChangeFormatter.styledAmount(row.nativeChange(), "detail-value"),
                        ChangeFormatter.styledAmount(row.nokChange(), "detail-value"),
                        ChangeFormatter.styledPercent(row.percentChange(), "detail-value"));
            }
        }

        return table;
    }
}
