package com.pecodigos.dbarena.ingame.battle.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pecodigos.dbarena.ingame.entities.Ability;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Skill {
    private Ability ability;
    private int currentCooldown;

    public boolean isAvailable() {
        return currentCooldown <= 0;
    }
}
