package edu.ntnu.idatt2003.millions.model;

import java.math.BigDecimal;
import javax.sound.sampled.Port;

public class Player {
  private final String name;
  private final BigDecimal startingMoney;
  private BigDecimal money;

  private final Portfolio portfolio;
  private final TransactionArchive transactionArchive;

  public Player(String name, BigDecimal startingMoney) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Player name cannot be null or blank");
    }
    if (startingMoney == null || startingMoney.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Start money cannot be negative");
    }

    this.name = name;
    this.startingMoney = startingMoney;
    this.money = startingMoney;

    this.portfolio = new Portfolio();
    this.transactionArchive = new TransactionArchive();
  }

  public String getName() {
    return name;
  }
  public BigDecimal getStartingMoney() {
    return startingMoney;
  }

  public BigDecimal getMoney() {
    return money;
  }

  public Portfolio getPortfolio() {
    return portfolio;
  }

  public TransactionArchive getTransactionArchive() {
    return transactionArchive;
  }

  public void addMoney(BigDecimal amount) {
    this.money = this.money.add(amount);
  }

  public void withdrawMoney(BigDecimal amount) {
    if (this.money.compareTo(amount) < 0) {
      throw new IllegalArgumentException("Cannot withdraw money less than zero");
    }
    this.money = this.money.subtract(amount);
  }
}
