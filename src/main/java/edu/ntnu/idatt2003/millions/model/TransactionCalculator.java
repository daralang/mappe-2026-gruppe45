package edu.ntnu.idatt2003.millions.model;

import java.math.BigDecimal;

public interface TransactionCalculator {
    BigDecimal calculateGross();
    BigDecimal calculateCommission();
    BigDecimal calculateTax();
    BigDecimal calculateTotal();
}
