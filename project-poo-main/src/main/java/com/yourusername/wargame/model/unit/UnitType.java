package com.yourusername.wargame.model.unit;

import java.io.Serializable;

/**
 * Enum defining the different types of units available in the game.
 * Each type holds its BaseUnitStats.
 */
public enum UnitType implements Serializable {
    INFANTRY("Infantry", 5, 3, 6, 4, 28, 1),       // Melee Range 1
    HEAVY_INFANTRY("Heavy Infantry", 10, 10, 4, 4, 38, 1), // Melee Range 1
    CAVALRY("Cavalry", 8, 3, 8, 6, 38, 1),         // Melee Range 1
    MAGE("Mage", 5, 1, 5, 5, 24, 2),               // Ranged Range 2
    ARCHER("Archer", 6, 2, 5, 7, 33, 3);         // Ranged Range 3

    private final BaseUnitStats stats;

    UnitType(String name, int attack, int defense, int movement, int vision, int hp, int range) {
        this.stats = new BaseUnitStats(name, attack, defense, movement, vision, hp, range);
    }

    /**
     * Gets the base statistics associated with this unit type.
     * @return The BaseUnitStats object.
     */
    public BaseUnitStats getStats() {
        return stats;
    }

    // --- Convenience Getters ---
    public String getName() { return stats.getName(); }
    public int getBaseAttack() { return stats.getBaseAttack(); }
    public int getBaseDefense() { return stats.getBaseDefense(); }
    public int getMaxMovementPoints() { return stats.getMaxMovementPoints(); }
    public int getVisionRange() { return stats.getVisionRange(); }
    public int getMaxHitPoints() { return stats.getMaxHitPoints(); }
    public int getAttackRange() { return stats.getAttackRange(); }

    @Override
    public String toString() {
        return stats.toString();
    }
}
