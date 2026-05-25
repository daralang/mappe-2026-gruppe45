package edu.ntnu.idatt2003.millions.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link StockStatsService}.
 */
class StockStatsServiceTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency NOK = Currency.getInstance("NOK");
    private static final BigDecimal USD_TO_NOK = BigDecimal.valueOf(10);

    private StockStatsService service;

    @BeforeEach
    void setUp() {
        service = new StockStatsService();
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
        return new Stock("Alva", "Dara Inc.", list, currency);
    }

    private static Stock usdStock(double... prices) {
        return stockWith(USD, prices);
    }

    private static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual),
                () -> "expected " + expected + " but was " + actual);
    }

    @Nested
    @DisplayName("priceInNok()")
    class PriceInNok {

        @Test
        @DisplayName("Should convert the latest price to NOK")
        void convertsLatestPriceToNok() {
            // Arrange: latest = 126 USD -> 126 * 10 = 1260 NOK
            Stock stock = usdStock(100, 110, 105, 126);

            // Act
            BigDecimal result = service.priceInNok(stock, converter());

            // Assert
            assertBigDecimalEquals(BigDecimal.valueOf(1260), result);
        }

        @Test
        @DisplayName("Should throw NullPointerException when stock is null")
        void throwsNullPointerExceptionWhenStockIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> service.priceInNok(null, converter()));
        }

        @Test
        @DisplayName("Should throw NullPointerException when converter is null")
        void throwsNullPointerExceptionWhenConverterIsNull() {
            // Arrange
            Stock stock = usdStock(100);

            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> service.priceInNok(stock, null));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when the stock currency is unsupported")
        void throwsIllegalArgumentExceptionWhenCurrencyIsUnsupported() {
            // Arrange: the converter rejects EUR as a source currency
            Stock stock = stockWith(EUR, 100, 110);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> service.priceInNok(stock, converter()));
        }
    }

    @Nested
    @DisplayName("changeInNok()")
    class ChangeInNok {

        @Test
        @DisplayName("Should convert the latest weekly change to NOK")
        void convertsLatestChangeToNok() {
            // Arrange: change = 126 - 105 = 21 USD -> 21 * 10 = 210 NOK
            Stock stock = usdStock(100, 110, 105, 126);

            // Act
            BigDecimal result = service.changeInNok(stock, converter());

            // Assert
            assertBigDecimalEquals(BigDecimal.valueOf(210), result);
        }

        @Test
        @DisplayName("Should return zero when only one price exists")
        void returnsZeroWhenOnlyOnePriceExists() {
            // Arrange: a single price means no change yet
            Stock stock = usdStock(100);

            // Act
            BigDecimal result = service.changeInNok(stock, converter());

            // Assert
            assertBigDecimalEquals(BigDecimal.ZERO, result);
        }

        @Test
        @DisplayName("Should throw NullPointerException when stock is null")
        void throwsNullPointerExceptionWhenStockIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> service.changeInNok(null, converter()));
        }

        @Test
        @DisplayName("Should throw NullPointerException when converter is null")
        void throwsNullPointerExceptionWhenConverterIsNull() {
            // Arrange
            Stock stock = usdStock(100, 110);

            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> service.changeInNok(stock, null));
        }
    }

    @Nested
    @DisplayName("highLowRangeInNok()")
    class HighLowRangeInNok {

        @Test
        @DisplayName("Should convert the full-window high-low range to NOK")
        void convertsFullWindowRangeToNok() {
            // Arrange: over four weeks high = 126, low = 100 -> range = 26 -> 260 NOK
            Stock stock = usdStock(100, 110, 105, 126);

            // Act
            BigDecimal result = service.highLowRangeInNok(stock, converter(), 4);

            // Assert
            assertBigDecimalEquals(BigDecimal.valueOf(260), result);
        }

        @Test
        @DisplayName("Should consider only the most recent weeks of the window")
        void considersOnlyTheMostRecentWeeks() {
            // Arrange: last two weeks = [105, 126] -> range = 21 -> 210 NOK
            Stock stock = usdStock(100, 110, 105, 126);

            // Act
            BigDecimal result = service.highLowRangeInNok(stock, converter(), 2);

            // Assert
            assertBigDecimalEquals(BigDecimal.valueOf(210), result);
        }

        @Test
        @DisplayName("Should throw NullPointerException when stock is null")
        void throwsNullPointerExceptionWhenStockIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> service.highLowRangeInNok(null, converter(), 4));
        }

        @Test
        @DisplayName("Should throw NullPointerException when converter is null")
        void throwsNullPointerExceptionWhenConverterIsNull() {
            // Arrange
            Stock stock = usdStock(100, 110);

            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> service.highLowRangeInNok(stock, null, 4));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when weeks is zero")
        void throwsIllegalArgumentExceptionWhenWeeksIsZero() {
            // Arrange
            Stock stock = usdStock(100, 110);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> service.highLowRangeInNok(stock, converter(), 0));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when the stock currency is unsupported")
        void throwsIllegalArgumentExceptionWhenCurrencyIsUnsupported() {
            // Arrange: the converter rejects EUR as a source currency
            Stock stock = stockWith(EUR, 100, 110);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> service.highLowRangeInNok(stock, converter(), 2));
        }
    }
}
