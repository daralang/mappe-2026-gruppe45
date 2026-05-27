package edu.ntnu.idatt2003.millions.service.notification;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.currency.FixedRateCurrencyConverter;
import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.model.loan.Loan;
import edu.ntnu.idatt2003.millions.model.loan.LoanOffer;
import edu.ntnu.idatt2003.millions.model.loan.LoanRiskLevel;
import edu.ntnu.idatt2003.millions.model.notification.Notification;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.player.PlayerStatusLevel;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private NotificationService service;
    private Player player;
    private Exchange exchange;
    private CurrencyConverter converter;

    private static final LoanOffer FAST_OFFER = new LoanOffer(
            "fast", new BigDecimal("0.0015"), 15,
            new BigDecimal("50000.00"), LoanRiskLevel.MEDIUM);

    /**
     * Initializes test fixtures before each test.
     * Test data values were generated with AI assistance and reviewed manually.
     */
    @BeforeEach
    void setUp() {
        service = new NotificationService();
        player = new Player("Test", new BigDecimal("100000.00"));
        converter = new FixedRateCurrencyConverter();
        Stock stock = new Stock("AAA", "AAA Corp",
                new ArrayList<>(List.of(new BigDecimal("100.00"))),
                Currency.getInstance("NOK"));
        exchange = new Exchange("TestEx", List.of(stock), converter);
    }

    private void advanceExchangeTo(int targetWeek) {
        while (exchange.getWeek() < targetWeek) {
            exchange.advance();
        }
    }

    @Nested
    @DisplayName("Loan maturity notifications")
    class LoanMaturity {

        @Test
        @DisplayName("Pushes WARNING when 3 weeks left")
        void onWeekAdvanced_pushesLoanMaturityWarningAt3WeeksLeft() {
            Loan loan = new Loan(FAST_OFFER, new BigDecimal("5000"), 0);
            try { player.takeLoan(loan, converter); } catch (Exception ignored) {}
            // takenAtWeek=0, termWeeks=15 → weeksRemaining at week=12 is 3
            advanceExchangeTo(12);

            service.onWeekAdvanced(player, exchange, converter);

            List<Notification> ns = player.getNotifications();
            assertTrue(ns.stream().anyMatch(n ->
                    n.severity() == Notification.Severity.WARNING
                    && n.titleKey().equals("notification.loanMaturity.soon.title")));
        }

        @Test
        @DisplayName("Pushes SEVERE when 1 week left")
        void onWeekAdvanced_pushesSevereAtOneWeekLeft() {
            Loan loan = new Loan(FAST_OFFER, new BigDecimal("5000"), 0);
            try { player.takeLoan(loan, converter); } catch (Exception ignored) {}
            // weeksRemaining at week=14 is 1
            advanceExchangeTo(14);

            service.onWeekAdvanced(player, exchange, converter);

            assertTrue(player.getNotifications().stream().anyMatch(n ->
                    n.severity() == Notification.Severity.SEVERE
                    && n.titleKey().equals("notification.loanMaturity.dueNextWeek.title")));
        }

        @Test
        @DisplayName("Does not push beyond 3 weeks left")
        void onWeekAdvanced_doesNotPushBeyond3Weeks() {
            Loan loan = new Loan(FAST_OFFER, new BigDecimal("5000"), 0);
            try { player.takeLoan(loan, converter); } catch (Exception ignored) {}
            // weeksRemaining at week=1 is 14 → no notification
            advanceExchangeTo(1);

            service.onWeekAdvanced(player, exchange, converter);

            assertTrue(player.getNotifications().stream().noneMatch(n ->
                    n.titleKey().startsWith("notification.loanMaturity")));
        }
    }

    @Nested
    @DisplayName("Debt ratio notifications")
    class DebtRatio {

        @Test
        @DisplayName("Pushes WARNING on first crossing above 85%")
        void onWeekAdvanced_pushesDebtRatioOnceOnCrossing() {
            // player capacity = 100000 * 50% = 50000. 85% of 50000 = 42500
            // Take a loan of 43000 → ratio = 43000/50000 = 86%
            LoanOffer offer = new LoanOffer("std", new BigDecimal("0.001"), 20,
                    new BigDecimal("100000"), LoanRiskLevel.LOW);
            Loan loan = new Loan(offer, new BigDecimal("43000"), 0);
            try { player.takeLoan(loan, converter); } catch (Exception ignored) {}

            service.onWeekAdvanced(player, exchange, converter);

            long count = player.getNotifications().stream()
                    .filter(n -> n.titleKey().equals("notification.debtRatio.high.title"))
                    .count();
            assertEquals(1, count);
        }

        @Test
        @DisplayName("Does not push debt ratio when already above threshold")
        void onWeekAdvanced_doesNotPushDebtRatioWhenAlreadyAbove() {
            LoanOffer offer = new LoanOffer("std", new BigDecimal("0.001"), 20,
                    new BigDecimal("100000"), LoanRiskLevel.LOW);
            Loan loan = new Loan(offer, new BigDecimal("43000"), 0);
            try { player.takeLoan(loan, converter); } catch (Exception ignored) {}

            service.onWeekAdvanced(player, exchange, converter);
            service.onWeekAdvanced(player, exchange, converter);

            long count = player.getNotifications().stream()
                    .filter(n -> n.titleKey().equals("notification.debtRatio.high.title"))
                    .count();
            assertEquals(1, count);
        }

        @Test
        @DisplayName("Pushes debt ratio again after dropping below and crossing again")
        void onWeekAdvanced_pushesDebtRatioAgainAfterDroppingAndCrossing() {
            LoanOffer offer = new LoanOffer("std", new BigDecimal("0.001"), 20,
                    new BigDecimal("100000"), LoanRiskLevel.LOW);
            Loan loan = new Loan(offer, new BigDecimal("43000"), 0);
            try { player.takeLoan(loan, converter); } catch (Exception ignored) {}

            // Cross above
            service.onWeekAdvanced(player, exchange, converter);
            // Simulate drop below by repaying the loan
            player.repayLoan(loan, 1);
            // Now wasAboveDebtThreshold=true but ratio is 0 → setWasAboveDebtThreshold(false)
            service.onWeekAdvanced(player, exchange, converter);
            // Take loan again to cross above threshold
            Loan loan2 = new Loan(offer, new BigDecimal("43000"), 1);
            try { player.takeLoan(loan2, converter); } catch (Exception ignored) {}
            service.onWeekAdvanced(player, exchange, converter);

            long count = player.getNotifications().stream()
                    .filter(n -> n.titleKey().equals("notification.debtRatio.high.title"))
                    .count();
            assertEquals(2, count);
        }
    }

    @Nested
    @DisplayName("Low cash notifications")
    class LowCash {

        @Test
        @DisplayName("Pushes WARNING on first time cash drops below next-week obligation")
        void onWeekAdvanced_pushesLowCashOnceOnCrossing() {
            // Give player a loan with weekly interest
            LoanOffer offer = new LoanOffer("std", new BigDecimal("0.001"), 5,
                    new BigDecimal("100000"), LoanRiskLevel.LOW);
            Loan loan = new Loan(offer, new BigDecimal("10000"), 0);
            try { player.takeLoan(loan, converter); } catch (Exception ignored) {}
            // Drain almost all cash so obligation at week+1 exceeds cash
            player.withdrawMoney(player.getCash().subtract(new BigDecimal("5")));

            service.onWeekAdvanced(player, exchange, converter);

            assertTrue(player.getNotifications().stream().anyMatch(n ->
                    n.titleKey().equals("notification.lowCash.title")));
        }

        @Test
        @DisplayName("Does not push low cash again when already flagged")
        void onWeekAdvanced_doesNotPushLowCashWhenAlreadyLow() {
            LoanOffer offer = new LoanOffer("std", new BigDecimal("0.001"), 5,
                    new BigDecimal("100000"), LoanRiskLevel.LOW);
            Loan loan = new Loan(offer, new BigDecimal("10000"), 0);
            try { player.takeLoan(loan, converter); } catch (Exception ignored) {}
            player.withdrawMoney(player.getCash().subtract(new BigDecimal("5")));

            service.onWeekAdvanced(player, exchange, converter);
            service.onWeekAdvanced(player, exchange, converter);

            long count = player.getNotifications().stream()
                    .filter(n -> n.titleKey().equals("notification.lowCash.title"))
                    .count();
            assertEquals(1, count);
        }
    }

    @Nested
    @DisplayName("Stock movement notifications")
    class StockMovement {

        private Stock ownedStock;

        /**
         * Initializes test fixtures before each test.
         * Test data values were generated with AI assistance and reviewed manually.
         */
        @BeforeEach
        void setupOwnedStock() {
            List<BigDecimal> prices = new ArrayList<>();
            prices.add(new BigDecimal("100.00"));
            prices.add(new BigDecimal("88.00")); // -12%
            ownedStock = new Stock("DROP", "Drop Corp", prices, Currency.getInstance("NOK"));
            Exchange ex = new Exchange("Ex2", List.of(ownedStock), converter);
            exchange = ex;

            Share share = new Share(ownedStock, new BigDecimal("1"), new BigDecimal("100"));
            player.getPortfolio().addShare(share);
        }

        @Test
        @DisplayName("Pushes INFO for 7%+ drop on owned stock")
        void onWeekAdvanced_pushesStockDropForOwnedStockWith10PercentDrop() {
            service.onWeekAdvanced(player, exchange, converter);

            assertTrue(player.getNotifications().stream().anyMatch(n ->
                    n.severity() == Notification.Severity.INFO
                    && n.titleKey().equals("notification.stockMovement.drop.title")));
        }

        @Test
        @DisplayName("Does not push stock drop for unowned stock")
        void onWeekAdvanced_doesNotPushStockDropForUnownedStock() {
            List<BigDecimal> prices2 = new ArrayList<>();
            prices2.add(new BigDecimal("100.00"));
            prices2.add(new BigDecimal("80.00")); // -20%
            Stock unowned = new Stock("UNO", "Unowned Corp", prices2, Currency.getInstance("NOK"));
            Exchange ex = new Exchange("Ex3", List.of(ownedStock, unowned), converter);
            exchange = ex;

            service.onWeekAdvanced(player, exchange, converter);

            // Only the owned stock (DROP) notification should appear
            long unownedNotifs = player.getNotifications().stream()
                    .filter(n -> n.bodyArgs().contains("UNO"))
                    .count();
            assertEquals(0, unownedNotifs);
        }

        @Test
        @DisplayName("Pushes INFO for 7%+ gain on owned stock")
        void onWeekAdvanced_pushesStockGainForOwnedStockWith10PercentGain() {
            List<BigDecimal> dropPrices = new ArrayList<>();
            dropPrices.add(new BigDecimal("100.00"));
            dropPrices.add(new BigDecimal("100.00")); // no change for DROP
            Stock dropFlat = new Stock("DROP", "Drop Corp", dropPrices, Currency.getInstance("NOK"));

            List<BigDecimal> gainPrices = new ArrayList<>();
            gainPrices.add(new BigDecimal("100.00"));
            gainPrices.add(new BigDecimal("115.00")); // +15%
            Stock gainStock = new Stock("GAIN", "Gain Corp", gainPrices, Currency.getInstance("NOK"));

            exchange = new Exchange("Ex4", List.of(dropFlat, gainStock), converter);
            Share gainShare = new Share(gainStock, new BigDecimal("1"), new BigDecimal("100"));
            player.getPortfolio().addShare(gainShare);

            service.onWeekAdvanced(player, exchange, converter);

            assertTrue(player.getNotifications().stream().anyMatch(n ->
                    n.titleKey().equals("notification.stockMovement.gain.title")));
        }
    }

    @Nested
    @DisplayName("Loan repayment notifications")
    class LoanRepayment {

        @Test
        @DisplayName("Pushes INFO when a loan is repaid this week")
        void onWeekAdvanced_pushesLoanRepaidWhenLoanMaturesAtWeek() {
            // Arrange: take a fast loan (term=15), advance to maturity week and repay
            Loan loan = new Loan(FAST_OFFER, new BigDecimal("5000"), 0);
            try { player.takeLoan(loan, converter); } catch (Exception ignored) {}
            advanceExchangeTo(15);
            player.repayLoan(loan, exchange.getWeek());
            // Act
            service.onWeekAdvanced(player, exchange, converter);
            // Assert
            assertTrue(player.getNotifications().stream().anyMatch(n ->
                    n.severity() == Notification.Severity.INFO
                    && n.titleKey().equals("notification.loanRepaid.title")));
        }

        @Test
        @DisplayName("Does not push loan-repaid when no repayment this week")
        void onWeekAdvanced_doesNotPushLoanRepaidIfNoMaturityThisWeek() {
            // Arrange: loan active but not yet at maturity week
            Loan loan = new Loan(FAST_OFFER, new BigDecimal("5000"), 0);
            try { player.takeLoan(loan, converter); } catch (Exception ignored) {}
            advanceExchangeTo(5);
            // Act: no repayment happens
            service.onWeekAdvanced(player, exchange, converter);
            // Assert
            assertTrue(player.getNotifications().stream().noneMatch(n ->
                    n.titleKey().equals("notification.loanRepaid.title")));
        }
    }

    @Nested
    @DisplayName("Status change notifications")
    class StatusChange {

        @Test
        @DisplayName("Does not push when status is unchanged")
        void onWeekAdvanced_doesNotPushWhenStatusUnchanged() {
            // Fresh player: previousStatus=NOVICE, getStatus()=NOVICE
            service.onWeekAdvanced(player, exchange, converter);

            assertTrue(player.getNotifications().stream().noneMatch(n ->
                    n.titleKey().startsWith("notification.status")));
        }

        @Test
        @DisplayName("Pushes MILESTONE on upgrade (NOVICE → INVESTOR)")
        void onWeekAdvanced_pushesMilestoneOnStatusUpgrade() {
            makePlayerQualifyForInvestor();
            player.setPreviousStatus(PlayerStatusLevel.NOVICE);

            service.onWeekAdvanced(player, exchange, converter);

            assertTrue(player.getNotifications().stream().anyMatch(n ->
                    n.severity() == Notification.Severity.MILESTONE
                    && n.titleKey().equals("notification.statusUpgrade.title")));
        }

        @Test
        @DisplayName("Pushes INFO on downgrade (SPECULATOR → NOVICE)")
        void onWeekAdvanced_pushesInfoOnStatusDowngrade() {
            // Fresh player has no trades → status = NOVICE
            player.setPreviousStatus(PlayerStatusLevel.SPECULATOR);

            service.onWeekAdvanced(player, exchange, converter);

            assertTrue(player.getNotifications().stream().anyMatch(n ->
                    n.severity() == Notification.Severity.INFO
                    && n.titleKey().equals("notification.statusDowngrade.title")));
        }

        @Test
        @DisplayName("Pushes exactly one MILESTONE when jumping from NOVICE directly to SPECULATOR")
        void onWeekAdvanced_pushesOneUpgradeWhenJumpingMultipleTiers() {
            makePlayerQualifyForSpeculator();
            player.setPreviousStatus(PlayerStatusLevel.NOVICE);

            service.onWeekAdvanced(player, exchange, converter);

            long count = player.getNotifications().stream()
                    .filter(n -> n.titleKey().equals("notification.statusUpgrade.title"))
                    .count();
            assertEquals(1, count);
        }

        @Test
        @DisplayName("Pushes exactly one INFO when dropping from SPECULATOR directly to NOVICE")
        void onWeekAdvanced_pushesOneDowngradeWhenDroppingMultipleTiers() {
            player.setPreviousStatus(PlayerStatusLevel.SPECULATOR);

            service.onWeekAdvanced(player, exchange, converter);

            long count = player.getNotifications().stream()
                    .filter(n -> n.titleKey().equals("notification.statusDowngrade.title"))
                    .count();
            assertEquals(1, count);
        }

        private void makePlayerQualifyForInvestor() {
            String symbol = exchange.getStocks().get(0).getSymbol();
            for (int i = 0; i < 10; i++) {
                exchange.advance();
                exchange.buy(symbol, new BigDecimal("1"), player);
            }
            // Ensure net worth >= startingMoney * 1.20 (100000 -> 120000)
            player.addMoney(new BigDecimal("25000"));
        }

        private void makePlayerQualifyForSpeculator() {
            String symbol = exchange.getStocks().get(0).getSymbol();
            for (int i = 0; i < 20; i++) {
                exchange.advance();
                exchange.buy(symbol, new BigDecimal("1"), player);
            }
            // Ensure net worth >= startingMoney * 2.00 (100000 -> 200000)
            player.addMoney(new BigDecimal("110000"));
        }
    }

    @Test
    @DisplayName("onLoanTaken pushes debt ratio notification when crossing threshold")
    void onLoanTaken_pushesDebtRatioWhenAppropriate() {
        LoanOffer offer = new LoanOffer("std", new BigDecimal("0.001"), 20,
                new BigDecimal("100000"), LoanRiskLevel.LOW);
        Loan loan = new Loan(offer, new BigDecimal("43000"), 0);
        try { player.takeLoan(loan, converter); } catch (Exception ignored) {}

        service.onLoanTaken(player, exchange.getWeek(), converter);

        assertTrue(player.getNotifications().stream().anyMatch(n ->
                n.titleKey().equals("notification.debtRatio.high.title")));
    }
}
