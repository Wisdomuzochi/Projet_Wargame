package com.yourusername.wargame.engine;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents Axial coordinates (q, r) for a hexagonal grid.
 * Immutable class.
 * See https://www.redblobgames.com/grids/hexagons/ for coordinate systems.
 */
public class AxialCoord implements Serializable {
    private static final long serialVersionUID = 1L; // For serialization

    private final int q; // Column coordinate
    private final int r; // Row coordinate
    // s coordinate is implicitly defined: q + r + s = 0

    public AxialCoord(int q, int r) {
        this.q = q;
        this.r = r;
    }

    public int getQ() {
        return q;
    }

    public int getR() {
        return r;
    }

    // Calculate implicit s coordinate
    public int getS() {
        return -q - r;
    }

    /**
     * Calculates the distance between this coordinate and another axial coordinate.
     * Uses the standard hexagonal grid distance formula.
     * distance = (abs(q1 - q2) + abs(r1 - r2) + abs(s1 - s2)) / 2
     * @param other The other coordinate.
     * @return The distance in number of hexes.
     */
    public int distanceTo(AxialCoord other) {
        Objects.requireNonNull(other, "Other coordinate cannot be null");
        int dq = Math.abs(this.q - other.q);
        int dr = Math.abs(this.r - other.r);
        int ds = Math.abs(this.getS() - other.getS());
        return (dq + dr + ds) / 2;
    }

    /**
     * Calculates the coordinate of a neighbor in a given direction.
     * Directions: 0: (+1, 0), 1: (0, +1), 2: (-1, +1), 3: (-1, 0), 4: (0, -1), 5: (+1, -1)
     * @param direction The direction index (0 to 5).
     * @return The neighboring AxialCoord.
     * @throws IllegalArgumentException if direction is invalid.
     */
    public AxialCoord neighbor(int direction) {
        // Axial directions array: [ [q, r], ... ]
        int[][] directions = {
            {+1, 0}, {0, +1}, {-1, +1},
            {-1, 0}, {0, -1}, {+1, -1}
        };
        if (direction < 0 || direction >= directions.length) {
            throw new IllegalArgumentException("Invalid direction: " + direction);
        }
        int[] d = directions[direction];
        return new AxialCoord(this.q + d[0], this.r + d[1]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AxialCoord that = (AxialCoord) o;
        return q == that.q && r == that.r;
    }

    @Override
    public int hashCode() {
        return Objects.hash(q, r);
    }

    @Override
    public String toString() {
        return "AxialCoord(" + q + ", " + r + ")";
    }
}
