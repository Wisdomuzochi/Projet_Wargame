package com.yourusername.wargame.model.terrain;

import java.io.Serializable;

/**
 * Enum defining the different types of terrain available in the game.
 * Each type has associated effects defined in TerrainEffect.
 */
public enum TerrainType implements Serializable {
    PLAIN("Plain", 1, 0.20, true),          // Cost 1, 20% Defense Bonus
    FOREST("Forest", 2, 0.40, true),        // Cost 2, 40% Defense Bonus
    HILLS("Hills", 2, 0.50, true),          // Cost 2, 50% Defense Bonus
    MOUNTAIN("Mountain", 3, 0.60, true),    // Cost 3, 60% Defense Bonus (Assume accessible for now)
    VILLAGE("Village", 1, 0.40, true),      // Cost 1, 40% Defense Bonus
    FORTRESS("Fortress", 1, 0.60, true),    // Cost 1, 60% Defense Bonus
    DEEP_WATER("Deep Water", 99, 0.0, false); // Cost 99 (effectively infinite), 0% Def, Inaccessible

    private final TerrainEffect effects;

    TerrainType(String displayName, int movementCost, double defenseBonus, boolean isAccessible) {
        this.effects = new TerrainEffect(displayName, movementCost, defenseBonus, isAccessible);
    }

    /**
     * Gets the effects associated with this terrain type.
     * @return The TerrainEffect object.
     */
    public TerrainEffect getEffects() {
        return effects;
    }

    /**
     * Convenience method to get movement cost.
     * @return The base movement cost.
     */
    public int getMovementCost() {
        return effects.getMovementCost();
    }

     /**
     * Convenience method to get defense bonus.
     * @return The defense bonus multiplier.
     */
    public double getDefenseBonus() {
        return effects.getDefenseBonus();
    }

     /**
     * Convenience method to check accessibility.
     * @return True if accessible, false otherwise.
     */
    public boolean isAccessible() {
        return effects.isAccessible();
    }

    /**
     * Convenience method to get display name.
     * @return The user-friendly name.
     */
    public String getDisplayName() {
        return effects.getDisplayName();
    }

    @Override
    public String toString() {
        return effects.toString();
    }
}
