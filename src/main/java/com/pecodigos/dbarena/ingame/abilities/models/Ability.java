package com.pecodigos.dbarena.ingame.abilities.models;

import com.pecodigos.dbarena.ingame.enums.EnergyType;
import com.pecodigos.dbarena.ingame.enums.skills.Distance;
import com.pecodigos.dbarena.ingame.enums.skills.PersistentType;
import com.pecodigos.dbarena.ingame.enums.skills.SkillType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ability {

    private String name;
    private String description;
    private Integer damage;
    private Integer cooldown;
    private Map<EnergyType, Integer> cost;
    private Distance distance;
    private SkillType skillType;
    private PersistentType persistentType;
    private Integer numberOfTurns;
    private boolean isUnique;
    private String pathToImg;
}
