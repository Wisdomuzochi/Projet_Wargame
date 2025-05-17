package com.yourusername.wargame.ui;

import com.yourusername.wargame.engine.*;
import com.yourusername.wargame.model.terrain.TerrainType;
import com.yourusername.wargame.model.unit.Unit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

/**
 * Panel that displays game information and controls.
 */
public class InfoPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private static final int PANEL_WIDTH = 250;
    private static final int PANEL_HEIGHT = 600;
    
    // Reference to the parent frame
    private final GameFrame gameFrame;
    
    // UI components
    private JLabel turnLabel;
    private JLabel playerLabel;
    private JButton endTurnButton;
    
    private JPanel unitInfoPanel;
    private JPanel terrainInfoPanel;
    private JPanel hoverInfoPanel;
    
    /**
     * Creates a new info panel.
     * @param gameFrame The parent game frame.
     */
    public InfoPanel(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(new Color(230, 230, 230));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        createComponents();
    }
    
    /**
     * Creates and arranges all the UI components.
     */
    private void createComponents() {
        // Game state section
        JPanel gameStatePanel = new JPanel();
        gameStatePanel.setLayout(new BoxLayout(gameStatePanel, BoxLayout.Y_AXIS));
        gameStatePanel.setBackground(getBackground());
        gameStatePanel.setBorder(BorderFactory.createTitledBorder("Game Status"));
        
        turnLabel = new JLabel("Turn: -");
        playerLabel = new JLabel("Player: -");
        
        endTurnButton = new JButton("End Turn");
        endTurnButton.setEnabled(false);
        endTurnButton.addActionListener(e -> gameFrame.requestEndTurn());
        
        gameStatePanel.add(turnLabel);
        gameStatePanel.add(Box.createVerticalStrut(5));
        gameStatePanel.add(playerLabel);
        gameStatePanel.add(Box.createVerticalStrut(10));
        gameStatePanel.add(endTurnButton);
        
        // Unit info section
        unitInfoPanel = new JPanel();
        unitInfoPanel.setLayout(new BoxLayout(unitInfoPanel, BoxLayout.Y_AXIS));
        unitInfoPanel.setBorder(BorderFactory.createTitledBorder("Selected Unit"));
        unitInfoPanel.setBackground(getBackground());
        
        JLabel unitPlaceholder = new JLabel("No unit selected");
        unitInfoPanel.add(unitPlaceholder);
        
        // Terrain info section
        terrainInfoPanel = new JPanel();
        terrainInfoPanel.setLayout(new BoxLayout(terrainInfoPanel, BoxLayout.Y_AXIS));
        terrainInfoPanel.setBorder(BorderFactory.createTitledBorder("Terrain"));
        terrainInfoPanel.setBackground(getBackground());
        
        JLabel terrainPlaceholder = new JLabel("No terrain selected");
        terrainInfoPanel.add(terrainPlaceholder);
        
        // Hover info section
        hoverInfoPanel = new JPanel();
        hoverInfoPanel.setLayout(new BoxLayout(hoverInfoPanel, BoxLayout.Y_AXIS));
        hoverInfoPanel.setBorder(BorderFactory.createTitledBorder("Hover Info"));
        hoverInfoPanel.setBackground(getBackground());
        
        JLabel hoverPlaceholder = new JLabel("Hover over a hex for info");
        hoverInfoPanel.add(hoverPlaceholder);
        
        // Add all sections to the panel
        add(gameStatePanel);
        add(Box.createVerticalStrut(10));
        add(unitInfoPanel);
        add(Box.createVerticalStrut(10));
        add(terrainInfoPanel);
        add(Box.createVerticalStrut(10));
        add(hoverInfoPanel);
        add(Box.createVerticalGlue()); // Push everything up
    }
    
    /**
     * Updates the information displayed in the panel.
     * @param gameState The current game state.
     * @param selectedHex The currently selected hex.
     * @param selectedUnit The currently selected unit.
     */
    public void updateInfo(GameState gameState, AxialCoord selectedHex, Unit selectedUnit) {
        if (gameState == null) {
            turnLabel.setText("Turn: -");
            playerLabel.setText("Player: -");
            endTurnButton.setEnabled(false);
            clearUnitInfo();
            clearTerrainInfo();
            return;
        }
        
        // Update game state info
        turnLabel.setText("Turn: " + gameState.getCurrentTurnNumber());
        Player currentPlayer = gameState.getCurrentPlayer();
        playerLabel.setText("Player: " + currentPlayer.getName());
        
        // Only enable the end turn button for the human player's turn
        endTurnButton.setEnabled(gameFrame.isPlayerTurn());
        
        // Update unit info if a unit is selected
        if (selectedUnit != null) {
            updateUnitInfo(selectedUnit);
        } else {
            clearUnitInfo();
        }
        
        // Update terrain info if a hex is selected
        if (selectedHex != null && gameState.getBoard() != null) {
            Hex hex = gameState.getBoard().getHex(selectedHex);
            if (hex != null) {
                updateTerrainInfo(hex.getTerrainType());
            } else {
                clearTerrainInfo();
            }
        } else {
            clearTerrainInfo();
        }
    }
    
    /**
     * Updates the hover information for the hovered hex.
     * @param gameState The current game state.
     * @param hoveredHex The currently hovered hex.
     */
    public void updateHoverInfo(GameState gameState, AxialCoord hoveredHex) {
        hoverInfoPanel.removeAll();
        
        if (hoveredHex != null && gameState != null && gameState.getBoard() != null) {
            Hex hex = gameState.getBoard().getHex(hoveredHex);
            if (hex != null) {
                JLabel coordLabel = new JLabel("Coord: " + hoveredHex.getQ() + ", " + hoveredHex.getR());
                JLabel terrainLabel = new JLabel("Terrain: " + hex.getTerrainType().getDisplayName());
                hoverInfoPanel.add(coordLabel);
                hoverInfoPanel.add(terrainLabel);
                
                if (hex.isOccupied()) {
                    Unit unit = hex.getUnit();
                    JLabel unitLabel = new JLabel("Unit: " + unit.getTypeName());
                    JLabel ownerLabel = new JLabel("Owner: " + unit.getOwner().getName());
                    JLabel healthLabel = new JLabel("Health: " + unit.getCurrentHitPoints() + "/" + unit.getMaxHitPoints());
                    
                    hoverInfoPanel.add(unitLabel);
                    hoverInfoPanel.add(ownerLabel);
                    hoverInfoPanel.add(healthLabel);
                } else {
                    hoverInfoPanel.add(new JLabel("Unit: None"));
                }
            } else {
                hoverInfoPanel.add(new JLabel("Hover over a hex for info"));
            }
        } else {
            hoverInfoPanel.add(new JLabel("Hover over a hex for info"));
        }
        
        hoverInfoPanel.revalidate();
        hoverInfoPanel.repaint();
    }
    
    /**
     * Updates the unit information section.
     * @param unit The unit to display information for.
     */
    private void updateUnitInfo(Unit unit) {
        unitInfoPanel.removeAll();
        
        // Format unit info
        JLabel nameLabel = new JLabel("Type: " + unit.getTypeName());
        JLabel ownerLabel = new JLabel("Owner: " + unit.getOwner().getName());
        
        DecimalFormat df = new DecimalFormat("0.#");
        JLabel attackLabel = new JLabel("Attack: " + unit.getBaseAttack());
        JLabel defenseLabel = new JLabel("Defense: " + unit.getBaseDefense());
        JLabel healthLabel = new JLabel("Health: " + unit.getCurrentHitPoints() + "/" + unit.getMaxHitPoints());
        JLabel moveLabel = new JLabel("Movement: " + unit.getCurrentMovementPoints() + "/" + unit.getMaxMovementPoints());
        JLabel rangeLabel = new JLabel("Attack Range: " + unit.getAttackRange());
        
        // Status info
        String status = "Ready";
        if (unit.hasAttackedThisTurn() && unit.hasMovedThisTurn()) {
            status = "Done for this turn";
        } else if (unit.hasAttackedThisTurn()) {
            status = "Has attacked";
        } else if (unit.hasMovedThisTurn()) {
            status = "Has moved";
        }
        JLabel statusLabel = new JLabel("Status: " + status);
        
        // Add all labels to the panel
        unitInfoPanel.add(nameLabel);
        unitInfoPanel.add(ownerLabel);
        unitInfoPanel.add(healthLabel);
        unitInfoPanel.add(attackLabel);
        unitInfoPanel.add(defenseLabel);
        unitInfoPanel.add(moveLabel);
        unitInfoPanel.add(rangeLabel);
        unitInfoPanel.add(statusLabel);
        
        unitInfoPanel.revalidate();
        unitInfoPanel.repaint();
    }
    
    /**
     * Updates the terrain information section.
     * @param terrainType The terrain type to display information for.
     */
    private void updateTerrainInfo(TerrainType terrainType) {
        terrainInfoPanel.removeAll();
        
        JLabel nameLabel = new JLabel("Type: " + terrainType.getDisplayName());
        JLabel moveCostLabel = new JLabel("Movement Cost: " + terrainType.getMovementCost());
        
        DecimalFormat df = new DecimalFormat("0.#");
        String bonus = df.format(terrainType.getDefenseBonus() * 100);
        JLabel defenseLabel = new JLabel("Defense Bonus: " + bonus + "%");
        
        JLabel accessLabel = new JLabel("Accessible: " + (terrainType.isAccessible() ? "Yes" : "No"));
        
        terrainInfoPanel.add(nameLabel);
        terrainInfoPanel.add(moveCostLabel);
        terrainInfoPanel.add(defenseLabel);
        terrainInfoPanel.add(accessLabel);
        
        terrainInfoPanel.revalidate();
        terrainInfoPanel.repaint();
    }
    
    /**
     * Clears the unit information section.
     */
    private void clearUnitInfo() {
        unitInfoPanel.removeAll();
        unitInfoPanel.add(new JLabel("No unit selected"));
        unitInfoPanel.revalidate();
        unitInfoPanel.repaint();
    }
    
    /**
     * Clears the terrain information section.
     */
    private void clearTerrainInfo() {
        terrainInfoPanel.removeAll();
        terrainInfoPanel.add(new JLabel("No terrain selected"));
        terrainInfoPanel.revalidate();
        terrainInfoPanel.repaint();
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PANEL_WIDTH, PANEL_HEIGHT);
    }
}