package edu.ntnu.idatt2003.millions.service.stock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.stock.StockHistoryService.WeeklyPriceChange;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link StockHistoryService}.
 */
class StockHistoryServiceTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency NOK = Currency.getInstance("NOK");
    private static final BigDecimal USD_TO_NOK = BigDecimal.valueOf(10);

    private StockHistoryService service;

    /**
     * Initializes test fixtures before each test.
     * Test data values were generated with AI assistance and reviewed manually.
     */
    @BeforeEach
    void setUp() {
        service = new StockHistoryService();
    }

    /**
     * A simple deterministic converter: USD is worth ten NOK, identity for equal
     * currencies, and any other source currency is rejected with
     * {@link IllegalArgumentException} (matching the {@link CurrencyConverter} contract).
     */
    private static CurrencyConverter converter() {
        return (amount, from, to) -> {
            if (from.equals(to)) {
                return amount;
            }
            if (from.equals(USD) && to.equals(NOK)) {
                return amount.multiply(USD_TO_NOK);
            }
            throw new IllegalArgumentException("Unsupported currency: " + from.getCurrencyCode());
        };
    }

    private static Stock stockWith(Currency currency, double... prices) {
        List<BigDecimal> list = new ArrayList<>();
        for (double price : prices) {
            list.add(BigDecimal.valueOf(price));
        }
        return new Stock("TST", "Test Inc.", list, currency);
    }

    private static Stock usdStock(double... prices) {
        return stockWith(USD, prices);
    }

    private static List<Integer> weeksOf(List<WeeklyPriceChange> rows) {
        return rows.stream().map(WeeklyPriceChange::week).toList();
    }

    private static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual),
                () -> "expected " + expected + " but was " + actual);
    }

    @Nested
    @DisplayName("getRecentWeeklyChanges()")
    class GetRecentWeeklyChanges {

        @Test
        @DisplayName("Should return four rows newest first when five prices exist")
        void returnsFourRowsNewestFirstWhenFivePricesExist() {
            // Arrange
            Stock stock = usdStock(100, 110, 105, 120, 126);

            // Act
            List<WeeklyPriceChange> rows = service.getRecentWeeklyChanges(stock, converter());

            // Assert
            assertEquals(4, rows.size());
            assertEquals(List.of(5, 4, 3, 2), weeksOf(rows));
        }

        @Test
        @DisplayName("Should compute native change, NOK change and percent for the newest week")
        void computesNativeNokAndPercentForNewestWeek() {
            // Arrange: week 5 = 126, week 4 = 120 -> native = 6, nok = 6 * 10 = 60,
            //          percent = 6 / 120 * 100 = 5.00
            Stock stock = usdStock(100, 110, 105, 120, 126);

            // Act
            WeeklyPriceChange newest = service.getRecentWeeklyChanges(stock, converter()).get(0);

            // Assert
            assertEquals(5, newest.week());
            assertBigDecimalEquals(BigDecimal.valueOf(6), newest.nativeChange());
            assertBigDecimalEquals(BigDecimal.valueOf(60), newest.nokChange());
            assertBigDecimalEquals(new BigDecimal("5.00"), newest.percentChange());
        }

        @Test
        @DisplayName("Should round percent half up to two decimals")
        void roundsPercentHalfUpToTwoDecimals() {
            // Arrange: prev = 160, cur = 181 -> native = 21,
            //          percent = 2100 / 160 = 13.125 -> HALF_UP to 2 decimals = 13.13
            Stock stock = usdStock(160, 181);

            // Act
            WeeklyPriceChange row = service.getRecentWeeklyChanges(stock, converter()).get(0);

            // Assert
            assertBigDecimalEquals(new BigDecimal("13.13"), row.percentChange());
        }

        @Test
        @DisplayName("Should produce a negative change and percent when the price falls")
        void producesNegativeChangeWhenPriceFalls() {
            // Arrange: prev = 200, cur = 190 -> native = -10, nok = -100,
            //          percent = -10 / 200 * 100 = -5.00
            Stock stock = usdStock(200, 190);

            // Act
            WeeklyPriceChange row = service.getRecentWeeklyChanges(stock, converter()).get(0);

            // Assert
            assertBigDecimalEquals(BigDecimal.valueOf(-10), row.nativeChange());
            assertBigDecimalEquals(BigDecimal.valueOf(-100), row.nokChange());
            assertBigDecimalEquals(new BigDecimal("-5.00"), row.percentChange());
        }

        @Test
        @DisplayName("Should return an empty list in week one when only one price exists")
        void returnsEmptyListWhenOnlyOnePriceExists() {
            // Arrange
            Stock stock = usdStock(100);

            // Act
            List<WeeklyPriceChange> rows = service.getRecentWeeklyChanges(stock, converter());

            // Assert
            assertTrue(rows.isEmpty());
        }

        @Test
        @DisplayName("Should return one row in week two when two prices exist")
        void returnsOneRowWhenTwoPricesExist() {
            // Arrange
            Stock stock = usdStock(100, 110);

            // Act
            List<WeeklyPriceChange> rows = service.getRecentWeeklyChanges(stock, converter());

            // Assert
            assertEquals(List.of(2), weeksOf(rows));
        }

        @Test
        @DisplayName("Should return two rows in week three when three prices exist")
        void returnsTwoRowsWhenThreePricesExist() {
            // Arrange
            Stock stock = usdStock(100, 110, 120);

            // Act
            List<WeeklyPriceChange> rows = service.getRecentWeeklyChanges(stock, converter());

            // Assert
            assertEquals(List.of(3, 2), weeksOf(rows));
        }

        @Test
        @DisplayName("Should cap at four rows by default when many prices exist")
        void capsAtFourRowsByDefaultWhenManyPricesExist() {
            // Arrange: six prices (weeks 1..6)
            Stock stock = usdStock(10, 20, 30, 40, 50, 60);

            // Act
            List<WeeklyPriceChange> rows = service.getRecentWeeklyChanges(stock, converter());

            // Assert
            assertEquals(List.of(6, 5, 4, 3), weeksOf(rows));
        }

        @Test
        @DisplayName("Should respect a custom maxRows smaller than the number of transitions")
        void respectsCustomMaxRowsSmallerThanTransitions() {
            // Arrange: five prices -> four transitions, but only two requested
            Stock stock = usdStock(100, 110, 120, 130, 140);

            // Act
            List<WeeklyPriceChange> rows = service.getRecentWeeklyChanges(stock, converter(), 2);

            // Assert
            assertEquals(List.of(5, 4), weeksOf(rows));
        }

        @Test
        @DisplayName("Should adapt to the available transitions when maxRows exceeds them")
        void adaptsToAvailableTransitionsWhenMaxRowsExceedsThem() {
            // Arrange: three prices -> only two transitions, ten requested
            Stock stock = usdStock(100, 110, 120);

            // Act
            List<WeeklyPriceChange> rows = service.getRecentWeeklyChanges(stock, converter(), 10);

            // Assert
            assertEquals(List.of(3, 2), weeksOf(rows));
        }

        @Test
        @DisplayName("Should throw NullPointerException when stock is null")
        void throwsNullPointerExceptionWhenStockIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> service.getRecentWeeklyChanges(null, converter()));
        }

        @Test
        @DisplayName("Should throw NullPointerException when converter is null")
        void throwsNullPointerExceptionWhenConverterIsNull() {
            // Arrange
            Stock stock = usdStock(100, 110);

            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> service.getRecentWeeklyChanges(stock, null));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when maxRows is zero")
        void throwsIllegalArgumentExceptionWhenMaxRowsIsZero() {
            // Arrange
            Stock stock = usdStock(100, 110);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> service.getRecentWeeklyChanges(stock, converter(), 0));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when maxRows is negative")
        void throwsIllegalArgumentExceptionWhenMaxRowsIsNegative() {
            // Arrange
            Stock stock = usdStock(100, 110);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> service.getRecentWeeklyChanges(stock, converter(), -1));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when the stock currency is unsupported")
        void throwsIllegalArgumentExceptionWhenCurrencyIsUnsupported() {
            // Arrange: the converter rejects EUR as a source currency
            Stock stock = stockWith(EUR, 100, 110);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> service.getRecentWeeklyChanges(stock, converter()));
        }
    }

    @Nested
    @DisplayName("getWeeklyChanges()")
    class GetWeeklyChanges {

        @Test
        @DisplayName("Should return rows for the selected range, newest first")
        void returnsRowsForSelectedRange() {
            // Arrange: prices weeks 1..5
            Stock stock = usdStock(100, 110, 105, 120, 126);

            // Act: weeks 2..4 -> transition rows 4, 3, 2
            List<WeeklyPriceChange> rows = service.getWeeklyChanges(stock, converter(), 2, 4);

            // Assert
            assertEquals(List.of(4, 3, 2), weeksOf(rows));
        }

        @Test
        @DisplayName("Should clamp the upper bound to the available price history")
        void clampsUpperBoundToAvailablePrices() {
            // Arrange: only 5 prices, but week 99 requested
            Stock stock = usdStock(100, 110, 105, 120, 126);

            // Act
            List<WeeklyPriceChange> rows = service.getWeeklyChanges(stock, converter(), 2, 99);

            // Assert
            assertEquals(List.of(5, 4, 3, 2), weeksOf(rows));
        }

        @Test
        @DisplayName("Should include a zero-change row for week one when fromWeek is one")
        void includesWeekOneZeroChangeRowWhenFromWeekIsOne() {
            // Arrange
            Stock stock = usdStock(100, 110, 105, 120, 126);

            // Act: from week 1 -> rows 3, 2 plus a zero-change row for week 1
            List<WeeklyPriceChange> rows = service.getWeeklyChanges(stock, converter(), 1, 3);

            // Assert
            assertEquals(List.of(3, 2, 1), weeksOf(rows));
        }

        @Test
        @DisplayName("Should return a single zero-change row for week one when range is one to one")
        void returnsWeekOneZeroChangeRowWhenRangeIsOneToOne() {
            // Arrange
            Stock stock = usdStock(100, 110, 105);

            // Act: range [1, 1] contains no transition, but week 1 zero-change row is appended
            List<WeeklyPriceChange> rows = service.getWeeklyChanges(stock, converter(), 1, 1);

            // Assert
            assertEquals(List.of(1), weeksOf(rows));
        }

        @Test
        @DisplayName("Should compute native change, NOK change and percent for a single-week range")
        void computesValuesForSingleWeekRange() {
            // Arrange: week 4 = 126, week 3 = 105 -> native = 21, nok = 210, percent = 2100 / 105 = 20.00
            Stock stock = usdStock(100, 110, 105, 126);

            // Act
            WeeklyPriceChange row = service.getWeeklyChanges(stock, converter(), 4, 4).get(0);

            // Assert
            assertEquals(4, row.week());
            assertBigDecimalEquals(BigDecimal.valueOf(21), row.nativeChange());
            assertBigDecimalEquals(BigDecimal.valueOf(210), row.nokChange());
            assertBigDecimalEquals(new BigDecimal("20.00"), row.percentChange());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when toWeek is before fromWeek")
        void throwsIllegalArgumentExceptionWhenToBeforeFrom() {
            // Arrange
            Stock stock = usdStock(100, 110, 105);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> service.getWeeklyChanges(stock, converter(), 5, 3));
        }

        @Test
        @DisplayName("Should throw NullPointerException when stock is null")
        void throwsNullPointerExceptionWhenStockIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> service.getWeeklyChanges(null, converter(), 1, 4));
        }

        @Test
        @DisplayName("Should throw NullPointerException when converter is null")
        void throwsNullPointerExceptionWhenConverterIsNull() {
            // Arrange
            Stock stock = usdStock(100, 110);

            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> service.getWeeklyChanges(stock, null, 1, 2));
        }
    }
}
