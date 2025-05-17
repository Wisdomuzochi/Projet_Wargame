package com.yourusername.wargame.ui;

import com.yourusername.wargame.engine.*;
import com.yourusername.wargame.model.terrain.TerrainType;
import com.yourusername.wargame.model.unit.Unit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * Panel that renders the hexagonal game board and handles mouse interactions.
 */
public class BoardPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    // Constants for rendering
    private static final int HEX_SIZE = 40; // Size of hexagon (distance from center to corner)
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;
    
    // Reference to the parent frame
    private final GameFrame gameFrame;
    
    // Game state reference
    private GameState gameState;
    
    // UI state
    private AxialCoord selectedHex;
    private Map<AxialCoord, Point> hexToPixel = new HashMap<>(); // Cache for hex center points
    private List<AxialCoord> moveHighlights = new ArrayList<>();
    private List<AxialCoord> attackHighlights = new ArrayList<>();
    
    /**
     * Creates a new board panel.
     * @param gameFrame The parent game frame.
     */
    public BoardPanel(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.DARK_GRAY);
        
        // Add mouse listeners
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                AxialCoord clickedCoord = pixelToAxial(e.getX(), e.getY());
                gameFrame.handleHexClick(clickedCoord);
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                AxialCoord hoverCoord = pixelToAxial(e.getX(), e.getY());
                gameFrame.handleHexHover(hoverCoord);
            }
        });
    }
    
    /**
     * Sets the current game state and refreshes the board.
     * @param gameState The new game state.
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        calculateHexPositions();
        repaint();
    }
    
    /**
     * Sets the currently selected hex.
     * @param coord The selected hex coordinates.
     */
    public void setSelectedHex(AxialCoord coord) {
        this.selectedHex = coord;
        repaint();
    }
    
    /**
     * Shows movement highlights for hexes.
     * @param coords The coordinates to highlight for movement.
     */
    public void showMoveHighlights(List<AxialCoord> coords) {
        this.moveHighlights = coords != null ? new ArrayList<>(coords) : new ArrayList<>();
        repaint();
    }
    
    /**
     * Shows attack highlights for hexes.
     * @param coords The coordinates to highlight for attack.
     */
    public void showAttackHighlights(List<AxialCoord> coords) {
        this.attackHighlights = coords != null ? new ArrayList<>(coords) : new ArrayList<>();
        repaint();
    }
    
    /**
     * Clears movement and attack highlights.
     */
    public void clearMoveAttackHighlights() {
        moveHighlights.clear();
        attackHighlights.clear();
        repaint();
    }
    
    /**
     * Clears all highlights including the selection.
     */
    public void clearAllHighlights() {
        selectedHex = null;
        moveHighlights.clear();
        attackHighlights.clear();
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (gameState == null) {
            drawInstructions(g2d);
            return;
        }
        
        Board board = gameState.getBoard();
        
        // Draw the highlights first (underneath)
        drawHighlights(g2d);
        
        // Draw all hexes
        for (Hex hex : board.getAllHexes()) {
            drawHex(g2d, hex);
        }
        
        // Draw units
        for (Hex hex : board.getAllHexes()) {
            if (hex.isOccupied()) {
                drawUnit(g2d, hex.getUnit(), hex.getCoordinates());
            }
        }
        
        // Draw selection highlight last (on top)
        if (selectedHex != null) {
            drawSelectionHighlight(g2d, selectedHex);
        }
    }
    
    /**
     * Calculates the pixel positions of all hexes on the board.
     */
    private void calculateHexPositions() {
        if (gameState == null) return;
        
        hexToPixel.clear();
        Board board = gameState.getBoard();
        
        // Calculate center of panel
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        
        // Calculate hex centers
        for (Hex hex : board.getAllHexes()) {
            AxialCoord coord = hex.getCoordinates();
            Point center = axialToPixel(coord.getQ(), coord.getR(), centerX, centerY);
            hexToPixel.put(coord, center);
        }
    }
    
    /**
     * Converts axial coordinates to pixel coordinates.
     */
    private Point axialToPixel(int q, int r, int centerX, int centerY) {
        double x = centerX + HEX_SIZE * 1.5 * q;
        double y = centerY + HEX_SIZE * (Math.sqrt(3)/2 * q + Math.sqrt(3) * r);
        return new Point((int)x, (int)y);
    }
    
    /**
     * Converts pixel coordinates to axial coordinates.
     */
    private AxialCoord pixelToAxial(int x, int y) {
        if (gameState == null) return null;
        
        // Find closest hex center
        double minDist = Double.MAX_VALUE;
        AxialCoord closest = null;
        
        for (Map.Entry<AxialCoord, Point> entry : hexToPixel.entrySet()) {
            Point center = entry.getValue();
            double dist = Math.sqrt(Math.pow(x - center.x, 2) + Math.pow(y - center.y, 2));
            
            if (dist < minDist && dist <= HEX_SIZE) {
                minDist = dist;
                closest = entry.getKey();
            }
        }
        
        return closest;
    }
    
    /**
     * Draws a hexagon for the given hex.
     */
    private void drawHex(Graphics2D g2d, Hex hex) {
        AxialCoord coord = hex.getCoordinates();
        Point center = hexToPixel.get(coord);
        
        if (center == null) return;
        
        // Create hexagon shape
        Polygon hexagon = createHexagonShape(center.x, center.y);
        
        // Fill with terrain color
        g2d.setColor(getTerrainColor(hex.getTerrainType()));
        g2d.fill(hexagon);
        
        // Draw border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.draw(hexagon);
        
        // Draw coordinates (optional, for debugging)
        // g2d.setColor(Color.BLACK);
        // g2d.drawString(coord.getQ() + "," + coord.getR(), center.x - 12, center.y);
    }
    
    /**
     * Draws a unit on the given hex.
     */
    private void drawUnit(Graphics2D g2d, Unit unit, AxialCoord coord) {
        Point center = hexToPixel.get(coord);
        if (center == null) return;
        
        // Determine color based on player
        Color unitColor;
        if (unit.getOwner().getPlayerIndex() == 0) {
            unitColor = Color.BLUE; // Player 1
        } else {
            unitColor = Color.RED; // Player 2 (or others)
        }
        
        // Draw unit circle
        int unitSize = HEX_SIZE * 2 / 3;
        g2d.setColor(unitColor);
        g2d.fillOval(center.x - unitSize/2, center.y - unitSize/2, unitSize, unitSize);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(center.x - unitSize/2, center.y - unitSize/2, unitSize, unitSize);
        
        // Draw unit type letter
        g2d.setColor(Color.WHITE);
        String typeInitial = unit.getTypeName().substring(0, 1);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(typeInitial);
        g2d.drawString(typeInitial, center.x - textWidth/2, center.y + fm.getAscent()/2);
        
        // Draw health bar
        drawHealthBar(g2d, unit, center.x, center.y + unitSize/2 + 5);
    }
    
    /**
     * Draws a health bar for a unit.
     */
    private void drawHealthBar(Graphics2D g2d, Unit unit, int centerX, int centerY) {
        int barWidth = HEX_SIZE;
        int barHeight = 4;
        
        int currentHP = unit.getCurrentHitPoints();
        int maxHP = unit.getMaxHitPoints();
        int filledWidth = (int)(((double)currentHP / maxHP) * barWidth);
        
        // Draw background
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(centerX - barWidth/2, centerY, barWidth, barHeight);
        
        // Draw filled portion
        Color healthColor;
        if (currentHP < maxHP / 3) {
            healthColor = Color.RED;
        } else if (currentHP < maxHP * 2 / 3) {
            healthColor = Color.YELLOW;
        } else {
            healthColor = Color.GREEN;
        }
        
        g2d.setColor(healthColor);
        g2d.fillRect(centerX - barWidth/2, centerY, filledWidth, barHeight);
        
        // Draw border
        g2d.setColor(Color.BLACK);
        g2d.drawRect(centerX - barWidth/2, centerY, barWidth, barHeight);
    }
    
    /**
     * Draws highlights for move and attack hexes.
     */
    private void drawHighlights(Graphics2D g2d) {
        // Draw move highlights
        for (AxialCoord coord : moveHighlights) {
            Point center = hexToPixel.get(coord);
            if (center != null) {
                Polygon hex = createHexagonShape(center.x, center.y);
                g2d.setColor(new Color(0, 0, 255, 64)); // Semi-transparent blue
                g2d.fill(hex);
            }
        }
        
        // Draw attack highlights
        for (AxialCoord coord : attackHighlights) {
            Point center = hexToPixel.get(coord);
            if (center != null) {
                Polygon hex = createHexagonShape(center.x, center.y);
                g2d.setColor(new Color(255, 0, 0, 64)); // Semi-transparent red
                g2d.fill(hex);
            }
        }
    }
    
    /**
     * Draws a selection highlight around a hex.
     */
    private void drawSelectionHighlight(Graphics2D g2d, AxialCoord coord) {
        Point center = hexToPixel.get(coord);
        if (center == null) return;
        
        Polygon hex = createHexagonShape(center.x, center.y);
        g2d.setColor(Color.YELLOW);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(hex);
    }
    
    /**
     * Creates a hexagon shape centered at the given point.
     */
    private Polygon createHexagonShape(int centerX, int centerY) {
        Polygon hexagon = new Polygon();
        
        for (int i = 0; i < 6; i++) {
            double angle = 2 * Math.PI / 6 * i;
            int x = (int)(centerX + HEX_SIZE * Math.cos(angle));
            int y = (int)(centerY + HEX_SIZE * Math.sin(angle));
            hexagon.addPoint(x, y);
        }
        
        return hexagon;
    }
    
    /**
     * Returns a color for the given terrain type.
     */
    private Color getTerrainColor(TerrainType terrain) {
        switch (terrain) {
            case PLAIN: return new Color(200, 255, 200); // Light green
            case FOREST: return new Color(34, 139, 34);  // Forest green
            case HILLS: return new Color(205, 133, 63);  // Peru/brown
            case MOUNTAIN: return new Color(128, 128, 128); // Gray
            case VILLAGE: return new Color(255, 215, 0);  // Gold
            case FORTRESS: return new Color(169, 169, 169); // Dark gray
            case DEEP_WATER: return new Color(30, 144, 255); // Blue
            default: return Color.WHITE;
        }
    }
    
    /**
     * Draws instructions when no game is active.
     */
    private void drawInstructions(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2d.drawString("Welcome to Wargame!", 300, 250);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g2d.drawString("To start a new game, go to File > New Game", 260, 280);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PANEL_WIDTH, PANEL_HEIGHT);
    }
}