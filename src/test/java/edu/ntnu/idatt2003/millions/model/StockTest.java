package edu.ntnu.idatt2003.millions.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class StockTest {
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
