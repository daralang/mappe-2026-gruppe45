package edu.ntnu.idatt2003.millions.model;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.currency.FixedRateCurrencyConverter;
import edu.ntnu.idatt2003.millions.model.loan.ExcessiveDebtException;
import edu.ntnu.idatt2003.millions.model.loan.Loan;
import edu.ntnu.idatt2003.millions.model.loan.LoanLedgerEntry;
import edu.ntnu.idatt2003.millions.model.loan.LoanLedgerEntryType;
import edu.ntnu.idatt2003.millions.model.loan.LoanOffer;
import edu.ntnu.idatt2003.millions.model.loan.LoanRiskLevel;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.player.PlayerStatusLevel;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.Purchase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Player} class.
 * <p>
 * This test class verifies the behaviour of the Player model, including
 * construction, money management, net worth calculation with currency
 * conversion, status progression, net worth history and the previous
 * net worth bookkeeping used by the game manager.
 * </p>
 * <p>
 * All tests follow the AAA pattern.
 * </p>
 */
class PlayerTest {

    private Player player;
    private CurrencyConverter converter;

    private static final Currency NOK = Currency.getInstance("NOK");
    private static final Currency USD = Currency.getInstance("USD");

    @BeforeEach
    void setUp() {
        player = new Player("Alva", new BigDecimal("1000.00"));
        converter = new FixedRateCurrencyConverter();
    }

    /**
     * Helper that creates a NOK-currency stock with a single price.
     * Keeping the stock currency aligned with the player's NOK balance avoids
     * conversion noise in the assertions.
     */
    private static Stock nokStock(String symbol, String company, BigDecimal price) {
        return new Stock(symbol, company,
                new ArrayList<>(List.of(price)), NOK);
    }

    @Nested
    @DisplayName("Player()")
    class Constructor {

        @Test
        @DisplayName("Should return correct name when valid player created")
        void returnsCorrectName() {
            // Act
            String name = player.getName();
            // Assert
            assertEquals("Alva", name);
        }

        @Test
        @DisplayName("Should return correct starting money when valid player created")
        void returnsCorrectStartingMoney() {
            // Act
            BigDecimal startingMoney = player.getStartingMoney();
            // Assert
            assertEquals(new BigDecimal("1000.00"), startingMoney);
        }

        @Test
        @DisplayName("Should set current balance equal to starting money")
        void setsMoneyEqualToStartingMoney() {
            // Act
            BigDecimal money = player.getMoney();
            // Assert
            assertEquals(new BigDecimal("1000.00"), money);
        }

        @Test
        @DisplayName("Should throw exception when name is null")
        void throwsExceptionWhenNameIsNull() {
            // Arrange
            BigDecimal startingMoney = new BigDecimal("1000.00");
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    new Player(null, startingMoney));
        }

