package com.yourusername.wargame.engine;

import com.yourusername.wargame.model.unit.Unit;

import java.io.Serializable;

/**
 * Handles fog of war mechanics, determining what each player can see.
 */
public class FogOfWar implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Updates the visibility state for the current player.
     * @param gameState The current game state.
     */
    public void updateVisibility(GameState gameState) {
        if (gameState == null) return;
        
        Board board = gameState.getBoard();
        if (board == null) return;
        
        // Reset all visibility
        for (Hex hex : board.getAllHexes()) {
            hex.resetVisibility();
        }
        
        // For each player, calculate what they can see based on their units
        for (int playerIdx = 0; playerIdx < gameState.getPlayers().size(); playerIdx++) {
            Player player = gameState.getPlayers().get(playerIdx);
            
            // Set visibility based on units
            for (Unit unit : player.getActiveUnits()) {
                AxialCoord unitPos = unit.getPosition();
                int visionRange = unit.getVisionRange();
                
                // Mark all hexes in vision range as visible
                for (Hex hex : board.getAllHexes()) {
                    AxialCoord hexCoord = hex.getCoordinates();
                    int distance = unitPos.distanceTo(hexCoord);
                    
                    if (distance <= visionRange) {
                        hex.setVisibleToPlayer(playerIdx, true);
                    }
                }
            }
        }
    }

    /**
     * Checks if a hex is visible to the specified player.
     * @param hex The hex to check visibility for.
     * @param playerIndex The player's index.
     * @return True if the hex is visible to the player.
     */
    public boolean isVisibleToPlayer(Hex hex, int playerIndex) {
        return hex != null && hex.isVisibleToPlayer(playerIndex);
    }
}