// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.service;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.player.PlayerStatusLevel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Stateless read service for player net-worth and status queries.
 */
public class PlayerStatsService {

    /**
     * Returns the player's current net worth in NOK.
     *
     * @param player    the active player
     * @param converter the currency converter used to value portfolio positions
     * @return net worth in NOK
     */
    public BigDecimal getNetWorth(Player player, CurrencyConverter converter) {
        return player.getNetWorth(converter);
    }

    /**
     * Returns the absolute change in net worth since the start of the game, in NOK.
     *
     * @param player    the active player
     * @param converter the currency converter used to value portfolio positions
     * @return net worth change in NOK; negative if the player has lost money
     */
    public BigDecimal getNetWorthChangeSinceStart(Player player, CurrencyConverter converter) {
        return player.getNetWorthChangeSinceStart(converter);
    }

    /**
     * Returns the percentage change in net worth since the start of the game.
     *
     * @param player    the active player
     * @param converter the currency converter used to value portfolio positions
     * @return percentage change; negative if the player has lost money
     */
    public BigDecimal getNetWorthChangePercentSinceStart(Player player, CurrencyConverter converter) {
        return player.getNetWorthChangePercentSinceStart(converter);
    }

    /**
     * Returns the absolute change in net worth since the previous week, in NOK.
     *
     * @param player    the active player
     * @param converter the currency converter used to value portfolio positions
     * @return net worth change in NOK; null if no week has been advanced yet
     */
    public BigDecimal getWeeklyNetWorthChange(Player player, CurrencyConverter converter) {
        return player.getWeeklyNetWorthChange(converter);
    }

    /**
     * Returns the percentage change in net worth since the previous week.
     *
     * @param player    the active player
     * @param converter the currency converter used to value portfolio positions
     * @return percentage change; null if no week has been advanced yet
     */
    public BigDecimal getWeeklyNetWorthChangePercent(Player player, CurrencyConverter converter) {
        return player.getWeeklyNetWorthChangePercent(converter);
    }

    /**
     * Returns the player's current status level.
     *
     * @param player    the active player
     * @param converter the currency converter used to compute net worth
     * @return the current {@link PlayerStatusLevel}
     */
    public PlayerStatusLevel getStatus(Player player, CurrencyConverter converter) {
        return player.getStatus(converter);
    }

    /**
     * Returns the player's progress toward the next status level as a value
     * between 0.0 and 1.0 (inclusive).
     *
     * <p>Progress is additive: two independent part-counts are summed and divided
     * by the total parts for the current band.
     *
     * <p><b>NOVICE → INVESTOR (20 parts):</b>
     * <pre>
     *   weeksPart  = min(10, weeksTraded)
     *   growthPart = min(10, max(0, floor(growthPercent / 2)))
     *   progress   = (weeksPart + growthPart) / 20
     * </pre>
     *
     * <p><b>INVESTOR → SPECULATOR (50 parts):</b>
     * <pre>
     *   weeksPart  = min(10, max(0, weeksTraded − 10))
     *   growthPart = min(40, max(0, floor((growthPercent − 20) / 2)))
     *   progress   = (weeksPart + growthPart) / 50
     * </pre>
     *
     * <p>For {@link PlayerStatusLevel#SPECULATOR}, always returns 1.0.
     * Negative growth contributes zero to the growth part (clamped via {@code max(0, …)}).
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

        if (status == PlayerStatusLevel.NOVICE) {
            int weeksPart  = Math.min(10, weeksTraded);
            int growthPart = Math.clamp((long) Math.floor(growthPercent / 2), 0, 10);
            return (weeksPart + growthPart) / 20.0;
        } else {
            int weeksPart = Math.clamp((long) weeksTraded - 10, 0, 10);
            int growthPart = Math.clamp((long) Math.floor((growthPercent - 20) / 2), 0, 40);
            return (weeksPart + growthPart) / 50.0;
        }
    }

    /**
     * Returns the player's recorded net-worth history — one entry per game week,
     * in chronological order from week 1 to the current week. The returned list
     * is a defensive copy and does not reflect later mutations of the player.
     *
     * @param player the active player
     * @return immutable snapshot of the net-worth history; never null
     * @throws NullPointerException if player is null
     */
    public List<BigDecimal> getNetWorthHistory(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return List.copyOf(player.getNetWorthHistory());
    }
}
