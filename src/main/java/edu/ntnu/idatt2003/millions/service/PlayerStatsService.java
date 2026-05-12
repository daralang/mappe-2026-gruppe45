package edu.ntnu.idatt2003.millions.service;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.player.PlayerStatusLevel;

import java.math.BigDecimal;

/**
 * Stateless read service for player net-worth and status queries.
 * All methods accept {@link Player} and {@link CurrencyConverter} as parameters
 * so this service holds no state and can be shared freely.
 */
public class PlayerStatsService {

    /** Returns the player's current net worth in NOK. */
    public BigDecimal getNetWorth(Player player, CurrencyConverter converter) {
        return player.getNetWorth(converter);
    }

    /** Returns the absolute change in net worth since the start of the game, in NOK. */
    public BigDecimal getNetWorthChangeSinceStart(Player player, CurrencyConverter converter) {
        return player.getNetWorthChangeSinceStart(converter);
    }

    /** Returns the percentage change in net worth since the start of the game. */
    public BigDecimal getNetWorthChangePercentSinceStart(Player player, CurrencyConverter converter) {
        return player.getNetWorthChangePercentSinceStart(converter);
    }

    /**
     * Returns the absolute change in net worth since the previous week, in NOK.
     * Returns null if no week has been advanced yet.
     */
    public BigDecimal getWeeklyNetWorthChange(Player player, CurrencyConverter converter) {
        return player.getWeeklyNetWorthChange(converter);
    }

    /**
     * Returns the percentage change in net worth since the previous week.
     * Returns null if no week has been advanced yet.
     */
    public BigDecimal getWeeklyNetWorthChangePercent(Player player, CurrencyConverter converter) {
        return player.getWeeklyNetWorthChangePercent(converter);
    }

    /** Returns the player's current status level. */
    public PlayerStatusLevel getStatus(Player player, CurrencyConverter converter) {
        return player.getStatus(converter);
    }

    /**
     * Returns the player's progress toward the next status level as a value
     * between 0.0 and 1.0 (inclusive).
     *
     * <p>Progress is computed as the average of two sub-scores, each capped at 1.0:
     * <ul>
     *   <li>weeks-traded score: {@code weeksTraded / weeksThreshold}</li>
     *   <li>growth score: {@code growthPercent / growthThreshold}</li>
     * </ul>
     *
     * <p>For {@link PlayerStatusLevel#NOVICE}, thresholds are 10 weeks and 20% growth.
     * For {@link PlayerStatusLevel#INVESTOR}, thresholds are 20 weeks and 100% growth.
     * For {@link PlayerStatusLevel#SPECULATOR}, always returns 1.0 (top level reached).
     *
     * @param player    the player to evaluate
     * @param converter the currency converter used to compute the current net worth
     * @return progress toward the next status, in {@code [0.0, 1.0]}
     */
    public double getProgressToNextStatus(Player player, CurrencyConverter converter) {
        PlayerStatusLevel status = getStatus(player, converter);
        if (status == PlayerStatusLevel.SPECULATOR) return 1.0;

        int weeksTraded = player.getTransactionArchive().countDistinctWeeks();
        double growthPercent = player.getNetWorthChangePercentSinceStart(converter).doubleValue();

        double weeksThreshold = (status == PlayerStatusLevel.NOVICE) ? 10.0 : 20.0;
        double growthThreshold = (status == PlayerStatusLevel.NOVICE) ? 20.0 : 100.0;

        double weeksScore = Math.min(1.0, weeksTraded / weeksThreshold);
        double growthScore = Math.min(1.0, Math.max(0.0, growthPercent / growthThreshold));

        return (weeksScore + growthScore) / 2.0;
    }
}
