package edu.ntnu.idatt2003.millions.model.player;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.currency.FixedRateCurrencyConverter;
import edu.ntnu.idatt2003.millions.model.player.Portfolio;
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

/**
 * Unit tests for the {@link Portfolio} class.
 * <p>
 * This test class verifies the behaviour of the Portfolio model, including adding, removing,
 * retrieving and filtering shares, as well as net worth calculation with currency conversion.
 * </p>
 * <p>
 * All tests follow the AAA pattern.
 * </p>
 */
class PortfolioTest {
    private Portfolio portfolio;
    private Share share;
    private CurrencyConverter converter;

    private static final Currency NOK = Currency.getInstance("NOK");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");

    @BeforeEach
    void setUp() {
        // Arrange
        portfolio = new Portfolio();
        share = new Share(
                new Stock("DIS", "The Walt Disney Company",
                        new ArrayList<>(List.of(new BigDecimal("100"))), NOK),
                new BigDecimal("10"), new BigDecimal("50"));
        converter = new FixedRateCurrencyConverter();
    }

    @Nested
    @DisplayName("addShare()")
    class AddShare {

        @Test
        @DisplayName("Should return true when share does not already exist in portfolio")
        void returnsTrueWhenShareIsNew() {
            // Act
            boolean actual = portfolio.addShare(share);
            // Assert
            assertTrue(actual);
            assertTrue(portfolio.contains(share));
        }

        @Test
        @DisplayName("Should return false when share already exists in portfolio")
        void returnsFalseWhenShareAlreadyExists() {
            // Arrange
            portfolio.addShare(share);
            // Act
            boolean actual = portfolio.addShare(share);
            // Assert
            assertFalse(actual);
        }

