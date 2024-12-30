package com.pecodigos.dbarena.ingame.battle.services;

import com.pecodigos.dbarena.ingame.battle.models.Fighter;
import com.pecodigos.dbarena.ingame.entities.AbilityCost;
import com.pecodigos.dbarena.ingame.enums.energy.EnergyType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class EnergyService {

    private EnergyService() {}

    public static boolean doesNotHaveEnoughEnergy(Map<EnergyType, Integer> playerEnergy, List<AbilityCost> abilityCosts) {
        if (abilityCosts == null || abilityCosts.isEmpty()) {
            return true;
        }

        for (AbilityCost cost : abilityCosts) {
            EnergyType requiredType = cost.getEnergyType();
            int requiredAmount = cost.getAmount();

            int playerAmount = playerEnergy.getOrDefault(requiredType, 0);

            if (playerAmount < requiredAmount) {
                return false;
            }
        }

        return true;
    }


    public static void consumeEnergy(Map<EnergyType, Integer> playerEnergy, List<AbilityCost> abilityCosts) {
        if (!doesntHaveEnoughEnergy(playerEnergy, abilityCosts)) {
            throw new IllegalStateException("Not enough energy to perform this skill");
        }

        for (AbilityCost cost : abilityCosts) {
            EnergyType type = cost.getEnergyType();
            int amount = cost.getAmount();

            playerEnergy.compute(type, (k, currentAmount) -> (currentAmount == null ? 0 : currentAmount) - amount);

            if (playerEnergy.get(type) <= 0) {
                playerEnergy.remove(type);
            }
        }
    }

    public static void getRandomEnergy(Map<EnergyType, Integer> playerEnergy, Fighter fighter) {
        if (fighter.isAlive()) {
            EnergyType randomEnergy = EnergyType.values()[ThreadLocalRandom.current().nextInt(EnergyType.values().length)];
            playerEnergy.put(randomEnergy, playerEnergy.getOrDefault(randomEnergy, 0) + 1);
        }
    }
}
