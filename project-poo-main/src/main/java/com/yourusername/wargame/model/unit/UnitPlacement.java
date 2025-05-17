package com.yourusername.wargame.model.unit;

import com.yourusername.wargame.engine.AxialCoord;
import com.yourusername.wargame.engine.Player;

import java.io.Serializable;

/**
 * Defines a unit placement on the map.
 */
public class UnitPlacement implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final UnitType unitType;
    
    /**
     * Creates a new UnitPlacement.
     * @param unitType The type of unit to place.
     */
    public UnitPlacement(UnitType unitType) {
        this.unitType = unitType;
    }
    
    /**
     * Creates a unit at the specified position for the given player.
     * @param owner The player who will own this unit.
     * @return A new Unit instance.
     */
    public Unit createUnit(Player owner) {
        return UnitFactory.createUnit(unitType, owner, null); // Position will be set by Board.placeUnit
    }
}
