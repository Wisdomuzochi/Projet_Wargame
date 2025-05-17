package com.yourusername.wargame.model.unit;

import com.yourusername.wargame.engine.AxialCoord;
import com.yourusername.wargame.engine.Player;

/**
 * Factory for creating Unit instances.
 */
public class UnitFactory {

    /**
     * Creates a new Unit instance.
     * @param type The UnitType of the unit to create.
     * @param owner The Player who will own the unit.
     * @param initialPosition The starting position of the unit.
     * @return A new Unit object initialized with base stats and state.
     */
    public static Unit createUnit(UnitType type, Player owner, AxialCoord initialPosition) {
        // Could add more complex logic here if needed (e.g., checking prerequisites)
        return new Unit(type, owner, initialPosition);
    }
}
