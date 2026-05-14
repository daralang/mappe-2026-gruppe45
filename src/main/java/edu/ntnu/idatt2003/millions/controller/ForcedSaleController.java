package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.loan.InsufficientSaleProceedsException;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.dialog.ForcedSaleDialog;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Controller for the forced-sale flow triggered when the player cannot cover
 * weekly interest from cash. Builds and shows the {@link ForcedSaleDialog},
 * and delegates the confirmed sale to {@link GameService}.
 */
public class ForcedSaleController {

    private final GameService gameService;

    public ForcedSaleController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Opens the forced-sale dialog for the given required interest amount.
     * Holdings are sorted ascending by return percentage (worst-performing first).
     *
     * @param interestDue total weekly interest the player must cover by selling shares
     */
    public void open(BigDecimal interestDue) {
        Player player = gameService.getPlayer();
        CurrencyConverter converter = gameService.getCurrencyConverter();

        List<Share> shares = player.getPortfolio().getShares().stream()
                .sorted(Comparator.comparing(Share::getReturnPercent))
                .toList();

        ForcedSaleDialog[] ref = new ForcedSaleDialog[1];
        ref[0] = new ForcedSaleDialog(
                shares, interestDue, player.getMoney(), converter,
                gameService.getExchange().getWeek(),
                selectedShares -> handleConfirm(ref[0], selectedShares, interestDue)
        );
        ref[0].show();
    }

    private void handleConfirm(ForcedSaleDialog dialog, List<Share> selectedShares,
                                BigDecimal interestAmount) {
        try {
            gameService.executeForcedSale(selectedShares, interestAmount);
            dialog.closeOnConfirm();
        } catch (InsufficientSaleProceedsException e) {
            dialog.showError(e.getMessage());
        }
    }
}
