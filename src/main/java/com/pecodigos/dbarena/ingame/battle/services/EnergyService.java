package com.pecodigos.dbarena.ingame.battle.services;

import com.pecodigos.dbarena.ingame.battle.models.Fighter;
import com.pecodigos.dbarena.ingame.entities.AbilityCost;
import com.pecodigos.dbarena.ingame.enums.energy.EnergyType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.HashMap;

@Service
public class EnergyService {

    private static final EnergyType[] GENERATABLE_ENERGY_TYPES = { EnergyType.COMBAT, EnergyType.KI, EnergyType.BLOODLINE, EnergyType.TECHNIQUE };

    private EnergyService() {}

    public static boolean hasEnoughEnergy(Map<EnergyType, Integer> playerEnergy, List<AbilityCost> abilityCosts) {
        if (abilityCosts == null || abilityCosts.isEmpty()) {
            return true;
        }

        int anyRequired = 0;
        Map<EnergyType, Integer> specificCosts = new HashMap<>();

        for (AbilityCost cost : abilityCosts) {
            EnergyType type = cost.getEnergyType();
            int amount = cost.getAmount();

            if (type == EnergyType.NONE || amount <= 0) continue;

            if (type == EnergyType.ANY) {
                anyRequired += amount;
                continue;
            }

            int available = playerEnergy.getOrDefault(type, 0);
            if (available < amount) {
                return false;
            }
            specificCosts.merge(type, amount, Integer::sum);
        }

        int leftoverEnergy = 0;
        for (EnergyType type : GENERATABLE_ENERGY_TYPES) {
            int available = playerEnergy.getOrDefault(type, 0);
            int reserved = specificCosts.getOrDefault(type, 0);
            leftoverEnergy += Math.max(0, available - reserved);
        }

        return leftoverEnergy >= anyRequired;
    }

    public static void consumeEnergy(Map<EnergyType, Integer> playerEnergy, List<AbilityCost> abilityCosts, Map<EnergyType, Integer> anyEnergyChoices) {
        if (!hasEnoughEnergy(playerEnergy, abilityCosts)) {
            throw new IllegalStateException("Not enough energy to perform this skill");
        }

        int unfulfilledAny = 0;

        for (AbilityCost cost : abilityCosts) {
            EnergyType type = cost.getEnergyType();
            int amount = cost.getAmount();

            if (type == EnergyType.NONE || amount <= 0) {
                continue;
            }

            if (type == EnergyType.ANY) {
                unfulfilledAny += amount;
                continue;
            }

            playerEnergy.compute(type, (k, currentAmount) -> (currentAmount == null ? 0 : currentAmount) - amount);

            if (playerEnergy.get(type) <= 0) {
                playerEnergy.remove(type);
            }
        }
        
        if (unfulfilledAny > 0) {
            if (anyEnergyChoices != null && !anyEnergyChoices.isEmpty()) {
                for (Map.Entry<EnergyType, Integer> entry : anyEnergyChoices.entrySet()) {
                    if (unfulfilledAny <= 0) break;
                    EnergyType type = entry.getKey();
                    int amount = entry.getValue();
                    if (amount > 0) {
                        int available = playerEnergy.getOrDefault(type, 0);
                        int toConsume = Math.min(amount, Math.min(available, unfulfilledAny));
                        
                        if (toConsume > 0) {
                            playerEnergy.compute(type, (k, currentAmount) -> currentAmount - toConsume);
                            if (playerEnergy.get(type) <= 0) playerEnergy.remove(type);
                            unfulfilledAny -= toConsume;
                        }
                    }
                }
            }
            if (unfulfilledAny > 0) {
                consumeAnyEnergy(playerEnergy, unfulfilledAny);
            }
        }
    }

    private static void consumeAnyEnergy(Map<EnergyType, Integer> playerEnergy, int amount) {
        int remaining = amount;
        for (EnergyType type : GENERATABLE_ENERGY_TYPES) {
            if (remaining == 0) break;
            int available = playerEnergy.getOrDefault(type, 0);
            if (available > 0) {
                int toConsume = Math.min(available, remaining);
                playerEnergy.put(type, available - toConsume);
                remaining -= toConsume;
                if (playerEnergy.get(type) <= 0) playerEnergy.remove(type);
            }
        }
    }

    public static void getRandomEnergy(Map<EnergyType, Integer> playerEnergy, Fighter fighter) {
        if (fighter.isAlive()) {
            EnergyType randomEnergy = GENERATABLE_ENERGY_TYPES[ThreadLocalRandom.current().nextInt(GENERATABLE_ENERGY_TYPES.length)];
            playerEnergy.put(randomEnergy, playerEnergy.getOrDefault(randomEnergy, 0) + 1);
        }
    }
}
