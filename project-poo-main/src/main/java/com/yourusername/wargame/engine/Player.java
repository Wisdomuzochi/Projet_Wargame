package com.yourusername.wargame.engine;

import com.yourusername.wargame.model.unit.Unit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a player in the game (can be Human or AI).
 * Holds player-specific information like name, color (optional), and units.
 */
public abstract class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final int playerIndex; // e.g., 0 for Player 1, 1 for Player 2
    private final List<Unit> units; // Units owned by this player
    private boolean isAi;

    protected Player(String name, int playerIndex, boolean isAi) {
        this.name = Objects.requireNonNull(name, "Player name cannot be null");
        if (playerIndex < 0) {
            throw new IllegalArgumentException("Player index cannot be negative.");
        }
        this.playerIndex = playerIndex;
        this.units = new ArrayList<>();
        this.isAi = isAi;
    }

    public String getName() {
        return name;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    public boolean isAi() {
        return isAi;
    }

    /**
     * Gets a list of units currently owned by this player.
     * Returns a defensive copy to prevent external modification of the internal list.
     * @return A new list containing the player's units.
     */
    public List<Unit> getUnits() {
        return new ArrayList<>(units);
    }

    /**
     * Gets a list of active (non-destroyed) units owned by this player.
     * @return A new list containing the player's active units.
     */
    public List<Unit> getActiveUnits() {
        return units.stream()
                    .filter(unit -> unit.getCurrentHitPoints() > 0)
                    .collect(Collectors.toList());
    }

    /**
     * Adds a unit to this player's control.
     * Should be called when a unit is created or transferred.
     * @param unit The unit to add.
     */
    public void addUnit(Unit unit) {
        if (unit != null && unit.getOwner() == this && !units.contains(unit)) {
            units.add(unit);
        } else if (unit != null && unit.getOwner() != this) {
             System.err.println("Warning: Attempted to add unit owned by another player: " + unit);
             // Or throw exception
        }
    }

    /**
     * Removes a unit from this player's control (e.g., when destroyed).
     * @param unit The unit to remove.
     */
    public void removeUnit(Unit unit) {
        if (unit != null) {
            units.remove(unit);
        }
    }

    /**
     * Called at the start of this player's turn.
     * Resets unit states (movement points, action flags) and performs healing.
     * @param gameState The current game state (needed for context like terrain).
     */
    public void startTurn(GameState gameState) {
        System.out.println("--- Starting Turn for Player " + (playerIndex + 1) + ": " + name + " ---");
        List<Unit> activeUnits = getActiveUnits(); // Get units before potential destruction this turn
        for (Unit unit : activeUnits) {
            unit.resetForNewTurn();

            // Apply healing if the unit has less than max HP
            if (unit.getCurrentHitPoints() < unit.getMaxHitPoints()) {
                int healAmount = (int) Math.ceil(unit.getMaxHitPoints() * 0.10);
                if (healAmount > 0) {
                    System.out.println("Unit " + unit.getId().toString().substring(0,4) + " (" + unit.getTypeName() + ") attempts to heal " + healAmount + " HP.");
                    unit.heal(healAmount); // Heal method handles max HP cap
                }
            }
        }
    }

    /**
     * Abstract method for the player to take their turn.
     * AI players will implement their logic here.
     * Human player turns are driven by UI interactions calling GameController methods.
     * @param controller The GameController to interact with the game state.
     */
    public abstract void performTurn(GameController controller);

    @Override
    public String toString() {
        return "Player [name=" + name + ", index=" + playerIndex + ", isAi=" + isAi + ", units=" + units.size() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return playerIndex == player.playerIndex && name.equals(player.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, playerIndex);
    }
}
