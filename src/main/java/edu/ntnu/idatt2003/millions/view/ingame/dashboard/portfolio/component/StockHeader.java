package edu.ntnu.idatt2003.millions.view.ingame.dashboard.portfolio.component;

import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Shared stock identification block used in transaction dialogs and the
 * share details modal. Renders a detail label, a bold "SYMBOL, Company"
 * line, and an optional hint line beneath it.
 */
public class StockHeader extends VBox {

    /**
     * @param stock the stock to identify
     * @param hint  optional hint line shown below the stock name; may be null to omit it
     */
    public StockHeader(Stock stock, String hint) {
        super(4);
        StyledText labelNode = StyledText.detailLabel(LanguageManager.get("dialog.stock.label"));

        Label valueNode = new Label(stock.getSymbol() + ", " + stock.getCompany());
        valueNode.getStyleClass().add("modal-section-value");

        getChildren().addAll(labelNode, valueNode);

        if (hint != null && !hint.isEmpty()) {
            getChildren().add(StyledText.detailLabel(hint));
        }
    }
}
