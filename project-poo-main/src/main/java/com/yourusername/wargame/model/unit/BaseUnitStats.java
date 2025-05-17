package com.yourusername.wargame.model.unit;

import java.io.Serializable;
import java.util.Objects;

/**
 * Holds the immutable base statistics for a specific type of unit.
 */
public class BaseUnitStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final int baseAttack;
    private final int baseDefense;
    private final int maxMovementPoints; // Potential de déplacement initial
    private final int visionRange;       // Champ de vision
    private final int maxHitPoints;      // PV initial
    private final int attackRange;       // Attack range (1 for melee, >1 for ranged)

    public BaseUnitStats(String name, int baseAttack, int baseDefense, int maxMovementPoints, int visionRange, int maxHitPoints, int attackRange) {
        this.name = Objects.requireNonNull(name, "Unit name cannot be null");
        // Basic validation, can be expanded
        if (baseAttack < 0 || baseDefense < 0 || maxMovementPoints <= 0 || visionRange <= 0 || maxHitPoints <= 0 || attackRange <= 0) {
            throw new IllegalArgumentException("Unit stats must be positive (or zero for attack/defense).");
        }
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.maxMovementPoints = maxMovementPoints;
        this.visionRange = visionRange;
        this.maxHitPoints = maxHitPoints;
        this.attackRange = attackRange;
    }

    // Getters for all stats
    public String getName() { return name; }
    public int getBaseAttack() { return baseAttack; }
    public int getBaseDefense() { return baseDefense; }
    public int getMaxMovementPoints() { return maxMovementPoints; }
    public int getVisionRange() { return visionRange; }
    public int getMaxHitPoints() { return maxHitPoints; }
    public int getAttackRange() { return attackRange; }

    @Override
    public String toString() {
        return String.format("%s [Att:%d, Def:%d, Mov:%d, Vis:%d, HP:%d, Rng:%d]",
                             name, baseAttack, baseDefense, maxMovementPoints, visionRange, maxHitPoints, attackRange);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseUnitStats that = (BaseUnitStats) o;
        return baseAttack == that.baseAttack &&
               baseDefense == that.baseDefense &&
               maxMovementPoints == that.maxMovementPoints &&
               visionRange == that.visionRange &&
               maxHitPoints == that.maxHitPoints &&
               attackRange == that.attackRange &&
               name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, baseAttack, baseDefense, maxMovementPoints, visionRange, maxHitPoints, attackRange);
    }
}
