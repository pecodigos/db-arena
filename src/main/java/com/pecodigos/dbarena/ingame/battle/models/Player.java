package com.pecodigos.dbarena.ingame.battle.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pecodigos.dbarena.ingame.battle.services.EnergyService;
import com.pecodigos.dbarena.ingame.enums.energy.EnergyType;
import com.pecodigos.dbarena.user.dtos.PublicProfileDTO;
import lombok.Data;

import java.util.*;

@Data
public class Player {
    private final PublicProfileDTO userProfile;
    private Map<EnergyType, Integer> energyPool;
    private Fighter[] team;
    private boolean isFirstTurn;
    private Random random = new Random();

    @JsonCreator
    public Player(@JsonProperty("userProfile") PublicProfileDTO userProfile) {
        this.userProfile = userProfile;
        this.energyPool = new EnumMap<>(EnergyType.class);
        this.team = new Fighter[3];
        this.isFirstTurn = false;
    }

    public String getUsername() {
        return userProfile.username();
    }

    public void generateEnergy() {
        int energyToGenerate = isFirstTurn ? 1 : (int) Arrays.stream(team).filter(Fighter::isAlive).count();
        for (int i = 0; i < energyToGenerate; i++) {
            EnergyType randomEnergy = EnergyType.values()[random.nextInt(EnergyType.values().length)];
            energyPool.put(randomEnergy, energyPool.getOrDefault(randomEnergy, 0) + 1);
        }

        isFirstTurn = false;
    }

    public void energyDrained() {
        if (!energyPool.isEmpty()) {

            EnergyType randomEnergy = (EnergyType) energyPool.keySet().toArray()[random.nextInt(energyPool.size())];

            int currentEnergyCount = energyPool.get(randomEnergy);
            if (currentEnergyCount > 0) {
                energyPool.put(randomEnergy, currentEnergyCount - 1);
            }
        }
    }

    public void useHelpfulSkill(Fighter[] targetCharacters, Skill skill) {
        if (!skill.isAvailable()) {
            throw new IllegalStateException("Skill on cooldown.");
        }
        if (Boolean.TRUE.equals(skill.getAbility().getIsHarmful())) return;

        if (EnergyService.doesNotHaveEnoughEnergy(this.energyPool, skill.getAbility().getCost())) {
            throw new IllegalStateException("Not enough energy to perform this skill");
        }

        EnergyService.consumeEnergy(this.energyPool, skill.getAbility().getCost());

        var skillEffectType = skill.getAbility().getEffectType();
        var skillHeal = skill.getAbility().getHelpingPoints();
        var skillCooldown = skill.getAbility().getCooldown();

        switch (skillEffectType) {
            case HEAL:
                for (Fighter targetCharacter : targetCharacters) {
                    if (skillHeal > 0) {
                        targetCharacter.setCurrentHp(targetCharacter.getCurrentHp() + skillHeal);
                    }
                }
                break;
            case DESTRUCTABLE_DEFENSE:
                for (Fighter targetCharacter : targetCharacters) {
                    targetCharacter.setCurrentDestructibleDefense(targetCharacter.getCurrentDestructibleDefense() + skillHeal);
                }
                break;
            case DAMAGE_REDUCTION:
                for (Fighter targetCharacter : targetCharacters) {
                    targetCharacter.setCurrentDamageReduction(targetCharacter.getCurrentDamageReduction() + skillHeal);
                }
                break;
            case INVULNERABLE:
                for (Fighter targetCharacter : targetCharacters) {
                    targetCharacter.setInvulnerable(true);
                }
                break;
            default:
                break;
        }
        skill.setCurrentCooldown(skillCooldown);
    }

    public void useHarmfulSkill(Fighter attacker, Fighter[] targetCharacters, Skill skill) {
        if (!skill.isAvailable()) {
            throw new IllegalStateException("Skill on cooldown.");
        }
        if (Boolean.FALSE.equals(skill.getAbility().getIsHarmful())) return;

        var skillDamage = skill.getAbility().getDamage();
        var increasedDamage = attacker.getCurrentBonusDamage();
        var damage = skillDamage + increasedDamage;
        var skillEffectType = skill.getAbility().getEffectType();
        var skillDamageType = skill.getAbility().getDamageType();
        int damageDealt;

        switch (skillEffectType) {
            case STUN:
                for (Fighter targetCharacter : targetCharacters) {
                    targetCharacter.setStunned(true);
                }
                break;
            default:
                break;
        }

        switch (skillDamageType) {
            case NONE:
                break;
            case FLAT:
                for (Fighter targetCharacter : targetCharacters) {
                    damageDealt = damage - (targetCharacter.getCurrentDestructibleDefense() + targetCharacter.getCurrentDamageReduction());
                    subtractDestructiveDefense(damage, targetCharacter);

                    if (damageDealt > 0) {
                        targetCharacter.setCurrentHp(targetCharacter.getCurrentDamageReduction() - damageDealt);
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
                        targetCharacter.setCurrentHp(targetCharacter.getCurrentHp() - damage);
                        if (targetCharacter.getCurrentHp() <= 0) {
                            targetCharacter.setAlive(false);
                            break;
                        }
                    }
                }
                break;
            case PIERCING:
                for (Fighter targetCharacter : targetCharacters) {
                    damageDealt = damage - targetCharacter.getCurrentDestructibleDefense();
                    subtractDestructiveDefense(damage, targetCharacter);

                    if (damageDealt > 0) {
                        targetCharacter.setCurrentHp(targetCharacter.getCurrentHp() - damageDealt);
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


    private void subtractDestructiveDefense(int damage, Fighter targetCharacter) {
        targetCharacter.setCurrentDestructibleDefense(Math.max(targetCharacter.getCurrentDestructibleDefense() - damage, 0));
    }

    public void reduceCooldowns() {
        for (Fighter fighter : team) {
            Skill[] skills = fighter.getSkills();
            for (Skill skill : skills) {
                skill.setCurrentCooldown(skill.getCurrentCooldown() - 1);
            }
        }
    }
}
