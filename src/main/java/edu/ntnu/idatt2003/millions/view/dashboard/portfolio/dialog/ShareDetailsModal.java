package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.dialog;

import edu.ntnu.idatt2003.millions.controller.trade.TradeController;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.PortfolioService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Modal;
import edu.ntnu.idatt2003.millions.view.component.ModalActions;
import edu.ntnu.idatt2003.millions.view.component.StockHeader;
import edu.ntnu.idatt2003.millions.view.component.SummaryBox;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import edu.ntnu.idatt2003.millions.util.MoneyFormatter;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Currency;

/**
 * Read-only details modal for a single share position. Shows the position
 * size, current market value, liquidation value, and return figures, with
 * action buttons to open the buy, sell, or sell-all dialogs.
 */
public class ShareDetailsModal extends Modal {

    private static final Currency NOK = Currency.getInstance("NOK");

    private final Share share;
    private final TradeController controller;
    private final PortfolioService portfolioService = new PortfolioService();

    public ShareDetailsModal(Share share, TradeController controller) {
        this.share = share;
        this.controller = controller;
    }

    @Override
    protected Region buildContent() {
        VBox content = new VBox();
        content.getChildren().addAll(
                buildStandardHeader(LanguageManager.get("details.title")),
                buildBody()
        );
        return content;
    }

    private VBox buildBody() {
        Stock stock = share.getStock();
        String currencyCode = stock.getCurrency().getCurrencyCode();
        boolean isForeign = !stock.getCurrency().equals(NOK);

        String hint = MessageFormat.format(
                LanguageManager.get("details.stock.hint"),
                currencyCode,
                MoneyFormatter.format(stock.getSalesPrice()));

        boolean gameOver = controller.isGameOver();

        Button buyMore = new Button(LanguageManager.get("details.button.buyMore"));
        buyMore.getStyleClass().addAll("modal-button", "modal-button-primary");
        buyMore.setDisable(gameOver);
        buyMore.setOnAction(e -> {
            close();
            Platform.runLater(() -> controller.openBuyDialog(stock));
        });

        Button sell = new Button(LanguageManager.get("details.button.sell"));
        sell.getStyleClass().addAll("modal-button", "modal-button-danger");
        sell.setDisable(gameOver);
        sell.setOnAction(e -> {
            close();
            Platform.runLater(() -> controller.openSellDialog(share));
        });

        Button sellAll = new Button(LanguageManager.get("details.button.sellAll"));
        sellAll.getStyleClass().addAll("modal-button", "modal-button-danger");
        sellAll.setDisable(gameOver);
        sellAll.setOnAction(e -> {
            close();
            Platform.runLater(() -> controller.openSellAllDialog(share));
        });

        VBox body = new VBox();
        body.getStyleClass().add("modal-body");
        body.getChildren().addAll(
                new StockHeader(stock, hint),
                buildPositionBox(currencyCode),
                buildValueBox(currencyCode, isForeign),
                buildReturnBox(currencyCode, isForeign),
                ModalActions.row(buyMore, sell, sellAll)
        );
        return body;
    }

    private SummaryBox buildPositionBox(String currencyCode) {
        SummaryBox box = new SummaryBox();
        box.setSectionTitle(LanguageManager.get("details.section.position"));
        box.addRow(
                LanguageManager.get("details.row.quantity"),
                MoneyFormatter.format(share.getQuantity()));
        box.addRow(
                LanguageManager.get("details.row.avgPrice"),
                MoneyFormatter.format(share.getPurchasePrice()) + " " + currencyCode,
                null, "tooltip.details.gav");
        box.addRow(
                LanguageManager.get("details.row.cost"),
                MoneyFormatter.format(share.getCost()) + " " + currencyCode);
        return box;
    }

    private SummaryBox buildValueBox(String currencyCode, boolean isForeign) {
        SummaryBox box = new SummaryBox();
        box.setSectionTitle(LanguageManager.get("details.section.now"));
        if (isForeign) {
            box.addRow(
                    LanguageManager.get("details.row.marketValue"),
                    MoneyFormatter.format(share.getCurrentValue()) + " " + currencyCode,
                    null, "tooltip.details.marketValue");
            box.addRow(
                    LanguageManager.get("details.row.marketValueNok"),
                    MoneyFormatter.format(portfolioService.getShareValueInNok(
                            share, controller.getCurrencyConverter())) + " NOK",
                    null, "tooltip.details.marketValueNok");
        } else {
            box.addRow(
                    LanguageManager.get("details.row.marketValue"),
                    MoneyFormatter.format(share.getCurrentValue()) + " NOK",
                    null, "tooltip.details.marketValue");
        }
        box.addRow(
                LanguageManager.get("details.row.liquidationValue"),
                MoneyFormatter.format(portfolioService.getLiquidationValueInNok(
                        share, controller.getCurrencyConverter())) + " NOK",
                null, "tooltip.details.liquidation");
        return box;
    }

    private SummaryBox buildReturnBox(String currencyCode, boolean isForeign) {
        SummaryBox box = new SummaryBox();
        box.setSectionTitle(LanguageManager.get("details.section.return"));

        BigDecimal returnNative = share.getReturnNative();
        boolean positiveNative = returnNative.signum() >= 0;
        String signNative = positiveNative ? "+" : "−";
        String returnNativeStr = signNative + MoneyFormatter.format(returnNative.abs());

        if (isForeign) {
            String returnNativeLabel = MessageFormat.format(
                    LanguageManager.get("details.row.returnNative"), currencyCode);
            box.addRow(
                    returnNativeLabel,
                    returnNativeStr + " " + currencyCode,
                    positiveNative ? "positive" : "negative",
                    "tooltip.details.returnNative");

            BigDecimal returnNok = portfolioService.getShareReturnInNok(
                    share, controller.getCurrencyConverter());
            boolean positiveNok = returnNok.signum() >= 0;
            String signNok = positiveNok ? "+" : "−";
            box.addRow(
                    LanguageManager.get("details.row.returnNok"),
                    signNok + MoneyFormatter.format(returnNok.abs()) + " NOK",
                    positiveNok ? "positive" : "negative",
                    "tooltip.shared.returnNok");
        } else {
            box.addRow(
                    LanguageManager.get("details.row.return"),
                    returnNativeStr + " NOK",
                    positiveNative ? "positive" : "negative",
                    "tooltip.details.return");
        }

        BigDecimal returnPct = share.getReturnPercent();
        boolean positivePct = returnPct.signum() >= 0;
        String signPct = positivePct ? "+" : "";
        box.addRow(
                LanguageManager.get("details.row.returnPercent"),
                signPct + returnPct.toPlainString() + "%",
                positivePct ? "positive" : "negative",
                "tooltip.shared.returnPct");

        BigDecimal weeklyChange = share.getStock().getWeeklyChangePercent();
        boolean positiveWeekly = weeklyChange.signum() >= 0;
        String signWeekly = positiveWeekly ? "+" : "";
        box.addRow(
                LanguageManager.get("details.row.weeklyChange"),
                signWeekly + weeklyChange.toPlainString() + "%",
                positiveWeekly ? "positive" : "negative",
                "tooltip.shared.weeklyChange");

        return box;
    }
}
