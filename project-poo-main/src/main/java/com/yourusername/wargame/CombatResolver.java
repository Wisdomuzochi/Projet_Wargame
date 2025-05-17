package com.yourusername.wargame;

import com.yourusername.wargame.engine.GameState;
import com.yourusername.wargame.model.terrain.TerrainType;
import com.yourusername.wargame.model.unit.Unit;

import java.io.Serializable;
import java.util.Random;

/**
 * Resolves combat between units.
 */
public class CombatResolver implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // For randomized combat outcomes
    private final Random random;
    
    /**
     * Creates a new CombatResolver with the provided random generator.
     * @param random The random generator to use.
     */
    public CombatResolver(Random random) {
        this.random = random;
    }
    
    /**
     * Resolves combat between attacker and defender units.
     * @param attacker The attacking unit.
     * @param defender The defending unit.
     * @param defenderTerrain The terrain the defender is on, for defense bonuses.
     * @param gameState The current game state.
     * @return True if the defender was destroyed, false otherwise.
     */
    public boolean resolveCombat(Unit attacker, Unit defender, TerrainType defenderTerrain, GameState gameState) {
        if (attacker == null || defender == null) {
            throw new IllegalArgumentException("Combat units cannot be null");
        }
        
        System.out.println("--== COMBAT ==--");
        System.out.println(attacker.getTypeName() + " (HP:" + attacker.getCurrentHitPoints() + 
                           ") attacks " + defender.getTypeName() + " (HP:" + defender.getCurrentHitPoints() + ")");
                
        // Calculate attack damage
        int baseAttack = attacker.getBaseAttack();
        int attackModifier = attackModifier(attacker);
        int totalAttack = baseAttack + attackModifier;
        
        // Calculate defense
        int baseDefense = defender.getBaseDefense();
        double terrainBonus = defenderTerrain.getDefenseBonus(); 
        int defenseModifier = defenseModifier(defender);
        double totalDefense = baseDefense * (1 + terrainBonus) + defenseModifier;
        
        // Calculate damage
        int damage = calculateDamage(totalAttack, (int)totalDefense);
        
        System.out.println("Attack: " + totalAttack + " vs Defense: " + (int)totalDefense);
        System.out.println("Damage: " + damage);
        
        // Apply damage
        boolean survived = defender.takeDamage(damage);
        
        // Execute counterattack if defender survived and in range
        if (survived && isInCounterAttackRange(attacker, defender)) {
            System.out.println("--== COUNTER-ATTACK ==--");
            executeCounterAttack(attacker, defender, gameState);
        }
        
        return !survived;
    }
    
    /**
     * Checks if defender is in range to counter-attack.
     * @param attacker The attacking unit.
     * @param defender The defending unit.
     * @return True if the defender can counter-attack.
     */
    private boolean isInCounterAttackRange(Unit attacker, Unit defender) {
        int distance = attacker.getPosition().distanceTo(defender.getPosition());
        return distance <= defender.getAttackRange();
    }
    
    /**
     * Executes a counter-attack from defender to attacker.
     * @param attacker The original attacker (now being counter-attacked).
     * @param defender The original defender (now counter-attacking).
     * @param gameState The current game state.
     */
    private void executeCounterAttack(Unit attacker, Unit defender, GameState gameState) {
        // Calculate counter-attack damage (reduced effectiveness)
        int baseCounterAttack = (int)(defender.getBaseAttack() * 0.7); // Counter-attacks are weaker
        int counterAttackModifier = attackModifier(defender);
        int totalCounterAttack = baseCounterAttack + counterAttackModifier;
        
        // Calculate defense against counter
        int baseDefense = attacker.getBaseDefense();
        int defenseModifier = defenseModifier(attacker);
        double totalDefense = baseDefense + defenseModifier;
        
        // Calculate damage
        int damage = calculateDamage(totalCounterAttack, (int)totalDefense);
        
        System.out.println("Counter-Attack: " + totalCounterAttack + " vs Defense: " + (int)totalDefense);
        System.out.println("Counter-Damage: " + damage);
        
        // Apply damage
        attacker.takeDamage(damage);
        
        // Note: No further counter-counters
    }
    
    /**
     * Calculate attack modifiers for a unit. Could be expanded with status effects.
     * @param unit The unit attacking.
     * @return An attack modifier value.
     */
    private int attackModifier(Unit unit) {
        // Could check for status effects, equipment, etc.
        return 0;
    }
    
    /**
     * Calculate defense modifiers for a unit. Could be expanded with status effects.
     * @param unit The unit defending.
     * @return A defense modifier value.
     */
    private int defenseModifier(Unit unit) {
        // Could check for status effects, equipment, etc.
        return 0;
    }
    
    /**
     * Calculate damage based on attack and defense values.
     * @param attack Attack value.
     * @param defense Defense value.
     * @return Calculated damage value.
     */
    private int calculateDamage(int attack, int defense) {
        // Basic formula with randomization: attack - defense with some variance
        int baseDamage = Math.max(0, attack - defense);
        
        // Add some randomness (0.8 - 1.2 multiplier)
        double randomFactor = 0.8 + (random.nextDouble() * 0.4);
        int finalDamage = (int) Math.round(baseDamage * randomFactor);
        
        // Ensure minimum damage for successful hits
        if (attack > defense && finalDamage < 1) {
            finalDamage = 1;
        }
        
        return finalDamage;
    }
}
