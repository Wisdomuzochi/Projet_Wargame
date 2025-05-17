package com.yourusername.wargame;

import com.yourusername.wargame.ui.GameFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Main entry point for the Wargame application.
 */
public class Main {

    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ensure UI creation and manipulation happens on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            System.out.println("Starting Wargame Application...");
            GameFrame frame = new GameFrame();
        });
    }
}
