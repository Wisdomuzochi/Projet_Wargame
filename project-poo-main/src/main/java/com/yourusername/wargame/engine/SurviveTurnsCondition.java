package com.yourusername.wargame.engine;

/**
 * Victory condition that is met when the player survives for a specified number of turns.
 */
public class SurviveTurnsCondition implements VictoryCondition {
    private static final long serialVersionUID = 1L;
    
    private final int targetTurns;
    
    /**
     * Creates a new SurviveTurnsCondition.
     * @param targetTurns The number of turns to survive.
     */
    public SurviveTurnsCondition(int targetTurns) {
        if (targetTurns <= 0) {
            throw new IllegalArgumentException("Target turns must be positive");
        }
        this.targetTurns = targetTurns;
    }
    
    @Override
    public boolean checkCondition(GameState gameState, Player player) {
        // Player must still have units and the turn number must be greater than target
        return !player.getActiveUnits().isEmpty() && gameState.getCurrentTurnNumber() > targetTurns;
    }
    
    @Override
    public String getDescription() {
        return "Survive for " + targetTurns + " turns.";
    }
}
