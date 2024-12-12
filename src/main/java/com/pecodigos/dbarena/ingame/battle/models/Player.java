package com.pecodigos.dbarena.ingame.battle.models;

import com.pecodigos.dbarena.ingame.enums.energy.EnergyType;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

@Data
@RequiredArgsConstructor
public class Player {
    private String username;
    private Map<EnergyType, Integer> energyPool;
    private Fighter[] team = new Fighter[3];
    private boolean isFirstTurn;

    private Random random = new Random();

    public void generateEnergy() {
        int energyToGenerate = isFirstTurn ? 1 : (int) Arrays.stream(team).filter(Fighter::isAlive).count();

        for (int i = 0; i < energyToGenerate; i++) {
            EnergyType randomEnergy = EnergyType.values()[this.random.nextInt(EnergyType.values().length)];
            energyPool.put(randomEnergy, energyPool.getOrDefault(randomEnergy, 0) + 1);
        }

        isFirstTurn = false;
    }

    public boolean hasEnoughEnergy(EnergyType type, int requiredAmount) {
        return energyPool.getOrDefault(type, 0) >= requiredAmount;
    }

    public void spendEnergy(EnergyType type, int amount) {
        if (hasEnoughEnergy(type, amount)) {
            energyPool.put(type, energyPool.get(type) - amount);
        } else {
            throw new IllegalStateException("Not enough energy.");
        }
    }

    public void turnEnergy() {
        energyPool.forEach((type, amount) -> energyPool.put(type, amount + 1));
    }
}
