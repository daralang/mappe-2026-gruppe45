package edu.ntnu.idatt2003.millions.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ShareTest {
  private Stock createStock() {
    return new Stock("NKE", "Nike, Inc",
        new ArrayList<>(List.of(new BigDecimal("120.00")))
    );
  }

  @Test
  void shouldReturnCorrectStockWhenValidShareCreated() {
    //Arrange
    Stock stock = new Stock("DIS", "The Walt Disney Company",
        new ArrayList<>(List.of(new BigDecimal("110.00"))));
    Share share = new Share(stock, new BigDecimal("20"), new BigDecimal("35.00"));

    //Act
    BigDecimal quantity = share.getQuantity();

    //Assert
    assertEquals(new BigDecimal("20"), quantity);
  }

  @Test
  void shouldReturnCorrectQuantityWhenValidShareCreated() {
    //Arrange
    Share share = new Share(createStock(), new BigDecimal("10"), new BigDecimal("40.00"));

    //Act
    BigDecimal quantity = share.getQuantity();

    //Assert
    assertEquals(new BigDecimal("10"), quantity);
  }

  @Test
  void shouldReturnCorrectPurchasePriceWhenValidShareCreated() {
    //Arrange
    Share share = new Share(createStock(), new BigDecimal("10"), new BigDecimal("90.00"));

    //Act
    BigDecimal price = share.getPurchasePrice();

    //Assert
    assertEquals(new BigDecimal("90.00"), price);
  }
}
