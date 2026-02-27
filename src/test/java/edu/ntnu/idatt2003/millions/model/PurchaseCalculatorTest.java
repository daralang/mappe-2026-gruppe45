package edu.ntnu.idatt2003.millions.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseCalculatorTest {

  private Stock createStockWithSalesPrice(BigDecimal salesPrice) {
    return new Stock("DCL", "Dara Inc",
        new ArrayList<>(List.of(salesPrice)));
  }

  private Share createShare(BigDecimal quantity, BigDecimal purchasePrice, BigDecimal salesPrice) {
    return new Share(createStockWithSalesPrice(salesPrice), quantity, purchasePrice);
  }

  private static void assertBigDecimalEquals(BigDecimal expected, BigDecimal calculated) {
    assertEquals(0, expected.compareTo(calculated),
        "Expected " + expected.toPlainString() + " to be equal to " + calculated.toPlainString());
  }

  @Test
  void shouldReturnSalesPriceTimesQuantityWhenValidShareGiven() {
    //Arrange
    Share share = createShare(new BigDecimal("20"), new BigDecimal("30"), new BigDecimal("70"));
    PurchaseCalculator calculator = new PurchaseCalculator(share);
    BigDecimal expected = new BigDecimal("30").multiply(new BigDecimal("20"));

    //Act
    BigDecimal calculated = calculator.calculateGross();

    //Assert
    assertBigDecimalEquals(expected, calculated);
  }

  @Test
  void shouldReturnPointFivePercentOfGrossWhenValidShareGiven() {
    //Arrange
    Share share = createShare(new BigDecimal("10"), new BigDecimal("30"), new BigDecimal("70"));
    PurchaseCalculator calculator = new PurchaseCalculator(share);

    BigDecimal gross = new BigDecimal("30").multiply(new BigDecimal("10"));
    BigDecimal expected = gross.multiply(new BigDecimal("0.005"));

    //Act
    BigDecimal calculated = calculator.calculateCommission();

    //Arrange
    assertBigDecimalEquals(expected, calculated);
  }

  @Test
  void shouldReturnZeroWhenAnyShareGiven() {
    //Arrange
    Share share = createShare(new BigDecimal("10"), new BigDecimal("30"), new BigDecimal("70"));
    PurchaseCalculator calculator = new PurchaseCalculator(share);

    //Act
    BigDecimal expected = calculator.calculateTax();

    //Assert
    assertBigDecimalEquals(BigDecimal.ZERO, expected);
  }

  @Test
  void shouldReturnGrossCommissionTaxWhenValidShareGiven() {
    //Arrange
    Share share = createShare(new BigDecimal("10"), new BigDecimal("30"), new BigDecimal("70"));
    PurchaseCalculator calculator = new PurchaseCalculator(share);

    BigDecimal gross = new BigDecimal("30").multiply(new BigDecimal("10"));
    BigDecimal commission = gross.multiply(new BigDecimal("0.005"));
    BigDecimal expected = gross.add(commission);

    //Act
    BigDecimal calculated = calculator.calculateTotal();

    //Arrange
    assertBigDecimalEquals(expected, calculated);
  }
}
