package com.pecodigos.dbarena.ingame.services;

import com.pecodigos.dbarena.ingame.enums.energy.EnergyType;
import com.pecodigos.dbarena.ingame.models.BattlePlayer;
import org.springframework.stereotype.Service;

@Service
public class EnergyService {

    // Add energy to a BattlePlayer's energy map
    public void addEnergy(BattlePlayer battlePlayer, EnergyType energyType, int amount) {
        // Ensure energyType is valid, then add energy
        if (battlePlayer == null || energyType == null) {
            throw new IllegalArgumentException("BattlePlayer or EnergyType cannot be null.");
        }

        // Add the energy to the player's map (or initialize to amount if it's 0)
        battlePlayer.getEnergy().put(energyType, battlePlayer.getEnergy().getOrDefault(energyType, 0) + amount);
    }

    // Subtract energy from a BattlePlayer's energy map
    public void subtractEnergy(BattlePlayer battlePlayer, EnergyType energyType, int amount) {
        // Ensure energyType is valid, then subtract energy
        if (battlePlayer == null || energyType == null) {
            throw new IllegalArgumentException("BattlePlayer or EnergyType cannot be null.");
        }

        // Retrieve the current energy level
        int currentEnergy = battlePlayer.getEnergy().getOrDefault(energyType, 0);

        // If there's enough energy, subtract the specified amount
        if (currentEnergy >= amount) {
            battlePlayer.getEnergy().put(energyType, currentEnergy - amount);
        } else {
            throw new IllegalStateException("Not enough energy of type: " + energyType);
        }
    }

    // Get the current energy for a specific energy type
    public int getEnergy(BattlePlayer battlePlayer, EnergyType energyType) {
        // Ensure battlePlayer and energyType are valid
        if (battlePlayer == null || energyType == null) {
            throw new IllegalArgumentException("BattlePlayer or EnergyType cannot be null.");
        }

        return battlePlayer.getEnergy().getOrDefault(energyType, 0);
    }
}
