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
}
