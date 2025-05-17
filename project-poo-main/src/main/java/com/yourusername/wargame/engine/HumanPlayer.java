package com.yourusername.wargame.engine;

/**
 * Concrete implementation of Player representing a human player.
 */
public class HumanPlayer extends Player {
    private static final long serialVersionUID = 1L;
    
    public HumanPlayer(String name, int playerIndex) {
        super(name, playerIndex, false);
    }
    
    @Override
    public void performTurn(GameController controller) {
        // Human players interact through the UI, so this method does nothing
        // The UI calls controller methods directly when the human makes decisions
        System.out.println("Human player " + getName() + " turn started. Waiting for UI input...");
    }
}