        @Test
        @DisplayName("Should throw exception when name is blank")
        void throwsExceptionWhenNameIsBlank() {
            // Arrange
            BigDecimal startingMoney = new BigDecimal("1000.00");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Player("", startingMoney));
        }

        @Test
        @DisplayName("Should throw exception when starting money is null")
        void throwsExceptionWhenStartingMoneyIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    new Player("Alva", null));
        }

        @Test
        @DisplayName("Should throw exception when starting money is negative")
        void throwsExceptionWhenStartingMoneyIsNegative() {
            // Arrange
            BigDecimal negativeMoney = new BigDecimal("-1");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Player("Alva", negativeMoney));
        }

        @Test
        @DisplayName("Should initialize with an empty portfolio")
        void initializesWithEmptyPortfolio() {
            // Act & Assert
            assertTrue(player.getPortfolio().getShares().isEmpty());
        }

        @Test
        @DisplayName("Should initialize with an empty transaction archive")
        void initializesWithEmptyTransactionArchive() {
            // Act & Assert
            assertTrue(player.getTransactionArchive().isEmpty());
        }

        @Test
        @DisplayName("Should seed net worth history with starting money")
        void seedsNetWorthHistoryWithStartingMoney() {
            // Act
            List<BigDecimal> history = player.getNetWorthHistory();
            // Assert
            assertEquals(1, history.size());
            assertEquals(0, new BigDecimal("1000.00").compareTo(history.getFirst()));
        }
    }

    @Nested
    @DisplayName("addMoney()")
    class AddMoney {

        @Test
        @DisplayName("Should increase balance by the given amount")
        void increasesBalance() {
            // Arrange
            BigDecimal amount = new BigDecimal("500.00");
            // Act
            player.addMoney(amount);
            // Assert
            assertEquals(new BigDecimal("1500.00"), player.getMoney());
        }

        @Test
        @DisplayName("Should throw exception when amount is negative")
        void throwsExceptionWhenAmountIsNegative() {
            // Arrange
            BigDecimal negativeAmount = new BigDecimal("-1");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    player.addMoney(negativeAmount));
        }
    }

    @Nested
    @DisplayName("withdrawMoney()")
    class WithdrawMoney {

        @Test
        @DisplayName("Should decrease balance by the given amount")
        void decreasesBalance() {
            // Arrange
            BigDecimal amount = new BigDecimal("500.00");
            // Act
            player.withdrawMoney(amount);
            // Assert
            assertEquals(new BigDecimal("500.00"), player.getMoney());
        }

        @Test
        @DisplayName("Should allow withdrawing entire balance")
        void allowsWithdrawingEntireBalance() {
            // Act
            player.withdrawMoney(new BigDecimal("1000.00"));
            // Assert
            assertEquals(0, BigDecimal.ZERO.compareTo(player.getMoney()));
        }

        @Test
        @DisplayName("Should throw exception when withdrawing more than current balance")
        void throwsExceptionWhenInsufficientFunds() {
            // Arrange
            BigDecimal amount = new BigDecimal("2000.00");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    player.withdrawMoney(amount));
        }

        @Test
        @DisplayName("Should throw exception when withdrawing a negative amount")
        void throwsExceptionWhenAmountIsNegative() {
            // Arrange
            BigDecimal negativeAmount = new BigDecimal("-1");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    player.withdrawMoney(negativeAmount));
        }
    }

    @Nested
    @DisplayName("getNetWorth()")
    class GetNetWorth {

        @Test
        @DisplayName("Should return balance only when portfolio is empty")
        void returnsMoneyWhenPortfolioIsEmpty() {
            // Act & Assert
            assertEquals(0, player.getMoney().compareTo(player.getNetWorth(converter)));
        }

        @Test
        @DisplayName("Should return balance plus portfolio value when portfolio has NOK shares")
        void returnsMoneyAndPortfolioValue() {
            // Arrange
            Share share = new Share(nokStock("DCL", "Dara, Inc", new BigDecimal("1000.00")),
                    new BigDecimal("10"), new BigDecimal("700.00"));
            player.getPortfolio().addShare(share);
            BigDecimal expected = player.getMoney()
                    .add(player.getPortfolio().getNetWorth(converter));
            // Act & Assert
            assertEquals(0, expected.compareTo(player.getNetWorth(converter)));
        }

        @Test
        @DisplayName("Should return correct net worth after money is withdrawn")
        void returnsCorrectNetWorthAfterMoneyIsWithdrawn() {
            // Arrange
            player.withdrawMoney(new BigDecimal("500.00"));
            BigDecimal expected = player.getMoney()
                    .add(player.getPortfolio().getNetWorth(converter));
            // Act & Assert
            assertEquals(0, expected.compareTo(player.getNetWorth(converter)));
        }

        @Test
        @DisplayName("Should return correct net worth after share is removed from portfolio")
        void returnsCorrectNetWorthAfterShareRemoved() {
            // Arrange
            Share share = new Share(nokStock("DCL", "Dara, Inc", new BigDecimal("1000.00")),
                    new BigDecimal("10"), new BigDecimal("700.00"));
            player.getPortfolio().addShare(share);
            player.getPortfolio().removeShare(share);
            // Act & Assert
            assertEquals(0, player.getMoney().compareTo(player.getNetWorth(converter)));
        }

        @Test
        @DisplayName("Should not return a value less than current money balance")
        void returnsNotValueLessBalance() {
            // Arrange
            Share share = new Share(nokStock("DCL", "Dara, Inc", new BigDecimal("1000.00")),
                    new BigDecimal("10"), new BigDecimal("700.00"));
            player.getPortfolio().addShare(share);
            // Act
            BigDecimal result = player.getNetWorth(converter);
            // Assert
            assertTrue(result.compareTo(player.getMoney()) >= 0);
        }

        @Test
        @DisplayName("Should add converted USD share value to NOK balance")
        void addsConvertedUsdShareValueToBalance() {
            // Arrange
            Share usdShare = new Share(
                    new Stock("AAPL", "Apple Inc",
                            new ArrayList<>(List.of(new BigDecimal("100.00"))), USD),
                    new BigDecimal("5"), new BigDecimal("50.00"));
            player.getPortfolio().addShare(usdShare);
            BigDecimal expected = player.getMoney()
                    .add(player.getPortfolio().getNetWorth(converter));
            // Act
            BigDecimal actual = player.getNetWorth(converter);
            // Assert
            assertEquals(0, expected.compareTo(actual));
        }

        @Test
        @DisplayName("Should throw NullPointerException when converter is null")
        void throwsExceptionWhenConverterIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    player.getNetWorth(null));
        }
    }

    @Nested
    @DisplayName("recordNetWorth()")
    class RecordNetWorth {

        @Test
        @DisplayName("Should append current net worth to history")
        void appendsCurrentNetWorthToHistory() {
            // Arrange
            int initialSize = player.getNetWorthHistory().size();
            // Act
            player.recordNetWorth(converter);
            // Assert
            assertEquals(initialSize + 1, player.getNetWorthHistory().size());
        }

        @Test
        @DisplayName("Should record matching value as getNetWorth")
        void recordsMatchingValue() {
            // Arrange
            BigDecimal expected = player.getNetWorth(converter);
            // Act
            player.recordNetWorth(converter);
            // Assert
            BigDecimal recorded = player.getNetWorthHistory().getLast();
            assertEquals(0, expected.compareTo(recorded));
        }

        @Test
        @DisplayName("Should support multiple recordings in order")
        void supportsMultipleRecordings() {
            // Arrange
            player.recordNetWorth(converter);
            player.addMoney(new BigDecimal("500.00"));
            player.recordNetWorth(converter);
            // Act
            List<BigDecimal> history = player.getNetWorthHistory();
            // Assert: starting money + two recordings
            assertEquals(3, history.size());
            assertTrue(history.getLast().compareTo(history.get(history.size() - 2)) >= 0);
        }

        @Test
        @DisplayName("Should throw NullPointerException when converter is null")
        void throwsExceptionWhenConverterIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    player.recordNetWorth(null));
        }
    }

    @Nested
    @DisplayName("getNetWorthHistory()")
    class GetNetWorthHistory {

        @Test
        @DisplayName("Should return a copy that does not affect the internal list")
        void returnsCopy() {
            // Arrange
            List<BigDecimal> history = player.getNetWorthHistory();
            // Act
            history.clear();
            // Assert
            assertFalse(player.getNetWorthHistory().isEmpty());
        }

        @Test
        @DisplayName("Should reflect every recordNetWorth call in order")
        void reflectsRecordingsInOrder() {
            // Arrange
            player.recordNetWorth(converter);
            player.addMoney(new BigDecimal("100.00"));
            player.recordNetWorth(converter);
            // Act
            List<BigDecimal> history = player.getNetWorthHistory();
            // Assert
            assertEquals(3, history.size());
        }
    }

    @Nested
    @DisplayName("recordTotalDebt()")
    class RecordTotalDebt {

        @Test
        @DisplayName("Should append current total debt to history")
        void appendsCurrentTotalDebtToHistory() {
            // Arrange
            int initialSize = player.getTotalDebtHistory().size();
            // Act
            player.recordTotalDebt();
            // Assert
            assertEquals(initialSize + 1, player.getTotalDebtHistory().size());
        }

        @Test
        @DisplayName("Should record matching value as getTotalDebt")
        void recordsMatchingValue() {
            // Arrange
            BigDecimal expected = player.getTotalDebt();
            // Act
            player.recordTotalDebt();
            // Assert
            BigDecimal recorded = player.getTotalDebtHistory().getLast();
            assertEquals(0, expected.compareTo(recorded));
        }

        @Test
        @DisplayName("Should record zero debt when no loans are active")
        void recordsZeroWhenNoLoans() {
            // Act
            player.recordTotalDebt();
            // Assert
            assertEquals(0, BigDecimal.ZERO.compareTo(player.getTotalDebtHistory().getLast()));
        }

        @Test
        @DisplayName("Should support multiple recordings in order")
        void supportsMultipleRecordings() {
            // Arrange
            LoanOffer offer = new LoanOffer("test", new BigDecimal("0.01"), 10,
                    new BigDecimal("50000.00"), LoanRiskLevel.LOW);
            player.recordTotalDebt(); // debt = 0
            player.takeLoan(new Loan(offer, new BigDecimal("200.00"), 0), converter);
            player.recordTotalDebt(); // debt = 200
            // Act
            List<BigDecimal> history = player.getTotalDebtHistory();
            // Assert: two recordings
            assertEquals(2, history.size());
            assertTrue(history.getLast().compareTo(history.getFirst()) > 0);
        }
    }

    @Nested
    @DisplayName("getTotalDebtHistory()")
    class GetTotalDebtHistory {

        @Test
        @DisplayName("Should start empty before any recording")
        void startsEmpty() {
            assertTrue(player.getTotalDebtHistory().isEmpty());
        }

        @Test
        @DisplayName("Should return a copy that does not affect the internal list")
        void returnsCopy() {
            // Arrange
            player.recordTotalDebt();
            List<BigDecimal> history = player.getTotalDebtHistory();
            // Act
            history.clear();
            // Assert
            assertFalse(player.getTotalDebtHistory().isEmpty());
        }

        @Test
        @DisplayName("Should reflect every recordTotalDebt call in order")
        void reflectsRecordingsInOrder() {
            // Arrange
            LoanOffer offer = new LoanOffer("test", new BigDecimal("0.01"), 10,
                    new BigDecimal("50000.00"), LoanRiskLevel.LOW);
            player.recordTotalDebt(); // 0
            player.takeLoan(new Loan(offer, new BigDecimal("100.00"), 0), converter);
            player.recordTotalDebt(); // 100
            // Act
            List<BigDecimal> history = player.getTotalDebtHistory();
            // Assert
            assertEquals(2, history.size());
            assertEquals(0, BigDecimal.ZERO.compareTo(history.getFirst()));
            assertEquals(0, new BigDecimal("100.00").compareTo(history.getLast()));
        }
    }

    @Nested
    @DisplayName("getStatus()")
    class GetStatus {

        @Test
        @DisplayName("Should return NOVICE when player has just started")
        void returnsNoviceStatusWhenPlayerHasJustStarted() {
            // Act & Assert
            assertEquals(PlayerStatusLevel.NOVICE, player.getStatus(converter));
        }

        @Test
        @DisplayName("Should return NOVICE when player has traded less than 10 weeks")
        void returnsNoviceStatusWhenLessThan10Weeks() {
            // Arrange
            Stock stock = nokStock("DCL", "Dara, Inc", new BigDecimal("1000.00"));
            Share share = new Share(stock, new BigDecimal("8"), new BigDecimal("100.00"));
            Purchase purchase = new Purchase(share, 1);
            purchase.commit(player);
            // Act & Assert
            assertEquals(PlayerStatusLevel.NOVICE, player.getStatus(converter));
        }

        @Test
        @DisplayName("Should return NOVICE when player has traded 10 weeks but not increased net "
                + "worth by 20%")
        void returnNoviceStatusWhenEnoughWeeksNotEnoughGrowth() {
            // Arrange
            player = new Player("AKL", new BigDecimal("800.00"));
            for (int week = 1; week <= 10; week++) {
                Stock stock = nokStock("MR" + week, "Majid Company" + week,
                        new BigDecimal("10.00"));
                Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("1.00"));
                Purchase purchase = new Purchase(share, week);
                purchase.commit(player);
            }
            // Not enough net worth growth
            player.addMoney(new BigDecimal("50.00"));
            // Act & Assert
            assertEquals(PlayerStatusLevel.NOVICE, player.getStatus(converter));
        }

        @Test
        @DisplayName("Should return INVESTOR when player has traded 10 weeks "
                + "and increased their net worth by 20%")
        void returnsInvestorWhenConditionsMet() {
            // Arrange
            player = new Player("AKL", new BigDecimal("1000.00"));
            for (int week = 1; week <= 10; week++) {
                Stock stock = nokStock("MR" + week, "Majid Company" + week,
                        new BigDecimal("10.00"));
                Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("1.00"));
                Purchase purchase = new Purchase(share, week);
                purchase.commit(player);
            }
            player.addMoney(new BigDecimal("500.00"));
            // Act & Assert
            assertEquals(PlayerStatusLevel.INVESTOR, player.getStatus(converter));
        }

        @Test
        @DisplayName("Should not return INVESTOR when player has traded 10 weeks but not "
                + "increased their net worth by 20%")
        void returnsNotInvestorWhenNotEnoughGrowth() {
            // Arrange
            player = new Player("AKL", new BigDecimal("1000.00"));
            for (int week = 1; week <= 10; week++) {
                Stock stock = nokStock("DCL", "Dara, Inc", new BigDecimal("10.00"));
                Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("1.00"));
                Purchase purchase = new Purchase(share, week);
                purchase.commit(player);
            }
            player.addMoney(new BigDecimal("50.00"));
            // Act & Assert
            assertNotEquals(PlayerStatusLevel.INVESTOR, player.getStatus(converter));
        }

        @Test
        @DisplayName("Should not return INVESTOR when player has increased net worth by 20% "
                + "but traded less than 10 weeks")
        void returnsNotInvestorWhenEnoughGrowthNotWeeks() {
            // Arrange
            player = new Player("AKL", new BigDecimal("1000.00"));
            for (int week = 1; week <= 5; week++) {
                Stock stock = nokStock("MR" + week, "Majid Company" + week,
                        new BigDecimal("10.00"));
                Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("1.00"));
                Purchase purchase = new Purchase(share, week);
                purchase.commit(player);
            }
            player.addMoney(new BigDecimal("500.00"));
            // Act & Assert
            assertNotEquals(PlayerStatusLevel.INVESTOR, player.getStatus(converter));
        }

        @Test
        @DisplayName("Should return SPECULATOR when player has traded 20 weeks and doubled"
                + " net worth")
        void returnsSpeculatorWhenConditionsMet() {
            // Arrange
            player = new Player("DCL", new BigDecimal("1000.00"));
            for (int week = 1; week <= 20; week++) {
                Stock stock = nokStock("MR" + week, "Majid Company" + week,
                        new BigDecimal("10.00"));
                Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("1.00"));
                Purchase purchase = new Purchase(share, week);
                purchase.commit(player);
            }
            player.addMoney(new BigDecimal("2000.00"));
            // Act & Assert
            assertEquals(PlayerStatusLevel.SPECULATOR, player.getStatus(converter));
        }

        @Test
        @DisplayName("Should not return SPECULATOR when player has traded 20 weeks "
                + "but not doubled their net worth")
        void returnsNotSpeculatorWhenNotEnoughGrowth() {
            // Arrange
            player = new Player("AKL", new BigDecimal("1000.00"));
            for (int week = 1; week <= 20; week++) {
                Stock stock = nokStock("DCL" + week, "Dara, Inc" + week,
                        new BigDecimal("10.00"));
                Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("1.00"));
                Purchase purchase = new Purchase(share, week);
                purchase.commit(player);
            }
            player.addMoney(new BigDecimal("500.00"));
            // Act & Assert
            assertNotEquals(PlayerStatusLevel.SPECULATOR, player.getStatus(converter));
        }

        @Test
        @DisplayName("Should not return SPECULATOR when player has doubled net worth "
                + "but traded less than 20 weeks")
        void returnsNotSpeculatorWhenNotEnoughWeeks() {
            // Arrange
            player = new Player("AKL", new BigDecimal("1000.00"));
            for (int week = 1; week <= 10; week++) {
                Stock stock = nokStock("DCL" + week, "Dara, Inc" + week,
                        new BigDecimal("10.00"));
                Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("1.00"));
                Purchase purchase = new Purchase(share, week);
                purchase.commit(player);
            }
            player.addMoney(new BigDecimal("2000.00"));
            // Act & Assert
            assertNotEquals(PlayerStatusLevel.SPECULATOR, player.getStatus(converter));
        }

        @Test
        @DisplayName("Should throw NullPointerException when converter is null")
        void throwsExceptionWhenConverterIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    player.getStatus(null));
        }
    }

    @Nested
    @DisplayName("previousNetWorth")
    class PreviousNetWorth {

        @Test
        @DisplayName("Should return null when previousNetWorth has not been set")
        void returnsNullWhenNotSet() {
            assertNull(player.getPreviousNetWorth());
        }

        @Test
        @DisplayName("Should return the value that was set")
        void returnsValueWhenSet() {
            // Arrange
            BigDecimal expected = new BigDecimal("5000.00");

            // Act
            player.setPreviousNetWorth(expected);

            // Assert
            assertEquals(expected, player.getPreviousNetWorth());
        }

        @Test
        @DisplayName("Should update when set multiple times")
        void updatesWhenSetMultipleTimes() {
            // Arrange
            player.setPreviousNetWorth(new BigDecimal("5000.00"));

            // Act
            player.setPreviousNetWorth(new BigDecimal("7500.00"));

            // Assert
            assertEquals(new BigDecimal("7500.00"), player.getPreviousNetWorth());
        }
    }

    @Nested
    @DisplayName("Loans")
    class Loans {

        private LoanOffer offer;

        @BeforeEach
        void setUpOffer() {
            // simple offer with generous maxPrincipal so tests can isolate capacity logic
            offer = new LoanOffer("test", new BigDecimal("0.01"), 10,
                    new BigDecimal("50000.00"), LoanRiskLevel.LOW);
        }

        @Test
        @DisplayName("getTotalDebt() returns zero when no active loans")
        void getTotalDebtReturnsZeroWithNoLoans() {
            // Act & Assert
            assertEquals(0, BigDecimal.ZERO.compareTo(player.getTotalDebt()));
        }

        @Test
        @DisplayName("getTotalDebt() sums principals of multiple loans")
        void getTotalDebtSumsMultipleLoans() {
            // Arrange — player starts with 1000, net worth=1000, capacity=500
            player.takeLoan(new Loan(offer, new BigDecimal("200.00"), 0), converter);
            // After first loan: money=1200, debt=200, net worth=1000, capacity=500, available=300
            player.takeLoan(new Loan(offer, new BigDecimal("150.00"), 0), converter);
            // Assert
            assertEquals(0, new BigDecimal("350.00").compareTo(player.getTotalDebt()));
        }

        @Test
        @DisplayName("getLoanCapacity() equals net worth times MAX_DEBT_RATIO")
        void getLoanCapacityEqualsNetWorthTimesRatio() {
            // Arrange
            BigDecimal expected = player.getNetWorth(converter)
                    .multiply(Player.MAX_DEBT_RATIO)
                    .setScale(2, RoundingMode.HALF_UP);
            // Act & Assert
            assertEquals(0, expected.compareTo(player.getLoanCapacity(converter)));
        }

        @Test
        @DisplayName("getAvailableLoanCapacity() equals loan capacity when no debt")
        void getAvailableLoanCapacityEqualsCapacityWithNoDebt() {
            // Act & Assert
            assertEquals(0, player.getLoanCapacity(converter)
                    .compareTo(player.getAvailableLoanCapacity(converter)));
        }

        @Test
        @DisplayName("getAvailableLoanCapacity() decreases after taking a loan")
        void getAvailableLoanCapacityDecreasesAfterLoan() {
            // Arrange
            BigDecimal before = player.getAvailableLoanCapacity(converter);
            // Act
            player.takeLoan(new Loan(offer, new BigDecimal("200.00"), 0), converter);
            BigDecimal after = player.getAvailableLoanCapacity(converter);
            // Assert
            assertTrue(after.compareTo(before) < 0);
        }

        @Test
        @DisplayName("takeLoan() increases player money by the principal")
        void takeLoanIncreasesMoney() {
            // Arrange
            BigDecimal before = player.getMoney();
            BigDecimal principal = new BigDecimal("300.00");
            // Act
            player.takeLoan(new Loan(offer, principal, 0), converter);
            // Assert
            assertEquals(0, before.add(principal).compareTo(player.getMoney()));
        }

        @Test
        @DisplayName("takeLoan() adds the loan to active loans")
        void takeLoanAddsToActiveLoans() {
            // Arrange
            Loan loan = new Loan(offer, new BigDecimal("300.00"), 0);
            // Act
            player.takeLoan(loan, converter);
            // Assert
            assertTrue(player.getActiveLoans().contains(loan));
        }

        @Test
        @DisplayName("takeLoan() throws ExcessiveDebtException when limit would be breached")
        void takeLoanThrowsWhenCapacityExceeded() {
            // Arrange — player starts with 1000, capacity = 500; request 600 > 500
            assertThrows(ExcessiveDebtException.class, () ->
                    player.takeLoan(new Loan(offer, new BigDecimal("600.00"), 0), converter));
        }

        @Test
        @DisplayName("takeLoan() throws NullPointerException when loan is null")
        void takeLoanThrowsWhenLoanIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    player.takeLoan(null, converter));
        }

        @Test
        @DisplayName("takeLoan() does not modify money or active loans when it throws")
        void takeLoanIsAtomic() {
            // Arrange
            BigDecimal moneyBefore = player.getMoney();
            int loanCountBefore = player.getActiveLoans().size();
            // Act — request 600 > capacity 500 → throws
            assertThrows(ExcessiveDebtException.class, () ->
                    player.takeLoan(new Loan(offer, new BigDecimal("600.00"), 0), converter));
            // Assert — state unchanged
            assertEquals(0, moneyBefore.compareTo(player.getMoney()));
            assertEquals(loanCountBefore, player.getActiveLoans().size());
        }

        @Test
        @DisplayName("repayLoan() removes the loan from active loans")
        void repayLoanRemovesFromActiveLoans() {
            // Arrange
            Loan loan = new Loan(offer, new BigDecimal("200.00"), 0);
            player.takeLoan(loan, converter);
            // Act
            player.repayLoan(loan, 1);
            // Assert
            assertFalse(player.getActiveLoans().contains(loan));
        }

        @Test
        @DisplayName("repayLoan() deducts the principal from player money")
        void repayLoanDeductsMoney() {
            // Arrange
            Loan loan = new Loan(offer, new BigDecimal("200.00"), 0);
            player.takeLoan(loan, converter); // money: 1000 + 200 = 1200
            BigDecimal moneyAfterTake = player.getMoney();
            // Act
            player.repayLoan(loan, 1); // money: 1200 - 200 = 1000
            // Assert
            assertEquals(0, moneyAfterTake.subtract(new BigDecimal("200.00")).compareTo(player.getMoney()));
        }

        @Test
        @DisplayName("repayLoan() throws when player cannot afford the principal")
        void repayLoanThrowsWhenInsufficientFunds() {
            // Arrange — give player a tiny balance by draining most of their cash
            Player broke = new Player("Broke", new BigDecimal("100.00"));
            // Manually add a loan with a principal larger than cash using internal via takeLoan
            // Use a fresh offer with maxPrincipal matching the player's capacity
            LoanOffer bigOffer = new LoanOffer("big", new BigDecimal("0.01"), 4,
                    new BigDecimal("50.00"), edu.ntnu.idatt2003.millions.model.loan.LoanRiskLevel.LOW);
            Loan loan = new Loan(bigOffer, new BigDecimal("50.00"), 0);
            broke.takeLoan(loan, converter); // money: 100 + 50 = 150
            broke.withdrawMoney(new BigDecimal("140.00")); // money: 10, principal: 50
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> broke.repayLoan(loan, 1));
        }

        @Test
        @DisplayName("repayLoan() throws when loan is not in the active list")
        void repayLoanThrowsForUnknownLoan() {
            // Arrange
            Loan loan = new Loan(offer, new BigDecimal("200.00"), 0);
            // Act & Assert — loan never taken, so not in active list
            assertThrows(IllegalArgumentException.class, () -> player.repayLoan(loan, 1));
        }

        @Test
        @DisplayName("getNetWorth() subtracts outstanding debt")
        void getNetWorthSubtractsDebt() {
            // Arrange — player starts with 1000 cash
            BigDecimal principal = new BigDecimal("300.00");
            player.takeLoan(new Loan(offer, principal, 0), converter);
            // money = 1300, debt = 300 → net worth = 1300 - 300 = 1000
            assertEquals(0, new BigDecimal("1000.00").compareTo(player.getNetWorth(converter)));
        }

        @Test
        @DisplayName("Taking a loan does not change net worth")
        void takingLoanDoesNotChangeNetWorth() {
            // Arrange
            BigDecimal before = player.getNetWorth(converter);
            // Act
            player.takeLoan(new Loan(offer, new BigDecimal("300.00"), 0), converter);
            // Assert — cash up by 300, debt up by 300: net effect is zero
            assertEquals(0, before.compareTo(player.getNetWorth(converter)));
        }

        @Test
        @DisplayName("Repaying a loan does not change net worth")
        void repayingLoanDoesNotChangeNetWorth() {
            // Arrange
            Loan loan = new Loan(offer, new BigDecimal("300.00"), 0);
            player.takeLoan(loan, converter);
            BigDecimal before = player.getNetWorth(converter);
            // Act
            player.repayLoan(loan, 1);
            // Assert — cash down by 300, debt down by 300: net effect is zero
            assertEquals(0, before.compareTo(player.getNetWorth(converter)));
        }

        @Test
        @DisplayName("getNetWorth() can be negative when debt exceeds assets")
        void getNetWorthCanBeNegative() {
            // Arrange — take a loan, then drain cash below the principal
            Loan loan = new Loan(offer, new BigDecimal("400.00"), 0);
            player.takeLoan(loan, converter);        // cash=1400, debt=400, net worth=1000
            player.withdrawMoney(new BigDecimal("1390.00")); // cash=10, debt=400, net worth=-390
            // Act & Assert
            assertTrue(player.getNetWorth(converter).compareTo(BigDecimal.ZERO) < 0);
        }

        @Test
        @DisplayName("getLoanCapacity() clamps to zero when net worth is negative")
        void getLoanCapacityIsZeroOnNegativeNetWorth() {
            // Arrange — same setup as above produces negative net worth
            Loan loan = new Loan(offer, new BigDecimal("400.00"), 0);
            player.takeLoan(loan, converter);
            player.withdrawMoney(new BigDecimal("1390.00"));
            // Act & Assert
            assertEquals(0, BigDecimal.ZERO.compareTo(player.getLoanCapacity(converter)));
        }

        @Test
        @DisplayName("takeLoan() appends a DISBURSEMENT ledger entry")
        void takeLoanAppendsDisbursementEntry() {
            Loan loan = new Loan(offer, new BigDecimal("200.00"), 1);
            player.takeLoan(loan, converter);
            List<LoanLedgerEntry> ledger = player.getLoanLedger();
            assertEquals(1, ledger.size());
            LoanLedgerEntry entry = ledger.getFirst();
            assertEquals(LoanLedgerEntryType.DISBURSEMENT, entry.type());
            assertEquals(0, new BigDecimal("200.00").compareTo(entry.amount()));
            assertEquals(loan, entry.loan());
        }

        @Test
        @DisplayName("repayLoan() appends a REPAYMENT ledger entry with negative amount")
        void repayLoanAppendsRepaymentEntry() {
            Loan loan = new Loan(offer, new BigDecimal("200.00"), 1);
            player.takeLoan(loan, converter);
            player.repayLoan(loan, 2);
            List<LoanLedgerEntry> ledger = player.getLoanLedger();
            assertEquals(2, ledger.size());
            LoanLedgerEntry repayment = ledger.getLast();
            assertEquals(LoanLedgerEntryType.REPAYMENT, repayment.type());
            assertEquals(0, new BigDecimal("-200.00").compareTo(repayment.amount()));
            assertEquals(2, repayment.week());
        }

        @Test
        @DisplayName("getLoanLedger() returns a defensive copy")
        void getLoanLedgerReturnsDefensiveCopy() {
            Loan loan = new Loan(offer, new BigDecimal("100.00"), 1);
            player.takeLoan(loan, converter);
            List<LoanLedgerEntry> ledger = player.getLoanLedger();
            ledger.clear();
            assertEquals(1, player.getLoanLedger().size());
        }

        @Test
        @DisplayName("Ledger is empty for a player with no loan activity")
        void ledgerIsEmptyWithNoActivity() {
            assertTrue(player.getLoanLedger().isEmpty());
        }

        @Test
        @DisplayName("Stacking loans to bypass the 50% cap is blocked")
        void stackingLoansIsBlocked() {
            // Arrange — player starts with 10 000 NOK, capacity = 5 000
            Player rich = new Player("Rich", new BigDecimal("10000.00"));
            LoanOffer bigOffer = new LoanOffer("big", new BigDecimal("0.01"), 10,
                    new BigDecimal("50000.00"), LoanRiskLevel.LOW);
            // First loan fills capacity exactly: debt=5000, net worth still 10000, available=0
            rich.takeLoan(new Loan(bigOffer, new BigDecimal("5000.00"), 0), converter);
            // Act & Assert — any further loan must be rejected
            assertThrows(ExcessiveDebtException.class, () ->
                    rich.takeLoan(new Loan(bigOffer, new BigDecimal("1.00"), 0), converter));
        }

        @Test
        @DisplayName("getWeeklyInterestDue() returns zero when no active loans")
        void getWeeklyInterestDueIsZeroWhenNoActiveLoans() {
            assertEquals(0, BigDecimal.ZERO.compareTo(player.getWeeklyInterestDue()));
        }

        @Test
        @DisplayName("getWeeklyInterestDue() sums interest across all active loans")
        void getWeeklyInterestDueSumsAllActiveLoans() {
            // Arrange — player starts with 1000 NOK, capacity = 500.
            // Two loans within capacity: 200 NOK at 1% = 2.00, 100 NOK at 1% = 1.00 → total 3.00
            player.takeLoan(new Loan(offer, new BigDecimal("200.00"), 1), converter);
            player.takeLoan(new Loan(offer, new BigDecimal("100.00"), 1), converter);
            assertEquals(0, new BigDecimal("3.00").compareTo(player.getWeeklyInterestDue()));
        }

        @Test
        @DisplayName("canCoverInterestThisWeek() returns true when cash >= interest due")
        void canCoverInterestThisWeekReturnsTrueWhenMoneyExceedsDue() {
            // Arrange — take 400 NOK loan (within 500 capacity): money = 1400, interest = 4.00
            player.takeLoan(new Loan(offer, new BigDecimal("400.00"), 1), converter);
            assertTrue(player.canCoverInterestThisWeek());
        }

        @Test
        @DisplayName("canCoverInterestThisWeek() returns false when cash < interest due")
        void canCoverInterestThisWeekReturnsFalseWhenMoneyIsLess() {
            // Arrange — take 400 NOK loan: money = 1400, interest = 4.00. Then drain to 3.00.
            player.takeLoan(new Loan(offer, new BigDecimal("400.00"), 1), converter);
            player.withdrawMoney(new BigDecimal("1397.00")); // money = 3.00 < 4.00 interest
            assertFalse(player.canCoverInterestThisWeek());
        }
    }
}
