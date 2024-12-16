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
    }

    public void takeDamage(int damage) {
        if (!isAlive) return;
        if (isInvulnerable) return;

        this.currentHp -= damage;

        if (this.currentHp <= 0) {
            this.isAlive = false;
        }
    }

    public void takeHeal(int heal) {
        if (!isAlive) return;

        this.currentHp += heal;
    }


    public void useHarmfulSkill(Fighter[] targetCharacters, Skill skill) {
        if (!skill.isAvailable()) {
            throw new IllegalStateException("Skill on cooldown.");
        }
        if (Boolean.FALSE.equals(skill.getAbility().getIsHarmful())) return;

        var increasedDamage = currentBonusDamage;
        var damage = skill.getAbility().getDamage() + increasedDamage;
        int damageDealt;

        switch (skill.getAbility().getEffectType()) {
            case STUN:
                for (Fighter targetCharacter : targetCharacters) {
                    targetCharacter.setStunned(true);
                }
                break;
            case ENERGY_DRAIN:
                for (Fighter targetCharacter : targetCharacters) {

                }
        }

        // Damage and type damage
        switch (skill.getAbility().getDamageType()) {
            case NONE:
                break;
            case FLAT:
                for (Fighter targetCharacter : targetCharacters) {
                    damageDealt = damage - (targetCharacter.currentDestructibleDefense + targetCharacter.currentDamageReduction);
                    targetCharacter.setCurrentDestructibleDefense(Math.max(targetCharacter.getCurrentDestructibleDefense() - damage, 0));

                    if (damageDealt > 0) {
                        targetCharacter.setCurrentHp(getCurrentHp() - damageDealt);
                        if (targetCharacter.getCurrentHp() <= 0) {
                            targetCharacter.setAlive(false);
                            break;
                        }
                    }
                }
                break;
            case AFFLICTION:
                for (Fighter targetCharacter : targetCharacters) {
                    if (damage > 0) {
                        targetCharacter.setCurrentHp(getCurrentHp() - damage);
                        if (targetCharacter.getCurrentHp() <= 0) {
                            targetCharacter.setAlive(false);
                            break;
                        }
                    }
                }
                break;
            case PIERCING:
                for (Fighter targetCharacter : targetCharacters) {
                    damageDealt = damage - targetCharacter.currentDestructibleDefense;
                    targetCharacter.setCurrentDestructibleDefense(Math.max(targetCharacter.getCurrentDestructibleDefense() - damage, 0));

                    if (damageDealt > 0) {
                        targetCharacter.setCurrentHp(getCurrentHp() - damageDealt);
                        if (targetCharacter.getCurrentHp() <= 0) {
                            targetCharacter.setAlive(false);
                            break;
                        }
                    }
                }
                break;
            default:
                throw new IllegalStateException("No type damage skill. Some bug has probably occur. Report this.");
        }
        var skillCooldown = skill.getAbility().getCooldown();
        skill.setCurrentCooldown(skillCooldown);
    }
}
