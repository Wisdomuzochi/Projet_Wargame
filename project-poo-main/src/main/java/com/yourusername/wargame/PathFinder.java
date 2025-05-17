package com.yourusername.wargame;

import com.yourusername.wargame.engine.AxialCoord;
import com.yourusername.wargame.engine.Board;
import com.yourusername.wargame.engine.Hex;
import com.yourusername.wargame.model.unit.Unit;

import java.io.Serializable;
import java.util.*;

/**
 * Finds paths for units on the game board using the A* algorithm.
 * Considers terrain movement costs and obstacles.
 */
public class PathFinder implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Helper class to store node information for A*
    private static class Node implements Comparable<Node> {
        AxialCoord coord;
        Node parent;
        double gCost; // Cost from start to this node
        double hCost; // Heuristic cost from this node to end
        double fCost; // gCost + hCost

        Node(AxialCoord coord, Node parent, double gCost, double hCost) {
            this.coord = coord;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.fCost, other.fCost); // PriorityQueue needs comparison
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(coord, node.coord);
        }

        @Override
        public int hashCode() {
            return Objects.hash(coord);
        }
    }

    /**
     * Finds the lowest-cost path for a unit from start to end coordinates.
     * Uses the A* algorithm.
     * @param unit The unit moving (used for movement cost calculation).
     * @param start The starting coordinate.
     * @param end The target coordinate.
     * @param board The game board.
     * @return A list of AxialCoords representing the path (excluding start, including end),
     *         or an empty list if no path is found.
     */
    public List<AxialCoord> findPath(Unit unit, AxialCoord start, AxialCoord end, Board board) {
        Objects.requireNonNull(unit, "Unit cannot be null");
        Objects.requireNonNull(start, "Start coordinate cannot be null");
        Objects.requireNonNull(end, "End coordinate cannot be null");
        Objects.requireNonNull(board, "Board cannot be null");

        Hex startHex = board.getHex(start);
        Hex endHex = board.getHex(end);

        // Basic checks: start/end exist and end is potentially reachable
        if (startHex == null || endHex == null || !endHex.isAccessible()) {
            System.err.println("Pathfinding: Start or End hex invalid or end inaccessible.");
            return Collections.emptyList();
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Map<AxialCoord, Node> allNodes = new HashMap<>(); // Store nodes to access G-cost easily

        Node startNode = new Node(start, null, 0, heuristic(start, end));
        openSet.add(startNode);
        allNodes.put(start, startNode);

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll(); // Node with lowest F-cost

            // Goal check
            if (currentNode.coord.equals(end)) {
                return reconstructPath(currentNode);
            }

            // Explore all 6 neighbors in hexagonal grid
            for (int i = 0; i < 6; i++) { 
                AxialCoord neighborCoord = currentNode.coord.neighbor(i);
                Hex neighborHex = board.getHex(neighborCoord);

                // Skip invalid or inaccessible hexes
                if (neighborHex == null || !neighborHex.isAccessible()) {
                    continue;
                }

                // Skip hexes occupied by other units (allow moving into the *end* hex if needed)
                Unit occupant = neighborHex.getUnit();
                if (occupant != null && occupant != unit && !neighborCoord.equals(end)) {
                     continue; // Blocked by another unit (not the target hex)
                }

                // Calculate movement cost for this step
                double movementCost = board.getMovementCost(neighborHex, unit);
                if (movementCost >= 999) continue; // Skip effectively impassable terrain

                double tentativeGCost = currentNode.gCost + movementCost;

                Node neighborNode = allNodes.get(neighborCoord);

                // If neighbor not visited or found a cheaper path to it
                if (neighborNode == null || tentativeGCost < neighborNode.gCost) {
                    if (neighborNode == null) {
                        neighborNode = new Node(neighborCoord, currentNode, tentativeGCost, heuristic(neighborCoord, end));
                        allNodes.put(neighborCoord, neighborNode);
                        openSet.add(neighborNode);
                    } else {
                        // Found a cheaper path, update node info
                        openSet.remove(neighborNode); // Remove old entry if present
                        neighborNode.parent = currentNode;
                        neighborNode.gCost = tentativeGCost;
                        neighborNode.fCost = neighborNode.gCost + neighborNode.hCost;
                        openSet.add(neighborNode); // Re-add with updated cost
                    }
                }
            }
        }

        // No path found
        System.out.println("Pathfinding: No path found from " + start + " to " + end);
        return Collections.emptyList();
    }

    /**
     * Heuristic function for A*: distance between two hex coordinates.
     * @param a Starting coordinate.
     * @param b Target coordinate.
     * @return The hex grid distance.
     */
    private double heuristic(AxialCoord a, AxialCoord b) {
        return a.distanceTo(b);
    }

    /**
     * Reconstructs the path from the end node back to the start using parent references.
     * @param endNode The final node reached by A*.
     * @return The path as a list of coordinates (excluding start, including end).
     */
    private List<AxialCoord> reconstructPath(Node endNode) {
        LinkedList<AxialCoord> path = new LinkedList<>();
        Node current = endNode;
        
        while (current != null && current.parent != null) { // Stop when parent is null (start node)
            path.addFirst(current.coord);
            current = current.parent;
        }
        
        return path;
    }
}
