package com.yourusername.wargame.engine;

import com.yourusername.wargame.model.unit.Unit;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Concrete implementation of Player representing an AI opponent.
 */
public class AiPlayer extends Player {
    private static final long serialVersionUID = 1L;

    public AiPlayer(String name, int playerIndex) {
        super(name, playerIndex, true);
    }

    @Override
    public void performTurn(GameController controller) {
        System.out.println("AI Player " + getName() + " is thinking...");

        // Get current game state (needed for board, enemy units etc.)
        GameState gameState = controller.getGameState();
        List<Unit> myActiveUnits = getActiveUnits();
        Player humanPlayer = gameState.getOpponent(this);

        // Very simple AI: Iterate through units and perform the first valid attack or move.
        for (Unit unit : myActiveUnits) {
            if (!unit.canAct()) { // Skip unit if it already acted
                 continue;
            }

            // 1. Try to Attack
            Optional<Unit> target = findBestTargetInRange(unit, humanPlayer.getActiveUnits(), gameState.getBoard());
            if (target.isPresent() && unit.canAttack()) {
                System.out.println("AI Unit " + unit.getId().toString().substring(0,4) + " attacking " + target.get().getId().toString().substring(0,4));
                controller.requestAttack(this, unit, target.get());
                unit.setHasAttackedThisTurn(true); // Mark as acted
                continue; // Move to next unit
            }

            // 2. If no attack possible/made, try to Move towards nearest enemy
            if (unit.canMove()) {
                Optional<Unit> nearestEnemy = findNearestEnemy(unit, humanPlayer.getActiveUnits());
                if (nearestEnemy.isPresent()) {
                    AxialCoord targetCoord = nearestEnemy.get().getPosition();
                    AxialCoord bestStep = findBestStepTowards(unit.getPosition(), targetCoord, gameState.getBoard(), unit);
                    if (bestStep != null && !bestStep.equals(unit.getPosition())) {
                        System.out.println("AI Unit " + unit.getId().toString().substring(0,4) + " moving towards " + targetCoord + " via " + bestStep);
                        List<AxialCoord> path = Collections.singletonList(bestStep);
                        controller.requestMove(this, unit, path);
                        unit.setHasMovedThisTurn(true); // Mark as acted
                        continue; // Move to next unit
                    }
                }
            }

            // If unit did nothing
            System.out.println("AI Unit " + unit.getId().toString().substring(0,4) + " couldn't act or chose not to.");
        }

        System.out.println("AI Player " + getName() + " finished turn.");
        controller.endTurn(this); // Signal end of turn to controller
    }

    // --- AI Helper Methods ---

    private Optional<Unit> findBestTargetInRange(Unit attacker, List<Unit> enemies, Board board) {
        Unit bestTarget = null;
        int minHp = Integer.MAX_VALUE;

        for (Unit enemy : enemies) {
            int distance = attacker.getPosition().distanceTo(enemy.getPosition());
            if (distance <= attacker.getAttackRange()) {
                // Basic targeting: prefer weakest target in range
                if (enemy.getCurrentHitPoints() < minHp) {
                    minHp = enemy.getCurrentHitPoints();
                    bestTarget = enemy;
                }
            }
        }
        return Optional.ofNullable(bestTarget);
    }

    private Optional<Unit> findNearestEnemy(Unit unit, List<Unit> enemies) {
        Unit nearest = null;
        int minDist = Integer.MAX_VALUE;

        for (Unit enemy : enemies) {
            int dist = unit.getPosition().distanceTo(enemy.getPosition());
            if (dist < minDist) {
                minDist = dist;
                nearest = enemy;
            }
        }
        return Optional.ofNullable(nearest);
    }

     private AxialCoord findBestStepTowards(AxialCoord start, AxialCoord target, Board board, Unit unit) {
        AxialCoord bestStep = start; // Stay put if no better option
        int minDist = start.distanceTo(target);

        for (int i = 0; i < 6; i++) { // Check all 6 neighbors
            AxialCoord neighbor = start.neighbor(i);
            Hex neighborHex = board.getHex(neighbor);

            if (neighborHex != null && neighborHex.isAccessible() && !neighborHex.isOccupied()) {
                int moveCost = board.getMovementCost(neighborHex, unit);
                if(unit.getCurrentMovementPoints() >= moveCost) {
                    int dist = neighbor.distanceTo(target);
                    if (dist < minDist) {
                        minDist = dist;
                        bestStep = neighbor;
                    }
                }
            }
        }
        // If bestStep is still start, it means no valid closer hex found or affordable
        if(bestStep.equals(start)) return null;
        return bestStep;
    }
}
