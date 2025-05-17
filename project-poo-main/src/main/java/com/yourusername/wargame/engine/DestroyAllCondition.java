package com.yourusername.wargame.engine;

import java.util.Objects;

/**
 * Victory condition that is met when all of the target player's units are destroyed.
 */
public class DestroyAllCondition implements VictoryCondition {
    private static final long serialVersionUID = 1L;
    
    private final Player targetPlayer;
    
    /**
     * Creates a new DestroyAllCondition.
     * @param targetPlayer The player whose units must be destroyed.
     */
    public DestroyAllCondition(Player targetPlayer) {
        this.targetPlayer = Objects.requireNonNull(targetPlayer, "Target player cannot be null");
    }
    
    @Override
    public boolean checkCondition(GameState gameState, Player player) {
        // Condition is met if the target player has no active units
        return targetPlayer.getActiveUnits().isEmpty();
    }
    
    @Override
    public String getDescription() {
        return "Destroy all of " + targetPlayer.getName() + "'s units.";
    }
}
