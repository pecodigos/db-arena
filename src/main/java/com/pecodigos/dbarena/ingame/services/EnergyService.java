package com.pecodigos.dbarena.ingame.services;

import com.pecodigos.dbarena.ingame.enums.energy.EnergyType;
import com.pecodigos.dbarena.ingame.models.BattlePlayer;

public class EnergyService {

    public void addEnergy(BattlePlayer battlePlayer, EnergyType energyType, int amount) {
        this.energy.put(energyType, this.energy.getOrDefault(energyType, 0) + amount);
    }

    public void subtractEnergy(EnergyType energyType, int amount) {
        int current = this.energy.getOrDefault(energyType, 0);
        if (current >= amount) {
            this.energy.put(energyType, current - amount);
        } else {
            throw new IllegalStateException("Not enough energy");
        }
    }

    public int getEnergy(EnergyType energyType) {
        return this.energy.getOrDefault(energyType, 0);
    }
}
