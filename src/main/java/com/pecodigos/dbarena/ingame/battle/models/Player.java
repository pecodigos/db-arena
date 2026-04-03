package com.pecodigos.dbarena.ingame.battle.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pecodigos.dbarena.ingame.battle.services.EnergyService;
import com.pecodigos.dbarena.ingame.entities.Character;
import com.pecodigos.dbarena.ingame.enums.energy.EnergyType;
import com.pecodigos.dbarena.ingame.enums.skills.DamageType;
import com.pecodigos.dbarena.ingame.enums.skills.EffectType;
import com.pecodigos.dbarena.user.dtos.PublicProfileDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.*;

@Data
@JsonIgnoreProperties({"random", "username"})
public class Player {
    private static final String PICCOLO_PROTECT_ABILITY_NAME = "Protect an Ally";
    private static final EnergyType[] GENERATABLE_ENERGY_TYPES = {
            EnergyType.COMBAT,
            EnergyType.KI,
            EnergyType.BLOODLINE,
            EnergyType.TECHNIQUE
    };

    private final PublicProfileDTO userProfile;
    private Map<EnergyType, Integer> energyPool;
    private Fighter[] team;
    private boolean isFirstTurn;
    @JsonIgnore
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

    public void grantInitialEnergy(int amount) {
        generateRandomEnergy(Math.max(0, amount));
    }

    public void generateEnergy() {
        int energyToGenerate = (int) Arrays.stream(team).filter(Fighter::isAlive).count();
        generateRandomEnergy(energyToGenerate);
        isFirstTurn = false;
    }

