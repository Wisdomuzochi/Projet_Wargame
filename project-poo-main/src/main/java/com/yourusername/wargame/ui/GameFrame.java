package com.yourusername.wargame.ui;

import com.yourusername.wargame.*;
import com.yourusername.wargame.engine.*;
import com.yourusername.wargame.model.terrain.TerrainType;
import com.yourusername.wargame.model.unit.Unit;
import com.yourusername.wargame.model.unit.UnitType;
import com.yourusername.wargame.model.unit.UnitPlacement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * The main window for the Wargame application.
 * Contains the game board panel, info panel, menus, and handles user interactions.
 */
public class GameFrame extends JFrame implements GameController.GameEventListener {
    
    private static final long serialVersionUID = 1L;
    
    // Core game components
    private final GameController gameController;
    
    // UI components
    private final BoardPanel boardPanel;
    private final InfoPanel infoPanel;
    
    // UI interaction state
    private AxialCoord selectedHex = null;
    private Unit selectedUnit = null;
    private boolean isHumanTurnActive = false;
    
    /**
     * Creates the main game window.
     */
    public GameFrame() {
        super("Wargame");
        
        // Initialize the game controller
        gameController = new GameController();
        gameController.setGameEventListener(this);
        
        // Set up the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create the UI components
        boardPanel = new BoardPanel(this);
        infoPanel = new InfoPanel(this);
        
        // Add components to the frame
        add(boardPanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.EAST);
        
        // Set up the menu bar
        setupMenuBar();
        
        // Finalize window setup
        pack();
        setLocationRelativeTo(null);  // Center on screen
        setVisible(true);
        
        // We could auto-load a default scenario
        // createDefaultScenario();
    }
    
    /**
     * Sets up the menu bar with all required menu items.
     */
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem newGameItem = new JMenuItem("New Game");
        newGameItem.addActionListener(e -> createDefaultScenario());
        
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(newGameItem);
        fileMenu.addSeparator();
        fileMenu.add(quitItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Wargame - A Battle for Wesnoth-style hexagonal strategy game\n" +
                            "Version 1.0\n\n" +
                            "Controls:\n" +
                            "- Click on your units to select them\n" +
                            "- Click on highlighted hexes to move or attack\n" +
                            "- Use the End Turn button when done",
                    "About Wargame",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        
        helpMenu.add(aboutItem);
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        // Set the menu bar
        setJMenuBar(menuBar);
    }
    
    /**
     * Creates a default scenario for testing.
     */
    private void createDefaultScenario() {
        // Create a board
        Board board = new Board(5);
        
        // Create players
        HumanPlayer humanPlayer = new HumanPlayer("Human Player", 0);
        AiPlayer aiPlayer = new AiPlayer("AI Player", 1);
        List<Player> players = Arrays.asList(humanPlayer, aiPlayer);
        
        // Create victory conditions
        Map<Player, List<VictoryCondition>> victoryConditions = new HashMap<>();
        victoryConditions.put(humanPlayer, Collections.singletonList(new DestroyAllCondition(aiPlayer)));
        victoryConditions.put(aiPlayer, Collections.singletonList(new DestroyAllCondition(humanPlayer)));
        
        // Create game state
        GameState gameState = new GameState(board, players, victoryConditions);
        
        // Create and place units for the human player
        Unit infantry1 = new Unit(UnitType.INFANTRY, humanPlayer, new AxialCoord(-3, 1));
        Unit archer1 = new Unit(UnitType.ARCHER, humanPlayer, new AxialCoord(-3, 0));
        Unit cavalry1 = new Unit(UnitType.CAVALRY, humanPlayer, new AxialCoord(-2, -1));
        
        // Create and place units for the AI player
        Unit infantry2 = new Unit(UnitType.INFANTRY, aiPlayer, new AxialCoord(3, -1));
        Unit archer2 = new Unit(UnitType.ARCHER, aiPlayer, new AxialCoord(3, -2));
        Unit mage2 = new Unit(UnitType.MAGE, aiPlayer, new AxialCoord(2, -3));
        
        // Add units to players
        humanPlayer.addUnit(infantry1);
        humanPlayer.addUnit(archer1);
        humanPlayer.addUnit(cavalry1);
        
        aiPlayer.addUnit(infantry2);
        aiPlayer.addUnit(archer2);
        aiPlayer.addUnit(mage2);
        
        // Place units on board
        board.placeUnit(infantry1, infantry1.getPosition());
        board.placeUnit(archer1, archer1.getPosition());
        board.placeUnit(cavalry1, cavalry1.getPosition());
        
        board.placeUnit(infantry2, infantry2.getPosition());
        board.placeUnit(archer2, archer2.getPosition());
        board.placeUnit(mage2, mage2.getPosition());
        
        // Initialize the game
        gameController.initializeGame(gameState);
        gameController.startGame();
    }
    
