package edu.ntnu.idatt2003.millions.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Stock} class
 * <p>
 *   This test class helps to verify core functionality is correctly constructed and
 *   that its methods return the expected values.
 * </p>
 * <p>
 *   These tests follow the AAA pattern and use descriptive names
 *   to document the expected behaviour.
 * </p>
 */
public class StockTest {

  /**
   * Verifies that the symbols is associated with the stock created.
   */
  @Test
  void getSymbol_ValidStockCreated_ShouldReturnCorrectSymbol() {
    //Arrange
    Stock stock = new Stock("BA", "The Boeing Company",
        new ArrayList<>(List.of(new BigDecimal("100.00"))));

    //Act
    String symbol = stock.getSymbol();

    //Assert
    assertEquals("BA", symbol);
  }

  /**
   * Verifies that the correct company is associated with the stock created.
   */
  @Test
  void getCompany_ValidStockCreated_ShouldReturnCorrectCompany() {
    //Arrange
    Stock stock = new Stock("DIS", "The Walt Disney Company",
        new ArrayList<>(List.of(new BigDecimal("100.00"))));

    //Act
    String company = stock.getCompany();

    //Assert
    assertEquals("The Walt Disney Company", company);
  }

  /**
   * Verifies that if it exists multiple prices that it returns the latest price created.
   */
  @Test
  void getPricesPrice_MultiplePricesExist_ShouldReturnLastPrice() {
    //Arrange
    Stock stock = new Stock("NKE", "Nike, Inc",
        new ArrayList<>(List.of(
            new BigDecimal("130.00"),
            new BigDecimal("150.00"))));
    //Act
    BigDecimal result = stock.getSalesPrice();
    //Assert
    assertEquals(new BigDecimal("150.00"), result);
  }

  /**
   * Verifies that the new sales price is updated and added in the price history.
   */
  @Test
  void addNewSalesPrice_NewPriceAdded_ShouldUpdateSalePrice() {
    //Arrange
    Stock stock = new Stock("NKE", "Nike, Inc",
        new ArrayList<>(List.of(new BigDecimal("130.00"))));
    //Act
    stock.addNewSalesPrice(new BigDecimal("110.00"));
    //Assert
    assertEquals(new BigDecimal("110.00"), stock.getSalesPrice());
  }
}
