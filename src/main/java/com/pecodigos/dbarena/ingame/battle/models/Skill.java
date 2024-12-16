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


}
