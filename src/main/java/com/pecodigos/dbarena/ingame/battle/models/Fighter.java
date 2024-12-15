package com.pecodigos.dbarena.ingame.battle.models;

import com.pecodigos.dbarena.ingame.entities.Character;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;


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

    public boolean isSkillAvailable(String skillName) {
        return Arrays.stream(skills)
                .filter(skill -> skill.getAbility().getName().equals(skillName))
                .anyMatch(Skill::isAvailable);
    }

    public void useSkill(String skillName) {
        var skill = Arrays.stream(skills)
                .filter(s -> s.getAbility().getName().equals(skillName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));

        skill.useSkill();
    }

    public void reduceCooldowns() {
        Arrays.stream(skills).forEach(Skill::reduceCooldown);
    }

    public void takeDamage(int damage) {
        if (!isAlive) return;
        if (isInvulnerable) return;

        this.currentHp -= damage;

        if (this.currentHp <= 0) {
            this.isAlive = false;
        }
    }

    public void endStun() {
        this.isStunned = false;
    }

    public void takeHeal(int heal) {
        if (!isAlive) return;

        this.currentHp += heal;
    }
}
