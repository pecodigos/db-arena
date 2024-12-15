package com.pecodigos.dbarena.ingame.battle.models;

import com.pecodigos.dbarena.ingame.entities.Ability;
import lombok.Data;

@Data
public class Skill {
    private Ability ability;
    private int currentCooldown;

    public boolean isAvailable() {
        return currentCooldown <= 0;
    }

    public void reduceCooldown() {
        if (currentCooldown > 0) {
            currentCooldown--;
        }
    }

    public void useSkill() {
        if (!isAvailable()) {
            throw new IllegalStateException("Skill on cooldown.");
        }

        this.currentCooldown = ability.getCooldown();
    }
}
