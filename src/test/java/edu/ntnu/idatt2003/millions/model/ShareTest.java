package edu.ntnu.idatt2003.millions.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Share} class.
 * <p>
 *   This test class helps to verify core functionality is correctly
 *   constructed and that its methods return the expected values.
 *   returns correct
 * </p>
 * <p>
 *   These tests follow the AAA pattern and
 *   use descriptive method names to document the expected behavior.
 * </p>
 */
public class ShareTest {

  /**
   * Helper method to avoid repetitive stocks created to test {@link Share} class methods.
   *
   * @return Stock created
   */
  private Stock createStock() {
    return new Stock("NKE", "Nike, Inc",
        new ArrayList<>(List.of(new BigDecimal("120.00")))
    );
  }

  /**
   * Verifies that the stock is associated with the share created.
   */
  @Test
  void shouldReturnCorrectStockWhenValidShareCreated() {
    //Arrange
    Stock stock = new Stock("DIS", "The Walt Disney Company",
        new ArrayList<>(List.of(new BigDecimal("110.00"))));
    Share share = new Share(stock, new BigDecimal("20"), new BigDecimal("35.00"));

    //Act
    Stock results= share.getStock();

    //Assert
    assertEquals(stock, results);
  }

  /**
   * Verifies that the share returns correct quantity with the share created
   * with the helper method.
   */
  @Test
  void shouldReturnCorrectQuantityWhenValidShareCreated() {
    //Arrange
    Share share = new Share(createStock(), new BigDecimal("10"), new BigDecimal("40.00"));

    //Act
    BigDecimal quantity = share.getQuantity();

    //Assert
    assertEquals(new BigDecimal("10"), quantity);
  }

  /**
   * Verifies that the share returns correct purchase price when share
   * is created with the helper method.
   */
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
