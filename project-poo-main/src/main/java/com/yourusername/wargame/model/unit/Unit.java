package com.yourusername.wargame.model.unit;

import com.yourusername.wargame.engine.AxialCoord;
import com.yourusername.wargame.engine.Player;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single unit instance on the game board.
 * Holds mutable state like current HP, position, owner, etc.
 */
public class Unit implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id; // Unique identifier for this specific unit instance
    private final UnitType unitType;
    private final Player owner; // Reference to the player controlling this unit

    private AxialCoord position;
    private int currentHitPoints;
    private int currentMovementPoints;
    private boolean hasMovedThisTurn;
    private boolean hasAttackedThisTurn;
    // Add other status effects if needed (e.g., poisoned, fortified)

    public Unit(UnitType unitType, Player owner, AxialCoord initialPosition) {
        this.id = UUID.randomUUID();
        this.unitType = Objects.requireNonNull(unitType, "UnitType cannot be null");
        this.owner = Objects.requireNonNull(owner, "Owner cannot be null");
        this.position = Objects.requireNonNull(initialPosition, "Initial position cannot be null");

        // Initialize dynamic state
        this.currentHitPoints = unitType.getMaxHitPoints();
        this.currentMovementPoints = unitType.getMaxMovementPoints(); // Start with full move
        this.hasMovedThisTurn = false;
        this.hasAttackedThisTurn = false;
    }

    // --- Getters for immutable properties ---
    public UUID getId() { return id; }
    public UnitType getUnitType() { return unitType; }
    public Player getOwner() { return owner; }

    // --- Getters and Setters for mutable state ---
    public AxialCoord getPosition() { return position; }
    public void setPosition(AxialCoord position) { this.position = position; }

    public int getCurrentHitPoints() { return currentHitPoints; }
    public int getMaxHitPoints() { return unitType.getMaxHitPoints(); } // Convenience

    public int getCurrentMovementPoints() { return currentMovementPoints; }
    public int getMaxMovementPoints() { return unitType.getMaxMovementPoints(); } // Convenience

    public boolean hasMovedThisTurn() { return hasMovedThisTurn; }
    public void setHasMovedThisTurn(boolean hasMoved) { this.hasMovedThisTurn = hasMoved; }

    public boolean hasAttackedThisTurn() { return hasAttackedThisTurn; }
    public void setHasAttackedThisTurn(boolean hasAttacked) { this.hasAttackedThisTurn = hasAttacked; }

    /**
     * Checks if the unit can act (move OR attack).
     */
    public boolean canAct() {
        return !hasAttackedThisTurn && !hasMovedThisTurn;
    }

    /**
     * Checks if the unit can still move.
     */
    public boolean canMove() {
        return currentMovementPoints > 0 && !hasMovedThisTurn;
    }

    /**
     * Checks if the unit can still attack.
     */
    public boolean canAttack() {
        return !hasAttackedThisTurn;
    }

    /**
     * Applies damage to the unit, reducing its HP. HP cannot go below 0.
     * @param amount The amount of damage to apply (should be non-negative).
     * @return true if the unit survived, false if the unit's HP dropped to 0 or below.
     */
    public boolean takeDamage(int amount) {
        if (amount < 0) amount = 0; // Prevent negative damage (healing)
        this.currentHitPoints -= amount;
        if (this.currentHitPoints < 0) {
            this.currentHitPoints = 0;
        }
        return this.currentHitPoints > 0;
    }

    /**
     * Heals the unit, increasing its HP. HP cannot exceed the maximum.
     * @param amount The amount to heal (should be non-negative).
     */
    public void heal(int amount) {
        if (amount < 0) amount = 0; // Prevent negative healing
        this.currentHitPoints += amount;
        if (this.currentHitPoints > getMaxHitPoints()) {
            this.currentHitPoints = getMaxHitPoints();
        }
    }

    /**
     * Spends movement points.
     * @param cost The number of points to spend (should be non-negative).
     */
    public void spendMovementPoints(int cost) {
        if (cost < 0) {
            throw new IllegalArgumentException("Movement cost cannot be negative.");
        }
        if (cost > this.currentMovementPoints) {
            this.currentMovementPoints = 0; // Spend all remaining if cost exceeds
        } else {
            this.currentMovementPoints -= cost;
        }
    }

    /**
     * Resets the unit's state for the start of a new turn.
     * Restores movement points, resets action flags.
     */
    public void resetForNewTurn() {
        this.currentMovementPoints = getMaxMovementPoints();
        this.hasMovedThisTurn = false;
        this.hasAttackedThisTurn = false;
    }
    
    /**
     * Alternative name for resetForNewTurn(), used by GameController.
     */
    public void startTurn() {
        resetForNewTurn();
    }

    // --- Base Stat Convenience Getters ---
    public int getBaseAttack() { return unitType.getBaseAttack(); }
    public int getBaseDefense() { return unitType.getBaseDefense(); }
    public int getVisionRange() { return unitType.getVisionRange(); }
    public int getAttackRange() { return unitType.getAttackRange(); }
    public String getTypeName() { return unitType.getName(); } // For display

    @Override
    public String toString() {
        return String.format("%s (%s) at %s - HP:%d/%d, MP:%d/%d - Owner: %s",
                             id.toString().substring(0, 8), // Short ID
                             unitType.getName(),
                             position,
                             currentHitPoints, getMaxHitPoints(),
                             currentMovementPoints, getMaxMovementPoints(),
                             owner != null ? owner.getName() : "None");
    }

    // Use UUID for unique identity of unit instances
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Unit unit = (Unit) o;
        return id.equals(unit.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
