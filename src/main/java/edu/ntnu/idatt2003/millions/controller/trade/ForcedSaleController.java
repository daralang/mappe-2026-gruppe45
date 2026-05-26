// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.controller.trade;

import edu.ntnu.idatt2003.millions.model.transaction.SalesCalculator;
import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.loan.InsufficientSaleProceedsException;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.dialog.ForcedSaleDialog;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the forced-sale flow triggered when the player cannot cover
 * weekly obligations from cash.
 */
@SuppressWarnings("ClassCanBeRecord")
public class ForcedSaleController {

    private static final Logger LOGGER = Logger.getLogger(ForcedSaleController.class.getName());

    private final GameService gameService;

    /**
     * Constructs a new ForcedSaleController backed by the given game service.
     *
     * @param gameService the game service used to execute the forced sale
     */
    public ForcedSaleController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Opens the forced-sale dialog for the given upcoming week. Holdings are
     * sorted ascending by return percentage (worst-performing first).
     *
     * @param currentWeek the week whose obligations (interest + any maturity) must be covered
     */
    public void open(int currentWeek) {
        Player player = gameService.getPlayer();
        CurrencyConverter converter = gameService.getCurrencyConverter();

        BigDecimal interestDue = player.getWeeklyInterestDue();
        BigDecimal maturityDue = player.getMaturityDueThisWeek(currentWeek);

        List<Share> shares = player.getPortfolio().getShares().stream()
                .sorted(Comparator.comparing(s -> s.getStock().getWeeklyChangePercent()))
                .toList();

        Map<Share, BigDecimal> netNokByShare = new LinkedHashMap<>();
        for (Share share : shares) {
            netNokByShare.put(share, SalesCalculator.calculateNetNok(share, converter));
        }

        ForcedSaleDialog[] ref = new ForcedSaleDialog[1];
        ref[0] = new ForcedSaleDialog(
                shares, interestDue, maturityDue, player.getCash(), netNokByShare, currentWeek,
                selectedShares -> handleConfirm(ref[0], selectedShares, currentWeek)
        );
        ref[0].show();
    }

    private void handleConfirm(ForcedSaleDialog dialog, List<Share> selectedShares, int currentWeek) {
        try {
            gameService.executeForcedSale(selectedShares, currentWeek);
            dialog.closeOnConfirm();
        } catch (InsufficientSaleProceedsException e) {
            dialog.showError(e.getMessage());
        } catch (IllegalStateException e) {
            dialog.showError(LanguageManager.get("error.gameOver"));
            LOGGER.log(Level.INFO, "Forced sale blocked — game is over", e);
        }
    }
}
