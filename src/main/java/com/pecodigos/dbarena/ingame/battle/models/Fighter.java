package com.pecodigos.dbarena.ingame.battle.models;

import com.pecodigos.dbarena.ingame.entities.Character;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Fighter {
    private Character character;
    private Skill[] skills;
    private Integer currentHp;
    private static final Integer MAX_HP = 100;
    private Integer currentDestructibleDefense;
    private Integer currentDamageReduction;
    private Integer currentBonusDamage;
    private boolean isStunned;
    private boolean isUnableToBecomeInvulnerable;
    private boolean isInvulnerable;
    private boolean isAlive;
}
