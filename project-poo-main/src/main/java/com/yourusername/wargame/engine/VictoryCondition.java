package com.yourusername.wargame.engine;

import java.io.Serializable;

/**
 * Interface for checking victory conditions in the game.
 */
@FunctionalInterface
public interface VictoryCondition extends Serializable {
    /**
     * Checks if this victory condition has been met for a specific player.
     * @param gameState The current state of the game.
     * @param player The player for whom to check the condition.
     * @return true if the condition is met for the player, false otherwise.
     */
    boolean checkCondition(GameState gameState, Player player);

    /**
     * Provides a description of the victory condition.
     * @return A string describing the objective.
     */
    default String getDescription() {
        return "Default victory condition description.";
    }
}
