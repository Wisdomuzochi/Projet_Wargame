package com.yourusername.wargame.engine;

import com.yourusername.wargame.model.terrain.TerrainType;
import com.yourusername.wargame.model.unit.Unit;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a single hexagonal tile on the game board.
 */
public class Hex implements Serializable {
    private static final long serialVersionUID = 1L; // For serialization

    private final AxialCoord coordinates;
    private final TerrainType terrainType;
    private Unit unit; // The unit currently occupying this hex (can be null)
    private boolean visibleToPlayer1; // Example visibility flag
    private boolean visibleToPlayer2; // Example visibility flag

    public Hex(AxialCoord coordinates, TerrainType terrainType) {
        this.coordinates = Objects.requireNonNull(coordinates, "Coordinates cannot be null");
        this.terrainType = Objects.requireNonNull(terrainType, "Terrain type cannot be null");
        this.unit = null; // Initially empty
        this.visibleToPlayer1 = false; // Initially not visible
        this.visibleToPlayer2 = false; // Initially not visible
    }

    public AxialCoord getCoordinates() {
        return coordinates;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public Unit getUnit() {
        return unit;
    }

    /**
     * Sets the unit occupying this hex.
     * @param unit The unit to place, or null to clear the hex.
     */
    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    /**
     * Checks if this hex is currently occupied by a unit.
     * @return true if a unit is present, false otherwise.
     */
    public boolean isOccupied() {
        return this.unit != null;
    }

    /**
     * Checks if a unit can enter this hex based on terrain accessibility.
     * Does NOT check for occupation.
     * @return true if the terrain is accessible, false otherwise.
     */
    public boolean isAccessible() {
        return this.terrainType.isAccessible();
    }

    // --- Visibility Methods ---
    public boolean isVisibleToPlayer(int playerIndex) {
        // Simple example for 2 players (index 0 and 1)
        if (playerIndex == 0) {
            return visibleToPlayer1;
        } else if (playerIndex == 1) {
            return visibleToPlayer2;
        }
        return false; // Or throw exception for invalid index
    }

    public void setVisibleToPlayer(int playerIndex, boolean visible) {
        if (playerIndex == 0) {
            this.visibleToPlayer1 = visible;
        } else if (playerIndex == 1) {
            this.visibleToPlayer2 = visible;
        }
        // Handle other players if necessary
    }

    // Reset visibility (e.g., at start of turn before recalculating)
    public void resetVisibility() {
        this.visibleToPlayer1 = false;
        this.visibleToPlayer2 = false;
    }

    @Override
    public String toString() {
        return "Hex[" + coordinates + ", " + terrainType.getDisplayName() + ", Unit: " + (unit != null ? unit.getUnitType().getName() : "None") + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hex hex = (Hex) o;
        return coordinates.equals(hex.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinates);
    }
}
