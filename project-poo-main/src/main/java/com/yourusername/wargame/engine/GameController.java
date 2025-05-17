package com.yourusername.wargame.engine;

import com.yourusername.wargame.CombatResolver;
import com.yourusername.wargame.PathFinder;
import com.yourusername.wargame.model.unit.Unit;
import com.yourusername.wargame.model.terrain.TerrainType;

import java.io.Serializable;
import java.util.*;

/**
 * Main controller for game mechanics.
 * Manages game state and handles player actions.
 */
public class GameController implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- Core game components ---
    private GameState gameState;
    private final PathFinder pathFinder;
    private final CombatResolver combatResolver;
    private final FogOfWar fogOfWar;
    
    // --- Game flow control ---
    private boolean gameRunning;
    private Player winner; // null until game over
    
    // --- Event listeners ---
    private GameEventListener listener;
    
    // --- Random generator ---
    private final Random random;
    
    /**
     * Constructor that initializes core game components.
     */
    public GameController() {
        this.random = new Random();
        this.pathFinder = new PathFinder();
        this.combatResolver = new CombatResolver(random);
        this.fogOfWar = new FogOfWar();
        this.gameRunning = false;
        this.gameState = null;
        this.winner = null;
    }
    
    /**
     * Initializes a new game with the provided game state.
     * @param gameState The initial game state.
     */
    public synchronized void initializeGame(GameState gameState) {
        if (gameState == null) {
            throw new IllegalArgumentException("GameState cannot be null");
        }
        
        // Stop any existing game
        this.gameRunning = false;
        
        // Set the new game state
        this.gameState = gameState;
        this.winner = null;
        
        // Initialize fog of war for player visibility
        fogOfWar.updateVisibility(gameState);
        
        System.out.println("Game initialized with " + gameState.getPlayers().size() + " players.");
    }
    
    /**
     * Start the game after initialization.
     */
    public synchronized void startGame() {
        if (gameState == null) {
            throw new IllegalStateException("Cannot start game: Game not initialized");
        }
        
        gameRunning = true;
        winner = null;
        
        // Reset all units for the first turn
        for (Player player : gameState.getPlayers()) {
            for (Unit unit : player.getActiveUnits()) {
                unit.startTurn();
            }
        }
        
        System.out.println("Game started. It's " + gameState.getCurrentPlayer().getName() + "'s turn.");
        
        // Notify UI and other listeners about initial state
        if (listener != null) {
            listener.onGameStateUpdate(gameState);
            listener.onTurnStart(gameState.getCurrentPlayer());
        }
        
        // If first player is AI, trigger its turn immediately
        Player firstPlayer = gameState.getCurrentPlayer();
        if (firstPlayer.isAi()) {
            System.out.println("First player is AI, triggering turn automatically.");
            ((AiPlayer) firstPlayer).performTurn(this);
        }
    }

    /**
     * Set a listener to be notified of game events.
     * @param listener The listener to set.
     */
    public void setGameEventListener(GameEventListener listener) {
        this.listener = listener;
    }

    /**
     * Process a move request from a player.
     * @param player The player requesting the move.
     * @param unit The unit to move.
     * @param path The path to follow (including final destination).
     * @return True if the move was successful, false otherwise.
     */
    public synchronized boolean requestMove(Player player, Unit unit, List<AxialCoord> path) {
        // Validate game state & prerequisites
        if (!gameRunning || gameState == null) {
            System.err.println("Game not running.");
            return false;
        }
        
        if (player != gameState.getCurrentPlayer()) {
            System.err.println("Not " + player.getName() + "'s turn.");
            return false;
        }
        
        if (unit == null || unit.getOwner() != player) {
            System.err.println("Unit is null or does not belong to player.");
            return false;
        }
        
        if (path == null || path.isEmpty()) {
            System.err.println("Path is empty or null.");
            return false;
        }
        
        if (unit.hasMovedThisTurn()) {
            System.err.println("Unit already moved this turn.");
            return false;
        }
        
        // Get the final destination
        AxialCoord destination = path.get(path.size() - 1);
        Hex destinationHex = gameState.getBoard().getHex(destination);
        if (destinationHex == null || !destinationHex.isAccessible() || destinationHex.isOccupied()) {
            System.err.println("Destination hex is invalid or occupied.");
            return false;
        }
        
        // Calculate total path cost
        int totalCost = 0;
        AxialCoord from = unit.getPosition();
        for (AxialCoord to : path) {
            Hex hex = gameState.getBoard().getHex(to);
            if (hex != null) {
                totalCost += gameState.getBoard().getMovementCost(hex, unit);
            }
            from = to;
        }
        
        // Check if unit has enough movement points
        if (totalCost > unit.getCurrentMovementPoints()) {
            System.err.println("Not enough movement points. Cost: " + totalCost + 
                               ", Available: " + unit.getCurrentMovementPoints());
            return false;
        }
        
        // Perform the move
        if (gameState.getBoard().moveUnit(unit, destination)) {
            // Update unit state
            unit.spendMovementPoints(totalCost);
            unit.setHasMovedThisTurn(true);
            
            // Update visibility after moving
            fogOfWar.updateVisibility(gameState);
            
            // Notify listeners
            if (listener != null) {
                listener.onUnitAction(unit, "move");
                listener.onGameStateUpdate(gameState);
            }
            
            System.out.println("Unit " + unit.getTypeName() + " moved to " + destination);
            return true;
        }
        
        return false;
    }
    
    /**
     * Find all possible moves for a unit.
     * @param unit The unit to find moves for.
     * @return A list of coordinates the unit can move to.
     */
    public List<AxialCoord> findPossibleMoves(Unit unit) {
        if (!gameRunning || gameState == null || unit == null) {
            return Collections.emptyList();
        }
        
        if (unit.hasMovedThisTurn() || unit.getCurrentMovementPoints() <= 0) {
            return Collections.emptyList();
        }
        
        List<AxialCoord> possibleMoves = new ArrayList<>();
        Board board = gameState.getBoard();
        AxialCoord unitPos = unit.getPosition();
        
        // Check all hexes within unit's movement range
        for (Hex hex : board.getAllHexes()) {
            AxialCoord coord = hex.getCoordinates();
            
            // Skip current position and occupied hexes
            if (coord.equals(unitPos) || hex.isOccupied() || !hex.isAccessible()) {
                continue;
            }
            
            // Find path to this coordinate
            List<AxialCoord> path = pathFinder.findPath(unit, unitPos, coord, board);
            if (!path.isEmpty()) {
                // Calculate path cost
                int cost = 0;
                AxialCoord from = unitPos;
                for (AxialCoord step : path) {
                    Hex stepHex = board.getHex(step);
                    cost += board.getMovementCost(stepHex, unit);
                    from = step;
                }
                
                // If path is within unit's movement range, add to possible moves
                if (cost <= unit.getCurrentMovementPoints()) {
                    possibleMoves.add(coord);
                }
            }
        }
        
        return possibleMoves;
    }
    
    /**
     * Find all possible attack targets for a unit.
     * @param unit The unit to find targets for.
     * @return A list of coordinates the unit can attack.
     */
    public List<AxialCoord> findPossibleAttacks(Unit unit) {
        if (!gameRunning || gameState == null || unit == null) {
            return Collections.emptyList();
        }
        
        if (unit.hasAttackedThisTurn()) {
            return Collections.emptyList();
        }
        
        List<AxialCoord> possibleAttacks = new ArrayList<>();
        Board board = gameState.getBoard();
        AxialCoord unitPos = unit.getPosition();
        Player currentPlayer = gameState.getCurrentPlayer();
        
        // Check all hexes within unit's attack range
        for (Hex hex : board.getAllHexes()) {
            AxialCoord coord = hex.getCoordinates();
            
            // Skip empty hexes and friendly units
            if (!hex.isOccupied()) continue;
            
            Unit targetUnit = hex.getUnit();
            if (targetUnit.getOwner() == currentPlayer) continue;
            
            // Check if within attack range
            int distance = unitPos.distanceTo(coord);
            if (distance <= unit.getAttackRange()) {
                possibleAttacks.add(coord);
            }
        }
        
        return possibleAttacks;
    }
    
    /**
     * Process an attack request from a player.
     * @param player The player requesting the attack.
     * @param attacker The attacking unit.
     * @param defender The defending unit.
     * @return True if the attack was successful, false otherwise.
     */
    public synchronized boolean requestAttack(Player player, Unit attacker, Unit defender) {
        // Validate game state & prerequisites
        if (!gameRunning || gameState == null) {
            System.err.println("Game not running.");
            return false;
        }
        
        if (player != gameState.getCurrentPlayer()) {
            System.err.println("Not " + player.getName() + "'s turn.");
            return false;
        }
        
        if (attacker == null || attacker.getOwner() != player) {
            System.err.println("Attacker is null or does not belong to player.");
            return false;
        }
        
        if (defender == null || defender.getOwner() == player) {
            System.err.println("Cannot attack your own units.");
            return false;
        }
        
        if (attacker.hasAttackedThisTurn()) {
            System.err.println("Unit already attacked this turn.");
            return false;
        }
        
        // Check range
        int distance = attacker.getPosition().distanceTo(defender.getPosition());
        if (distance > attacker.getAttackRange()) {
            System.err.println("Target out of range. Range: " + attacker.getAttackRange() + 
                              ", Distance: " + distance);
            return false;
        }
        
        // Get defender's terrain for defense calculations
        TerrainType defenderTerrain = gameState.getBoard().getHex(defender.getPosition()).getTerrainType();
        
        // Perform attack
        System.out.println("Attacking " + defender.getTypeName() + " with " + attacker.getTypeName());
        boolean defenderDestroyed = combatResolver.resolveCombat(attacker, defender, defenderTerrain, gameState);
        
        // Mark attacker as having attacked
        attacker.setHasAttackedThisTurn(true);
        attacker.setHasMovedThisTurn(true); // Can't move after attacking
        
        // If defender was destroyed, remove from board
        if (defenderDestroyed) {
            gameState.getBoard().removeUnit(defender);
            defender.getOwner().removeUnit(defender);
            System.out.println("Unit destroyed: " + defender.getTypeName());
            
            // Check if this attack caused a victory
            checkVictoryConditions();
        }
        
        // Update fog of war
        fogOfWar.updateVisibility(gameState);
        
        // Notify listeners
        if (listener != null) {
            listener.onCombatResolved(attacker, defender, 
                                      defender.getMaxHitPoints() - defender.getCurrentHitPoints(), 
                                      defenderDestroyed);
            listener.onGameStateUpdate(gameState);
        }
        
        return true;
    }
    
    /**
     * End the current player's turn and advance to the next.
     * @param player The player ending their turn.
     * @return True if turn was ended successfully.
     */
    public synchronized boolean endTurn(Player player) {
        if (!gameRunning || gameState == null) {
            return false;
        }
        
        if (player != gameState.getCurrentPlayer()) {
            System.err.println("Not " + player.getName() + "'s turn, cannot end turn.");
            return false;
        }
        
        // Advance to next player
        Player nextPlayer = gameState.advanceTurn();
        System.out.println("Turn ended. Next player: " + nextPlayer.getName());
        
        // Reset all units for the next player
        for (Unit unit : nextPlayer.getActiveUnits()) {
            unit.startTurn();
        }
        
        // Update visibility for the new player
        fogOfWar.updateVisibility(gameState);
        
        // Check if any victory conditions are met
        checkVictoryConditions();
        
        // Notify listeners
        if (listener != null) {
            listener.onTurnStart(nextPlayer);
            listener.onGameStateUpdate(gameState);
        }
        
        // If the next player is AI, handle their turn
        if (gameRunning && nextPlayer.isAi()) {
            ((AiPlayer) nextPlayer).performTurn(this);
        }
        
        return true;
    }
    
    /**
     * Check if any victory conditions have been met.
     */
    private void checkVictoryConditions() {
        if (!gameRunning || gameState == null) return;
        
        for (Player player : gameState.getPlayers()) {
            // Check each victory condition for this player
            List<VictoryCondition> conditions = gameState.getVictoryConditionsForPlayer(player);
            
            for (VictoryCondition condition : conditions) {
                if (condition.checkCondition(gameState, player)) {
                    // Victory condition met!
                    declareWinner(player);
                    return;
                }
            }
            
            // Also check if the player has no units left (defeat condition)
            if (player.getActiveUnits().isEmpty()) {
                Player opponent = gameState.getOpponent(player);
                if (opponent != null) {
                    declareWinner(opponent);
                    return;
                }
            }
        }
    }
    
    /**
     * Declare a winner and end the game.
     * @param winningPlayer The player who won.
     */
    private void declareWinner(Player winningPlayer) {
        if (!gameRunning || gameState == null) return;
        
        this.winner = winningPlayer;
        this.gameRunning = false;
        
        System.out.println("Game over! Winner: " + winningPlayer.getName());
        
        // Notify listeners
        if (listener != null) {
            listener.onGameOver(winningPlayer);
        }
    }
    
    /**
     * Get the current game state.
     * @return The game state, or null if no game is initialized.
     */
    public GameState getGameState() {
        return gameState;
    }
    
    /**
     * Check if a game is currently running.
     * @return True if a game is running, false otherwise.
     */
    public boolean isGameRunning() {
        return gameRunning && gameState != null;
    }
    
    /**
     * Get the winning player if the game is over.
     * @return The winning player, or null if no winner yet.
     */
    public Player getWinner() {
        return winner;
    }
    
    /**
     * Interface for objects that want to listen to game events.
     */
    public interface GameEventListener {
        /**
         * Called when the game state is updated.
         * @param newState The updated game state.
         */
        void onGameStateUpdate(GameState newState);
        
        /**
         * Called when a unit performs an action.
         * @param unit The unit that performed the action.
         * @param actionType The type of action (e.g., "move", "attack").
         */
        void onUnitAction(Unit unit, String actionType);
        
        /**
         * Called when combat is resolved.
         * @param attacker The attacking unit.
         * @param defender The defending unit.
         * @param damageDealt The amount of damage dealt.
         * @param defenderDestroyed Whether the defender was destroyed.
         */
        void onCombatResolved(Unit attacker, Unit defender, int damageDealt, boolean defenderDestroyed);
        
        /**
         * Called when a new turn starts.
         * @param player The player whose turn is starting.
         */
        void onTurnStart(Player player);
        
        /**
         * Called when the game is over.
         * @param winner The winning player.
         */
        void onGameOver(Player winner);
    }
}