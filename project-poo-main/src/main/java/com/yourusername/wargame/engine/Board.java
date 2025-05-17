package com.yourusername.wargame.engine;

import com.yourusername.wargame.model.terrain.TerrainType;
import com.yourusername.wargame.model.unit.Unit;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the game board, a collection of Hex tiles.
 * Uses Axial Coordinates.
 */
public class Board implements Serializable {
    private static final long serialVersionUID = 1L;

    // Using HashMap for potentially sparse maps or irregular shapes easily
    private final Map<AxialCoord, Hex> hexes;
    private final int mapRadius; // Example: for a hex shape map

    // Constructor for a hex-shaped map of a given radius
    public Board(int radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("Map radius cannot be negative.");
        }
        this.mapRadius = radius;
        this.hexes = new HashMap<>();
        generateHexagonalMap(radius);
    }

    // Constructor potentially loading from a definition
    public Board(Map<AxialCoord, TerrainType> mapDefinition) {
         this.hexes = new HashMap<>();
         this.mapRadius = calculateRadiusFromCoords(mapDefinition.keySet());
         Objects.requireNonNull(mapDefinition, "Map definition cannot be null");
         for (Map.Entry<AxialCoord, TerrainType> entry : mapDefinition.entrySet()) {
             hexes.put(entry.getKey(), new Hex(entry.getKey(), entry.getValue()));
         }
    }

    private void generateHexagonalMap(int radius) {
        for (int q = -radius; q <= radius; q++) {
            int r1 = Math.max(-radius, -q - radius);
            int r2 = Math.min(radius, -q + radius);
            for (int r = r1; r <= r2; r++) {
                AxialCoord coord = new AxialCoord(q, r);
                // Default to PLAINS for now, ideally load from scenario
                hexes.put(coord, new Hex(coord, TerrainType.PLAIN));
            }
        }
    }

     // Helper to estimate radius (needed for constructor)
    private int calculateRadiusFromCoords(Collection<AxialCoord> coords) {
        int maxDist = 0;
        AxialCoord center = new AxialCoord(0,0);
        for(AxialCoord coord : coords) {
            maxDist = Math.max(maxDist, center.distanceTo(coord));
        }
        return maxDist;
    }

    /**
     * Gets the Hex object at the specified coordinates.
     * @param coord The AxialCoord of the hex.
     * @return The Hex object, or null if no hex exists at that coordinate.
     */
    public Hex getHex(AxialCoord coord) {
        return hexes.get(coord);
    }

    /**
     * Gets all Hex objects on the board.
     * @return An unmodifiable collection of all Hexes.
     */
    public Collection<Hex> getAllHexes() {
        return Collections.unmodifiableCollection(hexes.values());
    }

    /**
     * Places a unit onto the board at its current position.
     * Updates both the Unit's position and the Hex's occupant.
     * @param unit The unit to place.
     * @param position The position to place the unit at.
     * @return true if placement was successful, false otherwise.
     */
    public boolean placeUnit(Unit unit, AxialCoord position) {
        if (unit == null || position == null) return false;

        Hex targetHex = getHex(position);
        if (targetHex == null || !targetHex.isAccessible() || targetHex.isOccupied()) {
            System.err.println("Placement failed: Target hex invalid, inaccessible or occupied at " + position);
            return false;
        }

        // If unit was already on the board, remove from old hex
        removeUnit(unit);

        targetHex.setUnit(unit);
        unit.setPosition(position);
        return true;
    }

    /**
     * Removes a unit from the board.
     * @param unit The unit to remove.
     */
    public void removeUnit(Unit unit) {
        if (unit == null) return;
        Hex currentHex = getHex(unit.getPosition());
        if (currentHex != null && currentHex.getUnit() == unit) {
            currentHex.setUnit(null);
        }
    }

    /**
     * Moves a unit from one hex to another.
     * @param unit The unit to move.
     * @param newPosition The target position.
     * @return true if the move was reflected on the board, false otherwise.
     */
    public boolean moveUnit(Unit unit, AxialCoord newPosition) {
         if (unit == null || newPosition == null) return false;

         Hex targetHex = getHex(newPosition);
         // Basic check: target exists and is not occupied BY ANOTHER unit
         if (targetHex == null || !targetHex.isAccessible() || (targetHex.isOccupied() && targetHex.getUnit() != unit) ) {
              System.err.println("Board move failed: Target hex invalid or occupied at " + newPosition);
              return false;
         }

         // Remove from old hex
         removeUnit(unit); // Uses unit.getPosition() internally

         // Place in new hex
         targetHex.setUnit(unit);
         unit.setPosition(newPosition); // Update unit's internal state
         return true;
    }

    /**
     * Calculates the movement cost for a specific unit to enter a given hex.
     * @param hex The target Hex.
     * @param unit The unit moving.
     * @return The movement point cost, or a large value if inaccessible.
     */
    public int getMovementCost(Hex hex, Unit unit) {
        if (hex == null || unit == null || !hex.isAccessible()) {
            return 999; // Effectively infinite cost for inaccessible
        }
        return hex.getTerrainType().getMovementCost();
    }

    /**
     * Finds all units on the board belonging to a specific player.
     * @param player The player whose units to find.
     * @return A list of units belonging to the player.
     */
    public List<Unit> getUnitsForPlayer(Player player) {
        return hexes.values().stream()
                .map(Hex::getUnit)
                .filter(unit -> unit != null && unit.getOwner().equals(player))
                .collect(Collectors.toList());
    }

     /**
     * Finds all units currently on the board.
     * @return A list of all units.
     */
     public List<Unit> getAllUnits() {
        return hexes.values().stream()
                .map(Hex::getUnit)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
     }

    public int getMapRadius() {
        return mapRadius;
    }

     public Map<AxialCoord, Hex> getHexesMap() {
         return Collections.unmodifiableMap(hexes);
     }

    @Override
    public String toString() {
        return "Board [Radius=" + mapRadius + ", Hexes=" + hexes.size() + "]";
    }
}
