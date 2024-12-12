package com.pecodigos.dbarena.ingame.battle.models;

import com.pecodigos.dbarena.ingame.entities.Character;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Fighter {
    private Character character;
    private Integer currentHp;
    private static final Integer MAX_HP = 100;
    private Integer currentDestructibleDefense;
    private Integer currentDamageReduction;
    private Integer currentBonusDamage;
    private boolean isStunned;
    private boolean isUnableToBecomeInvulnerable;
    private boolean isInvulnerable;
    private boolean isAlive;

    public void takeDamage(int damage) {
        if (isInvulnerable) return;

        this.currentHp -= damage;

        if (this.currentHp <= 0) {
            this.isAlive = false;
        }
    }

    public void stun() {
        this.isStunned = true;
    }

    public void endStun() {
        this.isStunned = false;
    }

    public void takeHeal(int heal) {
        if (!isAlive) return;

        this.currentHp += heal;
    }
}