        @Test
        @DisplayName("Should throw exception when share is null")
        void throwsExceptionWhenShareIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    portfolio.addShare(null));
        }
    }

    @Nested
    @DisplayName("removeShare()")
    class RemoveShare {

        @Test
        @DisplayName("Should return true when share exists in portfolio and is successfully removed")
        void returnsTrueWhenShareIsRemoved() {
            // Arrange
            portfolio.addShare(share);
            // Act
            boolean actual = portfolio.removeShare(share);
            // Assert
            assertTrue(actual);
            assertFalse(portfolio.contains(share));
        }

        @Test
        @DisplayName("Should return false when share does not exist in portfolio")
        void returnsFalseWhenShareDoesNotExist() {
            // Act
            boolean actual = portfolio.removeShare(share);
            // Assert
            assertFalse(actual);
        }

        @Test
        @DisplayName("Should throw exception when share is null")
        void throwsExceptionWhenShareIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    portfolio.removeShare(null));
        }
    }

    @Nested
    @DisplayName("getShares()")
    class GetShares {

        @Test
        @DisplayName("Should return a copy of the share list")
        void returnsCopyOfShareList() {
            // Arrange
            portfolio.addShare(share);
            // Act
            List<Share> shares = portfolio.getShares();
            shares.clear();
            // Assert
            assertEquals(1, portfolio.getShares().size());
        }

        @Test
        @DisplayName("Should only return shares matching the given symbol")
        void returnsOnlyMatchingSymbols() {
            // Arrange
            Share nike = new Share(
                    new Stock("NKE", "NIKE, Inc",
                            new ArrayList<>(List.of(new BigDecimal("100"))), NOK),
                    new BigDecimal("10"), new BigDecimal("50"));
            portfolio.addShare(share);
            portfolio.addShare(nike);
            // Act
            List<Share> result = portfolio.getShares("DIS");
            // Assert
            assertEquals(1, result.size());
            assertTrue(result.contains(share));
            assertFalse(result.contains(nike));
        }

        @Test
        @DisplayName("Should return empty list when portfolio is empty")
        void returnsEmptyListWhenPortfolioIsEmpty() {
            // Act & Assert
            assertTrue(portfolio.getShares().isEmpty());
        }

        @Test
        @DisplayName("Should return empty list when no shares match the given symbol")
        void returnsEmptyListWhenNoSymbolMatches() {
            // Arrange
            portfolio.addShare(share);
            // Act
            List<Share> result = portfolio.getShares("NKE");
            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return empty list when symbol is null")
        void returnsEmptyListWhenSymbolIsNull() {
            // Act
            List<Share> result = portfolio.getShares(null);
            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("setShares()")
    class SetShares {

        @Test
        @DisplayName("Should replace all shares when valid list is provided")
        void replacesAllSharesWhenValidListIsProvided() {
            // Arrange
            Share share2 = new Share(share.getStock(), new BigDecimal("3"), new BigDecimal("200"));
            portfolio.addShare(share);

            // Act
            portfolio.setShares(List.of(share2));

            // Assert
            assertEquals(List.of(share2), portfolio.getShares());
        }

        @Test
        @DisplayName("Should clear portfolio when empty list is provided")
        void clearsPortfolioWhenEmptyListIsProvided() {
            // Arrange
            portfolio.addShare(share);

            // Act
            portfolio.setShares(List.of());

            // Assert
            assertTrue(portfolio.getShares().isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when list is null")
        void throwsExceptionWhenListIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    portfolio.setShares(null));
        }

        @Test
        @DisplayName("Should throw exception when list contains null share")
        void throwsExceptionWhenListContainsNull() {
            // Arrange
            List<Share> sharesWithNull = new ArrayList<>();
            sharesWithNull.add(null);

            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    portfolio.setShares(sharesWithNull));
        }
    }

    @Nested
    @DisplayName("contains()")
    class Contains {

        @Test
        @DisplayName("Should return true when share exists in portfolio")
        void returnsTrueWhenShareExists() {
            // Arrange
            portfolio.addShare(share);
            // Act & Assert
            assertTrue(portfolio.contains(share));
        }

        @Test
        @DisplayName("Should return false when share does not exist in portfolio")
        void returnsFalseWhenShareDoesNotExist() {
            // Act & Assert
            assertFalse(portfolio.contains(share));
        }

        @Test
        @DisplayName("Should throw exception when share is null")
        void throwsExceptionWhenShareIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    portfolio.contains(null));
        }
    }

    @Nested
    @DisplayName("addShare merge logic")
    class AddShareMergeLogic {

        @Test
        @DisplayName("Should return true when adding a second different share object with the same stock symbol")
        void returnsTrueWhenAddingSecondShareWithSameSymbol() {
            // Arrange
            portfolio.addShare(share);
            Share second = new Share(share.getStock(), new BigDecimal("5"), new BigDecimal("80"));
            // Act
            boolean result = portfolio.addShare(second);
            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Should keep only one share per stock symbol after two purchases")
        void portfolioHasOneShareAfterTwoPurchasesOfSameStock() {
            // Arrange
            portfolio.addShare(share);
            Share second = new Share(share.getStock(), new BigDecimal("5"), new BigDecimal("80"));
            // Act
            portfolio.addShare(second);
            // Assert
            assertEquals(1, portfolio.getShares().size());
        }

        @Test
        @DisplayName("Should sum quantities when merging two shares of the same stock")
        void mergedShareHasCorrectQuantityAfterTwoPurchases() {
            // Arrange
            portfolio.addShare(share);
            Share second = new Share(share.getStock(), new BigDecimal("5"), new BigDecimal("80"));
            portfolio.addShare(second);
            // Act
            Share merged = portfolio.getShares().getFirst();
            // Assert
            assertEquals(0, new BigDecimal("15").compareTo(merged.getQuantity()));
        }

        @Test
        @DisplayName("Should compute weighted-average purchase price (GAV) when merging two shares")
        void mergedShareHasCorrectGavAfterTwoPurchases() {
            // Arrange: share = qty 10 @ 50, second = qty 5 @ 80
            // GAV = (10×50 + 5×80) / (10+5) = 900 / 15 = 60.0000
            portfolio.addShare(share);
            Share second = new Share(share.getStock(), new BigDecimal("5"), new BigDecimal("80"));
            portfolio.addShare(second);
            // Act
            Share merged = portfolio.getShares().getFirst();
            // Assert
            assertEquals(0, new BigDecimal("60.0000").compareTo(merged.getPurchasePrice()));
        }

        @Test
        @DisplayName("Should keep only one share per symbol after three purchases of the same stock")
        void portfolioHasOneShareAfterThreePurchasesOfSameStock() {
            // Arrange
            Share second = new Share(share.getStock(), new BigDecimal("5"), new BigDecimal("80"));
            Share third  = new Share(share.getStock(), new BigDecimal("5"), new BigDecimal("60"));
            // Act
            portfolio.addShare(share);
            portfolio.addShare(second);
            portfolio.addShare(third);
            // Assert
            assertEquals(1, portfolio.getShares().size());
        }

        @Test
        @DisplayName("Should not merge shares of different stock symbols")
        void doesNotMergeSharesOfDifferentSymbols() {
            // Arrange
            Share nike = new Share(
                    new Stock("NKE", "NIKE, Inc",
                            new ArrayList<>(List.of(new BigDecimal("100"))), NOK),
                    new BigDecimal("5"), new BigDecimal("80"));
            // Act
            portfolio.addShare(share);   // DIS
            portfolio.addShare(nike);    // NKE
            // Assert
            assertEquals(2, portfolio.getShares().size());
        }
    }

    @Nested
    @DisplayName("getNetWorth()")
    class GetNetWorth {

        @Test
        @DisplayName("Should return zero when portfolio is empty")
        void returnsZeroWhenPortfolioIsEmpty() {
            // Act & Assert
            assertEquals(0, BigDecimal.ZERO.compareTo(portfolio.getNetWorth(converter)));
        }

        @Test
        @DisplayName("Should return market value (salesPrice × quantity) of a single NOK share")
        void returnsTotalSalesValueOfSingleNokShare() {
            // Arrange
            portfolio.addShare(share);
            BigDecimal expected = share.getCurrentValue(); // 100 × 10 = 1000 NOK
            // Act & Assert
            assertEquals(0, expected.compareTo(portfolio.getNetWorth(converter)));
        }

        @Test
        @DisplayName("Should sum market values of multiple NOK shares in portfolio")
        void sumsValueOfMultipleNokShares() {
            // Arrange
            Share share2 = new Share(
                    new Stock("DCL", "Dara, Inc",
                            new ArrayList<>(List.of(new BigDecimal("50.00"))), NOK),
                    new BigDecimal("10"), new BigDecimal("50.00"));
            portfolio.addShare(share);
            portfolio.addShare(share2);
            BigDecimal expected = share.getCurrentValue()    // 100 × 10 = 1000
                    .add(share2.getCurrentValue());           // 50 × 10  =  500
            // Act & Assert
            assertEquals(0, expected.compareTo(portfolio.getNetWorth(converter)));
        }

        @Test
        @DisplayName("Should return zero when share has zero quantity")
        void returnsZeroWhenShareHasZeroQuantity() {
            // Arrange
            Share zeroShare = new Share(
                    new Stock("DCL", "Dara, Inc",
                            new ArrayList<>(List.of(new BigDecimal("100.00"))), NOK),
                    new BigDecimal("0"), new BigDecimal("50.00"));
            portfolio.addShare(zeroShare);
            // Act & Assert
            assertEquals(0, BigDecimal.ZERO.compareTo(portfolio.getNetWorth(converter)));
        }

        @Test
        @DisplayName("Should return correct net worth after share is removed")
        void returnsCorrectNetWorthAfterShareIsRemoved() {
            // Arrange
            portfolio.addShare(share);
            portfolio.removeShare(share);
            // Act & Assert
            assertEquals(0, BigDecimal.ZERO.compareTo(portfolio.getNetWorth(converter)));
        }

        @Test
        @DisplayName("Should equal market value (salesPrice × quantity) for NOK shares — no fees deducted")
        void returnsGrossMarketValueForNokShares() {
            // Arrange
            portfolio.addShare(share);
            BigDecimal expected = share.getCurrentValue(); // 100 × 10 = 1000 NOK
            // Act
            BigDecimal result = portfolio.getNetWorth(converter);
            // Assert
            assertEquals(0, expected.compareTo(result));
        }

        @Test
        @DisplayName("Should convert USD share market value to NOK using the converter")
        void convertsUsdShareValueToNok() {
            // Arrange
            Share usdShare = new Share(
                    new Stock("AAPL", "Apple Inc",
                            new ArrayList<>(List.of(new BigDecimal("100.00"))), USD),
                    new BigDecimal("10"), new BigDecimal("50.00"));
            portfolio.addShare(usdShare);
            BigDecimal expected = converter.convert(usdShare.getCurrentValue(), USD, NOK);
            // Act
            BigDecimal actual = portfolio.getNetWorth(converter);
            // Assert
            assertEquals(0, expected.compareTo(actual));
        }

        @Test
        @DisplayName("Should sum converted market values when shares have different currencies")
        void sumsConvertedValuesAcrossCurrencies() {
            // Arrange
            Share usdShare = new Share(
                    new Stock("AAPL", "Apple Inc",
                            new ArrayList<>(List.of(new BigDecimal("100.00"))), USD),
                    new BigDecimal("5"), new BigDecimal("50.00"));
            Share eurShare = new Share(
                    new Stock("SAP", "SAP SE",
                            new ArrayList<>(List.of(new BigDecimal("200.00"))), EUR),
                    new BigDecimal("3"), new BigDecimal("100.00"));
            portfolio.addShare(usdShare);
            portfolio.addShare(eurShare);

            BigDecimal usdInNok = converter.convert(usdShare.getCurrentValue(), USD, NOK);
            BigDecimal eurInNok = converter.convert(eurShare.getCurrentValue(), EUR, NOK);
            BigDecimal expected = usdInNok.add(eurInNok);
            // Act
            BigDecimal actual = portfolio.getNetWorth(converter);
            // Assert
            assertEquals(0, expected.compareTo(actual));
        }

        @Test
        @DisplayName("Should return market value directly when all shares are already in NOK")
        void returnsIdenticalValueForNokShares() {
            // Arrange
            portfolio.addShare(share);
            BigDecimal expected = share.getCurrentValue(); // 100 × 10 = 1000 NOK
            // Act
            BigDecimal actual = portfolio.getNetWorth(converter);
            // Assert
            assertEquals(0, expected.compareTo(actual));
        }

        @Test
        @DisplayName("Should throw NullPointerException when converter is null")
        void throwsExceptionWhenConverterIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    portfolio.getNetWorth(null));
        }

        @Test
        @DisplayName("Should throw NullPointerException when converter is null and portfolio is empty")
        void throwsExceptionWhenConverterIsNullAndPortfolioEmpty() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    portfolio.getNetWorth(null));
        }
    }
}
