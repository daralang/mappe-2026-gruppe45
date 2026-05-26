// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.model.transaction;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Share;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Calculator for the financial components of a stock sale transaction.
 *
 * <p>Calculates gross value, commission, tax, and total net payout based on
 * the sales price, quantity, and purchase costs of a given {@link Share}.</p>
 */
public class SalesCalculator implements TransactionCalculator {
    private final BigDecimal salesPrice;
    private final BigDecimal quantity;
    private final BigDecimal purchaseCosts;

    /**
     * Commission rate applied to the gross value of the sale (1%).
     */
    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.01");

    /**
     * Tax rate applied to the profit of the sale (30%).
     */
    private static final BigDecimal TAX_RATE = new BigDecimal("0.3");

    /**
     * Constructs a SalesCalculator for the given share.
     *
     * @param share the share being sold
     * @throws NullPointerException if the share is null
     */
    public SalesCalculator(Share share) {
        Objects.requireNonNull(share, "Share cannot be null");
        this.salesPrice = share.getStock().getSalesPrice();
        this.quantity = share.getQuantity();
        PurchaseCalculator purchaseCalculator = new PurchaseCalculator(share);
        this.purchaseCosts = purchaseCalculator.calculateTotal();
    }

    /**
     * Calculates the realized profit (or loss) from this sale, after commission and tax.
     *
     * @return the realized profit (positive) or loss (negative)
     */
    public BigDecimal calculateProfit() {
        return calculateTotal().subtract(purchaseCosts);
    }

    /**
     * Calculates the realized profit (or loss) as a percentage of the
     * original purchase costs.
     *
     * @return the profit percent, or zero if the original cost was zero
     */
    public BigDecimal calculateProfitPercent() {
        if (purchaseCosts.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return calculateProfit()
                .multiply(BigDecimal.valueOf(100))
                .divide(purchaseCosts, 1, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the gross value of the sale.
     * Gross value is defined as sales price multiplied by quantity.
     *
     * @return the gross sale value
     */
    @Override
    public BigDecimal calculateGross() {
        return this.salesPrice.multiply(this.quantity);
    }

    /**
     * Calculates the commission fee for the sale.
     * Commission is 1% of the gross value.
     *
     * @return the commission amount
     */
    @Override
    public BigDecimal calculateCommission() {
        return this.calculateGross().multiply(COMMISSION_RATE);
    }

    /**
     * Calculates the tax for the sale.
     * Tax is 30% of the pre-tax profit. Returns zero if the position was sold at a loss.
     *
     * @return the tax amount, or zero if there is no profit
     */
    @Override
    public BigDecimal calculateTax() {
        BigDecimal profit = this.calculateGross().subtract(this.calculateCommission()).subtract(this.purchaseCosts);
        if (profit.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return profit.multiply(TAX_RATE);
    }

    /**
     * Calculates the total net payout of the sale.
     *
     * @return the net payout to the seller
     */
    @Override
    public BigDecimal calculateTotal() {
        return this.calculateGross().subtract(this.calculateCommission()).subtract(this.calculateTax());
    }

    /**
     * Calculates the net payout of selling the entire position and converts
     * the result to NOK using the given currency converter.
     *
     * @param share     the share being sold
     * @param converter the converter used to translate the native-currency net to NOK
     * @return the net sale value in NOK
     * @throws NullPointerException if share or converter is null
     */
    public static BigDecimal calculateNetNok(Share share, CurrencyConverter converter) {
        Objects.requireNonNull(share, "Share cannot be null");
        Objects.requireNonNull(converter, "Converter cannot be null");
        BigDecimal netNative = new SalesCalculator(share).calculateTotal();
        return converter.convert(netNative, share.getStock().getCurrency(), Currency.getInstance("NOK"));
    }
}