    /**
     * Called by BoardPanel when a hex is clicked.
     * Handles unit selection and movement/attack actions.
     */
    public void handleHexClick(AxialCoord coord) {
        if (!isHumanTurnActive || coord == null || gameController.getGameState() == null) {
            return;
        }
        
        GameState gameState = gameController.getGameState();
        Player currentPlayer = gameState.getCurrentPlayer();
        Hex clickedHex = gameState.getBoard().getHex(coord);
        
        if (clickedHex == null) {
            return;
        }
        
        Unit unitOnHex = clickedHex.getUnit();
        
        if (selectedUnit == null) {
            // No unit selected yet, try to select one
            if (unitOnHex != null && unitOnHex.getOwner() == currentPlayer) {
                selectedUnit = unitOnHex;
                selectedHex = coord;
                updateHighlights();
            } else {
                // Just show info for the hex
                selectedHex = coord;
                selectedUnit = null;
                boardPanel.clearMoveAttackHighlights();
            }
        } else {
            // Unit already selected, try to perform action
            if (unitOnHex == null) {
                // Try to move to empty hex
                List<AxialCoord> possibleMoves = gameController.findPossibleMoves(selectedUnit);
                if (possibleMoves.contains(coord)) {
                    gameController.requestMove(currentPlayer, selectedUnit, Collections.singletonList(coord));
                    selectedUnit = null;
                    selectedHex = null;
                    boardPanel.clearAllHighlights();
                }
            } else if (unitOnHex.getOwner() != currentPlayer) {
                // Try to attack enemy unit
                if (selectedUnit.getPosition().distanceTo(coord) <= selectedUnit.getAttackRange()) {
                    gameController.requestAttack(currentPlayer, selectedUnit, unitOnHex);
                    selectedUnit = null;
                    selectedHex = null;
                    boardPanel.clearAllHighlights();
                }
            } else if (unitOnHex.getOwner() == currentPlayer) {
                // Select another friendly unit
                selectedUnit = unitOnHex;
                selectedHex = coord;
                updateHighlights();
            }
        }
        
        // Update UI
        boardPanel.setSelectedHex(selectedHex);
        infoPanel.updateInfo(gameState, selectedHex, selectedUnit);
        boardPanel.repaint();
    }
    
    /**
     * Updates the movement and attack highlights for the selected unit.
     */
    private void updateHighlights() {
        if (selectedUnit != null && gameController.getGameState() != null) {
            List<AxialCoord> possibleMoves = gameController.findPossibleMoves(selectedUnit);
            List<AxialCoord> possibleAttacks = gameController.findPossibleAttacks(selectedUnit);
            
            boardPanel.showMoveHighlights(possibleMoves);
            boardPanel.showAttackHighlights(possibleAttacks);
        } else {
            boardPanel.clearMoveAttackHighlights();
        }
    }
    
    /**
     * Called by InfoPanel when the end turn button is clicked.
     */
    public void requestEndTurn() {
        if (isHumanTurnActive && gameController.getGameState() != null) {
            Player currentPlayer = gameController.getGameState().getCurrentPlayer();
            gameController.endTurn(currentPlayer);
            
            // Clear selection and highlights
            selectedUnit = null;
            selectedHex = null;
            boardPanel.clearAllHighlights();
        }
    }
    
    /**
     * Returns true if it's currently a human player's turn.
     */
    public boolean isPlayerTurn() {
        return isHumanTurnActive;
    }
    
    /**
     * Called by BoardPanel when the mouse hovers over a hex.
     */
    public void handleHexHover(AxialCoord coord) {
        if (gameController.getGameState() != null) {
            infoPanel.updateHoverInfo(gameController.getGameState(), coord);
        }
    }
    
    // --- GameEventListener implementation ---
    
    @Override
    public void onGameStateUpdate(GameState newState) {
        SwingUtilities.invokeLater(() -> {
            boardPanel.setGameState(newState);
            infoPanel.updateInfo(newState, selectedHex, selectedUnit);
        });
    }
    
    @Override
    public void onUnitAction(Unit unit, String actionType) {
        SwingUtilities.invokeLater(() -> {
            GameState state = gameController.getGameState();
            boardPanel.setGameState(state);
            infoPanel.updateInfo(state, selectedHex, selectedUnit);
        });
    }
    
    @Override
    public void onCombatResolved(Unit attacker, Unit defender, int damageDealt, boolean defenderDestroyed) {
        SwingUtilities.invokeLater(() -> {
            GameState state = gameController.getGameState();
            boardPanel.setGameState(state);
            infoPanel.updateInfo(state, selectedHex, selectedUnit);
            
            if (defenderDestroyed && defender == selectedUnit) {
                selectedUnit = null;
                selectedHex = null;
                boardPanel.clearAllHighlights();
            }
        });
    }
    
    @Override
    public void onTurnStart(Player player) {
        SwingUtilities.invokeLater(() -> {
            isHumanTurnActive = !player.isAi();
            
            GameState state = gameController.getGameState();
            infoPanel.updateInfo(state, null, null);
            
            selectedUnit = null;
            selectedHex = null;
            boardPanel.clearAllHighlights();
            
            if (isHumanTurnActive) {
                JOptionPane.showMessageDialog(this,
                        player.getName() + "'s turn!",
                        "Turn Change",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }
    
    @Override
    public void onGameOver(Player winner) {
        SwingUtilities.invokeLater(() -> {
            isHumanTurnActive = false;
            
            JOptionPane.showMessageDialog(this,
                    "Game Over!\nWinner: " + winner.getName(),
                    "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);
            
            selectedUnit = null;
            selectedHex = null;
            boardPanel.clearAllHighlights();
        });
    }
}