    private void generateRandomEnergy(int energyToGenerate) {
        for (int i = 0; i < energyToGenerate; i++) {
            EnergyType randomEnergy = GENERATABLE_ENERGY_TYPES[random.nextInt(GENERATABLE_ENERGY_TYPES.length)];
            energyPool.put(randomEnergy, energyPool.getOrDefault(randomEnergy, 0) + 1);
        }
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

    public void useHelpfulSkill(Fighter[] targetCharacters, Skill skill, java.util.Map<com.pecodigos.dbarena.ingame.enums.energy.EnergyType, Integer> anyEnergyChoices) {
        if (!skill.isAvailable()) {
            throw new IllegalStateException("Skill on cooldown.");
        }
        if (Boolean.TRUE.equals(skill.getAbility().getIsHarmful())) return;

        if (!EnergyService.hasEnoughEnergy(this.energyPool, skill.getAbility().getCost())) {
            throw new IllegalStateException("Not enough energy to perform this skill");
        }

        EnergyService.consumeEnergy(this.energyPool, skill.getAbility().getCost(), anyEnergyChoices);

        var skillEffectType = skill.getAbility().getEffectType();
        var skillHeal = skill.getAbility().getHelpingPoints();
        var skillCooldown = skill.getAbility().getCooldown();

        switch (skillEffectType) {
            case HEAL:
                for (Fighter targetCharacter : targetCharacters) {
                    if (targetCharacter == null || !targetCharacter.isAlive()) continue;
                    if (skillHeal > 0) {
                        targetCharacter.setCurrentHp(targetCharacter.getCurrentHp() + skillHeal);
                    }
                }
                break;
            case DESTRUCTABLE_DEFENSE:
                for (Fighter targetCharacter : targetCharacters) {
                    if (targetCharacter == null || !targetCharacter.isAlive()) continue;
                    targetCharacter.setCurrentDestructibleDefense(targetCharacter.getCurrentDestructibleDefense() + skillHeal);
                }
                break;
            case DAMAGE_REDUCTION:
                for (Fighter targetCharacter : targetCharacters) {
                    if (targetCharacter == null || !targetCharacter.isAlive()) continue;
                    targetCharacter.setCurrentDamageReduction(targetCharacter.getCurrentDamageReduction() + skillHeal);
                    targetCharacter.setDamageReductionTurns(skill.getAbility().getDurationInTurns());
                }
                break;
            case INVULNERABLE:
                for (Fighter targetCharacter : targetCharacters) {
                    if (targetCharacter == null || !targetCharacter.isAlive()) continue;
                    targetCharacter.setInvulnerable(true);
                    targetCharacter.setInvulnerableTurns(skill.getAbility().getDurationInTurns());
                }
                break;
            case PREPARATION:
                for (Fighter targetCharacter : targetCharacters) {
                    if (targetCharacter == null || !targetCharacter.isAlive()) continue;
                    if (skill.getAbility().getBonusDamage() != null && skill.getAbility().getBonusDamage() > 0) {
                        targetCharacter.setCurrentBonusDamage(targetCharacter.getCurrentBonusDamage() + skill.getAbility().getBonusDamage());
                        targetCharacter.setBonusDamageTurns(skill.getAbility().getDurationInTurns());
                    }
                    if (skill.getAbility().getDamageReduction() != null && skill.getAbility().getDamageReduction() > 0) {
                        targetCharacter.setCurrentDamageReduction(targetCharacter.getCurrentDamageReduction() + skill.getAbility().getDamageReduction());
                        targetCharacter.setDamageReductionTurns(skill.getAbility().getDurationInTurns());
                    }
                }
                break;
            default:
                break;
        }

        registerActiveEffectOnTargets(targetCharacters, skill, false, findCasterForSkill(skill));
        skill.setCurrentCooldown(skillCooldown);
    }

    public void useHarmfulSkill(
            Fighter attacker,
            Fighter[] targetCharacters,
            Skill skill,
            java.util.Map<com.pecodigos.dbarena.ingame.enums.energy.EnergyType, Integer> anyEnergyChoices,
            Player defendingPlayer
    ) {
        if (!skill.isAvailable()) {
            throw new IllegalStateException("Skill on cooldown.");
        }
        if (Boolean.FALSE.equals(skill.getAbility().getIsHarmful())) return;

        if (!EnergyService.hasEnoughEnergy(this.energyPool, skill.getAbility().getCost())) {
            throw new IllegalStateException("Not enough energy to perform this skill");
        }

        EnergyService.consumeEnergy(this.energyPool, skill.getAbility().getCost(), anyEnergyChoices);

        int skillDamage = Optional.ofNullable(skill.getAbility().getDamage()).orElse(0);
        var increasedDamage = attacker.getCurrentBonusDamage() != null ? attacker.getCurrentBonusDamage() : 0;
        var weakness = attacker.getWeaknessAmount() != null ? attacker.getWeaknessAmount() : 0;
        var skillEffectType = skill.getAbility().getEffectType();
        var skillDamageType = skill.getAbility().getDamageType();
        
        boolean shouldStunConditionally = false;
        if (skill.getAbility().getStunIfHasActiveEffect() != null && attacker.getActiveEffects() != null) {
            String requiredActiveEffect = skill.getAbility().getStunIfHasActiveEffect();
            for (ActiveEffect effect : attacker.getActiveEffects()) {
                if (effect.getName().equals(requiredActiveEffect)) {
                    shouldStunConditionally = true;
                    break;
                }
            }
        }

        switch (skillEffectType) {
            case STUN:
                for (Fighter targetCharacter : targetCharacters) {
                    if (targetCharacter == null || !targetCharacter.isAlive() || targetCharacter.isInvulnerable()) {
                        continue;
                    }
                    targetCharacter.setStunned(true);
                    targetCharacter.setStunTurns(Math.max(1, skill.getAbility().getDurationInTurns()));
                }
                break;
            case WEAKEN:
                for (Fighter targetCharacter : targetCharacters) {
                    if (targetCharacter == null || !targetCharacter.isAlive() || targetCharacter.isInvulnerable()) {
                        continue;
                    }
                    targetCharacter.setWeaknessAmount(skill.getAbility().getHelpingPoints());
                    targetCharacter.setWeaknessTurns(skill.getAbility().getDurationInTurns());
                }
                break;
            default:
                break;
        }

        if (shouldStunConditionally) {
            for (Fighter targetCharacter : targetCharacters) {
                if (targetCharacter == null || !targetCharacter.isAlive() || targetCharacter.isInvulnerable()) {
                    continue;
                }
                targetCharacter.setStunned(true);
                targetCharacter.setStunTurns(Math.max(1, skill.getAbility().getDurationInTurns()));
            }
        }

        switch (skillDamageType) {
            case NONE:
                break;
            case FLAT:
                for (int i = 0; i < targetCharacters.length; i++) {
                    Fighter targetCharacter = targetCharacters[i];
                    int damage = resolveDamageForTarget(skill, skillDamage, increasedDamage, weakness, i);
                    applyResolvedDamage(targetCharacter, damage, skillDamageType, defendingPlayer);
                }
                break;
            case AFFLICTION:
                for (int i = 0; i < targetCharacters.length; i++) {
                    Fighter targetCharacter = targetCharacters[i];
                    int damage = resolveDamageForTarget(skill, skillDamage, increasedDamage, weakness, i);
                    applyResolvedDamage(targetCharacter, damage, skillDamageType, defendingPlayer);
                }
                break;
            case PIERCING:
                for (int i = 0; i < targetCharacters.length; i++) {
                    Fighter targetCharacter = targetCharacters[i];
                    int damage = resolveDamageForTarget(skill, skillDamage, increasedDamage, weakness, i);
                    applyResolvedDamage(targetCharacter, damage, skillDamageType, defendingPlayer);
                }
                break;
            default:
                throw new IllegalStateException("No type damage skill. Some bug has probably occur. Report this.");
        }

        registerActiveEffectOnTargets(targetCharacters, skill, true, attacker);
        var skillCooldown = skill.getAbility().getCooldown();
        skill.setCurrentCooldown(skillCooldown);
    }

    private int resolveDamageForTarget(Skill skill, int primaryDamage, int bonusDamage, int weaknessAmount, int targetIndex) {
        int baseDamage = primaryDamage;
        if (targetIndex > 0 && skill.getAbility().getSecondaryDamage() != null) {
            baseDamage = skill.getAbility().getSecondaryDamage();
        }

        return Math.max(0, baseDamage + bonusDamage - weaknessAmount);
    }

    private void applyResolvedDamage(Fighter targetCharacter, int damage, DamageType damageType, Player defendingPlayer) {
        if (targetCharacter == null || !targetCharacter.isAlive() || targetCharacter.isInvulnerable() || damage <= 0) {
            return;
        }

        int hpDamage = switch (damageType) {
            case NONE -> 0;
            case AFFLICTION -> damage;
            case FLAT -> {
                int destructibleDefense = Optional.ofNullable(targetCharacter.getCurrentDestructibleDefense()).orElse(0);
                int damageReduction = Optional.ofNullable(targetCharacter.getCurrentDamageReduction()).orElse(0);
                subtractDestructiveDefense(damage, targetCharacter);
                yield Math.max(0, damage - destructibleDefense - damageReduction);
            }
            case PIERCING -> {
                int destructibleDefense = Optional.ofNullable(targetCharacter.getCurrentDestructibleDefense()).orElse(0);
                subtractDestructiveDefense(damage, targetCharacter);
                yield Math.max(0, damage - destructibleDefense);
            }
        };

        if (hpDamage <= 0) {
            return;
        }

        if (redirectProtectedDamage(targetCharacter, hpDamage, defendingPlayer)) {
            return;
        }

        applyDirectHpDamage(targetCharacter, hpDamage);
    }

    private boolean redirectProtectedDamage(Fighter targetCharacter, int hpDamage, Player defendingPlayer) {
        Fighter protector = findProtectingCaster(targetCharacter, defendingPlayer);
        if (protector == null || protector == targetCharacter) {
            return false;
        }

        int transferredDamage = hpDamage / 2;
        int remainingDamage = hpDamage - transferredDamage;

        applyDirectHpDamage(targetCharacter, remainingDamage);
        if (transferredDamage > 0) {
            applyDirectHpDamage(protector, transferredDamage);
        }

        return true;
    }

    private Fighter findProtectingCaster(Fighter protectedTarget, Player defendingPlayer) {
        if (protectedTarget == null || defendingPlayer == null || defendingPlayer.getTeam() == null || protectedTarget.getActiveEffects() == null) {
            return null;
        }

        for (ActiveEffect activeEffect : protectedTarget.getActiveEffects()) {
            if (activeEffect == null || !PICCOLO_PROTECT_ABILITY_NAME.equals(activeEffect.getName())) {
                continue;
            }

            String casterCharacterName = activeEffect.getCasterCharacterName();
            if (casterCharacterName == null || casterCharacterName.isBlank()) {
                continue;
            }

            for (Fighter alliedFighter : defendingPlayer.getTeam()) {
                if (alliedFighter == null || !alliedFighter.isAlive() || alliedFighter == protectedTarget || alliedFighter.getCharacter() == null) {
                    continue;
                }

                if (casterCharacterName.equals(alliedFighter.getCharacter().getName())) {
                    return alliedFighter;
                }
            }
        }

        return null;
    }

    private void applyDirectHpDamage(Fighter fighter, int hpDamage) {
        if (fighter == null || !fighter.isAlive() || hpDamage <= 0) {
            return;
        }

        fighter.setCurrentHp(Math.max(0, Optional.ofNullable(fighter.getCurrentHp()).orElse(0) - hpDamage));
        if (fighter.getCurrentHp() <= 0) {
            fighter.setAlive(false);
        }
    }

    private void subtractDestructiveDefense(int damage, Fighter targetCharacter) {
        targetCharacter.setCurrentDestructibleDefense(Math.max(targetCharacter.getCurrentDestructibleDefense() - damage, 0));
    }

    public void reduceCooldowns() {
        for (Fighter fighter : team) {
            if (fighter == null) {
                continue;
            }
            fighter.consumeStunTurn();
            fighter.tickEndOfTurn();
            Skill[] skills = fighter.getSkills();
            for (Skill skill : skills) {
                skill.setCurrentCooldown(Math.max(0, skill.getCurrentCooldown() - 1));
            }
        }
    }

    public void prepareForTurn() {
        for (Fighter fighter : team) {
            if (fighter == null) {
                continue;
            }
            fighter.tickStartOfTurn();
        }
    }

    private void registerActiveEffectOnTargets(Fighter[] targets, Skill skill, boolean harmful, Fighter caster) {
        if (targets == null || targets.length == 0 || !shouldTrackAsOngoingEffect(skill)) {
            return;
        }

        var ability = skill.getAbility();
        int durationInTurns = Math.max(1, Optional.ofNullable(ability.getDurationInTurns()).orElse(1));
        if (ability.getEffectType() == EffectType.DOT && ability.getDamageType() != DamageType.NONE) {
            durationInTurns = Math.max(1, durationInTurns - 1);
        }
        String casterCharacterName = Optional.ofNullable(caster)
                .map(Fighter::getCharacter)
                .map(Character::getName)
                .orElse(null);

        for (Fighter target : targets) {
            if (target == null) {
                continue;
            }

            List<ActiveEffect> activeEffects = Optional.ofNullable(target.getActiveEffects())
                    .map(ArrayList::new)
                    .orElseGet(ArrayList::new);

            activeEffects.removeIf(effect -> Objects.equals(effect.getName(), ability.getName()));
            activeEffects.add(new ActiveEffect(
                    ability.getName(),
                    ability.getDescription(),
                    ability.getImagePath(),
                    durationInTurns,
                    harmful,
                    Boolean.TRUE.equals(ability.getIsInvisible()),
                    this.getUserProfile().username(),
                    casterCharacterName,
                    ability.getEffectType(),
                    ability.getDamageType(),
                    ability.getDamage()
            ));

            target.setActiveEffects(activeEffects);
        }
    }

    private Fighter findCasterForSkill(Skill skill) {
        if (skill == null || team == null) {
            return null;
        }

        for (Fighter fighter : team) {
            if (fighter == null || fighter.getSkills() == null) {
                continue;
            }

            for (Skill fighterSkill : fighter.getSkills()) {
                if (fighterSkill == skill) {
                    return fighter;
                }

                if (fighterSkill != null
                        && fighterSkill.getAbility() != null
                        && skill.getAbility() != null
                        && Objects.equals(fighterSkill.getAbility().getName(), skill.getAbility().getName())) {
                    return fighter;
                }
            }
        }

        return null;
    }

    private boolean shouldTrackAsOngoingEffect(Skill skill) {
        if (skill == null || skill.getAbility() == null) {
            return false;
        }

        var ability = skill.getAbility();
        int durationInTurns = Optional.ofNullable(ability.getDurationInTurns()).orElse(1);
        EffectType effectType = Optional.ofNullable(ability.getEffectType()).orElse(EffectType.NONE);

        if (durationInTurns > 1) {
            return true;
        }

        return switch (effectType) {
            case STUN,
                 REFLECT,
                 ENERGY_REMOVAL,
                 ENERGY_DRAIN,
                 DOT,
                 PREPARATION,
                 INVULNERABLE,
                 HEAL,
                 DAMAGE_REDUCTION,
                 DESTRUCTABLE_DEFENSE,
                 INCREASE_DAMAGE,
                 WEAKEN -> true;
            default -> false;
        };
    }
}
