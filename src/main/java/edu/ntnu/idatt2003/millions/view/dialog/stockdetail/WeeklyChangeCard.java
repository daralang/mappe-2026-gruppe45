package edu.ntnu.idatt2003.millions.view.dialog.stockdetail;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.StockHistoryService;
import edu.ntnu.idatt2003.millions.service.StockHistoryService.WeeklyPriceChange;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.WeekRangeFilter;
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
import java.util.List;
import java.util.Objects;

/**
 * A self-contained weekly price-change section for a single {@link Stock}.
 */
public class WeeklyChangeCard extends VBox {

    /**
     * Maximum number of data rows visible in the scroll pane before the
     * vertical scrollbar appears.
     */
    private static final int MAX_VISIBLE_ROWS = 4;

    /**
     * Approximate height per row in pixels.
     */
    private static final double ROW_HEIGHT = 22;

    /** Vertical gap between rows in the weekly-change {@link GridPane}. */
    private static final int TABLE_VGAP = 6;

    /** Top padding applied by the {@code stock-detail-weekly} style class. */
    private static final int TABLE_TOP_PADDING = 4;

    /**
     * Constructs a {@code WeeklyChangeSection} for the given stock and
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

        VBox tableContainer = new VBox();

        ScrollPane scroll = new ScrollPane(tableContainer);
        scroll.getStyleClass().add("content-scroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setMaxHeight(scrollMaxHeight());

        Runnable rebuild = () -> tableContainer.getChildren().setAll(
                buildTable(stock, converter, historyService,
                        filter.getFromWeek(), filter.getToWeek()));

        filter.fromWeekProperty().addListener((obs, oldVal, newVal) -> rebuild.run());
        filter.toWeekProperty().addListener((obs, oldVal, newVal) -> rebuild.run());
        rebuild.run();

        getStyleClass().add("stock-detail-panel");
        setSpacing(8);
        getChildren().addAll(buildHeader(filter), scroll);
    }

    /**
     * Computes the maximum height of the scroll pane.
     *
     * @return the scroll pane max height in pixels
     */
    private double scrollMaxHeight() {
        int visibleRows = MAX_VISIBLE_ROWS + 1;
        return visibleRows * ROW_HEIGHT
                + (visibleRows - 1) * TABLE_VGAP
                + TABLE_TOP_PADDING;
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
     * Builds the weekly-change table for the selected week range, or an info
     * label when no transition exists in that range (e.g. only week 1 selected).
     * Rows are supplied by {@link StockHistoryService}, newest first.
     *
     * @param stock          the stock whose history is read
     * @param converter      the currency converter for the NOK column
     * @param historyService the service providing {@link WeeklyPriceChange} rows
     * @param fromWeek       the first selected week (inclusive)
     * @param toWeek         the last selected week (inclusive)
     * @return the populated {@link GridPane}, or an info label when empty
     */
    private Region buildTable(Stock stock, CurrencyConverter converter,
                              StockHistoryService historyService,
                              int fromWeek, int toWeek) {
        List<WeeklyPriceChange> rows = historyService.getWeeklyChanges(
                stock, converter, fromWeek, toWeek);

        if (rows.isEmpty()) {
            return StyledText.detailLabel(LanguageManager.get("stockDetail.weeklyEmpty"));
        }

        String code = stock.getCurrency().getCurrencyCode();

        GridPane table = new GridPane();
        table.getStyleClass().add("stock-detail-weekly");
        table.setHgap(12);
        table.setVgap(TABLE_VGAP);

        HPos[] alignments = {HPos.LEFT, HPos.RIGHT, HPos.RIGHT, HPos.RIGHT};
        for (HPos alignment : alignments) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setHalignment(alignment);
            table.getColumnConstraints().add(col);
        }

        table.addRow(0,
                StyledText.detailLabel(LanguageManager.get("col.week")),
                StyledText.detailLabel(MessageFormat.format(
                        LanguageManager.get("stockDetail.changeNative"), code)),
                StyledText.detailLabel(LanguageManager.get("stockDetail.changeNok")),
                StyledText.detailLabel(LanguageManager.get("stockDetail.changePct")));

        int rowIndex = 1;
        for (WeeklyPriceChange row : rows) {
            table.addRow(rowIndex++,
                    StyledText.detailValue(String.valueOf(row.week())),
                    ChangeFormatter.styledAmount(row.nativeChange(), "detail-value"),
                    ChangeFormatter.styledAmount(row.nokChange(), "detail-value"),
                    ChangeFormatter.styledPercent(row.percentChange(), "detail-value"));
        }

        return table;
    }
}
