package edu.ntnu.idatt2003.millions.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Portfolio} class.
 * <p>
 *   This test class verifies the behaviour of the Portfolio model, including adding, removing,
 *   retrieving and filtering shares.
 * </p>
 * <p>
 *   All tests follow the AAA pattern.
 * </p>
 */
public class PortfolioTest {

//HELPER METHODS
  /**
   * Creates a Stock instance with the given symbol.
   * <p>
   *   The company name is automatically assigned based on the symbol to simplify test setup.
   * </p>
   *
   * @param symbol the stock symbol
   * @return a new Stock instance
   */
  private Stock createStock(String symbol) {
    return new Stock(symbol, symbol.equals("DIS") ? "The Walt Disney Company" : "NIKE, Inc",
        new ArrayList<>(List.of(new BigDecimal("100"))));
  }

  /**
   * Creates a Share instance using the default symbol "DIS"
   *
   * @return a share associated with the default stock.
   */
  private Share createShare() {
    return createShare("DIS");
  }

  /**
   * Creates a Share instance with a specific stock symbol.
   *
   * @param symbol the stock symbol
   * @return a Share associated with the given symbol.
   */
  private Share createShare(String symbol) {
    return new Share(createStock(symbol), new BigDecimal("10"), new BigDecimal("50"));
  }


  //ADD SHARES TO PORTFOLIO TEST
  /**
   * Verifies if new added share returns true if the share is not added before.
   */
  @Test
    public void shouldReturnTrueWhenNewShareAddedDontExist_addShare() {
      //Arrange
      Portfolio portfolio = new Portfolio();
      Share share = createShare();
      //Act
      boolean actual = portfolio.addShare(share);
      //Assert
      assertTrue(actual);
      assertTrue(portfolio.contains(share));
  }

  /**
   * Verifies if the new added share returns false if the share already exists in the portfolio.
   */
  @Test
  public void shouldReturnFalseWhenNewShareAlreadyExists_addShare() {
    //Arrange
    Portfolio portfolio = new Portfolio();
    Share share = createShare();
    portfolio.addShare(share);    //Adds the same share twice.

    //Act
    boolean actual = portfolio.addShare(share);

    //Assert
    assertFalse(actual);
    assertTrue(portfolio.contains(share));
  }

  //REMOVE SHARES FROM PORTFOLIO TEST
  /**
   * Verifies if the condition returns true if the share is in the list and
   * is removed successfully
   */
  @Test
  public void shouldReturnTrueWhenShareExistsRemoved_removeShare() {
    //Arrange
    Portfolio portfolio = new Portfolio();
    Share share = createShare();
    portfolio.addShare(share);

    //Act
    boolean actual = portfolio.removeShare(share);

    //Assert
    assertTrue(actual);
    assertFalse(portfolio.contains(share));
  }

  /**
   * Verifies if the method returns false if the share is not in the list and method is used.
   */
  @Test
  public void shouldReturnFalseWhenShareDoesNotExist_removeShare() {
    //Arrange
    Portfolio portfolio = new Portfolio();
    Share share = createShare();

    //Act
    boolean actual = portfolio.removeShare(share);

    //Assert
    assertFalse(actual);
  }

  //RETURN SHARES
  /**
   * Verifies that the method returns a copy of the list.
   */
  @Test
  public void shouldReturnCopyListOfShares_getShares() {
    //Arrange
    Portfolio portfolio = new Portfolio();
    Share share = createShare();
    portfolio.addShare(share);

    //Act
    List<Share> shares = portfolio.getShares();
    shares.clear();

    //Assert
    assertEquals(1,portfolio.getShares().size());

  }

  /**
   * Verifies that the method returns only shares whose stock symbol matches the given symbol.
   * The test adds shares with different stock symbols and ensures that only the matching shares
   * are returned.
   */
  @Test
  public void shouldReturnOnlyMatchingSymbols_getShares() {
    //Arrange
    Portfolio portfolio = new Portfolio();

    Share dis = createShare("DIS");
    Share nike = createShare("NKE");

    portfolio.addShare(dis);
    portfolio.addShare(nike);

    //Act
    List<Share> result = portfolio.getShares("DIS");

    //Assert
    assertEquals(1,result.size());
    assertTrue(result.contains(dis));
    assertFalse(result.contains(nike));
  }
}
