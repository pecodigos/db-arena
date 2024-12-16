package com.pecodigos.dbarena.ingame.battle.models;

import com.pecodigos.dbarena.ingame.enums.energy.EnergyType;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Data
@RequiredArgsConstructor
public class Player {
    private Map<EnergyType, Integer> energyPool = new HashMap<>();
    private Fighter[] team = new Fighter[3];
    private boolean isFirstTurn;

    public void generateEnergy() {
        int energyToGenerate = isFirstTurn ? 1 : (int) Arrays.stream(team).filter(Fighter::isAlive).count();
        for (int i = 0; i < energyToGenerate; i++) {
            EnergyType randomEnergy = EnergyType.values()[new Random().nextInt(EnergyType.values().length)];
            energyPool.put(randomEnergy, energyPool.getOrDefault(randomEnergy, 0) + 1);
        }

        isFirstTurn = false;
    }

    public void useSkill(int sourceIndex, int targetIndex, String skillName) {
        var fighter = team[sourceIndex];
        fighter.useSkill(skillName);
    }

    public void energyDrained() {
        if (!energyPool.isEmpty()) {
            EnergyType randomEnergy = (EnergyType) energyPool.keySet().toArray()[new Random().nextInt(energyPool.size())];

            int currrentEnergyCount = energyPool.get(randomEnergy);
            if (currrentEnergyCount > 0) {
                energyPool.put(randomEnergy, currrentEnergyCount - 1);
            }
        }
    }


    public void reduceCooldowns() {
        for (Fighter fighter : team) {
            Skill[] skills = fighter.getSkills();
            for (Skill skill : skills) {
                skill.setCurrentCooldown(skill.getCurrentCooldown() - 1);
            }
        }
    }
}
