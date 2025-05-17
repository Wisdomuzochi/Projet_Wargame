package com.yourusername.wargame.engine;

import java.io.Serializable;
import java.util.*;

/**
 * Holds the current state of the game.
 * This includes the board, players, and game progress information.
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final Board board;
    private final List<Player> players;
    private final Map<Player, List<VictoryCondition>> victoryConditions;
    
    private int currentPlayerIndex;
    private int turnNumber;
    
    /**
     * Creates a new game state with the specified board and players.
     * @param board The game board.
     * @param players The players participating in the game.
     * @param victoryConditions The victory conditions for each player.
     */
    public GameState(Board board, List<Player> players, Map<Player, List<VictoryCondition>> victoryConditions) {
        this.board = Objects.requireNonNull(board, "Board cannot be null");
        
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("Players list cannot be null or empty");
        }
        this.players = new ArrayList<>(players);
        
        if (victoryConditions == null) {
            this.victoryConditions = new HashMap<>();
        } else {
            this.victoryConditions = new HashMap<>(victoryConditions);
        }
        
        this.currentPlayerIndex = 0;
        this.turnNumber = 1;
    }
    
    /**
     * Gets the game board.
     * @return The board.
     */
    public Board getBoard() {
        return board;
    }
    
    /**
     * Gets the list of players.
     * @return An unmodifiable list of players.
     */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }
    
    /**
     * Gets the player whose turn it currently is.
     * @return The current player.
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
    
    /**
     * Gets the opponent of the specified player.
     * Currently assumes 2 players only.
     * @param player The player whose opponent to get.
     * @return The opponent player, or null if player not found or no opponent.
     */
    public Player getOpponent(Player player) {
        if (players.size() <= 1) return null;
        
        int playerIndex = players.indexOf(player);
        if (playerIndex < 0) return null;
        
        return players.get((playerIndex + 1) % players.size());
    }
    
    /**
     * Gets the current turn number.
     * @return The turn number.
     */
    public int getCurrentTurnNumber() {
        return turnNumber;
    }
    
    /**
     * Advances the turn to the next player.
     * @return The next player.
     */
    public Player advanceTurn() {
        // Move to next player
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        
        // If we've gone through all players, increment turn number
        if (currentPlayerIndex == 0) {
            turnNumber++;
        }
        
        return getCurrentPlayer();
    }
    
    /**
     * Gets the victory conditions for a specific player.
     * @param player The player.
     * @return The list of victory conditions for the player, or an empty list if none.
     */
    public List<VictoryCondition> getVictoryConditionsForPlayer(Player player) {
        return victoryConditions.getOrDefault(player, Collections.emptyList());
    }
    
    /**
     * Creates a deep copy of this game state.
     * Useful for AI calculations or undo functionality.
     * @return A deep copy of this game state.
     */
    public GameState deepCopy() {
        // Note: This is a placeholder implementation that would need to be expanded
        // to properly deep copy all mutable objects (board, players, etc.)
        throw new UnsupportedOperationException("Deep copy not implemented yet");
    }
}
