package com.yourusername.wargame.model.terrain;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the effects of a specific terrain type.
 * Immutable class.
 */
public class TerrainEffect implements Serializable {
    private static final long serialVersionUID = 1L; // For serialization

    private final int movementCost; // Base movement point cost to enter this terrain
    private final double defenseBonus; // Defense bonus multiplier (e.g., 0.2 for 20%)
    private final boolean isAccessible; // Can units enter this terrain?
    private final String displayName; // User-friendly name

    public TerrainEffect(String displayName, int movementCost, double defenseBonus, boolean isAccessible) {
        this.displayName = Objects.requireNonNull(displayName, "Display name cannot be null");
        if (movementCost < 0) {
            throw new IllegalArgumentException("Movement cost cannot be negative.");
        }
        if (defenseBonus < 0.0) {
            throw new IllegalArgumentException("Defense bonus cannot be negative.");
        }
        this.movementCost = movementCost;
        this.defenseBonus = defenseBonus;
        this.isAccessible = isAccessible;
    }

    public int getMovementCost() {
        return movementCost;
    }

    // Returns the defense bonus as a multiplier (e.g., 0.6 for 60%)
    public double getDefenseBonus() {
        return defenseBonus;
    }

    public boolean isAccessible() {
        return isAccessible;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName + " (Cost: " + movementCost + ", Def: " + (int)(defenseBonus * 100) + "%, Access: " + isAccessible + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerrainEffect that = (TerrainEffect) o;
        return movementCost == that.movementCost &&
               Double.compare(that.defenseBonus, defenseBonus) == 0 &&
               isAccessible == that.isAccessible &&
               displayName.equals(that.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, movementCost, defenseBonus, isAccessible);
    }
}
